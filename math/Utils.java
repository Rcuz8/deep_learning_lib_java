package com.ai.math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import static com.ai.print.Log.logn;

public class Utils {

    public static double rounded(double d) {
        return roundd(d,8); }
    public static double deepround(double d) {
        return roundd(d,9); }
    public static double round5(double d) {
        return roundd(d,5); }
    public static double rounded2(double d) {
        return roundd(d,3); }
    public static double roundd(double value, int places) {
        try {
            BigDecimal bd = new BigDecimal(value).setScale(places, RoundingMode.HALF_UP);
            return bd.doubleValue();
        } catch (NumberFormatException formexc) {
            throw new Error("Infinite value passed as weight.");
//            return value;
        }

    }

    public static double random_smallNumber() {
        double neg = Math.random() > 0.5 ? 1 : -1;
        return Math.random() / 8 * neg;
    }

    public static boolean withinStdDev(double target, double value, double stdDev) {
        return (Math.abs(target-value) <= stdDev);
    }

    private static double epow(double x) {
        return Math.pow(Math.E, x);
    }

    public static double tanh(double x) {
        return (epow(x) - epow(-x)) / (epow(x) + epow(-x));
    }
    public static double tanh_prime(double z) {
        return (1-Math.pow(z, 2));
    }

    private final static double leaky_epsilon = 0.01;

    public static double leakyReLU(double x) {
        return Math.max(leaky_epsilon*x, x);
    }
    public static double leakyReLU_prime(double x) {
        if (x >= 0) return 1;
        else return leaky_epsilon;
    }

    public static double decay(double _for, double multiplier, double decayRate, double domainLength ) {

        return (multiplier * Math.pow(1-(decayRate/domainLength), _for));
    }

    public static double decay(double _for, double multiplier, double convergeIn) {
        double h = 5 / convergeIn; // y = mult / e^5 -> will be a low #
        return (multiplier / epow(h * _for));
    }
}
