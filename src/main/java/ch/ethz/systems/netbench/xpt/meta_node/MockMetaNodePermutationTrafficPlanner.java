package ch.ethz.systems.netbench.xpt.meta_node;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.poissontraffic.flowsize.FlowSizeDistribution;

public class MockMetaNodePermutationTrafficPlanner extends MetaNodePermutationTrafficPlanner {

    public MockMetaNodePermutationTrafficPlanner(Map<Integer, TransportLayer> idToTransportLayerMap,
            double lambdaFlowStartsPerSecond, FlowSizeDistribution flowSizeDistribution, NBProperties configuration) {
        super(idToTransportLayerMap, lambdaFlowStartsPerSecond, flowSizeDistribution, configuration);
        
    }

    @Override
    protected int getNumMetaNode() {
        return configuration.getIntegerPropertyOrFail("mock_meta_node_num");
    }

    @Override
    protected List<Integer> getServesPerMNList(int MNId){
        List<Integer> servers = new LinkedList<>();
        int ToRsPerMN = configuration.getGraphDetails().getNumTors() / getNumMetaNode();
        for(int i = MNId*ToRsPerMN; i<MNId*ToRsPerMN+ToRsPerMN; i++ ){
            servers.addAll(configuration.getGraphDetails().getServersOfTor(i));
        }
        
        return servers;
    }
    
}