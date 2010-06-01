package overview.ovi.ipa;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.Vector;

import overview.util.SerializedConnection;

public class TransportThread extends Thread {
	SerializedConnection conn = null;
	
	public TransportThread () {
		if (System.getProperty ("ovHost") != null) {
			String[] info = System.getProperty ("ovHost").split (":");
			String host = info[0];
			int port = Integer.parseInt (info[1]);

			try {
				conn = new SerializedConnection (new Socket (host, port));
				conn.put ("source");

				System.err.println ("IPA: Connected to host successfully.");
			}

			catch (Exception e) {
				System.err.println ("IPA: Error connecting to OVD.");
				System.err.println (e);
				e.printStackTrace ();
			}
		}
	}

	public synchronized void putEvent (String[] e) {
		if (conn != null) {
			try { conn.put (e); }
			
			catch (Exception ex) {
				System.err.println("IPA: Error writing events to OVD.");
				System.err.println(ex);
				ex.printStackTrace();

				return;
			}
		}
	}
}
