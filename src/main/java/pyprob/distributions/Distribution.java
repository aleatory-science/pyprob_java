package pyprob.distributions;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import pyprob.MessageHandler;

public interface Distribution {
    default Tensor sample(MessageHandler messageHandler,
                          boolean control, boolean replace,
                          String address, String name) {
        return Tensors.empty();
    }
    default void observe(MessageHandler messageHandler,
                         Tensor value, String address, String name) {
    }
}
