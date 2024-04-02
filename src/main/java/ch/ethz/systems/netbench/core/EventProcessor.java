package ch.ethz.systems.netbench.core;

import ch.ethz.systems.netbench.core.run.traffic.FlowStartEvent;
import ch.ethz.systems.netbench.core.network.PacketArrivalEvent;
import ch.ethz.systems.netbench.core.network.PacketDispatchedEvent;

import java.util.PriorityQueue;

import ch.ethz.systems.netbench.core.network.Event;

public class EventProcessor implements Runnable {
    private final PriorityQueue<? extends Event> eventQueue;

    public EventProcessor(PriorityQueue<? extends Event> eventQueue) {
        this.eventQueue = eventQueue;
    }

    @Override
    public void run() {
        while (true) {
            Event event = eventQueue.poll(); // Blocking call to wait for events
            event.trigger(); // Execute the event
        }
    }
}