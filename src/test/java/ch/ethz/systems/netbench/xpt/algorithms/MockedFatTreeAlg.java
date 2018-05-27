package ch.ethz.systems.netbench.xpt.algorithms;

import edu.asu.emit.algorithm.graph.BaseGraph;
import edu.asu.emit.algorithm.graph.Vertex;
import edu.asu.emit.algorithm.graph.algorithms.FatTreeShortestPathAlg;

import java.util.HashMap;
import java.util.HashSet;

public class MockedFatTreeAlg extends FatTreeShortestPathAlg{

    public MockedFatTreeAlg(BaseGraph graph, int deg) {
        super(graph, deg,false);
    }


    @Override
    protected HashSet<Vertex> getPathFromVToLevel(Vertex s, int level, HashMap<Vertex, Vertex> predecessorIndex) {
        return super.getPathFromVToLevel(s,level,predecessorIndex);
    }


}
