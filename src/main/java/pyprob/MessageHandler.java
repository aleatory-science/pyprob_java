package pyprob;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.ArrayReshape;
import ch.ethz.idsc.tensor.alg.Dimensions;
import com.google.flatbuffers.FlatBufferBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import ppx.Message;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.stream.IntStream;

public class MessageHandler implements Closeable {
    private final Logger logger =
            LoggerFactory.getLogger(MessageHandler.class);
    private final ZContext context;
    private final ZMQ.Socket socket;
    private boolean isSocketConnected;
    private final FlatBufferBuilder builder;

    public MessageHandler() {
        context = new ZContext();
        builder = new FlatBufferBuilder();
        socket = this.context.createSocket(SocketType.REP);
        isSocketConnected = false;
    }

    public boolean isSocketConnected() {
        return isSocketConnected;
    }

    public FlatBufferBuilder getBuilder() {
        return builder;
    }

    public void startServer(String serverAddress) {
        this.isSocketConnected = this.socket.bind(serverAddress);
        if (!this.isSocketConnected) {
            logger.debug("Unable to bind to socket: {}", serverAddress);
            return;
        }
        logger.info("PPX (Java): ZMQ REP server listening at {}\n", serverAddress);
    }

    public Message receiveMessage() {
        var data = this.socket.recv();
        return Message.getRootAsMessage(ByteBuffer.wrap(data));
    }

    public void sendMessage(int message) {
        this.builder.finish(message);
        this.socket.send(this.builder.sizedByteArray());
        this.builder.clear();
    }

    public Tensor tensor(ppx.Tensor protocolTensor) {
        var data = IntStream.range(0, protocolTensor.dataLength())
                            .mapToDouble(protocolTensor::data)
                            .toArray();
        var shape = IntStream.range(0, protocolTensor.shapeLength())
                             .map(protocolTensor::shape)
                             .boxed()
                             .toArray(Integer[]::new);
        var res = Tensors.vectorDouble(data);
        if (shape.length > 0) {
            return ArrayReshape.of(res, shape);
        }
        else {
            return res;
        }
    }

    public int protocolTensor(Tensor tensor) {
        var data = tensor.flatten(-1)
                .map(Scalar.class::cast)
                .map(Scalar::number)
                .map(Number::doubleValue)
                .mapToDouble(Double::doubleValue)
                .toArray();
        var dataVector = ppx.Tensor.createDataVector(this.builder, data);
        var shape = Dimensions.of(tensor).stream()
                              .mapToInt(Integer::intValue)
                              .toArray();
        var shapeVector = ppx.Tensor.createShapeVector(this.builder, shape);
        return ppx.Tensor.createTensor(this.builder, dataVector, shapeVector);
    }

    @Override
    public void close() {
        if (context != null) context.close();
    }
}
