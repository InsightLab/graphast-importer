package core;

import static org.junit.Assert.*;

import org.insightlab.graphast.model.Graph;
import org.insightlab.graphast.query.map_matching.MapMatchingStrategy;
import org.insightlab.graphast.query.map_matching.NaiveMapMatchingStrategy;
import org.insightlab.graphast.query.shortestpath.DijkstraStrategy;
import org.insightlab.graphast.query.shortestpath.ShortestPathStrategy;
import org.insightlab.graphast.query.utils.DistanceVector;
import org.insightlab.graphast.storage.GraphStorage;
import org.insightlab.graphast.storage.GraphStorageFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import com.graphhopper.util.StopWatch;

public class ImporterTest {
	
	private static final String osmDir = "C:/Users/ErickLima/Documents/OSM datasets/monaco-latest.osm.pbf";
	private static final String graphastDir = "C:/Users/ErickLima/Documents/graphast_graphs/monaco";

	private static Graph g;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Main.main(new String[] {osmDir, graphastDir});
		GraphStorage storage = GraphStorageFactory.getKryoGraphStorage();
		g = storage.load(graphastDir);
	}

	@Test
	public void testQuantities() {
		assertEquals("Number of nodes", 815, g.getNumberOfNodes());
		assertEquals("Number of edges", 1013, g.getNumberOfEdges());
	}
	
	@Test
	public void monacoShortestPathTest() {
		
		MapMatchingStrategy mapMatching = new NaiveMapMatchingStrategy(g);
		ShortestPathStrategy shortestPath = new DijkstraStrategy(g);
		
		long sourceId = mapMatching.getNearestNode(43.740174, 7.424376).getId();
		long targetId = mapMatching.getNearestNode(43.735554, 7.416147).getId();
		
		assertEquals("Source id", 229, sourceId);
		assertEquals("Target id", 150, targetId);

		StopWatch sw = new StopWatch();

		sw.start();
		DistanceVector result = shortestPath.run(sourceId, targetId);
		sw.stop();

		result.printPathTo(targetId);
		System.out.println("Execution Time of monacoShortestPathTest(): " + sw.getTime() + " ms");
		System.out.println("Path Total Cost: " + result.getDistance(targetId));
		assertEquals("Distance", 1072.743, result.getDistance(targetId), 0.0001);

	}

}
