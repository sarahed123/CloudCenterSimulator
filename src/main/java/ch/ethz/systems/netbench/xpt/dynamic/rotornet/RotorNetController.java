package ch.ethz.systems.netbench.xpt.dynamic.rotornet;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.xpt.dynamic.controller.DynamicController;

import java.util.ArrayList;
import java.util.Map;

public class RotorNetController extends DynamicController {
    public static long sNextReconfigurationTime = 0;
    protected final long mReconfigurationInterval;
    protected final long mReconfigurationTime;
    int mNumCycles;
    int mCurrCycle;
    ArrayList<RotorMap> mRotorMaps;
    RotorSwitch[] mRotorsArray;
    public RotorNetController(Map<Integer, NetworkDevice> idToNetworkDevice, NBProperties configuration) {
        super(idToNetworkDevice, configuration);
        if(max_degree >= idToNetworkDevice.size()-1){
            throw new RuntimeException("max degree is bigger then network size minus 1");
        }
        if(idToNetworkDevice.size()%max_degree!=0){
            throw new RuntimeException("max degree must be devisable by network size");
        }
        mReconfigurationTime = configuration.getLongPropertyOrFail("rotor_net_reconfiguration_time_ns");
        mReconfigurationInterval = configuration.getLongPropertyOrFail("rotor_net_reconfiguration_interval_ns");
        RotorSwitch.setMaxBufferSizeByte(configuration.getLongPropertyOrFail("max_rotor_buffer_size_byte"));
        RotorMap.setRandom(Simulator.selectIndependentRandom("random_rotor_port"));
        mNumCycles = mIdToNetworkDevice.size()/max_degree;
        RotorMap.sNumOfNodes = idToNetworkDevice.size();
        mRotorMaps = new ArrayList<>();
        mCurrCycle = 0;
        mRotorsArray = (RotorSwitch[]) mIdToNetworkDevice.values().toArray(new RotorSwitch[mIdToNetworkDevice.size()]);
        setInitialConnections();
        registerReconfigurationEvent();

    }

    private void setInitialConnections() {
        RotorSwitch[] devices = mRotorsArray;
        for(int j = 0;j<mIdToNetworkDevice.size();j++){
            for(int i=0 ; i<max_degree; i+=1){
                RotorSwitch device = (RotorSwitch) devices[j];
                device.addConnection(devices[(j+mNumCycles*i+1)%mIdToNetworkDevice.size()].getIdentifier());
            }
            mRotorMaps.add(devices[j].getRotorMap());
        }
    }

    @Override
    public void initRoute(int source, int dest, long flowId) {
    	

        
    }

    public void reconfigureRotorSwitches(){
        RotorSwitch[] devices = mRotorsArray;
        mCurrCycle++;
        if(mCurrCycle==mNumCycles) {
            resetAllMaps();
            mCurrCycle = 0;
            return;
        }
        RotorMap tempMap = devices[0].getRotorMap();
        for(int i=0 ; i<devices.length-1; i+=1){
            RotorSwitch device = devices[i];
            RotorSwitch nextDevice = devices[(i+1)];
            device.setRotortMap(nextDevice.getRotorMap());
        }
        devices[devices.length-1].setRotortMap(tempMap);
    }

	private void resetAllMaps() {
		for(int i=0 ; i<mRotorMaps.size(); i+=1){
            RotorMap map = mRotorMaps.get(i);
            map.getOriginalDevice().setRotortMap(map);
		}
	}

	@Override
    public String toString(){
        String toShow = "";
        for(int i = 0; i< mRotorsArray.length;i++){
            toShow += mRotorsArray[i].getIdentifier() + " " + mRotorsArray[i].getRotorMap().toString() + "\n";
        }
        return toShow;
    }

    public RotorSwitch getDevice(int dest) {
        RotorSwitch device = mRotorsArray[dest];
        assert(device.getIdentifier() == dest);
        return device;
    }

    protected void startTransmmisions() {
        for(int i = 0; i< mRotorsArray.length;i++){
            mRotorsArray[i].sendPendingData();
        }
    }

    protected void registerReconfigurationEvent() {
        Simulator.registerEvent(new RotorReconfigurationEvent(mReconfigurationInterval,mReconfigurationTime));
        sNextReconfigurationTime = Simulator.getCurrentTime() + mReconfigurationInterval;

    }
}
