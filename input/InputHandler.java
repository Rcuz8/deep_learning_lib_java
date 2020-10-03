//package com.ai.input;
//
//import com.ai.learn.general.Example;
//import com.ai.utils.CollectionUtils;
//import com.ai.utils.IndexMap;
//import com.ai.utils.StringUtils;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Scanner;
//
//import static com.ai.print.Log.logn;
//
//public class InputHandler {
//
//    File file; // don't use
//    Scanner sc;
//    int data_length;
//    String splittoken;
//    IndexMap map;
//
//    public InputHandler(String filename, String splittoken) {
//        try {
//
//            file = new File(filename);
//            sc = new Scanner(file);
//
//            this.splittoken = splittoken;
//
//            data_length = Data_length();
//
//            map = new IndexMap();
//
//        } catch (FileNotFoundException e) {
//            logn("File not found: " + filename);
//        }
//    }
//
//    /* Parses example from format:
//        name\tdata1\tdata2...
//     */
//
//    private Example nextExample(List<Integer> inputColumns, List<Integer> outputColumns, int exampleNumber){
//        String nextLine = sc.nextLine();
//        if (nextLine.isEmpty()) return null; // bad format
//
//        // split
//        List<String> strings = StringUtils.split(nextLine,splittoken);
//        // parse
//        List<Object> parsed_tokens = CollectionUtils.properlyParsedList(strings,map);
//
//        // sublists
//        List<Object> outputs = CollectionUtils.filterIndices(parsed_tokens,inputColumns);
//        List<Object> inputs = CollectionUtils.filterIndices(parsed_tokens,outputColumns);
//
//        return new Example(inputs, outputs, exampleNumber);
//    }
//
//    /*
//        @param inputColumns The column indices of the input data
//        @param outputColumns The column indices of the output data
//     */
//    public List<Example> Examples_parse(List<Integer> outputColumns) {
//        List<Integer> inputColumns = CollectionUtils.Data_inputIndices(data_length,outputColumns);
//        Scanner_restart();
//        List<Example> examples = new ArrayList<>();
//        // get each example
//        while (sc.hasNextLine())
//            examples.add(nextExample(inputColumns,outputColumns,examples.size()));
//        return examples;
//    }
//
//
//    int Data_length() {
//        try {
//            sc = new Scanner(file);
//            // first line is column titles (or just data, hopefully fully-filled -> will cause error if not fully-filled)
//            String s = sc.nextLine();
//            return StringUtils.split(s,splittoken).size();
//        } catch (FileNotFoundException e) {
//            logn("File not found!! ");
//            return 0;
//        }
//    }
//
//    public static int Data_length(String filename, String tkn) {
//        try {
//            Scanner sc = new Scanner(new File(filename));
//            // first line is column titles (or just data, hopefully fully-filled -> will cause error if not fully-filled)
//            String s = sc.nextLine();
//            return StringUtils.split(s,tkn).size();
//        } catch (FileNotFoundException e) {
//            logn("File not found!! ");
//            return 0;
//        }
//    }
//
//
//    public void Scanner_restart() {
//        try {
//            sc = new Scanner(file);
//
//            // Assume one line for column names
////            sc.nextLine();
//
//        } catch (FileNotFoundException e) {
//            logn("File not found!! ");
//        }
//    }
//
//
//}
