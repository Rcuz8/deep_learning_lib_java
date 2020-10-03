//package com.ai.input;
//
//import com.ai.learn.general.Example;
//
//import java.util.Arrays;
//import java.util.List;
//
//public class IHHelper {
//
//    List<Integer> outs;
//    InputHandler handler;
//    String filename;
//    String tkn;
//    public IHHelper(String filename, String tkn) {
//        handler = new InputHandler(filename,tkn);
//        outs = Arrays.asList(InputHandler.Data_length(filename,tkn)-1);
//        this.filename = filename;
//        this.tkn = tkn;
//    }
//
//    public IHHelper outputs(int... outs) {
//        this.outs.clear();
//        for (int out : outs) {
//            this.outs.add(out);
//        }
//        return this;
//    }
//
//    public IHHelper output_useLast() {
//        outs = Arrays.asList(InputHandler.Data_length(filename,tkn)-1);
//        return this;
//    }
//
//    public List<Example> parse() {
//        return handler.Examples_parse(outs);
//    }
//
//
//}
