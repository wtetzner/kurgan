package org.bovinegenius.kurgan.types;

import java.math.BigInteger;

public class Coercions {
    private static BigInteger make(String str, int radix) {
        return new BigInteger(str, radix);
    }

    public static BigInteger parseInt(String value) {
        if (value == null) {
            return null;
        } else if (value.startsWith("-")) {
            if (value.startsWith("-0x")) {
                return make("-" + value.substring(3), 16);
            } else if (value.startsWith("-0b")) {
                return make("-" + value.substring(3), 2);
            } else {
                return make(value, 10);
            }
        } else {
            if (value.startsWith("0x")) {
                return make(value.substring(2), 16);
            } else if (value.startsWith("0b")) {
                return make(value.substring(2), 2);
            } else {
                return make(value, 10);
            }
        }
    }
}
