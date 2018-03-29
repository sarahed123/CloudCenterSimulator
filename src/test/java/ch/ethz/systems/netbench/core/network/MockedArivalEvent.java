package ch.ethz.systems.netbench.core.network;

import ch.ethz.systems.netbench.core.run.infrastructure.BaseInitializer;

public class MockedArivalEvent extends PacketArrivalEvent {
	NetworkDevice arrivalNetworkDevice;
	MockedArivalEvent(long timeFromNowNs, Packet packet, NetworkDevice arrivalNetworkDevice) {
		super(timeFromNowNs, packet, arrivalNetworkDevice);
		this.arrivalNetworkDevice = arrivalNetworkDevice;
	}
	
	 protected NetworkDevice getNetworkDevice() {
		return arrivalNetworkDevice;
    	
    }

}
