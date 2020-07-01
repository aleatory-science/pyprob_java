package edu.diku.pyprob_java.distributions;

import ch.ethz.idsc.tensor.Tensor;
import ppx.Distribution;
import edu.diku.pyprob_java.MessageHandler;

public class Gamma implements TensorDistribution {
    private Tensor concentration;
    private Tensor rate;

    public Gamma(Tensor concentration, Tensor rate) {
        this.concentration = concentration;
        this.rate = rate;
    }

    public Tensor getConcentration() {
        return concentration;
    }

    public void setConcentration(Tensor concentration) {
        this.concentration = concentration;
    }

    public Tensor getRate() {
        return rate;
    }

    public void setRate(Tensor rate) {
        this.rate = rate;
    }

    @Override
    public byte fbDistType() {
        return Distribution.Gamma;
    }

    @Override
    public int fbCreateDist(MessageHandler messageHandler) {
        var builder = messageHandler.getBuilder();
        var mConcentration = messageHandler.protocolTensor(this.concentration);
        var mRate = messageHandler.protocolTensor(this.rate);
        return ppx.Gamma.createGamma(builder, mConcentration, mRate);
    }
}
