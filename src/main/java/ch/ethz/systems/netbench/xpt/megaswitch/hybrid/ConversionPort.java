package ch.ethz.systems.netbench.xpt.megaswitch.hybrid;

import org.apache.commons.lang3.tuple.ImmutablePair;

import ch.ethz.systems.netbench.core.log.EmptyPortLogger;
import ch.ethz.systems.netbench.core.log.PortLogger;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.ext.basic.EcnTailDropOutputPort;
import ch.ethz.systems.netbench.ext.basic.IpHeader;
import ch.ethz.systems.netbench.ext.basic.IpPacket;

public class ConversionPort extends EcnTailDropOutputPort{

    private boolean mDisable;

    public ConversionPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link, long maxQueueSizeBytes, long ecnThresholdKBytes) {
        super(ownNetworkDevice, targetNetworkDevice, link, maxQueueSizeBytes, ecnThresholdKBytes);
        mDisable = false;

    }

    @Override
    protected void registerPacketArrivalEvent(Packet packet) {
//    	ImmutablePair p = targetNetworkDevice.getSourceDestinationEncapsulated((IpPacket)packet);
    	
//    	if(!RemoteRoutingController.getInstance().hasRoute(p)) {
//    		SimulationLogger.increaseStatisticCounter("PACKET_SENT_AFTER_PATH_RECOVEREd);
//    		return;
//    	}
        if(mDisable){
            // this shoud not happen

            System.out.println(packet.toString());
            throw new RuntimeException();
        }
        targetNetworkDevice.receiveFromEncapsulating(packet);
    }

    @Override
    public void enqueue(Packet packet) {
//    	if(!ownNetworkDevice.getConfiguration().getBooleanPropertyWithDefault("enable_jumbo_flows", false)) {
//    		assert(getQueue().isEmpty());
//    	}
        if(!mDisable)
            super.enqueue(packet);
        else{
            System.out.println(packet.toString());
            throw new RuntimeException();
        }


    }
    
    protected PortLogger createNewPortLogger() {
		// TODO Auto-generated method stub
		return new EmptyPortLogger(this);
	}


    public void disable() {
        this.mDisable = true;
    }
}
