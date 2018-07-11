package ch.ethz.systems.netbench.xpt.mega_switch;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.hybrid.OpticElectronicHybrid;

public class MockFullHybrid extends OpticElectronicHybrid {

	public boolean routedThroughCircuit;
	public boolean routedThroughPacketSwitch;
	public MockFullHybrid(int identifier, TransportLayer transportLayer, Intermediary intermediary,
			NBProperties configuration) {
		super(identifier, transportLayer, intermediary, configuration);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void routeThroughCircuit(IpPacket packet) {
		routedThroughCircuit = true;
		super.routeThroughCircuit(packet);
	}
	
	@Override
	protected void routeThroughtPacketSwitch(TcpPacket packet) {
		routedThroughPacketSwitch = true;
		super.routeThroughtPacketSwitch(packet);
			
	}

}
