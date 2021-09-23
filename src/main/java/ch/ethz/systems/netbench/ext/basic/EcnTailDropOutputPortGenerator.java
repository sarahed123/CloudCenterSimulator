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
import ch.ethz.systems.netbench.xpt.meta_node.v1.MetaNodeOutputPort;

public class EcnTailDropOutputPortGenerator extends OutputPortGenerator {

    protected final long maxQueueSizeBytes;
    protected final long ecnThresholdKBytes;

    public EcnTailDropOutputPortGenerator(long maxQueueSizeBytes, long ecnThresholdKBytes, NBProperties configuration) {
    	super(configuration);
        this.maxQueueSizeBytes = maxQueueSizeBytes;
        this.ecnThresholdKBytes = ecnThresholdKBytes;
        SimulationLogger.logInfo("Port", "ECN_TAIL_DROP(maxQueueSizeBytes=" + maxQueueSizeBytes + ", ecnThresholdKBytes=" + ecnThresholdKBytes + ")");
    }

    @Override
    public OutputPort generate(NetworkDevice ownNetworkDevice, NetworkDevice towardsNetworkDevice, Link link) {
        long maxQueueSizeBytes = this.maxQueueSizeBytes * 
            configuration.getGraph().getEdgeCapacity(ownNetworkDevice.getIdentifier(), towardsNetworkDevice.getIdentifier());
        long ecnThresholdKBytes = this.ecnThresholdKBytes * 
            configuration.getGraph().getEdgeCapacity(ownNetworkDevice.getIdentifier(), towardsNetworkDevice.getIdentifier());;
        if(ownNetworkDevice.isServer() || towardsNetworkDevice.isServer()){
            maxQueueSizeBytes = configuration.getLongPropertyWithDefault("server_output_port_max_queue_size_bytes", maxQueueSizeBytes);
            ecnThresholdKBytes = configuration.getLongPropertyWithDefault("server_output_port_ecn_threshold_k_bytes", ecnThresholdKBytes);
        }
        
        if(Simulator.getConfiguration().getBooleanPropertyWithDefault("distributed_protocol_enabled",false)){
            return new DistributedProtocolPort(ownNetworkDevice, towardsNetworkDevice, link, maxQueueSizeBytes, ecnThresholdKBytes);
        }
        if(configuration.getPropertyWithDefault("network_device_routing", "").equals("meta_node_router")){
            return new MetaNodeOutputPort(ownNetworkDevice,towardsNetworkDevice,link,maxQueueSizeBytes,ecnThresholdKBytes);
        }
        return new EcnTailDropOutputPort(ownNetworkDevice, towardsNetworkDevice, link, maxQueueSizeBytes, ecnThresholdKBytes);
    }

}
