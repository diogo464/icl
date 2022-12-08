interface Foo {
	int foo(int a, int b, int c, int d);
}

public class Main implements Foo {
	public static void main(String[] args) {
		float x = 10.25f;
		float y = 12.2512f;
		var z = x == y;
	}

	public int foo(int a, int b, int c, int d) {
		return a + b + c + d;
	}
}
