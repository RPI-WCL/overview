package overview.ovi.compiler.definitions;

import overview.ovi.compiler.SimpleNode;

/*
	WhenDeclaration := "when" ("start" | "finish") MethodDeclaration "->" EventDeclaration ";"
*/

public class WhenDeclaration extends SimpleNode {
	public WhenDeclaration()		{}
	public WhenDeclaration(int id)		{ super(id); }
}
