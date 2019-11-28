package ch.ethz.systems.netbench.xpt.simple.simpleudp;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.log.FlowLogger;
import ch.ethz.systems.netbench.core.network.TransportLayer;

public class UDPFlowLogger extends FlowLogger {
    public UDPFlowLogger(long flowId, int sourceId, int targetId, long flowSizeByte) {
        super(flowId, sourceId, targetId, flowSizeByte);
//        this.flowStartTime = TransportLayer.flowMap.get(flowId).startTime;
//        this.measureStartTime = TransportLayer.flowMap.get(flowId).startTime;
    }



}
