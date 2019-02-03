package ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Event;

public class TeardownEvent extends Event {
	ReservationPacket mPacket;
	DistributedOpticServer mDevice;
	boolean active;
	private long mResetTime;
	private boolean finished;
	public TeardownEvent(long timeFromNowNs,ReservationPacket rp, DistributedOpticServer distributedOpticServer) {
		super(timeFromNowNs);
		mPacket = rp;
		mDevice = distributedOpticServer;
		active = true;
		mResetTime = Long.MAX_VALUE;
		finished = false;
	}

	@Override
	public void trigger() {
		if(active) {
			mDevice.teardownCircuit(mPacket);

			SimulationLogger.increaseStatisticCounter("AUTO_CIRCUIT_TEARDOWN_COUNT");
			finished = true;
		}
			

	}
	
	public void reset(long timeFromNow) {
		active = false;
		mResetTime = timeFromNow;
		
	}
	
	public void finish() {
		active = false;
		finished = true;
	}


	@Override
	public boolean retrigger() {
		if(!finished) {
			active = true;
			time = Simulator.getTimeFromNow(mResetTime);
			return true;
		}
		return false;
	}
}
