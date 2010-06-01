package overview.ovi;

import overview.ovi.compiler.OverviewParser;
import overview.ovi.compiler.ParseException;
import overview.ovi.compiler.definitions.CompilationUnit;

public class OverViewInstrumenter {

	public static void main(String[] arguments) {
		System.out.println("OverView Instrumenter v0.1");

		OverviewParser parser = null;

		for (int i = 0; i < arguments.length; i++) {
			System.out.println("\tparsing " + arguments[i]);

			CompilationUnit compilationUnit = null;
			try {
				if (parser == null) {
					parser = new OverviewParser( new java.io.FileInputStream(arguments[i]) );
				} else {
					parser.ReInit( new java.io.FileInputStream(arguments[i]) );
				}

				compilationUnit = (CompilationUnit)parser.CompilationUnit();
			} catch (java.io.IOException e) {
				System.err.println(e);
				throw new Error("IOException reading entity specification: " + arguments[i]);
			} catch (ParseException e) {
				System.err.println(e);
				throw new Error("ParseException on entity specification: " + arguments[i]);
			}

			EntitySpecification entitySpecification = new EntitySpecification( compilationUnit );
			System.out.println("\tinstrumenting byte code...");
			entitySpecification.instrumentByteCode();
			System.out.println("\tcompleted.\n");
		}
	}
}
