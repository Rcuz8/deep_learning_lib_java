package com.ai.print;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class Log {
    public static void log(Object o) { System.out.print(o); }
    public static void logn(Object o) {
        System.out.println(o);
//        try {
//            FileWriter myWriter = new FileWriter("output/outfile.txt", true);
//            myWriter.append(o.toString() + "\n");
//            myWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
    public static void loga(List<Object> o) { log("[ "); o.forEach((ob) -> log(ob.toString() + " ")); log("]"); }
    public static void loga(Object[] o) { log("[ "); Arrays.asList(o).forEach((ob) -> log(ob.toString() + " ")); log("]"); }
    public static void err(Object o) { System.err.println(o); }
}
