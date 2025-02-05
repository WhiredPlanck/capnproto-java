package org.capnproto.utils;

import java.util.Objects;

public class StrUtil {
    public static String baseName(final String path) {
        final int splashPos = path.lastIndexOf('/');
        if (splashPos != -1) {
            return path.substring(splashPos + 1);
        } else {
            return path;
        }
    }

    public static String toTitleCase(final String str) {
        if (str == null || str.isEmpty()) return "";

        final StringBuilder builder = new StringBuilder(str.length());
        boolean next = true;
        for (final char ch : str.toCharArray()) {
            if (Character.isSpaceChar(ch)) {
                next = true;
            } else if (next) {
                builder.append(Character.toTitleCase(ch));
                next = false;
            } else {
                builder.append(Character.toLowerCase(ch));
            }
        }
        return builder.toString();
    }

    public static String capitalize(final String s) {
        Objects.requireNonNull(s);
        if (!s.isEmpty()) {
            final CharSequence transform;
            final char first = s.toCharArray()[0];
            if (Character.isLowerCase(first)) {
                transform = Character.toString(Character.toTitleCase(first));
            } else {
                transform = Character.toString(first);
            }
            return transform + s.substring(1);
        } else {
            return s;
        }
    }

    public static String toSnakeCase(final CharSequence s, boolean upper) {
        Objects.requireNonNull(s);
        final var builder = new StringBuilder();
        boolean firstChar = true;
        final int length = s.length();
        for (int i = 0; i < length; i++) {
            final char ch = s.charAt(i);
            if (Character.isUpperCase(ch) && !firstChar) {
                builder.append('_');
            }
            if (upper) {
                builder.append(Character.toUpperCase(ch));
            } else {
                builder.append(Character.toLowerCase(ch));
            }
            firstChar = false;
        }
        return builder.toString();
    }

    public static String subBeforeLast(final String s, final String delimiter, final String missingDefaultValue) {
        Objects.requireNonNull(s);
        final int i = s.lastIndexOf(delimiter);
        return (i == -1) ? missingDefaultValue : s.substring(0, i);
    }

    public static String subBeforeLast(final String s, final String delimiter) {
        return subBeforeLast(s, delimiter, s);
    }
}
