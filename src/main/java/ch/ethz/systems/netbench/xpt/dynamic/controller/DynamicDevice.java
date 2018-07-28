package ch.ethz.systems.netbench.xpt.dynamic.controller;

import ch.ethz.systems.netbench.core.network.NetworkDevice;

public interface DynamicDevice {

	public void addConnection(NetworkDevice  source,NetworkDevice  dest);
	
	public void removeConnection(NetworkDevice  source,NetworkDevice dest);
	

}
