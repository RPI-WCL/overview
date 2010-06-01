package overview.ovi.events;

import overview.ovi.compiler.definitions.Value;

import org.apache.bcel.classfile.Utility;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.InstructionList;

import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.INVOKEINTERFACE;

public class EventMethod implements EventArgumentPart {

	public String methodClassType;
	public String methodName;
	public String methodSignature;
	public String methodType;

	public EventArgument[] eventArguments;

	public String getType() {
		return methodType;
	}

	public String toString() {
		return "method: " + methodClassType + ", " + methodName + ", " + methodSignature;
	}

	public String resolveClasses(String baseClassName, String entityClassName, String eventMethod, String[] eventMethodNames, String[] eventMethodTypes) {
		methodClassType = baseClassName;

		/**
		 *      Use reflection to get the type of the field.
		 */
		if (eventMethodTypes == null) {
			eventMethodTypes = new String[0];
			eventMethodNames = new String[0];
		}
		Class[] parameterTypes = new Class[ eventArguments.length ];
		methodType = "";
		try {
			for (int i = 0; i < parameterTypes.length; i++) {
				parameterTypes[i] = Class.forName( eventArguments[i].resolveClasses(entityClassName, eventMethod, eventMethodNames, eventMethodTypes) );
			}

			methodType = Class.forName(baseClassName).getMethod(methodName, parameterTypes).getReturnType().getName();

		} catch (Exception e) {
			System.err.println("Overview Instrumenter Error:");
			System.err.println("\tCould not get get Class");
			System.err.println("\tClass: " + baseClassName);
			System.err.println("\tMethod: " + methodName);
			System.err.println("\tException: " + e);
			e.printStackTrace();
			return "";
		}

		String[] parTypesString = new String[ parameterTypes.length ];
		for (int i = 0; i < parameterTypes.length; i++) {
			parTypesString[i] = parameterTypes[i].getName();
		}

		methodSignature = Utility.methodTypeToSignature(methodType, parTypesString);

		return methodType;
	}

	public void insertPart(InstructionList patch, MethodGen methodGen, ConstantPoolGen constantPoolGen) {
		for (int i = 0; i < eventArguments.length; i++) {
			eventArguments[i].insertArgument(patch, methodGen, constantPoolGen);
		}

		try {
			if (Class.forName(methodClassType).isInterface()) {
				//nargs must be eventArgs+1 to account for the class being invoked upon
				int method = constantPoolGen.addInterfaceMethodref(methodClassType, methodName, methodSignature);
				patch.append(new INVOKEINTERFACE(method, (eventArguments.length + 1)));
			} else {
				int method = constantPoolGen.addMethodref(methodClassType, methodName, methodSignature);
				patch.append(new INVOKEVIRTUAL(method));
			}
		} catch (Exception e) {
			System.err.println("Could not load class: " + methodClassType);
			System.err.println("Exception: e");
			e.printStackTrace();
		}
	}

	public EventMethod(String methodName, Value[] arguments) {
		this.methodName = methodName;

		eventArguments = new EventArgument[ arguments.length ];
		for (int i = 0; i < arguments.length; i++) {
			eventArguments[i] = new EventArgument( arguments[i] );
		}
	}
}
