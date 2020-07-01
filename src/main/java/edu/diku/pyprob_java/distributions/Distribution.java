package edu.diku.pyprob_java.distributions;

import ch.ethz.idsc.tensor.Tensor;
import ppx.MessageBody;
import ppx.ObserveResult;
import ppx.SampleResult;
import edu.diku.pyprob_java.MessageHandler;

import java.io.IOException;

public interface Distribution<T> {
    byte fbDistType();

    int fbCreateDist(MessageHandler messageHandler);

    T convertSample(Tensor tensor);

    Tensor convertObserve(T t);

    default T sample(MessageHandler messageHandler,
                          boolean control, boolean replace,
                          String address, String name) throws IOException {
        if (!messageHandler.isSocketConnected()) {
            throw new IOException("PPX (Java): Sampling while not connected to server");
        }
        var builder = messageHandler.getBuilder();
        var mDist = this.fbCreateDist(messageHandler);
        var mAddress = builder.createString(address);
        var mName = builder.createString(name);
        var mSample = ppx.Sample.createSample(builder, mAddress, mName,
                this.fbDistType(), mDist, control, replace);
        var message = ppx.Message.createMessage(builder, MessageBody.Sample, mSample);
        messageHandler.sendMessage(message);
        var resMessage = messageHandler.receiveMessage();
        if (resMessage.bodyType() == MessageBody.SampleResult) {
            var sampleResult = new SampleResult();
            var convres = resMessage.body(sampleResult);
            assert convres != null;
            return this.convertSample(messageHandler.tensor(sampleResult.result()));
        }
        throw new IOException("PPX (Java): Unexpected result message");
    }

    default void observe(MessageHandler messageHandler,
                         T value, String address, String name) throws IOException {
        if (!messageHandler.isSocketConnected()) {
            throw new IOException("PPX (Java): Observing while not connected to server");
        }
        var builder = messageHandler.getBuilder();
        var mValue = value == null ? 0 : messageHandler.protocolTensor(this.convertObserve(value));
        var mDist = this.fbCreateDist(messageHandler);
        var mAddress = builder.createString(address);
        var mName = builder.createString(name);
        var mObserve = ppx.Observe.createObserve(builder, mAddress, mName,
                this.fbDistType(), mDist, mValue);
        var message = ppx.Message.createMessage(builder, MessageBody.Observe, mObserve);
        messageHandler.sendMessage(message);
        var resMessage = messageHandler.receiveMessage();
        if (resMessage.bodyType() == MessageBody.ObserveResult) {
            var observeResult = new ObserveResult();
            var convres = resMessage.body(observeResult);
            assert convres != null;
            return;
        }
        throw new IOException("PPX (Java): Unexpected result message");
    }
}
