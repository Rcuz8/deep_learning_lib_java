package com.ai.learn.nn.unit;

import com.ai.learn.general.Perceptron;
import com.ai.learn.nn.NetworkLayer;

import java.util.List;

public class PerceptronUnit extends Unit {

    public PerceptronUnit(NetworkLayer layer, int unitIndex, double bias) {
        super(layer, unitIndex, bias);
    }

    public PerceptronUnit(double bias) {
        super(bias);
    }

    @Override
    public double g_prime(double z) {
        return Perceptron.g_prime(z);
    }

    @Override
    public void update(List<Double> X, double y, double alpha) {
        Perceptron.update(weights,hypothesisFor(X),X,y,alpha);
    }

    @Override
    public double threshold(Double z) {
        return Perceptron.threshold(z);
    }
}
