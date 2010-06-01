package overview.ovi.compiler.definitions;

import overview.ovi.compiler.SimpleNode;

/*
	"watch" <IDENTIFIER> "is" Value ";"
*/

public class WatchDeclaration extends SimpleNode {
	public WatchDeclaration()		{}
	public WatchDeclaration(int id)		{ super(id); }
}
