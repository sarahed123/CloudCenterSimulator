package ch.ethz.systems.netbench.xpt.dynamic.controller;

import ch.ethz.systems.netbench.core.network.NetworkDevice;

public interface DynamicDevice {

	public void addConnection(NetworkDevice  source,NetworkDevice  dest, int serverSource, int serverDest);
	
	public void removeConnection(int serverSource, int serverDest);
	

}
