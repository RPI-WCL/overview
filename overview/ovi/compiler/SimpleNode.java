/* Generated By:JJTree: Do not edit this line. SimpleNode.java */

package overview.ovi.compiler;

import java.lang.reflect.Constructor;

public class SimpleNode implements Node {
	public Node parent;
	public Node[] children;
	public int id;
	protected OverviewParser parser;

	public Token first_token, last_token;
	public Token tokens[];

	public SimpleNode() {
		System.err.println("default simple node constructor should not be used");
	}

	public SimpleNode(int i) {
		id = i;
//		System.err.println("Created a: " + OverviewParserTreeConstants.jjtNodeName[id]);
	}

	public SimpleNode(OverviewParser p, int i) {
		this(i);
		parser = p;
	}

	public SimpleNode getChild(int i) { return (SimpleNode)children[i]; }
	public Token getToken(int i) {
		if (tokens == null || i >= tokens.length) return null;
		return tokens[i];
	}

	public String getPreCode() { return ""; }
	public String getPostCode() { return ""; }

	public void addToken(Token t) {
		if (tokens == null) {
			tokens = new Token[1];
		} else {
			Token toks[] = new Token[tokens.length + 1];
			System.arraycopy(tokens, 0, toks, 0, tokens.length);
			tokens = toks;
		}
		tokens[tokens.length-1] = t;
	}

	public String getChildCode() {
		String code = "";

		if (children != null) {
			for (int i = 0; i < children.length; i++) {
//				code += children[i].getJavaCode();
			}
		} else if (tokens != null) {
			return getTokenCode();
		}

		return code;
	}

	public String getTokenCode() {
		String code = "";

		for (int i = 0; i < tokens.length; i++) {
			code += tokens[i].image;
		}

		return code;
	}


	public String getJavaCode() {
		return getPreCode() + getChildCode() + getPostCode();
	}


	public static Node jjtCreate(int id) {
		try {
			Class cls = Class.forName( "overview.ovi.compiler.definitions." + OverviewParserTreeConstants.jjtNodeName[id] );

			Class parTypes[] = { int.class };

			Constructor nodeConstructor = cls.getConstructor(parTypes);

			Object[] argList = { new Integer(id) };

			return (Node)nodeConstructor.newInstance(argList);
		} catch (Exception e) {
			System.err.println("Error creating a node: " + e);
		}
		return null;
	}

	public static Node jjtCreate(OverviewParser p, int id) {
		try {
			Node retVal = (Node)jjtCreate(id);
			((SimpleNode)retVal).parser = p;

			return retVal;
		} catch (Exception e) {
			System.err.println("Error creating a node: " + e);
		}
		return null;
	}

	public void jjtOpen() {
	}

	public void jjtClose() {
	}

	public void jjtSetParent(Node n) { parent = n; }
	public Node jjtGetParent() { return parent; }

	public void jjtAddChild(Node n, int i) {
		if (children == null) {
			children = new Node[i + 1];
		} else if (i >= children.length) {
			Node c[] = new Node[i + 1];
			System.arraycopy(children, 0, c, 0, children.length);
			children = c;
		}
		children[i] = n;
	}

	public Node jjtGetChild(int i) {
		return children[i];
	}

	public int jjtGetNumChildren() {
		return (children == null) ? 0 : children.length;
	}

	/* You can override these two methods in subclasses of SimpleNode to
	     customize the way the node appears when the tree is dumped.  If
	     your output uses more than one line you should override
	     toString(String), otherwise overriding toString() is probably all
	     you need to do. */

	public String toString() { return OverviewParserTreeConstants.jjtNodeName[id]; }
	public String toString(String prefix) { return prefix + toString(); }

	/* Override this method if you want to customize how the node dumps
	     out its children. */

	public void dump(String prefix) {
		System.out.println(toString(prefix));
		if (children != null) {
			for (int i = 0; i < children.length; ++i) {
				SimpleNode n = (SimpleNode)children[i];
				if (n != null) {
					n.dump(prefix + " ");
				}
			}
		}
	}
}

