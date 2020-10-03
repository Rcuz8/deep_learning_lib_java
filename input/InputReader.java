package com.ai.input;
import com.ai.utils.StringUtils;
import java.util.List;

public abstract class InputReader {

	String splittoken;

	public InputReader(String splittoken) {
		this.splittoken = splittoken;
	}

	List<String> split(String input) {
		// NEW: Split & trim
		return StringUtils.splittrim(input,splittoken);
	}
	/* Next input, split by token */
	public abstract List<String> next();
	public abstract boolean hasNext();
	public abstract int dataLength();
	public abstract void reset();
}