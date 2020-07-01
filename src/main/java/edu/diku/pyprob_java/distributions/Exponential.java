package edu.diku.pyprob_java.distributions;

import ch.ethz.idsc.tensor.Tensor;
import edu.diku.pyprob_java.MessageHandler;
import ppx.Distribution;

public class Exponential implements TensorDistribution {
    private Tensor rate;

    public Exponential(Tensor rate) {
        this.rate = rate;
    }

    public Tensor getRate() {
        return rate;
    }

    public void setRate(Tensor rate) {
        this.rate = rate;
    }

    @Override
    public byte fbDistType() {
        return Distribution.Exponential;
    }

    @Override
    public int fbCreateDist(MessageHandler messageHandler) {
        var builder = messageHandler.getBuilder();
        var mRate = messageHandler.protocolTensor(this.rate);
        return ppx.Exponential.createExponential(builder, mRate);
    }
}
