package ch.ethz.systems.netbench.xpt.dynamic.rotornet;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.InputPort;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.xpt.dynamic.controller.DynamicController;

import java.util.Map;

public class RotorNetController extends DynamicController {
    int mNumCycles;
    int mCurrCycle;
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
        mCurrCycle = 0;
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
    	
    	RotorSwitch[] devices = (RotorSwitch[]) mIdToNetworkDevice.values().toArray();
    	mCurrCycle++;
    	if(mCurrCycle==mNumCycles) {
    		resetAllMaps(devices);
    		mCurrCycle = 0;
    		return;
    	}
        Map<Integer, OutputPort> tempMap = devices[0].getOutputportMap();
        for(int i=0 ; i<devices.length-1; i+=1){
            RotorSwitch device = devices[i];
            RotorSwitch nextDevice = devices[(i+1)];
            device.setOutputportMap(nextDevice.getOutputportMap());
        }
        devices[devices.length-1].setOutputportMap(tempMap);
        
    }

	private void resetAllMaps(RotorSwitch[] devices) {
		for(int i=0 ; i<devices.length-1; i+=1){
            RotorSwitch device = devices[i];
            Map<Integer, OutputPort> map = device.getOutputportMap();
            RotorOutputPort first = (RotorOutputPort) map.values().toArray()[0];
            first.getOriginalDevice().setOutputportMap(map);
		}
	}
}
