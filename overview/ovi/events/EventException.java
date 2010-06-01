package overview.ovi.events;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.InstructionList;

public class EventException implements EventArgumentPart {
	public EventException() {}

	public String getType() {
		System.err.println("Trying to get type of an exception. Error events currently not implemented.");
		return "";
	}

	public String resolveClasses(String baseClassName, String entityClassName, String eventMethod, String[] eventMethodNames, String[] eventMethodTypes) {
		System.err.println("Trying to resolve classes for an error event.  Currently not implemented.");
		return "";
	}

	public void insertPart(InstructionList patch, MethodGen methodGen, ConstantPoolGen constantPoolGen) {
		System.err.println("Trying to instrument code for an error event.  Currently not implemented.");
	}
}
