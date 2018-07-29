package ch.ethz.systems.netbench.xpt.dynamic.rotornet;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.InputPort;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.xpt.dynamic.controller.DynamicController;

import java.util.Map;

public class RotorNetController extends DynamicController {
    int mNumCycles;
    public RotorNetController(Map<Integer, NetworkDevice> idToNetworkDevice, NBProperties configuration) {
        super(idToNetworkDevice, configuration);
        if(max_degree >= idToNetworkDevice.size()-1){
            throw new RuntimeException("max degree is bigger then network size minus 1");
        }
        if(max_degree%idToNetworkDevice.size()!=0){
            throw new RuntimeException("max degree must be devisable by network size");
        }
        mNumCycles = mIdToNetworkDevice.size()/max_degree;
        setInitialConnections();
    }

    private void setInitialConnections() {
        NetworkDevice[] devices = (NetworkDevice[]) mIdToNetworkDevice.keySet().toArray();

        for(int i=0 ; i<max_degree; i+=1){
            for(int j = 0;j<mIdToNetworkDevice.size();j++){
                RotorSwitch device = (RotorSwitch) devices[j];
                device.addConnection(device,devices[(j+mNumCycles*i+1)%mIdToNetworkDevice.size()]);
            }
        }
    }

    @Override
    public void initRoute(int source, int dest, long flowId) {
        NetworkDevice[] devices = (NetworkDevice[]) mIdToNetworkDevice.keySet().toArray();
        for(int i=0 ; i<devices.length; i+=1){
            RotorSwitch device = (RotorSwitch) devices[i];
            Map<Integer, OutputPort> outputMap = device.getOutputportMap();
            Map<Integer, InputPort> inputMap = device.getInputPortmap();
            RotorSwitch nextDevice = (RotorSwitch) devices[(i+1) % devices.length];
            device.setOutputportMap(nextDevice.getOutputportMap());
            RotorSwitch prevDevice = (RotorSwitch) devices[(i-1)%devices.length];
            device.setInputPortMap(prevDevice.getInputPortmap());

        }
    }
}
