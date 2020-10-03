package com.ai.learn.nn.test;

import com.ai.input.FileInputReader;
import com.ai.input.InputParser;
import com.ai.learn.general.Example;
import com.ai.learn.nn.Link;
import com.ai.learn.nn.NeuralNetwork;
import com.ai.learn.nn.Yardstick;
import com.ai.learn.nn.unit.LogisticUnit;
import com.ai.learn.nn.unit.Unit;
import com.ai.nlp.BoW;
import com.ai.utils.MapItem;
import com.ai.utils.NormalizationMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ai.math.Utils.rounded2;
import static com.ai.print.Log.logn;
import static com.ai.print.Log.log;

public class nntest {

    static String test_file_folder_loc = "src/test/data/nn/";

    public static InputParser Get_XOR_Parser() {
        String filename = test_file_folder_loc + "XOR";
        String tkn = ",";
        FileInputReader reader = new FileInputReader(filename,tkn);
        logn(reader.entireInput());
        return new InputParser(reader).withOutputs(2,3);
    }

    public static InputParser Get_Iris_Parser() {
        String filename = test_file_folder_loc + "iris.data.txt";
        String tkn = ",";
        return new InputParser(new FileInputReader(filename,tkn)).useLast();
    }

    public static InputParser Get_RPI_Parser() {
//        String filename = test_file_folder_loc + "hob_rpi.data.txt";
        String filename = test_file_folder_loc + "rpi_simple.txt"; // 97% accuracy
        String syns = test_file_folder_loc + "data-synonyms.txt";
        String tkn = ",";
        FileInputReader reader = new FileInputReader(filename,tkn);
        logn(reader.entireInput());
        return new InputParser(reader)
                .withOutputs(7,9)
                .addBagFilter(9,new BoW().from(syns).fetch());
    }

    public static InputParser Get_SLU_Parser() {
        String filename = test_file_folder_loc + "hob_slu.data.txt";
        String syns = test_file_folder_loc + "data-synonyms.txt";
        String tkn = ",";
        FileInputReader reader = new FileInputReader(filename,tkn);
        logn(reader.entireInput());
        return new InputParser(reader)
                .withOutputs(8)
                .addBagFilter(8,new BoW().from(syns).fetch());
    }

    public static void RUN(NeuralNetwork nn, InputParser parser,double LR, List<MapItem<Integer,Integer>> buckets) {
        boolean[] isCategorical = parser.isStringValues();
        List<Example> exs = parser.Examples_parse();
        parser.rewriteBags();
        NormalizationMap nm = Example.normalize(exs, isCategorical);
        if (buckets != null) {
            for (MapItem<Integer, Integer> bucket : buckets) {
                int index = bucket.key;
                int nbuckets = bucket.value;
                Example.List_BucketInput(exs,index,nbuckets);
            }
        }
        double stdDev = 0.1;
        int ninterval = 300;
        int epochs = 6000;
        NeuralNetwork.TrainingParams trainingParams = new NeuralNetwork.TrainingParams(exs,exs,LR,epochs,ninterval);
        Yardstick.EvaluationParameters evalParams = new Yardstick.EvaluationParameters(nm,isCategorical,parser.outputIndices(),stdDev);
        nn.train_segmented(trainingParams,evalParams);
    }

    public static void RPI_RUN() {
        NeuralNetwork nn = new NeuralNetwork()
                .type(NeuralNetwork.NetType.Tanh)
                .withInputs(8)
                .withHiddens(9)
                .withOutputs(2)
                .initialized();
        InputParser parser = Get_RPI_Parser();
        double LR = 0.1;
        List<MapItem<Integer, Integer>> buckets = Arrays.asList(
                new MapItem<>(/* data index */ 0 ,/* # buckets */ 3),
                new MapItem<>(/* data index */ 3 ,/* # buckets */ 3),
                new MapItem<>(/* data index */ 5 ,/* # buckets */ 3)
                );
        RUN(nn,parser,LR, buckets);
    }

    public static void SLU_RUN() {
        NeuralNetwork nn = new NeuralNetwork()
                .type(NeuralNetwork.NetType.Sigmoid)
                .withInputs(7)
                .withHiddens(8)
                .withOutputs(1)
                .initialized();
        InputParser parser = Get_SLU_Parser();
        double LR = 4;
        List<MapItem<Integer, Integer>> buckets = Arrays.asList(
                new MapItem<>(/* data index */ 0 ,/* # buckets */ 3),
                new MapItem<>(/* data index */ 3 ,/* # buckets */ 3),
                new MapItem<>(/* data index */ 5 ,/* # buckets */ 3)
        );
        RUN(nn,parser,LR, buckets);
    }

    public static void XOR_RUN() {
        NeuralNetwork nn = new NeuralNetwork()
                .withInputs(2)
                .withHiddens(2)
                .withOutputs(2)
                .initialized();
        InputParser parser = Get_XOR_Parser();
        double LR = 2;
        RUN(nn,parser,LR, null);
    }

    public static void IRIS_RUN() {
        NeuralNetwork nn = new NeuralNetwork()
                .withInputs(4)
                .withHiddens(7)
                .withOutputs(1)
                .initialized();
        InputParser parser = Get_Iris_Parser();
        double LR = 3;
        RUN(nn,parser,LR, null);
    }

    public static void main(String[] args) {
        SLU_RUN();
    }
}





/*

        DEPRECATED BELOW


 */


//
//    /* Correct Logistic 2-bit adder */
//    public static NeuralNetwork Generate_Correct_Logistic_XOR_Network() {
//        NeuralNetwork nn = new NeuralNetwork();
//        /* a(0,0) output weights */
//        double W_0_0_0 =  2;
//        double W_0_0_1 = -2;
//        /* a(0,1) output weights */
//        double W_0_1_0 =  2;
//        double W_0_1_1 = -2;
//        /* hidden layer 1 biases */
//        double b_1_0 = -1;
//        double b_1_1 =  3;
//        /* a(1,0) output weights */
//        double W_1_0_0 =  0; // or epsilon
//        double W_1_0_1 =  2;
//        /* a(1,1) output weights */
//        double W_1_1_0 = -2;
//        double W_1_1_1 =  2;
//        /* output biases */
//        double b_2_0 =  1;
//        double b_2_1 = -3;
//
//        /* Two Input layers, two hidden, two output */
//
//        nn.newOutputLayer();
//
//        nn.addUnit(0,new LogisticUnit(1.0));
//        nn.addUnit(0,new LogisticUnit(1.0));
//
//        nn.newOutputLayer();
//
//        nn.addUnit(1,new LogisticUnit(b_1_0));
//        nn.addUnit(1,new LogisticUnit(b_1_1));
//
//        nn.newOutputLayer();
//
//        nn.addUnit(2,new LogisticUnit(b_2_0));
//        nn.addUnit(2,new LogisticUnit(b_2_1));
//
//        nn.setLink(0,0,0,W_0_0_0);
//        nn.setLink(0,0,1,W_0_0_1);
//        nn.setLink(0,1,0,W_0_1_0);
//        nn.setLink(0,1,1,W_0_1_1);
//        nn.setLink(1,0,0,W_1_0_0);
//        nn.setLink(1,0,1,W_1_0_1);
//        nn.setLink(1,1,0,W_1_1_0);
//        nn.setLink(1,1,1,W_1_1_1);
//        return nn;
//    }
//
//    public static NeuralNetwork Generate_Test_XOR_Network() {
//        NeuralNetwork nn = new NeuralNetwork();
//        /* in-hidden layer weights (0) */
//        double W_0_0_0 = 0.3;
//        double W_0_0_1 = 0.5;
//        /* in-hidden layer weights (1) */
//        double W_0_1_0 = -0.7;
//        double W_0_1_1 = 0.1;
//        /* hidden layer 1 biases */
//        double b_1_0 = 0.1;
//        double b_1_1 = 0.5;
//        /* hidden-out layer weights (0) */
//        double W_1_0_0 = -0.2;
//        double W_1_0_1 = 0.9;
//        /* hidden-out layer weights (1) */
//        double W_1_1_0 = -0.2;
//        double W_1_1_1 = -0.3;
//        /* output biases */
//        double b_2_0 = -0.3;
//        double b_2_1 = -0.5;
//
//        /* Two Input layers, two hidden, two output */
//
//        nn.newOutputLayer();
//
//        nn.addUnit(0,new LogisticUnit(1.0));
//        nn.addUnit(0,new LogisticUnit(1.0));
//
//        nn.newOutputLayer();
//
//        nn.addUnit(1,new LogisticUnit(b_1_0));
//        nn.addUnit(1,new LogisticUnit(b_1_1));
//
//        nn.newOutputLayer();
//
//        nn.addUnit(2,new LogisticUnit(b_2_0));
//        nn.addUnit(2,new LogisticUnit(b_2_1));
//
//        nn.setLink(0,0,0,W_0_0_0);
//        nn.setLink(0,0,1,W_0_0_1);
//        nn.setLink(0,1,0,W_0_1_0);
//        nn.setLink(0,1,1,W_0_1_1);
//        nn.setLink(1,0,0,W_1_0_0);
//        nn.setLink(1,0,1,W_1_0_1);
//        nn.setLink(1,1,0,W_1_1_0);
//        nn.setLink(1,1,1,W_1_1_1);
//        return nn;
//    }
//
//
//
//    public static void n_ass(String test, double a, double b) {
//        boolean same = Math.abs(a-b) < 0.000001;
//        log("\nAssertion \"" + test);log("\": " + (same ? "PASSED" : "FAILED") /*+ "\t\t\t (" + a + ", " + b + ")"*/);
//    }
//
//    public static void ass(String test, boolean value) {
//        log("\nAssertion \"" + test);log("\": " + (value ? "PASSED" : "FAILED"));
//    }
//
//
//
//




//
//
//    /* Verify both directions of a link do, in fact, have some value */
//    public static void verifyDoubleLinkValue(NeuralNetwork nn, int l, int i, int j, double value) {
//        // Get both units
//        Unit ui = nn.unit(l,i);
//        Unit uj = nn.unit(l+1,j);
//        // Get both links
//        Link li = ui.linkTo(uj);
//        Link lj = uj.linkTo(ui);
//        ass("Link [" +  ui.smallstring() + "," + uj.smallstring() + "] has the correct value", li.weight() == value && lj.weight() == value);
//    }
//
//    /* Verify both directions of a link are, in fact, the same */
//    public static void verifyDoubleLinkConsistency(NeuralNetwork nn, int l, int i, int j) {
//        // Get both units
//        Unit ui = nn.unit(l,i);
//        Unit uj = nn.unit(l+1,j);
//        // Get both links
//        Link li = ui.linkTo(uj);
//        Link lj = uj.linkTo(ui);
//        ass("Link [" +  ui.smallstring() + "," + uj.smallstring() + "] is consistent", li.weight() == lj.weight());
//    }
//
//    /* Verify all double-links in a network */
//    public static void verifyLinks(NeuralNetwork nn) {
//        for (Link link: nn.allLinks())
//            if (!link.isBias)
//                verifyDoubleLinkConsistency(nn, link.from.layer.index(), link.from.getIndexInLayer(), link.to.getIndexInLayer());
//    }
//
//    /* Verify each link weight in a network */
//    public static void verify_test_XOR_link_weights(NeuralNetwork nn) {
//        /* in-hidden layer weights (0) */
//        double W_0_0_0 = 0.3;
//        double W_0_0_1 = 0.5;
//        /* in-hidden layer weights (1) */
//        double W_0_1_0 = -0.7;
//        double W_0_1_1 = 0.1;
//        /* hidden-out layer weights (0) */
//        double W_1_0_0 = -0.2;
//        double W_1_0_1 = 0.9;
//        /* hidden-out layer weights (1) */
//        double W_1_1_0 = -0.2;
//        double W_1_1_1 = -0.3;
//
//        verifyDoubleLinkValue(nn,0,0,0,W_0_0_0);
//        verifyDoubleLinkValue(nn,0,0,0,W_0_0_0);
//        verifyDoubleLinkValue(nn,0,0,1,W_0_0_1);
//        verifyDoubleLinkValue(nn,0,1,0,W_0_1_0);
//        verifyDoubleLinkValue(nn,0,1,1,W_0_1_1);
//        verifyDoubleLinkValue(nn,1,0,0,W_1_0_0);
//        verifyDoubleLinkValue(nn,1,0,1,W_1_0_1);
//        verifyDoubleLinkValue(nn,1,1,0,W_1_1_0);
//        verifyDoubleLinkValue(nn,1,1,1,W_1_1_1);
//    }
//
//    /* Verify correct backward-propagation values */
//    public static void verify_back_propagation_perc(NeuralNetwork nn) {
//
//        // below are the correct values of the XOR on input (0,1)
//        // after backward propagation
//
//        /* in-hidden layer weights (0) */
//        double W_0_0_0 = 0.3;
//        double W_0_0_1 = 0.5;
//        /* in-hidden layer weights (1) */
//        double W_0_1_0 = 0.481952;
//        double W_0_1_1 = 0.198496;
//        /* hidden layer 1 biases */
//        double b_1_0 = 1.281952;
//        double b_1_1 = 0.598496;
//        /* hidden-out layer weights (0) */
//        double W_1_0_0 = -0.2;
//        double W_1_0_1 = 0.9;
//        /* hidden-out layer weights (1) */
//        double W_1_1_0 = -0.2;
//        double W_1_1_1 = -1.668;
//        /* output biases */
//        double b_2_0 = -0.3;
//        double b_2_1 = -1.868;
//
//        Link res_w_0_0_0 = nn.getLink(0,0,0);
//        Link res_w_0_0_1 = nn.getLink(0,0,1);
//        Link res_w_0_1_0 = nn.getLink(0,1,0);
//        Link res_w_0_1_1 = nn.getLink(0,1,1);
//        Link res_w_1_0_0 = nn.getLink(1,0,0);
//        Link res_w_1_0_1 = nn.getLink(1,0,1);
//        Link res_w_1_1_0 = nn.getLink(1,1,0);
//        Link res_w_1_1_1 = nn.getLink(1,1,1);
//
//        Link res_b_1_0 = nn.getBiasLink(1,0);
//        Link res_b_1_1 = nn.getBiasLink(1,1);
//        Link res_b_2_0 = nn.getBiasLink(2,0);
//        Link res_b_2_1 = nn.getBiasLink(2,1);
//
//        n_ass("Link for " + res_w_0_0_0 + " is correct", res_w_0_0_0.weight(), W_0_0_0 );
//        n_ass("Link for " + res_w_0_0_1 + " is correct", res_w_0_0_1.weight(), W_0_0_1 );
//        n_ass("Link for " + res_w_0_1_0 + " is correct", res_w_0_1_0.weight(), W_0_1_0 );
//        n_ass("Link for " + res_w_0_1_1 + " is correct", res_w_0_1_1.weight(), W_0_1_1 );
//        n_ass("Link for " + res_w_1_0_0 + " is correct", res_w_1_0_0.weight(), W_1_0_0 );
//        n_ass("Link for " + res_w_1_0_1 + " is correct", res_w_1_0_1.weight(), W_1_0_1 );
//        n_ass("Link for " + res_w_1_1_0 + " is correct", res_w_1_1_0.weight(), W_1_1_0 );
//        n_ass("Link for " + res_w_1_1_1 + " is correct", res_w_1_1_1.weight(), W_1_1_1 );
//
//        n_ass("Bias link for " + res_b_1_0 + " is correct", res_b_1_0.weight(), b_1_0 );
//        n_ass("Bias link for " + res_b_1_1 + " is correct", res_b_1_1.weight(), b_1_1 );
//        n_ass("Bias link for " + res_b_2_0 + " is correct", res_b_2_0.weight(), b_2_0 );
//        n_ass("Bias link for " + res_b_2_1 + " is correct", res_b_2_1.weight(), b_2_1 );
//
//    }
//    /* Verify correct backward-propagation values */
//    public static void verify_back_propagation_log(NeuralNetwork nn, List<Example> exs) {
//        double[] data_1 = {0.3, 0.5, -0.397723627, 0.107973843, -0.104682352, 0.797212109, -0.026319922, -0.487291749, 0.402276373, 0.507973843, -0.031002273, -0.79007964};
//        double[] data_2 = {0.3, 0.5, -0.397723627, 0.107973843, -0.071786851, 0.996132265, 0.007953297, -0.280040492, 0.464527368, 0.467183013, 0.023893603, -0.458122502};
//        double[] data_3 = {0.301426214, 0.499925713, -0.397723627, 0.107973843, -0.06546358, 1.002014613, 0.014667841, -0.273794155, 0.465953582, 0.467108725, 0.033160697, -0.449501604};
//        double[] data_4 = {0.308353571, 0.500581404, -0.39079627, 0.108629535, -0.063963748, 1.019658744, 0.016558681, -0.251550181, 0.472880939, 0.467764417, 0.035696871, -0.419665876};
//        double[] data_5 = {0.308353571, 0.500581404, -0.392729448, 0.11017383, -0.067157855, 1.0063611, 0.012629865, -0.267906548, 0.470947762, 0.469308712, 0.029560372, -0.445213229};
//        // below are the correct values of the XOR on input (0,1)
//        // after backward propagation
//        double[][] data = {data_1,data_2,data_3, data_4, data_5};
//        int curr = data.length-1;
//
//        nn.train(exs, 1, 0.95, false);
//        for (Link link : nn.allLinks()) {
//            logn(link);
//        }
////
////            /* in-hidden layer weights (0) */
////            double W_0_0_0 = data[curr][0];
////            double W_0_0_1 = data[curr][1];
////            /* in-hidden layer weights (1) */
////            double W_0_1_0 = data[curr][2];
////            double W_0_1_1 = data[curr][3];
////            /* hidden layer 1 biases */
////            double b_1_0 = data[curr][8];
////            double b_1_1 = data[curr][9];
////            /* hidden-out layer weights (0) */
////            double W_1_0_0 = data[curr][4];
////            double W_1_0_1 = data[curr][5];
////            /* hidden-out layer weights (1) */
////            double W_1_1_0 = data[curr][6];
////            double W_1_1_1 = data[curr][7];
////            /* output biases */
////            double b_2_0 = data[curr][10];
////            double b_2_1 = data[curr][11];
////
////            Link res_w_0_0_0 = nn.getLink(0,0,0);
////            Link res_w_0_0_1 = nn.getLink(0,0,1);
////            Link res_w_0_1_0 = nn.getLink(0,1,0);
////            Link res_w_0_1_1 = nn.getLink(0,1,1);
////            Link res_w_1_0_0 = nn.getLink(1,0,0);
////            Link res_w_1_0_1 = nn.getLink(1,0,1);
////            Link res_w_1_1_0 = nn.getLink(1,1,0);
////            Link res_w_1_1_1 = nn.getLink(1,1,1);
////
////            Link res_b_1_0 = nn.getBiasLink(1,0);
////            Link res_b_1_1 = nn.getBiasLink(1,1);
////            Link res_b_2_0 = nn.getBiasLink(2,0);
////            Link res_b_2_1 = nn.getBiasLink(2,1);
////
////            n_ass("Link for " + res_w_0_0_0 + " is correct", res_w_0_0_0.weight(), W_0_0_0 );
////            n_ass("Link for " + res_w_0_0_1 + " is correct", res_w_0_0_1.weight(), W_0_0_1 );
////            n_ass("Link for " + res_w_0_1_0 + " is correct", res_w_0_1_0.weight(), W_0_1_0 );
////            n_ass("Link for " + res_w_0_1_1 + " is correct", res_w_0_1_1.weight(), W_0_1_1 );
////            n_ass("Link for " + res_w_1_0_0 + " is correct", res_w_1_0_0.weight(), W_1_0_0 );
////            n_ass("Link for " + res_w_1_0_1 + " is correct", res_w_1_0_1.weight(), W_1_0_1 );
////            n_ass("Link for " + res_w_1_1_0 + " is correct", res_w_1_1_0.weight(), W_1_1_0 );
////            n_ass("Link for " + res_w_1_1_1 + " is correct", res_w_1_1_1.weight(), W_1_1_1 );
////
////            n_ass("Bias link for " + res_b_1_0 + " is correct", res_b_1_0.weight(), b_1_0 );
////            n_ass("Bias link for " + res_b_1_1 + " is correct", res_b_1_1.weight(), b_1_1 );
////            n_ass("Bias link for " + res_b_2_0 + " is correct", res_b_2_0.weight(), b_2_0 );
////            n_ass("Bias link for " + res_b_2_1 + " is correct", res_b_2_1.weight(), b_2_1 );
//
//    }
//    /* Verify correct forward-propagation values */
//    public static void verify_propagation(NeuralNetwork nn) {
//
//        // below are the correct values of the XOR on input (0,1)
//        // after forward propagation
//
//        double a_0_0 = 0;
//        double a_0_1 = 1;
//        double a_1_0 = 0;
//        double a_1_1 = 1;
//        double a_2_0 = 0;
//        double a_2_1 = 0;
//
//        double in_1_0 = -0.6;
//        double in_1_1 =  0.6;
//        double in_2_0 = -0.5;
//        double in_2_1 = -0.8;
//
//        Unit u_0_0 = nn.unit(0,0);
//        Unit u_0_1 = nn.unit(0,1);
//        Unit u_1_0 = nn.unit(1,0);
//        Unit u_1_1 = nn.unit(1,1);
//        Unit u_2_0 = nn.unit(2,0);
//        Unit u_2_1 = nn.unit(2,1);
//
//        // Now we'll verify that we have, indeed actually
//        // gotten those same values
//
//        ass(u_0_0.smallstring() + " has correct value", u_0_0.getValue() == a_0_0);
//        ass(u_0_1.smallstring() + " has correct value", u_0_1.getValue() == a_0_1);
//        ass(u_1_0.smallstring() + " has correct value", u_1_0.getValue() == a_1_0);
//        ass(u_1_1.smallstring() + " has correct value", u_1_1.getValue() == a_1_1);
//        ass(u_2_0.smallstring() + " has correct value", u_2_0.getValue() == a_2_0);
//        ass(u_2_1.smallstring() + " has correct value", u_2_1.getValue() == a_2_1);
//
//        ass(u_1_0.smallstring() + " has correct input sum", u_1_0.getIn() == in_1_0);
//        ass(u_1_1.smallstring() + " has correct input sum", u_1_1.getIn() == in_1_1);
//        ass(u_2_0.smallstring() + " has correct input sum", u_2_0.getIn() == in_2_0);
//        ass(u_2_1.smallstring() + " has correct input sum", u_2_1.getIn() == in_2_1);
//
//    }
//
//    public static void verify_all_network_correct_outputs(NeuralNetwork nn, List<Example> exs) {
//
//        for (Example ex: exs) {
//            nn.trainer.SetInputs(ex);
//            nn.trainer.propagate();
//            ass("Example outputs are properly propagated", nn.outputs().equals(ex.Outputs_casted()));
//        }
//
//    }
//
//    public static void xor_test_all_forward_propagation_outputs() {
//        logn("\n(1) Generating a test net..");
//        NeuralNetwork nn = Generate_Correct_Logistic_XOR_Network();
//        List<Example> exs = Get_XOR_Examples();
//        logn("\n(2) Verifying network outputs..");
//        verify_all_network_correct_outputs(nn,exs);
//    }
//
//    public static void xor_test_one_deep_forward_propagation() {
//        logn("\n(1) Generating a test net..");
//        NeuralNetwork nn = Generate_Test_XOR_Network();
//        List<Example> exs = Get_XOR_Examples();
//        logn("\n(2) Verifying double-link consistency..");
//        // Verify double-link consistency
////        verifyLinks(nn);
//        logn("\n(3) Verifying link weight correctness..");
//        // Verify link weight correctness
////        verify_test_XOR_link_weights(nn);
//        logn("\n(4) Propagating network..");
//        // propagate network on example 1
//        nn.trainer.SetInputs(exs.get(0));
//        nn.trainer.propagate();
//        logn("\n(5) Verifying propagation correctness..");
//        log(nn.outputs());
//        for (Link l: nn.allLinks())
//            logn(l);
//        // Verify propagation
////        verify_propagation(nn);
//        //TODO: Verify back-propagation
//    }
//
//    public static void xor_test_back_propagation() {
//        logn("\n(1) Generating a test net..");
//        NeuralNetwork nn = Generate_Test_XOR_Network();
//        List<Example> exs = Get_XOR_Examples();
//        logn("\n(4) Propagating network..");
//        // propagate network on example 1
////        nn.train(exs.subList(0,1), 1, 0.95, false);
//        logn("\n(5) Verifying propagation correctness..");
////        for (Link l: nn.allLinks())
////            logn(l);
//        // Verify propagation
//        verify_back_propagation_log(nn, exs);
//        //TODO: Verify back-propagation
//    }