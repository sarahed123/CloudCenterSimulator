package edu.asu.emit.algorithm.graph.algorithms;

import java.util.*;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.run.infrastructure.BaseInitializer;
import edu.asu.emit.algorithm.graph.*;

public class FatTreeShortestPathAlg extends DijkstraShortestPathAlg {
	int ftDegree;
	boolean isInExtendedTopology;
	HashMap<Vertex,Vertex> sourcePredecessorIndex;
	HashMap<Vertex,Vertex> destPredecessorIndex;
	public FatTreeShortestPathAlg(BaseGraph graph, int degree,boolean isInExtendedTopology) {
		super(graph, 6);
		ftDegree = degree;
		this.isInExtendedTopology = isInExtendedTopology;
		sourcePredecessorIndex = new HashMap<Vertex,Vertex>();
		destPredecessorIndex = new HashMap<Vertex,Vertex>();

	}
	
	/*@Override
	public void clear() {
		super.clear();
		isInDecsent.clear();
	}

	
	@Override
	protected void addToPredecessorIndex(Vertex curAdjacentVertex, Vertex vertex) {
		boolean isRightServer = BaseInitializer.getInstance().getNetworkDeviceById(vertex.getId()).isServer();
		boolean isLeftServer = BaseInitializer.getInstance().getNetworkDeviceById(curAdjacentVertex.getId()).isServer();
		if(!isLeftServer && !isRightServer){
			if(vertex.getId() > curAdjacentVertex.getId()) {
				isInDecsent.put(curAdjacentVertex, true);
			}else {
				isInDecsent.put(curAdjacentVertex, false);
			}
		}

		super.addToPredecessorIndex(curAdjacentVertex, vertex);
		
	}*/
	
	@Override
	protected List<Vertex> getVertexNeighbours(Vertex v, boolean isSource2sink){
		List<Vertex> neighbours = super.getVertexNeighbours(v,isSource2sink);
		neighbours.removeIf(vertex -> graph.getEdgeCapacity(v,vertex)==0 || vertex.getId()<v.getId());
		return neighbours;
	}

	protected int getCoreLevel(Vertex s, Vertex t) {
		int level = 2; // level of least common ancestor

		if(s.getId()/(ftDegree/2)==t.getId()/(ftDegree/2)){ // look only 1 level up
			level = 1;
		}
		if(s.getId()==t.getId()){
			level = 0;
		}
		return level;
	}
	
	@Override
	public Paths getShortestPath(Vertex sourceVertex, Vertex sinkVertex)	{
		clear();
		Vertex s = sourceVertex;
		Vertex t = sinkVertex;
		if(isInExtendedTopology){
			s = getVertexNeighbours(s,true).get(0);
			t = getVertexNeighbours(t,true).get(0);
		}
		int level = getCoreLevel(s,t);
		
		HashSet<Vertex> sourceCores = getPathFromVToLevel(s,level,sourcePredecessorIndex);
		HashSet<Vertex> destCores = getPathFromVToLevel(s,level,destPredecessorIndex);
		HashSet<Vertex> finalSet = sourceCores;
		finalSet.retainAll(destCores);
		Paths ps = calculatePaths(finalSet,s,t);
		return null;
	}



	private  Paths calculatePaths(HashSet<Vertex> finalSet, Vertex s, Vertex t) {
		for(Vertex coreVertex : finalSet) {
			Path p = getPathForCoreVertex(coreVertex,s,t);
		}
		
		return null;
		
	}

	private Path getPathForCoreVertex(Vertex coreVertex, Vertex s, Vertex t) {
		LinkedList<Vertex> sourceAlmostToCore = new LinkedList<Vertex>();
		LinkedList<Vertex> destAlmostToCore = new LinkedList<Vertex>();
		Vertex v = sourcePredecessorIndex.get(coreVertex);
		while(v!=null) {
			sourceAlmostToCore.add(v);
			v=sourcePredecessorIndex.get(v);
		}
		
		v = destPredecessorIndex.get(coreVertex);
		while(v!=null) {
			destAlmostToCore.add(v);
			v=destPredecessorIndex.get(v);
		}
		Collections.reverse(sourceAlmostToCore);
		LinkedList<Vertex> finalPath = new LinkedList<Vertex>();
		for(Vertex u : sourceAlmostToCore) {
			finalPath.add(u);
		}
		finalPath.add(coreVertex);
		for(Vertex u : destAlmostToCore) {
			finalPath.add(u);
		}
		return new Path(finalPath,finalPath.size());
	}

	@Override
	public void clear() {
		sourcePredecessorIndex.clear();
		destPredecessorIndex.clear();
	}

	protected HashSet<Vertex> getPathFromVToLevel(Vertex s, int level, HashMap<Vertex, Vertex> predecessorIndex) {
		HashSet<Vertex> coreSet = new HashSet<Vertex>();
		coreSet.add(s);
		while(level > 0){
			HashSet<Vertex> newCoreSet = new HashSet<Vertex>();
			for(Vertex v : coreSet){
				for(Vertex u : getVertexNeighbours(v,true)){
					newCoreSet.add(u);
					predecessorIndex.put(u,v);
				}
			}
			coreSet = newCoreSet;
			level--;
		}
		return coreSet;
	}

}
