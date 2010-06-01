package overview.ovp;

import java.util.Vector;
import overview.ovp.dc.DataCollector;
import overview.ovp.viz.VisFrame;
import overview.ovd.OverViewDaemon;

public class OverViewPresenter {
	public static void main (String argv[]) {
		Vector<String> logs = new Vector<String> (), hosts = new Vector<String> (), ports = new Vector<String> ();

		boolean offline = false;
		boolean forcedirected = false;

		//modify argument handling to include future visualization modules
		for (String c : argv) {
			if (c.equals ("-h") || c.equals ("--help")) {
				System.err.println ("Usage: OverViewPresenter [-h] [host:port+] [-f forcedirected] [-l logfile+]");
				System.exit (0);
			}

			else if (c.equals ("-l") || c.equals ("--log")) {
				offline = true;
			}

			else if (c.equals ("-f")) {
				forcedirected = true;
			}

			else if (offline) {
				logs.addElement (c);
			}

			else {
				try {
					String[] name = c.split (":");
					hosts.addElement (name[0]); ports.addElement (name[1]);
				} catch (Exception e) {
					System.err.println ("Invalid host:port received: " + c);
					System.err.println ("Usage: OverViewPresenter [-h] [host:port+] [-f forcedirected] [-l logfile+]");
					System.exit (1);
				}
			}
		}

		new VisFrame (forcedirected);
		DataCollector.startCollecting (logs, hosts, ports);
	}
}
