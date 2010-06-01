package overview.ovp.dc;

import java.io.*;
import java.util.Vector;
import overview.util.*;

public class LogTranslationThread extends Thread {
	Vector<Pair<Long, String[]>> events;
	
	public LogTranslationThread (String logfile) {
		events = new Vector<Pair<Long, String[]>> ();

		try {
			BufferedReader in = new BufferedReader (new FileReader (logfile));

			String line;
			while ((line = in.readLine ()) != null) {
				String[] metaline = line.split (Strings.SUBSEP, 2);
				events.add (new Pair<Long, String[]> (new Long (metaline[0]), metaline[1].split (Strings.SUBSEP)));
			}

			in.close ();
		}

		catch (Exception e) {
			System.err.println ("Could not read from " + logfile + ".");
			System.err.println (e);
		}
	}

	public void run () {
		for (Pair<Long, String[]> p : events) {
			try { this.sleep (p.first.longValue ()); }
			catch (InterruptedException e) { }
			EventDispatcher.addEvent (p.second);
		}
	}
}
