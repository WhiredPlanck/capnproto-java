package org.capnproto;

public class Utils {
    public static String baseName(final String path) {
        final int splashPos = path.lastIndexOf('/');
        if (splashPos != -1) {
            return path.substring(splashPos + 1);
        } else {
            return path;
        }
    }
}
