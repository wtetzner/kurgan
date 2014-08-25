package org.bovinegenius.kurgan.util;

public class Util {
    @SuppressWarnings("unchecked")
    public static <T> T coalesce(T... ts) {
        for (T t : ts) {
            if (t != null) return t;
        }
        return null;
    }
}
