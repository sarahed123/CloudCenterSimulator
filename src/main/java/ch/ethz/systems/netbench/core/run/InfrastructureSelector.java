package ch.ethz.systems.netbench.core.run;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.config.exceptions.PropertyValueInvalidException;
import ch.ethz.systems.netbench.core.run.infrastructure.IntermediaryGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.LinkGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.NetworkDeviceGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.OutputPortGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.TransportLayerGenerator;
import ch.ethz.systems.netbench.core.run.routing.remote.LightOutputPortGenerator;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingTransportLayerGenerator;
import ch.ethz.systems.netbench.ext.bare.BareTransportLayerGenerator;
import ch.ethz.systems.netbench.ext.basic.EcnTailDropOutputPortGenerator;
import ch.ethz.systems.netbench.ext.basic.PerfectSimpleLinkGenerator;
import ch.ethz.systems.netbench.ext.demo.DemoIntermediaryGenerator;
import ch.ethz.systems.netbench.ext.demo.DemoTransportLayerGenerator;
import ch.ethz.systems.netbench.ext.ecmp.EcmpSwitchGenerator;
import ch.ethz.systems.netbench.ext.ecmp.ForwarderSwitchGenerator;
import ch.ethz.systems.netbench.ext.flowlet.IdentityFlowletIntermediaryGenerator;
import ch.ethz.systems.netbench.ext.flowlet.UniformFlowletIntermediaryGenerator;
import ch.ethz.systems.netbench.ext.hybrid.EcmpThenValiantSwitchGenerator;
import ch.ethz.systems.netbench.ext.valiant.RangeValiantSwitchGenerator;
import ch.ethz.systems.netbench.xpt.asaf.routing.priority.PriorityFlowletIntermediaryGenerator;
import ch.ethz.systems.netbench.xpt.dynamic.device.DynamicSwitchGenerator;
import ch.ethz.systems.netbench.xpt.dynamic.opera.OperaSwitchGenerator;
import ch.ethz.systems.netbench.xpt.dynamic.rotornet.RotorSwitchGenerator;
import ch.ethz.systems.netbench.xpt.megaswitch.hybrid.ElectronicOpticHybridGenerator;
import ch.ethz.systems.netbench.xpt.megaswitch.server_optic.OpticServerGenerator;
import ch.ethz.systems.netbench.xpt.meta_node.v1.MetaNodeSwitchGenerator;
import ch.ethz.systems.netbench.xpt.meta_node.v1.MetaNodeTransportLayerGenerator;
import ch.ethz.systems.netbench.xpt.meta_node.v2.EpochMNTransportLayerGenerator;
import ch.ethz.systems.netbench.xpt.meta_node.v2.EpochMetaNodeSwitchGenerator;
import ch.ethz.systems.netbench.xpt.meta_node.v2.EpochOutputPortGenerator;
import ch.ethz.systems.netbench.xpt.newreno.newrenodctcp.NewRenoDctcpTransportLayerGenerator;
import ch.ethz.systems.netbench.xpt.newreno.newrenotcp.NewRenoTcpTransportLayerGenerator;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.RemoteSourceRoutingSwitchGenerator;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.semi.SemiRemoteRoutingSwitchGenerator;
import ch.ethz.systems.netbench.xpt.simple.simpledctcp.SimpleDctcpTransportLayerGenerator;
import ch.ethz.systems.netbench.xpt.simple.simpletcp.SimpleTcpTransportLayerGenerator;
import ch.ethz.systems.netbench.xpt.simple.simpleudp.SimpleUdpTransportLayerGenerator;
import ch.ethz.systems.netbench.xpt.sourcerouting.EcmpThenSourceRoutingSwitchGenerator;
import ch.ethz.systems.netbench.xpt.sourcerouting.SourceRoutingSwitchGenerator;
import ch.ethz.systems.netbench.xpt.voijslav.ports.BoundedPriorityOutputPortGenerator;
import ch.ethz.systems.netbench.xpt.voijslav.ports.PriorityOutputPortGenerator;
import ch.ethz.systems.netbench.xpt.voijslav.ports.UnlimitedOutputPortGenerator;
import ch.ethz.systems.netbench.xpt.voijslav.tcp.buffertcp.BufferTcpTransportLayerGenerator;
import ch.ethz.systems.netbench.xpt.voijslav.tcp.distmeantcp.DistMeanTcpTransportLayerGenerator;
import ch.ethz.systems.netbench.xpt.voijslav.tcp.distrandtcp.DistRandTcpTransportLayerGenerator;
import ch.ethz.systems.netbench.xpt.voijslav.tcp.lstftcp.LstfTcpTransportLayerGenerator;
import ch.ethz.systems.netbench.xpt.voijslav.tcp.pfabric.PfabricTransportLayerGenerator;
import ch.ethz.systems.netbench.xpt.voijslav.tcp.pfzero.PfzeroTransportLayerGenerator;
import ch.ethz.systems.netbench.xpt.voijslav.tcp.sparktcp.SparkTransportLayerGenerator;
import ch.ethz.systems.netbench.xpt.voijslav.tcp.sphalftcp.SpHalfTcpTransportLayerGenerator;
import ch.ethz.systems.netbench.xpt.voijslav.tcp.sptcp.SpTcpTransportLayerGenerator;

class InfrastructureSelector {

    private InfrastructureSelector() {
        // Only static class
    }

    /**
     * Select the network device generator, which, given its identifier,
     * generates an appropriate network device (possibly with transport layer).
     *
     * Selected using following properties:
     * network_device=...
     * network_device_intermediary=...
     *
     * @return  Network device generator.
     * @param configuration
     */
    static NetworkDeviceGenerator selectNetworkDeviceGenerator(NBProperties configuration) {

        /*
         * Select intermediary generator.
         */
        IntermediaryGenerator intermediaryGenerator;
        switch (configuration.getPropertyOrFail("network_device_intermediary")) {

            case "demo": {
                intermediaryGenerator = new DemoIntermediaryGenerator(configuration);
                break;
            }

            case "identity": {
                intermediaryGenerator = new IdentityFlowletIntermediaryGenerator(configuration);
                break;
            }

            case "uniform": {
                intermediaryGenerator = new UniformFlowletIntermediaryGenerator(configuration);
                break;
            }

            case "low_high_priority": {
                intermediaryGenerator = new PriorityFlowletIntermediaryGenerator(configuration);
                break;
            }

            default:
                throw new PropertyValueInvalidException(
                        configuration,
                        "network_device_intermediary"
                );

        }

        /*
         * Select network device generator.
         */
        switch (configuration.getPropertyOrFail("network_device")) {

            case "forwarder_switch":
                return new ForwarderSwitchGenerator(intermediaryGenerator, configuration.getGraphDetails().getNumNodes(), configuration);

            case "ecmp_switch":
                return new EcmpSwitchGenerator(intermediaryGenerator, configuration.getGraphDetails().getNumNodes(),configuration);

            case "random_valiant_ecmp_switch":
                return new RangeValiantSwitchGenerator(intermediaryGenerator, configuration.getGraphDetails().getNumNodes(), configuration);

            case "ecmp_then_random_valiant_ecmp_switch":
                return new EcmpThenValiantSwitchGenerator(intermediaryGenerator, configuration.getGraphDetails().getNumNodes(), configuration);

            case "source_routing_switch":
                return new SourceRoutingSwitchGenerator(intermediaryGenerator, configuration.getGraphDetails().getNumNodes(), configuration);
            case "remote_source_routing_switch":
                return new RemoteSourceRoutingSwitchGenerator(intermediaryGenerator, configuration.getGraphDetails().getNumNodes(), configuration);

            case "ecmp_then_source_routing_switch":
                return new EcmpThenSourceRoutingSwitchGenerator(intermediaryGenerator, configuration.getGraphDetails().getNumNodes(), configuration);

            case "hybrid_optic_electronic":
                return new ElectronicOpticHybridGenerator(intermediaryGenerator,configuration);

            case "rotor_switch":
                return new RotorSwitchGenerator(intermediaryGenerator,configuration);
            case "dynamic_switch":
                return new DynamicSwitchGenerator(intermediaryGenerator,configuration);
            case "semi_remote_routing_switch":
                return new SemiRemoteRoutingSwitchGenerator(intermediaryGenerator,configuration);
            case "optic_server":
                return new OpticServerGenerator(intermediaryGenerator,configuration);
            case "opera_switch":
                return new OperaSwitchGenerator(intermediaryGenerator,configuration);
            case "meta_node_switch":
                return new MetaNodeSwitchGenerator(intermediaryGenerator,configuration.getGraphDetails().getNumNodes(),configuration);
            case "epoch_meta_node_switch":
                return new EpochMetaNodeSwitchGenerator(intermediaryGenerator,configuration.getGraphDetails().getNumNodes(),configuration);
            default:
                throw new PropertyValueInvalidException(
                        configuration,
                        "network_device"
                );

        }

    }

    /**
     * Select the link generator which creates a link instance given two
     * directed network devices.
     *
     * Selected using following property:
     * link=...
     *
     * @return  Link generator
     * @param configuration
     */
    static LinkGenerator selectLinkGenerator(NBProperties configuration) {

        switch (configuration.getPropertyOrFail("link")) {

            case "perfect_simple":
                return new PerfectSimpleLinkGenerator(
                	configuration
		);

            default:
                throw new PropertyValueInvalidException(
                        configuration,
                        "link"
                );

        }

    }

    /**
     * Select the output port generator which creates a port instance given two
     * directed network devices and the corresponding link.
     *
     * Selected using following property:
     * output_port=...
     *
     * @return  Output port generator
     * @param configuration
     */
    static OutputPortGenerator selectOutputPortGenerator(NBProperties configuration) {

        switch (configuration.getPropertyOrFail("output_port")) {

            case "ecn_tail_drop":

                return new EcnTailDropOutputPortGenerator(
                        configuration.getLongPropertyOrFail("output_port_max_queue_size_bytes"),
                        configuration.getLongPropertyOrFail("output_port_ecn_threshold_k_bytes"),
                        configuration
                );
            case "meta_node_epoch":

                return new EpochOutputPortGenerator(
                        configuration.getLongPropertyOrFail("output_port_max_queue_size_bytes"),
                        configuration.getLongPropertyOrFail("output_port_ecn_threshold_k_bytes"),
                        configuration
                );

            case "priority":
                return new PriorityOutputPortGenerator(configuration);

            case "bounded_priority":
                return new BoundedPriorityOutputPortGenerator(
                        configuration.getLongPropertyOrFail("output_port_max_queue_size_bytes")*8, configuration
                );

            case "unlimited":
                return new UnlimitedOutputPortGenerator(configuration);
            case "remote":
            case "light_port":
                return new LightOutputPortGenerator(configuration);

            default:
                throw new PropertyValueInvalidException(
                        configuration,
                        "output_port"
                );

        }

    }

    /**
     * Select the transport layer generator.
     *
     * @return  Transport layer generator
     * @param configuration
     */
    static TransportLayerGenerator selectTransportLayerGenerator(NBProperties configuration) {

        switch (configuration.getPropertyOrFail("transport_layer")) {

            case "demo":
                return new DemoTransportLayerGenerator(configuration);

            case "bare":
                return new BareTransportLayerGenerator(configuration);

            case "tcp":
                return new NewRenoTcpTransportLayerGenerator(configuration);

            case "lstf_tcp":
                return new LstfTcpTransportLayerGenerator(configuration);

            case "sp_tcp":
                return new SpTcpTransportLayerGenerator(configuration);

            case "sp_half_tcp":
                return new SpHalfTcpTransportLayerGenerator(configuration);

            case "pfabric":
                return new PfabricTransportLayerGenerator(configuration);
                
            case "pfzero":
                return new PfzeroTransportLayerGenerator(configuration);
                
            case "buffertcp":
                return new BufferTcpTransportLayerGenerator(configuration);

            case "distmean":
                return new DistMeanTcpTransportLayerGenerator(configuration);

            case "distrand":
                return new DistRandTcpTransportLayerGenerator(configuration);
            
            case "sparktcp":
                return new SparkTransportLayerGenerator(configuration);
                
            case "dctcp":
                return new NewRenoDctcpTransportLayerGenerator(configuration);

            case "simple_tcp":
                return new SimpleTcpTransportLayerGenerator(configuration);

            case "simple_dctcp":
                return new SimpleDctcpTransportLayerGenerator(configuration);
                
            case "remote":
                return new RemoteRoutingTransportLayerGenerator(configuration);

            case "null":
                return new NullTrasportLayer(configuration);

            case "simple_udp":
                return new SimpleUdpTransportLayerGenerator(configuration);
            case "meta_node":
                return new MetaNodeTransportLayerGenerator(configuration);
            case "meta_node_epoch":
                return new EpochMNTransportLayerGenerator(configuration);
            default:
                throw new PropertyValueInvalidException(
                        configuration,
                        "transport_layer"
                );

        }

    }

}
