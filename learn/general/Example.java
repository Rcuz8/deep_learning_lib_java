package com.ai.learn.general;

import com.ai.input.InputParser;
import com.ai.input.InputReader;
import com.ai.input.ListInputReader;
import com.ai.math.Utils;
import com.ai.nlp.BoW;
import com.ai.utils.NormalizationMap;

import java.util.*;
import java.util.function.Predicate;

import static com.ai.print.Log.logn;

public class Example {
    public List<Object> inputs;
    public List<Object> outputs;
    public int index;

    public Example(List inputs, List outputs, int index) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.index = index;
    }

    @Override
    public String toString() {
        String s = "\nExample #" + index + "\n\tIns:  [ ";
        for (int i = 0; i < inputs.size(); i++) {
            s += inputs.get(i).toString() + " ";
        }
        s += "]\n\tOuts: [ ";
        for (int i = 0; i < outputs.size(); i++) {
            s += outputs.get(i).toString() + " ";
        }
        s+= "]";
        return s;
    }


    public List<Double> Outputs_casted() {
        return (List<Double>) (List) outputs;
    }
    public List<Double> Inputs_casted() {
        return (List<Double>) (List) inputs;
    }

    @Override
    public boolean equals(Object obj) {
        return (index == ((Example) obj).index);
    }

    // Xnew =  ((Xold-Xmin) / (Xmax - Xmin))
    private static double normalizePoint(double x, double min, double max) {
        return ((x-min) / (max - min));
    }

    private static double normalizePoint(List<Double> data_vector, int index, double[] mins_vector, double[] maxes_vector) {
        return normalizePoint(data_vector.get(index), mins_vector[index], maxes_vector[index]);
    }

    // Assumes double inputs
    public static NormalizationMap normalize(List<Example> exs, boolean[] isStrings) {
        // get in/out sizes
        int INPUT_SIZE = exs.get(0).inputs.size();
        int OUTPUT_SIZE = exs.get(0).outputs.size();
        // Get mins / maxes for ins & outs
        double[] maxes_ins = new double[INPUT_SIZE];
        double[] mins_ins = new double[INPUT_SIZE];
        double[] maxes_outs = new double[OUTPUT_SIZE];
        double[] mins_outs = new double[OUTPUT_SIZE];
        // init to INF values
        for (int i = 0; i < INPUT_SIZE; i++) {
            maxes_ins[i] = Double.MIN_VALUE;
            mins_ins[i] = Double.MAX_VALUE;
        }
        for (int i = 0; i < OUTPUT_SIZE; i++) {
            maxes_outs[i] = Double.MIN_VALUE;
            mins_outs[i] = Double.MAX_VALUE;
        }

        // for each example, ensure corresponding index in mins/maxes is the max
        for (Example ex : exs) {
            List<Double> ins = ex.Inputs_casted();
            List<Double> outs = ex.Outputs_casted();
            // ensure min/max for inputs
            for (int i = 0; i < INPUT_SIZE; i++) {
                maxes_ins[i] = Math.max(ins.get(i),maxes_ins[i]);
                mins_ins[i] = Math.min(ins.get(i),mins_ins[i]);
            }
            // ensure min/max for outputs
            for (int i = 0; i < OUTPUT_SIZE; i++) {
                maxes_outs[i] = Math.max(outs.get(i),maxes_outs[i]);
                mins_outs[i] = Math.min(outs.get(i),mins_outs[i]);
            }
        }

//        logn("Min/Max arrays: ");
//        logn(Arrays.toString(mins_ins));
//        logn(Arrays.toString(maxes_ins));
//        logn(Arrays.toString(mins_outs));
//        logn(Arrays.toString(maxes_outs));
//        logn("End of min/max arrays");

        // Norm map looks like this:
        // (data index) 0 -> [ 1 -> 0.2, 2 -> 0.4, ..., 5 -> 1 ]  (5 possible outcomes)
        // (data index) 1 -> [ 1 -> 0.1, 2 -> 0.2, ..., 10 -> 1 ] (10 possible outcomes)

        // We only care to unnormalize outputs, but the frontend will need to normalize as well so we'll carry both
        NormalizationMap nm = new NormalizationMap(INPUT_SIZE + OUTPUT_SIZE,isStrings);
        // Normalize examples
        for (Example ex : exs) {
            List<Double> ins = ex.Inputs_casted();
            List<Double> outs = ex.Outputs_casted();
            // normalize inputs
            for (int i = 0; i < INPUT_SIZE; i++) {
                double norm = normalizePoint(ins,i,mins_ins,maxes_ins);
                nm.putMap(i, ins.get(i),norm);
                ex.inputs.set(i, norm );
            }
            // normalize outputs
            for (int i = 0; i < OUTPUT_SIZE; i++) {
                double norm = normalizePoint(outs,i,mins_outs,maxes_outs);
                nm.putMap(INPUT_SIZE + i, outs.get(i),norm);
                ex.outputs.set(i, norm);
            }
        }

        // Done
        return nm;
    }

    public void prependConstant(Double i) { inputs.add(0,i); }

    public static void List_prependConstant(List<Example> examples, Double i) {
        examples.forEach((ex) -> ex.prependConstant(i));
    }

    public static class ParseResult {
        public List<Example> exs;
        public boolean[] isStrings;
        public NormalizationMap nm;

        public ParseResult(List<Example> exs, boolean[] isStrings, NormalizationMap nm) {
            this.exs = exs;
            this.isStrings = isStrings;
            this.nm = nm;
        }
    }

    public static ParseResult parsedString(String unparsed_examples, List<Integer> outputs) {
        String TKN_1 = ",";
        String TKN_2 = "!!!";
        // Split up request data
        String[] rows = unparsed_examples.split(TKN_2);
        // fill into usable data lists
        List<String> data_rows = new ArrayList<>();
        Collections.addAll(data_rows, rows);
        // setup model query
        InputReader reader = new ListInputReader(data_rows, TKN_1);
        InputParser parser = new InputParser(reader).withOutputs(outputs);
        // get isString values
        boolean[] isStrings = parser.isStringValues();
        // parse for examples
        List<Example> exs = parser.Examples_parse();
        // normalize examples
        NormalizationMap nm = Example.normalize(exs, isStrings);

        return new ParseResult(exs,isStrings, nm);
    }

    public static class FilterResult {
        public List<Example> in;
        public List<Example> out;

        public FilterResult(){
            in = new ArrayList<>();
            out = new ArrayList<>();
        }
    }

    public static List<FilterResult> multifilter(List<Example> exs, Predicate<Example>... predicates) {
        List<FilterResult> results = new ArrayList<>();
        while (results.size() < predicates.length) results.add(new FilterResult());
        for (Example example : exs) {
            // for each predicate, if it passes, add to the respective "in" group, else to the "out" group
            for (int i = 0; i < predicates.length; i++) {
                // if passes this predicate
                if (predicates[i].test(example)) {
                    // add to in group
                    results.get(i).in.add(example);
                } else {
                    // add to out group
                    results.get(i).out.add(example);
                }
            }
        }
        return results;
    }

    public static FilterResult filter(List<Example> exs, Predicate<Example>... predicates) {
        FilterResult results = new FilterResult();
        for (Example example : exs) {
            boolean passesAll = true;
            // for each predicate, update whether or not it has passed all predicates
            for (Predicate<Example> predicate : predicates) {
                // if passes this predicate
                if (!predicate.test(example)) {
                    passesAll = false;
                }
            }
            // if passed all
            if (passesAll) {
                // add to in group
                results.in.add(example);
            } else {
                // add to out group
                results.out.add(example);
            }
        }
        return results;
    }

    public void printRawData() {
        List<Double> ins = Inputs_casted();
        List<Double> outs = Outputs_casted();
        for (int i = 0; i < ins.size(); i++) {
            System.out.print(ins.get(i) + ",");
        }
        for (int i = 0; i < outs.size(); i++) {
            if (i < outs.size() - 1) System.out.print(outs.get(i) + ",");
            else System.out.print(outs.get(i));
        }
        System.out.print("\n");
    }

    public static void List_PrintRaw(List<Example> exs) {
        for (Example ex : exs) {
            ex.printRawData();
        }
    }

    public void bucket_Input(int index, int nbuckets) {
        double interval = 1.0 / nbuckets;
        double in = Inputs_casted().get(index);
        for (double bucket = 0; bucket <= 1; bucket+=interval) {
            if (in <= bucket) {
                inputs.set(index, Utils.rounded2(bucket));
                return;
            }
        }
    }

    public static void List_BucketInput(List<Example> exs, int index, int nbuckets) {
        for (Example ex : exs) {
            ex.bucket_Input(index,nbuckets);
        }
    }


}
