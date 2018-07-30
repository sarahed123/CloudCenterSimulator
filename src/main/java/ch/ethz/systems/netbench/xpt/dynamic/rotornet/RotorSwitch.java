package ch.ethz.systems.netbench.xpt.dynamic.rotornet;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.*;
import ch.ethz.systems.netbench.xpt.dynamic.device.DynamicSwitch;

import java.util.Map;

public class RotorSwitch extends DynamicSwitch {

    /**
     * Constructor of a network device.
     *
     * @param identifier     Network device identifier
     * @param transportLayer Transport layer instance (null, if only router and not a server)
     * @param intermediary   Flowlet intermediary instance (takes care of flowlet support)
     * @param configuration
     */
    protected RotorSwitch(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary, configuration);
    }

    @Override
    public void receive(Packet genericPacket) {

    }

    @Override
    protected void receiveFromIntermediary(Packet genericPacket) {

    }

    Map<Integer, InputPort>  getInputPortmap(){
        return sourceIdToInputPort;
    }

    Map<Integer, OutputPort> getOutputportMap(){
        return targetIdToOutputPort;
    }

    void setInputPortMap(Map<Integer, InputPort> map){
        sourceIdToInputPort = map;
    }

    void setOutputportMap(Map<Integer, OutputPort> map){
        targetIdToOutputPort = map;
    }


}
