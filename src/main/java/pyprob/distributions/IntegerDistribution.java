package pyprob.distributions;

import ch.ethz.idsc.tensor.DoubleScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;

public interface IntegerDistribution extends Distribution<Integer> {
    @Override
    default Integer convertSample(Tensor tensor) {
        return ((Scalar)tensor).number().intValue();
    }

    @Override
    default Tensor convertObserve(Integer t) {
        return DoubleScalar.of(t);
    }
}
