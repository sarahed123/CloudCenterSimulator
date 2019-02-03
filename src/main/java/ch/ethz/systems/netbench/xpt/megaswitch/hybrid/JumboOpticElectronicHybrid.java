package ch.ethz.systems.netbench.xpt.megaswitch.hybrid;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.*;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.Encapsulatable;
import ch.ethz.systems.netbench.xpt.megaswitch.JumboFlow;
import ch.ethz.systems.netbench.xpt.megaswitch.MegaSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.FlowPathExists;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;

public class JumboOpticElectronicHybrid extends OpticElectronicHybrid implements MegaSwitch {

    protected long circuitThreshold;
    protected NetworkDevice electronic;
    protected NetworkDevice optic;
    protected HashMap<Pair<Integer,Integer>,JumboFlow> mJumboFlowMap;
    private long mNumAllocatedFlows;
    private long mNumDeAllocatedFlows;
    protected ConversionUnit conversionUnit;

    public JumboOpticElectronicHybrid(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary,configuration);
        circuitThreshold = configuration.getLongPropertyOrFail("hybrid_circuit_threshold_byte");
        mJumboFlowMap = new HashMap<>();
        mNumAllocatedFlows = 0;
        mNumDeAllocatedFlows = 0;

    }



    protected void routeThroughtPacketSwitch(TcpPacket packet) {
        this.electronic.receiveFromEncapsulating(packet);

    }

    protected void routeThroughCircuit(IpPacket packet, long jumboFlowiId,int sourceToR, int destToR) {
        try {
            initRoute(packet,jumboFlowiId);
        }catch(FlowPathExists e) {

        }
        this.conversionUnit.enqueue(this.identifier,packet.getDestinationId(),packet);



    }

    protected void initRoute(IpPacket packet, long jumboFlowiId) {
        getRemoteRouter().initRoute(this.identifier,packet.getDestinationId(),jumboFlowiId);
    }




//    protected TcpPacket deEncapsulatePacket(Encapsulatable packet) {
//        return (TcpPacket) packet.deEncapsualte();
//    }

    public void onFlowFinished(int source, int dest,int serverSource,int serverDest, long flowId) {
        JumboFlow jumboFlow = getJumboFlow(source,dest);
        jumboFlow.onFlowFinished(flowId);
        if(jumboFlow.getNumFlows()==0){
            conversionUnit.onFlowFinish(source,dest,flowId);
            recoverPath(source,dest,serverSource,serverDest,jumboFlow.getId());
            mJumboFlowMap.remove(new ImmutablePair<>(source, dest));
        }

    }

    protected void recoverPath(int source, int dest,int serverSource,int serverDest, long jumboFlowId) {
        try {

            getRemoteRouter().recoverPath(source,dest,jumboFlowId);
        }catch(NoPathException e) {

        }
    }








}