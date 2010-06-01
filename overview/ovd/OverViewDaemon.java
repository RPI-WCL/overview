package overview.ovd;

import java.io.*;
import java.net.*;
import java.util.*;
import overview.util.*;

public class OverViewDaemon {
	public static final int DEFAULT_PORT = 6060;

	public static void main (String[] args) {
		ServerSocket socket;

		try {
			socket = new ServerSocket (DEFAULT_PORT);
			System.out.println ("OVD listening on port " + DEFAULT_PORT + ".");

			// initialize ConnectionHandler's list of event sinks
			ConnectionHandler.sinks = Collections.synchronizedList (new LinkedList<SerializedConnection> ());

			/*
			for (int i = 0; i < args.length; ++i) {
				String[] hoststr = args[i].split (":");
				String host; int port;
				if (hoststr.length != 2) {
					System.err.println ("Invalid Host: " + args[i] + "; ignoring.");
					continue;
				}
				
				host = hoststr[0];
				try { port = Integer.parseInt (hoststr[1]); }
				catch (Exception e) {
					System.err.println ("Invalid Host: " + args[i] + "; ignoring.");
					continue;
				}

				SerializedConnection conn;
				try {
					conn = new SerializedConnection (new Socket (host, port));
					conn.put ("sink"); // we are a sink
				}
			}
			*/

			while (true)
				try { new ConnectionHandler (new SerializedConnection (socket.accept ())).start (); }
				catch (Exception e) { System.err.println ("Invalid connection received."); }
		}

		catch (IOException e) {
			System.err.println ("OVD: Error: Couldn't listen on port " + DEFAULT_PORT + ".");
		}
	}
}
