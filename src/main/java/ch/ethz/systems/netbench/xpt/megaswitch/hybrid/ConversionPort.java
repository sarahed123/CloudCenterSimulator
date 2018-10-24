package ch.ethz.systems.netbench.xpt.megaswitch.hybrid;

import ch.ethz.systems.netbench.core.log.EmptyPortLogger;
import ch.ethz.systems.netbench.core.log.PortLogger;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.ext.basic.EcnTailDropOutputPort;
import ch.ethz.systems.netbench.ext.basic.IpHeader;

public class ConversionPort extends EcnTailDropOutputPort{

    public ConversionPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link, long maxQueueSizeBytes, long ecnThresholdKBytes) {
        super(ownNetworkDevice, targetNetworkDevice, link, maxQueueSizeBytes, ecnThresholdKBytes);

    }

    @Override
    protected void registerPacketArrivalEvent(Packet packet) {
        targetNetworkDevice.receiveFromEncapsulating(packet);
    }

    @Override
    public void enqueue(Packet packet) {
    	if(!ownNetworkDevice.getConfiguration().getBooleanPropertyWithDefault("enable_jumbo_flows", false)) {
    		assert(getQueue().isEmpty());
    	}
        
        super.enqueue(packet);

    }
    
    protected PortLogger createNewPortLogger() {
		// TODO Auto-generated method stub
		return new EmptyPortLogger(this);
	}
}
