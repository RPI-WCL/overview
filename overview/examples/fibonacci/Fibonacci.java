package overview.examples.fibonacci;

public class Fibonacci {
	private int nodes;

	Fibonacci () { nodes = 0; }

	public static void main (String[] args) {
		int i = 0;
		Fibonacci f = new Fibonacci ();

		if (args.length > 0) i = Integer.parseInt (args[0]);

		System.out.println (f.fib (i, f.getName (i), null));
		System.exit (0);
	}

	private String getName (int i) { return "CM" + (nodes++) + " (" + i + ")"; }

	public int fib (int i, String name, String parent) {
		make (name, "localhost");
		if (parent != null) talk (name, parent);

		if (i < 2) {
			error (name, "I am a leaf node.");
			return 1;
		}

		else
			return fib (i - 1, getName (i - 1), name) + fib (i - 2, getName (i - 2), name);
	}
	
	private void make (String name, String hostAndPort) {}
	private void talk (String name, String parent) {}
	private void kill (String name) {}
	private void error (String where, String why) {}
}
