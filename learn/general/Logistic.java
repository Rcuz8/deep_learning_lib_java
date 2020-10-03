package com.ai.learn.general;

import com.ai.math.Vector;

import java.util.List;

import static com.ai.math.Utils.rounded;

public class Logistic {

    public static void wi_incr(List<Double> weights, int i, double val) { weights.set(i, rounded(weights.get(i) + val) ); }

    public static List<Double> update(List<Double> weights, List<Double> X, double y, double alpha) {
        // wi ← wi +α(y−hw(x))×hw(x)(1−hw(x))×xi .
        for (int i = 0; i < weights.size(); i++) {
            double hwx = threshold(Vector.dot(weights,X));
            double delta = alpha*(y-hwx)*hwx*(1-hwx)*X.get(i);
            wi_incr(weights, i,  delta  );
        }
        return weights;
    }

    public static double threshold(Double z) {
        return rounded((double) 1 / (1 + Math.pow(Math.E,z*-1)));
    }

    public static double g_prime(double z) {
        return z*(1-z);
    }
}
