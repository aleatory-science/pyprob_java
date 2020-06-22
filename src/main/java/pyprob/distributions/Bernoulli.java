package pyprob.distributions;

import ch.ethz.idsc.tensor.Tensor;
import ppx.Distribution;
import pyprob.MessageHandler;

public class Bernoulli implements BooleanDistribution {
    private Tensor probs;

    public Bernoulli(Tensor probs) {
        this.probs = probs;
    }

    public Tensor getProbs() {
        return probs;
    }

    public void setProbs(Tensor probs) {
        this.probs = probs;
    }

    @Override
    public byte fbDistType() {
        return Distribution.Bernoulli;
    }

    @Override
    public int fbCreateDist(MessageHandler messageHandler) {
        var builder = messageHandler.getBuilder();
        var mProbs = messageHandler.protocolTensor(this.probs);
        return ppx.Bernoulli.createBernoulli(builder, mProbs);
    }
}
