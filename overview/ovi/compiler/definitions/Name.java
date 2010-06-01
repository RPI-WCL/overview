package overview.ovi.compiler.definitions;

import overview.ovi.compiler.SimpleNode;

/*
	Name := <IDENTIFIER> ("." <IDENTIFIER>)*
*/

public class Name extends SimpleNode {
	public Name()			{}
	public Name(int id)		{ super(id); }

	public String getName() {
		String name = "";

		for (int i = 0; i < tokens.length; i++) {
			name += getToken(i).image;
		}
		return name;
	}
}
