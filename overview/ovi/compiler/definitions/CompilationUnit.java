package overview.ovi.compiler.definitions;

import overview.ovi.compiler.SimpleNode;

/*
	CompilationUnit := EntityDeclaration <EOF>
*/

public class CompilationUnit extends SimpleNode {
	public CompilationUnit()		{}
	public CompilationUnit(int id)		{ super(id); }
}
