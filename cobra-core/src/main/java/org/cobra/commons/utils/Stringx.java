package org.cobra.commons.utils;

public class Stringx {

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static int fixedLength(String s) {
        int ans = Integer.BYTES; // used fixed 4-byte to write N-following bytes
        if (!isBlank(s))
            ans += s.getBytes().length;

        return ans;
    }
}
