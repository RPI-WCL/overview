package overview.ovi.events;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.InstructionList;

public interface EventArgumentPart {

	/**
	 *	Splice Code takes a splice and adds this argument to the splice,
	 *	so that the argument can be used as the argument of a method
	 *	invocation.
	 */

	public void insertPart(InstructionList patch, MethodGen methodGen, ConstantPoolGen constantPoolGen);

	public String resolveClasses(String baseClassName, String entityClassName, String eventMethod, String[] eventMethodNames, String[] eventMethodTypes);

	public String getType();
}
