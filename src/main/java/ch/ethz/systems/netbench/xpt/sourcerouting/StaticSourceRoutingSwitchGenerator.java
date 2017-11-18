package ch.ethz.systems.netbench.xpt.sourcerouting;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.IntermediaryGenerator;

public class StaticSourceRoutingSwitchGenerator extends
		SourceRoutingSwitchGenerator {

	public StaticSourceRoutingSwitchGenerator(
			IntermediaryGenerator intermediaryGenerator, int numNodes) {
		super(intermediaryGenerator, numNodes);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void log(){
    	SimulationLogger.logInfo("Network device", "SOURCE_ROUTING_SWITCH(numNodes=" + numNodes + ")");
    }
	
	public NetworkDevice generate(int identifier, TransportLayer transportLayer) {
        return new StaticSourceRoutingSwitch(identifier, transportLayer, numNodes, intermediaryGenerator.generate(identifier));
    }

}
