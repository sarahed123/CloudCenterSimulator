package ch.ethz.systems.netbench.xpt.megaswitch;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.*;

import java.util.ArrayList;

public interface MegaSwitch{
    public void extend(NetworkDevice networkDevice, NBProperties conf);
    public void receiveFromEncapsulatedDevice(Packet packet);
    public void onFlowFinished(int sourceToRId, int destToRId,int sourceServer,int destServerId, long flowId);
    public NetworkDevice getAsNetworkDevice();
    public NetworkDevice getEncapsulatedDevice(String type);
}
