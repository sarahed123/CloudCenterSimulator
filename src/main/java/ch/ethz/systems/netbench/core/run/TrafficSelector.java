package ch.ethz.systems.netbench.core.run;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.config.exceptions.PropertyValueInvalidException;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.poissontraffic.FromStringArrivalPlanner;
import ch.ethz.systems.netbench.core.run.traffic.TrafficPlanner;
import ch.ethz.systems.netbench.ext.poissontraffic.PoissonArrivalPlanner;
import ch.ethz.systems.netbench.ext.simpletraffic.SimpleTrafficPlanner;
import ch.ethz.systems.netbench.ext.trafficpair.TrafficPairPlanner;
import ch.ethz.systems.netbench.ext.poissontraffic.flowsize.*;
import ch.ethz.systems.netbench.xpt.fluidflow.FluidFlowTrafficPlanner;
import ch.ethz.systems.netbench.xpt.meta_node.MetaNodeA2ATrafficPlanner;
import ch.ethz.systems.netbench.xpt.meta_node.MetaNodePermutationTrafficPlanner;
import ch.ethz.systems.netbench.xpt.meta_node.v1.MockMetaNodePermutationTrafficPlanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class TrafficSelector {

    /**
     * Select the traffic planner which creates and registers the start
     * of flows during the run.
     *
     * Selected using following property:
     * traffic=...
     *
     * @param idToTransportLayer    Node identifier to transport layer mapping
     *
     * @return Traffic planner
     */
    static TrafficPlanner selectPlanner(Map<Integer, TransportLayer> idToTransportLayer, NBProperties configuration) {

        switch (configuration.getPropertyOrFail("traffic")) {

            case "poisson_arrival":

                FlowSizeDistribution flowSizeDistribution;
                switch (configuration.getPropertyOrFail("traffic_flow_size_dist")) {

                    case "original_simon": {
                        flowSizeDistribution = new OriginalSimonFSD();
                        break;
                    }

                    case "pfabric_data_mining_lower_bound": {
                        flowSizeDistribution = new PFabricDataMiningLowerBoundFSD();
                        break;
                    }
                    case "pfabric_data_mining_upper_bound": {
                        flowSizeDistribution = new PFabricDataMiningUpperBoundFSD();
                        break;
                    }

                    case "pfabric_web_search_lower_bound": {
                        flowSizeDistribution = new PFabricWebSearchLowerBoundFSD();
                        break;
                    }

                    case "pfabric_web_search_upper_bound": {
                        flowSizeDistribution = new PFabricWebSearchUpperBoundFSD();
                        break;
                    }
                    case "pareto": {
                        flowSizeDistribution = new ParetoFSD(
                                configuration.getDoublePropertyOrFail("traffic_flow_size_dist_pareto_shape"),
                                configuration.getLongPropertyOrFail("traffic_flow_size_dist_pareto_mean_kilobytes")
                        );
                        break;
                    }

                    case "uniform": {
                        flowSizeDistribution = new UniformFSD(
                                configuration.getLongPropertyOrFail("traffic_flow_size_dist_uniform_mean_bytes")
                        );
                        break;
                    }

                    case "from_csv": {
                        flowSizeDistribution = new FromCSV(
                                configuration.getProperty("csv_size_dist_file_bytes")
                        );
                        break;
                    }

                    default: {
                        throw new PropertyValueInvalidException(
                                configuration,
                                "traffic_flow_size_dist"
                        );
                    }

                }

                // Attempt to retrieve pair probabilities file
                String pairProbabilitiesFile = configuration.getPropertyWithDefault("traffic_probabilities_file", null);

                if (pairProbabilitiesFile != null) {

                    // Create poisson arrival plan from file
	                return new PoissonArrivalPlanner(
	                        idToTransportLayer,
	                        configuration.getIntegerPropertyOrFail("traffic_lambda_flow_starts_per_s"),
	                        flowSizeDistribution,
                            configuration.getPropertyOrFail("traffic_probabilities_file"),
                            configuration
	                );

                }
                else {

                    // If we don't supply the pair probability file we fallback to all-to-all
                    String generativePairProbabilities = configuration.getPropertyWithDefault("traffic_probabilities_generator", "all_to_all");

                    switch (generativePairProbabilities) {
                        case "all_to_all":

                            return new PoissonArrivalPlanner(
                                    idToTransportLayer,
                                    configuration.getIntegerPropertyOrFail("traffic_lambda_flow_starts_per_s"),
                                    flowSizeDistribution,
                                    PoissonArrivalPlanner.PairDistribution.ALL_TO_ALL,
                                    configuration
                            );

                        case "all_to_all_fraction":
                            return new PoissonArrivalPlanner(
                                    idToTransportLayer,
                                    configuration.getIntegerPropertyOrFail("traffic_lambda_flow_starts_per_s"),
                                    flowSizeDistribution,
                                    PoissonArrivalPlanner.PairDistribution.ALL_TO_ALL_FRACTION,
                                    configuration
                            );

                        case "all_to_all_server_fraction":
                            return new SimpleTrafficPlanner(
                                    idToTransportLayer,
                                    configuration.getIntegerPropertyOrFail("traffic_lambda_flow_starts_per_s"),
                                    flowSizeDistribution,
                                    PoissonArrivalPlanner.PairDistribution.ALL_TO_ALL_SERVER_FRACTION, configuration
                            );

                        case "pairings_fraction":
                            return new PoissonArrivalPlanner(
                                    idToTransportLayer,
                                    configuration.getIntegerPropertyOrFail("traffic_lambda_flow_starts_per_s"),
                                    flowSizeDistribution,
                                    PoissonArrivalPlanner.PairDistribution.PAIRINGS_FRACTION,
                                    configuration
                            );

                        case "server_pairings_fraction":
                            return new PoissonArrivalPlanner(
                                    idToTransportLayer,
                                    configuration.getIntegerPropertyOrFail("traffic_lambda_flow_starts_per_s"),
                                    flowSizeDistribution,
                                    PoissonArrivalPlanner.PairDistribution.SERVER_PAIRINGS_FRACTION,
                                    configuration
                            );
                            
                        case "skew_pareto_distribution":
                            return new PoissonArrivalPlanner(
                                    idToTransportLayer,
                                    configuration.getDoublePropertyOrFail("traffic_lambda_flow_starts_per_s"),
                                    flowSizeDistribution,
                                    PoissonArrivalPlanner.PairDistribution.PARETO_SKEW_DISTRIBUTION,
                                    configuration
                            );

                        case "dual_all_to_all_fraction":
                            return new PoissonArrivalPlanner(
                                    idToTransportLayer,
                                    configuration.getIntegerPropertyOrFail("traffic_lambda_flow_starts_per_s"),
                                    flowSizeDistribution,
                                    PoissonArrivalPlanner.PairDistribution.DUAL_ALL_TO_ALL_FRACTION,
                                    configuration
                            );

                        case "dual_all_to_all_server_fraction":
                            return new PoissonArrivalPlanner(
                                    idToTransportLayer,
                                    configuration.getIntegerPropertyOrFail("traffic_lambda_flow_starts_per_s"),
                                    flowSizeDistribution,
                                    PoissonArrivalPlanner.PairDistribution.DUAL_ALL_TO_ALL_SERVER_FRACTION,
                                    configuration
                            );
                        case "fluid_flow":
                            return new FluidFlowTrafficPlanner(idToTransportLayer,
                                    configuration.getIntegerPropertyOrFail("traffic_lambda_flow_starts_per_s"),
                                    flowSizeDistribution,
                                    configuration.getIntegerPropertyWithDefault("scenario_topology_extend_servers_per_tl_node",0) == 0 ?
                                            PoissonArrivalPlanner.PairDistribution.ALL_TO_ALL : PoissonArrivalPlanner.PairDistribution.ALL_TO_ALL_FRACTION,
                                    configuration
                            );
                        case "fluid_flow_perm":
                            return new FluidFlowTrafficPlanner(idToTransportLayer,
                                    configuration.getIntegerPropertyOrFail("traffic_lambda_flow_starts_per_s"),
                                    flowSizeDistribution,
                                    PoissonArrivalPlanner.PairDistribution.PAIRINGS_FRACTION,
                                    configuration
                            );
                        case "fluid_flow_density_matrix":
                            return new FluidFlowTrafficPlanner(idToTransportLayer,
                                configuration.getIntegerPropertyOrFail("traffic_lambda_flow_starts_per_s"),
                                flowSizeDistribution,
                                PoissonArrivalPlanner.PairDistribution.DENSITY_MATRIX,
                                configuration
                        );
                        case "meta_node_premutation_traffic":
                            return new MetaNodePermutationTrafficPlanner(
                                    idToTransportLayer,
                                    configuration.getIntegerPropertyOrFail("traffic_lambda_flow_starts_per_s"),
                                    flowSizeDistribution,
                                    configuration
                            );
                        case "meta_node_a2a_traffic":
                            return new MetaNodeA2ATrafficPlanner(
                                    idToTransportLayer,
                                    configuration.getIntegerPropertyOrFail("traffic_lambda_flow_starts_per_s"),
                                    flowSizeDistribution,
                                    configuration
                            );
                        case "mock_meta_node_premutation_traffic":
                            return new MockMetaNodePermutationTrafficPlanner(
                                    idToTransportLayer,
                                    configuration.getIntegerPropertyOrFail("traffic_lambda_flow_starts_per_s"),
                                    flowSizeDistribution,
                                    configuration
                                );
                        default:
                            throw new PropertyValueInvalidException(configuration, "traffic_probabilities_generator");

                    }


                }

            case "traffic_pair":

                switch (configuration.getPropertyOrFail("traffic_pair_type")) {

                    case "all_to_all":
                        return new TrafficPairPlanner(
                                idToTransportLayer,
                                TrafficPairPlanner.generateAllToAll(
                                        configuration.getGraphDetails().getNumNodes(), configuration
                                ),
                                configuration.getLongPropertyOrFail("traffic_pair_flow_size_byte"), configuration
                        );

                    case "stride":
                        return new TrafficPairPlanner(
                                idToTransportLayer,
                                TrafficPairPlanner.generateStride(
                                        configuration.getGraphDetails().getNumNodes(),
                                        configuration.getIntegerPropertyOrFail("traffic_pair_stride")
                                ),
                                configuration.getLongPropertyOrFail("traffic_pair_flow_size_byte"), configuration
                        );

                    case "custom":
                        List<Integer> list = configuration.getDirectedPairsListPropertyOrFail("traffic_pairs");
                        List<TrafficPairPlanner.TrafficPair> pairs = new ArrayList<>();
                        for (int i = 0; i < list.size(); i += 2) {
                            pairs.add(new TrafficPairPlanner.TrafficPair(list.get(i), list.get(i + 1)));
                        }
                        return new TrafficPairPlanner(
                                idToTransportLayer,
                                pairs,
                                configuration.getLongPropertyOrFail("traffic_pair_flow_size_byte"), configuration
                        );
                }

            case "traffic_arrivals_string":
                return new FromStringArrivalPlanner(idToTransportLayer, configuration.getPropertyOrFail("traffic_arrivals_list"), configuration);

            default:
                throw new PropertyValueInvalidException(
                        configuration,
                        "traffic"
                );

        }

    }

}
