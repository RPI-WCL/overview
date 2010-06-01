package overview.ovi.ipa;

public class InstrumentedProfilingAgent {
	private static TransportThread transportThread = null;

	private static long events = 0;
	private static long start_time = 0;

	static {
		transportThread = new TransportThread ();
		transportThread.start();

		start_time = System.currentTimeMillis ();
	}

	public static void initialize () {}

	public static void clear () { events = 0; }

	public static double getEventsPerSecond () {
		return (double) events / (double)(System.currentTimeMillis () - start_time);
	}

	public static long getEvents () { return events; }

	public static void putEvent (String eventType, String arg0) {
		++events;

		if (System.getProperty("ovVerbose") != null)
			System.err.println(eventType + ": (" + arg0 + ")");

		String[] event = { eventType, arg0 };
		transportThread.putEvent (event);
	}

	public static void putEvent (String eventType, String arg0, String arg1) {
		++events;

		if (System.getProperty("ovVerbose") != null)
			System.err.println(eventType + ": (" + arg0 + ", " + arg1 + ")");

		String[] event = { eventType, arg0, arg1 };
		transportThread.putEvent (event);
	}
	
	public static void putEvent (String eventType, String arg0, String arg1, String arg2) {
		++events;

		if (System.getProperty("ovVerbose") != null)
			System.err.println(eventType + ": (" + arg0 + ", " + arg1 + ", " + arg2 + ")");

		String[] event = { eventType, arg0, arg1, arg2 };
		transportThread.putEvent (event);
	}
}
