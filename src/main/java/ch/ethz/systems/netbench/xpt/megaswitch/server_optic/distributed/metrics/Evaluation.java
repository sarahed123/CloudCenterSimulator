package ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed.metrics;

public class Evaluation {

    private boolean mEvaluated;
    private double mEvaluation;

    public Evaluation(double evaluation){
        mEvaluated = false;
        mEvaluation = evaluation;
    }

    public boolean isEvaluated() {
        return mEvaluated;
    }

    public double getEvaluation() {
        return mEvaluation;
    }

    public void markEvaluated() {
        mEvaluated = true;
    }

}
