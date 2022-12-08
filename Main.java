interface Foo {
	int foo(int a, int b, int c, int d);
}

public class Main implements Foo {
	public static void main(String[] args) {
		var mystring = new String("Hello, World!");
		System.out.println(mystring);

		Foo m = new Main();
		m.foo(1, 2, 3, 4);
	}

	public int foo(int a, int b, int c, int d) {
		return a + b + c + d;
	}
}
