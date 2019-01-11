package ch.ethz.systems.netbench.ext.basic;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.run.infrastructure.OutputPortGenerator;
import ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed.DistributedOpticServerToR;
import ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed.DistributedProtocolPort;

public class EcnTailDropOutputPortGenerator extends OutputPortGenerator {

    private final long maxQueueSizeBytes;
    private final long ecnThresholdKBytes;

    public EcnTailDropOutputPortGenerator(long maxQueueSizeBytes, long ecnThresholdKBytes, NBProperties configuration) {
    	super(configuration);
        this.maxQueueSizeBytes = maxQueueSizeBytes;
        this.ecnThresholdKBytes = ecnThresholdKBytes;
        SimulationLogger.logInfo("Port", "ECN_TAIL_DROP(maxQueueSizeBytes=" + maxQueueSizeBytes + ", ecnThresholdKBytes=" + ecnThresholdKBytes + ")");
    }

    @Override
    public OutputPort generate(NetworkDevice ownNetworkDevice, NetworkDevice towardsNetworkDevice, Link link) {
        if(Simulator.getConfiguration().getBooleanPropertyWithDefault("distributed_protocol_enabled",false)){
            return new DistributedProtocolPort(ownNetworkDevice, towardsNetworkDevice, link, maxQueueSizeBytes, ecnThresholdKBytes);
        }
        return new EcnTailDropOutputPort(ownNetworkDevice, towardsNetworkDevice, link, maxQueueSizeBytes, ecnThresholdKBytes);
    }

}
