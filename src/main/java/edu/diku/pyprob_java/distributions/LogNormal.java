package edu.diku.pyprob_java.distributions;

import ch.ethz.idsc.tensor.Tensor;
import ppx.Distribution;
import edu.diku.pyprob_java.MessageHandler;

public class LogNormal implements TensorDistribution {
    private Tensor loc;
    private Tensor scale;

    public LogNormal(Tensor loc, Tensor scale) {
        this.loc = loc;
        this.scale = scale;
    }

    public Tensor getLoc() {
        return loc;
    }

    public void setLoc(Tensor loc) {
        this.loc = loc;
    }

    public Tensor getScale() {
        return scale;
    }

    public void setScale(Tensor scale) {
        this.scale = scale;
    }

    @Override
    public byte fbDistType() {
        return Distribution.LogNormal;
    }

    @Override
    public int fbCreateDist(MessageHandler messageHandler) {
        var builder = messageHandler.getBuilder();
        var mLoc = messageHandler.protocolTensor(this.loc);
        var mScale = messageHandler.protocolTensor(this.scale);
        return ppx.LogNormal.createLogNormal(builder, mLoc, mScale);
    }
}
