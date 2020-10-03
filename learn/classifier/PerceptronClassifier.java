package com.ai.learn.classifier;

import com.ai.learn.general.Perceptron;
import java.util.List;

public class PerceptronClassifier extends LinearClassifier {

    public PerceptronClassifier(int nweights) {
        super(nweights);
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

/*

    Old, working code:

    @Override
    public void update(List<Double> X, double y, double alpha) {
        // wi ← wi +  α(y−hw(X)) × xi
        double hwx = hypothesisFor(X);
        for (int i = 0; i < weights.size(); i++) {
            weights.set(i, rounded( weights.get(i) + ( alpha * (y-hwx) * X.get(i) ) ) );
        }
    }

    @Override
    public double threshold(Double z) {
        return z >= 0 ? 1 : 0;
    }

 */






/*


    ----------------

    Old logging code:

//        logn("\n Performing weight update.");
//        logn("\tWeights      : " + weights);
//        logn("\tInput Vector : " + X + "\n\tOutput       : " + y + "\n\talpha        : " + alpha);
//        logn("\tWeight updates: ");
//        logn("\t\ty-hw(x) = " + (y-hwx) + "\t (h(x) = " + ((y == hwx) ? "correct)" : "wrong)"));
//        logn("\t\t\tW[" + i + "] = w["+ i + "] + " + alpha + " * ("+y+" - "+hwx+") * "+X.get(i));
//        logn("\t\t|-> Weights are " + weights);
 */