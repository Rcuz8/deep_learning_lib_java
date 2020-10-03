package com.ai.learn.general;
import java.util.List;
import static com.ai.math.Utils.rounded;

public class Perceptron {

    public static List<Double> update(List<Double> weights, double hwx, List<Double> X, double y, double alpha) {
        // wi ← wi +  α(y−hw(X)) × xi
        for (int i = 0; i < weights.size(); i++) {
            weights.set(i, rounded( weights.get(i) + ( alpha * (y-hwx) * X.get(i) ) ) );
        }
        return weights;
    }

    public static double threshold(Double z) {
        return z >= 0 ? 1 : 0;
    }

    public static double g_prime(double z) {
        return 1;
    }

}
