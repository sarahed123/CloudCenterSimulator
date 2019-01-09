package ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.server_optic.OpticServerToR;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.FlowPathExists;

public class DistributedOpticServerToR extends OpticServerToR {
    public DistributedOpticServerToR(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary, configuration);
    }

    private void tryReserveResources(ReservationPacket rp) {
        if(rp.isSuccess()){
            throw new FlowPathExists(rp.getFlowId());
        }
        int color = rp.getColor();
        int nextHop = rp.getNextHop(this.getIdentifier());
        if(nextHop == rp.getServerDest()){
            if(((DistributedController) getRemoteRouter()).serverColorAvailable(rp.getServerDest(),color,true)){
                ((DistributedController) getRemoteRouter()).reserveServerColor(rp.getServerDest(),color,true);
                ((DistributedController) getRemoteRouter()).updateRoutingTable(this.identifier, rp.getSourceId(),rp.getServerDest(),nextHop);
                rp.markSuccess();
                rp.reverse();

            }else{
                rp.markFailure();
            }
            return;
        }
        long capacity = ((DistributedController) getRemoteRouter()).checkEdgeCapacity(this.identifier,nextHop,color);
        if(capacity==0){
            rp.markFailure();
            rp.reverse();
            return;
        }
        ((DistributedController) getRemoteRouter()).updateRoutingTable(this.identifier, rp.getSourceId(),rp.getServerDest(),nextHop);
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
        if(rp.idDeAllocation()){
            deallocateReservation(rp);
        }else{
            try{
                tryReserveResources(rp);
            }catch (FlowPathExists e){

            }

        }

        int nextHop = rp.getNextHop(this.getIdentifier());
        if(this.targetIdToOutputPort.containsKey(nextHop)){
            //if this is true then we must be at the final stage
            this.targetIdToOutputPort.get(nextHop).enqueue(rp);
            return;
        }
        this.electronic.getTargetOuputPort(nextHop).enqueue(rp);
    }

    private void deallocateReservation(ReservationPacket rp) {
        int color = rp.getColor();
        int nextHop = rp.getNextHop(this.getIdentifier());
        if(nextHop != rp.getServerDest()){
            assert(((DistributedController) getRemoteRouter()).checkEdgeCapacity(this.identifier,nextHop,color)==0);
            ((DistributedController) getRemoteRouter()).increaseEdgeCapacity(this.identifier,nextHop,color);
        }else{
            ((DistributedController) getRemoteRouter()).deallocateServerColor(nextHop,color,true);
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
