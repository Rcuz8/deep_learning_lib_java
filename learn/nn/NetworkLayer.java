package com.ai.learn.nn;
import com.ai.learn.nn.unit.Unit;
import com.ai.math.Utils;
import com.ai.utils.JSONBuilder;

import java.util.ArrayList;
import java.util.List;

import static com.ai.print.Log.logn;


/*
    A Network Layer, as implemented here, is a list of nodes, interconnected
    to the nodes in adjacent layers. The network layer itself only manages the
    connections between adjacent layers.
    FUNCTIONS:
        - Add unit
        - Remove unit
 */
public class NetworkLayer {
    // Network the layer is a part of
    NeuralNetwork network;
    // nodes in this layer
    public List<Unit> units;
    // index of layer within the network
    private int index;

    /* Create net layer - Simple initializations */
    public NetworkLayer(int index, NeuralNetwork network) {
        this.index = index;
        this.units = new ArrayList<>();
        this.network = network;
    }
    /* Create net layer - Simple initializations */
    public NetworkLayer(List<Unit> units, int index, NeuralNetwork network) {
        this.units = units;
        this.index = index;
        this.network = network;
    }

    /* Adds unit to layer
        1. Set it's variables to fit into layer (index, layer)
        2. disconnect it from previous connections (local change, not made elsewhere if node is from another layer/network)
        3. connect it to its adjacent layers
    */
    public void addUnit(Unit unit) {
        // Set it's index
        unit.setIndexInLayer(units.size());
        // set it's layer
        unit.layer = this;
        // ensure unit has no previous connections
        unit.clearInputs();
        unit.clearOutputs();
        // if we're not in the input layer, connect previous layer's units to this one
        if (!isInputLayer()) {
            // connect each previous-layer input to this node &
            // connect this node to each previous-layer input
            dualconnect(prev(), unit, true);
        }
        // if we're not in the output layer, connect next layer's units to this one
        if (!isOutputLayer()) {
            // connect each next-layer input to this node &
            // connect this node to each next-layer input
            dualconnect(next(), unit, false);
        }
        // add unit to layer
        units.add(unit);
    }

    /* Remove unit from layer
    */
    public void removeUnit(int index_in_layer) {
        // ensure index is ok
        assert ( index_in_layer >= 0 && index_in_layer < units.size() ) : "Invalid unit removal index provided";
        // get unit
        Unit unit = units.get(index_in_layer);
        // disconnect from previous & next layers
        if (!isInputLayer())
            prev().disconnect(unit);
        if (!isOutputLayer())
            next().disconnect(unit);
        // remove unit
        units.remove(index_in_layer);
        // re-index units
        resetIndices();
    }

    /* Stick this layer between two others
        - For each unit
            - clear inputs & connect to previous layer's outputs
            - clear outputs & connect to next layer's inputs
    */
    public void reconnect() {
        // if this isn't input layer, disconnect inputs & reconnect to prev's outputs
        if (!isInputLayer()) {
            // for each node in current layer
            for (Unit unit : units) {
                // clear inputs
                unit.clearInputs();
                // reconnect to prev's outputs
                dualconnect(prev(), unit, true);
            }
        }
        // if this isn't output layer, disconnect outputs & reconnect to next's inputs
        if (!isOutputLayer()) {
            // for each node in current layer
            for (Unit unit : units) {
                // clear outputs
                unit.clearOutputs();
                // reconnect to next's inputs
                dualconnect(next(), unit, false);
            }
        }
    }



    /* CONNECTION FUNCTIONALITY */

    /* Creates two-way connection between a layer and a unit
        NOTE: unitIsOutput should be true if we're connecting a note backwards, false otherwise
    */
    private static void dualconnect(NetworkLayer layer, Unit unit, boolean unitIsOutput) {
        // connect each layer input to this node
        unit.addInputs(layer.unitLinks(unitIsOutput, unit));
        // connect this node to each layer input
        layer.connect(unitIsOutput,unit);
    }

    /* Removes two-way connection between a layer and a unit
    */
    private void disconnect(Unit from) {
        /* for each node in that layer, disconnect 'from' node */
        // for each node in current layer
        for (Unit unit: units) {
            // disconnect
            unit.disconnectFrom(from);
        }
    }

    /* Connect every node in the layer to some unit
        NOTE: Connection will be of weight 1.0
    */
    private void connect(boolean node_is_on_next_layer, Unit dst) {
        // for each node in current layer
        for (Unit unit : units) {
            // if the node is on the next layer (we're creating a path TO the node "dst"),
            // create outward link on current-layer node towards "dst".
            if (node_is_on_next_layer) unit.addOutput(new Link(unit,dst,1));
            // otherwise, create inward link from "dst" to current-layer node
            else unit.addInput(new Link(dst,unit,1));
        }
    }

    /* Get List of links from current layer to some unit.
        @param node_is_on_next_layer Should be true if the connection is going outwards, else false.
        NOTE: Connection will be of weight 1.0
     */
    public List<Link> unitLinks(boolean node_is_on_next_layer, Unit dst) {
        // Initialize list of links
        List<Link> links = new ArrayList<>();
        // for each node in current layer
        for (Unit unit : units) {
            // if the node is on the next layer (we're creating a path TO the node "dst"),
            // add outward link from current-layer node towards "dst".
            if (node_is_on_next_layer)
                links.add(new Link(unit,dst,1));
            // otherwise, add inward link from "dst" to current-layer node.
            else
                links.add(new Link(dst,unit,1));
        }
        // return list of links
        return links;
    }



    /* ADDITIONAL */


    /* Getters/Setters */

    public int index() { return index; }
    public void setIndex(int to) { index = to; }



    /* Helpers */

    // is input/output layer
    public boolean isInputLayer() { return prev() == null; }
    public boolean isOutputLayer() { return next() == null; }
    // Next & Previous layers
    public NetworkLayer prev() { if (index == 0) return null; return network.layers.get(index-1); }
    public NetworkLayer next() { if (index == network.layers.size()-1) return null; return network.layers.get(index-1); }

    /* Reset unit indices */
    public void resetIndices() {
        // for each node in layer
        for (int i = 0; i < units.size(); i++) {
            // set layer i's index to i
            units.get(i).setIndexInLayer(i);
        }
    }

    /* (For Printing/accounting purposes), Get all inbound link weights
        NOTE: return empty list if input layer
     */
    public List<Double> getIncomingLinkWeights() {
        List<Double> weights = new ArrayList<>();
        // if input layer, return empty;
        if (isInputLayer()) return weights;
        // add each node's inbound link weights to collection
        for (Unit unit: units)
            weights.addAll(unit.getIncomingLinkWeights());
        // return collection
        return weights;
    }

    /* Generate random weights for each node's incoming links */
    public void randomizeIncomingLinkWeights() {
        // if input layer, stop
        if (isInputLayer()) return;
        // for each node, randomize it's incoming weights
        for (Unit unit: units)
            unit.randomizeIncomingLinkWeights();
    }

    /* Get all node's inbound links
        NOTE: return empty list if input layer
     */
    public List<Link> getIncomingLinks() {
        List<Link> links = new ArrayList<Link>();
        // if input layer, return empty;
        if (isInputLayer()) return new ArrayList<Link>();
        for (Unit u: units)
            links.addAll(u.getIncomingLinks());
        return links;
    }

    /* Topical */

    @Override
    public String toString() {
        String smtab = "\n\t";
        String mdtab = "\n\t\t";
        String s = smtab + "";
        if (isInputLayer())
            s+= "Input Layer " +mdtab;
        else if (isOutputLayer())
            s+= "Output Layer " +mdtab;
        else
            s+= "Hidden Layer (" + index +")" + mdtab;
        if (units.isEmpty()) s += "[Empty]";
        for (Unit unit : units) {
            s += unit.toString() + mdtab;
        }
        return s;
    }

    public String smallstring() {
        return ("L" + index());
    }

    public String json() {
        JSONBuilder bldr = new JSONBuilder();
        for (Unit unit : this.units) {
            bldr.append(unit.json());
        }
        String json = bldr.json();
        logn("Layer " + smallstring() + " has json: " + json);

        return json;
    }

}
