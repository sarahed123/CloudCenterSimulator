package ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.server_optic.OpticServerToR;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.FlowPathExists;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import edu.asu.emit.algorithm.graph.Path;

public class DistributedOpticServerToR extends OpticServerToR {
    public DistributedOpticServerToR(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary, configuration);
    }

    private void tryReserveResources(ReservationPacket rp) {
        if(rp.idDeAllocation()) {
        	throw new NoPathException();
        }
       
        if(rp.isSuccess()){
            throw new FlowPathExists(rp.getFlowId());
        }

        int color = rp.getColor();
        int nextHop = rp.getNextHop(this.getIdentifier());
        if(nextHop == rp.getServerDest()){
        		
            try{
//                ((DistributedController) getRemoteRouter()).reserveServerColor(rp.getServerDest(),color,true);
                ((DistributedController) getRemoteRouter()).updateRoutingTable(identifier,rp.getPrevHop(),nextHop,rp.getColor());
                //the end server will handle this.
//                rp.markSuccess();
//                Path p = new Path(rp.getPath(),rp.getColor());
//                rp.setId(p.getId());
//                SimulationLogger.regiserPathActive(p,true);
//                ((DistributedController) getRemoteRouter()).onAallocation();
//                rp.reverse();
            }catch(NoPathException e){
                rp.markFailure();
                rp.reverse();
                throw new NoPathException();
            }
            return;
        }
        long capacity = ((DistributedController) getRemoteRouter()).checkEdgeCapacity(this.identifier,nextHop,color);
        if(capacity==0){
            rp.markFailure();
            rp.reverse();
            SimulationLogger.increaseStatisticCounter("DISTRIBUTED_TOR_NO_PATH");
            throw new NoPathException();
        }
        assert(capacity==1);
        ((DistributedController) getRemoteRouter()).updateRoutingTable(this.identifier,rp.getPrevHop(),nextHop,rp.getColor());
        ((DistributedController) getRemoteRouter()).decreaseEdgeCapacity(this.identifier,nextHop,color);

    }


    @Override
    public void receive(Packet genericPacket) {

        try{
            handleReservationPacket(genericPacket);
            return;
        }catch (ClassCastException e){

        }

       super.receive(genericPacket);
    }

    protected void handleReservationPacket(Packet genericPacket) {
        ReservationPacket rp = (ReservationPacket) genericPacket;
        try {
        	tryReserveResources(rp);
        }catch (FlowPathExists e){

        }catch (NoPathException e) {
        	deallocateReservation(rp);
//            if(rp.finishedDealloc()) return;
        }

        int nextHop = rp.getNextHop(this.getIdentifier());
        if(this.targetIdToOutputPort.containsKey(nextHop)){
            //if this is true then we must be at the final stage
            this.targetIdToOutputPort.get(nextHop).enqueue(rp);
            return;
        }
        rp.setPrevHop(this.identifier);
//        System.out.println(rp);
//        System.out.println(identifier);
//        System.out.println(nextHop);
//        System.out.println(rp.getFlowId());
        DistributedProtocolPort port = (DistributedProtocolPort) this.electronic.getTargetOuputPort(nextHop);
        port.enqueue(rp);
    }

    private void deallocateReservation(ReservationPacket rp) {
        int color = rp.getColor();
        int nextHop = rp.getNextHop(this.getIdentifier());
        if(nextHop != rp.getServerDest()){

//            System.out.println(identifier + " " + nextHop);
            int l = rp.isReversed() ? nextHop : this.identifier;
            int r = rp.isReversed() ? this.identifier : nextHop;
//            System.out.println(((DistributedController) getRemoteRouter()).checkEdgeCapacity(l,r,color));
//            System.out.println(rp.toString());
            if(((DistributedController) getRemoteRouter()).checkEdgeCapacity(l,r,color)!=0){
                System.out.println(rp);
                System.out.println(((DistributedController) getRemoteRouter()).checkEdgeCapacity(l,r,color));
                System.out.println(rp.getFlowId());
                System.out.println("l " + l + " r " + r);

                throw new RuntimeException();
            }
            ((DistributedController) getRemoteRouter()).increaseEdgeCapacity(l,r,color);
        }else{
//            ((DistributedController) getRemoteRouter()).deallocateServerColor(nextHop,color,rp.isReversed() ? false : true);
//            if(!rp.isReversed()) {
//            	SimulationLogger.regiserPathActive(new Path(rp.getPath(),rp.getColor(),rp.getId()),false);
//                rp.onFinishDeallocation();
//                ((DistributedController) getRemoteRouter()).onDeallocation();
//            }
            
        }

    }

    @Override
    public boolean hadlePacketFromEncapsulating(Packet packet) {
        try{
            handleReservationPacket(packet);
        }catch (ClassCastException e){
            return false;
        }
        return true;
    }
}
