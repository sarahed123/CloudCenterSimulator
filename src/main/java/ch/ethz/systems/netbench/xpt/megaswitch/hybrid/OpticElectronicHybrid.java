package ch.ethz.systems.netbench.xpt.megaswitch.hybrid;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.*;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.Encapsulatable;
import ch.ethz.systems.netbench.xpt.megaswitch.MegaSwitch;

import java.util.HashMap;

public class OpticElectronicHybrid extends NetworkDevice implements MegaSwitch {

    protected long circuitThreshold;
    protected NetworkDevice electronic;
    protected NetworkDevice optic;
    public OpticElectronicHybrid(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary,configuration);
        circuitThreshold = configuration.getLongPropertyOrFail("hybrid_circuit_threshold");

    }
    @Override
    public void receive(Packet genericPacket) {
        Encapsulatable packet = (Encapsulatable) genericPacket;

        int destinationToR = configuration.getGraphDetails().getTorIdOfServer(packet.getDestinationId());

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
    public void receiveFromEncapsulatedDevice(Packet packet) {
        Encapsulatable ipPacket = (Encapsulatable) packet;
        if (ipPacket.getDestinationId() == this.identifier) {
            IpPacket deEncapse = ipPacket.deEncapsualte();
            targetIdToOutputPort.get(deEncapse.getDestinationId()).enqueue(deEncapse);
        }
    }

    @Override
    public NetworkDevice getAsNetworkDevice() {
        return this;
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

	@Override
    public InputPort getSourceInputPort(int sourceNetworkDeviceId, String technology){
        if(technology == null) {
            return this.getSourceInputPort(sourceNetworkDeviceId);
        }
        switch (technology){
            case "optic":
                return optic.getSourceInputPort(sourceNetworkDeviceId);
            case "electronic":
                return electronic.getSourceInputPort(sourceNetworkDeviceId);
            default:
                throw new RuntimeException("bad technology " + technology);
        }
    }
}
