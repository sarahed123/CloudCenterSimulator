package ch.ethz.systems.netbench.xpt.algorithms;

import ch.ethz.systems.netbench.core.config.GraphDetails;
import ch.ethz.systems.netbench.core.config.GraphReader;
import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Vertex;
import edu.asu.emit.algorithm.graph.algorithms.FatTreeShortestPathAlg;
import edu.asu.emit.algorithm.utils.VertexWeightTieBreaker;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

@RunWith(MockitoJUnitRunner.class)
public class FatTreePathsAlgTest {
    Graph g;
    MockedFatTreeAlg alg;
    @Before
    public void setup() throws IOException {
        Pair<Graph, GraphDetails> result = GraphReader.read("example/topologies/fat_tree/fat_tree_k14.topology");
        g = result.getLeft();
        Vertex.setTieBreaker(new VertexWeightTieBreaker() {
            @Override
            public int breakTie(Vertex vertex, Vertex vertex1) {
                return 0;
            }
        });
        alg = new MockedFatTreeAlg(g,4);
    }


    @Test
    public void testPredecessorMap(){
        HashMap<Vertex,Vertex> predecessors = new HashMap<>();
        alg.getPathFromVToLevel(new Vertex(0),2,predecessors);
        Arrays.toString(predecessors.entrySet().toArray());


    }

}
