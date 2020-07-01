Java bindings for [PyProb](https://github.com/pyprob/pyprob).
The code is based loosely on the [PyProb C++ bindings](https://github.com/pyprob/pyprob_cpp).

### Building
The project uses [Gradle](https://gradle.org) as a build system.
It has been tested using Gradle 6.3 and Oracle OpenJDK 14.

Additional Requirements:
* [Flatbuffers](https://google.github.io/flatbuffers/) to compile the [PyProb PPX](https://github.com/pyprob/ppx) interface.

Necessary build commands:
* `gradle createFlatBuffers` to generate the Java files from the PPX description
* `gradle generateBuildConfig` to generate static information from git, used in the program.
* `gradle build` to compile the Java project.

### Basic usage
We have ported the [Marsaglia Gaussian](https://github.com/pyprob/pyprob/blob/master/examples/gaussian_unknown_mean_marsaglia.ipynb) model to Java as an example usage.
The core idea is that one writes a model in Java by inheriting from the `edu.diku.pyprob_java.Model` and implementing
the `call()` method which runs the computations in the model. Inside `call()` one can use `sample()` and
`observe()` statements to respectively probabilistically sample and condition the model on data.

To use the model from the Java side, one needs to create a `MessageHandler` to communicate using PPX and ZeroMQ
to the Python-based [PyProb](https://github.com/pyprob/pyprob) inference module.
This is usually done as follows:

```java
 try (var messageHandler = new MessageHandler()) {
       var model = new MarsagliaGaussian("Marsaglia Gaussian", messageHandler);
       model.run(serverAddress);
    } catch (InterruptedException e) {
        System.out.println(e.toString());
    }
```

Where `serverAddress` is some valid ZeroMQ address, e.g. `tcp://127.0.0.1:5555` or `ipc://@MarsagliaGaussian`.

From the Python-based PyProb point of view one needs to create a `RemoteModel` that connects to the same `serverAddress`.
For the Marsaglia Gaussian example [notebook](https://github.com/pyprob/pyprob/blob/master/examples/gaussian_unknown_mean_marsaglia.ipynb), one can simply replace the `GaussianUnknownMean` class and `model = GaussianUnknownMean()` lines
 with `model = RemoteModel(serverAddress)` and run the notebook.
