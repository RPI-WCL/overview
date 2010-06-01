package overview.ovp.viz;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.applet.*;

public class VisFrame extends Frame {
	public static final int FUDGE_WIDTH = 10, FUDGE_HEIGHT = 30;

	public VisFrame (boolean forcedirected) {
		super ("OverView");
		setSize (640 + FUDGE_WIDTH, 640 + FUDGE_HEIGHT);

		// quit if somebody closes the window
		addWindowListener (new WindowAdapter () {
			public void windowClosing (WindowEvent e) { System.exit (0); } });

		// begin embedded processing applet
		//boolean for now since there are only two visualization, data type needs to be changed upon development of new vis. modules
		//maybe use an enum type
		Applet embed = new Visualization (forcedirected);
		setLayout (new BorderLayout ());
		add (embed, BorderLayout.CENTER);
		embed.init ();

		// show frame
		setVisible (true);
	}
}
