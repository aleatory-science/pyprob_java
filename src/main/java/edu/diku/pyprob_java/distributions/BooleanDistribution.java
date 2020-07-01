package edu.diku.pyprob_java.distributions;

import ch.ethz.idsc.tensor.DoubleScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;

public interface BooleanDistribution extends Distribution<Boolean> {
    @Override
    default Boolean convertSample(Tensor tensor) {
        return ((Scalar)tensor).number().intValue() != 0;
    }

    @Override
    default Tensor convertObserve(Boolean t) {
        return DoubleScalar.of(t ? 1. : 0.);
    }
}
