package ch.ethz.systems.netbench.xpt.mega_switch.dynamic;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.xpt.dynamic.controller.DynamicController;
import ch.ethz.systems.netbench.xpt.dynamic.controller.DynamicDevice;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import edu.asu.emit.algorithm.graph.VariableGraph;
import edu.asu.emit.algorithm.graph.Vertex;

public class MockDynamicController extends DynamicController {
	HashMap<Pair<Integer,Integer>,Boolean> routed;
	HashMap<Pair<Integer,Integer>,Boolean> recovered;
	public MockDynamicController(Map<Integer, NetworkDevice> idToNetworkDevice, NBProperties configuration) {
		super(idToNetworkDevice, configuration);
		routed = new HashMap<>();
		recovered = new HashMap<>();
	}
	
	@Override
	public void initRoute(int source, int dest, long flowId) {
		super.initRoute(source, dest, flowId);
		routed.put(new ImmutablePair<Integer, Integer>(source, dest), true);

	}
	
	@Override
	public void recoverPath(int src, int dst) {
		super.recoverPath(src, dst);
		recovered.put(new ImmutablePair<Integer, Integer>(src, dst), true);

	}
	
	public boolean routed(int source, int dest) {
		// TODO Auto-generated method stub
		return routed.getOrDefault(new ImmutablePair<Integer, Integer>(source, dest), false);
	}
	
	public boolean recovered(int source, int dest) {
		// TODO Auto-generated method stub
		return recovered.getOrDefault(new ImmutablePair<Integer, Integer>(source, dest), false);
	}

}