package overview.ovi.events;

import org.apache.bcel.classfile.Utility;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.PUSH;


public class EventFloatingPointLiteral implements EventArgumentPart, EventLiteral {
	double value;

	public String getType() {
		return "double";
	}

	public EventFloatingPointLiteral(double value) {
		this.value = value;
	}

	public String toString() {
		return "double Literal: " + value;
	}

	public String resolveClasses(String baseClassName, String entityClassName, String eventMethod, String[] eventMethodNames, String[] eventMethodTypes) {
		return "double";
	}

	public void insertPart(InstructionList patch, MethodGen methodGen, ConstantPoolGen constantPoolGen) {
		patch.append(new PUSH(constantPoolGen, value));
	}
}
