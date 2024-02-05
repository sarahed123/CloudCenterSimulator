![NetBench](images/netbench_banner.png)

NetBench is a packet-level simulator, focused on modelling congestion and queueing.

## Getting Started final Project

#### 1. Software dependencies

* **Java 8:** Version 8 of Java; both Oracle JDK and OpenJDK are supported and produce under that same seed deterministic results. Additionally the project uses the Apache Maven software project management and comprehension tool (version 3+).

* **Python 2 (optional):** Recent version of Python 2 for analysis; be sure you can globally run `python <script.py>` (see step 2.2).

#### 2. Building

1. Build the executable `NetBench.jar` by using the following maven command: `mvn clean compile assembly:single`

#### 3. Running

1. Execute a demo run by using the following command: `java -jar -ea NetBench.jar ./example/runs/demo.properties`

2. After the run, the log files are saved in the `./temp/demo` folder

3. If you have python 2 installed, you can view calculated statistics about flow completion and port utilization (e.g. mean FCT, 99th %-tile port utilization, ....) in the `./temp/demo/analysis` folder.

## Software structure

There are three sub-packages in *netbench*: (a) core, containing core functionality, (b) ext (extension), which contains functionality implemented and quite thoroughly tested, and (c) xpt (experimental), which contains functionality not yet as thoroughly tested but reasonably vetted and assumed to be correct for the usecase it was written for.

The framework is written based on five core components:
1. **Network device**: abstraction of a node, can be a server (has a transport layer) or merely function as switch (no transport layer);
2. **Transport layer**: maintains the sockets for each of the flows that are started at the network device and for which it is the destination;
3. **Intermediary**: placed between the network device and transport layer, is able to modify each packet before arriving at the transport layer and after leaving the transport layer;
4. **Link**: quantifies the capabilities of the physical link, which the output port respects;
5. **Output port**: models output ports and their queueing behavior.

Look into `ch.ethz.systems.netbench.ext.demo` for an impression how to extend the framework.  If you've written an extension, it is necessary to add it in its respective selector in `ch.ethz.systems.netbench.run`. If you've added new properties, be sure to add them in the `ch.ethz.systems.netbench.config.BaseAllowedProperties` class.

More information about the framework can be found in the thesis located at [https://www.research-collection.ethz.ch/handle/20.500.11850/156350](https://www.research-collection.ethz.ch/handle/20.500.11850/156350) (section 4.2: NetBench: Discrete Packet Simulator).
