package ch.ethz.systems.netbench.xpt.dynamic.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.FlowPathExists;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.VariableGraph;
import edu.asu.emit.algorithm.graph.Vertex;

public class DynamicController extends RemoteRoutingController {
	protected Map<Integer, NetworkDevice> mIdToNetworkDevice;
	protected int max_degree;

	public DynamicController(Map<Integer, NetworkDevice> idToNetworkDevice,NBProperties configuration) {
		super(configuration);
		mPaths = new HashMap<>();
		mIdToNetworkDevice = idToNetworkDevice;
		this.mMainGraph = new VariableGraph(configuration.getGraph(), "graph_weight");
		max_degree = configuration.getIntegerPropertyOrFail("max_dynamic_switch_degree");
		System.out.println("Setting max dynamic degree to " + max_degree);

	}

	@Override
	protected void reset_state(NBProperties configuration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initRoute(int sourceToR, int destToR, int serverSource, int serverDest, long flowId) {
		Pair<Integer, Integer> pair = new ImmutablePair<>(serverSource,serverDest);

		Vertex sourceVertex = new Vertex(sourceToR);
		Vertex destVertex = new Vertex(destToR);
		if(mPaths.containsKey(pair)) {
			mFlowIdsOnCircuit.get(pair).add(flowId);
			throw new FlowPathExists(flowId);
		}
		if(mTransmittingSources.getOrDefault(sourceToR,0) >= max_degree || mRecievingDestinations.getOrDefault(destToR,0)>=max_degree){
			SimulationLogger.increaseStatisticCounter("DYNAMIC_CONTROLLER_NO_PATH");
			throw new NoPathException(sourceToR,destToR);
		}
//		((VariableGraph) mMainGraph).addEdge(source,dest,1);
		DynamicDevice sourceDevice =  (DynamicDevice) mIdToNetworkDevice.get(sourceToR);
		sourceDevice.addConnection(mIdToNetworkDevice.get(sourceToR),mIdToNetworkDevice.get(destToR),serverSource,serverDest);
		LinkedList<Vertex> path = new LinkedList<Vertex>();
		path.add(sourceVertex);
		path.add(destVertex);
		Path finalPath = new Path(path, 1);
		mPaths.put(pair, finalPath);
		HashSet hs = (HashSet) mFlowIdsOnCircuit.getOrDefault(pair,new HashSet<>());
		hs.add(flowId);
		mFlowIdsOnCircuit.put(pair,hs);
		logRoute(finalPath,sourceToR,destToR,flowId, Simulator.getCurrentTime(),true);
		onPathAllocation(sourceToR,destToR);
		mAllocateddPathsNum++;
	}


	@Override
	public void reset() {
		((VariableGraph) mMainGraph).clear();

	}

	@Override
	public void recoverPath(int src, int dst,int serverSource, int serverDest, long flowId) {
//		if(!((VariableGraph) mMainGraph).hasEdge(src,dst)){
//			return;
//		}

		Pair<Integer, Integer> pair = new ImmutablePair<Integer, Integer>(serverSource, serverDest);
		Path p = mPaths.get(pair);

		if(p==null) {
			throw new NoPathException();
		}
//		((VariableGraph) mMainGraph).deleteEdge(pair);
		mFlowIdsOnCircuit.get(pair).remove(flowId);
		if(mFlowIdsOnCircuit.get(pair).isEmpty()){
			DynamicDevice sourceDevice =  (DynamicDevice) mIdToNetworkDevice.get(src);
			sourceDevice.removeConnection(serverSource,serverDest);
			logRoute(mPaths.get(pair),src,dst,flowId, Simulator.getCurrentTime(),false);
			mPaths.remove(pair);
			onPathDeAllocation(src,dst);
			mDeAllocatedPathsNum--;
		}


	}


	@Override
	public void dumpState(String dumpFolderName) throws IOException {
		// TODO Auto-generated method stub

	}


}
