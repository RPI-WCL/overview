package overview.ovi.events;

import org.apache.bcel.classfile.Utility;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.PUSH;


public class EventStringLiteral implements EventArgumentPart, EventLiteral {
	String value;

	public String getType() {
		return "java.lang.String";
	}

	public EventStringLiteral(String value) {
		this.value = value;
	}

	public String toString() {
		return "String Literal: " + value;
	}

	public String resolveClasses(String baseClassName, String entityClassName, String eventMethod, String[] eventMethodNames, String[] eventMethodTypes) {
		return "java.lang.String";
	}

	public void insertPart(InstructionList patch, MethodGen methodGen, ConstantPoolGen constantPoolGen) {
		patch.append(new PUSH(constantPoolGen, value));
	}
}
