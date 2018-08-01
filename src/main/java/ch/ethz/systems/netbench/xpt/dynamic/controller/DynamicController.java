package ch.ethz.systems.netbench.xpt.dynamic.controller;

import java.awt.List;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
		this.mG = new VariableGraph(configuration.getGraph(), "graph_weight");
		max_degree = configuration.getIntegerPropertyOrFail("max_dynamic_switch_degree");
	}

	@Override
	protected void reset_state(NBProperties configuration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initRoute(int source, int dest, long flowId) {
		Pair<Integer, Integer> pair = new ImmutablePair<>(source,dest);

		Vertex sourceVertex = new Vertex(source);
		Vertex destVertex = new Vertex(dest);
		if(mPaths.containsKey(pair)) {
			throw new FlowPathExists(flowId);
		}
		if(mG.getAdjacentVertices(sourceVertex).size() >= max_degree || mG.getPrecedentVertices(destVertex).size() >= max_degree) {
			SimulationLogger.increaseStatisticCounter("DYNAMIC_CONTROLLER_NO_PATH");
			throw new NoPathException();
		}
		((VariableGraph)mG).addEdge(source,dest,1);
		DynamicDevice sourceDevice =  (DynamicDevice) mIdToNetworkDevice.get(source);
		sourceDevice.addConnection(mIdToNetworkDevice.get(source),mIdToNetworkDevice.get(dest));
		LinkedList<Vertex> path = new LinkedList<Vertex>();
		path.add(sourceVertex);
		path.add(destVertex);
		mPaths.put(pair, new Path(path, mG.getEdgeWeight(sourceVertex, destVertex)));
	}

	@Override
	public void reset() {
		((VariableGraph)mG).clear();

	}

	@Override
	public void recoverPath(int src, int dst) {
		if(!((VariableGraph)mG).hasEdge(src,dst)){
			return;
		}
		Pair<Integer, Integer> pair = new ImmutablePair<Integer, Integer>(src, dst);
		((VariableGraph)mG).deleteEdge(pair);
		DynamicDevice sourceDevice =  (DynamicDevice) mIdToNetworkDevice.get(src);
		sourceDevice.removeConnection(mIdToNetworkDevice.get(src),mIdToNetworkDevice.get(dst));
		mPaths.remove(pair);
		

	}

	@Override
	public void dumpState(String dumpFolderName) throws IOException {
		// TODO Auto-generated method stub

	}

}
