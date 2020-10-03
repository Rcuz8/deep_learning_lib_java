package com.ai.learn.nn;
import static com.ai.math.Utils.round5;
import static com.ai.math.Utils.roundd;
import static com.ai.math.Utils.rounded2;
import static com.ai.print.Log.logn;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.ai.learn.general.*;
import com.ai.learn.nn.unit.*;
import com.ai.math.Utils;
import com.ai.math.Vector;
import com.ai.utils.*;

/*
    A neural network, as implemented here, is a trainable collection
    of layers of nodes.
    FUNCTIONS:
        - Add/remove layer
        - Add/remove node from layer
        - Train the network (refer to NetworkTrainer)
 */
public class NeuralNetwork {

    // list of layers
    public List<NetworkLayer> layers;
    public NetworkTrainer trainer;

    // Optional
    NetType type;
    private Activation activation = Logistic::threshold;
    private Activation activationPrime = Logistic::g_prime;

    // Creates a new network
    public NeuralNetwork() {
        layers = new LinkedList<>();
        trainer = new NetworkTrainer(this);
        type  = NetType.Sigmoid;
        activation = Logistic::threshold;
        activationPrime = Logistic::g_prime;
    }

    @FunctionalInterface
    public static interface Activation {
        double get(double z);
    }

    // get the activation
    public double activation(double z) {
        return activation.get(z);
    }
    // get the activation prime
    public double activationPrime(double z) {
        return activationPrime.get(z);
    }



    /*  ADD/REMOVE FROM NETWORK  */

    /* Append layer to network
        NOTE: null = new, empty layer
     */
    public void appendLayer(NetworkLayer layer) {
        // if it is a new layer, create it
        if (layer == null) {
            layer = new NetworkLayer(layers.size(),this);
        }
        // add layer to network
        layers.add(layer);
        // reconnect new layer with it's adjacent layers
        layer.reconnect();
    }
    /* Prepend layer to network
        NOTE: null = new, empty layer
     */
    public void prependLayer(NetworkLayer layer) {
        // if it is a new layer, create it
        if (layer == null) {
            layer = new NetworkLayer(0,this);
        }
        // prepend layer to network
        layers.add(0, layer);
        // reset each layer's index
        resetIndices();
        // reconnect new layer with it's adjacent layers
        layer.reconnect();
    }
    /* Insert layer to network at index
        NOTE: null = new, empty layer
     */
    public void insertLayer(NetworkLayer layer, int index) {
        // if it is a new layer, create it
        if (layer == null) {
            layer = new NetworkLayer(index,this);
        }
        // add layer to network
        layers.add(index, layer);
        // reset each layer's index
        resetIndices();
        // reconnect new layer with it's adjacent layers
        layer.reconnect();
    }
    /* Add unit to network */
    public void addUnit(int layer, Unit u) {
        layers.get(layer).addUnit(u);
    }
    /* Add unit to network */
    public void addOutputUnit(Unit u) {
        layers.get(layers.size()-1).addUnit(u);
    }
    /* Remove layer from network */
    public void removeLayer(int at) {
        assert (at >= 0 && at < layers.size()) : "Invalid removal index";
        // get layer
        NetworkLayer layer = layers.get(at);
        // for each node in the layer, remove the node
        while (layer.units.size() > 0)
            layer.removeUnit(0);
        // remove from layers
        layers.remove(at);
        // re-index layers
        resetIndices();
    }
    /* Remove node from layer */
    public void removeUnit(int layer, int unit) {
        assert (layer >= 0 && layer < layers.size()) : "Invalid removal index";
        // get layer
        NetworkLayer l = layers.get(layer);
        // remove node
        l.removeUnit(unit);
    }


    /* TRAINING */

    /* Train the network ntimes on examples with learning rate alpha */
    public void train(List<Example> examples, int ntimes, double alpha, boolean is_firstTrainingSet) {
        if (is_firstTrainingSet) trainer.initializeUnits();
        trainer.BACKPROP_LEARNING(examples, ntimes, alpha);
    }

    /* PREDICTION */
    public List<Double> predict(Example example) {
        // set the inputs
        trainer.SetInputs(example);
        // prop
        trainer.propagate();
        // return the outputs
        return outputs();
    }


    /* HELPERS */

    /* Get network outputs */
    public List<Double> outputs() {
        // initialize new outputs list
        List<Double> values = new ArrayList<>();
        // for each unit on last layer of network
        for (Unit unit : last().units) {
            // add unit's static value (NOTE: value is NOT calculated)
            values.add(unit.value);
        }
        // return output values
        return values;
    }

    /* Reset layer indices */
    public void resetIndices() {
        // for each layer in network
        for (int i = 0; i < layers.size(); i++) {
            // set layer i's index to i
            layers.get(i).setIndex(i);
        }
    }

    /* Set Link Weight of w(l,i,j) */
    public void setLink(int LayerIndex, int UnitFromIndex, int UnitToIndex, double to) {
        // get unit a(l,i)
        Unit u = unit(LayerIndex,UnitFromIndex);
        // get unit a(l+1,j)
        Unit u2 = unit(LayerIndex+1,UnitToIndex);
        // update connection
        u.linkTo(u2).setWeight(to);
    }

    /* Get Bias Link of w(l,i) */
    public Link getBiasLink(int LayerIndex, int UnitFromIndex) {
        // get unit a(l,i)
        Unit u = unit(LayerIndex,UnitFromIndex);
        // return connection weight
        return u.inputs.get(0);
    }

    /* Get Link Weight of w(l,i,j) */
    public Link getLink(int LayerIndex, int UnitFromIndex, int UnitToIndex) {
        // get unit a(l,i)
        Unit u = unit(LayerIndex,UnitFromIndex);
        // get unit a(l+1,j)
        Unit u2 = unit(LayerIndex+1,UnitToIndex);
        // return connection
        return u.linkTo(u2);
    }
    /* Get unit in some layer at some index */
    public Unit unit(int layer, int index) {
        // get layer
        NetworkLayer netlayer = layers.get(layer);
        return netlayer.units.get(index);
    }
    /* Get output layer */
    public NetworkLayer last() {
        if (layers.isEmpty()) return null;
        return layers.get(layers.size()-1);
    }
    /* Get # outputs in network */
    public int noutputs() {
        if (layers.isEmpty()) return 0;
        return layers.get(layers.size()-1).units.size();
    }
    /* Get Input Layer */
    public NetworkLayer first() {
        if (layers.isEmpty()) return null;
        return layers.get(0);
    }
    /* Get # inputs in network */
    public int ninputs() {
        if (layers.isEmpty()) return 0;
        return layers.get(0).units.size();
    }
    /* Generate new output layer */
    public void newOutputLayer() {
        // append null layer
        appendLayer(null);
    }
    /* Generate new input layer */
    public void newInputLayer() {
        // prepend null layer
        prependLayer(null);
    }
    /* Generate new layer at some index */
    public void newLayer(int index) {
        // insert null layer
        insertLayer(null, index);
    }

    /* Get all node's inbound links
        NOTE: return empty list if input layer
     */
    public List<Link> allLinks() {
        List<Link> links = new ArrayList<Link>();
        for (NetworkLayer layer: layers)
            links.addAll(layer.getIncomingLinks());
        return links;
    }


    /* TOPICAL */

    public void printWeights() {
        logn("Network weights: ");
        // for each layer
        for (NetworkLayer layer : layers) {
            // for each node
            for (Unit unit : layer.units) {
                // for each outbound link
                for (Link wij : unit.outputs) {
                    // print link weight
                    logn(wij);
                }
            }
        }
    }

    public String json() {
        JSONBuilder bldr = new JSONBuilder();
        for (NetworkLayer layer : this.layers) {
            bldr.insert("L" + layer.index(), layer.json());
        }
        return bldr.json();
    }

    @Override
    public String toString() {
        String s = "Neural Network: \n";
        // for each network layer
        for (NetworkLayer layer : layers) {
            // add layer to string
            s += layer.toString();
        }
        return s;
    }

    public enum NetType { Perceptron, Sigmoid, Tanh, ReLU };

    public NeuralNetwork type(NetType type) {
        this.type = type;
        if (type == NetType.Sigmoid) {
            activation = Logistic::threshold;
            activationPrime = Logistic::g_prime;
        } else if (type == NetType.Perceptron) {
            activation = Perceptron::threshold;
            activationPrime = Perceptron::g_prime;
        } else if (type == NetType.Tanh) {
            activation = Tanh::threshold;
            activationPrime = Tanh::g_prime;
        } else if (type == NetType.ReLU) {
            activation = ReLU::threshold;
            activationPrime = ReLU::g_prime;
        }

        return this;
    }

    public Unit newUnit() {
        if (type == NetType.Perceptron)
            return new PerceptronUnit(1);
        else if (type == NetType.Sigmoid)
            return new LogisticUnit(1);
        else if (type == NetType.Tanh)
            return new TanhUnit(1);
        else if (type == NetType.ReLU)
            return new ReLUUnit(1);
        return null;
    }

    public NeuralNetwork withInputs(int inputs ) {
        this.layers.clear();
        // add input layer
        this.newInputLayer();
        // fill out input layer
        while (inputs > 0) {
            this.addUnit(0, newUnit());
            inputs--;
        }
        return this;
    }

    public NeuralNetwork withHiddens(int... hiddens) {
        // for each hidden layer
        for (int j: hiddens) {
            if (j > 0)
                // add hidden layer
                this.newOutputLayer();
            // fill out hidden layer
            while (j > 0) {
                    this.addOutputUnit(newUnit());
                j--;
            }
        }
        return this;
    }

    public NeuralNetwork withOutputs(int outputs ) {
        // add output layer
        this.newOutputLayer();
        // fill out input layer
        while (outputs > 0) {
            this.addOutputUnit(newUnit());
            outputs--;
        }
        return this;
    }

    public NeuralNetwork initialized() {
        // shuffle weights
        this.trainer.initializeUnits();
        return this;
    }

    public static class LearningDimensions {
        public int l1_size;
        public int l2_size;
        public double learningRate;

        public LearningDimensions(int l1_size, int l2_size, double learningRate) {
            this.l1_size = l1_size;
            this.l2_size = l2_size;
            this.learningRate = learningRate;
        }
    }

    public static void main(String[] args) {
        double[] ins = {0.23, 1.7, -9};
        console.log("\nin=" + ins[0]);
        console.log("RELU - f(x): " + Utils.round5(Utils.leakyReLU(ins[0])) + "\t\tf'(x): "+ Utils.round5(Utils.leakyReLU_prime(ins[0])));
        console.log("TANH - f(x): " + Utils.round5(Utils.tanh(ins[0])) + "\t\tf'(x): "+ Utils.deepround(Utils.tanh_prime(ins[0])));
        console.log("\nin=" + ins[1]);
        console.log("RELU - f(x): " + Utils.round5(Utils.leakyReLU(ins[1])) + "\t\tf'(x): "+ Utils.round5(Utils.leakyReLU_prime(ins[1])));
        console.log("TANH - f(x): " + Utils.round5(Utils.tanh(ins[1])) + "\t\tf'(x): "+ Utils.round5(Utils.tanh_prime(ins[1])));

        console.log("\nin=" + ins[2]);
        console.log("RELU - f(x): " + Utils.round5(Utils.leakyReLU(ins[2])) + "\t\tf'(x): "+ Utils.round5(Utils.leakyReLU_prime(ins[2])));
        console.log("TANH - f(x): " + Utils.round5(Utils.tanh(ins[2])) + "\t\tf'(x): "+ Utils.round5(Utils.tanh_prime(ins[2])));
    }

    public static LearningDimensions bestFitNetwork(List<Example> exs, boolean[] isStringVector, NormalizationMap nm, List<Integer> output_indices) {
        int L1_MIN = 2;
        int L1_MAX = 5;
        int L2_MIN = 0;
        int L2_MAX = 2;
        double[] LRs = {5, 7.5, 12.5, 17};
        int N_EPOCHS = 85;
        int ninputs = exs.get(0).inputs.size();
        int noutputs = exs.get(0).outputs.size();
        double best_accuracy = Integer.MAX_VALUE;
        LearningDimensions best_dimensions = null;

        for (int i = L1_MIN; i < L1_MAX; i++) {
            for (int j = L2_MIN; j < L2_MAX; j++) {
                for (int k = 0; k < LRs.length; k++) {
                    NeuralNetwork nn = new NeuralNetwork()
                            .withInputs(ninputs)
                            .withHiddens(i, j)
                            .withOutputs(noutputs)
                            .initialized();
                    nn.train(exs,N_EPOCHS,LRs[k],false);
                    // Accuracy is the sum of all the output accuracies of a network
                    double acc = Yardstick.avgAccuracy(nn, exs,isStringVector, nm, output_indices);
                    System.out.println("Test ("+i+","+j+","+LRs[k]+") = " + acc);
                    if (acc < best_accuracy) {
                        best_accuracy = acc;
                        best_dimensions = new LearningDimensions(i,j,LRs[k]);
                    }
                }
            }
        }
        console.log("Best-Fit Network dimensions are: \n\tL1: " + best_dimensions.l1_size + "\n\tL2: " + best_dimensions.l2_size + "\n\tLR: " + best_dimensions.learningRate );
        return best_dimensions;
    }

    public static class TrainingParams {
        public List<Example> training_data;
        public List<Example> testdata;
        public double alpha;
        public int epochs;
        public int notificationInterval;
        public TrainingParams(List<Example> training_data, List<Example> testdata, double alpha, int epochs, int notificationInterval) {
            this.training_data = training_data;
            this.testdata = testdata;
            this.alpha = alpha;
            this.epochs = epochs;
            this.notificationInterval = notificationInterval;
        }
    }

    public void train_segmented(TrainingParams trainingParams, Yardstick.EvaluationParameters evalParams) {
        logn("\n(.) Training network (seg)..");
        int epochs = trainingParams.epochs;
        double alpha = trainingParams.alpha;
        double minAlpha = 0.4;
        while (epochs > 0) {
            train(trainingParams.training_data, 1, alpha, false);
            double x = trainingParams.epochs-epochs;
            if (epochs % trainingParams.notificationInterval == 0) {
                double acc = Yardstick.accuracy_avg(this,trainingParams.testdata,evalParams);
                logn("Accuracy: " + rounded2(acc) + "\t\tmse: " + rounded2(Yardstick.mseAvg(this,trainingParams.testdata))+ "\t\talpha: " + rounded2(alpha) + "\t\t\tn=" + (int) x);
                if (acc > 0.94) {
                    logn("\n\n\t\t~~~ Converged ~~~");
                    logn("\n\tEpochs to converge: " + (int) x);
                    break;
                }
            }
//            alpha = Utils.decay(x, trainingParams.alpha, trainingParams.epochs) + minAlpha;
            epochs--;
        }
    }


//
//    /* Get the model's error^2 on some example */
//    private double errorOn(Example example, boolean[] isStringVector, LossMap lm) {
//        if (example == null)
//            return 0;
//        // create sum
//        double sum_error = 0;
//        // get inputs
//        List<Double> inputs = example.Inputs_casted();
//        // get expected outputs
//        List<Double> expected = example.Outputs_casted();
//        // get actual outputs
//        List<Double> actual = predict(example);
//        // for each input
//        for (int i = 0; i < expected.size(); i++) {
//            // Make informal guesses
//            int expected_rnd = (int) Math.round(expected.get(i));
//            int actual_rnd = (int) Math.round(actual.get(i));
//            boolean same_rnd = expected_rnd == actual_rnd;
//            // input is a string,
//            if (isStringVector[i]) {
//                // if both rounded integers are equal, model would predict correctly -> loss = 0
//                if (same_rnd) {
//                    // loss = 0, do nothing
//                } else {
//                    // otherwise, loss = 0.5
//                    sum_error += STR_MISS_LOSS;
//                    // add formal mistake to loss map
//                    lm.addLoss(expected_rnd,actual_rnd);
//                }
//            } else { /* Not a string value -> treat normally */
//
//                // add (expected - actual)^2 to the sum
//                sum_error += ( Math.pow(expected.get(i) - actual.get(i), 2) );
//                // If not the same expected value
//                if (!same_rnd) {
//                    // add formal mistake to loss map
//                    lm.addLoss(expected_rnd,actual_rnd);
//                }
//
//            }
//        }
//        return sum_error;
//    }
//
//    /* Returns mean-squared error (Accuracy) of model on some examples */
//    public double accuracy(List<Example> examples, boolean[] isStringVector) {
//        double sum_error = 0;
//        double total = examples.size();
//        // create loss map
//        LossMap lm = new LossMap();
//        for (Example example : examples) {
//            if (example == null)
//                continue;
//            sum_error += errorOn(example, isStringVector, lm);
//        }
//        return sum_error / total;
//    }


}
