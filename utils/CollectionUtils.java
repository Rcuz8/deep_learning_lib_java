package com.ai.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollectionUtils {


    public static <E> List<E>  resultsIntersectionSet(List<List<E>> inputListOfLists )
    {
        Set<E> intersection = new HashSet<>();

        if ( !inputListOfLists.isEmpty() )
            intersection.addAll(inputListOfLists.get( 0 ));

        for ( List<E> filterResultList : inputListOfLists )
        {
            intersection.retainAll( filterResultList );
        }

        return new ArrayList<>( intersection );
    }

    public static <E> List<E> combine(List<List<E>> LOL )
    {
        List<E> list = new ArrayList<>();
        for (List<E> es : LOL) {
            list.addAll(es);
        }
        return list;
    }

    public static <E> List<E> filterIndices(List<E> l, List<Integer> indices) {
        List<E> x = new ArrayList<>();
        for (int i = 0; i < l.size(); i++) {
            if (!indices.contains(i))
                x.add(l.get(i));
        }
        return x;
    }

    public static List<Integer> intCasted(List<String> list) {
        if (list == null) return null;
        List<Integer> objlist = new ArrayList<>();
        for (String s: list) {
            if (StringUtils.isInt(s)) objlist.add(StringUtils.getInt(s));
            else return null;
        }
        return objlist;
    }

    public static List<Object> properlyParsedList(List<String> list, IndexMap map) {
        List<Object> objlist = new ArrayList<>();
        int index = 0;
        for (String s: list) {
            if (StringUtils.isDouble(s)) objlist.add(StringUtils.getDouble(s));
            else if (StringUtils.isInt(s)) objlist.add(StringUtils.getInt(s));
            else {
                if (map.indexFor(index,s) == -1) map.put(index,s);
                objlist.add(map.indexFor(index,s)); // add the continuous-val index, not the string data
            }
            index++;
        }
        return objlist;
    }

    public static List<Object> properlyParsedList_standalone(List<String> list) {
        List<Object> objlist = new ArrayList<>();
        for (String s: list) {
            if (StringUtils.isDouble(s)) objlist.add(StringUtils.getDouble(s));
            else if (StringUtils.isInt(s)) objlist.add(StringUtils.getInt(s));
            else objlist.add(s);
        }
        return objlist;
    }

    // 0 = false, 1 = true
    public static List<Double> isStringIndices(List<String> list) {
        List<Double> isString = new ArrayList<>();
        for (String s: list) {
            if (StringUtils.isDouble(s)) isString.add(0.0);
            else if (StringUtils.isInt(s))isString.add(0.0);
            else isString.add(1.0);
        }
        return isString;
    }

    public static List<Integer> Data_inputIndices(int data_size, List<Integer> output_indices) {
        List<Integer> input_indices = new ArrayList<>();
        for (int i = 0; i < data_size; i++) {
            if (!output_indices.contains(i))
                input_indices.add(i);
        }
        return input_indices;
    }

    public static List<Integer> copy(List<Integer> prev) {
        List<Integer> list = new ArrayList<>();
        for (int v : prev) {
            list.add(v);
        }
        return list;
    }

    public static boolean allExist(Object... objects) {
        for (Object object : objects) {
            if (object == null)
                return false;
        }
        return true;
    }

    

}
