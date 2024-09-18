package ch.ethz.systems.netbench.core;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Event;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.PacketArrivalEvent;
import ch.ethz.systems.netbench.core.network.PacketDispatchedEvent;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.random.RandomManager;
import ch.ethz.systems.netbench.core.run.infrastructure.BaseInitializer;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.core.run.traffic.FlowStartEvent;
import ch.ethz.systems.netbench.core.state.SimulatorStateSaver;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.simple.TcpPacketResendEvent;
import ch.ethz.systems.netbench.xpt.tcpbase.FullExtTcpPacket;
import ch.ethz.systems.netbench.xpt.xpander.XpanderRouter;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * The simulator is responsible for offering general
 * services to all other interacting parts, such as
 * configurations and current time management. It uses
 * a deterministic priority queue to precisely execute
 * event after event.
 */
public class Simulator {
	// Time interval at which to show the percentage of progress
	private static long PROGRESS_SHOW_INTERVAL_NS = 10000000L; // 0.01s = 10mss

	// Main ordered event queue (run variable)
	private static PriorityQueue<Event> eventQueue = new PriorityQueue<>();
	// Event queues map according to servers(source_id)
	final static int NUM_SERVER = 8;

	private static PriorityQueue<Event>[] queuesServer = new PriorityQueue[NUM_SERVER];

	private static final Object lockThreshold = new Object();
	private static final Object lockNow = new Object();
	private static final Object lockBarrier = new Object();
	private static final Object lockRemainThread = new Object();
	private static AtomicInteger activeThreads = new AtomicInteger(NUM_SERVER);
	private static volatile CyclicBarrier barrier = new CyclicBarrier(NUM_SERVER);
	

	// Current time in ns in the simulation (run variable)
	private static long now;
	private static boolean endedDueToFlowThreshold;
	private static final long offsetTime = 200000;
	

	// Threshold to end
	private static long finishFlowIdThreshold;
	private static final Set<Long> finishedFlows = new HashSet<>();

	// Whether the simulator is setup
	private static boolean isSetup = false;

	private static long totalRuntimeNs;
	// Randomness manager
	private static RandomManager randomManager;

	// Configuration
	private static NBProperties configuration;

	private static int currentCommand;

	private Simulator() {
		// Static class only
	}

	private static Comparator<Event> eventComparator = new Comparator<Event>() {
		@Override
		public int compare(Event e1, Event e2) {
			int timeCompare = Long.compare(e1.getTime(), e2.getTime());
			if (timeCompare != 0) {
				return timeCompare;
			}
			return Long.compare(e1.getEid(), e2.getEid());
		}
    };
    
	
       

	/**
	 * Retrieve the configuration.
	 *
	 * This contains two categories.
	 *
	 * (A) Solely data about a specific run,
	 * e.g. topology, run time, link type,
	 * protocol, etc.
	 *
	 * (B) Settings for the internal workings, e.g. TCP retransmission
	 * time-out, flowlet gap, and model parameters in general.
	 *
	 * @return Run configuration properties
	 */
	public static NBProperties getConfiguration() {
		return configuration;
	}

	/**
	 * Setup the simulator with only a seed, leaving out
	 * a run configuration specification.
	 *
	 * @param seed Random seed (set 0 for random)
	 */
	public static void setup(long seed) {
		setup(seed, null);
	}

	/**
	 * Setup the simulator by giving random seed.
	 * Completely resets the simulation by clearing the event queue
	 * and resetting the simulation epoch to zero.
	 * Also, loads in the default internal configuration.
	 *
	 * @param seed          Random seed (set 0 for random)
	 * @param configuration Configuration instance (set null if there is no
	 *                      configuration (an empty one), e.g. in tests)
	 */
	public static void setup(long seed, NBProperties configuration) {

		// Prevent double setup
		if (isSetup) {
			throw new RuntimeException(
					"The simulator can only be setup once. Call reset() before setting it up again.");
		}

		// Open simulation logger
		SimulationLogger.open(configuration);

		// Setup random seed
		if (seed == 0) {
			seed = new Random().nextLong();
			SimulationLogger.logInfo("Seed randomly chosen", "TRUE");
		} else {
			SimulationLogger.logInfo("Seed randomly chosen", "FALSE");
		}
		SimulationLogger.logInfo("Seed", String.valueOf(seed));
		randomManager = new RandomManager(seed);

		// Internal state reset
		now = 0;
		eventQueue.clear();

		// Initialize the queues of servers
		for (int i = 0; i < NUM_SERVER; i++) {
			queuesServer[i] = new PriorityQueue<Event>(eventComparator);
		}

		// Configuration
		Simulator.configuration = configuration;

		restoreState();

		// It is now officially setup
		isSetup = true;

	}

	private static void restoreState() {
		if (configuration == null) {
			// this can happen in tests.
			return;
		}
		if (configuration.getPropertyWithDefault("from_state", null) != null) {
			String folderName = configuration.getPropertyWithDefault("from_state", null);
			JSONObject json = SimulatorStateSaver.loadJson(folderName + "/" + "simulator_data.json");
			now = (long) json.get("now");
			eventQueue = (PriorityQueue<Event>) SimulatorStateSaver
					.readObjectFromFile(folderName + "/" + "simulator_queue.ser");
			TransportLayer.restorState(configuration);
			System.out.println("Done restoring simulator");
		}

	}

	/**
	 * Create a random number generator which guarantees the same sequence
	 * when the same universal seed is fed in <i>setup()</i>.
	 *
	 * This is used when a section wants to be independent of whatever configuration
	 * has come before it that may have used the universal random number generator.
	 * An example is the flow generation.
	 *
	 * @param name Name of the independent random number generator
	 *
	 * @return Independent random number generator
	 */
	public static Random selectIndependentRandom(String name) {
		return randomManager.getRandom(name);
	}

	/**
	 * Run the simulator for the specified amount of time.
	 *
	 * @param runtimeNanoseconds Running time in ns
	 */
	public static void runNs(long runtimeNanoseconds) {
		runNs(runtimeNanoseconds, -1);
	}

	public static long getTotalRunTimeNs() {
		return totalRuntimeNs;
	}

	/**
	 * Run the simulator for at most the specified amount of time, or
	 * until the first N flows have been finished.
	 *
	 * @param runtimeNanoseconds     Running time in ns
	 * @param flowsFromStartToFinish Number of flows from start to finish (e.g.
	 *                               40000 will make the simulation
	 *                               run until all flows with identifier < 40000 to
	 *                               finish, or until the runtime
	 *                               is exceeded)
	 */
	public static void runNs(long runtimeNanoseconds, long flowsFromStartToFinish) {

		// Reset run variables (queue is not cleared because it has to start somewhere,
		// e.g. flow start events)
		// now = 0;
		totalRuntimeNs = runtimeNanoseconds;

		// Finish flow threshold, if it is negative the flow finish will be very far in
		// the future
		finishFlowIdThreshold = flowsFromStartToFinish;
		if (flowsFromStartToFinish <= 0) {
			flowsFromStartToFinish = Long.MAX_VALUE;
		}

		final long flowsFromStartToFinishFinal = flowsFromStartToFinish;
		PROGRESS_SHOW_INTERVAL_NS = runtimeNanoseconds / 50l;

		// Log start
		System.out.println("Starting simulation (total time: " + runtimeNanoseconds + "ns);...");

		// Time loop
		long startTime = System.currentTimeMillis();

		endedDueToFlowThreshold = false;

		ExecutorService executor = Executors.newFixedThreadPool(NUM_SERVER);

		now = Long.MAX_VALUE;
		for (int i = 0; i < NUM_SERVER; i++) {
			if (!queuesServer[i].isEmpty() && queuesServer[i].peek().getTime() <= totalRuntimeNs) {
				now = Math.min(now, queuesServer[i].peek().getTime());
			}
		}
		
        
       
		// for (int i = 0; i < NUM_SERVER; i++) {
		// 	System.out.println("Queue number " + i);
		// 	while(!queuesServer[i].isEmpty()){
		// 		System.out.println("event: " + queuesServer[i].peek().getTime());
		// 		queuesServer[i].poll();
				
		// 	}
		// }

		for (int i = 0; i < NUM_SERVER; i++) {
			final int numServer = i; // Capturing the row index for each thread
			// check how many threads run(have events to trigger)
			// if (!queuesServer[numServer].isEmpty()) {
			// 	numThreadRun++;
			// }
			
			executor.submit(() -> runThread(queuesServer[numServer], flowsFromStartToFinishFinal, barrier,Thread.currentThread().getId(),numServer)); 
           
		}

		// wait for all the threads
		executor.shutdown();
		try {
			// Wait indefinitely until all tasks have completed execution
			// executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Make sure run ends at the final time if it ended because there were no
		// more events or the runtime was exceeded
		if (!endedDueToFlowThreshold) {
			now = runtimeNanoseconds;
		}

		// Log end
		System.out.println("Simulation finished (simulated " + (runtimeNanoseconds / 1e9) + "s in a real-world time of "
				+ ((System.currentTimeMillis() - startTime) / 1000.0) + "s).");

	}

	private static void runThread(PriorityQueue<Event> queueServer, long flowsFromStartToFinish,CyclicBarrier barrier,long threadID,int numServer) {
		long realTime = System.currentTimeMillis();
		long nextProgressLog = PROGRESS_SHOW_INTERVAL_NS;
		long nowThread = 0;
		Event event;
		synchronized (System.out) {
			System.out.println("Thread ID: " + threadID + " is processing server " + numServer + " start run in time: " + realTime + " ,size: " + queueServer.size());
		}
		while (!queueServer.isEmpty()) {
			synchronized (System.out) {
				System.out.println("---Thread ID " + threadID + ", the window betweeen " + now + " to " + (now+offsetTime)+" ,size: "+queueServer.size() + "---");
			}
			while (!queueServer.isEmpty() && (nowThread = queueServer.peek().getTime()) <= (now+offsetTime)) {
				event = queueServer.peek();
				synchronized (System.out) {
					System.out.println("Thread ID " + threadID + " ,Event peeked: " + event.getEid() + " ,Event Type: " + event.getClass().getSimpleName() + " ,time: " + event.getTime());
			    }
				if (nowThread <= totalRuntimeNs) {
					queueServer.poll();
					event.trigger();
					if (event.retrigger()) {
						registerEvent(event);
					}
				} else {
					break;
				}

				// Log elapsed time
				if (nowThread > nextProgressLog) {
					nextProgressLog += PROGRESS_SHOW_INTERVAL_NS;
					long realTimeNow = System.currentTimeMillis();
					System.out.println("Elapsed " + (double) PROGRESS_SHOW_INTERVAL_NS / (double) 1000000000
							+ "s simulation in " + ((realTimeNow - realTime) / 1000.0) + "s real (total progress: "
							+ ((((double) nowThread) / ((double) totalRuntimeNs)) * 100) + "%).");
					realTime = realTimeNow;

					if (RemoteRoutingController.getInstance() != null) {
						System.out.print(RemoteRoutingController.getInstance().getCurrentState());
					}
				}

				if (finishedFlows.size() >= flowsFromStartToFinish) {
					synchronized (lockThreshold) {
                        endedDueToFlowThreshold = true;
                    }
					// break;
				}

			}


			if (nowThread > totalRuntimeNs || queueServer.isEmpty()) {
				synchronized(lockRemainThread){
					int remainingThreads = activeThreads.decrementAndGet();
					System.out.println("Thread" + threadID + "completed. Remaining threads: " + remainingThreads);
					if(remainingThreads > 0){
						reinitializeBarrier(remainingThreads);
					}
					break; 
				}

			}

			try {
                // Wait for all threads to reach this point
                int index = barrier.await();
                if (index == 0) {
                    // Only one thread (the last one to arrive) will execute this
                    synchronized (lockNow) {
						long min = checkMinEventTime();
						now = (min/offsetTime) * offsetTime;
						// now = min;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
		}

	}
	/* check min time event of all head queues*/
	private static long checkMinEventTime() {
		long min = queuesServer[0].peek().getTime();
		long minCheck;
        for (int i = 1; i < NUM_SERVER; i++) {
			if((minCheck = queuesServer[i].peek().getTime()) < min){
				min = minCheck;
			}
		}
		return min;
		
    }
    
	private static void reinitializeBarrier(int remainingThreads) {
        synchronized (lockBarrier) {
            if (remainingThreads > 0) {
                barrier.reset(); 
                barrier = new CyclicBarrier(remainingThreads); 
            }
        }
    }

	
	/**
     * Register an event in the simulation.
     *
     * @param event Event instance
     */
    public static void registerEvent(Event event) {
        eventQueue.add(event);
        long flowId;
        if (event instanceof FlowStartEvent) {
            FlowStartEvent flowStartEvent = (FlowStartEvent) event;
            int targetId = flowStartEvent.getTargetId();
            queuesServer[targetId % NUM_SERVER].add(event);
        } else if (event instanceof PacketArrivalEvent) {
            PacketArrivalEvent packetArrivalEvent = (PacketArrivalEvent) event;
            flowId = packetArrivalEvent.getPacket().getFlowId();
            queuesServer[(int) flowId % NUM_SERVER].add(event);
        } else if (event instanceof PacketDispatchedEvent) {
            PacketDispatchedEvent packetDispatchedEvent = (PacketDispatchedEvent) event;
            flowId = packetDispatchedEvent.getPacket().getFlowId();
            queuesServer[(int) flowId % NUM_SERVER].add(event);
        } else if (event instanceof TcpPacketResendEvent) {
			TcpPacketResendEvent tcpPacketResendEvent = (TcpPacketResendEvent) event;
			flowId = tcpPacketResendEvent.getFlowId();
			queuesServer[(int) flowId % NUM_SERVER].add(event);
		}
    }
   
	// public static void registerEvent(Event event) {
	// 	eventQueue.add(event);
	// 	int source_id;
	// 	Packet packet;
	// 	if (event instanceof FlowStartEvent) {
	// 		source_id = ((FlowStartEvent) event).getNetWorkDeviceId();
	// 	} else if (event instanceof PacketArrivalEvent) {
	// 		packet = ((PacketArrivalEvent) event).getPacket();
	// 		source_id = (int) (((IpPacket) packet).getSourceId());
	// 	} else {
	// 		packet = ((PacketDispatchedEvent) event).getPacket();
	// 		source_id = (int) (((IpPacket) packet).getSourceId());
	// 	}
	// 	queuesServer[source_id].add(event);

	// }

	private static boolean handleUserInput(String input) {
		switch (input) {
			case "s":
			case "start":
				return false;
			case "dump-state":
				SimulatorStateSaver.save(configuration);
				break;
		}
		return true;

	}

	/**
	 * Register to the simulator that a flow has been finished.
	 *
	 * @param flowId Flow identifier
	 */
	public static void registerFlowFinished(long flowId) {
		if (flowId < finishFlowIdThreshold) {
			finishedFlows.add(flowId);
		}
	}

	

	/**
	 * Retrieve the current time plus the amount of nanoseconds specified.
	 * This is used to plan events in the future.
	 *
	 * @param nanoseconds Amount of nanoseconds from now
	 *
	 * @return Time in nanoseconds
	 */
	public static long getTimeFromNow(long nanoseconds) {
		return now + nanoseconds;
	}

	/**
	 * Retrieve the current time in nanoseconds since simulation start.
	 *
	 * @return Current time in nanoseconds
	 */
	public static long getCurrentTime() {
		return now;
	}

	/**
	 * Retrieve the amount of events currently in the event queue.
	 *
	 * @return Number of events
	 */
	public static int getEventSize() {
		int eventSize = 0;
		for (int i = 0; i < NUM_SERVER; i++) {
			eventSize += queuesServer[i].size();
		}
		return eventSize;
	}

	/**
	 * Clean up everything of the simulation.
	 */
	public static void reset() {
		reset(true);
	}

	/**
	 * Clean up everything of the simulation.
	 *
	 * @param throwawayLogs True iff the logs should be thrown out
	 */
	public static void reset(boolean throwawayLogs) {

		// Close logger
		if (throwawayLogs) {
			SimulationLogger.closeAndThrowaway();
		} else {
			SimulationLogger.close();
		}

		// Reset random number generation
		randomManager = null;

		// Reset any run variables
		now = 0;
		eventQueue.clear();
		finishedFlows.clear();
		TransportLayer.staticReset();
		finishFlowIdThreshold = -1;

		for (int i = 0; i < NUM_SERVER; i++) {
			if (queuesServer[i] != null) {
				queuesServer[i].clear(); // Clear the queue
			}
		}

		// Reset configuration
		configuration = null;
		// No longer setup
		isSetup = false;

	}

	public static void dumpState(String dumpFolderName) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(
				new FileOutputStream(dumpFolderName + "/" + "simulator_queue.ser"));

		oos.writeObject(eventQueue);
		System.out.println("Done writing queue");
		JSONObject obj = new JSONObject();
		obj.put("now", now);
		FileWriter file = new FileWriter(dumpFolderName + "/" + "simulator_data.json");
		file.write(obj.toJSONString());
		file.flush();

	}

	public static boolean isFlowFinished(long flowId) {
		return finishedFlows.contains(flowId);
	}
}
