package com.ai.learn.nn;
import com.ai.learn.general.Example;
import com.ai.learn.general.Logistic;
import com.ai.learn.general.Tanh;
import com.ai.learn.nn.unit.Unit;
import static com.ai.print.Log.logn;
import java.util.*;

/*
    A Network Trainer, as implemented here, will train a neural
    network using the Back-Prop Learning Algorithm.

    FUNCTIONS:
        - Initialize Units (to random values)
        - Learn (via propagation)

 */
public class NetworkTrainer {

    public NeuralNetwork network;

    public NetworkTrainer(NeuralNetwork network) {
        this.network = network;
    }

    /* Initialize */
    public void initializeUnits() {
        // for each layer in the network
        for (NetworkLayer layer : network.layers) {
            // randomize incoming weights
            layer.randomizeIncomingLinkWeights();
        }
    }

    /* TRAINING / BACK-PROPAGATION */

    /* Train on examples n times */
    public void BACKPROP_LEARNING(List<Example> examples, int ntimes, double alpha) {
        //  local variables: Δ, a vector of errors, indexed by network node
        Map<Unit, Double> Deltas = new HashMap<>();

        /* Momentum snippet */
        Map<Link, Double> PrevDeltas = new HashMap<>(); // momentum "prev wij" storage
        double momentum = 0.1;
        //  while (NOT stopping-criterion)
        while (ntimes > 0) {
            // NEW - may not help whatsoever
//            initializeUnits(); // ALERT ! NOT SURE IF THIS WILL F THINGS UP OR NOT -> IT WORKS FOR AN EX. WITHOUT THIS
            // for each example
            for (Example example : examples) {
                if (example == null)
                    continue;
                // set inputs
                SetInputs(example);
                // propagate inputs forward
                propagate();
                // propagate outputs backwards
                backprop(Deltas,example, alpha, PrevDeltas, momentum);
            }
            // decrement ntimes
            ntimes--;
        }
    }

    /* Set inputs of the network to those of some example */
    public void SetInputs(Example example) {
        // ensure network is not empty
        assert !network.layers.isEmpty() : "Can't evaluate example, layers is empty";
        // maintain index for example inputs
        int i = 0;
        // get example inputs (as Doubles)
        List<Double> ins = example.Inputs_casted();
        // for each node in the input layer
        for (Unit unit : network.first().units) {
            // get example input at index
            double xi = ins.get(i);
            // set its value to xi
            unit.setValue(xi);
            // increment i
            i++;
        }
    }

    /* Propagate forwards */
    public void propagate() {
        // for L=2 to N
        for (int l = 1; l < L(); l++) {
            // calculate each node's values at this layer
            calculateLayer(l);
        }
    }

    /* Propagate deltas backward from output layer to input layer */
    private void backprop(Map<Unit,Double> Deltas, Example example, double alpha, Map<Link,Double> PrevDeltas, double momentum) {
        // Get example outputs
        List<Double> Y = example.Outputs_casted();
        // for each node j in the output layer
        for (Unit j : network.last().units) {
            // get node's index within it's layer
            int j_index = j.getIndexInLayer();
            // calculate g'(inj)
            double g_ = g_prime(j.getValue());
            // get the output at the same index of j (yj)
            double yj = Y.get(j_index);
            // get j's value (aj)
            double aj = j.getValue();
            // compile into value
            double jvalue = g_ * (yj - aj);
            // Δ[j] ← g′(inj) × (yj − aj)
            Deltas.put(j, jvalue );
        }

        // for l = L − 1 to 1   (for each non-output layer, in reverse order)
        for (int l = outputLayer()-1; l > inputLayer(); l--) {
            // get the layer
            NetworkLayer layer = network.layers.get(l);
            // for each node i in layer l
            for (Unit i : layer.units) {
                // begin the sum for (link weight * delta[j])
                double sum = 0;
                // for each link weight that i is the predecessor to
                for (Link wij : i.outputs) {
                    // get connected node
                    Unit j = wij.to;
                    // get delta[j]
                    Double dj = Deltas.get(j);
                    // sum goes up by the link weight * delta[j]
                    sum += wij.weight() * dj ;
                }
                // calculate g'(in(i))
                double g_ = g_prime(i.getValue());
                // calculate Δ[i] ← g′(in(i)) × sum(for j: wi,j − Δ[j])
                double di = g_ * sum;
                // put into Deltas (Δ[i] = di)
                Deltas.put(i,di);
            }
        }

        /* Update every weight in network using deltas */
        // Get every link in the network
        List<Link> allLinks = network.allLinks();
        // for each weight wi,j in network
        for (Link wij: allLinks) {

            /* wi,j ← wi,j + α × ai × Δ[j] */

            // Get the connecting-from value from the link
            double ai = wij.fromValue();
            // Get the connecting-to value from the Deltas
            Double dj = Deltas.get(wij.to);

            /* momentum snippet */
            double wij_momentum = 0;
            if (PrevDeltas.containsKey(wij)) {
                wij_momentum = PrevDeltas.get(wij) * momentum;
            }

            // Calculate the change
            double delta_wij = alpha * ai * dj  /* + momentum */ + wij_momentum;
            // Calculate the new wij value
            double new_wij_weight = wij.weight()  + delta_wij;
            // Set new wij weight
            wij.setWeight(new_wij_weight);
            // Set prev delta
            PrevDeltas.put(wij,delta_wij);
        }

    }

    /* Helpers */

    /* Calculate each node's values at this layer */
    private void calculateLayer(int l) {
        // ensure layer index is ok
        assert (l > 0 && l <= L()-1) : "Passed invalid layer to calc layer.";
        // get layer
        NetworkLayer layer = network.layers.get(l);
        // for each node j in layer l do
        for (Unit j : layer.units) {
            // begin sum  of inputs * weights
            // sum = sum (input weight * ai (prev value -> 0-1))
            double sum = 0;
            // for each input weight of j
            for (Link link : j.inputs) {
                // add input weight * ai to sum
                sum += link.weight() * link.fromValue();
            }
            // set the input value of j to calculated sum
            j.setIn( sum );
            // set value of j to threshold(in)
            j.setValue(j.threshold(j.in));
        }
    }

    /* Calculate g'(z) -> (hw(x))(1-hw(x))
        @param z should be hw(x)
    */
    private double g_prime(double z) {
        return network.activationPrime(z);
    }

    /* Gets # layers in network */
    public int L() {
        return network.layers.size();
    }
    /* Index of input layer */
    public int inputLayer() {
        return 0;
    }
    public int outputLayer() {
        return L()-1;
    }

}
