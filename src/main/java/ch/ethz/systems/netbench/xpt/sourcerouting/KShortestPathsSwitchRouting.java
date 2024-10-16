package ch.ethz.systems.netbench.xpt.sourcerouting;

import ch.ethz.systems.netbench.core.config.GraphDetails;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.routing.RoutingPopulator;
import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Vertex;
import edu.asu.emit.algorithm.graph.algorithms.YenTopKShortestPathsAlg;

import java.io.*;
import java.util.*;


public class KShortestPathsSwitchRouting extends RoutingPopulator {

    private static final String PATHS_CACHE_DIRECTORY = "paths-cache";
    private final Map<Integer, NetworkDevice> idToNetworkDevice;

    public KShortestPathsSwitchRouting(Map<Integer, NetworkDevice> idToNetworkDevice,NBProperties configuration) {
    	super(configuration);
        SimulationLogger.logInfo("Routing", "K_SHORTEST_PATHS");
        this.idToNetworkDevice = idToNetworkDevice;
    }

    /**
     * Determine the K-shortest paths between the ToRs, using a cache when available.
     * If no valid cache is available, it runs Yen's K-shortest paths algorithm to
     * create that cache.
     */
    @Override
    public void populateRoutingTables() {
        
        // Select all the nodes which are ToR
        GraphDetails details = configuration.getGraphDetails();
        int k = configuration.getIntegerPropertyOrFail("k_for_k_shortest_paths");

        // Create graph and prepare shortest path algorithm
        Graph graph = configuration.getGraph();
        try {
            String fileName = getKspCacheFilename(details);
            File f = new File(fileName);
            System.out.println(fileName);
            if (f.exists() && f.length() > 0) {
                BufferedReader br = new BufferedReader(new FileReader(fileName));
                String kLine = br.readLine();
                String spl[] = kLine.split("=");
                int kInFile = Integer.valueOf(spl[1]);
                if (kInFile >= k) {
                    determineKspRoutingStateCached(k, details);
                } else {
                    determineKspRoutingStateUncached(k, details, graph);
                }
            } else {
                determineKspRoutingStateUncached(k, details, graph);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Determine the K-shortest paths between the ToRs with a cache present by reading from file.
     *
     * @param k         K in k-shortest paths
     * @param details   Graph details
     */
    private void determineKspRoutingStateCached(int k, GraphDetails details) {

        try {

            System.out.print("Determining KSP-" + k + " routing state (cache present, so reading from file " + getKspCacheFilename(details) + ")...");

            // Open file stream
            FileReader input = new FileReader(getKspCacheFilename(details));
            BufferedReader br = new BufferedReader(input);

            // Go over parameter lines one-by-one, stop when encountering non-parameter lines
            String line;
            br.readLine(); // Skip the k=X line
            while ((line = br.readLine()) != null) {
                line = line.trim();

                // Split up by comma
                String[] split = line.split(",");
                if (split.length != 4) {
                    throw new IllegalArgumentException("Invalid k-paths line:\n" + line);
                }

                // Retrieve k value for this line and only add if it passes the threshold
                int kPath = Integer.valueOf(split[0]);
                if (kPath <= k) {

                    // Retrieve source and destination graph device identifier
                    int src = Integer.valueOf(split[1]);
                    int dst = Integer.valueOf(split[2]);
                    String pSpl [] = split[3].split("-");
                    SourceRoutingPath path = new SourceRoutingPath();
                    for (String s  : pSpl) {
                        path.add(Integer.valueOf(s));
                    }

                    // Add path as potential path to graph device for the set destination
                    ((SourceRoutingSwitch) idToNetworkDevice.get(src)).addPathToDestination(dst, path);

                }

            }

            // Close stream
            br.close();

            System.out.println(" done.");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Determine the K-shortest paths between the ToRs without a cache present using Yen's K-shortest paths algorithm.
     * It automatically creates a cache for the topology.
     *
     * @param k         K in k-shortest paths
     * @param details   Topology details
     * @param graph     Graph
     */
    private void determineKspRoutingStateUncached(int k, GraphDetails details, Graph graph) {

        // Retrieve ToR nodes
        Set<Integer> torNodes = details.getTorNodeIds();

        System.out.print("Determining KSP-" + k + " routing state (no cache present; writing cache to " + getKspCacheFilename(details) + ")...");

        try {

            // Create path cache file
            File f = new File(PATHS_CACHE_DIRECTORY);
            if (!f.mkdirs() && !f.exists()) {
                throw new RuntimeException("Could not create paths cache folder: " + PATHS_CACHE_DIRECTORY + ".");
            }
            BufferedWriter pathsFile = new BufferedWriter(
                    new FileWriter(getKspCacheFilename(details))
            );
            pathsFile.write("k=" + k + "\n");

            // Go over every graph device pair and set the forwarder switch routing table
            for (Integer i : torNodes) {
                for (Integer j : torNodes) {

                    if (!i.equals(j)) {

                        // Find shortest paths as many wanted
                        YenTopKShortestPathsAlg alg = new YenTopKShortestPathsAlg(graph, graph.getVertex(i), graph.getVertex(j),configuration.getProperty("graph_edge_weight_rule"));
                        int found = 0;
                        while (alg.hasNext()) {

                            // Start of the path line
                            StringBuilder pathLine = new StringBuilder();
                            pathLine.append(found + 1).append(",").append(i).append(",").append(j).append(",");

                            // Get path in vertices
                            SourceRoutingPath path = new SourceRoutingPath();
                            for (Vertex v : alg.next().getVertexList()) {
                                path.add(v.getId());
                                pathLine.append(String.valueOf(v.getId())).append("-");
                            }

                            // Write away path line
                            String temp = pathLine.substring(0, pathLine.length() - 1); // Remove last -
                            temp += "\n";
                            pathsFile.write(temp);

                            // Add to routing state
                            ((SourceRoutingSwitch) idToNetworkDevice.get(i)).addPathToDestination(j, path);

                            // Finish if the maximum amount of paths is reached
                            found++;
                            if (found >= k) {
                                break;
                            }

                        }

                        // Create warning message if not enough different paths could be found
                        if (found != k) {
                            System.out.println("WARNING: could only find " + found + " paths for " + i + " -> " + j + ", which is less than k=" + k + ".");
                        }

                    }

                }

                // Log progress...
                if (torNodes.size() > 10 && ((i + 1) % Math.ceil((torNodes.size() / 100.0))) == 0) {
                    System.out.print(" " + (((double) i + 1) / (torNodes.size()) * 100) + "%...");
                }

            }

            System.out.println(" done.");

            pathsFile.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    /**
     * Retrieve the K-shortest paths cache file name.
     *
     * @param details   Topology details
     *
     * @return The K-shortest paths cache file name (no guarantee that it exists, only what is expected)
     */
    private static String getKspCacheFilename(GraphDetails details) {
        return PATHS_CACHE_DIRECTORY + "/" + details.getIdHash() + ".kspaths";
    }

}
