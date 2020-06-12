package ch.ethz.systems.netbench.xpt.meta_node;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.routing.RoutingPopulator;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class MNController extends RoutingPopulator {
    Map<Integer, NetworkDevice> idToNetworkDevice;
    Map<Pair<Integer,Integer>, Long> loadMap;
    static MNController sInstance = null;
    private int metaNodeNum;
    private long linkSpeedBpns;
    private int metaNodeSize;
    private long initialTokenSizeKB;
    private long tokenTimeout;
    private MNController(NBProperties configuration, Map<Integer, NetworkDevice> idToNetworkDevice) {
        super(configuration);
        this.idToNetworkDevice = idToNetworkDevice;
        loadMap = new HashMap<>();
        metaNodeNum = configuration.getGraphDetails().getMetaNodeNum();
        if(metaNodeNum%idToNetworkDevice.size() != 0){
            throw new IllegalStateException("MetaNode num must perfectly divide network switch num");
        }
        metaNodeSize = idToNetworkDevice.size() / metaNodeNum;
        linkSpeedBpns = configuration.getLongPropertyOrFail("link_bandwidth_bit_per_ns");
        initialTokenSizeKB = configuration.getLongPropertyWithDefault("meta_node_default_token_size_kilo_bytes", 10);
        tokenTimeout = configuration.getLongPropertyWithDefault("meta_node_token_timeout_ns", 30000);
        initMetaNodes();
        initDevices();
    }

    private void initDevices() {
        for(NetworkDevice nd: idToNetworkDevice.values()){
            MetaNodeSwitch mnsw = (MetaNodeSwitch) nd;
            int MN = mnsw.getIdentifier()/metaNodeSize;
            mnsw.setMetaNodeId(MN);
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

    }

    public static MNController getInstance(NBProperties configuration, Map<Integer, NetworkDevice> idToNetworkDevice){
        if(sInstance == null){
            return new MNController(configuration,idToNetworkDevice);
        }
        throw new IllegalStateException("Controller already initialized");
    }

    public static MNController getInstance(){
        if(sInstance == null){
            throw new IllegalStateException("Controller not initialized");
        }
        return sInstance;
    }

    public MetaNodeToken getToken(int MNSource, int MNDest, long KBytes){
        Pair p = new ImmutablePair(MNSource,MNDest);
        long directBuffer = loadMap.get(p);
        long directTransferTime = calcTransferTimeNS(KBytes + directBuffer);
        long minTransferTime = directTransferTime;
        int dest = MNDest;
        for(int i=0; i<metaNodeNum; i++){
            if(i==MNDest) continue;
            Pair<Integer,Integer> firstHop = new ImmutablePair<>(MNSource,i);
            Pair<Integer,Integer> secondHop = new ImmutablePair<>(i,MNDest);
            long indirectTransferTime = calcTransferTimeNS(loadMap.get(firstHop) + loadMap.get(secondHop) + KBytes);
            if(indirectTransferTime<minTransferTime){
                minTransferTime = indirectTransferTime;
                dest = i;
            }
        }

        Pair<Integer,Integer> firstHop = new ImmutablePair<>(MNSource,dest);
        loadMap.put(firstHop,loadMap.get(firstHop)+KBytes);
        if(dest!=MNDest){
            Pair<Integer,Integer> secondHop = new ImmutablePair<>(dest,MNDest);
            loadMap.put(secondHop,loadMap.get(secondHop)+KBytes);
        }
        return new MetaNodeToken(KBytes, dest, tokenTimeout);
    }

    private long calcTransferTimeNS(long KBytes) {

        return (KBytes*1000000*8) / linkSpeedBpns;
    }


    public int getMetaNodeId(int identifier) {
        return identifier/metaNodeNum;
    }

    public MetaNodeToken getToken(int mnSource, int mnDest) {
        return getToken(mnSource,mnDest,initialTokenSizeKB);
    }
}
