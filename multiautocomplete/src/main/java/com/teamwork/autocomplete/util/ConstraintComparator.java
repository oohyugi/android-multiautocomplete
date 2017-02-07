package com.teamwork.autocomplete.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.Comparator;

/**
 * Abstract implementation of {@link Comparator} that supports an additional text constraint to implement custom
 * ordering rules when filtering objects based on a user-entered input.
 * <p>
 * Extend this class and implement your {@link #compare(CharSequence, Object, Object)} method as if you were using a
 * standard Comparator.
 * <p>
 * This class is thread safe, and subclasses should maintain this property.
 */
public abstract class ConstraintComparator<T> implements Comparator<T> {

    // GuardedBy("this")
    private CharSequence constraint;

    public synchronized final @Nullable CharSequence getConstraint() {
        return constraint;
    }

    public synchronized final void setConstraint(@Nullable CharSequence constraint) {
        this.constraint = constraint;
    }

    @Override
    @WorkerThread
    public synchronized final int compare(@NonNull T o1, @NonNull T o2) {
        return compare(constraint, o1, o2);
    }

    /**
     * Compares the two objects for order, providing the current text constraint for filtering and applying custom
     * rules to the ordering.
     *
     * @param constraint The constraint text.
     * @param o1         the first object to be compared.
     * @param o2         the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater
     * than the second.
     * @see Comparator#compare(Object, Object)
     */
    @WorkerThread
    public abstract int compare(@Nullable CharSequence constraint, @NonNull T o1, @NonNull T o2);

}