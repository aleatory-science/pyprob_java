package pyprob.distributions;

import ch.ethz.idsc.tensor.Tensor;
import ppx.MessageBody;
import pyprob.MessageHandler;

import java.io.IOException;

public class Normal implements TensorDistribution {
    private Tensor mean;
    private Tensor stddev;

    public Normal(Tensor mean, Tensor stddev) {
        this.mean = mean;
        this.stddev = stddev;
    }

    public Tensor getMean() {
        return mean;
    }

    public void setMean(Tensor mean) {
        this.mean = mean;
    }

    public Tensor getStddev() {
        return stddev;
    }

    public void setStddev(Tensor stddev) {
        this.stddev = stddev;
    }

    @Override
    public byte fbDistType() {
        return ppx.Distribution.Normal;
    }

    @Override
    public int fbCreateDist(MessageHandler messageHandler) {
        var mMean = messageHandler.protocolTensor(this.mean);
        var mStddev = messageHandler.protocolTensor(this.stddev);
        return ppx.Normal.createNormal(messageHandler.getBuilder(), mMean, mStddev);
    }
}
