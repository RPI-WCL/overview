package overview.ovp.dc;

import java.util.Vector;

public class DataCollector {
	private static Vector<Thread> collectionThreads;

	public static void startCollecting (Vector<String> logs, Vector<String> hosts, Vector<String> ports) {
		if (collectionThreads == null)
			collectionThreads = new Vector<Thread> ();

		for (int i = 0; i < logs.size (); ++i)
			collectionThreads.add (new LogTranslationThread (logs.get (i)));

		for (int i = 0; i < hosts.size (); ++i)
			collectionThreads.add (new CollectionThread (hosts.get (i), ports.get (i)));

		for (Thread t : collectionThreads) t.start ();
	}
}
