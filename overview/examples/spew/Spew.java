package overview.examples.spew;

import java.util.Vector;

// Spew is a program that spits out lots and lots of containers with single entities in them.
// It's used to test to make sure that Containers are being drawn correctly.

class Spew {
	public static void main (String args[]) {
		int m = 0, n = 0, k = 0, p = 0, q = 0, r = 0;
		if (args.length > 0) m = Integer.parseInt (args[0]); // m = # of containers
		if (args.length > 1) n = Integer.parseInt (args[1]); // n = # of entities
		if (args.length > 2) k = Integer.parseInt (args[2]); // k = # of communications
		if (args.length > 3) p = Integer.parseInt (args[3]); // p = # of migrations
		if (args.length > 4) q = Integer.parseInt (args[4]); // q = # of splits
		if (args.length > 5) r = Integer.parseInt (args[5]); // r = # of merges
		
		for (int i = 0; i < n; ++i) test ("entity" + i, "container" + randomInt (m));
		for (int i = 0; i < k; ++i) talk ("entity" + randomInt (n), "entity" + randomInt (n));
		for (int i = 0; i < p; ++i) move ("entity" + randomInt (n), "container" + randomInt (m));

		// build the entity vector
		Vector<String> entities = new Vector<String> ();
		for (int i = 0; i < n; ++i) entities.add ("entity" + i);

		// split off q children into random containers
		for (int i = 0; i < q; ++i) {
			String par = entities.get (randomInt (entities.size ()));
			String child = par + ".s" + i;

			entities.add (child);

			splt (par, child, "container" + randomInt (m));
		}

		// merge r times
		for (int i = 0; i < r; ++i) {
			String par = entities.get (randomInt (entities.size ()));
			String child = entities.remove (randomInt (entities.size ()));

			mrge (par, child);
		}
		
		//for (int i = 0; i < entities.size (); ++i)
		//	kill (entities.get (i));
		
		//for (int i = 0; i < n; ++i) kill ("entity" + i);
	}

	private static int randomInt (int n) { return (int) Math.floor (Math.random () * n); }

	public static void test (String en, String cn) { }
	public static void talk (String alice, String bob) { }
	public static void move (String en, String cn) { }
	public static void kill (String en) { }
	public static void splt (String pa, String ch, String cn) { }
	public static void mrge (String pa, String ch) { }
}
