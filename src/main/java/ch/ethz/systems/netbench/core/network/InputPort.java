package ch.ethz.systems.netbench.core.network;

public class InputPort {

	NetworkDevice ownNetworkDevice;
	NetworkDevice sourceNetworkDevice;
	Link link;
	public InputPort(NetworkDevice ownNetworkDevice, NetworkDevice sourceNetworkDevice) {
		this.ownNetworkDevice = ownNetworkDevice;
		this.sourceNetworkDevice = sourceNetworkDevice;
	}
	
	public NetworkDevice getOwnNetworkDevice() {
		return this.ownNetworkDevice;
	}
	
	public NetworkDevice getSourceNetworkDevice() {
		return this.sourceNetworkDevice;
	}

}
