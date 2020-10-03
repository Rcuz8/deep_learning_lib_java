package com.ai.utils;

import com.ai.input.FileInputReader;
import org.apache.tomcat.jni.Error;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.lang.Exception;

public class JSONBuilder {

    public List<Element> elements = new ArrayList<>();

    public class Element {
        String key;
        Object value;

        public Element(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object other) {
            Element oth = (Element) other;
            return oth.key.equals(key);
        }
    }

    public static String jsonify(Object obj) {
        if (StringUtils.isInt(obj.toString()) || StringUtils.isDouble(obj.toString()))
            return obj.toString();
        if (obj.getClass() == String.class) {
            return obj.toString();
        }

        Class<?> objClass = obj.getClass();

        Field[] fields = objClass.getFields();
        String str = "{ ";
        for(Field field : fields) {
            String name = field.getName();
            try {
                Object value = field.get(obj);
                str += "\"" + name + "\": " + (value != null ? value.toString() : value) + ',';
            } catch (IllegalAccessException e) {/* Not an issue */}
        }
        str = str.substring(0,str.length()-1) + " }";
        return str;
    }

    // breaks if { is in not first 2 chars
    public static String content(String str) {
        return str.substring(2,str.length()-2); // or could do better & use extract() -> more expressive
    }

    public JSONBuilder insert(String key, Object value) {
        Element entry = new Element(key,value);
        if (elements.indexOf(entry) > -1) return this;
        elements.add(entry);
        return this;
    }

    public JSONBuilder append(String json) {
        elements.add(new Element(null, content(json)));
        return this;
    }

    public String json() {
        String str = "{ ";
        for (Element e : this.elements) {
            if (e.key != null)
                str += "\"" + e.key + "\": " + e.value.toString() + ", ";
            else
                str += e.value.toString() + ", ";
        }
        str = str.substring(0,str.length()-2) + " }";
        return str;
    }

    public String json_nl() {
        String str = "\n{ \n";
        for (Element e : this.elements) {
            if (e.key != null)
                str += "\"" + e.key + "\": " + e.value.toString() + ", ";
            else
                str += e.value.toString() + ", ";
            str += '\n';
        }
        str = str.substring(0,str.length()-3) + " }";
        return str;
    }

    /* Parsing */


    public static String objExtract(String json) {
        return json.substring(json.indexOf("{")+1,json.lastIndexOf("}")).trim();
    }
    public static String listExtract(String json) {
        return json.substring(json.indexOf("[")+1,json.lastIndexOf("]")).trim();
    }
    public static String strExtract(String json) {
        return json.substring(json.indexOf("\"")+1,json.lastIndexOf("\"")).trim();
    }

    /* Verify a string obeys certain tag set parameters */
    private static boolean tag_set_verify_string(String json, String ok_tag,String ok_tag_close, String[] bads) {
        boolean hasBrackets = json.contains(ok_tag) && json.contains(ok_tag_close);
        if (!hasBrackets) return false;

        for (int i = 0; i < bads.length; i++) {
            if (!json.contains(bads[i])) continue;
            if (json.indexOf(ok_tag) > json.indexOf(bads[i])) return false;
        }

        return true;
    }

    private static boolean isList(String json) {
        String ok_tag = "[";
        String ok_tag_close = "]";
        String bad_tag_1 = "{";
        String bad_tag_2 = "\"";
        String[] bad_tags = {bad_tag_1,bad_tag_2};
        return tag_set_verify_string(json,ok_tag,ok_tag_close,bad_tags);
    }

    private static boolean isObject(String json) {
        String ok_tag = "{";
        String ok_tag_close = "}";
        String bad_tag_1 = "[";
        String bad_tag_2 = "\"";
        String[] bad_tags = {bad_tag_1,bad_tag_2};
        return tag_set_verify_string(json,ok_tag,ok_tag_close,bad_tags);
    }

    private static boolean isString(String json) {
        String ok_tag = "\"";
        String ok_tag_close = "\""; // may be weird, but doesn't matter
        String bad_tag_1 = "{";
        String bad_tag_2 = "[";
        String[] bad_tags = {bad_tag_1,bad_tag_2};
        return tag_set_verify_string(json,ok_tag,ok_tag_close,bad_tags);
    }

    // simple
    public static boolean isValid(String json) {
        return json.contains("{") && json.contains("}");
    }

    private static class JSON_KV {
        String key,value;
        int lastIndexUsed;


        public JSON_KV(String key, String value, int lastIndexUsed) {
            this.key = key;
            this.value = value;
            this.lastIndexUsed = lastIndexUsed;
        }
    }
    // "hi": ...
    // NOTE: Cannot accept strings with comma (,) at end
    public static JSON_KV pullKV(String json) {
        int open_quo = json.indexOf("\"");
        int close_quo = json.substring(open_quo+1).indexOf("\"") + open_quo + 2;
        String key = json.substring(open_quo,close_quo);
        String remainder = json.substring(json.indexOf(":")+1).trim();
        Character close;
        int finalIndex;
        String value;
        if (remainder.charAt(0) == '{') {
            close = '}';
            finalIndex = remainder.lastIndexOf(close);
            value = remainder.substring(0,finalIndex+1);
        } else if (remainder.charAt(0) == '[') {
            close = ']';
            finalIndex = remainder.lastIndexOf(close);
            value = remainder.substring(0,finalIndex+1);
        } else if (remainder.charAt(0) == '\"') {
            close = '\"';
            finalIndex = remainder.substring(1).indexOf(close) + 2;
            value = remainder.substring(0,finalIndex);
        } else { // number
            finalIndex = remainder.indexOf(",")-1; // last char
            value = remainder.substring(1,finalIndex);
        }


        return new JSON_KV(key,value, finalIndex-1 + json.indexOf(remainder));

    }

    public static List<JSON_KV> pullKVs(String json) {
        String obj = objExtract(json).trim();
        List<JSON_KV> elements = new ArrayList<>();
        while (!obj.isEmpty()) {
            JSON_KV kv = pullKV(obj);
            elements.add(kv);
            obj = obj.substring(kv.lastIndexUsed+1); // move past KV pair
            if (!obj.contains(",")) break; // done if no more elements
            obj = obj.substring(obj.indexOf(",")+1).trim(); // cut out ,
        }
        return elements;
    }

    // "hi": ...
    public static MapItem<String, List<Object>> match(JSON_KV kv) {
        MapItem<String, List<Object>> num = matchNumber(kv);
        if (num != null)
            return new MapItem<>(kv.key,num.value);
        MapItem<String, List<Object>> str = matchString(kv);
        if (str != null)
            return new MapItem<>(kv.key,str.value);
        MapItem<String, List<Object>> arr = matchArray(kv);
        if (arr != null)
            return new MapItem<>(kv.key,arr.value);
        MapItem<String, List<Object>> obj = matchObject(kv);
        if (obj != null)
            return new MapItem<>(kv.key,obj.value);
        return null;
    }

    // "hi" : 5
    public static MapItem<String, List<Object>> matchNumber(JSON_KV kv) {
        if (!StringUtils.isDouble(kv.value)) return null;
        Double val = StringUtils.getDouble(kv.value);
        List<Object> valueList = Arrays.asList(val);
        return new MapItem<>(kv.key,valueList);
    }

    // "hi" : "there"
    public static MapItem<String, List<Object>> matchString(JSON_KV kv) {
        if (!isString(kv.value)) return null;
        List<Object> valueList = Arrays.asList(kv.value);
        return new MapItem<>(kv.key,valueList);
    }

    // "hi": [a,b,c] OR "hi": ['a','b',c] OR "hi": [ {"a": ..,..}, "b": .., "c":.. }
    public static MapItem<String, List<Object>> matchArray(JSON_KV kv) {
        if (!isList(kv.value)) return null;
        String val = listExtract(kv.value);
        return matchArray_express(kv.key, val);
    }

    // "hi":  {"a": ..,..}   -> Real return value = MapItem<String, List<MapItem<String, List<Object>>>>
    public static MapItem<String, List<Object>> matchObject(JSON_KV kv) {
        if (!isObject(kv.value)) return null;
        return matchObject_express(kv.key,kv.value);
    }

    public static MapItem<String, List<Object>> matchObject_express(String key, String json) {
        List<JSON_KV> object_elements = pullKVs(json);
        // List<MapItem>
        List<Object> elements = new ArrayList<>();
        for (JSON_KV object_element : object_elements) {
            // check if we can go deeper
            MapItem<String, List<Object>> match = match(object_element);
            if (match != null)
                elements.add(match);
            else
                elements.add(object_element.value);
        }

        return new MapItem<>(key,elements);
    }

    public static MapItem<String, List<Object>> matchArray_express(String key, String json) {
        // Some will be numbers, some str, some object
        List<Object> list = CollectionUtils.properlyParsedList_standalone(StringUtils.split(json,","));
        // Should go through list, matching objects
        for (int i = 0; i < list.size(); i++) {
            Object o = list.get(i);
            String _class = o.getClass().getName();
            // check if it's an object or array
            if (_class.equals("String")) { // it is
                String item = (String) o;  // = {"a": ..,..} or [...]
                if (isObject(item)) {
                    list.set(i, matchObject_express(i + "-json",item));
                } else if (isList(item)) {
                    // UNTESTED -> Array of arrays
                    list.set(i, matchArray_express(i + "-list",item));
                } else {
                    list.set(i, null);// Should never execute
                }
            }
        }

        return new MapItem<>(key,list);

    }

    public static MapItem<String, List<Object>> jsonParse(String json) {
        return matchObject_express("<ROOT>", json.replaceAll("\n", " ") );
    }




    public static void main(String[] args) {

        String test_file_folder_loc = "src/test/data/nn/";
        String[] filenames = {"hudl-hobart_vs_rpi.json", "hudl-hobart_vs_slu.json", "hudl-hobart_vs_union.json"};

        String filename = test_file_folder_loc + filenames[2];
        FileInputReader rdr = new FileInputReader(filename,null);
        String entireFile = rdr.entireInput();
        MapItem<String, List<Object>> parse = JSONBuilder.jsonParse(entireFile);
        MapItem<String, List<Object>> headersMap = (MapItem<String, List<Object>>) parse.value.get(0);
        MapItem<String, List<Object>> dataMap = (MapItem<String, List<Object>>) parse.value.get(1);
        List<String> headers = new ArrayList<>();
        for (Object o : headersMap.value) {
            headers.add(o.toString());
        }
        String input_data = dataMap.value.get(0).toString();

        console.log(headers);
        console.log(strExtract(input_data));

        String OUTPUT_INDICES_HOB_RPI = "8,10"; // Gain/Loss, Off Play

        List<Integer> outputs = CollectionUtils.intCasted(StringUtils.split(OUTPUT_INDICES_HOB_RPI,","));

        /* Cleaning Time */
        CleaningLady.CleanResult cleanResult = new CleaningLady().cleanse(input_data,headers,outputs);

        for (List<String> list : cleanResult.data)
            console.log(list);
    }



//        /* Test 1 - Simple case
//         *
//         *      { "hi": "there", "ooo": { "ahh" : "im inside the other!" }  }
//         *
//         */
//        JSONBuilder bldr = new JSONBuilder();
//        bldr.insert("hi", "\"there\"");
//        bldr.insert("hi", 5);
//        bldr.insert("ooo", "{ \"ahh\" : \"im inside the other!\" } ");
//        String json = bldr.json();
//        console.log(json);
//        MapItem<String, List<Object>> parse = JSONBuilder.jsonParse(json);
//        console.log(JSONBuilder.jsonify(parse));
//
//        /* Test 2 - Array case
//         *
//         *      { "hi": "there", "ooo": { "ahh" : [1,2,3] }  }
//         *
//         */
//        JSONBuilder bldr = new JSONBuilder();
//        bldr.insert("hi", "\"there\"");
//        bldr.insert("hi", 5);
//        bldr.insert("ooo", "{ \"ahh\" : [1,2,3] } ");
//        String json = bldr.json();
//        console.log(json);
//        MapItem<String, List<Object>> parse = JSONBuilder.jsonParse(json);
//        console.log(JSONBuilder.jsonify(parse));

}
