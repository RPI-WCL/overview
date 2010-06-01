package overview.ovi.events;

import overview.ovi.compiler.definitions.Value;


public class WatchDescriptor {

	public String watchName;
	public EventArgument watchField;

	public void resolveClasses(String entityClassName, String eventMethod, String[] eventMethodNames, String[] eventMethodTypes) {
		watchField.resolveClasses(entityClassName, eventMethod, eventMethodNames, eventMethodTypes);
	}

	public WatchDescriptor(String watchName, Value value) {
		this.watchName = watchName;
		watchField = new EventArgument(value);
	}
}
