package overview.ovp.viz;

interface Renderer {
	public void draw ();
	public void handle (String[] event);
	public void unhandle (String[] event);
}
