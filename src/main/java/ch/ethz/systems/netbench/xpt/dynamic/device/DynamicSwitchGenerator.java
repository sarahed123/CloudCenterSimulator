package ch.ethz.systems.netbench.xpt.dynamic.device;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.IntermediaryGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.NetworkDeviceGenerator;
import ch.ethz.systems.netbench.ext.demo.DemoIntermediaryGenerator;

public class DynamicSwitchGenerator extends NetworkDeviceGenerator {

	IntermediaryGenerator intermediaryGenerator;
	public DynamicSwitchGenerator(NBProperties configuration) {
		super(configuration);
		intermediaryGenerator = new DemoIntermediaryGenerator(configuration);
		// TODO Auto-generated constructor stub
	}

	@Override
	public NetworkDevice generate(int identifier) {
		// TODO Auto-generated method stub
		return new DynamicSwitch(identifier, null, intermediaryGenerator.generate(identifier), configuration);
	}

	@Override
	public NetworkDevice generate(int identifier, TransportLayer server) {
		// TODO Auto-generated method stub
		return null;
	}

}
