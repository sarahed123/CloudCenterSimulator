package ch.ethz.systems.netbench.core;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Event;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.random.RandomManager;
import ch.ethz.systems.netbench.core.run.infrastructure.BaseInitializer;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.core.run.traffic.FlowStartEvent;
import ch.ethz.systems.netbench.core.network.PacketArrivalEvent;
import ch.ethz.systems.netbench.core.network.PacketDispatchedEvent;
import ch.ethz.systems.netbench.core.state.SimulatorStateSaver;
import ch.ethz.systems.netbench.core.EventProcessor;
import ch.ethz.systems.netbench.xpt.xpander.XpanderRouter;
import org.json.simple.JSONObject;
import javax.swing.text.html.HTMLDocument.Iterator;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import javax.swing.text.html.HTMLDocument.Iterator;

import java.lang.Thread;

/**
 * The simulator is responsible for offering general
 * services to all other interacting parts, such as
 * configurations and current time management. It uses
 * a deterministic priority queue to precisely execute
 * event after event.
 */

public class Simulator extends Thread {
	// Time interval at which to show the percentage of progress
	private static long PROGRESS_SHOW_INTERVAL_NS = 10000000L; // 0.01s = 10mss

	// Main ordered event queue (run variable)
	private static PriorityQueue<Event> eventQueue = new PriorityQueue<>();

	// !code add!
	private static PriorityQueue<PacketArrivalEvent> packetArrivalEventQueue = new PriorityQueue<>();
	private static PriorityQueue<PacketDispatchedEvent> packetDispatchedEventQueue = new PriorityQueue<>();
	private static PriorityQueue<FlowStartEvent> flowStartEventQueue = new PriorityQueue<>();

	private static Thread packetArrivalThread;
	private static Thread packetDispatchedThread;
	private static Thread flowStartThread;

	// Current time in ns in the simulation (run variable)
	private static long now;

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

	private static long startTime;
	private static long nextProgressLog;

	private Simulator() {
		// Static class only
	}

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

		// eventQueue.clear();

		// !add code!/
		packetArrivalEventQueue.clear();
		packetDispatchedEventQueue.clear();
		flowStartEventQueue.clear();

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

	public static void runNs(long runtimeNanoseconds, long flowsFromStartToFinish) {

		// Reset run variables (queue is not cleared because it has to start somewhere,
		// e.g. flow start events)
		// now = 0;

		packetArrivalThread = new Thread(() -> run(packetArrivalEventQueue));
		packetDispatchedThread = new Thread(() -> run(packetDispatchedEventQueue));
		flowStartThread = new Thread(() -> run(flowStartEventQueue));

		totalRuntimeNs = runtimeNanoseconds;

		NonblockingBufferedReader reader = new NonblockingBufferedReader(System.in);
		// Finish flow threshold, if it is negative the flow finish will be very far in
		// the future
		finishFlowIdThreshold = flowsFromStartToFinish;
		if (flowsFromStartToFinish <= 0) {
			flowsFromStartToFinish = Long.MAX_VALUE;
		}
		PROGRESS_SHOW_INTERVAL_NS = runtimeNanoseconds / 50l;

		// Log start
		System.out.println("Starting simulation (total time: " + runtimeNanoseconds + "ns);...");

		// Time loop
		startTime = System.currentTimeMillis();
		long realTime = System.currentTimeMillis();
		nextProgressLog = PROGRESS_SHOW_INTERVAL_NS;
		boolean endedDueToFlowThreshold = false;

		long timeEventPacketArrival, timeEventPacketDispatched, timeEventFlowStart;

		while ((!packetArrivalEventQueue.isEmpty() || !packetDispatchedEventQueue.isEmpty()
				|| !flowStartEventQueue.isEmpty()) && now <= runtimeNanoseconds) {
			while (reader.isPaused()) {
				try {
					System.out.println("Input command");
					BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
					// System.out.println(buffer.readLine());
					if (!handleUserInput(buffer.readLine())) {
						reader.reset();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					throw new RuntimeException(e);
				}

			}

			// peek the all event in the head of every queue
			Event eventPacketArrival = packetArrivalEventQueue.peek();
			Event eventPacketDispatched = packetDispatchedEventQueue.peek();
			Event eventFlowStart = flowStartEventQueue.peek();

			// check the time of the head events
			timeEventPacketArrival = eventPacketArrival.getTime();
			timeEventPacketDispatched = eventPacketDispatched.getTime();
			timeEventFlowStart = eventFlowStart.getTime();

			// check the min event to trigger
			long min = timeEventPacketArrival;
			String eventMin = "eventPacketArrival";

			if (timeEventPacketDispatched < min) {
				min = timeEventPacketDispatched;
				eventMin = "eventPacketDispatched";
			}
			if (timeEventFlowStart < min) {
				min = timeEventFlowStart;
				eventMin = "eventFlowStart";
			}

			// trigger the min time event
			now = min;
			if (now <= runtimeNanoseconds) {
				if (eventMin == "eventPacketArrival") {
					packetArrivalEventQueue.poll();
					eventPacketArrival.trigger();
					if (eventPacketArrival.retrigger()) {
						registerEvent(eventPacketArrival);
					}
				} else if (eventMin == "eventPacketDispatched") {
					packetDispatchedEventQueue.poll();
					eventPacketDispatched.trigger();
					if (eventPacketDispatched.retrigger()) {
						registerEvent(eventPacketDispatched);
					}
				} else {
					flowStartEventQueue.poll();
					eventFlowStart.trigger();
					if (eventFlowStart.retrigger()) {
						registerEvent(eventFlowStart);
					}
				}
			}

			// Log elapsed time
			if (now > nextProgressLog) {
				nextProgressLog += PROGRESS_SHOW_INTERVAL_NS;
				long realTimeNow = System.currentTimeMillis();
				System.out.println("Elapsed " + (double) PROGRESS_SHOW_INTERVAL_NS / (double) 1000000000
						+ "s simulation in " + ((realTimeNow - realTime) / 1000.0) + "s real (total progress: "
						+ ((((double) now) / ((double) runtimeNanoseconds)) * 100) + "%).");
				realTime = realTimeNow;

				if (RemoteRoutingController.getInstance() != null) {
					System.out.print(RemoteRoutingController.getInstance().getCurrentState());
				}
			}

			if (finishedFlows.size() >= flowsFromStartToFinish) {
				endedDueToFlowThreshold = true;
				break;
			}

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

	private static void run(PriorityQueue<? extends Event> queue) {

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
	// public static void runNs(long runtimeNanoseconds, long
	// flowsFromStartToFinish) {

	// // Reset run variables (queue is not cleared because it has to start
	// somewhere,
	// // e.g. flow start events)
	// // now = 0;
	// totalRuntimeNs = runtimeNanoseconds;
	// NonblockingBufferedReader reader = new NonblockingBufferedReader(System.in);
	// // Finish flow threshold, if it is negative the flow finish will be very far
	// in
	// // the future
	// finishFlowIdThreshold = flowsFromStartToFinish;
	// if (flowsFromStartToFinish <= 0) {
	// flowsFromStartToFinish = Long.MAX_VALUE;
	// }
	// PROGRESS_SHOW_INTERVAL_NS = runtimeNanoseconds / 50l;

	// // Log start
	// System.out.println("Starting simulation (total time: " + runtimeNanoseconds +
	// "ns);...");

	// // Time loop
	// long startTime = System.currentTimeMillis();
	// long realTime = System.currentTimeMillis();
	// long nextProgressLog = PROGRESS_SHOW_INTERVAL_NS;
	// boolean endedDueToFlowThreshold = false;

	// packetArrivalThread = new Thread(() ->
	// eventProcessor(packetArrivalEventQueue, runtimeNanoseconds, reader,
	// nextProgressLog, realTime, flowsFromStartToFinish, endedDueToFlowThreshold));
	// packetDispatchedThread = new Thread(() ->
	// eventProcessor(packetDispatchedEventQueue, runtimeNanoseconds, reader,
	// nextProgressLog, realTime, flowsFromStartToFinish, endedDueToFlowThreshold));
	// flowStartThread = new Thread(
	// () -> eventProcessor(flowStartEventQueue, runtimeNanoseconds, reader,
	// nextProgressLog, realTime,
	// flowsFromStartToFinish, endedDueToFlowThreshold));

	// while ((!packetArrivalEventQueue.isEmpty() ||
	// !packetDispatchedEventQueue.isEmpty()
	// || !flowStartEventQueue.isEmpty()) && now <= runtimeNanoseconds) {
	// while (reader.isPaused()) {
	// try {
	// System.out.println("Input command");
	// BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
	// // System.out.println(buffer.readLine());
	// if (!handleUserInput(buffer.readLine())) {
	// reader.reset();
	// }
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// throw new RuntimeException(e);
	// }

	// }

	// if (finishedFlows.size() >= flowsFromStartToFinish) {
	// endedDueToFlowThreshold = true;
	// break;
	// }
	// }

	// packetArrivalThread.start();
	// packetDispatchedThread.start();
	// flowStartThread.start();

	// packetArrivalThread.interrupt();
	// packetDispatchedThread.interrupt();
	// flowStartThread.interrupt();

	// // Make sure run ends at the final time if it ended because there were no
	// // more events or the runtime was exceeded
	// if (!endedDueToFlowThreshold) {
	// now = runtimeNanoseconds;
	// }

	// // Log end
	// System.out.println("Simulation finished (simulated " + (runtimeNanoseconds /
	// 1e9) + "s in a real-world time of "
	// + ((System.currentTimeMillis() - startTime) / 1000.0) + "s).");

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
	 * Register an event in the simulation.
	 * !add code!
	 * 
	 * @param event Event instance
	 */
	public static void registerEvent(Event event) {
		eventQueue.add(event);
		if (event instanceof PacketArrivalEvent) {
			packetArrivalEventQueue.add((PacketArrivalEvent) event);
		} else if (event instanceof PacketDispatchedEvent) {
			packetDispatchedEventQueue.add((PacketDispatchedEvent) event);
		} else if (event instanceof FlowStartEvent) {
			flowStartEventQueue.add((FlowStartEvent) event);
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
		return eventQueue.size();
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
		// eventQueue.clear();

		// !add code!/
		packetArrivalEventQueue.clear();
		packetDispatchedEventQueue.clear();
		flowStartEventQueue.clear();

		finishedFlows.clear();
		TransportLayer.staticReset();
		finishFlowIdThreshold = -1;

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
