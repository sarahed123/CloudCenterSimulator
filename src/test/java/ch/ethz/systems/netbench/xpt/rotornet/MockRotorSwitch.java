package ch.ethz.systems.netbench.xpt.rotornet;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.basic.PerfectSimpleLinkGenerator;
import ch.ethz.systems.netbench.xpt.dynamic.rotornet.RotorOutputPortGenerator;
import ch.ethz.systems.netbench.xpt.dynamic.rotornet.RotorSwitch;

public class MockRotorSwitch extends RotorSwitch {
    protected MockRotorSwitch(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary, configuration);
        mRotorMap = new MockRotorMap(new MockRotorOutputPortGenerator(configuration) , new PerfectSimpleLinkGenerator(configuration),this);
        mRotorMap.setCurrentDevice(this);
    }

    @Override
    public void receive(Packet p){

        MockRotorPacket rp = (MockRotorPacket) p;
        rp.traversed(this.getIdentifier());
        super.receive(p);
    }

    public void setBufferSize(int bufferSize) {
        this.mCurrentBufferSize = bufferSize;
    }

    public long getBufferSize() {
        return mCurrentBufferSize;
    }
}
