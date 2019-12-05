package ch.ethz.systems.netbench.xpt.dynamic.opera;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.*;
import ch.ethz.systems.netbench.ext.basic.TcpHeader;
import ch.ethz.systems.netbench.xpt.dynamic.rotornet.ReconfigurationDeadlineException;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class OperaSwitch extends NetworkDevice {

    private List<Packet> directCircuitBuffers;
    private List<Packet> inDirectCircuitBuffers;

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

        try {
            controller.getOutpuPort(this.identifier,header.getDestinationId()).enqueue(genericPacket);
        } catch (OperaNoPathException  | ReconfigurationDeadlineException e) {
                directCircuitBuffers.add(genericPacket);
        }
    }

    @Override
    public void receive(Packet genericPacket) {
        TcpHeader header = (TcpHeader) genericPacket;
        if(this.identifier==header.getDestinationId() && this.intermediary!=null){
            this.passToIntermediary(genericPacket);
            return;
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

        ArrayList<ImmutablePair<Integer,Integer>> possibilities = controller.getPossiblities(this.identifier,header.getDestinationId());

        ImmutablePair<Integer,Integer> nextHopPair = possibilities.get(header.getHash(this.identifier) % possibilities.size());
        try {
            if(header.getSourceId()==this.identifier && !controller.hasPacketPath(header)){
                throw new ReconfigurationDeadlineException();
            }
            controller.getOutpuPort(this.identifier,nextHopPair.getRight()).enqueue(genericPacket);
        } catch (OperaNoPathException e) {
            throw new RuntimeException();
        }catch(ReconfigurationDeadlineException e){

            inDirectCircuitBuffers.add(genericPacket);
        }
    }

    private boolean flowExceedsThreshold(Packet genericPacket) {

        long flowSize = flowSizeMapBit.getOrDefault(genericPacket.getFlowId(),0L);
        flowSize = flowSize + genericPacket.getSizeBit();
        flowSizeMapBit.put(genericPacket.getFlowId(),flowSize);
        return flowSize > directCircuitThresholdBit;
    }

    @Override
    protected void receiveFromIntermediary(Packet genericPacket) {
        receive(genericPacket);
    }

    public void sendPending() {
        for(Packet p: directCircuitBuffers){
            receive(p);
        }
        for(Packet p: inDirectCircuitBuffers){
            receive(p);
        }
    }
}
