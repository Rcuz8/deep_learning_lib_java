package com.ai.input;

import com.ai.learn.general.Example;
import com.ai.learn.nn.NeuralNetwork;
import com.ai.math.Vector;
import com.ai.nlp.BoW;
import com.ai.utils.CollectionUtils;
import com.ai.utils.IndexMap;
import com.ai.input.InputReader;
import com.ai.utils.NormalizationMap;
import com.ai.utils.StringUtils;
import org.apache.tomcat.jni.Error;

import static com.ai.print.Log.logn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class InputParser {

    InputReader reader;
    IndexMap map;
    boolean use_last_data_index_as_output = false;
    List<Integer> stored_outputs = null;
    List<BagFilter> bagFilters; // filter these indices by their respective bags of words

    class BagFilter {
        public BoW bag;
        public int inputIndex;

        public BagFilter(BoW bag, int inputIndex) {
            this.bag = bag;
            this.inputIndex = inputIndex;
        }
    }

    public InputParser(InputReader reader) {
        this.reader = reader;
        map = new IndexMap();
        bagFilters = new ArrayList<>();
    }

    /* Parses example from format:
        name\tdata1\tdata2...
     */
    private Example nextExample(List<Integer> inputColumns, List<Integer> outputColumns, int exampleNumber){
        // split
        List<String> strings = reader.next();
        if (strings.isEmpty()) return null; // bad format

        /* We now want to filter down desired data indices */

        // for every bag of words filter
        for (BagFilter bagFilter : bagFilters) {
            int i = bagFilter.inputIndex;
            BoW bag = bagFilter.bag;
            String prev = strings.get(i);
            String snapped = bag.get(prev);
            // set new data piece to the bag's word for it
            strings.set(i,snapped);
        }

        /* Resume parsing / generating */

        // parse
        List<Object> parsed_tokens = CollectionUtils.properlyParsedList(strings,map);

        // sublists
        List<Object> outputs = CollectionUtils.filterIndices(parsed_tokens,inputColumns);
        List<Object> inputs = CollectionUtils.filterIndices(parsed_tokens,outputColumns);

        return new Example(inputs, outputs, exampleNumber);
    }

    /*
        @param outputColumns The column indices of the output data
     */
    public List<Example> Examples_parse() {
        verifyOutputsExist("Examples_parse");
        List<Integer> outputColumns = CollectionUtils.copy(stored_outputs);
        int data_length = reader.dataLength();
        List<Integer> inputColumns = CollectionUtils.Data_inputIndices(data_length,outputColumns);
        reader.reset();
        List<Example> examples = new ArrayList<>();
        // get each example
        while (reader.hasNext())
            examples.add(nextExample(inputColumns,outputColumns,examples.size()));
        reader.reset();
        return examples;
    }

    public static class StringInputResult {
        public NeuralNetwork net;
        public List<Example> exs;
        public double optimal_learningRate;
        public List<Integer> output_indices;
        public boolean[] isStrings;
        public NormalizationMap nm;

        public StringInputResult(NeuralNetwork net, List<Example> exs, List<Integer> outputs, double optimal_learningRate, boolean[] isStrings, NormalizationMap nm) {
            this.net = net;
            this.exs = exs;
            this.output_indices = outputs;
            this.optimal_learningRate = optimal_learningRate;
            this.isStrings = isStrings;
            this.nm = nm;
        }
    }

    /* Resets input & regathers stuff */
    public boolean[] isStringValues() {
        reader.reset();

        List<List<Double>> nexts = new ArrayList<>();
        int N_NEXTS = 3;

        // Get each boolean (3 Ensures less chance of missing)
        while (nexts.size() < N_NEXTS)
            nexts.add(CollectionUtils.isStringIndices(reader.next()));

        // Avg & round the boolean result
        List<Integer> isStringList = Vector.vround(Vector.avg(nexts));

        boolean[] isStringValues = new boolean[isStringList.size()];

        // map from ints to doubles
        for (int i = 0; i < isStringList.size(); i++) {
            isStringValues[i] = isStringList.get(i) == 1;
        }

        // clear the input
        reader.reset();

        return isStringValues;

    }

    public InputParser useLast() {
        use_last_data_index_as_output = true;
        stored_outputs = Arrays.asList(reader.dataLength()-1);
        return this;
    }

    private void verifyOutputsExist(String location) {
        if (stored_outputs == null) {
            System.err.println(location + "() : Parsing with no stored inputs!");
            throw new RuntimeException(location + "() : Atttempting to parse examples with no output columns provided!");
        }
    }

    public InputParser withOutputs(int... outputs) {
        stored_outputs = new ArrayList<>();
        for (int out : outputs)
            stored_outputs.add(out);
        return this;
    }

    public InputParser withOutputs(List<Integer> outputs) {
        stored_outputs = new ArrayList<>();
        stored_outputs.addAll(outputs);
        return this;
    }

    public int ninputs() {
        verifyOutputsExist("ninputs");
        int dl = reader.dataLength();
        return dl - stored_outputs.size();
    }

    public int noutputs() {
        verifyOutputsExist("noutputs");
        return stored_outputs.size();
    }

    public double mappingFor(int data_index, String value) {
        return map.indexFor(data_index,value);
    }

    public IndexMap indexMap() {
        return map;
    }

    public List<Integer> outputIndices() {
        return stored_outputs;
    }

    public InputParser addBagFilter(int inputIndex, BoW bag) {
        bagFilters.add(new BagFilter(bag, inputIndex));
        return this;
    }

    public void rewriteBags() {
        for (BagFilter bagFilter : bagFilters) {
            bagFilter.bag.write();
        }
    }




//    public static StringInputResult GenerateInputResult(String data_string, String output_indices) {
//        // Split up request data
//        String[] outputs_strings = output_indices.split(",");
//        // fill into usable data lists
//        List<Integer> outputs = new ArrayList<>();
//        for (String output : outputs_strings) {
//            outputs.add(StringUtils.getInt(output));
//        }
//        // Parse input examples
//        Example.ParseResult pr = Example.parsedString(data_string,outputs);
//        // Get best-fit network dimensions
//        NeuralNetwork.LearningDimensions dimensions = NeuralNetwork.bestFitNetwork(pr.exs,pr.isStrings, pr.nm);
//        // n x 5 x 4 x n_outputs neural net
//        NeuralNetwork nn = new NeuralNetwork()
//                .withInputs(pr.exs.get(0).inputs.size())
//                .withHiddens(dimensions.l1_size,dimensions.l2_size)
//                .withOutputs(outputs.size())
//                .initialized();
//        return new InputParser.StringInputResult(nn,pr.exs, outputs, dimensions.learningRate, pr.isStrings, pr.nm);
//    }

}
