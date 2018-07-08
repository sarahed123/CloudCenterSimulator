package ch.ethz.systems.netbench.core.network;

import ch.ethz.systems.netbench.core.Simulator;

public class InputPort extends Port {

	NetworkDevice sourceNetworkDevice;
	Link link;
	public InputPort(NetworkDevice ownNetworkDevice, NetworkDevice sourceNetworkDevice, Link link) {
		this.ownNetworkDevice = ownNetworkDevice;
		this.sourceNetworkDevice = sourceNetworkDevice;
		this.link = link;
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
		ownNetworkDevice.receive(packet);
	}
}
