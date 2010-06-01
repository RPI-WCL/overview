package overview.ovi;

import overview.ovi.compiler.definitions.*;
import overview.ovi.events.*;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Utility;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.Type;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.LDC;

import org.apache.bcel.Constants;

public class EntitySpecification implements Constants {

	/**
	 *	classFile desginates what class is actually being instrumented
	 */
	String			classFile;
	String			className;

	/**
	 *	entityName is the name of the entity being instrumented
	 */
	String			entityName;

	/**
	 *	watches are all the descriptors for every watch statement in
	 *	the entity specification, and can instrument the byte-code to
	 *	add watch statements (to contact the IPA) where applicable.
	 */
	WatchDescriptor[]	watches;

	/**
	 *	events are all the descriptors for the event statements in
	 *	the entity specification, and can instrument the byte-code to
	 *	add event statements (to contact the IPA) where applicable.
	 */
	EventDescriptor[]	events;	

	public void instrumentByteCode() {
		try {
			JavaClass javaClass = new ClassParser(classFile).parse();
			/**
			 *	Backup the old classfile.
			 */
			javaClass.dump(classFile + "_old");
			ConstantPool constantPool = javaClass.getConstantPool();
			ConstantPoolGen constantPoolGen = new ConstantPoolGen(constantPool);

			Method[] methods = javaClass.getMethods();
			for (int i = 0; i < methods.length; i++) {
				MethodGen methodGen = null;
				Type[] methodTypes = null;
				InstructionList methodInstructionList = null;

				for (int j = 0; j < events.length; j++) {
					if ( methods[i].getName().equals(events[j].sourceMethodName) ) {
						System.err.println("instrumenting: " + events[j].sourceMethodName);

						if (methodGen == null) {
							methodGen = new MethodGen(methods[i], className, constantPoolGen);
							methodTypes = methodGen.getArgumentTypes();
							methodInstructionList = methodGen.getInstructionList();
						}

						//Compare Method Types
						boolean sameTypes = true;
						if (events[j].sourceMethodArgumentClasses.length != methodTypes.length) {
							sameTypes = false;
						} else {
							for (int k = 0; k < events[j].sourceMethodArgumentClasses.length; k++) {
								if (!methodTypes[k].toString().equals(events[j].sourceMethodArgumentClasses[k])) {
									sameTypes = false;
									break;
								}
							}
						}

						if (sameTypes) {
							InstructionList patch = getPatch(methodGen, methods[i], constantPoolGen, events[j]);
							methodInstructionList = insertPatch(methods[i], methodInstructionList, patch, events[j].start);

							/*
							 *	Have to increase the stack size, in case our instrumented code increased it.
							 */
						}
					}
				}
				if (methodGen != null) {
					if(methodGen.getMaxStack() < methodInstructionList.size()) methodGen.setMaxStack(methodInstructionList.size());

					methods[i] = methodGen.getMethod();
					methodInstructionList.dispose();
				}
			}
			javaClass.setConstantPool(constantPoolGen.getFinalConstantPool());
			javaClass.dump(classFile);
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}


	public InstructionList getPatch(MethodGen methodGen, Method method, ConstantPoolGen constantPoolGen, EventDescriptor eventDescriptor) {
		//Make sure we can actually instrument this method
		if(method.isNative() || method.isAbstract() || (method.getCode() == null)) {
			System.err.println("Uninstrumentable method:");
			if (method.isNative()) System.err.println("\tMethod " + method + " is native.");
			else if (method.isAbstract()) System.err.println("\tMethod " + method + " is abstract.");
			else if (method.getCode() == null) System.err.println("\tMethod " + method + " has no available code.");
			System.exit(0);
			return null;
		}

		InstructionList patch = new InstructionList();

		String[] eventParameterTypes = new String[eventDescriptor.eventArguments.length+1];

		constantPoolGen.addString( eventDescriptor.eventType );
		patch.append(new LDC( constantPoolGen.lookupString( eventDescriptor.eventType ) ));
		eventParameterTypes[0] = "java.lang.String";

		for (int i = 1; i < eventParameterTypes.length; i++) {
			eventDescriptor.eventArguments[i-1].insertArgument( patch, methodGen, constantPoolGen );
			eventParameterTypes[i] = eventDescriptor.eventArguments[i-1].getType();
		}
		int ipaCall = constantPoolGen.addMethodref("overview/ovi/ipa/InstrumentedProfilingAgent", "putEvent",
							   Utility.methodTypeToSignature("void", eventParameterTypes));
		patch.append(new INVOKESTATIC(ipaCall));

		/**
		 *	Just use this for debugging purposes, to see what code
		 *	we actually put in the method.
		 */
//		System.err.println(patch.toString(true));
		return patch;
	}

	public InstructionList insertPatch(Method method, InstructionList methodInstructionList, InstructionList patch, boolean start) {
		String name = method.getName();
		InstructionHandle[] instructionHandles = methodInstructionList.getInstructionHandles();

		// First let the super or other constructor be called
		if(name.equals("<init>")) {
			for(int j=1; j < instructionHandles.length; j++) {
				if(instructionHandles[j].getInstruction() instanceof INVOKESPECIAL) {
					methodInstructionList.append(instructionHandles[j], patch);	// Should check: method name == "<init>"
					break;
				}
			}
		} else {
			System.err.println("instructionHandles.length: " + instructionHandles.length);

			if (start) {
				methodInstructionList.insert(instructionHandles[0], patch);
			} else {
				methodInstructionList.insert(instructionHandles[instructionHandles.length-1], patch);
			}
		}
		return methodInstructionList;
	}

	public boolean select(String name) {
		return true;
	}

	public EntitySpecification(CompilationUnit compilationUnit) {
		EntityDeclaration entityDeclaration = (EntityDeclaration)compilationUnit.getChild(0);

		entityName = entityDeclaration.getToken(1).image;
		className = entityDeclaration.getChild(0).getJavaCode();

		classFile = className;
		classFile = classFile.replace('.', '/');
		classFile += ".class";

		EntityBody entityBody = (EntityBody)entityDeclaration.getChild(1);

		int numEvents = 0;
		int numWatches = 0;
		for (int i = 0; i < entityBody.children.length; i++) {
			if (entityBody.getChild(i) instanceof WatchDeclaration) numWatches++;
			if (entityBody.getChild(i) instanceof WhenDeclaration) numEvents++;
		}

		watches = new WatchDescriptor[numWatches];
		events = new EventDescriptor[numEvents];

		int currentWatch = 0;
		int currentEvent = 0;
		for (int i = 0; i < entityBody.children.length; i++) {
			if (entityBody.getChild(i) instanceof WatchDeclaration) {

				watches[currentWatch] = new WatchDescriptor( entityBody.getChild(i).getToken(1).image, (Value)entityBody.getChild(i).getChild(0) );

				watches[currentWatch].resolveClasses(className, null, null, null);
				currentWatch++;

			} else if (entityBody.getChild(i) instanceof WhenDeclaration) {

				boolean start = false;
				if (entityBody.getChild(i).getToken(1).image.equals("start")) start = true;

				String eventType = entityBody.getChild(i).getChild(1).getToken(0).image;

				String sourceMethodName = entityBody.getChild(i).getChild(0).getToken(0).image;
				String[] sourceMethodArgumentNames;
				String[] sourceMethodArgumentClasses;
				if (entityBody.getChild(i).getChild(0).children != null) {
					sourceMethodArgumentNames = new String[entityBody.getChild(i).getChild(0).children.length];
					sourceMethodArgumentClasses = new String[entityBody.getChild(i).getChild(0).children.length];

					for (int j = 0; j < entityBody.getChild(i).getChild(0).children.length; j++) {
						sourceMethodArgumentNames[j] = entityBody.getChild(i).getChild(0).getChild(j).getToken(0).image;
						sourceMethodArgumentClasses[j] = entityBody.getChild(i).getChild(0).getChild(j).getChild(0).getJavaCode();
					}
				} else {
					sourceMethodArgumentNames = new String[0];
					sourceMethodArgumentClasses = new String[0];
				}

				Value[] arguments;

				if (entityBody.getChild(i).getChild(1).children != null) {
					arguments = new Value[ entityBody.getChild(i).getChild(1).children.length ];
					for (int j = 0; j < entityBody.getChild(i).getChild(1).children.length; j++) {
						arguments[j] = (Value)entityBody.getChild(i).getChild(1).getChild(j);
					}
				} else {
					arguments = new Value[0];
				}

				events[currentEvent] = new EventDescriptor(start, sourceMethodName, sourceMethodArgumentNames, 
									   sourceMethodArgumentClasses, eventType, arguments);

				events[currentEvent].resolveClasses(className, sourceMethodName, sourceMethodArgumentNames, sourceMethodArgumentClasses);
				currentEvent++;
			}
		}
	}
}
