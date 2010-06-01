package overview.ovi.events;

import org.apache.bcel.classfile.Utility;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.GETFIELD;


public class EventField implements EventArgumentPart {

	public String fieldClassType;
	public String fieldName;
	public String fieldSignature;
	public String fieldType;

	public String getType() {
		return fieldType;
	}

	public EventField(String fieldName) {
		this.fieldName = fieldName;
	}

	public String toString() {
		return "field: " + fieldClassType + ", " + fieldName + ", " + fieldSignature;
	}

	public String resolveClasses(String baseClassName, String entityClassName, String eventMethod, String[] eventMethodNames, String[] eventMethodTypes) {
		fieldClassType = baseClassName;

		/**
		 *	Use reflection to get the type of the field.
		 */
		fieldType = "";
		try {
			if (fieldName.equals("this")) {
				fieldType = baseClassName;
			} else {
				fieldType = Class.forName(baseClassName).getDeclaredField(fieldName).getType().getName();
			}
		} catch (Exception e) {
			System.err.println("Overview Instrumenter Error:");
			System.err.println("\tCould not get get Class");
			System.err.println("\tClass: " + baseClassName);
			System.err.println("\tField: " + fieldName);
			System.err.println("\tException: " + e);
			return "";
		}
		fieldSignature = Utility.getSignature(fieldType);

		return fieldType;
	}

	public void insertPart(InstructionList patch, MethodGen methodGen, ConstantPoolGen constantPoolGen) {
		int field = constantPoolGen.addFieldref(fieldClassType, fieldName, fieldSignature);
		patch.append(new GETFIELD(field));
	}
}
