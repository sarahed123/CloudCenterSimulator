package ch.ethz.systems.netbench.ext.poissontraffic;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.run.traffic.TrafficPlanner;
import ch.ethz.systems.netbench.ext.poissontraffic.flowsize.FlowSizeDistribution;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PoissonArrivalPlanner extends TrafficPlanner {

    public enum PairDistribution {
        ALL_TO_ALL,
        ALL_TO_ALL_FRACTION,
        ALL_TO_ALL_SERVER_FRACTION,
        PARETO_SKEW_DISTRIBUTION,
        PAIRINGS_FRACTION,
        SERVER_PAIRINGS_FRACTION,
        DUAL_ALL_TO_ALL_FRACTION,
        DUAL_ALL_TO_ALL_SERVER_FRACTION,
        DENSITY_MATRIX
    }
    protected NBProperties configuration;
    private final double lambdaFlowStartsPerSecond;
    protected final FlowSizeDistribution flowSizeDistribution;
    private final Random ownIndependentRng;
    protected final RandomCollection<Pair<Integer, Integer>> randomPairGenerator;

    protected PoissonArrivalPlanner(Map<Integer, TransportLayer> idToTransportLayerMap, double lambdaFlowStartsPerSecond, FlowSizeDistribution flowSizeDistribution,NBProperties configuration) {
        super(idToTransportLayerMap,configuration);
        this.lambdaFlowStartsPerSecond = lambdaFlowStartsPerSecond;
        this.flowSizeDistribution = flowSizeDistribution;
        this.ownIndependentRng = Simulator.selectIndependentRandom("poisson_inter_arrival");
        this.randomPairGenerator = new RandomCollection<>(Simulator.selectIndependentRandom("pair_probabilities_draw"));
    }

    /**
     * Constructor.
     *
     * @param idToTransportLayerMap     Maps a network device identifier to its corresponding transport layer
     * @param lambdaFlowStartsPerSecond Poisson-arrival lambda
     * @param flowSizeDistribution      Flow size distribution
     * @param fileName                  Name of the input file with communication probability densities
     */
    public PoissonArrivalPlanner(Map<Integer, TransportLayer> idToTransportLayerMap, double lambdaFlowStartsPerSecond, FlowSizeDistribution flowSizeDistribution, String fileName,NBProperties configuration) {
        this(idToTransportLayerMap, lambdaFlowStartsPerSecond, flowSizeDistribution,configuration);
        this.readPairProbabilitiesFromFile(fileName);
        SimulationLogger.logInfo("Flow planner", "POISSON_ARRIVAL(lambda=" + lambdaFlowStartsPerSecond + ", fileName=" + fileName + ")");
    }
    
    /**
     * Constructor.
     *
     * @param idToTransportLayerMap     Maps a network device identifier to its corresponding transport layer
     * @param lambdaFlowStartsPerSecond Poisson-arrival lambda
     * @param flowSizeDistribution      Flow size distribution
     * @param pairDistribution          Choice of static pair distribution which is valid for any topology
     */
    public PoissonArrivalPlanner(Map<Integer, TransportLayer> idToTransportLayerMap, double lambdaFlowStartsPerSecond, FlowSizeDistribution flowSizeDistribution, PairDistribution pairDistribution, NBProperties configuration) {
        this(idToTransportLayerMap, lambdaFlowStartsPerSecond, flowSizeDistribution,configuration);
        this.configuration = configuration;
        switch (pairDistribution) {

            case ALL_TO_ALL:
                this.setPairProbabilitiesAllToAll();
                break;

            case ALL_TO_ALL_FRACTION:
                this.setPairProbabilitiesAllToAllInFraction();
                break;

            case ALL_TO_ALL_SERVER_FRACTION:
                this.setPairProbabilitiesAllToAllInServerFraction(idToTransportLayerMap);
                break;

            case PAIRINGS_FRACTION:
                this.setPairProbabilitiesPairingsInFraction();
                break;

            case SERVER_PAIRINGS_FRACTION:
                this.setPairProbabilitiesServerPairingsInFraction();
                break;
                
            case PARETO_SKEW_DISTRIBUTION:
                this.setPairProbabilitiesParetoSkew();
                break;

            case DUAL_ALL_TO_ALL_FRACTION:
                this.setPairProbabilitiesDualAllToAllFraction();
                break;

            case DUAL_ALL_TO_ALL_SERVER_FRACTION:
                this.setPairProbabilitiesDualAllToAllServerFraction();
                break;
            case DENSITY_MATRIX:
                this.setPairProbabilitiesFromDesity();
                break;
            default:
                throw new IllegalArgumentException("Invalid pair distribution given: " + pairDistribution + ".");

        }
        SimulationLogger.logInfo("Flow planner", "POISSON_ARRIVAL(lambda=" + lambdaFlowStartsPerSecond + ", pairDistribution=" + pairDistribution + ")");
    }

    private void setPairProbabilitiesFromDesity(){
        double activeFractionX = configuration.getDoublePropertyOrFail("traffic_probabilities_active_fraction");
        int nodesNum =  configuration.getGraphDetails().getNumTors();
        int totalNumOfPairs = nodesNum * (nodesNum - 1);
        double torPairProb = 1 / (double) totalNumOfPairs;
        LinkedList<ImmutablePair<Integer,Integer>> allPairs = new LinkedList<>();
        for (int i = 0; i < nodesNum; i++) {
            for (int j = 0; j < nodesNum; j++) {
                if(i==j) continue;
                    allPairs.add(new ImmutablePair<>(i,j));
            }
        }
        Set<Integer> sources = new HashSet<>();
        Set<Integer> dests = new HashSet<>();
        Collections.shuffle(allPairs);
        double activeFraction = activeFractionX*allPairs.size();
        double servProb = activeFraction * configuration.getIntegerPropertyOrFail("scenario_topology_extend_servers_per_tl_node");
        for(int i = 0; i<activeFraction;i++){
            Pair<Integer,Integer> pair = allPairs.get(i);
            Set<Integer> sourceServers = configuration.getGraphDetails().getServersOfTor(pair.getLeft());
            Set<Integer> destServers = configuration.getGraphDetails().getServersOfTor(pair.getRight());
            sources.add(pair.getLeft());
            dests.add(pair.getRight());
            for(int s: sourceServers){
                for(int d: destServers){
                    addToPool(1d/servProb,new ImmutablePair<>(s,d));

                }
            }

        }
        SimulationLogger.logInfo("NUM_TOR_SOURCE_DEST_PAIRS", Integer.toString((int)activeFraction));
        SimulationLogger.logInfo("NUM_TOR_SOURCES_PARTICIPATING", Integer.toString(sources.size()));
        SimulationLogger.logInfo("NUM_TOR_DESTS_PARTICIPATING", Integer.toString(dests.size()));
    }
    
    /**
     * Set the communication pair probabilities to be all-to-all uniform.
     */
    private void setPairProbabilitiesParetoSkew() {

        System.out.print("Generating pareto-skewed pair probabilities between all ToRs...");

        // Get the random generator for this part
        Random gen = Simulator.selectIndependentRandom("skew_pareto_distribution");
        ParetoDistribution pareto = new ParetoDistribution(
                configuration.getDoublePropertyOrFail("traffic_pareto_skew_shape"),
                10, // Scale does not matter because of normalization
                gen
        );

        // ToRs, shuffled
        List<Integer> tors = new ArrayList<>(configuration.getGraphDetails().getTorNodeIds());
        Collections.shuffle(tors, gen);

        // For every ToR, draw its probability mass
        ArrayList<Double> probRes = new ArrayList<>();
        double sumAll = 0;
        for (int i = 0; i < tors.size(); i++) {
            double curProb = pareto.draw();
            sumAll += curProb;
            probRes.add(curProb);
        }

        // Normalize the "probability mass" by the total sum of "probability mass",
        // such that the results is a normalized Pareto distribution
        for (int i = 0; i < probRes.size(); i++) {
            probRes.set(i, probRes.get(i) / sumAll);
        }
        Collections.sort(probRes);

        // Calculate how much probability is wasted on the diagonal
        double torExcluded = 0.0;
        for (int i = 0; i < tors.size(); i++) {
            torExcluded += probRes.get(i) * probRes.get(i);
        }

        // Write away to random pair generator the pairs and their respective probability
        for (int i = 0; i < tors.size(); i++) {
            for (int j = 0; j < tors.size(); j++) {
                if (i != j) {

                    double torPairProb = probRes.get(i) * probRes.get(j) / (1 - torExcluded); // ToR-pair probability with diagonal waste normalized out
                    List<Integer> srcServers = new ArrayList<>(configuration.getGraphDetails().getServersOfTor(tors.get(i)));
                    List<Integer> dstServers = new ArrayList<>(configuration.getGraphDetails().getServersOfTor(tors.get(j)));

                    double serverProb = torPairProb / (srcServers.size() * dstServers.size());
                    for (int src : srcServers) {
                        for (int dst : dstServers) {
                            addToPool(serverProb,new ImmutablePair<>(src, dst));
                        }
                    }

                }
            }
        }

        System.out.println(" done.");
        System.out.println("Top 20 ToR probabilities:");
        for (int i = tors.size() - 1; i >= Math.max(0, tors.size() - 20); i--) {
            System.out.println("ToR #" + tors.get(i) + " has probability " + probRes.get(i));
        }

    }

    protected void addToPool(double serverProb, ImmutablePair<Integer, Integer> pair) {
        this.randomPairGenerator.add(serverProb, pair);
    }

    /**
     * Set the communication pair probabilities to be all-to-all uniform.
     */
    private void setPairProbabilitiesAllToAll() {

        System.out.print("Generating all-to-all pair probabilities between all nodes with a transport layer...");

        // Uniform probability for every server pair
        double pdfNumBytes = 1.0 / (this.idToTransportLayerMap.size() * (this.idToTransportLayerMap.size() - 1));

        // Add uniform probability for every pair
        for (Integer src : this.idToTransportLayerMap.keySet()){
            for (Integer dst : this.idToTransportLayerMap.keySet()){
                if(!src.equals(dst)) {
                    addToPool(pdfNumBytes,new ImmutablePair<>(src, dst));
                }
            }
        }

        System.out.println(" done.");

    }

    /**
     * Set the pair probabilities to be all-to-all within a fraction of the servers.
     */
    private void setPairProbabilitiesAllToAllInServerFraction(Map<Integer, TransportLayer> idToTransportLayerMap) {


        // Retrieve necessary parameters from the extension
        double activeFractionX = configuration.getDoublePropertyOrFail("traffic_probabilities_active_fraction");
        boolean fractionIsOrdered = configuration.getBooleanPropertyOrFail("traffic_probabilities_active_fraction_is_ordered");

        // Shuffle nodes
        List<Integer> servers = new ArrayList<>();
        for (Integer i : idToTransportLayerMap.keySet()) {
            servers.add(i);
        }
        Collections.sort(servers);
        if (!fractionIsOrdered) {
            Collections.shuffle(servers, Simulator.selectIndependentRandom("all_to_all_fraction_shuffle"));
        }
        int numChosenServers = (int) Math.floor(servers.size() * activeFractionX);

        List<Integer> chosen = initRandomServerSet(servers,numChosenServers);


        // Log chosen fraction
        Collections.sort(chosen);
        SimulationLogger.logInfo("A2A_FRACTION_CHOSEN_SERVERS", chosen.toString());

        System.out.println(" done.");

    }

    protected List<Integer> initRandomServerSet(List<Integer> servers, int numChosenServers){
        List<Integer> chosen = new ArrayList<>();
        // Probability between each server pair
        double serverPairProb = 1.0 / (numChosenServers * (numChosenServers - 1));
        // Go over every server pair
        for (int i = 0; i < numChosenServers; i++) {
            chosen.add(servers.get(i));
            for (int j = 0; j < numChosenServers; j++) {
                if (i != j) {
                    addToPool(serverPairProb, new ImmutablePair<>(servers.get(i), servers.get(j)));
                }

            }
        }
        return chosen;
    }

    /**
     * Set the pair probabilities to be all-to-all within a fraction of the nodes.
     */
    private void setPairProbabilitiesAllToAllInFraction() {


        // Retrieve necessary parameters from the extension
        int numTors = configuration.getGraphDetails().getNumTors();
        int serversPerNodeToExtendWith = configuration.getIntegerPropertyOrFail("scenario_topology_extend_servers_per_tl_node");
        boolean fractionIsOrdered = configuration.getBooleanPropertyOrFail("traffic_probabilities_active_fraction_is_ordered");
        double activeFractionX = configuration.getDoublePropertyOrFail("traffic_probabilities_active_fraction");

        // Shuffle nodes
        List<Integer> tors = new ArrayList<>();
        for (int i = 0; i < numTors; i++) {
            tors.add(i);
        }
        if (!fractionIsOrdered) {
            Collections.shuffle(tors, Simulator.selectIndependentRandom("all_to_all_fraction_shuffle"));
        }
        int numChosenTors = (int) Math.floor(numTors * activeFractionX);

        // Probability between each server pair
        //double serverPairProb = 1.0 / (numChosenTors * (numChosenTors - 1) * serversPerNodeToExtendWith * serversPerNodeToExtendWith);
	double serverPairs = 0.0;
        for (int i = 0; i < numChosenTors; i++) {
            for (int j = 0; j < numChosenTors; j++) {

                    int torA = tors.get(i);
                    int torB = tors.get(j);
                    for (Integer svrA : configuration.getGraphDetails().getServersOfTor(torA)) {
                        for (Integer svrB : configuration.getGraphDetails().getServersOfTor(torB)) {
                            // Add to random pair generator
		    	    if(svrA.equals(svrB)) continue;
                            serverPairs++;
                        }
                    }


            }
        }
	double serverPairProb = 1.0/ serverPairs;

        System.out.print("Generating all-to-all pair probabilities in " + (activeFractionX * 100) + "% fraction " + tors.size() + " ToRs between their servers...");

        // Go over every ToR pair
        List<Integer> chosen = new ArrayList<>();
        for (int i = 0; i < numChosenTors; i++) {
            chosen.add(tors.get(i));
            for (int j = 0; j < numChosenTors; j++) {

                    int torA = tors.get(i);
                    int torB = tors.get(j);

                    for (Integer svrA : configuration.getGraphDetails().getServersOfTor(torA)) {
                        for (Integer svrB : configuration.getGraphDetails().getServersOfTor(torB)) {
                            // Add to random pair generator
		    	    if(svrA.equals(svrB)) continue;
                            addToPool(serverPairProb, new ImmutablePair<>(svrA, svrB));
                        }
                    }


            }
        }

        // Log chosen fraction
        Collections.sort(chosen);
        SimulationLogger.logInfo("A2A_FRACTION_CHOSEN_TORS", chosen.toString());

        System.out.println(" done.");

    }

    /**
     * Set the pair probabilities to be dual all-to-all within two fraction of the nodes.
     */
    private void setPairProbabilitiesDualAllToAllFraction() {

        // Retrieve necessary parameters from the extension
        int numTors = configuration.getGraphDetails().getNumTors();
        boolean fractionIsOrdered = configuration.getBooleanPropertyOrFail("traffic_probabilities_active_fraction_is_ordered");
        double activeFractionA = configuration.getDoublePropertyOrFail("traffic_probabilities_fraction_A");
        double probabilityMassA = configuration.getDoublePropertyOrFail("traffic_probabilities_mass_A");


        // Shuffle nodes
        List<Integer> tors = new ArrayList<>();
        for (int i = 0; i < numTors; i++) {
            tors.add(i);
        }
        if (!fractionIsOrdered) {
            Collections.shuffle(tors, Simulator.selectIndependentRandom("all_to_all_fraction_shuffle"));
        }
        int numChosenTorsA = (int) Math.floor(numTors * activeFractionA);
        int numChosenTorsB = numTors - numChosenTorsA;

        if (numChosenTorsA == 0 || numChosenTorsB == 0) {
            throw new RuntimeException("Cannot have an empty fraction on either side for the all-to-all dual fraction probabilities.");
        }

        double probabilityPerA = probabilityMassA / (double) numChosenTorsA;
        double probabilityPerB = (1.0 - probabilityMassA) / (double) numChosenTorsB;

        // Calculate how much probability is wasted on the diagonal
        double wastedProbability = 0.0;
        wastedProbability += numChosenTorsA * probabilityPerA * probabilityPerA;
        wastedProbability += numChosenTorsB * probabilityPerB * probabilityPerB;

        SimulationLogger.logInfo("DUAL_FRACTION_A", "Portion A: (n=" + numChosenTorsA + ", p=" + probabilityPerA + ", tot=" + probabilityMassA + ")");
        SimulationLogger.logInfo("DUAL_FRACTION_B", "Portion B: (n=" + numChosenTorsB + ", p=" + probabilityPerB + ", tot=" + (1-probabilityMassA) + ")");
        System.out.println("Portion A: (n=" + numChosenTorsA + ", p=" + probabilityPerA + ", tot=" + probabilityMassA + ")");
        System.out.println("Portion B: (n=" + numChosenTorsB + ", p=" + probabilityPerB + ", tot=" + (1-probabilityMassA) + ")");
        System.out.print("Generating all-to-all dual fraction probabilities... ");

        // Go over every ToR pair
        for (int i = 0; i < tors.size(); i++) {
            double torProbI = i < numChosenTorsA ? probabilityPerA : probabilityPerB;
            for (int j = 0; j < tors.size(); j++) {
                double torProbJ = j < numChosenTorsA ? probabilityPerA : probabilityPerB;
                if (i != j) {

                    // ToR-pair probability with diagonal waste normalized out
                    double torPairProb = torProbI * torProbJ / (1 - wastedProbability);
                    List<Integer> srcServers = new ArrayList<>(configuration.getGraphDetails().getServersOfTor(tors.get(i)));
                    List<Integer> dstServers = new ArrayList<>(configuration.getGraphDetails().getServersOfTor(tors.get(j)));

                    double serverProb = torPairProb / (srcServers.size() * dstServers.size());
                    for (int src : srcServers) {
                        for (int dst : dstServers) {
                            addToPool(serverProb, new ImmutablePair<>(src, dst));
                        }
                    }

                }

            }
        }

        System.out.println(" done.");

    }

    /**
     * Set the pair probabilities to be dual all-to-all within two fraction of the nodes.
     */
    private void setPairProbabilitiesDualAllToAllServerFraction() {

        // Retrieve necessary parameters from the extension
        int numTors = configuration.getGraphDetails().getNumTors();
        boolean fractionIsOrdered = configuration.getBooleanPropertyOrFail("traffic_probabilities_active_fraction_is_ordered");
        double activeFractionA = configuration.getDoublePropertyOrFail("traffic_probabilities_fraction_A");
        double probabilityMassA = configuration.getDoublePropertyOrFail("traffic_probabilities_mass_A");


        // Shuffle nodes
        List<Integer> tors = new ArrayList<>();
        for (int i = 0; i < numTors; i++) {
            tors.add(i);
        }
        if (!fractionIsOrdered) {
            Collections.shuffle(tors, Simulator.selectIndependentRandom("all_to_all_fraction_shuffle"));
        }
        int numChosenTorsA = (int) Math.floor(numTors * activeFractionA);
        int numChosenTorsB = numTors - numChosenTorsA;

        if (numChosenTorsA == 0 || numChosenTorsB == 0) {
            throw new RuntimeException("Cannot have an empty fraction on either side for the all-to-all dual fraction probabilities.");
        }

        double probabilityPerA = probabilityMassA / (double) numChosenTorsA;
        double probabilityPerB = (1.0 - probabilityMassA) / (double) numChosenTorsB;

        // Calculate how much probability is wasted on the diagonal
        double wastedProbability = 0.0;
        wastedProbability += numChosenTorsA * probabilityPerA * probabilityPerA;
        wastedProbability += numChosenTorsB * probabilityPerB * probabilityPerB;

        SimulationLogger.logInfo("SERVER_DUAL_FRACTION_A", "Portion A: (n=" + numChosenTorsA + ", p=" + probabilityPerA + ", tot=" + probabilityMassA + ")");
        SimulationLogger.logInfo("SERVER_DUAL_FRACTION_B", "Portion B: (n=" + numChosenTorsB + ", p=" + probabilityPerB + ", tot=" + (1-probabilityMassA) + ")");
        System.out.println("Portion A: (n=" + numChosenTorsA + ", p=" + probabilityPerA + ", tot=" + probabilityMassA + ")");
        System.out.println("Portion B: (n=" + numChosenTorsB + ", p=" + probabilityPerB + ", tot=" + (1-probabilityMassA) + ")");
        System.out.print("Generating all-to-all dual fraction probabilities... ");

        // Go over every ToR pair
        for (int i = 0; i < tors.size(); i++) {
            double torProbI = i < numChosenTorsA ? probabilityPerA : probabilityPerB;
            for (int j = 0; j < tors.size(); j++) {
                double torProbJ = j < numChosenTorsA ? probabilityPerA : probabilityPerB;
                if (i != j) {
                    // ToR-pair probability with diagonal waste normalized out
                    double torPairProb = torProbI * torProbJ / (1 - wastedProbability);
                    addToPool(torPairProb, new ImmutablePair<>(tors.get(i), tors.get(j)));
                }

            }
        }

        System.out.println(" done.");

    }

    private void setPairProbabilitiesServerPairingsInFraction() {
        double activeFractionX = configuration.getDoublePropertyOrFail("traffic_probabilities_active_fraction");
        List<Integer> serverIds = configuration.getGraphDetails().getServerNodeIds().stream().collect(Collectors.toList());
        Random pairingsRandom = Simulator.selectIndependentRandom("server_parings_fraction");
        Collections.shuffle(serverIds, pairingsRandom);
        int numTors = configuration.getGraphDetails().getNumTors();

        double serverPairs = 0.0;
        // Go over every ToR pair
        List<org.apache.commons.lang3.tuple.Pair<Integer, Integer>> chosen = new ArrayList<>();
        while (serverIds.size() >= 2 ) {

            // Choose two at random
            int idxFirst = Math.abs(pairingsRandom.nextInt()) % serverIds.size();
            int first = serverIds.get(idxFirst);
            int idxSecond = Math.abs(pairingsRandom.nextInt()) % serverIds.size();
            int second = serverIds.get(idxSecond);
            while (first == second) {
                idxSecond = Math.abs(pairingsRandom.nextInt()) % serverIds.size();
                second = serverIds.get(idxSecond);
            }
            serverPairs += 2;
            chosen.add(new ImmutablePair<>(first, second));
            serverIds.remove(Integer.valueOf(first));
            serverIds.remove(Integer.valueOf(second));

        }
        double serverPairProb = 1.0/serverPairs;
        for(Pair<Integer,Integer> p: chosen){
            int svrA = p.getLeft();
            int svrB = p.getRight();
            addToPool(serverPairProb, new ImmutablePair<>(svrA, svrB));
            addToPool(serverPairProb, new ImmutablePair<>(svrB, svrA));
        }
        SimulationLogger.logInfo("PAIRINGS_FRACTION_CHOSEN_SERVERS", chosen.toString());
    }

    /**
     * Set the pair probabilities to be pairings within a fraction of the nodes.
     */
    private void setPairProbabilitiesPairingsInFraction() {


        // Retrieve necessary parameters from the extension
        int numTors = configuration.getGraphDetails().getNumTors();
        int serversPerNodeToExtendWith = configuration.getIntegerPropertyOrFail("scenario_topology_extend_servers_per_tl_node");
        boolean fractionIsOrdered = configuration.getBooleanPropertyOrFail("traffic_probabilities_active_fraction_is_ordered");
        double activeFractionX = configuration.getDoublePropertyOrFail("traffic_probabilities_active_fraction");

        // Shuffle nodes
        List<Integer> tors = new ArrayList<>();
        for (int i = 0; i < numTors; i++) {
            tors.add(i);
        }
        if (!fractionIsOrdered) {
            Collections.shuffle(tors, Simulator.selectIndependentRandom("all_to_all_fraction_shuffle"));
        }
        int numChosenTors = (int) Math.floor(numTors * activeFractionX);

        // Probability between each server pair
        //double serverPairProb = 1.0 / (Math.floor((double) numChosenTors / 2) * serversPerNodeToExtendWith * serversPerNodeToExtendWith * 2);

        System.out.print("Generating pairings pair probabilities in " + (activeFractionX * 100) + "% fraction " + tors.size() + " ToRs between their servers...");

        List<Integer> remaining = new ArrayList<>();
        for (int i = 0; i < numChosenTors; i++) {
            remaining.add(tors.get(i));
        }

        Random pairingsRandom = Simulator.selectIndependentRandom("pairings_fraction");
	double serverPairs = 0.0;
        // Go over every ToR pair
        List<org.apache.commons.lang3.tuple.Pair<Integer, Integer>> chosen = new ArrayList<>();
        while (remaining.size() >= 2 ) {

            // Choose two at random
            int idxFirst = Math.abs(pairingsRandom.nextInt()) % remaining.size();
            int first = remaining.get(idxFirst);
            int idxSecond = Math.abs(pairingsRandom.nextInt()) % remaining.size();
            int second = remaining.get(idxSecond);
            while (first == second) {
                idxSecond = Math.abs(pairingsRandom.nextInt()) % remaining.size();
                second = remaining.get(idxSecond);
            }

            // Add to random pair generator
            for (Integer svrA : configuration.getGraphDetails().getServersOfTor(first)) {
                for (Integer svrB : configuration.getGraphDetails().getServersOfTor(second)) {
		    if(svrA.equals(svrB)) continue;
                    serverPairs+=2;
		}
            }
            chosen.add(new ImmutablePair<>(first, second));

            // Remove ones already decided for
            remaining.remove(idxFirst);
            if (idxFirst < idxSecond) {
                idxSecond = idxSecond - 1;
            }
            remaining.remove(idxSecond);

        }
	double serverPairProb = 1.0/serverPairs;
        for(Pair<Integer,Integer> p: chosen){
		for (Integer svrA : configuration.getGraphDetails().getServersOfTor(p.getLeft())) {
                	for (Integer svrB : configuration.getGraphDetails().getServersOfTor(p.getRight())) {
                    		if(svrA.equals(svrB)) continue;
                    		addToPool(serverPairProb, new ImmutablePair<>(svrA, svrB));
	                   	addToPool(serverPairProb, new ImmutablePair<>(svrB, svrA));
        	        }
            	}
	}
	// Log chosen fraction
        Collections.sort(chosen);
        SimulationLogger.logInfo("PAIRING_NUM_CHOSEN_TORS", String.valueOf(numChosenTors));
        SimulationLogger.logInfo("PAIRINGS_FRACTION_CHOSEN_TORS", chosen.toString());

        System.out.println(" done.");

    }
    
    /**
     * Read in the communication pair probabilities from a file.
     *
     * File line structure:
     * # This is a comment line which is ignored
     * [tor_pair_id], [src], [dst], [pdf_num_bytes]
     *
     * @param fileName  File name of the pair probabilities file
     */
    private void readPairProbabilitiesFromFile(String fileName) {

        try {

            System.out.print("Reading in communication pair probabilities...");

            // Open file stream
            FileReader input = new FileReader(fileName);
            BufferedReader br = new BufferedReader(input);

            // Go over all lines
            String line;

            // Read in others
            double totalPdfSum = 0.0;
            while ((line = br.readLine()) != null) {

                // Remove trailing whitespace
                line = line.trim();

                // Skip empty lines
                if (line.equals("") || line.startsWith("#")) {
                    continue;
                }

                String[] spl = line.split(",");

                if (spl.length != 4) {
                    throw new RuntimeException("Communication probability line must have [tor_pair_id, src, dst, pdf_num_bytes]");
                }

                // Convert to correct format
                // int torPairId = Integer.valueOf(spl[0]);
                int src = Integer.valueOf(spl[1]);
                int dst = Integer.valueOf(spl[2]);
                double pdfNumBytes = Double.valueOf(spl[3]);

                // Add random pair generator
                addToPool(pdfNumBytes, new ImmutablePair<>(src, dst));

                // Add to total probability sum
                totalPdfSum += pdfNumBytes;

            }

            if (Math.abs(1.0 - totalPdfSum) > 1e-6) {
                throw new RuntimeException("Probability density did not sum up to approximately 1, but was " + totalPdfSum);
            }

            // Close file stream
            br.close();

            System.out.println(" done.");

        } catch (IOException e) {
            throw new RuntimeException("Graph: failed to read file: " + e.getMessage());
        }

    }

    @Override
    public void createPlan(long durationNs) {

        System.out.print("Creating arrival plan...");

        // Statistics tracking
        long time = 0;
        int x = 0;
        long sum = 0;

        // Generate flow start events until the duration
        // of the experiment has lapsed
        long nextProgressLog = durationNs / 10;
        while (time <= durationNs) {

            // Poisson arrival
            //
            // F(x) = 1 - e^(-lambda * x)
            // <=>
            // ln( 1 - F(x) ) = -lambda * x
            // <=>
            // x = -ln( 1 - F(x) ) / lambda
            // <=>
            // x = -ln( Uniform(x) ) / lambda
            //
            long interArrivalTime = (long) (-Math.log(ownIndependentRng.nextDouble()) / (lambdaFlowStartsPerSecond / 1e9));

            // Uniform arrival
            //
            // long interArrivalTime = (long) (1 / (lambdaFlowStartsPerSecond / 1e9));

            // Add to sum for later statistics
            sum += interArrivalTime;

            // Register flow
            Pair<Integer, Integer> pair = choosePair();
            registerFlow(time, pair.getLeft(), pair.getRight(), flowSizeDistribution.generateFlowSizeByte());

            // Advance time to next arrival
            time += interArrivalTime;
            x++;

            if (time > nextProgressLog) {
                System.out.print(" " + (100 * time / durationNs) + "%...");
                nextProgressLog += durationNs / 10;
            }

        }

        System.out.println(" done.");

        // Log plan created
        System.out.println("Poisson Arrival plan created.");
        System.out.println("Number of flows created: " + x + ".");
        System.out.println("Mean inter-arrival time: " + (sum / x) + " (expectation: "
                + (1 / (lambdaFlowStartsPerSecond / 1e9)) + ")");
        SimulationLogger.logInfo("Flow planner number flows", String.valueOf(x));
        SimulationLogger.logInfo("Flow planner mean inter-arrival time", String.valueOf((sum / x)));
        SimulationLogger.logInfo("Flow planner expected inter-arrival time", String.valueOf((1 / (lambdaFlowStartsPerSecond / 1e9))));
        SimulationLogger.logInfo("Flow planner poisson rate lambda (flow starts/s)", String.valueOf(lambdaFlowStartsPerSecond));

    }

    /**
     * Choose a random pair based on their probability density weight.
     *
     * @return (src, dst) pair
     */
    protected Pair<Integer, Integer> choosePair() {
        return this.randomPairGenerator.next();
    }

}
