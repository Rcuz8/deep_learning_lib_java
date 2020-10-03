package com.ai.learn.nn.unit;

import com.ai.learn.general.Logistic;
import com.ai.learn.general.Perceptron;
import com.ai.learn.general.Tanh;
import com.ai.learn.nn.NetworkLayer;

import java.util.List;

public class TanhUnit extends Unit {

    public TanhUnit(NetworkLayer layer, int unitIndex, double bias) {
        super(layer, unitIndex, bias);
    }

    public TanhUnit(double bias) {
        super(bias);
    }

    @Override
    public double g_prime(double z) {
        return Tanh.g_prime(z);
    }

    @Override
    public void update(List<Double> X, double y, double alpha) {
        Tanh.update(weights,X,y,alpha);
    }

    @Override
    public double threshold(Double z) {
        return Tanh.threshold(z);
    }
}
