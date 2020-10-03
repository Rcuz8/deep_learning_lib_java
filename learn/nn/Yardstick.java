package com.ai.learn.nn;

import com.ai.learn.general.Example;
import com.ai.math.Utils;
import com.ai.math.Vector;
import com.ai.utils.IndexMap;
import com.ai.utils.LossMap;
import com.ai.utils.NormalizationMap;

import java.util.ArrayList;
import java.util.List;

public class Yardstick {


    public static class AccuracyResult {
        public List<Double> outputAccuracies;
        public List<LossMap> losses;

        public AccuracyResult(List<Double> outputAccuracies, List<LossMap> losses) {
            this.outputAccuracies = outputAccuracies;
            this.losses = losses;
        }
    }

    private static double STR_MISS_LOSS = 0.5;

    public static double unnorm(int index, double normalized, NormalizationMap map) {
        return map.getUnnormalized(index, normalized);
    }

    /* CORE FUNCTIONS */

    /* Get the model's error^2 on some example
     * NOTE: Assuming double inputs
     * NOTE: String loss treated as equal (given constant weight)
     * */
    private static List<Double> errorOn_Vector(NeuralNetwork nn, Example example, boolean[] isStringVector, List<LossMap> lossMaps, NormalizationMap nm, List<Integer> output_indices) {
        if (example == null)
            return null;
        // create sum vector
        List<Double> sumVector = new ArrayList<>();
        while (sumVector.size() < nn.noutputs()) sumVector.add(0.0);
        // get expected outputs
        List<Double> expected = example.Outputs_casted();
        // get actual outputs
        List<Double> actual = nn.predict(example);
        // for each input
        for (int i = 0; i < expected.size(); i++) {
            // get output index
            int output_index = output_indices.get(i);
            // Get info
            double should_be = expected.get(i);
            double is = actual.get(i);
            // Make informal guesses
            double is_after_snapping = nm.snap_no(output_index,is); // snap to nearest
            /*
                Problem is that the MISSES are VERY slight, but the rounding is often away from the correct answer.
                So, while the premise of this calculation IS correct, it's quite difficult to pass the rounding benchmark.

                Need to see convergence over time and if the margins get closer.
                I believe they are, so maybe data ordering is just highly important to prevent misinformed predictions?
             */
            double Error = Math.pow(should_be - is, 2);
            boolean same_rnd = should_be == is_after_snapping;
            // input is a string,
            if (isStringVector[i]) {
                // if both rounded integers are equal, model would predict correctly -> loss = 0
                if (same_rnd) {
                    // loss = 0, do nothing
                } else {
                    // otherwise, loss = 0.5
                    sumVector.set(i,Error);
                    // add formal mistake to loss map
                    lossMaps.get(i).addLoss(should_be,is_after_snapping);
                }
            } else { /* Not a string value -> treat normally */

                // add (expected - actual)^2 to the sum vector
                sumVector.set(i, Error );
                // If not the same expected value
//                if (!same_rnd) {
//                    // add formal mistake to loss map
//                    lossMaps.get(i).addLoss(should_be,is_after_snapping);
//                }

            }
        }

        return sumVector;
    }

    /* Returns mean-squared error (Accuracy) of model on some examples
     * @param isStringVector is whether or not each index represents a string,
     * in which case we treat each miss as a constant loss, rather than determined by quantity
     * */
    public static AccuracyResult accuracyVector(NeuralNetwork nn, List<Example> examples, boolean[] isStringVector, NormalizationMap nm, IndexMap indexMap, List<String> data_headers, List<Integer> output_indices) {
        // create sum vector
        List<Double> sumVector = new ArrayList<>();
        // create loss map
        List<LossMap> lossMaps = new ArrayList<>();
        // fill out vector
        while (sumVector.size() < nn.noutputs()) sumVector.add(0.0);
        while (lossMaps.size() < nn.noutputs())  lossMaps.add(new LossMap().withHeaders(data_headers).withaDataMappings(indexMap,nm,output_indices.get(lossMaps.size())));
        for (Example example : examples) {
            if (example == null)
                continue;
            sumVector = Vector.sum(sumVector, errorOn_Vector(nn, example, isStringVector, lossMaps, nm, output_indices));
        }

        List<Double> avgAccuracy = Vector.vround5(Vector.divideBy(sumVector, examples.size()));

        return new AccuracyResult(avgAccuracy, lossMaps);
    }




//    public static double avgAccuracy(NeuralNetwork nn, List<Example> exs, boolean[] isStringVector, NormalizationMap nm, IndexMap indexMap,List<String> data_headers, List<Integer> output_indices) {
//        List<Double> acc_vector = accuracyVector(nn, exs, isStringVector, nm, indexMap, data_headers, output_indices).outputAccuracies;
//        double vsum = Vector.sum(acc_vector);
//        return vsum / acc_vector.size();
//    }





    /* Convenience version of accuracy measurement for best-fit network ONLY */

    private static List<Double> bf_errorOn_Vector(NeuralNetwork nn, Example example, boolean[] isStringVector, NormalizationMap nm, List<Integer> output_indices) {
        if (example == null)
            return null;
        // create sum vector
        List<Double> sumVector = new ArrayList<>();
        while (sumVector.size() < nn.noutputs()) sumVector.add(0.0);
        // get expected outputs
        List<Double> expected = example.Outputs_casted();
        // get actual outputs
        List<Double> actual = nn.predict(example);
        // for each input
        for (int i = 0; i < expected.size(); i++) {
            // get output index
            int output_index = output_indices.get(i);
            // Get info
            double should_be = expected.get(i);
            double is = actual.get(i);
            // NORMALIZED -> THIS DOESN'T FIT, IT NEEDS TO UN-NORMALIZE
            // Make informal guesses
            double is_after_snapping = nm.snap_no(output_index,is); // snap to nearest
            boolean same_rnd = should_be == is_after_snapping;
            // input is a string,
            if (isStringVector[i]) {
                // if both rounded integers are equal, model would predict correctly -> loss = 0
                if (same_rnd) {
                    // loss = 0, do nothing
                } else {
                    // otherwise, loss = 0.5
                    sumVector.set(i,STR_MISS_LOSS);
                }
            } else { /* Not a string value -> treat normally */

                // add (expected - actual)^2 to the sum vector
                sumVector.set(i, ( Math.pow(should_be - is, 2) ));

            }
        }

        return sumVector;
    }

    public static AccuracyResult bf_accuracyVector(NeuralNetwork nn, List<Example> examples, boolean[] isStringVector, NormalizationMap nm, List<Integer> output_indices) {
        // create sum vector
        List<Double> sumVector = new ArrayList<>();
        // fill out vector
        while (sumVector.size() < nn.noutputs()) sumVector.add(0.0);
        for (Example example : examples) {
            if (example == null)
                continue;
            sumVector = Vector.sum(sumVector, bf_errorOn_Vector(nn, example, isStringVector, nm, output_indices));
        }

        List<Double> avgAccuracy = Vector.vround5(Vector.divideBy(sumVector, examples.size()));

        return new AccuracyResult(avgAccuracy, null);
    }

    public static double avgAccuracy(NeuralNetwork nn, List<Example> exs, boolean[] isStringVector, NormalizationMap nm, List<Integer> output_indices) {
        List<Double> acc_vector = bf_accuracyVector(nn, exs, isStringVector, nm, output_indices).outputAccuracies;
        double vsum = Vector.sum(acc_vector);
        return vsum / acc_vector.size();
    }

    public static class Prediction {
        public List<Double> unnormalized;
        public List<Double> normalized;
        public Prediction(List<Double> unnormalized, List<Double> normalized) {
            this.unnormalized = unnormalized;
            this.normalized = normalized;
        }
    }

    public static class EvaluationParameters {
        public NormalizationMap nm;
        boolean[] isCategorical;
        List<Integer> outputIndices;
        double stdDev; // Std dev for evaluating accuracy of normalized continuous data

        public EvaluationParameters(NormalizationMap nm, boolean[] isCategorical, List<Integer> outputIndices, double stdDev) {
            this.nm = nm;
            this.isCategorical = isCategorical;
            this.outputIndices = outputIndices;
            this.stdDev = stdDev;
        }
    }

    /*
        Predict for a network.

        The NN implementation's "predict" does not round categorical data, this does.

        Generates both normalized & unnormalized representation for prediction.

        Rounding process:
        Then, is the data is categorical, we snap it to it's nearest domain value
            (ex. data index 3's domain = [1,2,7,24], unnormalized datum = 3.5 -> Snap to 2.)

     */
    public static Prediction predict(NeuralNetwork nn, Example example, EvaluationParameters params) {
        // Use NN's "predict" to get predicted double values
        List<Double> predictions_no = nn.predict(example);
        // Initialize unnormalized predictions
        List<Double> predictions_un = new ArrayList<>();
        for (int i = 0; i < predictions_no.size(); i++) {
            int outputIndex = params.outputIndices.get(i);
            // unnormalize ( all snapping work is done in getUnnormalized() )
            double unnormalized = params.nm.getUnnormalized(outputIndex, predictions_no.get(i));
            predictions_un.add(unnormalized);
        }

        return new Prediction(predictions_un,predictions_no);
    }


    /*
        This should determine how fitting the network was for this example

        Very simple.

        If the data is categorical,

            Correctness = snapped value (already unnormalized) = expected value (after unnormalization)

        If the data is continuous, we evaluate if it's within a given standard deviation of what it should be.
        NOTE: For this, we need to keep normalized so the std. dev is still relative to the column's domain,

     */

    public static boolean[] correct(NeuralNetwork nn, Example example, EvaluationParameters params) {
        // Generate true prediction
        Prediction prediction = predict(nn,example,params);
        // Generate expected outputs
        List<Double> should_be = example.Outputs_casted();

        int noutputs = should_be.size(); // get output size

        boolean[] correct = new boolean[noutputs];

        for (int i = 0; i < noutputs; i++) {
            // get normalized prediction for this item
            double item_value = prediction.unnormalized.get(i);
            // get normalized expected value for this item
            double item_should_be = params.nm.getUnnormalized(params.outputIndices.get(i), should_be.get(i));
            // if categorical, correct means same
            if (params.isCategorical[params.outputIndices.get(i)]) {
                correct[i] = (  item_value == item_should_be   );
            } else {
                // otherwise, correct means within stdDev
                correct[i] = (    Utils.withinStdDev( /* target */ item_should_be, /* value */ item_value, /* std. dev */ params.stdDev)   );
            }
        }

        return correct;
    }

    public static List<Double> accuracy(NeuralNetwork nn, List<Example> examples, EvaluationParameters params) {
        List<Double> accuracies = new ArrayList<>();
        while (accuracies.size() < nn.noutputs()) accuracies.add(0.0);

        for (Example example : examples) {
            boolean[] correct = correct(nn,example,params);
            for (int i = 0; i < correct.length; i++) {
                if (correct[i])
                    accuracies.set(i, accuracies.get(i) + 1);
            }
        }

        List<Double> avgAccuracy = Vector.vround5(Vector.divideBy(accuracies, examples.size()));

        return avgAccuracy;

    }

    public static double accuracy_avg(NeuralNetwork nn, List<Example> examples, EvaluationParameters params) {
        List<Double> acc_vector = accuracy(nn,examples,params);
        double vsum = Vector.sum(acc_vector);
        double avg = vsum / acc_vector.size();
        return avg;
    }






    /* OLD FUNCTIONS */


    public static List<Double> mse(NeuralNetwork nn, Example example) {
        if (example == null)
            return null;
        // create sum vector
        List<Double> sumVector = new ArrayList<>();
        while (sumVector.size() < example.outputs.size()) sumVector.add(0.0);
        // get expected outputs
        List<Double> expected = example.Outputs_casted();
        // get actual outputs
        List<Double> actual = nn.predict(example);
        // for each input
        for (int i = 0; i < expected.size(); i++) {
            // add (expected - actual)^2 to the sum vector
            sumVector.set(i, ( Math.pow(expected.get(i) - actual.get(i), 2) ));
        }

        return sumVector;
    }

    /* Returns mean-squared error (Accuracy) of model on some examples */
    public static List<Double> mse(NeuralNetwork nn, List<Example> examples) {
        // create sum vector
        List<Double> sumVector = new ArrayList<>();
        while (sumVector.size() < examples.get(0).outputs.size()) sumVector.add(0.0);
        double total = examples.size();
        for (Example example : examples) {
            sumVector = Vector.sum(sumVector, mse(nn, example));
        }

        return Vector.vround5(Vector.divideBy(sumVector, total));
    }

    public static double mseAvg(NeuralNetwork nn, List<Example> examples) {
        List<Double> acc_vector = mse(nn,examples);
        double vsum = Vector.sum(acc_vector);
        double avg = vsum / acc_vector.size();
        return avg;
    }

}
