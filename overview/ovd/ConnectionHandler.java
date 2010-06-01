package overview.ovd;

import java.io.*;
import java.net.*;
import java.util.*;
import overview.util.*;

class ConnectionHandler extends Thread {
	boolean source;
	SerializedConnection conn;
	public static List<SerializedConnection> sinks;

	ConnectionHandler (SerializedConnection client) {
		conn = client;
		String type = "nothing";

		try { type = (String) conn.get (); }
		catch (Exception e) {
			System.err.println ("OVD: Connection reception failed.");
			System.err.println (e);
			e.printStackTrace ();
		}

		if (type.equals ("source")) {
			System.out.println ("Joined by event source from " + conn.toString () + ".");
			source = true;
		}

		else if (type.equals ("sink")) {
			synchronized (sinks) { sinks.add (conn); }

			System.out.println ("Joined by event sink from " + conn.toString () + ".");
			source = false;
		}
	}

	public void run () {
		if (source)
			while (true) {
				String[] event = null;
				try { event = (String[]) conn.get (); }
				catch (Exception e) {
					System.out.println ("Left by event source from " + conn.toString () + ".");
					break;
				}

				if (event != null)
					synchronized (sinks) {
						for (Iterator<SerializedConnection> s = sinks.iterator (); s.hasNext (); ) {
							SerializedConnection sc = s.next ();

							try { sc.put (event); }
							catch (Exception e) {
								s.remove ();
								System.out.println ("Left by event sink from " + sc.toString () + ".");
							}
						}
					}
			}
	}
}
