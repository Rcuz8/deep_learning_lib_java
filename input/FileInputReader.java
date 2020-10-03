
package com.ai.input;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;
import com.ai.input.InputReader;

import static com.ai.print.Log.logn;

public class FileInputReader extends InputReader {

	File file; // don't use
    Scanner sc;
    int data_length;
    String splittoken;
    boolean usesDataHeader = false;

	public FileInputReader(String filename, String splittoken) {
		super(splittoken);
        try {

            file = new File(filename);
            sc = new Scanner(file);

            this.splittoken = splittoken;

            if (splittoken != null) // just want file read, not for AI
                data_length = calcDataLength(); // shouldn't be effected by data header's presence, but may

        } catch (FileNotFoundException e) {
            logn("File not found: " + filename);
        }
    }

    public FileInputReader usesDataHeader() {
    	usesDataHeader = true;
    	data_length = calcDataLength();
    	return this;
    }


	@Override
	public List<String> next() {
		return split(sc.nextLine());
	}

	@Override
	public boolean hasNext() {
		return sc.hasNextLine();
	}

	@Override
	public void reset() {
		try {
            sc = new Scanner(file);

        } catch (FileNotFoundException e) {
            logn("File not found: " + file.getName());
        }
	}

	@Override
	public int dataLength() {
		return data_length;
	}

	public String entireInput() {
	    reset();
	    String in = "";
	    while (sc.hasNextLine())
	        in += sc.nextLine();
	    reset();
	    return in;
    }


	private int calcDataLength() {
		try {
            Scanner scr = new Scanner(file);
            // get rid of header
            if (usesDataHeader) scr.nextLine();
            return split(scr.nextLine()).size();
        } catch (FileNotFoundException e) {
            logn("File not found!! ");
            return 0;
        }
	}

}