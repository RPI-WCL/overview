package overview.ovp.viz;

import java.util.*;
import java.util.concurrent.*;
import overview.util.*;
import processing.core.*;

import processing.opengl.*;

public class ForceVisualization implements Renderer {

	private PApplet r2;
	private ForceEntity toplevel;
	private Map<String, ForceEntity> entities;
	private Set communications;

	private HashMap<ForceEntity, Integer> entityColor;

	float cx = 0, cy = 0;
	float dx = 0, dy = 0;
	float d = 0;
	float mx = 0, my = 0;
	float dim;
	float camx, camy, cams;

	private ForceEntity preselected = null;
	int c = 0;
	public long startTime;
	private boolean firstEvent = true;

	public ForceVisualization(PApplet r) {

		r2 = r; // render to r
		dim = Math.min(r2.width, r2.height) / 2;
		camx = 0;
		camy = 0;
		cams = dim;
		r2.textFont(r2.loadFont("trebuchetms.vlw"), 12);
		// initialize the entity tree
		toplevel = new ForceEntity("toplevel", r2.height / 2, r2.width / 2);
		entities = new ConcurrentHashMap<String, ForceEntity>();
		communications = Collections.synchronizedSet(new HashSet<Communication>());

		entityColor = new HashMap<ForceEntity, Integer>();
	}

	public void draw() {

		r2.background(0);
		r2.pushMatrix();

		// translate so that the centroid is in the center of the visualization
		r2.translate((r2.width / 2) - cx, (r2.height / 2) - cy);
		// forces
		if (r2.mousePressed && preselected != null) {
			preselected.vx += (mx - preselected.x) / 2;
			preselected.vy += (my - preselected.y) / 2;
		}
		// repel all other entities
		for (Iterator i = entities.values().iterator(); i.hasNext();) {
			ForceEntity e = (ForceEntity) i.next();
			for (Iterator j = entities.values().iterator(); j.hasNext();)
				e.repel((ForceEntity) j.next());

		}

		synchronized (communications) {
			for (Iterator i = communications.iterator(); i.hasNext();)
				((Communication) i.next()).update();
		}

		for (Iterator i = entities.values().iterator(); i.hasNext();)
			((ForceEntity) i.next()).resolve();

		// draw communications
		synchronized (communications) {
			for (Iterator k = communications.iterator(); k.hasNext();) {
				Communication comm = (Communication) k.next();
				r2.strokeWeight(dim / cams);
				comm.draw();
				r2.line(comm.source.x, comm.source.y, comm.target.x, comm.target.y);
			}
		}

		// draw entities
		for (Iterator l = entities.values().iterator(); l.hasNext();)
			((ForceEntity) l.next()).draw();

		// find camera position using the centroid
		int c = 0;
		cx = 0;
		cy = 0;
		for (ForceEntity fe : entities.values()) {
			++c;
			cx += fe.x;
			cy += fe.y;
		}

		if (c != 0) {
			cx /= c;
			cy /= c;
		}

		// find distance to furthest point from centroid
		d = 0;
		for (Iterator i = entities.values().iterator(); i.hasNext();) {
			ForceEntity e = (ForceEntity) i.next();

			d = Math.max(d, r2.dist(cx, cy, e.x, e.y) + 32);
		}

		// scale camera using furthest point in each axis
		dx = 0;
		dy = 0;
		for (ForceEntity fe : entities.values()) {
			dx = Math.max(dx, Math.abs(cx - fe.x));
			dy = Math.max(dy, Math.abs(cy - fe.y));
		}

		mx = (r2.mouseX - r2.width / 2) * cams / dim + camx;
		my = (r2.mouseY - r2.height / 2) * cams / dim + camy;

		r2.popMatrix();
		// draw conventions
		int c1 = 1;
		for (ForceEntity fe : entityColor.keySet()) {
			r2.fill(entityColor.get(fe));
			r2.stroke(entityColor.get(fe));
			r2.ellipse(r2.width - 150, (r2.height - 100) - (20 * c1), 10, 10);
			r2.text(fe.getName(), r2.width - 130, (r2.height - 100) - (20 * c1));
			c1++;
		}
		r2.fill(255);
		r2.textAlign(r2.LEFT);
	}

	void position(String name) {
		if (!entities.containsKey(name))
			entities.put(name, new ForceEntity(name, r2.random(camx - cams, camx + cams), r2.random(camy - cams, camy
					+ cams)));
	}

	void deletion(String name) {
		entities.remove(name);
		synchronized (communications) {
			for (Iterator i = communications.iterator(); i.hasNext();) {
				Communication c = (Communication) i.next();

				if (c.source.name.equals(name) || c.target.name.equals(name))
					i.remove();
			}
		}
	}

	void mousePressed() {
		System.out.println("MOUSE PRESSED");
		System.out.println(r2.mouseX + "," + r2.mouseY);
		ForceEntity selected = null;
		for (Iterator i = entities.values().iterator(); i.hasNext();) {
			ForceEntity e = (ForceEntity) i.next();
			if (e.inside(mx, my))
				selected = e;
		}

		if (r2.mouseButton == r2.LEFT) {
			if (selected == null) {
				if (preselected == null)
					position("ForceEntity " + c++);

				preselected = null;
			} else {
				if (preselected == null)
					preselected = selected;

				else {
					communicate(preselected, selected);
					preselected = null;
				}
			}
		}

		else if (r2.mouseButton == r2.RIGHT) {
			if (selected != null)
				deletion(selected.name);

			preselected = null;
		}
	} // closes mouse pressed

	private void communicate(ForceEntity alice, ForceEntity bob) {
		synchronized (communications) {
			try {
				communications.add(new Communication(alice, bob));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private ForceEntity getEntity(String name) {
		return getEntity(name, null);
	}

	private ForceEntity getEntity(String name, ForceEntity parent) {

		// if such an entity exists, return it
		if (entities.containsKey(name)) {
			ForceEntity c = entities.get(name);

			// handle migratory cases
			// skip migration if parent is null.
			if (parent != null && c.getParent() != parent)
				c.setParent(parent);

			return c;

		}

		// otherwise, put a new entry in the map and return THAT
		ForceEntity c = parent == null ? new ForceEntity(name, toplevel, toplevel) : new ForceEntity(name, parent,
				toplevel);

		entities.put(name, c);
		return c;
	}

	public void handle(String[] event) {
		if (firstEvent && entities.size() > 8) {
			startTime = System.currentTimeMillis();
			firstEvent = false;
		}
		for (int i = 0; i < event.length; i++)
			System.out.print("-" + event[i]);
		System.out.print("\n");

		for (int i = 0; i < event.length; i++)
			if (event[i].trim().length() == 0) {
				System.err.println("ZERO LENGTH EVENT OR EVENT ARGUMENT RECEIVED");
				return;
			}

		if (event == null || event.length == 0) {
			System.err.println("NULL EVENT RECEIVED.");
			return;
		}
		// creation and migration are handled EXACTLY the same.
		// I think this is a failing of the event selection in OverView.
		// I would merge the two into a single event, but I'm not yet
		// sure if the will always be the same (ie. in other types of
		// visualizations).
		else if (event[0].equals("creation") || event[0].equals("migration")) {
			if (event.length == 3) {
				// System.out.println("MIGRATION!!!");
				getEntity(event[1], getEntity(event[2]));
				return;
			}

			else if (event.length == 2) {
				getEntity(event[1]);
				return;
			}
		}

		else if (event[0].equals("deletion") && event.length == 2) {
			// getEntity (event[1]).hibernate ();
			// System.out.println("ENTITY DELETED.");
			return;
		}

		else if (event[0].equals("communication") && event.length == 3) {
			if (!event[1].equals(event[2]))
				communicate(getEntity(event[1]), getEntity(event[2]));
			return;
		}

		System.err.println("BAD EVENT: " + event[0] + " (" + Strings.join(", ", event, 1) + ")");
	}

	public void unhandle(String[] event) {
		if (event == null || event.length == 0)
			System.err.println("NULL UNDO EVENT RECEIVED.");

		// roll back the entity's state.
		// I love how creation, migration, and deletion are all
		// handled the exact same way
		else if (event[0].equals("creation") || event[0].equals("migration") || event[0].equals("deletion")) {
			getEntity(event[1]).rollback();
		}

		else if (event[0].equals("communication") && event.length == 3) {
			if (!event[1].equals(event[2]))
				communicate(getEntity(event[1]), getEntity(event[2]));
			return;
		}

		else
			System.err.println("BAD UNDO EVENT: " + event[0] + " (" + Strings.join(", ", event, 1) + ")");

	}

	public class ForceEntity {

		static final float k = 4096;
		public int level;
		public float x, y, tx, ty, tr, vx, vy;

		private ForceEntity toplevel;
		private Stack<ForceEntity> parent;
		private List<ForceEntity> children, orphans;
		private Integer containerColor;

		public String name;

		boolean container, drawn;

		// create toplevel entity
		public ForceEntity(String name, float x, float y) {
			this.x = x;
			this.y = y;
			this.name = name;

			toplevel = this;
			container = false;
			parent = null; // toplevel HAS no parent
			System.out.println("ENTITY CREATED name:" + name + " at coordinates: " + this.x + "," + this.y);

			double vx = 0;
			double vy = 0;

			children = Collections.synchronizedList(new LinkedList<ForceEntity>());
			orphans = Collections.synchronizedList(new LinkedList<ForceEntity>());
		}

		// create an entity inside a parent
		public ForceEntity(String n, ForceEntity p, ForceEntity t) {

			this.name = n;
			this.x = r2.random(0, r2.width);
			this.y = r2.random(0, r2.height);
			// TODO: assign values to tx and ty
			parent = new Stack<ForceEntity>();
			toplevel = t;
			container = p == t;
			drawn = false;

			children = Collections.synchronizedList(new LinkedList<ForceEntity>());
			setParent(p);

			System.out.println("ENTITY CREATED name:" + p.name + "," + name + " at coordinates: " + this.x + ","
					+ this.y);
		}

		void repel(ForceEntity e) {
			if (e == this /* || this.container || e.container */)
				return;

			double dx = x - e.x;
			double dy = y - e.y;
			double d2 = Math.pow(dx, 2) + Math.pow(dy, 2);
			double d = Math.sqrt(d2);
			double f = k / d2; // coulomb's law - masses are not considered

			vx += dx * f / d;
			vy += dy * f / d;

		}

		void resolve() {
			x += vx;
			vx = 0;
			y += vy;
			vy = 0;
		}

		void draw() {
			if (parent != null && !parent.isEmpty()) {
				int color = 0;
				// get color assigned to parent
				if (entityColor.containsKey(parent.peek()))
					color = (Integer) entityColor.get(parent.peek());
				else {
					// generate color
					System.out.println("GENERATING RANDOM COLOR...");
					Random randomGenerator = new Random();
					color = r2.color(randomGenerator.nextInt(256), randomGenerator.nextInt(256), randomGenerator
							.nextInt(256));
					entityColor.put(parent.peek(), color);
					System.out.println(color);
				}
				r2.fill(color);
				r2.stroke(color);
				r2.ellipse(this.x, this.y, 20, 20);
			} else {
				r2.fill(0);
				r2.stroke(255);
				r2.ellipse(this.x, this.y, 20, 20);
				// r2.text("orphan"+"."+name, x, y + 4);
			}
			// r2.fill(255);
			// r2.text(name, x+15, y);
		}

		boolean inside(float x, float y) {
			return r2.dist(this.x, this.y, x, y) < 16;
		}

		// public void hibernate() {
		// ForceEntity p = getParent();
		// if (p != null) {
		// tx = p.tx;
		// ty = p.ty;
		// }
		// tr = 0;
		// setParent(null); // no parent for me
		//
		// // note that we're now orphaned
		// toplevel.addOrphan(this);
		// }

		public ForceEntity getParent() {
			if (parent == null)
				return null; // toplevel has no parent

			try {
				return parent.peek();
			} // return the parent if it exists
			catch (EmptyStackException e) {
				return null;
			} // or not
		}

		public void setParent(ForceEntity p) {
			// if we have a parent, remove ourselves from their children
			ForceEntity par = getParent();
			if (par != null)
				par.removeChild(this);

			// otherwise, this is a creation message.
			// add ourselves to our new parent
			if (p != null) {
				p.addChild(this);
				level = p.level + 1;
			}
			if (parent != null)
				parent.push(p);

			// same as above. if p is not null, ensure we're not orphaned
			// otherwise, make sure we are
			if (p != null)
				toplevel.removeOrphan(this);
			else
				toplevel.addOrphan(this);
		}

		public void addOrphan(ForceEntity orphan) {
			if (orphans != null)
				orphans.add(orphan);
		}

		public void removeOrphan(ForceEntity orphan) {
			if (orphans != null)
				orphans.remove(orphan);
		}

		public void addChild(ForceEntity child) {
			children.add(child);
			// repositionChildren ();
		}

		public void removeChild(ForceEntity child) {
			children.remove(child);
			// repositionChildren ();
		}

		public void rollback() {
			if (parent == null)
				return; // toplevel can't roll back, silly

			ForceEntity par = null;

			try {
				par = parent.pop();
				if (par != null)
					par.removeChild(this);

				ForceEntity par2 = parent.peek();

				// this is undoing a deletion
				// so, we move to the parent, like we would in a
				// creation
				if (par == null) {
					x = par2.x;
					y = par2.y;

				}

				if (par2 != null)
					par2.addChild(this);

				// undo creation
				else
					throw new Exception();

				// if we've been orphaned, note it
				if (par2 == null)
					toplevel.addOrphan(this);
				else
					toplevel.removeOrphan(this);
			}

			// either we rolled back to the beginning (empty stack)
			// or we undid a creation message
			// draw ourselves disappear
			catch (Exception e) {
				if (par != null) {
					tx = par.tx;
					ty = par.ty;
				}
				tr = 0;

				// we're orphaned
				toplevel.addOrphan(this);
			}
		}

		public String getName() {
			return this.name;
		}

		public float getX() {
			return this.x;
		}

		public float getY() {
			return this.y;
		}

	} // closes ForceEntity

	// TODO: message instead of communication
	public class Communication {
		static final double k = .0625, l = 64;

		ForceEntity source, target;

		public Communication(ForceEntity a, ForceEntity b) throws Exception {
			if (a == null || b == null)
				throw new Exception("communications can't have null pointers!");
			this.source = a;
			this.target = b;

		}

		void update() {
			double dx = this.target.x - this.source.x == 0 ? 1 : this.target.x - this.source.x;
			double dy = this.target.y - this.source.y == 0 ? 0 : this.target.y - this.source.y;

			double d = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
			double f = k * (d - l);

			source.vx += dx * f / d;
			target.vx -= dx * f / d;
			source.vy += dy * f / d;
			target.vy -= dy * f / d;
		}

		void draw() {
			r2.stroke(255);
			r2.strokeWeight(dim / cams);
			r2.line(source.x, source.y, target.x, target.y);
		}

		public ForceEntity getSource() {
			return this.source;
		}

		public ForceEntity getTarget() {
			return this.target;
		}

		// the two below line are used to make sure that A => B is the same as B
		// => A, for efficient checking.
		// however, this demands that a HashSet is used, otherwise hashCode sits
		// doing nothing.
		public boolean equals(Object rhs) {
			return rhs instanceof Communication && hashCode() == rhs.hashCode();
		}

		public int hashCode() {
			return source.hashCode() ^ target.hashCode();
		}
	}

} // closes ForceVisualization

