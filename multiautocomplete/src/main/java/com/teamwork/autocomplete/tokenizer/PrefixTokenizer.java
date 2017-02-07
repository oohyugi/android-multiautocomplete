package com.teamwork.autocomplete.tokenizer;

import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.MultiAutoCompleteTextView;

import java.util.Arrays;

/**
 * Implementation of {@link android.widget.MultiAutoCompleteTextView.Tokenizer} that finds tokens which are prefixed by
 * one of the passed chars. This is useful, for example, to manage autocomplete for user handles ('@' prefix).
 * <p>
 * Only one separator is supported per tokenizer. The default is the space character, to change override
 * {@link #getDefaultSeparator}.
 * <p>
 * This implementation does not filter on the token characters (i.e. '@han:dle;' is a valid token), but only terminates
 * it when a separator is found.
 */
public class PrefixTokenizer implements MultiAutoCompleteTextView.Tokenizer {

    private final char[] prefixes;

    public PrefixTokenizer(char prefix) {
        prefixes = new char[] { prefix };
    }

    public PrefixTokenizer(char... prefixes) {
        this.prefixes = Arrays.copyOf(prefixes, prefixes.length);
    }

    @Override
    public int findTokenStart(CharSequence text, int cursor) {
        // base case: empty string
        if (text.length() == 0) {
            return 0;
        }

        // iterate back until we find the prefix
        char separator = getDefaultSeparator();
        for (int i = cursor - 1; i >= 0; i--) {
            if (matchesPrefix(text, i)) {
                if (i == 0 || text.charAt(i - 1) == separator) {
                    return i;
                }
            }
        }

        return cursor;
    }

    private boolean matchesPrefix(@NonNull CharSequence text, int index) {
        for (char prefix : prefixes) {
            if (text.charAt(index) == prefix) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int findTokenEnd(CharSequence text, int cursor) {
        // base case: empty string
        int length = text.length();
        if (length == 0) {
            return 0;
        }

        // iterate forward until we get a space or the end of the string
        char separator = getDefaultSeparator();
        for (int i = cursor; i < length; i++) {
            if (text.charAt(i) == separator) {
                return i == 0 ? 0 : i - 1;
            }
        }

        return length;
    }

    @Override
    public CharSequence terminateToken(CharSequence text) {
        int i = text.length();
        char separator = getDefaultSeparator();

        // the text is empty or already has a trailing space
        if (i == 0 || (i > 0 && text.charAt(i - 1) == separator)) {
            return text;
        }

        // append a trailing space to the string
        if (text instanceof Spanned) {
            SpannableString sp = new SpannableString(text + String.valueOf(separator));
            TextUtils.copySpansFrom((Spanned) text, 0, text.length(), Object.class, sp, 0);
            return sp;
        } else {
            return text + String.valueOf(separator);
        }
    }

    protected char getDefaultSeparator() {
        return ' ';
    }

}