package overview.ovi.events;

import overview.ovi.compiler.definitions.Value;


public class EventDescriptor {

	public boolean		start;
	public String		sourceMethodName;
	public String[]		sourceMethodArgumentNames;
	public String[]		sourceMethodArgumentClasses;

	public String		eventType;
	public EventArgument[]	eventArguments;

	public void resolveClasses(String entityClassName, String eventMethod, String[] eventMethodNames, String[] eventMethodTypes) {
		for (int i = 0; i < eventArguments.length; i++) {
			eventArguments[i].resolveClasses(entityClassName, eventMethod, eventMethodNames, eventMethodTypes);
		}
	}

	public EventDescriptor(boolean start, String sourceMethodName, String[] sourceMethodArgumentNames,
			       String[] sourceMethodArgumentClasses, String eventType, Value[] arguments) {

		this.start = start;
		this.sourceMethodName = sourceMethodName;

		this.sourceMethodArgumentNames = sourceMethodArgumentNames;
		this.sourceMethodArgumentClasses = sourceMethodArgumentClasses;

		this.eventType = eventType;
		eventArguments = new EventArgument[ arguments.length ];
		for (int i = 0; i < arguments.length; i++) {
			eventArguments[i] = new EventArgument( arguments[i] );
		}
	}

}
