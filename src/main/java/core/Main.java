package core;

import org.apache.log4j.BasicConfigurator;

public class Main {
	
	public static void main(String[] args) {
		BasicConfigurator.configure();
		if (args.length != 2) throw new IllegalArgumentException("expected 2 arguments, " + args.length + " passed");
		String osmFile = args[0].replace('\\', '/');
		String graphastDir = args[1].replace('\\', '/');
		new Importer(osmFile, graphastDir).execute();
	}

}
