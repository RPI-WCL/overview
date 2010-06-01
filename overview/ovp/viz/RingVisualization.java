package overview.ovp.viz;

import java.util.*;
import overview.util.*;
import processing.core.*;

public class RingVisualization implements Renderer {
	private RingEntity toplevel;
	private HashMap<String, RingEntity> entities;
	private List<WeightedPair<RingEntity, RingEntity>> communications;
	private PApplet r2;

	public RingVisualization (PApplet r) {
		r2 = r; // render to r

		// initialize the entity tree
		toplevel = new RingEntity (320);
		entities = new HashMap<String, RingEntity> ();
		communications = Collections.synchronizedList (new LinkedList<WeightedPair<RingEntity, RingEntity>> ());
	}

	public void draw () {
		r2.background (0);

		r2.pushMatrix ();
		r2.translate (r2.width / 2, r2.height / 2);

		// draw entities
		toplevel.move (0, 0, r2.min (r2.width / 2, r2.height / 2));
		toplevel.draw ();

		for (RingEntity e : entities.values ()) e.clear ();

		// draw communications
		synchronized (communications) {
			for (Iterator<WeightedPair<RingEntity, RingEntity>> i = communications.iterator (); i.hasNext (); ) {
				WeightedPair<RingEntity, RingEntity> p = i.next ();

				if (p.weight > 0) {
					r2.stroke (255, p.weight * 255); //stroke color and opacity
//					p.weight -= 0.05;

					if (p.first.getParent () != null && p.second.getParent () != null)
						r2.line ((int) Math.round (p.first.x),
						         (int) Math.round (p.first.y),
						         (int) Math.round (p.second.x),
						         (int) Math.round (p.second.y));
				}
			}
		}

		r2.popMatrix ();

		// draw HUD
		r2.fill (255);
		r2.textAlign (r2.LEFT);
		r2.text (toplevel.within (r2.mouseX - r2.width / 2, r2.mouseY - r2.height / 2).name, 8, 22);
	}

	public void handle (String[] event) {
		for (int i = 0 ; i<event.length ; i++){System.out.print("-"+event[i]);}
		System.out.print("\n");
		
		if (event == null || event.length == 0) {
			System.err.println ("NULL EVENT RECEIVED.");
			return;
		}

		// creation and migration are handled EXACTLY the same.
		// I think this is a failing of the event selection in OverView.
		// I would merge the two into a single event, but I'm not yet
		// sure if the will always be the same (ie. in other types of
		// visualizations).
		else if (event[0].equals ("creation") || event[0].equals ("migration")) {
			if (event.length == 3) {
				getEntity (event[1], getEntity (event[2]));
				return;
			}

			else if (event.length == 2) {
				getEntity (event[1]);
				return;
			}
		}

		else if (event[0].equals ("deletion") && event.length == 2) {
			getEntity (event[1]).hibernate ();
			return;
		}

		else if (event[0].equals ("communication") && event.length == 3) {
			if (!event[1].equals (event[2]))
				communicate (getEntity (event[1]), getEntity (event[2]));
			return;
		}

		System.err.println ("BAD EVENT: " + event[0] + " (" + Strings.join (", ", event, 1) + ")");
	}

	public void unhandle (String[] event) {
		if (event == null || event.length == 0)
			System.err.println ("NULL UNDO EVENT RECEIVED.");

		// roll back the entity's state.
		// I love how creation, migration, and deletion are all
		// handled the exact same way
		else if (event[0].equals ("creation") || event[0].equals ("migration") || event[0].equals ("deletion")) {
			getEntity (event[1]).rollback ();
		}

		else if (event[0].equals ("communication") && event.length == 3) {
			if (!event[1].equals (event[2]))
				communicate (getEntity (event[1]), getEntity (event[2]));
			return;
		}

		else
			System.err.println ("BAD UNDO EVENT: " + event[0] + " (" + Strings.join (", ", event, 1) + ")");
	}

	private void communicate (RingEntity alice, RingEntity bob) {
		synchronized (communications) {
			for (Iterator<WeightedPair<RingEntity, RingEntity>> i = communications.iterator (); i.hasNext (); ) {
				WeightedPair<RingEntity, RingEntity> p = i.next ();

				if ((p.first == alice && p.second == bob) ||
				    (p.first == bob && p.second == alice)) {
					p.weight = 1;
					return;
				}
			}

			communications.add (new WeightedPair<RingEntity, RingEntity> (alice, bob));
		}
	}

	private RingEntity getEntity (String name) {
		return getEntity (name, null);
	}

	private RingEntity getEntity (String name, RingEntity parent) {
		// if such an entity exists, return it
		if (entities.containsKey (name)) {
			RingEntity c = entities.get (name);

			// handle migratory cases
			// skip migration if parent is null.
			if (parent != null && c.getParent () != parent)
				c.setParent (parent);

			return c;
		}

		// otherwise, put a new entry in the map and return THAT
		RingEntity c = parent == null ? new RingEntity (name, toplevel, toplevel)
					      : new RingEntity (name, parent, toplevel);

		entities.put (name, c);
		return c;
	}

	class RingEntity {
		public String name;

		public int level;
		public float x, y, r, tx, ty, tr;
		private Stack<RingEntity> parent;
		private RingEntity toplevel;
		private boolean drawn, container;

		private List<RingEntity> children, orphans;

		// create the toplevel entity
		RingEntity (float radius) {
			name = "";

			level = 0;
			x = tx = 0; y = ty = 0; r = tr = radius;
			parent = null; // toplevel HAS no parent
			toplevel = this;
			container = false;
			drawn = false;

			children = Collections.synchronizedList (new LinkedList<RingEntity> ());
			orphans = Collections.synchronizedList (new LinkedList<RingEntity> ());
		}

		// create an entity inside a parent
		RingEntity (String n, RingEntity p, RingEntity t) {
			name = n;

			level = 0;
			x = tx = p.x; y = ty = p.y; r = tr = 0;
			parent = new Stack<RingEntity> ();
			toplevel = t;
			container = p == t;
			drawn = false;

			children = Collections.synchronizedList (new LinkedList<RingEntity> ());
		
			setParent (p);
		}

		public void hibernate () {
			RingEntity p = getParent ();
			if (p != null) { tx = p.tx; ty = p.ty; } tr = 0;
			setParent (null); // no parent for me

			// note that we're now orphaned
			toplevel.addOrphan (this);
		}

		public void rollback () {
			if (parent == null) return; // toplevel can't roll back, silly

			RingEntity par = null;

			try {
				par = parent.pop ();
				if (par != null) par.removeChild (this);

				RingEntity par2 = parent.peek ();

				// this is undoing a deletion
				// so, we move to the parent, like we would in a 
				// creation
				if (par == null) { x = par2.x; y = par2.y; r = 0; }

				if (par2 != null) par2.addChild (this);

				// undo creation
				else throw new Exception ();

				// if we've been orphaned, note it
				if (par2 == null) toplevel.addOrphan (this);
				else toplevel.removeOrphan (this);
			}

			// either we rolled back to the beginning (empty stack)
			// or we undid a creation message
			// draw ourselves disappear
			catch (Exception e) {
				if (par != null) { tx = par.tx; ty = par.ty; }
				tr = 0;

				// we're orphaned
				toplevel.addOrphan (this);
			}
		}

		public RingEntity getParent () {
			if (parent == null) return null; // toplevel has no parent

			try { return parent.peek (); } // return the parent if it exists
			catch (EmptyStackException e) { return null; } // or not
		}

		public void setParent (RingEntity p) {
			// if we have a parent, remove ourselves from their children
			RingEntity par = getParent ();
			if (par != null) par.removeChild (this);

			// otherwise, this is a creation message.
			// this means we should pop out from the center of the parent
			else if (p != null) { x = p.x; y = p.y; r = 0; }

			// add ourselves to our new parent
			if (p != null) { p.addChild (this); level = p.level + 1; }
			if (parent != null) parent.push (p);

			// same as above. if p is not null, ensure we're not orphaned
			// otherwise, make sure we are
			if (p != null) toplevel.removeOrphan (this);
			else toplevel.addOrphan (this);
		}

		public void addOrphan (RingEntity orphan) {
			if (orphans != null)
				orphans.add (orphan);
		}

		public void removeOrphan (RingEntity orphan) {
			if (orphans != null)
				orphans.remove (orphan);
		}

		public void addChild (RingEntity child) {
			children.add (child);
			repositionChildren ();
		}

		public void removeChild (RingEntity child) {
			children.remove (child);
			repositionChildren ();
		}

		public void move (float nx, float ny, float nr) {
			tx = nx; ty = ny; tr = nr;
			repositionChildren ();
		}

		private void repositionChildren () {
			float n = (float) children.size (),
			      s = (float) Math.sin (Math.PI / n),
			      a = (float) (tr / (1 + s)),
			      z = (float) (a * s * .75),
			      t = (float) (2 * Math.PI / n);

			if (n == 1)
				children.get (0).move (x, y, (float) (tr * .75));

			else
				for (int i = 0; i < children.size (); ++i)
					children.get (i).move((float) (x + Math.cos (t * i) * a),
							      (float) (y + Math.sin (t * i) * a),
							      z);
		}

		public void draw () {
			r2.ellipseMode (r2.CORNER);

			if (!drawn)
				// only draw myself if I'm not the toplevel entity
				if (parent != null) {
					x += (tx - x) * .25;
					y += (ty - y) * .25;
					r += (tr - r) * .25;

					int ex = Math.round (x - r),
					    ey = Math.round (y - r),
					    ed = Math.round (r * 2);

					if (ed != 0) {
						// draw shape
						r2.fill (127 - (128 >> level));
						r2.stroke (255);

						if (container) r2.rect (ex, ey, ed, ed);
						else r2.ellipse (ex, ey, ed, ed);
					}
				}

			drawn = true;
		
			// draw children
			synchronized (children) {
				for (Iterator<RingEntity> i = children.iterator (); i.hasNext (); )
					i.next ().draw ();
			}

			// draw orphans, if applicable
			if (orphans != null)
				synchronized (orphans) {
					for (Iterator<RingEntity> i = orphans.iterator (); i.hasNext (); )
						i.next ().draw ();
				}
		}

		public void clear () { drawn = false; }

		// this function tests if [cx,cy] is contained inside the entity
		// note that a container tests ortogonal distance,
		// and that any other entity tests radial distance.
		public RingEntity within (float cx, float cy) {
			float dx = x - cx,
			      dy = y - cy;

			if (container ? Math.abs (dx) < r && Math.abs (dy) < r
				      : Math.sqrt (dx * dx + dy * dy) <= r) {
				if (orphans != null)
					synchronized (orphans) {
						for (Iterator<RingEntity> i = orphans.iterator (); i.hasNext (); ) {
							RingEntity f = i.next ().within (cx, cy);

							if (f != null) return f;
						}
					}

				synchronized (children) {
					for (Iterator<RingEntity> i = children.iterator (); i.hasNext (); ) {
						RingEntity f = i.next ().within (cx, cy);

						if (f != null) return f;
					}
				}

				return this;
			}

			return parent == null ? this : null;
		}
	}
}
