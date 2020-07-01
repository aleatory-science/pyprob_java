package edu.diku.pyprob_java.distributions;

import ch.ethz.idsc.tensor.Tensor;
import ppx.Distribution;
import edu.diku.pyprob_java.MessageHandler;

public class Binomial implements IntegerDistribution {
    private int totalCount;
    private Tensor probs;

    public Binomial(int totalCount, Tensor probs) {
        this.totalCount = totalCount;
        this.probs = probs;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public Tensor getProbs() {
        return probs;
    }

    public void setProbs(Tensor probs) {
        this.probs = probs;
    }

    @Override
    public byte fbDistType() {
        return Distribution.Binomial;
    }

    @Override
    public int fbCreateDist(MessageHandler messageHandler) {
        var builder = messageHandler.getBuilder();
        var mTotalCount = messageHandler.protocolTensor(this.convertObserve(this.totalCount));
        var mProbs = messageHandler.protocolTensor(this.probs);
        return ppx.Binomial.createBinomial(builder, mTotalCount, mProbs);
    }
}
