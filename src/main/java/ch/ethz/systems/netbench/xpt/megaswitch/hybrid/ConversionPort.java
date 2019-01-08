package ch.ethz.systems.netbench.xpt.megaswitch.hybrid;

import ch.ethz.systems.netbench.ext.basic.TcpPacket;
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

import java.util.HashSet;

public class ConversionPort extends EcnTailDropOutputPort{

    private boolean mDisable;
    private HashSet<Long> finishedFlows;
    public ConversionPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link, long maxQueueSizeBytes, long ecnThresholdKBytes) {
        super(ownNetworkDevice, targetNetworkDevice, link, maxQueueSizeBytes, ecnThresholdKBytes);
        mDisable = false;
        finishedFlows = new HashSet<>();

    }

    @Override
    protected void registerPacketArrivalEvent(Packet packet) {
//    	ImmutablePair p = targetNetworkDevice.getSourceDestinationEncapsulated((IpPacket)packet);
    	
//    	if(!RemoteRoutingController.getInstance().hasRoute(p)) {
//    		SimulationLogger.increaseStatisticCounter("PACKET_SENT_AFTER_PATH_RECOVEREd);
//    		return;
//    	}
        if(finishedFlows.contains(packet.getFlowId())){
            // this shoud not happen
            System.out.println("packet transmitted after flow finished");
            System.out.println(packet.toString());
            System.out.println(packet.getFlowId());
            System.out.println(((TcpPacket)packet).resent);
            return;
//            throw new RuntimeException();
        }
        targetNetworkDevice.receiveFromEncapsulating(packet);
    }

    @Override
    public void enqueue(Packet packet) {
//    	if(!ownNetworkDevice.getConfiguration().getBooleanPropertyWithDefault("enable_jumbo_flows", false)) {
//    		assert(getQueue().isEmpty());
//    	}
        if(!finishedFlows.contains(packet.getFlowId()))
            super.enqueue(packet);
        else{
            System.out.println("flow is finished for " + packet.getFlowId());
            System.out.println(packet.toString());
        }


    }
    
    protected PortLogger createNewPortLogger() {
		// TODO Auto-generated method stub
		return new EmptyPortLogger(this);
	}



    public void onFlowFinished(long flowId) {
        finishedFlows.add(flowId);
    }
}
