package com.ai.utils;


import com.ai.learn.general.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CleaningLady {

    static final String TKN_1 = ",";
    static final String TKN_2 = "!!!";
    static final double THRESHOLD_PERC = 0.7; // 70%

    public class CleanResult {
        public List<String> headers;
        public List<List<String>> data;
        public List<Integer> used_indices;
        public List<Integer> outputIndices;

        public CleanResult(List<String> headers, List<List<String>> data, List<Integer> used_indices, List<Integer> outputIndices) {
            this.headers = headers;
            this.data = data;
            this.used_indices = used_indices;
            this.outputIndices = outputIndices;
        }
    }

    public static List<String> split_data(String data) { return StringUtils.split(data, TKN_2); }

    private static class DataInfo {
        List<Integer> usedIndices;
        List<List<String>> data;

        public DataInfo(List<Integer> usedIndices, List<List<String>> data) {
            this.usedIndices = usedIndices;
            this.data = data;
        }
    }

    // Split up the data into rows/cols & get the indices being used
    public static DataInfo used_data_indices(List<String> rows) {

        // Initialize used
        int len = StringUtils.split(rows.get(0),TKN_1).size();
        // initialize usages
        List<Integer> usages = new ArrayList<>();
        while (usages.size() < len) usages.add(0);

        List<List<String>> data = new ArrayList<>(); // I'd like to only split data once, so I'm maintaining this here

        /* For each row of data, increment each element's usage */

        // for every row
        for (String row : rows) {
            // split up data by "," token
            List<String> splitrow = StringUtils.split(row,TKN_1);
            // for each data element
            for (int data_column = 0; data_column < splitrow.size(); data_column++) {
                // if it's not empty, increment usage amount
                if (splitrow.get(data_column) != null && !splitrow.get(data_column).isEmpty())
                    usages.set(data_column, usages.get(data_column) + 1);
            }
            // push data row to all data
            data.add(splitrow);
        }

        /* For each element's (aggregated) usage, if it passes the threshold, it is clean */

        List<Integer> usedIndices = new ArrayList<>();

        for (int data_column = 0; data_column < usages.size(); data_column++) {
            double usage = (double) usages.get(data_column);
            if (usage / rows.size() >= THRESHOLD_PERC)
                usedIndices.add(data_column);
        }

        return new DataInfo(usedIndices, data);

    }

    // Return new data with a trimmed width, given the data & indices that are clean
    public List<List<String>> trim_data_width(DataInfo di) {

        List<List<String>> cleanRows = new ArrayList<>();

        // for every data row
        for (int i = 0; i < di.data.size(); i++) {
            // get unclean row
            List<String> dirtyRow = di.data.get(i);
            // make a new, clean row of data
            List<String> cleanRow = new ArrayList<>();
            // NEW : remove rows with any empty data
            boolean allClean = true;
            // fill in the used indices
            for (Integer usedIndex : di.usedIndices) {
                String usedData = dirtyRow.get(usedIndex);
                cleanRow.add(usedData);
                if (usedData == null || usedData.length() == 0) allClean = false;
            }
            // NEW : don't add rows with any empty data
            if (allClean)
                // push new clean row
                cleanRows.add(cleanRow);
        }

        return cleanRows;

    }

    // retokenize, if need be, the data
    public String retokenize(List<List<String>> data) {
        String str = "";
        for (List<String> datum : data) {
            str += datum.toString() + TKN_2;
        }
        str = str.substring(0,str.length()-TKN_2.length());
        return str;
    }

    // clean up headers
    public List<String> clean_headers(List<String> headers, List<Integer> usedIndices) {
        return usedIndices.stream().map(index -> headers.get(index)).collect(Collectors.toList());
    }


    public CleanResult cleanse(String string_data, List<String> old_headers, List<Integer> old_outputIndices) {
        // Split up data into just rows
        List<String> rows = split_data(string_data);
        // Get the rows/cols info & the indices being used
        DataInfo info = used_data_indices(rows);
        // Trim the data width
        List<List<String>> clean_data = trim_data_width(info);
        // get new headers
        List<String> clean_headers = clean_headers(old_headers,info.usedIndices);
        // get new index mapping
        List<Integer> indexMapping = clean_headers.stream().map((header) -> old_headers.indexOf(header)).collect(Collectors.toList());
        List<Integer> clean_output_indices = old_outputIndices.stream().map((index) -> indexMapping.indexOf(index)).collect(Collectors.toList());
        // done
        return new CleanResult(clean_headers,clean_data,info.usedIndices, clean_output_indices);
    }




}
