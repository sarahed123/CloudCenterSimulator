package ch.ethz.systems.netbench.xpt.megaswitch;

import ch.ethz.systems.netbench.ext.basic.IpHeader;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.TcpHeader;

public interface Encapsulatable extends IpHeader {

	public Encapsulatable encapsulate(int newDestination);
	public Encapsulatable deEncapsualte();
}
