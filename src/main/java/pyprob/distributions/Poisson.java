package pyprob.distributions;

import ch.ethz.idsc.tensor.Tensor;
import ppx.Distribution;
import pyprob.MessageHandler;

public class Poisson implements IntegerDistribution {
    private Tensor rate;

    public Poisson(Tensor rate) {
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
        return Distribution.Poisson;
    }

    @Override
    public int fbCreateDist(MessageHandler messageHandler) {
        var builder = messageHandler.getBuilder();
        var mRate = messageHandler.protocolTensor(this.rate);
        return ppx.Poisson.createPoisson(builder, mRate);
    }
}
