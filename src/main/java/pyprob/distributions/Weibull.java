package pyprob.distributions;

import ch.ethz.idsc.tensor.Tensor;
import ppx.Distribution;
import pyprob.MessageHandler;

public class Weibull implements TensorDistribution {
    private Tensor scale;
    private Tensor concentration;

    public Weibull(Tensor scale, Tensor concentration) {
        this.scale = scale;
        this.concentration = concentration;
    }

    public Tensor getScale() {
        return scale;
    }

    public void setScale(Tensor scale) {
        this.scale = scale;
    }

    public Tensor getConcentration() {
        return concentration;
    }

    public void setConcentration(Tensor concentration) {
        this.concentration = concentration;
    }

    @Override
    public byte fbDistType() {
        return Distribution.Weibull;
    }

    @Override
    public int fbCreateDist(MessageHandler messageHandler) {
        var builder = messageHandler.getBuilder();
        var mScale = messageHandler.protocolTensor(this.scale);
        var mConcentration = messageHandler.protocolTensor(this.concentration);
        return ppx.Weibull.createWeibull(builder, mScale, mConcentration);
    }
}
