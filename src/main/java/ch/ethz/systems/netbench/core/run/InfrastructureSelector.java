package ch.ethz.systems.netbench.core.run;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.config.exceptions.PropertyValueInvalidException;
import ch.ethz.systems.netbench.core.run.infrastructure.IntermediaryGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.LinkGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.NetworkDeviceGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.OutputPortGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.TransportLayerGenerator;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingOutputPortGenerator;
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
import ch.ethz.systems.netbench.xpt.newreno.newrenodctcp.NewRenoDctcpTransportLayerGenerator;
import ch.ethz.systems.netbench.xpt.newreno.newrenotcp.NewRenoTcpTransportLayerGenerator;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.RemoteSourceRoutingSwitchGenerator;
import ch.ethz.systems.netbench.xpt.simple.simpledctcp.SimpleDctcpTransportLayerGenerator;
import ch.ethz.systems.netbench.xpt.simple.simpletcp.SimpleTcpTransportLayerGenerator;
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
                intermediaryGenerator = new DemoIntermediaryGenerator();
                break;
            }

            case "identity": {
                intermediaryGenerator = new IdentityFlowletIntermediaryGenerator();
                break;
            }

            case "uniform": {
                intermediaryGenerator = new UniformFlowletIntermediaryGenerator();
                break;
            }

            case "low_high_priority": {
                intermediaryGenerator = new PriorityFlowletIntermediaryGenerator();
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
                return new ForwarderSwitchGenerator(intermediaryGenerator, configuration.getGraphDetails().getNumNodes());

            case "ecmp_switch":
                return new EcmpSwitchGenerator(intermediaryGenerator, configuration.getGraphDetails().getNumNodes());

            case "random_valiant_ecmp_switch":
                return new RangeValiantSwitchGenerator(intermediaryGenerator, configuration.getGraphDetails().getNumNodes());

            case "ecmp_then_random_valiant_ecmp_switch":
                return new EcmpThenValiantSwitchGenerator(intermediaryGenerator, configuration.getGraphDetails().getNumNodes());

            case "source_routing_switch":
                return new SourceRoutingSwitchGenerator(intermediaryGenerator, configuration.getGraphDetails().getNumNodes());
            case "remote_source_routing_switch":
                return new RemoteSourceRoutingSwitchGenerator(intermediaryGenerator, configuration.getGraphDetails().getNumNodes());

            case "ecmp_then_source_routing_switch":
                return new EcmpThenSourceRoutingSwitchGenerator(intermediaryGenerator, configuration.getGraphDetails().getNumNodes());

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
                        configuration.getLongPropertyOrFail("link_delay_ns"),
                        configuration.getLongPropertyOrFail("link_bandwidth_bit_per_ns")
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
                        configuration.getLongPropertyOrFail("output_port_ecn_threshold_k_bytes")
                );

            case "priority":
                return new PriorityOutputPortGenerator();

            case "bounded_priority":
                return new BoundedPriorityOutputPortGenerator(
                        configuration.getLongPropertyOrFail("output_port_max_queue_size_bytes")*8
                );

            case "unlimited":
                return new UnlimitedOutputPortGenerator();
            case "remote":
                return new RemoteRoutingOutputPortGenerator();

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
                return new DemoTransportLayerGenerator();

            case "bare":
                return new BareTransportLayerGenerator();

            case "tcp":
                return new NewRenoTcpTransportLayerGenerator();

            case "lstf_tcp":
                return new LstfTcpTransportLayerGenerator();

            case "sp_tcp":
                return new SpTcpTransportLayerGenerator();

            case "sp_half_tcp":
                return new SpHalfTcpTransportLayerGenerator();

            case "pfabric":
                return new PfabricTransportLayerGenerator();
                
            case "pfzero":
                return new PfzeroTransportLayerGenerator();
                
            case "buffertcp":
                return new BufferTcpTransportLayerGenerator();

            case "distmean":
                return new DistMeanTcpTransportLayerGenerator();

            case "distrand":
                return new DistRandTcpTransportLayerGenerator();
            
            case "sparktcp":
                return new SparkTransportLayerGenerator();
                
            case "dctcp":
                return new NewRenoDctcpTransportLayerGenerator();

            case "simple_tcp":
                return new SimpleTcpTransportLayerGenerator();

            case "simple_dctcp":
                return new SimpleDctcpTransportLayerGenerator();
                
            case "remote":
                return new RemoteRoutingTransportLayerGenerator();

            default:
                throw new PropertyValueInvalidException(
                        configuration,
                        "transport_layer"
                );

        }

    }

}
