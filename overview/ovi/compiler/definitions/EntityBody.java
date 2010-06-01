package overview.ovi.compiler.definitions;

import overview.ovi.compiler.SimpleNode;

/*
	EntityBody := "{" (WatchDeclaration | WhenDeclaration)* "}"
*/

public class EntityBody extends SimpleNode {
	public EntityBody()			{}
	public EntityBody(int id)		{ super(id); }
}
