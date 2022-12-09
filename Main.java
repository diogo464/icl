interface Foo {
	int foo(int a, int b, int c, int d);
}

public class Main implements Foo {
	public static void main(String[] args) {
	}

	public double bar(double x, double y, double z) {
		return x + y + z;
	}

	public int foo(int a, int b, int c, int d) {
		return a + b + c + d;
	}
}
