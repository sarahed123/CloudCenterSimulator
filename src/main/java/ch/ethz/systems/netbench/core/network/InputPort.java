package ch.ethz.systems.netbench.core.network;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.ext.basic.IpPacket;

public class InputPort extends Port {
	int encapsulatingDeviceId;
	NetworkDevice sourceNetworkDevice;
	Link link;
	public InputPort(NetworkDevice ownNetworkDevice, NetworkDevice sourceNetworkDevice, Link link) {
		this.ownNetworkDevice = ownNetworkDevice;
		this.sourceNetworkDevice = sourceNetworkDevice;
		this.link = link;
		this.encapsulatingDeviceId = -1;
		if(ownNetworkDevice.getEncapsulatingDevice()!=null) {
			this.encapsulatingDeviceId = ownNetworkDevice.getEncapsulatingDevice().getAsNetworkDevice().identifier;
		}
	}
	
	public NetworkDevice getOwnNetworkDevice() {
		return this.ownNetworkDevice;
	}
	
	public NetworkDevice getSourceNetworkDevice() {
		return this.sourceNetworkDevice;
	}

    public void registerPacketArrivalEvent(Packet packet) {
		Simulator.registerEvent(
				new PacketArrivalEvent(
						link.getDelayNs(),
						packet,
						this
                )
        );
    }



	public void receive(Packet packet) {
		try{
			if(((IpPacket)packet).getDestinationId()==encapsulatingDeviceId) {
				ownNetworkDevice.passToEncapsulatingDevice(packet);
				return;
			}
		}catch (ClassCastException e){

		}

		ownNetworkDevice.receive(packet);
	}
}
