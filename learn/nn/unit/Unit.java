package com.ai.learn.nn.unit;

import com.ai.learn.classifier.LinearClassifier;
import com.ai.learn.nn.Link;
import com.ai.learn.nn.NetworkLayer;
import com.ai.math.Utils;
import com.ai.math.Vector;
import com.ai.utils.JSONBuilder;

import java.util.ArrayList;
import java.util.List;

import static com.ai.math.Utils.rounded;
import static com.ai.math.Utils.rounded2;
import static com.ai.print.Log.logn;

/*
    A unit, as implemented here, is a biased node with a few variables,
    interconnected to other nodes.
    Functions:
        - Update it's connections
        - Inform that a connection has been updated elsewhere (in the link class)
 */
public abstract class Unit extends LinearClassifier {

    /*
        Abstract:

        NOTE: All evaluation is done FROM INPUTS (Weights = input weights)

        A node (unit) has a value of Threshold( weights * inputs )
            weights = input link weights (that are adjusted via propagation & I control)
            inputs = results coming from previous nodes
            outputs = results of next nodes

        Values for the node:
            in = weights * inputs
            value = Threshold(in)

        To maintain formality, only use 'inputs' & 'outputs' for their next node's values,
        forget about the link weight, bc we have that stored internally.

     */
    // threshold(in)
    public double value = 0;
    // sum of inputs in_i
    public double in = 0;
    // unit's network layer
    public NetworkLayer layer;
    // input links -> ONLY USE UNWEIGHTED FROM_VALUE FROM EACH & mult that by weights
    public List<Link> inputs;
    // output links -> ONLY USE UNWEIGHTED FROM_VALUE FROM EACH & mult that by weights
    public List<Link> outputs;
    // index of node within it's layer
    private int unitIndexInLayer;

    /* Two ways to create a unit:
        - In a layer (with layer info)
        - Standalone, to be added later
    */

    /* Create Unit (with layer info
        This should do simple empty-initializers + add bias link
            - Link weights will be set at some other time */
    public Unit (NetworkLayer layer, int unitIndexInLayer, double bias) {
        // initiaze weights to empty list
        super();
        // set layer
        this.layer = layer;
        // initialize inputs & outputs to empty lists
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        // Add bias link (new input = 1, weight = bias)
        addBiasLink(bias);
        // set network index
        this.unitIndexInLayer = unitIndexInLayer;
    }

    /* Create Unit (with layer info
        This should do simple empty-initializers + add bias link
    */
    public Unit (double bias) {
        // initiaze weights to empty list
        super();
        // set layer
        this.layer = null;
        // initialize inputs & outputs to empty lists
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        // Add bias link (new input = 1, weight = bias)
        addBiasLink(bias);
        // set network index
        this.unitIndexInLayer = -1; // Ensure THIS GETS SET LATER
    }

    /* Add a bias link (add input = 1 (implemented in Link fromValue), weight = bias) */
    public void addBiasLink(double bias) {
        inputs.add(Link.BiasLink(this, bias));
        LinkUpdated();
    }

    /* Link updated -> Ensure weights match */
    public void LinkUpdated() {
        // clear weights
        weights.clear();
        // add bias weight in
        weights.add(1.0);
        // from 1 to N, add in the link weights
        for(int i = 1; i < inputs.size(); i++) {
            Link link = inputs.get(i);
            weights.add(link.weight());
        }
    }


    /* Getters/setters */
    public double getValue() {
        return value;
    }
    public void setValue(double to) { value = to; }
    public double getIn() {
        return in;
    }
    public void setIn(double in) {
        this.in = in;
    }
    public void setBias(double to) { inputs.get(0).setWeight(to); }
    public void setIndexInLayer(int to) { unitIndexInLayer = to; }
    public int getIndexInLayer() { return unitIndexInLayer; }

    /* UPDATE CONNECTIONS */
    public void addInputs(List<Link> ins) {
        inputs.addAll(ins);
        LinkUpdated();
    }
    public void addOutputs(List<Link> outs) {
        outputs.addAll(outs);
        LinkUpdated();
    }
    public void addInput(Link in) {
        inputs.add(in);
        LinkUpdated();
    }
    public void addOutput(Link out) {
        outputs.add(out);
        LinkUpdated();
    }
    public void removeInput(Link in) {
        inputs.remove(in);
        LinkUpdated();
    }
    public void removeOutput(Link out) {
        outputs.remove(out);
        LinkUpdated();
    }
    public void clearInputs() {
        if (inputs.isEmpty()) return;
        double bias = inputs.remove(0).weight();
        inputs.clear();
        weights.clear();
        addBiasLink(bias);
        LinkUpdated();
    }
    public void clearOutputs() {
        outputs.clear();
    }

    /* Get link to another node */
    public Link linkTo(Unit u) {
        // if it is on the next layer
        if (u.layer.index() == this.layer.index() + 1) {
            // search outputs
            for (Link link : outputs) {
                if (link.to == null) continue;
                if (link.to.equals(u)) return link;
            }
        } else if (u.layer.index() == this.layer.index() - 1) {
            // search inputs
            for (Link link : inputs) {
                if (link.from == null) continue;
                if (link.from.equals(u)) return link;
            }
        }
        return null; // this should never execute if things are setup correctly
    }

    /* Disconnect this unit from */
    public void disconnectFrom(Unit u) {
        // if it is on the next layer
        if (u.layer.index() == this.layer.index() + 1) {
            int removeIndex = -1;
            int current_index = 0;
            // search outputs
            for (Link link : outputs) {
                if (link.to.equals(u)) removeIndex = current_index;
                current_index++;
            }
            if (removeIndex != -1)
                outputs.remove(removeIndex);
        } else if (u.layer.index() == this.layer.index() - 1) {
            int removeIndex = -1;
            int current_index = 0;
            // search inputs
            for (Link link : inputs) {
                if (link.from.equals(u)) removeIndex = current_index;
                current_index++;
            }
            if (removeIndex != -1)
                inputs.remove(removeIndex);
        }
    }


    /* Helpers */

    /* Get all inbound links
        NOTE: return empty list if input-layer node
     */
    public List<Link> getIncomingLinks() {
        // if input-layer node, return empty;
        if (layer.isInputLayer()) return new ArrayList<Link>();
        return inputs;
    }

    /* (For Printing/accounting purposes), Get all inbound link weights
        NOTE: return empty list if input-layer node
     */
    public List<Double> getIncomingLinkWeights() {
        List<Double> weights = new ArrayList<>();
        // if input-layer node, return empty;
        if (layer.isInputLayer()) return weights;
        // for each inbound link, add weight
        for (Link input: inputs)
            weights.add(input.weight());
        return weights;
    }

    /* Generate random weights for incoming links */
    public void randomizeIncomingLinkWeights() {
        // if input-layer node, stop (we'd like to avoid touching those)
        if (layer.isInputLayer()) return;
        // for each inbound link, randomize weight
        for (Link input: inputs)
            input.setWeight(Utils.random_smallNumber());
        LinkUpdated();
    }

    /* Topical - equals / string */

    @Override
    public boolean equals(Object obj) {
        Unit other = (Unit) obj;
        if (other == null) return false;
        return other.layer.index() == layer.index() && other.unitIndexInLayer == unitIndexInLayer;
    }

    public String smallstring() {
        if (layer == null) return "a(DNE, " + unitIndexInLayer + ")";
        return "a(" + layer.index() + ", " + unitIndexInLayer + ")";
    }
    @Override
    public String toString() {
        String smtab = "\n\t\t";
        String mdtab = "\n\t\t\t";
        String bigtab = "\n\t\t\t\t";
        String s = smtab+ smallstring();
        if (layer == null || !layer.isInputLayer()) {
            s += mdtab+"weights: ";
            for (Double weight : weights) {
                s += rounded2(weight) + " ";
            }
        }
        s += mdtab+"inputs: ";
        for (Link input : inputs) {
            s += bigtab + input.inputString();
        }
        s += mdtab + "in_i: " + in;
        if (layer == null || !layer.isInputLayer()) {
            s += mdtab+ "OUT:  g( ";
            for (int i = 0; i < inputs.size(); i++) {
                s += rounded2(weights.get(i)) + "(" + rounded2(inputs.get(i).fromValue()) + ") ";
            }
            s += ") = " + value;
        } else {
            s += mdtab+ "OUT:  " + value;
        }

        return s;
    }

    public String json() {
        // Weight string
        String str = "[";

        for (Link link : inputs) {
            str += (link.weight()) + ",";
        }
        str = str.substring(0,str.length()-1) + ']';
        String json = new JSONBuilder().insert(smallstring(),str).json();
        return json ;
    }

    public abstract double g_prime(double z);


    // /* Get input values  -> DEPRECATED */
    // public List<Double> inputValues() {
    //     List<Double> inputValues = new ArrayList<>();
    //     for (Link input : inputs) {
    //         inputValues.add(input.fromValue());
    //     }
    //     return inputValues;
    // }
}
