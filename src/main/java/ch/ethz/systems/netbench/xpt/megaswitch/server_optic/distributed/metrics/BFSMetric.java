package ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed.metrics;

import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.algorithms.BFS;

public class BFSMetric implements Metric {

    private final Graph[] mGraphs;
    private final BFS[] mBFSs;
    private double mOpportunitiesMissed;
    private double mComparisons;
    public BFSMetric(Graph[] graphs){
        mGraphs = graphs;
        mComparisons = 0d;
        mOpportunitiesMissed = 0d;
        mBFSs = new BFS[graphs.length];
        for(int i=0; i<graphs.length; i++){
            mBFSs[i] = new BFS(graphs[i]);
        }
    }

    @Override
    public Evaluation evaluatePathRequest(int source, int dest) {
        int distance = Integer.MAX_VALUE;
        for(BFS bfs: mBFSs){
            try{
                distance = bfs.getDistance(source,dest);
            }catch (NoPathException e){

            }

        }
        return new Evaluation(distance==Integer.MAX_VALUE ? 0d : 1d);
    }

    @Override
    public void compareEvaluationWithResult(Evaluation evaluation, boolean result) {
        if(!result && evaluation.getEvaluation()==1d){
            mOpportunitiesMissed +=1d;
        }
        mComparisons++;
        evaluation.markEvaluated();

    }

    @Override
    public double getMetric() {
        return mOpportunitiesMissed/mComparisons;
    }


}
