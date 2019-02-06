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

public class JumboOpticElectronicHybrid extends OpticElectronicHybrid {


    public JumboOpticElectronicHybrid(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary,configuration);


    }

    @Override
    protected JumboFlow getJumboFlow(int sourceToR, int destToR, int serverSource, int serverDest) {
        return getJumboFlow(sourceToR,destToR).setSourceToR(sourceToR).setDestToR(destToR);
    }





//    /**
//     * called when flowId has finished
//     * will recover the path if the corresponding jumbo flow finished
//     * @param sourceToR
//     * @param destToR
//     * @param serverSource
//     * @param serverDest
//     * @param flowId
//     */
//    public void onFlowFinished(int sourceToR, int destToR,int serverSource,int serverDest, long flowId) {
//        JumboFlow jumboFlow = getJumboFlow(sourceToR,destToR,serverSource,serverDest);
//        jumboFlow.onFlowFinished(flowId);
//        if(jumboFlow.getNumFlows()==0){
//            conversionUnitRecover(jumboFlow,flowId);
//            recoverPath(sourceToR,destToR,serverSource,serverDest,jumboFlow.getId());
//            mJumboFlowMap.remove(new ImmutablePair<>(jumboFlow.getSource(), jumboFlow.getDest()));
//        }
//
//    }
//
//    protected void conversionUnitRecover(JumboFlow jumbo, long flowId) {
//        conversionUnit.onFlowFinish(jumbo.getSource(),jumbo.getDest(),jumbo.getId());
//    }









}