package core;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;

public class OSMToGraphHopperReader {

	public static GraphHopper createGraph(String osmFile) {
		GraphHopperOSM hopper = new GraphHopperOSM();
		
		hopper.setOSMFile(osmFile);
		
		String[] split = osmFile.split("/");
		String osmName = split[split.length-1];
		String hopperLocation = "tmp_hopper/" + osmName + "/";
		
		hopper.setGraphHopperLocation(hopperLocation);
		hopper.setEncodingManager(new EncodingManager("car"));

		hopper.importOrLoad();
		return hopper;
	}
	
}
