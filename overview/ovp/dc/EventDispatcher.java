package overview.ovp.dc;

import java.io.*;
import java.util.*;
import java.security.AccessControlException;
import overview.ovp.viz.*;
import overview.util.*;

public class EventDispatcher {
	static int pointer, eventcount;
	static long epoch;
	static String logfile;
	static PrintStream p;
	static Visualization ov;
	static Vector<Pair<Long, String[]>> history;

	synchronized public static void begin (Visualization v) {
		logfile = "events.log";
		try { p = new PrintStream (new FileOutputStream (logfile)); }
		catch (FileNotFoundException e) {
			System.err.println ("File does not exist.");
		}
		catch (AccessControlException e) {
			System.err.println ("Do not have permission to open file.");
		}

		ov = v;
		history = new Vector<Pair<Long, String[]>> ();

		pointer = 0;
		eventcount = 0;
		epoch = new Date ().getTime ();
	}

	synchronized public static int getPointer () { return pointer; }
	synchronized public static int getEventCount () { return eventcount; }

	synchronized public static void addEvents (Vector<String[]> events) {
		for (String[] event : events) addEvent (event);
	}

	synchronized public static void addEvent (String[] event) {
		if (ov == null || history == null) return;

		// if we're at the end of the history, draw the new event as it happens
		if (pointer == eventcount) {
			++pointer; ++eventcount;

			Vector<String[]> events = new Vector<String[]> ();
			events.add (event);

			ov.handle (events);
		}

		// if we're not, just increase the count
		else ++eventcount;

		// add the event to the history
		long newtime = new Date ().getTime (),
		     duration = newtime - epoch;
		epoch = newtime;
		history.add (new Pair<Long, String[]> (new Long (duration), event));

		// add the event to the log
		if (p != null)
			p.println (duration +
				   Strings.SUBSEP +
				   Strings.join (Strings.SUBSEP, event));
	}

	// simple hack to simply step forward/back by a small amount
	synchronized public static void step (int delta) { goTo (pointer + delta); }

	// jump to a particular pointer
	synchronized public static void goTo (int where) {
		// make sure it's bounded correctly
		if (where < 0) where = 0;
		else if (where > eventcount) where = eventcount;

		//System.err.println ("goTo: " + pointer + " -> " + where + " (" + eventcount + ")");

		if (where == pointer) return; // no point going where we already are

		Vector<String[]> events = new Vector<String[]> ();
		boolean undo = pointer > where;

		while (pointer < where) events.add (history.get (pointer++).second);
		while (pointer > where) events.add (history.get (--pointer).second);

		if (undo) ov.unhandle (events); else ov.handle (events);
	}
}
