package com.teamwork.autocomplete.adapter;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.teamwork.autocomplete.MultiAutoComplete;
import com.teamwork.autocomplete.filter.TokenFilter;
import com.teamwork.autocomplete.view.AutoCompleteViewBinder;

import java.util.List;

/**
 * Interface for a typed adapter for the auto complete view.
 * <p>
 * Its purpose is to allow client code to set the data set for the type it represents, and optionally listen to token
 * changes within the user-typed text with an {@link OnTokensChangedListener}.
 * <p>
 * Use the static methods in the {@link Build} class to construct an instance.
 */
public interface AutoCompleteTypeAdapter<Model> {

    /**
     * Set a list of items for this type adapter.
     * Note that the list will be processed and indexed asynchronously, and used for the next filtering.
     * <p>
     * This method can be called to replace an existing data set, even after the {@link MultiAutoComplete} has been
     * created. The data set change will be notified and the filter updated accordingly.
     *
     * @param items The List of items to set into the type adapter.
     */
    @MainThread
    void setItems(@NonNull List<Model> items);

    /**
     * Set a {@link OnTokensChangedListener} to listen for changes in the matched tokens in the user-typed text.
     * <p>
     * <b>Note:</b> this method can only be called if the token filter returned by
     * {@link TokenFilter#getValidTokenPattern()} is not null, since it's required to detect the token matches within
     * the user typed string.
     *
     * @param listener The listener, or null to reset it.
     * @throws IllegalStateException when the adapter's {@link TokenFilter#getValidTokenPattern()} is null.
     * @see OnTokensChangedListener
     */
    @MainThread
    void setOnTokensChangedListener(@Nullable OnTokensChangedListener<Model> listener);

    /**
     * Static factory methods to construct an instance of {@link AutoCompleteTypeAdapter}.
     */
    class Build {

        @NonNull
        public static <M> AutoCompleteTypeAdapter<M> from(@NonNull AutoCompleteViewBinder<M> binder, @NonNull TokenFilter<M> filter) {
            //noinspection ConstantConditions
            if (binder == null || filter == null) {
                throw new IllegalArgumentException("View binder and token filter must not be null");
            }
            return new BaseTypeAdapterDelegate<>(binder, filter);
        }
    }

}