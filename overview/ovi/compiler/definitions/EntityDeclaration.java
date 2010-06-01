package overview.ovi.compiler.definitions;

import overview.ovi.compiler.SimpleNode;

/*
	EntityDeclaration := "entity" <IDENTIFIER> "is" Name EntityBody
*/

public class EntityDeclaration extends SimpleNode {
	public EntityDeclaration()		{}
	public EntityDeclaration(int id)	{ super(id); }
}
