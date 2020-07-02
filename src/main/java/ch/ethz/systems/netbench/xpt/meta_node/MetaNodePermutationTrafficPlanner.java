package ch.ethz.systems.netbench.xpt.meta_node;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.traffic.TrafficPlanner;
import ch.ethz.systems.netbench.ext.poissontraffic.PoissonArrivalPlanner;
import ch.ethz.systems.netbench.ext.poissontraffic.flowsize.FlowSizeDistribution;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MetaNodePermutationTrafficPlanner extends PoissonArrivalPlanner {

    public MetaNodePermutationTrafficPlanner(Map<Integer, TransportLayer> idToTransportLayerMap, double lambdaFlowStartsPerSecond, FlowSizeDistribution flowSizeDistribution, NBProperties configuration) {
        super(idToTransportLayerMap, lambdaFlowStartsPerSecond, flowSizeDistribution, configuration);
        this.configuration = configuration;
        this.setTrafficPorbabilities();
    }

    private void setTrafficPorbabilities() {
        System.out.print("Generating permutation traffic between meta nodes");
        int numMetaNodes = getNumMetaNode();
        int pairNum = configuration.getIntegerPropertyWithDefault("num_pairs_in_meta_node_permutation", numMetaNodes/2);

        List<Integer> MNs = new LinkedList<>();

        for(int i = 0; i < numMetaNodes; i++){
            MNs.add(i);
        }
        Collections.shuffle(MNs, Simulator.selectIndependentRandom("meta_node_permutation_shuffle"));

        int p = 0;
        double serverPairNum = 0.0;
        while(p<pairNum){
            List<Integer> serversA = getServesPerMNList(2*p);
            List<Integer> serversB = getServesPerMNList(2*p+1);
            for(int serverA: serversA){
                for(int serverB: serversB){
                    serverPairNum+=2d;
                }
            }
            p++;
        }
        double serverPairProb = 1d / serverPairNum;


        p = 0;
        while(p<pairNum){
            List<Integer> serversA = getServesPerMNList(2*p);
            List<Integer> serversB = getServesPerMNList(2*p+1);
            for(int serverA: serversA){
                for(int serverB: serversB){
                    addToPool(serverPairProb, new ImmutablePair<Integer,Integer>(serverA,serverB));
                    addToPool(serverPairProb, new ImmutablePair<Integer,Integer>(serverB,serverA));

                }
            }
            p++;
        }
        System.out.println(" done.");
    }

    protected int getNumMetaNode() {
        return configuration.getGraphDetails().getMetaNodeNum();
    }

    List<Integer> getServesPerMNList(int MNId){
        int numToRs = configuration.getGraphDetails().getNumTors();
        List<Integer> servers = new LinkedList<>();
        int serversPerMN = getNumServersPerMN();
        for(int i=0; i<serversPerMN; i++){
            servers.add(numToRs + MNId*serversPerMN+ i);
        }
        return servers;
    }

    protected int getNumServersPerMN(){
        return configuration.getGraphDetails().getNumServers()/getNumMetaNode();
    }



}
