package com.teamwork.autocomplete;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.teamwork.autocomplete.adapter.NullTypeAdapterDelegate;
import com.teamwork.autocomplete.adapter.TypeAdapterDelegate;
import com.teamwork.autocomplete.util.ConstraintComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of an "adapter of adapters" that is set into the {@link android.widget.MultiAutoCompleteTextView} to
 * manage and filter multiple data sets.
 * <p>
 * The {@link Filter} component delegates returning the autocomplete filtered results to one of the registered
 * adapters.
 */
class AutoCompleteAdapter extends BaseAdapter implements Filterable {

    private final NullTypeAdapterDelegate nullTypeAdapter = new NullTypeAdapterDelegate();

    private final LayoutInflater layoutInflater;
    private final List<TypeAdapterDelegate> typeAdapters;

    private TypeAdapterDelegate currentTypeAdapter = nullTypeAdapter;
    private AutoCompleteFilter filter;

    private CharSequence currentConstraint;

    AutoCompleteAdapter(@NonNull Context context, @NonNull List<TypeAdapterDelegate> typeAdapters) {
        this.layoutInflater = LayoutInflater.from(context);
        this.typeAdapters = typeAdapters;
    }

    @Override
    public int getCount() {
        return currentTypeAdapter.getCount();
    }

    @Override
    public Object getItem(int position) {
        return currentTypeAdapter.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return currentTypeAdapter.getItemId(position);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getViewTypeCount() {
        return typeAdapters.size() /* NullTypeAdapterDelegate */ + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (currentTypeAdapter instanceof NullTypeAdapterDelegate) {
            return typeAdapters.size(); // last index + 1
        }
        return typeAdapters.indexOf(currentTypeAdapter);
    }

    @Override
    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return currentTypeAdapter.getView(layoutInflater, position, convertView, parent, currentConstraint);
    }

    @Override
    public @NonNull Filter getFilter() {
        if (filter == null) {
            filter = new AutoCompleteFilter();
        }
        return filter;
    }

    /**
     * {@link Filter} for the main adapter.
     * It checks which type adapter filter can handle the current token, and delegates the filtering to the chosen
     * adapter. The adapter data set is then set to the filtered items of the type adapter.
     *
     * @see com.teamwork.autocomplete.filter.TokenFilter
     */
    private class AutoCompleteFilter extends Filter {

        @Override
        @WorkerThread
        protected FilterResults performFiltering(CharSequence token) {
            FilterResults filterResults = new FilterResults();
            FilterResultsWrapper resultsWrapper = new FilterResultsWrapper();

            CharSequence constraint = null;
            List<Object> filteredData = new ArrayList<>();
            TypeAdapterDelegate typeAdapter;

            if (token != null) {
                // retrieve the first type adapter that supports this token
                typeAdapter = getCurrentTypeAdapter(token);
                constraint = typeAdapter.getFilter().stripHandle(token);

                // filter data based on the constraint (stripped by any handle)
                List<?> filteredList = typeAdapter.performFiltering(constraint);
                filteredData.addAll(filteredList);

                // sort filtered results if there is a custom comparator
                ConstraintComparator comparator = typeAdapter.getFilter().getConstraintComparator();
                if (comparator != null) {
                    comparator.setConstraint(constraint);
                    //noinspection unchecked
                    Collections.sort(filteredData, comparator);
                }
            } else {
                // there is no original data without a type adapter: the adapter will be empty
                typeAdapter = nullTypeAdapter;
            }

            // we pass adapter and results in a wrapper to avoid accessing the class state from the worker thread
            resultsWrapper.constraint = constraint;
            resultsWrapper.typeAdapter = typeAdapter;
            resultsWrapper.results = filteredData;
            filterResults.values = resultsWrapper;
            filterResults.count = filteredData.size();
            return filterResults;
        }

        private @NonNull TypeAdapterDelegate getCurrentTypeAdapter(@NonNull CharSequence token) {
            for (TypeAdapterDelegate typeAdapter : typeAdapters) {
                if (typeAdapter.getFilter().supportsToken(token)) {
                    return typeAdapter;
                }
            }
            return nullTypeAdapter;
        }

        @Override
        protected void publishResults(CharSequence token, FilterResults results) {
            FilterResultsWrapper resultsWrapper = (FilterResultsWrapper) results.values;
            currentConstraint = resultsWrapper.constraint;
            currentTypeAdapter = resultsWrapper.typeAdapter;

            //noinspection unchecked
            currentTypeAdapter.setFilteredItems(resultsWrapper.results);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            //noinspection unchecked
            return currentTypeAdapter.getFilter().toTokenString(resultValue);
        }
    }

    /**
     * Simple wrapper for a {@link android.widget.Filter.FilterResults#values} to post the {@link
     * TypeAdapterDelegate} to the main thread from {@link Filter#performFiltering(CharSequence)}.
     */
    private static class FilterResultsWrapper {
        CharSequence constraint;
        TypeAdapterDelegate typeAdapter;
        List<?> results;
    }

}