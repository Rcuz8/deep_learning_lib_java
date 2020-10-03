package com.ai.learn.nn.unit;

import com.ai.learn.general.Logistic;
import com.ai.learn.nn.NetworkLayer;
import com.ai.learn.nn.unit.Unit;

import java.util.List;

public class LogisticUnit extends Unit {

    public LogisticUnit(NetworkLayer layer, int unitIndex, double bias) {
        super(layer, unitIndex, bias);
    }

    public LogisticUnit(double bias) {
        super(bias);
    }

    @Override
    public double g_prime(double z) {
        return Logistic.g_prime(z);
    }

    @Override
    public void update(List<Double> X, double y, double alpha) {
        Logistic.update(weights,X,y,alpha);
    }

    @Override
    public double threshold(Double z) {
        return Logistic.threshold(z);
    }
}
