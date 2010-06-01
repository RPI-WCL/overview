package overview.util;

import java.io.*;
import java.net.*;
import java.util.*;

// this class simplifies sending/receiving serialized Objects

public class SerializedConnection {
	private String identifier;
	private ObjectInputStream in;
	private ObjectOutputStream out;

	public SerializedConnection (Socket s) throws IOException {
		identifier = s.getInetAddress ().getHostName ();
		out = new ObjectOutputStream (s.getOutputStream ());
		in = new ObjectInputStream (s.getInputStream ());
	}

	public void put (Object o) throws IOException { out.writeObject (o); }
	public Object get () throws IOException, ClassNotFoundException { return in.readObject (); }

	public String toString () { return identifier; }
}
