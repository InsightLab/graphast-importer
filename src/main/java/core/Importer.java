package core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.insightlab.graphast.model.Edge;
import org.insightlab.graphast.model.Node;
import org.insightlab.graphast.model.components.spatial_components.Geometry;
import org.insightlab.graphast.model.components.spatial_components.Point;
import org.insightlab.graphast.model.components.spatial_components.SpatialEdgeComponent;
import org.insightlab.graphast.model.components.spatial_components.SpatialNodeComponent;
import org.insightlab.graphast.serialization.SerializationUtils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.graphhopper.GraphHopper;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.PointList;


public class Importer {
	
	private String osmFile, graphastDir;
	private HashSet<Integer> nodeIdSet;
	private Kryo kryo;
	
	public Importer(String osmFile, String graphastDir) {
		this.osmFile = osmFile;
		this.graphastDir = graphastDir;
		this.nodeIdSet = new HashSet<>();
		this.kryo = new Kryo();
	}
	
	private void saveNode(int id, Output output, NodeAccess nodeAccess) {
		if (!nodeIdSet.contains(id)) {
			nodeIdSet.add(id);
			Node node = new Node(id);
			Point p = new Point(nodeAccess.getLatitude(id), nodeAccess.getLongitude(id));
			node.addComponent(new SpatialNodeComponent(p));
			kryo.writeObject(output, node);
		}
	}
	
	private boolean isValid(int fromNodeId, int toNodeId, int direction) {
		return !(fromNodeId == toNodeId || direction == -99999);
	}
	
	private Geometry getGeometry(EdgeIterator edgeIterator) {
		PointList pl = edgeIterator.fetchWayGeometry(3); //3 gets all point including from node and to node
		List<Point> geometry = new ArrayList<Point>();
		for(int i = 0; i < pl.size(); i++) {
			Point p = new Point(pl.getLatitude(i),pl.getLongitude(i));
			geometry.add(p);
		}
		return new Geometry(geometry);
	}
	
	private void saveEdge(EdgeIterator edgeIterator, Output output) {
		int edgeId = edgeIterator.getEdge();
		int fromNodeId = edgeIterator.getBaseNode();
		int toNodeId = edgeIterator.getAdjNode();		
		double distance = edgeIterator.getDistance();
		int direction = getDirection(edgeIterator.getFlags());
		
		Edge e = new Edge(fromNodeId, toNodeId, distance);
		e.setId(edgeId);
		
		if(direction == 0) e.setBidirectional(true); // Bidirectional
		else if(direction == -1) e.invert(); // One direction: to -> from
			
		e.addComponent(new SpatialEdgeComponent(getGeometry(edgeIterator), edgeIterator.getName()));
		
		kryo.writeObject(output, e);		
	}

	public void execute() {
		
		try {
			this.graphastDir = SerializationUtils.ensureDirectory(this.graphastDir);
			
			File f = new File(this.graphastDir);
			if (!f.exists()) f.mkdirs();
		
			Output nodeOutput = new Output(new FileOutputStream(this.graphastDir + "nodes.phast"));
			Output edgeOutput = new Output(new FileOutputStream(this.graphastDir + "edges.phast"));
			Output graphComponentOutput = new Output(new FileOutputStream(this.graphastDir + "graph_components.phast"));
			graphComponentOutput.close();
	
			double initialTime = System.currentTimeMillis();
	
			GraphHopper gh = OSMToGraphHopperReader.createGraph(osmFile);
			GraphHopperStorage gs = gh.getGraphHopperStorage();
			EdgeIterator edgeIterator = gs.getAllEdges();
			NodeAccess nodeAccess = gs.getNodeAccess();
			
			int numberOfEdges = 0;
			while(edgeIterator.next()) {
				
				int fromNodeId = edgeIterator.getBaseNode();
				int toNodeId = edgeIterator.getAdjNode();
				int direction = getDirection(edgeIterator.getFlags());
	
				if (!isValid(fromNodeId, toNodeId, direction)) continue;

				saveNode(fromNodeId, nodeOutput, nodeAccess);
				saveNode(toNodeId, nodeOutput, nodeAccess);

				saveEdge(edgeIterator, edgeOutput);
				numberOfEdges++;
				
			}
			
			nodeOutput.close();
			edgeOutput.close();
			
			System.out.println("Number of nodes: " + nodeIdSet.size());
			System.out.println("Number of edges: " + numberOfEdges);
	
			double totalTime = System.currentTimeMillis() - initialTime;
			System.out.println("Time: " + (totalTime/1000.) + " s");
			
			gh.close();

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}
	
	public int getDirection(long flags) {
		int direction = (int) (flags & 3);
		switch(direction) {
		case 1: return 1;
		case 2: return -1;
		case 3: return 0;
		default: return -99999;
		}
	}


}
