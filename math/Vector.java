package com.ai.math;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

public class Vector {

    public static double dot(Double[] X, Double[] Y) {
        if (X.length != Y.length) throw new Error("dot got diff size params!");
        double sum = 0;
        for (int i = 0; i < X.length; i++) {
            sum += (double) (  (double) X[i] *  (double) Y[i] );
        }
        return sum;
    }

    public static double dot(List<Double> X, List<Double> Y) {
        if (X.size() != Y.size()) throw new Error("dot got diff size params!");
        double sum = 0;
        for (int i = 0; i < X.size(); i++) {
            double x = (double) X.get(i);
            double y = (double) Y.get(i);
            sum += ( x * y );
        }
        return sum;
    }

    public static double diffSquared(List<Double> X, List<Double> Y) {
        if (X.size() != Y.size()) throw new Error("dot got diff size params!");
        double sum = 0;
        for (int i = 0; i < X.size(); i++) {
            double x =  X.get(i);
            double y =  Y.get(i);
            sum += Math.pow(( x - y ), 2);
        }
        return sum;
    }

    public static List<Double> diff(List<Double> X, List<Double> Y) {
        if (X.size() != Y.size()) throw new Error("dot got diff size params!");
        List<Double> list = new ArrayList<>();
        for (int i = 0; i < X.size(); i++) {
            double x =  X.get(i);
            double y =  Y.get(i);
            list.add( x - y );
        }
        return list;
    }

    public static double sum(List<Double> X) {
        double sum = 0;
        for (Double x : X) {
            sum += x;
        }
        return sum;
    }

    public static List<Double> sum(List<Double> X, List<Double> Y) {
        if (X.size() != Y.size()) throw new Error("dot got diff size params!");
        List<Double> list = new ArrayList<>();
        for (int i = 0; i < X.size(); i++) {
            double x =  X.get(i);
            double y =  Y.get(i);
            list.add( x + y );
        }
        return list;
    }

    public static List<Double> divideBy(List<Double> X, double N) {
        List<Double> list = new ArrayList<>();
        for (int i = 0; i < X.size(); i++) {
            double x =  X.get(i);
            list.add( x / N );
        }
        return list;
    }

    public static List<Double> vround5(List<Double> X) {
        List<Double> list = new ArrayList<>();
        for (int i = 0; i < X.size(); i++) {
            double x =  X.get(i);
            list.add( Utils.round5(x));
        }
        return list;
    }

    public static List<Integer> vround(List<Double> X) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < X.size(); i++) {
            double x =  X.get(i);
            int rounded = (int) Math.round(x);
            list.add( rounded );
        }
        return list;
    }

    /* ALL LISTS MUST BE SAME SIZE */
    public static List<Double> sumlist(List<List<Double>> X) {
        List<Double> sumlist = new ArrayList<>();
        // initialize sums
        while (sumlist.size() < X.get(0).size()) sumlist.add(0.0);
        for (int i = 0; i < X.size(); i++) {
            List<Double> thislist = X.get(i);
            for (int j = 0; j < thislist.size(); j++) {
                sumlist.set(j, sumlist.get(j) + thislist.get(j));
            }
        }
        return sumlist;
    }

    public static List<Double> avg(List<List<Double>> X) {
        // Get sums
        List<Double> sumlist = sumlist(X);
        // avg it out
        List<Double> avglist = new ArrayList<>();

        for (int i = 0; i < sumlist.size(); i++) {
            avglist.add(sumlist.get(i) / X.size());
        }
        return avglist;
    }




    }
