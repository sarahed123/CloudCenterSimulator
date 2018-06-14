package ch.ethz.systems.netbench.core.run;


import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.config.TopologyServerExtender;
import ch.ethz.systems.netbench.core.config.exceptions.PropertyConflictException;
import ch.ethz.systems.netbench.core.config.exceptions.PropertyMissingException;
import ch.ethz.systems.netbench.core.config.exceptions.PropertyValueInvalidException;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.BaseInitializer;
import ch.ethz.systems.netbench.core.run.routing.RoutingPopulator;
import ch.ethz.systems.netbench.core.run.traffic.TrafficPlanner;
import ch.ethz.systems.netbench.core.utility.UnitConverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainFromProperties {

    /**
     * Main from properties file.
     *
     * @param args  Command line arguments
     */
    public static void main(String args[]) {

        // Load in the configuration properties
        List<NBProperties> runConfigurations = generateRunConfigurationFromArgs(args);

    	SimulationLogger.openCommon(runConfigurations.get(0));

        
        do {
        	// General property: random seed
            long seed = runConfigurations.get(0).getLongPropertyOrFail("seed");

            // General property: running time in nanoseconds
            long runtimeNs = determineRuntimeNs(runConfigurations.get(0));

            // Setup simulator (it is now public known)
            Simulator.setup(seed, runConfigurations.get(0));

            for(int j = 0;j<runConfigurations.size();j++){

            }
            // Copy configuration files for reproducibility
            SimulationLogger.copyRunConfiguration();

            // Manage topology (e.g. extend with servers if said by configuration)
            manageTopology();

            // Initialization of the three components
            BaseInitializer initializer = generateInfrastructure(runConfigurations.get(0));
            populateRoutingState(initializer.getIdToNetworkDevice());
            if(runConfigurations.get(0).getPropertyWithDefault("from_state",null)==null) {
            	planTraffic(runtimeNs, initializer.getIdToTransportLayer());
            }else {
            	
            }
            

            // Save analysis command
            String analysisCommand = Simulator.getConfiguration().getPropertyWithDefault("analysis_command", null);

            // Perform run
            System.out.println("ACTUAL RUN\n==================");
            try {
            	Simulator.runNs(runtimeNs, Simulator.getConfiguration().getLongPropertyWithDefault("finish_when_first_flows_finish", -1));
            }catch(Exception e) {
            	e.printStackTrace();
            }
            
            Simulator.reset(false);
            System.out.println("Finished run.\n");

            // Perform analysis
            System.out.println("ANALYSIS\n==================");
            if (analysisCommand != null) {
                runCommand(analysisCommand + " " + SimulationLogger.getRunFolderFull(), true);
                System.out.println("Finished analysis.");
            } else {
                System.out.println("No analysis command given; analysis is skipped.");
            }
            try {
				SimulationLogger.logCommon(runConfigurations.get(0));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	runConfigurations.get(0).nextSubConfiguration();
        }while(runConfigurations.get(0).hasSubConfiguration());
        SimulationLogger.closeCommon();

    }

    /**
     * Generate the configuration from the arguments.
     *
     * Command-line template:
     * java -jar NetBench.jar /path/to/run_config.properties param1=val1 param2=val2
     *
     * @param args  Command-line arguments
     *
     * @return Final run configuration
     */
    private static List<NBProperties> generateRunConfigurationFromArgs(String[] args) {

        // Argument length of at least one
        if (args.length < 1) {
            throw new RuntimeException("Expecting first argument to be configuration properties file for the run.");
        }

        List<NBProperties> propertiesList = new LinkedList<NBProperties>();


        // Load in the configuration properties
        NBProperties runConfiguration = new NBProperties(
                args[0],
                BaseAllowedProperties.LOG,
                BaseAllowedProperties.PROPERTIES_RUN,
                BaseAllowedProperties.EXTENSION,
                BaseAllowedProperties.EXPERIMENTAL,
                BaseAllowedProperties.BASE_DIR_VARIANTS
        );
        propertiesList.add(runConfiguration);
        

        // Dynamic overwrite of temporary config using arguments given from command line
        for (int i = 1; i < args.length; i++) {
            try{
                int index = args[i].indexOf('=');
                if(index!=-1){
                    throw new InvalidPropertiesFormatException("arg " + i + " is not a valid property format");
                }
                String param = args[i].substring(0, index);
                String value = args[i].substring(index + 1);
                runConfiguration.overrideProperty(param, value);
            } catch (InvalidPropertiesFormatException e) {
                runConfiguration = new NBProperties(
                        args[0],
                        BaseAllowedProperties.PROPERTIES_RUN,
                        BaseAllowedProperties.EXTENSION,
                        BaseAllowedProperties.EXPERIMENTAL,
                        BaseAllowedProperties.BASE_DIR_VARIANTS
                );
                propertiesList.add(runConfiguration);
            }

        }
        
        propertiesList.get(0).loadSubConfigurtations();
        propertiesList.get(0).constructBaseDir();
        return propertiesList;

    }

    /**
     * Determine the amount of running time in nanoseconds.
     *
     * @param runConfiguration  Run configuration
     *
     * @return Running time in nanoseconds
     */
    private static long determineRuntimeNs(NBProperties runConfiguration) {

        if (runConfiguration.isPropertyDefined("run_time_s") && runConfiguration.isPropertyDefined("run_time_ns")) {
            throw new PropertyConflictException(runConfiguration, "run_time_s", "run_time_ns");

        } else if (runConfiguration.isPropertyDefined("run_time_s")) {
            return UnitConverter.convertSecondsToNanoseconds(runConfiguration.getDoublePropertyOrFail("run_time_s"));

        } else if (runConfiguration.isPropertyDefined("run_time_ns")) {
            return runConfiguration.getLongPropertyOrFail("run_time_ns");

        } else {
            throw new PropertyMissingException(runConfiguration, "run_time_s");
        }

    }

    /**
     * Generate the infrastructure (network devices, output ports,
     * links and transport layers) of the run.
     *
     * @return  Initializer of the infrastructure
     * @param configuration
     */
    private static BaseInitializer generateInfrastructure(NBProperties configuration) {

        // Start infrastructure
        System.out.println("\nINFRASTRUCTURE\n==================");
        BaseInitializer initializer = BaseInitializer.init();
        // 1.1) Generate nodes
        initializer.extend(
                configuration,
                InfrastructureSelector.selectOutputPortGenerator(configuration),
                InfrastructureSelector.selectNetworkDeviceGenerator(configuration),
                InfrastructureSelector.selectLinkGenerator(configuration),
                InfrastructureSelector.selectTransportLayerGenerator(configuration)
        );

        // Finished infrastructure
        System.out.println("Finished creating infrastructure.\n");

        return initializer;

    }

    /**
     * Populate the routing state.
     *
     * @param idToNetworkDevice     Mapping of identifier to network device
     */
    private static void populateRoutingState(Map<Integer, NetworkDevice> idToNetworkDevice) {

        // Start routing
        System.out.println("ROUTING STATE\n==================");

        // 2.1) Populate the routing tables in the switches using the topology defined
        RoutingPopulator populator = RoutingSelector.selectPopulator(idToNetworkDevice);
        populator.populateRoutingTables();

        // Finish routing
        System.out.println("Finished routing state setup.\n");

    }

    /**
     * Plan the traffic.
     *
     * @param runtimeNs             Running time in nanoseconds
     * @param idToTransportLayer    Mapping from node identifier to transport layer
     */
    private static void planTraffic(long runtimeNs, Map<Integer, TransportLayer> idToTransportLayer) {

        // Start traffic generation
        System.out.println("TRAFFIC\n==================");
        
        
        // 3.1) Create flow plan for the simulator
        TrafficPlanner planner = TrafficSelector.selectPlanner(idToTransportLayer);
        planner.createPlan(runtimeNs);

        // Finish traffic generation
        System.out.println("Finished generating traffic flow starts.\n");

    }

    /**
     * Manage the topology, meaning that the topology can be extended with servers.
     *
     * It uses the following properties:
     * scenario_topology_file=/path/to/topology.txt
     * scenario_topology_extend_with_servers=regular
     * scenario_topology_extend_servers_per_tl_node=4
     *
     * It will override the scenario_topology_file in the existing configuration.
     */
    private static void manageTopology(NBProperties configuration) {

        // Copy of original topology to the run folder
        SimulationLogger.copyFileToRunFolder(configuration.getPropertyOrFail("scenario_topology_file"), "original_topology.txt");

        // Topology extension
        if (configuration.isPropertyDefined("scenario_topology_extend_with_servers")) {
            if (configuration.getPropertyWithDefault("scenario_topology_extend_with_servers", "").equals("regular")) {

                // Number of servers to add to each transport layer node
                int serversPerNodeToExtendWith = configuration.getIntegerPropertyOrFail("scenario_topology_extend_servers_per_tl_node");

                // Extend topology
                new TopologyServerExtender(
                        configuration.getTopologyFileNameOrFail(),
                        SimulationLogger.getRunFolderFull() + "/extended_topology.txt"
                ).extendRegular(serversPerNodeToExtendWith);

                // Log info about extension
                SimulationLogger.logInfo("OVERRODE_TOPOLOGY_FILE_WITH_SERVER_EXTENSION", "servers/node=" + serversPerNodeToExtendWith);

            } else {
                throw new PropertyValueInvalidException(configuration, "scenario_topology_extend_with_servers");
            }

            // Override configuration property
            configuration.overrideProperty("scenario_topology_file", SimulationLogger.getRunFolderFull() + "/extended_topology.txt");
            SimulationLogger.logInfo("ARG_OVERRIDE_PARAM(scenario_topology_file)", SimulationLogger.getRunFolderFull() + "/extended_topology.txt");

        }

    }

    /**
     * Run a command in the prompt (e.g. to call a python script).
     * Error write output is always shown.
     *
     * @param cmd           Command
     * @param showOutput    Whether to show the normal write output from the command in the console
     */
    public static void runCommand(String cmd, boolean showOutput) {

        Process p;
        try {

            System.out.println("Running command \"" + cmd + "\"...");

            // Start process
            p = Runtime.getRuntime().exec(cmd);

            // Fetch input streams
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            // Read the output from the command
            String s;
            while ((s = stdInput.readLine()) != null && !showOutput) {
                System.out.println(s);
            }

            // Read any errors from the attempted command
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }

            // Wait for the command thread to be ended
            p.waitFor();
            p.destroy();

            System.out.println("... command has been executed (any output is shown above).");

        } catch (Exception e) {
            throw new RuntimeException("Command failed: " + cmd);
        }

    }

}
