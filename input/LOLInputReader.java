package com.ai.input;

import java.util.List;
import java.util.Scanner;
import com.ai.input.InputReader;

public class LOLInputReader extends InputReader {

    int curr = -1;
    List<List<String>> inputs;

    public LOLInputReader(List<List<String>> inputs) {
        super(null);
        this.inputs = inputs;
    }

    @Override
    public List<String> next() {
        if (!hasNext()) return null;
        curr++;
        return inputs.get(curr);
    }

    @Override
    public int dataLength() {
        return inputs.get(0).size();
    }

    @Override
    public boolean hasNext() {
        int MAX_RETURNABLE_SLOT = inputs.size()-1;
        int WHERE_WE_WILL_BE = curr+1;
        return (WHERE_WE_WILL_BE <= MAX_RETURNABLE_SLOT);
    }

    @Override
    public void reset() {
        curr = -1;
    }

}
