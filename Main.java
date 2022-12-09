interface Foo {
	int foo(int a, int b, int c, int d);
}

public class Main implements Foo {
	public static void main(String[] args) {
		// This is to show symbol . instead of ,
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
		// Define the maximum number of decimals (number of symbols #)
		DecimalFormat df = new DecimalFormat("#.##########", otherSymbols);
		df.format(5.0);
	}

	public double bar(double x, double y, double z) {
		return x + y + z;
	}

	public int foo(int a, int b, int c, int d) {
		return a + b + c + d;
	}
}
