package pyprob.distributions;

import ch.ethz.idsc.tensor.Tensor;
import ppx.Distribution;
import pyprob.MessageHandler;

public class Beta implements TensorDistribution {
    private Tensor concentration0;
    private Tensor concentration1;

    public Beta(Tensor concentration1, Tensor concentration0) {
        this.concentration1 = concentration1;
        this.concentration0 = concentration0;
    }

    public Tensor getConcentration0() {
        return concentration0;
    }

    public void setConcentration0(Tensor concentration0) {
        this.concentration0 = concentration0;
    }

    public Tensor getConcentration1() {
        return concentration1;
    }

    public void setConcentration1(Tensor concentration1) {
        this.concentration1 = concentration1;
    }

    @Override
    public byte fbDistType() {
        return Distribution.Beta;
    }

    @Override
    public int fbCreateDist(MessageHandler messageHandler) {
        var builder = messageHandler.getBuilder();
        var mC0 = messageHandler.protocolTensor(this.concentration0);
        var mC1 = messageHandler.protocolTensor(this.concentration1);
        return ppx.Beta.createBeta(builder, mC1, mC0);
    }
}
