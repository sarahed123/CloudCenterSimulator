package ch.ethz.systems.netbench.xpt.meta_node;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.routing.RoutingPopulator;
import ch.ethz.systems.netbench.ext.ecmp.EcmpRoutingUtility;
import edu.asu.emit.algorithm.graph.Vertex;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MNController extends RoutingPopulator {
    protected Map<Integer, NetworkDevice> idToNetworkDevice;
    protected Map<Pair<Integer,Integer>, Long> loadMap;
    static MNController sInstance = null;
    private int metaNodeNum;
    private long linkSpeedBpns;
    private int metaNodeSize;
    private long initialTokenSizeBytes;
    private long tokenTimeout;
    private int matchingsNum;
    private Random rand;
    protected MNController(NBProperties configuration, Map<Integer, NetworkDevice> idToNetworkDevice) {
        super(configuration);
        this.idToNetworkDevice = idToNetworkDevice;
        loadMap = new HashMap<>();
        int ToRnum = configuration.getGraphDetails().getNumTors();
        metaNodeNum = configuration.getGraphDetails().getMetaNodeNum();

        if(ToRnum%metaNodeNum != 0){
            throw new IllegalStateException("MetaNode num must perfectly divide network switch num");
        }
        metaNodeSize = ToRnum / metaNodeNum;
        linkSpeedBpns = configuration.getLongPropertyOrFail("link_bandwidth_bit_per_ns");
        initialTokenSizeBytes = configuration.getLongPropertyWithDefault("meta_node_default_token_size_bytes", 15000);
        tokenTimeout = configuration.getLongPropertyWithDefault("meta_node_token_timeout_ns", 30000);
        rand = Simulator.selectIndependentRandom("mn_switch_randomizer");
        initMetaNodes();
        initDevices();
        matchingsNum = calcMatchingNum();

    }

    /**
     * relies on perfect symmetry
     * @return
     */
    private int calcMatchingNum(){
       List<Vertex> vertices = configuration.getGraph().getAdjacentVertices(new Vertex(0));
       int ToRVertices = 0;
       for(Vertex u: vertices){
           if(!idToNetworkDevice.get(u.getId()).isServer()){
                ToRVertices++;
           }
       }
        SimulationLogger.logInfo("META_NODE_MATCHING_NUM", Integer.toString(matchingsNum));
       return ToRVertices/(metaNodeNum-1);
    }

    private void initDevices() {

        for(int i=0; i<configuration.getGraphDetails().getNumTors(); i++)   {
            MetaNodeSwitch mnsw = (MetaNodeSwitch) idToNetworkDevice.get(i);
            int MN = mnsw.getIdentifier()/metaNodeSize;
            mnsw.setMetaNodeId(MN);
            mnsw.setRandomizer(this.rand);
        }
    }

    private void initMetaNodes() {
        for(int i = 0; i< metaNodeNum; i++){
            for(int j = 0; j< metaNodeNum; j++){
                if(i==j) continue;

                loadMap.put(new ImmutablePair<>(i,j), 0l);
            }
        }
    }

    @Override
    public void populateRoutingTables() {
        EcmpRoutingUtility.populateShortestPathRoutingTables(this.idToNetworkDevice, true, this.configuration);
    }

    public static MNController getInstance(NBProperties configuration, Map<Integer, NetworkDevice> idToNetworkDevice){
        if(sInstance == null){
            sInstance = new MNController(configuration,idToNetworkDevice);
            return sInstance;
        }
        throw new IllegalStateException("Controller already initialized");
    }

    public static MNController getInstance(){
        if(sInstance == null){
            throw new IllegalStateException("Controller not initialized");
        }
        return sInstance;
    }

    private long calcTransferTimeNS(int MNSource, int MNDest, long bytes){
        assert MNSource!=MNDest;
        Pair p = new ImmutablePair(MNSource,MNDest);
        long directBuffer = loadMap.get(p);
        long directTransferTime = calcTransferTimeNS(bytes + directBuffer);
        long transferTime = directTransferTime;
        return transferTime;
    }

    private long calcTransferTimeNS(int MNSource, int middleHop, int MNDest, long bytes){
        return calcTransferTimeNS(MNSource, middleHop, bytes) + calcTransferTimeNS(middleHop,MNDest,bytes);
    }

    protected int getNextMNDest(int MNSource, int MNDest, long bytes){

        long minTransferTime = calcTransferTimeNS(MNSource,MNDest,bytes);
        int dest = MNDest;
        for(int i=0; i<metaNodeNum; i++){
            if(i==MNDest || i==MNSource) continue;
            long indirectTransferTime = calcTransferTimeNS(MNSource, i, MNDest, bytes);
            if(indirectTransferTime<minTransferTime){
                minTransferTime = indirectTransferTime;
                dest = i;
            }
        }
        return dest;
    }

    public MetaNodeToken getToken(int MNSource, int MNDest, long bytes){
        int dest = getNextMNDest(MNSource, MNDest, bytes);
        return getToken(MNSource, MNDest, dest, bytes);

    }

    private MetaNodeToken getToken(int MNSource, int MNDest, int middleHop, long bytes) {
        Pair<Integer,Integer> firstHop = new ImmutablePair<>(MNSource,middleHop);
        loadMap.put(firstHop,loadMap.get(firstHop)+bytes);
        if(middleHop!=MNDest){
            Pair<Integer,Integer> secondHop = new ImmutablePair<>(middleHop,MNDest);
            loadMap.put(secondHop,loadMap.get(secondHop)+bytes);
        }
        return new MetaNodeToken(bytes, middleHop, tokenTimeout);
    }

    private long calcTransferTimeNS(long bytes) {

        return ((bytes*8) / linkSpeedBpns)/matchingsNum;
    }


    public int getMetaNodeId(int identifier) {
        return identifier/metaNodeNum;
    }

    public MetaNodeToken getToken(int MNSource, int MNDest) {
        return getToken(MNSource,MNDest,initialTokenSizeBytes);
    }

    public int getMatchingsNum(){
        return this.matchingsNum;
    }
}
