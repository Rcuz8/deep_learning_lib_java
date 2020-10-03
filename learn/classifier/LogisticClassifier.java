package com.ai.learn.classifier;
import com.ai.learn.general.Logistic;

import java.util.List;
import static com.ai.math.Utils.rounded;

public class LogisticClassifier extends LinearClassifier {

    public LogisticClassifier(int nweights) {
        super(nweights);
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


/*

    Old, working code

    public void wi_incr(int i, double val) { weights.set(i, rounded(weights.get(i) + val) ); }

    @Override
    public void update(List<Double> X, double y, double alpha) {
        // wi ← wi +α(y−hw(x))×hw(x)(1−hw(x))×xi .
        for (int i = 0; i < weights.size(); i++) {
            double hwx = hypothesisFor(X);
            double delta = alpha*(y-hwx)*hwx*(1-hwx)*X.get(i);
            wi_incr(i,  delta  );
        }
    }

    @Override
    public double threshold(Double z) {
        return rounded((double) 1 / (1 + Math.pow(Math.E,z*-1)));
    }



 */