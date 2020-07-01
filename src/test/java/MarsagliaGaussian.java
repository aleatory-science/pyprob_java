import ch.ethz.idsc.tensor.DoubleScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import edu.diku.pyprob_java.MessageHandler;
import edu.diku.pyprob_java.Model;
import edu.diku.pyprob_java.distributions.Normal;
import edu.diku.pyprob_java.distributions.Uniform;

import java.io.IOException;

public class MarsagliaGaussian extends Model {
    private final Scalar priorMean;
    private final Scalar priorStd;
    private final Scalar likelihoodStd;

    public MarsagliaGaussian(String name, MessageHandler messageHandler,
                             Scalar priorMean, Scalar priorStd,
                             Scalar likelihoodStd) {
        super(name, messageHandler);
        this.priorMean = priorMean;
        this.priorStd = priorStd;
        this.likelihoodStd = likelihoodStd;
    }

    public MarsagliaGaussian(String name, MessageHandler messageHandler) {
        this(name, messageHandler, DoubleScalar.of(1.0),
                DoubleScalar.of(Math.sqrt(5.0)), DoubleScalar.of(Math.sqrt(2)));
    }

    @Override
    public Tensor call() throws IOException {
        var likelihoodMean = this.marsaglia();
        var likelihood = new Normal(likelihoodMean, this.likelihoodStd);

        this.observe(likelihood, "obs0");
        this.observe(likelihood, "obs1");

        return likelihoodMean;
    }

    private Tensor marsaglia() throws IOException {
        var uniform = new Uniform(DoubleScalar.of(-1.0),
                                  DoubleScalar.of(1.0));
        Scalar s = DoubleScalar.of(1.0);
        Scalar x = DoubleScalar.of(0.0);
        Scalar y;
        while (s.number().doubleValue() >= 1.0) {
            x = this.sample(uniform).Get(0);
            y = this.sample(uniform).Get(0);
            s = x.multiply(x).add(y.multiply(y)).Get();
        }
        return this.priorMean.add(this.priorStd.multiply(x.multiply(
            DoubleScalar.of(Math.sqrt(-2.0 * Math.log(s.number().doubleValue())
                                                     / s.number().doubleValue()))
        )));
    }

    public Scalar getPriorMean() {
        return priorMean;
    }

    public Scalar getPriorStd() {
        return priorStd;
    }

    public Scalar getLikelihoodStd() {
        return likelihoodStd;
    }

    public static void main(String[] args) throws IOException {
        try (var messageHandler = new MessageHandler()) {
           var model = new MarsagliaGaussian("Marsaglia Gaussian", messageHandler);
           model.run("tcp://127.0.0.1:5555");
        } catch (InterruptedException e) {
            System.out.println(e.toString());
        }
    }
}
