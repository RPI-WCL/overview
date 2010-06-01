package overview.ovi.events;

import org.apache.bcel.classfile.Utility;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.PUSH;


public class EventCharacterLiteral implements EventArgumentPart, EventLiteral {
	char value;

	public String getType() {
		return "char";
	}

	public EventCharacterLiteral(char value) {
		this.value = value;
	}

	public String toString() {
		return "char Literal: " + value;
	}

	public String resolveClasses(String baseClassName, String entityClassName, String eventMethod, String[] eventMethodNames, String[] eventMethodTypes) {
		return "char";
	}

	public void insertPart(InstructionList patch, MethodGen methodGen, ConstantPoolGen constantPoolGen) {
		patch.append(new PUSH(constantPoolGen, value));
	}
}
