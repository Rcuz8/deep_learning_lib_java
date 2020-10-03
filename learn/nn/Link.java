package com.ai.learn.nn;

import com.ai.learn.nn.unit.Unit;
import com.ai.math.Utils;

import static com.ai.math.Utils.rounded2;

/*
    A link. as implemented here, is a non-evaluating connection between units.
    Functions:
        - Get from/to values
        - Update link weight
 */
public class Link {

    public Unit from;
    public Unit to;
    private double weight;
    public boolean isBias = false;

    /* Contructors - Create a simple weighted connection */
    public Link(Unit from, Unit to, double weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }
    public Link(Unit from, Unit to) {
        this.from = from;
        this.to = to;
        weight = 1;
    }
    /* Bias contructor */
    public static Link BiasLink(Unit to, double bias) {
        Link link = new Link(null, to, bias);
        link.isBias = true;
        return link;
    }

    /* values -> there is no evaluation done here, just retrieval of from & to values */

    /* Get From node's value
        NOTE: if bias node, will return 1
    */
    public double fromValue() {
        if (isBias) return 1;
        else return from.getValue();
    }
    /* Get To node's value
        NOTE: if output node, will return 0
    */
    public double toValue() {
        if (to != null)
            return to.getValue();
        else return 0;
    }

    public double weight() { return weight; }
    /* Set weight
        In addition to changing the value, this should update & notify
        the unit's counterpart.
    */
    public void setWeight(double to_weight) {
        weight = Utils.deepround(to_weight);
        // expensive, but part of the deal
        if (!isBias)
            update_and_notify_Counterpart();
    }
    /* ONLY FOR USE IN update_and_notify_Counterpart() */
    public void setWeight_noNotify(double to_weight) {
        this.weight = to_weight;
    }
    /*
        1. updates the link's counterpart
        2. notify the node at the other end of the counterpart
            that a change has been made
     */
    private void update_and_notify_Counterpart() {
        if (from != null) {
            from.linkTo(to).setWeight_noNotify(weight);
            from.LinkUpdated();
        }
        if (to != null) {
            to.linkTo(from).setWeight_noNotify(weight);
            to.LinkUpdated();
        }
    }


    /* Topical - equals / descriptors */

    @Override
    public boolean equals(Object obj) {
        return obj.toString().equals(toString());
    }

    /*  'a(i,j) -> 0.02 -> a(i+1,k)' */
    @Override
    public String toString() {
        if (!isBias)
            return "" + from.smallstring() + " -> " + rounded2(weight) + " -> " + to.smallstring() + "";
        else return (to.layer == null || to.layer.isInputLayer() ? "[INPUT]" : "bias") + " -> " + rounded2(weight)  + " -> " + to.smallstring() + "";
    }
    /*  'a(i,j)' OR 'bias' */
    public String fromNodeString() {
        if (isBias) return "bias ";
        else return from.smallstring();
    }
    /* 'a(i,j)' OR '[Empty]' */
    public String toNodeString() {
        if (to == null) return "[Empty]";
        else return to.smallstring();
    }
    /*  'a(i,j) -> 0.02' */
    public String inputString() {
        if (!isBias)
            return "" + from.smallstring() + " -> " + rounded2(weight) + "("+rounded2(from.getValue())+")";
        else return (to.layer == null || to.layer.isInputLayer() ? "[INPUT]" : "bias") + " -> " + rounded2(weight);
    }
    /*  '0.02' OR '[0.02]' */
    public String stringWeight() {
        if (isBias) return "[" + rounded2(weight) + "]";
        else return rounded2(weight)  + "";
    }
}
