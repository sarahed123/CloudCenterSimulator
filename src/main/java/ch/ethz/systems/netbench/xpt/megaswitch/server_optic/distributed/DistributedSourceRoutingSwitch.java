package ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed;

import org.apache.commons.lang3.tuple.ImmutablePair;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.semi.SemiRemoteRoutingSwitch;

public class DistributedSourceRoutingSwitch extends SemiRemoteRoutingSwitch {

	public DistributedSourceRoutingSwitch(int identifier, TransportLayer transportLayer, Intermediary intermediary,
                                          NBProperties configuration) {
		super(identifier, transportLayer, intermediary, configuration);
		// TODO Auto-generated constructor stub
	}

	public void updateForwardingTable(int prevHop, int nextHop, int color) {
//		System.out.println("updateForwardingTable " + " prevhop " + prevHop + " color " + color + " nexthop " + nextHop + " id " + identifier);
		super.updateForwardingTable(prevHop, color, nextHop);
		
	}
	
	public OutputPort getNextHop(int prevHop, int color) {
		// TODO Auto-generated method stub
		return forwardingTable.get(new ImmutablePair<Integer,Integer>(prevHop,color));
	}
	
	protected void forwardToNextSwitch(IpPacket packet) {
    	TcpPacket tcpPacket = (TcpPacket) packet;
    	int prevHop = tcpPacket.getPrevHop();
//		System.out.println("forwardToNextSwitch " + " prevhop " + prevHop + " color " + tcpPacket.getColor());
    	tcpPacket.setPrevHop(identifier);
    	forwardingTable.get(new ImmutablePair<Integer,Integer>(prevHop,tcpPacket.getColor())).enqueue(packet);
		
	}


}
