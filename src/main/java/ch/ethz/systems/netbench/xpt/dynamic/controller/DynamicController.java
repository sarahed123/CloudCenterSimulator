package ch.ethz.systems.netbench.xpt.dynamic.controller;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import edu.asu.emit.algorithm.graph.VariableGraph;
import edu.asu.emit.algorithm.graph.Vertex;

public class DynamicController extends RemoteRoutingController {
	Map<Integer, NetworkDevice> mIdToNetworkDevice;
	int max_degree;
	public DynamicController(Map<Integer, NetworkDevice> idToNetworkDevice,NBProperties configuration) {
		super(configuration);
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
		if(mG.getAdjacentVertices(new Vertex(source)).size() >= max_degree || mG.getPrecedentVertices(new Vertex(dest)).size() >= max_degree) {
			throw new NoPathException();
		}
		((VariableGraph)mG).addEdge(new ImmutablePair<Integer, Integer>(source, dest));
		DynamicDevice sourceDevice =  (DynamicDevice) mIdToNetworkDevice.get(source);
		sourceDevice.addConnection(mIdToNetworkDevice.get(source),mIdToNetworkDevice.get(dest));

	}

	@Override
	public void reset() {
		((VariableGraph)mG).clear();

	}

	@Override
	public void recoverPath(int src, int dst) {
		((VariableGraph)mG).deleteEdge(new ImmutablePair<Integer, Integer>(src, dst));
		DynamicDevice sourceDevice =  (DynamicDevice) mIdToNetworkDevice.get(src);
		sourceDevice.removeConnection(mIdToNetworkDevice.get(src),mIdToNetworkDevice.get(dst));
		

	}

	@Override
	public void dumpState(String dumpFolderName) throws IOException {
		// TODO Auto-generated method stub

	}

}
