package ch.ethz.systems.netbench.xpt.dynamic.rotornet;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.run.infrastructure.LinkGenerator;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.ext.basic.PerfectSimpleLink;
import ch.ethz.systems.netbench.ext.basic.PerfectSimpleLinkGenerator;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import java.lang.Integer;
import java.util.*;

public class RotorMap extends LinkedList<Integer> {
    LinkGenerator mLinkGeneraor;
    RotorSwitch  mCurrentDevice;
    RotorOutputPortGenerator mOutputPortGenerator;
    HashMap<Integer, RotorOutputPort> mOutputPortMap;
    RotorSwitch mOriginalDevice;
    static Random mRnd;
    public RotorMap(RotorOutputPortGenerator rotorOutputPortGenerator, LinkGenerator linkGenerator, RotorSwitch rotorSwitch){
        mOutputPortGenerator = rotorOutputPortGenerator;
        mLinkGeneraor = linkGenerator;
        mOutputPortMap = new HashMap<>();
        mOriginalDevice = rotorSwitch;
    }

    static void setRandom(Random rnd){
        mRnd = rnd;
    }

    public void setCurrentDevice(RotorSwitch rs){
        mCurrentDevice = rs;

    }

    public RotorOutputPort getOutpurPort(int dest){
        if(!this.contains(dest)){
            throw new NoPathException();
        }
        RotorOutputPort port = mOutputPortMap.get(dest);
        if(port == null) {
            RotorNetController controller = (RotorNetController) RemoteRoutingController.getInstance();
            RotorSwitch rs = controller.getDevice(dest);
            port = (RotorOutputPort) mOutputPortGenerator.generate(mCurrentDevice, rs, mLinkGeneraor.generate(mCurrentDevice, rs));
            port.setRotorMap(this);
            mOutputPortMap.put(dest,port);
        }
        return port;
    }

    public RotorOutputPort getRandomOutputPort(){
        return (RotorOutputPort) this.toArray()[mRnd.nextInt(this.size())];
    }


    void put(int dest){
        this.add(dest);
    }


    public RotorSwitch getOriginalDevice() {
        return mOriginalDevice;
    }

    public void clearOutputPorts() {
        mOutputPortMap.clear();
    }
}
