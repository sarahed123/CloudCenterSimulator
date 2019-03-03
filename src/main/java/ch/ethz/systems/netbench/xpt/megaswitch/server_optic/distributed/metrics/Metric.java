package ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed.metrics;

public interface Metric {

    public Evaluation evaluatePathRequest(int source, int dest);
    public void compareEvaluationWithResult(Evaluation evaluation, boolean result);
    public double getMetric();
}
