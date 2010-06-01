package overview.ovp.dc;
	
import java.io.*;
import java.net.*;
import java.util.Vector;
import overview.util.SerializedConnection;

// stupid kludge.
// Java can't figure out what is received over a socket, so there's no way
// to resolve this particular warning. So, we suppress it.
@SuppressWarnings ("unchecked")

public class CollectionThread extends Thread {
	String host;
	int port;
		
	public CollectionThread (String host, String port) {
		this.host = host;
		this.port = new Integer(port).intValue();
	}

	public void run () {
		SerializedConnection conn;

		try {
			// connect, and announce we are an event sink
			conn = new SerializedConnection (new Socket (host, port));
			conn.put ("sink");
		} catch (Exception e) {
			System.err.println("Error connecting to remote JVM: " + host + ":" + port);
			System.err.println(e);
			e.printStackTrace();
			return;
		}

		while (true) {
			try {
				String[] event = (String[]) conn.get ();
				EventDispatcher.addEvent(event);
			} catch (EOFException e) {
				return;
			} catch (Exception e) {
				System.err.println("Error receiving events from remote JVM: " + host + ":" + port);
				System.err.println(e);
				e.printStackTrace();
				return;
			}
		}
	}
}

