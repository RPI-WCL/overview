package overview.util;

public class Strings {
	public static final String SUBSEP = "\034";

	public static String join (String glue, String[] array) {
		return join (glue, array, 0);
	}

	public static String join (String glue, String[] array, int start) {
		if (array.length == start) return "";

		String result = array[start];

		for (int i = start + 1; i < array.length; ++i)
			result += glue + array[i];

		return result;
	}
}
