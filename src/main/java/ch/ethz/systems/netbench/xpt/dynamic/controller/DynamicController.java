package ch.ethz.systems.netbench.xpt.dynamic.controller;

import java.io.IOException;
import java.util.HashMap;
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
	public void initRoute(int source, int dest, int serverSource, int serverDest, long flowId) {
		Pair<Integer, Integer> pair = new ImmutablePair<>(source,dest);

		Vertex sourceVertex = new Vertex(source);
		Vertex destVertex = new Vertex(dest);
		if(mPaths.containsKey(pair)) {
			throw new FlowPathExists(flowId);
		}
		if(mMainGraph.getAdjacentVertices(sourceVertex).size() >= max_degree || mMainGraph.getPrecedentVertices(destVertex).size() >= max_degree) {
			SimulationLogger.increaseStatisticCounter("DYNAMIC_CONTROLLER_NO_PATH");
			throw new NoPathException();
		}
		((VariableGraph) mMainGraph).addEdge(source,dest,1);
		DynamicDevice sourceDevice =  (DynamicDevice) mIdToNetworkDevice.get(source);
		sourceDevice.addConnection(mIdToNetworkDevice.get(source),mIdToNetworkDevice.get(dest));
		LinkedList<Vertex> path = new LinkedList<Vertex>();
		path.add(sourceVertex);
		path.add(destVertex);
		Path finalPath = new Path(path, mMainGraph.getEdgeWeight(sourceVertex, destVertex));
		mPaths.put(pair, finalPath);
		logRoute(finalPath,source,dest,flowId, Simulator.getCurrentTime(),true);
	}


	@Override
	public void reset() {
		((VariableGraph) mMainGraph).clear();

	}

	@Override
	public void recoverPath(int src, int dst, long jumboFlowId) {
		if(!((VariableGraph) mMainGraph).hasEdge(src,dst)){
			return;
		}
		Pair<Integer, Integer> pair = new ImmutablePair<Integer, Integer>(src, dst);
		((VariableGraph) mMainGraph).deleteEdge(pair);
		DynamicDevice sourceDevice =  (DynamicDevice) mIdToNetworkDevice.get(src);
		sourceDevice.removeConnection(mIdToNetworkDevice.get(src),mIdToNetworkDevice.get(dst));
		logRoute(mPaths.get(pair),src,dst,jumboFlowId, Simulator.getCurrentTime(),false);
		mPaths.remove(pair);

	}

	@Override
	public void recoverPath(int sourceToR, int destToR, int serverSource, int serverDest, long flowId) {

	}

	@Override
	public void dumpState(String dumpFolderName) throws IOException {
		// TODO Auto-generated method stub

	}

}
