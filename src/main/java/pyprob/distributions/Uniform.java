package pyprob.distributions;

import ch.ethz.idsc.tensor.Tensor;
import pyprob.MessageHandler;

public class Uniform implements TensorDistribution {
    private Tensor low;
    private Tensor high;

    public Uniform(Tensor low, Tensor high) {
        this.low = low;
        this.high = high;
    }

    public Tensor getLow() {
        return low;
    }

    public void setLow(Tensor low) {
        this.low = low;
    }

    public Tensor getHigh() {
        return high;
    }

    public void setHigh(Tensor high) {
        this.high = high;
    }

    @Override
    public byte fbDistType() {
        return ppx.Distribution.Uniform;
    }

    @Override
    public int fbCreateDist(MessageHandler messageHandler) {
        var builder = messageHandler.getBuilder();
        var mLow = messageHandler.protocolTensor(this.low);
        var mHigh = messageHandler.protocolTensor(this.high);
        return ppx.Uniform.createUniform(builder, mLow, mHigh);
    }
}
