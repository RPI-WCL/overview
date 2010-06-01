package overview.ovp.viz;

import java.util.*;
import overview.ovp.dc.DataCollector;
import overview.ovp.dc.EventDispatcher;
import processing.core.*;

// the history slider should be exported to an auxiliary class at some point

public class Visualization extends PApplet {
	private int index, size;
	private boolean slider;
	private Renderer renderer;
	private boolean forceDirected;
	
	public Visualization(boolean forcedirected){
		this.forceDirected=forcedirected;
	}

	public void setup () {
		size (640, 640, P3D);
		textFont (loadFont ("freesans16.vlw.gz"));

		// variables for controlling the history slider
		index = 0;
		size = 0;
		slider = false;
		//modify for new visualization modules default is hierarchic concentric
		if(forceDirected)
			renderer = new ForceVisualization(this);
		else
			renderer = new RingVisualization (this);
		EventDispatcher.begin (this);
	}

	public void draw () {
		renderer.draw ();

		stroke (255);
		noFill ();
		rect (16, height - 48, width - 32, 32);

		float[] sc = slidercoords ();

		if (slider) {
			EventDispatcher.goTo (round (size * (mouseX - 18 - sc[2] / 2) / (width - 64)));
			sc = slidercoords ();
		}

		rect (sc[0], sc[1], sc[2], sc[2]);
	}

	private float[] slidercoords () {
		index = EventDispatcher.getPointer ();
		size = EventDispatcher.getEventCount ();

		float percentage = size == 0 ? 1 : (float) index / (float) size,
		      slider_left = 18 + (width - 64) * percentage,
		      slider_top = height - 46,
		      slider_side = 28;

		return new float[] { slider_left, slider_top, slider_side };
	}

	public void mousePressed () {
		float[] sc = slidercoords ();
		if (mouseX > sc[0] && mouseX < sc[0] + sc[2] && mouseY > sc[1] && mouseY < sc[1] + sc[2])
			slider = true;

		else slider = false;
	}

	public void mouseReleased () { slider = false; }

	public void handle (Vector<String[]> events) {
		for (String[] event : events)
			renderer.handle (event);
	}

	public void unhandle (Vector<String[]> events) {
		for (String[] event : events)
			renderer.unhandle (event);
	}

	public void addSource (String sources) {
		println ("addSource: " + sources);

		Vector<String> hosts = new Vector<String> (),
		               ports = new Vector<String> ();

		String[] srclist = sources.split (",");

		for (int i = 0; i < srclist.length; ++i) {
			String[] splt = srclist[i].split (":");
			if (splt.length != 2) continue;

			hosts.add (splt[0]);
			ports.add (splt[1]);
		}

		DataCollector.startCollecting (new Vector<String> (), hosts, ports);
	}
}

