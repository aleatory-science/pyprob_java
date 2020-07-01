package edu.diku.pyprob_java.distributions;

import ch.ethz.idsc.tensor.Tensor;

public interface TensorDistribution extends Distribution<Tensor> {
    @Override
    default Tensor convertSample(Tensor tensor) {
        return tensor;
    }

    @Override
    default Tensor convertObserve(Tensor tensor) {
        return tensor;
    }
}
