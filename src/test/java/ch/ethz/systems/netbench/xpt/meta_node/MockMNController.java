package ch.ethz.systems.netbench.xpt.meta_node;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

public class MockMNController extends MNController {
    static MockMNController sInstance = null;

    protected MockMNController(NBProperties configuration, Map<Integer, NetworkDevice> idToNetworkDevice) {
        super(configuration, idToNetworkDevice);
    }

    public static MockMNController getInstance(NBProperties configuration, Map<Integer, NetworkDevice> idToNetworkDevice){
        sInstance = new MockMNController(configuration,idToNetworkDevice);
        return sInstance;
    }

    public static MockMNController getInstance(){
        if(sInstance == null){
            throw new IllegalStateException("Controller not initialized");
        }
        return sInstance;
    }


    public Map<Pair<Integer,Integer>, Long> getLoadMap(){
        return loadMap;
    }

    public MetaNodeSwitch getDevice(int id){
        return (MetaNodeSwitch) idToNetworkDevice.get(id);
    }
}
