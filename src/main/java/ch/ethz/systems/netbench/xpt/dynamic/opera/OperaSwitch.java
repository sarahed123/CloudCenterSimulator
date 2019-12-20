package ch.ethz.systems.netbench.xpt.dynamic.opera;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.*;
import ch.ethz.systems.netbench.ext.basic.TcpHeader;
import ch.ethz.systems.netbench.xpt.dynamic.rotornet.ReconfigurationDeadlineException;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;

public class OperaSwitch extends NetworkDevice {

    private LinkedList<Packet> directCircuitBuffers;
    private LinkedList<Packet> inDirectCircuitBuffers;

    private HashMap<Long,Long> flowSizeMapBit;
    private long directCircuitThresholdBit;
    /**
     * Constructor of a network device.
     *
     * @param identifier     Network device identifier
     * @param transportLayer Transport layer instance (null, if only router and not a server)
     * @param intermediary   Flowlet intermediary instance (takes care of flowlet support)
     * @param configuration
     */
    public OperaSwitch(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary, configuration);
        directCircuitThresholdBit = configuration.getLongPropertyOrFail("opera_direct_circuit_threshold_byte") * 8L;
        directCircuitBuffers = new LinkedList<>();
        inDirectCircuitBuffers = new LinkedList<>();
        flowSizeMapBit = new HashMap<>();

    }

    private void routeDirectly(Packet genericPacket)  {
        TcpHeader header = (TcpHeader) genericPacket;
        OperaController controller = OperaController.getInstance();
        int dest = configuration.getGraphDetails().getTorIdOfServer(header.getDestinationId());
        try {
            controller.getOutpuPort(this.identifier,dest,header.getHash(this.identifier)).enqueue(genericPacket);
        } catch (OperaNoPathException  | ReconfigurationDeadlineException e) {

                directCircuitBuffers.add(genericPacket);
        }
    }

    @Override
    public void receive(Packet genericPacket) {
        Set<Integer> servers = configuration.getGraphDetails().getServersOfTor(this.identifier);
        TcpHeader header = (TcpHeader) genericPacket;

        boolean destToR = servers.contains(header.getDestinationId());
        if(destToR){
            getTargetOuputPort(header.getDestinationId()).enqueue(genericPacket);
            return;
        }

        if(this.identifier==header.getDestinationId()){

            this.passToIntermediary(genericPacket);
            return;
        }

        if(servers.contains(header.getSourceId()) || this.identifier==header.getSourceId()){
            updateFlowSize(genericPacket);
        }
        if(flowExceedsThreshold(genericPacket) && header.getSourceId() == this.identifier){
            routeDirectly(genericPacket);
            return;
        }
        routeInDirectly(genericPacket);

    }

    @Override
    public InputPort getSourceInputPort(int sourceNetworkDeviceId) {
        return OperaController.getInstance().getInputPort(this.identifier,sourceNetworkDeviceId);
    }

    private void routeInDirectly(Packet genericPacket) {

        OperaController controller = OperaController.getInstance();
        TcpHeader header = (TcpHeader) genericPacket;
        int dest = configuration.getGraphDetails().getTorIdOfServer(header.getDestinationId());

        ArrayList<ImmutablePair<Integer,Integer>> possibilities = controller.getPossiblities(this.identifier,dest);

        ImmutablePair<Integer,Integer> nextHopPair = possibilities.get(header.getHash(this.identifier) % possibilities.size());

        try {
            if(header.getSourceId()==this.identifier && !controller.hasPacketPath(this.identifier, dest, header.getHash(identifier))){
                throw new ReconfigurationDeadlineException();
            }
            controller.getOutpuPort(this.identifier,nextHopPair.getRight(), header.getHash(this.identifier)).enqueue(genericPacket);


        } catch (OperaNoPathException e) {
            throw new RuntimeException();
        }catch(ReconfigurationDeadlineException e){

            inDirectCircuitBuffers.add(genericPacket);
        }
    }

    private boolean flowExceedsThreshold(Packet genericPacket) {

        long flowSize = flowSizeMapBit.getOrDefault(genericPacket.getFlowId(),0L);
        return flowSize > directCircuitThresholdBit;
    }

    @Override
    protected void receiveFromIntermediary(Packet genericPacket) {
        // TcpHeader header = (TcpHeader) genericPacket;
        receive(genericPacket);
    }

    private void updateFlowSize(Packet packet){

        long flowSize = flowSizeMapBit.getOrDefault(packet.getFlowId(),0L);
        flowSize = flowSize + packet.getSizeBit();
        flowSizeMapBit.put(packet.getFlowId(),flowSize);
    }

    public void sendPending() {
        int size = directCircuitBuffers.size();

        for(int i = 0; i < size; i++){
            receive(directCircuitBuffers.pop());
        }
        size = inDirectCircuitBuffers.size();
        for(int i = 0; i < size; i++){
            Packet p = inDirectCircuitBuffers.pop();
            receive(p);
        }

    }
}
