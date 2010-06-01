package overview.ovi.events;

import overview.ovi.compiler.definitions.Value;
import overview.ovi.compiler.definitions.ValuePart;
import overview.ovi.compiler.definitions.Literal;
import overview.ovi.compiler.definitions.CharacterLiteral;
import overview.ovi.compiler.definitions.FloatingPointLiteral;
import overview.ovi.compiler.definitions.IntegerLiteral;
import overview.ovi.compiler.definitions.StringLiteral;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Utility;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.ALOAD;

public class EventArgument {

	public EventArgumentPart[] eventArgumentParts;

	public String getType() {
		return eventArgumentParts[ eventArgumentParts.length-1 ].getType();
	}

	public void insertArgument(InstructionList patch, MethodGen methodGen, ConstantPoolGen constantPoolGen) {
		if (eventArgumentParts[0] instanceof EventField) {
			String fieldName = ((EventField)eventArgumentParts[0]).fieldName;
			String fieldSignature = ((EventField)eventArgumentParts[0]).fieldSignature;
			LocalVariableGen[] localVariableTable = methodGen.getLocalVariables();
			int i = 0;
			for (; i < localVariableTable.length; i++) {
				if (localVariableTable[i].getName().equals(fieldName) && localVariableTable[i].getType().getSignature().equals(fieldSignature)) break;

				if (i == localVariableTable.length) {
					System.err.println("Overview Instrumenter Error:");
					System.err.println("\tCould not find specified local variable: " + fieldName);
					System.err.println("\tEvent Method: " + methodGen.toString());
					System.err.println("\tArgument Part: " + eventArgumentParts[0].toString());
					return;
				}
			}

			patch.append(new ALOAD(localVariableTable[i].getIndex()));

			for (i = 1; i < eventArgumentParts.length; i++) {
				eventArgumentParts[i].insertPart(patch, methodGen, constantPoolGen);
			}
		} else if (eventArgumentParts[0] instanceof EventLiteral) {
			eventArgumentParts[0].insertPart(patch, methodGen, constantPoolGen);
		} else {
			System.err.println("Overview Instrumenter Error:");
			System.err.println("\tMethod argument does not specify an object to operate off of (either \"this\" or method argument)");
			System.err.println("\tEvent Method: " + methodGen.toString());
			System.err.println("\tArgument Part: " + eventArgumentParts[0].toString());
			return;
		}

	}

	public String resolveClasses(String entityClassName, String eventMethod, String[] eventMethodNames, String[] eventMethodTypes) {
		if (eventArgumentParts[0] instanceof EventLiteral) {
			return eventArgumentParts[0].getType();
		}

		if ( !(eventArgumentParts[0] instanceof EventField) ) {
			System.err.println("Overview Instrumenter Error:");
			System.err.println("\tMethod argument does not specify an object to operate off of (either \"this\" or method argument)");
			System.err.println("\tEvent Method: " + eventMethod);
			System.err.println("\tArgument Part: " + eventArgumentParts[0].toString());
			return "";
		}

		String baseClassName = "";
		int offset = 0;
		if ( ((EventField)eventArgumentParts[0]).fieldName.equals("this") ) {
			baseClassName = entityClassName;
		} else {
			/**
			 *	We need to match the initial field to a parameter of the source method for this event.
			 */
			for (int i = 0; i < eventMethodNames.length; i++) {
				if (eventMethodNames[i].equals( ((EventField)eventArgumentParts[0]).fieldName )) {
					baseClassName = eventMethodTypes[i];
					break;
				}
				if (i == eventMethodNames.length) {
					System.err.println("Overview Instrumenter Error:");
					System.err.println("\tMethod argument does not specify an object to operate off of (either \"this\" or method argument)");
					System.err.println("\tArgument: " + eventArgumentParts[0].toString());
					return "";
				}
			}
			((EventField)eventArgumentParts[0]).fieldClassType = baseClassName;
			((EventField)eventArgumentParts[0]).fieldType = baseClassName;
			((EventField)eventArgumentParts[0]).fieldSignature = Utility.getSignature(baseClassName);
			offset = 1;
		}

		for (int i = offset; i < eventArgumentParts.length; i++) {
			/**
			 *	resolveClasses returns the type of the field, or return type of the method
			 */
			baseClassName = eventArgumentParts[i].resolveClasses(baseClassName, entityClassName, eventMethod, eventMethodNames, eventMethodTypes);
		}
		return baseClassName;
	}

	public EventArgument(Value value) {
		if (value.children == null) {
			if (value.getToken(0).image.equals("this")) {
				eventArgumentParts = new EventArgumentPart[1];
				eventArgumentParts[0] = new EventField("this");
			} else if (value.getToken(0).image.equals("exception")) {
				eventArgumentParts = new EventArgumentPart[1];
				eventArgumentParts[0] = new EventException();
			} else {
				System.err.println("unknown argument: " + value.getToken(0).image);
				System.err.println("\tFrom EventArgument.<init>");
			}
		} else {
			if (value.getChild(0) instanceof Literal) {
				eventArgumentParts = new EventArgumentPart[1];
				if (value.getChild(0).getChild(0) instanceof StringLiteral) {
					String stringLiteral = value.getChild(0).getChild(0).getJavaCode();
					eventArgumentParts[0] = new EventStringLiteral(stringLiteral.substring(1,stringLiteral.length()-1));
				} else if (value.getChild(0).getChild(0) instanceof CharacterLiteral) {
					eventArgumentParts[0] = new EventCharacterLiteral(new Character(value.getChild(0).getChild(0).getJavaCode().charAt(1)).charValue());
				} else if (value.getChild(0).getChild(0) instanceof FloatingPointLiteral) {
					eventArgumentParts[0] = new EventFloatingPointLiteral(new Double(value.getChild(0).getChild(0).getJavaCode()).doubleValue());
				} else if (value.getChild(0).getChild(0) instanceof IntegerLiteral) {
					eventArgumentParts[0] = new EventIntegerLiteral(new Integer(value.getChild(0).getChild(0).getJavaCode()).intValue());
				}
			} else if (value.tokens != null && value.getToken(0).image.equals("this")) {
				eventArgumentParts = new EventArgumentPart[ value.children.length + 1 ];
				eventArgumentParts[0] = new EventField("this");

				for (int i = 0; i < value.children.length; i++) {
					if (value.getChild(i).tokens.length > 1) {
						Value[] arguments;
						if (value.getChild(i).children != null) {
							arguments = new Value[ value.getChild(i).children.length ];
							for (int j = 0; j < arguments.length; j++) {
								arguments[j] = (Value)value.getChild(i).getChild(j);
							}
						} else {
							arguments = new Value[0];
						}
						String methodName = value.getChild(i).getToken(0).image;
						eventArgumentParts[i+1] = new EventMethod(methodName, arguments);
					} else {
						String fieldName = value.getChild(i).getToken(0).image;
						eventArgumentParts[i+1] = new EventField(fieldName);
					}
				}
			} else {
				eventArgumentParts = new EventArgumentPart[ value.children.length ];

				for (int i = 0; i < value.children.length; i++) {
					if (value.getChild(i).tokens.length > 1) {
						Value[] arguments;
						if (value.getChild(i).children != null) {
							arguments = new Value[ value.getChild(i).children.length ];
							for (int j = 0; j < arguments.length; j++) {
								arguments[j] = (Value)value.getChild(i).getChild(j);
							}
						} else {
							arguments = new Value[0];
						}

						String methodName = value.getChild(i).getToken(0).image;
						eventArgumentParts[i] = new EventMethod(methodName, arguments);
					} else {
						String fieldName = value.getChild(i).getToken(0).image;
						eventArgumentParts[i] = new EventField(fieldName);
					}
				}
			}
		}
	}
}
