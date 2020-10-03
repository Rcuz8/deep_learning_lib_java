package com.ai.utils;

import java.util.*;

public class StringUtils {

    public static boolean isInt(String s) {
        return getInt(s) != null;
    }
    public static boolean isDouble(String s) {
        return getDouble(s) != null;
    }
    public static Double getDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    public static Integer getInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static List<String> split(String s, String regex) {
        s = s.replaceAll(" +", " ");
        String[] a = s.split(regex,-1);
        return new ArrayList<>(Arrays.asList(a));
    }

    public static List<String> splittrim(String s, String regex) {
        s = s.replaceAll(" +", " ");
        String[] a = s.split(regex,-1);
        List<String> strs = new ArrayList<>();
        for (String s1 : a) {
            strs.add(s1.trim());
        }
        return strs;
    }

    // Returns length of function for longest common
    // substring of X[0..m-1] and Y[0..n-1]
    static int lcs(String X, String Y, int i, int j, int count) {

        if (i == 0 || j == 0) {
            return count;
        }

        if (X.charAt(i - 1) == Y.charAt(j - 1)) {
            count = lcs(X, Y, i - 1, j - 1, count + 1);
        }
        count = Math.max(count, Math.max(lcs(X, Y, i, j - 1, 0),
                lcs(X, Y, i - 1, j, 0)));
        return count;
    }

    public static int lcs(String a, String b) {
        int n, m;
        n = a.length();
        m = b.length();
        return lcs(a,b, n, m, 0);
    }

    public static String maxlen(String a, String b) {
        return a.length() >= b.length() ? a : b;
    }
    public static String minlen(String a, String b) {
        return a.length() < b.length() ? a : b;
    }

    public static int nshared(String a, String b) {
        String max = maxlen(a,b);
        String min = minlen(a,b);
        Map<Character, Boolean> map = new HashMap<>();
        for (int i = 0; i < max.length(); i++) {
            map.putIfAbsent(max.charAt(i), true);
        }
        Map<Character, Boolean> sharedMap = new HashMap<>();
        for (int i = 0; i < min.length(); i++) {
            if (map.containsKey(min.charAt(i))) sharedMap.putIfAbsent(min.charAt(i),true);
        }
        return sharedMap.size();
    }

    public static double nearness(String a, String b) {
        double shared = (double) nshared(a,b) / maxlen(a,b).length();
        List<String> asplit = StringUtils.splittrim(a," ");
        List<String> bsplit = StringUtils.splittrim(b," ");
        List<String> joined = CollectionUtils.resultsIntersectionSet(Arrays.asList(asplit,bsplit));
        double JOIN_WEIGHT = 3.5;
        shared = shared + shared * (JOIN_WEIGHT * joined.size());
        return shared;
//        double lcs = (double) lcs(a,b) / maxlen(a,b).length();
//
//        double WEIGHT_SHARED = 0.3;
//        double WEIGHT_LCS = 1-WEIGHT_SHARED;
//
//        return shared * WEIGHT_SHARED + lcs * WEIGHT_LCS;
    }



}
