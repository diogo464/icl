package icl.utils;

public class CalcUtils {
	@SuppressWarnings("unchecked")
	public static <T> boolean oneOf(T v, T... opts) {
		for (var opt : opts)
			if (v.equals(opt))
				return true;
		return false;
	}
}
