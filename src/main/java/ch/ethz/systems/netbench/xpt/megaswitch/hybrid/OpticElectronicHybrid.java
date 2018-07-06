package ch.ethz.systems.netbench.xpt.megaswitch.hybrid;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.MegaSwitch;

public class OpticElectronicHybrid extends MegaSwitch {

    protected NetworkDevice electronic;
    protected NetworkDevice optic;
    public OpticElectronicHybrid(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary,configuration);

    }
    @Override
    public void receive(Packet genericPacket) {
        IpPacket packet = (IpPacket) genericPacket;
        int destinationToR = configuration.getGraphDetails().getTorIdOfServer(packet.getDestinationId());
        if (destinationToR == this.identifier) {
            targetIdToOutputPort.get(packet.getDestinationId()).enqueue(packet);
            return;
        }
        this.optic.initCircuit(this.identifier,destinationToR,packet.getFlowId());
        this.optic.receive(genericPacket);
    }

    @Override
    protected void receiveFromIntermediary(Packet genericPacket) {
        throw new RuntimeException("Hybrid switch is not a server");
    }

    @Override
    public void extend(NetworkDevice networkDevice, NBProperties networkConf){
        String networkType = networkConf.getPropertyOrFail("network_type");
        switch (networkType){
            case "optic":
                this.optic = networkDevice;
                this.optic.setEncapsulatingDevice(this);
                break;
            case "electronic":
                this.electronic = networkDevice;
                this.electronic.setEncapsulatingDevice(this);
                break;
            default:
                throw new RuntimeException("bad network type " + networkType);
        }
    }

    @Override
    public OutputPort getTargetOuputPort(int targetId, String technology) {
		if(technology == null) {
			 return this.getTargetOuputPort(targetId);
		}
    	switch (technology){
    	case "optic":
            return optic.getTargetOuputPort(targetId);
        case "electronic":
        	return electronic.getTargetOuputPort(targetId);
        default:
            throw new RuntimeException("bad technology " + technology);
    	}
	}
}
