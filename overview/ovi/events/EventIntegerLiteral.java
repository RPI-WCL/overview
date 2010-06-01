package overview.ovi.events;

import org.apache.bcel.classfile.Utility;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.PUSH;


public class EventIntegerLiteral implements EventArgumentPart, EventLiteral {
	int value;

	public String getType() {
		return "int";
	}

	public EventIntegerLiteral(int value) {
		this.value = value;
	}

	public String toString() {
		return "Integer Literal: " + value;
	}

	public String resolveClasses(String baseClassName, String entityClassName, String eventMethod, String[] eventMethodNames, String[] eventMethodTypes) {
		return "int";
	}

	public void insertPart(InstructionList patch, MethodGen methodGen, ConstantPoolGen constantPoolGen) {
		patch.append(new PUSH(constantPoolGen, value));
	}
}
