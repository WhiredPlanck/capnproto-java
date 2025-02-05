package org.capnproto.utils;

import java.util.List;
import java.util.Objects;

public class CollectionUtil {
    public static <T> int lastIndex(final List<T> c) {
        Objects.requireNonNull(c);
        return c.size() - 1;
    }

    public static <T> T last(final List<T> c) {
        Objects.requireNonNull(c);
        return c.get(lastIndex(c));
    }
}
