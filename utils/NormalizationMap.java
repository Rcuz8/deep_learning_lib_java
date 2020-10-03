package com.ai.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NormalizationMap {

    // Norm map looks like this:
    // (data index) 0 -> [ 1 -> 0.2, 2 -> 0.4, ..., 5 -> 1 ]  (5 possible outcomes)
    // (data index) 1 -> [ 1 -> 0.1, 2 -> 0.2, ..., 10 -> 1 ] (10 possible outcomes)

    //  - 2-map implementation to prevent search for indices when UPDATING values -

    // (data index) 0 -> [ 1 -> 0.2, 2 -> 0.4, ..., 5 -> 1 ]  (5 possible outcomes
    private List<Map<Double, Double>> forwardsNormalize = new ArrayList<>();
    // (data index) 0 -> [ 0.2 -> 1, 0.4 -> 2, ..., 1 -> 5 ]  (5 possible outcomes)
    private List<Map<Double, Double>> backwardsNormalize = new ArrayList<>();

    private List<Double> maxes = new ArrayList<>();
    private List<Double> mins = new ArrayList<>();
    private boolean[] isCategorical;

    public NormalizationMap(int size, boolean[] isCategorical) {
        while (forwardsNormalize.size() < size) {
            forwardsNormalize.add(new HashMap<>());
            backwardsNormalize.add(new HashMap<>());
            maxes.add(Double.MIN_VALUE);
            mins.add(Double.MAX_VALUE);
        }
        this.isCategorical = isCategorical;
    }

    public void putMap(int index, double num, double normalized) {
        if (index > forwardsNormalize.size()) throw new Error("Cannot put index " + index + " into Norm map of length " + forwardsNormalize.size());
        forwardsNormalize.get(index).put(num,normalized);
        backwardsNormalize.get(index).put(normalized,num);
        if (num > maxes.get(index)) maxes.set(index, num);
        if (num < mins.get(index)) mins.set(index, num);
    }

    /* Unnormalize a continuous point
     Xnew =  ((Xold-Xmin) / (Xmax - Xmin))
     => Xnew (Xmax - Xmin) = (Xold-Xmin)
     => [ Xnew (Xmax - Xmin) ] + Xmin = Xold   => X = ( Xnew (Xmax - Xmin) ) + Xmin
    */
    private static double unnormalizeContinuousPoint(double x, double min, double max) {
        return ( x * (max - min) ) + min;
    }

    private double unnormalizeContinuousPoint(int data_index, double normalized) {
        double min = mins.get(data_index);
        double max = maxes.get(data_index);
        return unnormalizeContinuousPoint(normalized, min, max);
    }

    public double getUnnormalized(int index, double normalized) {
        if (isCategorical[index])
            return backwardsNormalize.get(index).get(snap_no(index, normalized) ); // snap & get
        else
            return unnormalizeContinuousPoint(index, normalized);
    }

    public double getNormalized(int index, double unnormalized) {
        return forwardsNormalize.get(index).get(unnormalized);
    }

    // snap (round) to nearest normalized value
    public double snap_no(int data_index, double actual) {
        Double normalized_value = nearest_key(data_index,actual,backwardsNormalize);
        if (normalized_value == null) throw new Error("Could not round to nearest normalized value");
        return normalized_value;
    }

    // snap (round) to nearest unnormalized value
    public double snap_un(int data_index, double actual) {
        Double unnormalized_value = nearest_key(data_index,actual,forwardsNormalize);
        if (unnormalized_value == null) throw new Error("Could not round to nearest unnormalized value");
        return unnormalized_value;
    }

    public Double nearest_key(int data_index, double actual, List<Map<Double, Double>> list) {
        Double min_distance = Double.MAX_VALUE;
        Double value = null;

        // 0.2 -> ...
        for (Map.Entry<Double,Double> entry : list.get(data_index).entrySet()) {
            double diff = Math.abs(entry.getKey()-actual);
            if (diff < min_distance) {
                min_distance = diff;
                value = entry.getKey();
            }
        }
        if (value == null) throw new Error("Could not round to nearest normalized value");
        return value;
    }

    public double max(int data_index) {
        return maxes.get(data_index);
    }
    public double min(int data_index) {
        return mins.get(data_index);
    }


}
