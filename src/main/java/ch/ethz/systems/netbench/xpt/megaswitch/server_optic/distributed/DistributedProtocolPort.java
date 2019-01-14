package ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed;

import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.ext.basic.EcnTailDropOutputPort;
import ch.ethz.systems.netbench.ext.basic.IpHeader;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

public class DistributedProtocolPort extends EcnTailDropOutputPort {
	private int numRpPackets;
    public DistributedProtocolPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link, long maxQueueSizeBytes, long ecnThresholdKBytes) {
        super(ownNetworkDevice, targetNetworkDevice, link, maxQueueSizeBytes, ecnThresholdKBytes, new LinkedList<Packet>());
        numRpPackets = 0;
    }
    
    protected void log(Packet packet) {
    	if(getBufferOccupiedBits() >= ecnThresholdKBits * 4) {
		
//    		System.out.println("Port on device " + this.getOwnId() + " has surpassed ecn thresh with " + numRpPackets + " rp packets. last packet " + packet.toString());
    	}
		
	}

    @Override
    protected void addPacketToQueue(Packet packet){
        try{
            ReservationPacket rp = (ReservationPacket) packet;
            ((LinkedList<Packet>)queue).addFirst(packet);
            numRpPackets++;
            return;
        }catch (ClassCastException e){

        }
        super.addPacketToQueue(packet);
    }
    
    protected Packet popFromQueue() {
		// TODO Auto-generated method stub
    	Packet p = super.popFromQueue();
    	try{
            ReservationPacket rp = (ReservationPacket) p;
            numRpPackets--;
    	}catch(ClassCastException e) {
    		
    	}
    	
		return p;
	}
    
    @Override
    protected boolean hasBufferSpace(IpHeader packet) {
    	 try{
             ReservationPacket rp = (ReservationPacket) packet;
             return true;
         }catch (ClassCastException e){

         }
         return super.hasBufferSpace(packet);
    	
    }
}
