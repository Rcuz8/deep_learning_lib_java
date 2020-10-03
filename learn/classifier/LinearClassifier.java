package com.ai.learn.classifier;

import com.ai.learn.general.Example;
import com.ai.learn.nn.Link;
import com.ai.math.Utils;
import com.ai.math.Vector;

import java.util.ArrayList;
import java.util.List;

import static com.ai.print.Log.logn;


public abstract class LinearClassifier {

    public List<Double> weights;
    int t;
    /* Standard Case */
    public LinearClassifier(int size) {
        weights = new ArrayList<>(size);
        while (weights.size() < size) weights.add(0.0);
        t = 0;
    }
    /* Dynamic case -> Neural Networks */
    public LinearClassifier() {
        weights = new ArrayList<>();
        weights.add(0.0);
        t = 0;
    }

    public abstract void update(List<Double> X, double y, double alpha);

    public double hypothesisFor(List<Double> X) {
        double evaluation = Vector.dot(X,weights);
        return threshold(evaluation);
    }

    public abstract double threshold(Double z);

    public double alpha() {
        return 0.95;
        //t++; return (double) 900/(900 + t);
    }
//    public double alpha() { return 0.05; }

    public void update(Example example) {
        // We made discrete strings continuous data -> Map them to an attribute index, within their respective example index
        List<Double> ins = (List<Double>) (List) example.inputs;
        List<Double> outs = (List<Double>) (List) example.outputs;
        if (outs == null || outs.isEmpty()) {
            throw new Error("Size mismatch");
        }
        Double out = outs.get(0);
        double alpha = alpha();
        update(ins,out,alpha);
    }


    /* Train the classifier */
    public void train(List<Example> examples) {
        for (Example example : examples) {
            update(example);
        }
    }

    /**
     * Return the proportion of the given Examples that are classified
     * correctly by this LinearClassifier.
     * This is probably only meaningful for classifiers that use
     * a hard threshold. Use with care.
     */
    public double accuracy(List<Example> examples) {
        int ncorrect = 0;
        for (Example ex : examples) {
            List<Double> ins = (List<Double>) (List) ex.inputs;
            Double out = ((List<Double>) (List) ex.outputs).get(0);
            double result = hypothesisFor(ins);
            if (result == out) {
                ncorrect += 1;
            }
        }
        return (double)ncorrect / examples.size();
    }

    /**
     * Return the squared error per example for this Linearlassifier
     * using the L2 (squared error) loss function.
     */
    public double squaredErrorPerSample(List<Example> examples) {
        double sum = 0;
        for (Example ex : examples) {
            List<Double> ins = (List<Double>) (List) ex.inputs;
            Double out = ((List<Double>) (List) ex.outputs).get(0);
            double result = hypothesisFor(ins);
            double error = out - result;
            sum += error*error;
        }
        return sum / examples.size();
    }


}
