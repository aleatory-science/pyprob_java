package edu.diku.pyprob_java;

import ch.ethz.idsc.tensor.Tensor;
import com.github.ahmadsalim.pyprob_java.BuildConfig;
import edu.diku.pyprob_java.distributions.Distribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ppx.*;

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;

public abstract class Model {
    private final String systemName;
    private final String name;
    private final MessageHandler messageHandler;
    private volatile boolean isInterrupted;
    private final Logger logger = LoggerFactory.getLogger(Model.class);
    private boolean defaultControl = true;
    private boolean defaultReplace = false;

    public Model(String name, MessageHandler messageHandler) {
        this.systemName = String.format("%s %s (%s:%s)",
                BuildConfig.APP_NAME,
                BuildConfig.APP_VERSION,
                BuildConfig.GIT_BRANCH,
                BuildConfig.GIT_COMMIT_HASH);
        this.name = name;
        this.messageHandler = messageHandler;
        this.isInterrupted = false;
    }

    protected MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public String getName() {
        return name;
    }

    public void interrupt() {
        this.isInterrupted = true;
    }

    public boolean isDefaultControl() {
        return defaultControl;
    }

    public void setDefaultControl(boolean defaultControl) {
        this.defaultControl = defaultControl;
    }

    public boolean isDefaultReplace() {
        return defaultReplace;
    }

    public void setDefaultReplace(boolean defaultReplace) {
        this.defaultReplace = defaultReplace;
    }

    public abstract Tensor call() throws IOException;

    public void run(String serverAddress) throws InterruptedException, IOException {
        if (this.isInterrupted) {
            throw new InterruptedException();
        }
        this.messageHandler.startServer(serverAddress);
        logger.info("PPX (Java): System name: {}\n", this.systemName);
        logger.info("PPX (Java): Model name: {}\n", this.name);
        var numTraces = 0;
        while (!this.isInterrupted) {
            var message = this.messageHandler.receiveMessage();
            switch (message.bodyType()) {
                case MessageBody.Run:
                    numTraces++;
                    runModel(numTraces);
                    break;
                case MessageBody.Handshake:
                    Handshake handshake = new Handshake();
                    var convres = message.body(handshake);
                    assert convres != null;
                    var systemName = handshake.systemName();
                    handshake(systemName);
                    break;
                default:
                    reset();
                    break;
            }
        }
        if (this.isInterrupted) {
            throw new InterruptedException();
        }
    }

    private String extractAddress() {
        var walker = StackWalker.getInstance(RETAIN_CLASS_REFERENCE);
        var nameList = walker.walk(stack ->
                        stack.takeWhile(sf -> !(sf.getMethodName().equals("call") &&
                                                Model.class.isAssignableFrom(sf.getDeclaringClass())))
                              .skip(1)
                              .map(sf -> String.format("%s_%s", sf.getClassName(), sf.getMethodName()))
                              .map(st -> st.replace(".", "__"))
                              .collect(Collectors.toList())
                     );
        Collections.reverse(nameList);
        return String.format("[%s]", String.join(";", nameList));
    }

    public <T> T sample(edu.diku.pyprob_java.distributions.Distribution<T> dist) throws IOException {
        var address = this.extractAddress();
        return dist.sample(this.messageHandler, this.defaultControl,
                           this.defaultReplace, address, "");
    }

    public <T> T sample(edu.diku.pyprob_java.distributions.Distribution<T> dist, String name) throws IOException {
        var address = this.extractAddress();
        return dist.sample(this.messageHandler, this.defaultControl,
                           this.defaultReplace, address, name);
    }

    public <T> T sample(edu.diku.pyprob_java.distributions.Distribution<T> dist, boolean control, boolean replace) throws IOException {
        var address = this.extractAddress();
        return dist.sample(this.messageHandler, control, replace, address, "");
    }

    public <T> T sample(edu.diku.pyprob_java.distributions.Distribution<T> dist, boolean control, boolean replace, String name) throws IOException {
        var address = this.extractAddress();
        return dist.sample(this.messageHandler, control, replace, address, name);
    }

    public <T> void observe(edu.diku.pyprob_java.distributions.Distribution<T> dist, T value) throws IOException {
        var address = this.extractAddress();
        dist.observe(this.messageHandler, value, address, "");
    }

    public <T> void observe(edu.diku.pyprob_java.distributions.Distribution<T> dist, String name) throws IOException {
        var address = this.extractAddress();
        dist.observe(this.messageHandler, null, address, name);
    }

    public <T> void observe(Distribution<T> dist, T value, String name) throws IOException {
        var address = this.extractAddress();
        dist.observe(this.messageHandler, value, address, name);
    }

    public Tensor tag(Tensor value) {
        return this.tag(value, "");
    }

    public Tensor tag(Tensor value, String name) {
        var address = this.extractAddress();
        if (!this.messageHandler.isSocketConnected()) {
            logger.warn("PPX (Java): Warning: Not connected for tagging\n.");
            return value;
        }
        var builder = this.messageHandler.getBuilder();
        var resAddress = builder.createString(address);
        var resName = builder.createString(name);
        var resTensor = this.messageHandler.protocolTensor(value);
        var tag = Tag.createTag(builder, resAddress, resName, resTensor);
        var message = Message.createMessage(builder, MessageBody.Tag, tag);
        this.messageHandler.sendMessage(message);
        var resMessage = this.messageHandler.receiveMessage();
        logger.info("PPX (Java): Received message after tagging: {}", resMessage);
        return value;
    }

    private void runModel(int numTraces) throws IOException {
        logger.info("PPX (Java): Executed traces: {}\n", numTraces);
        var result = this.messageHandler.protocolTensor(this.call());
        var runResult = RunResult.createRunResult(this.messageHandler.getBuilder(),
                result);
        var resMessage = Message.createMessage(this.messageHandler.getBuilder(),
                MessageBody.RunResult, runResult);
        this.messageHandler.sendMessage(resMessage);
    }

    private void handshake(String systemName) {
        logger.info("PPX (Java): Connected to PPL system: {}\n", systemName);
        var resSystemName = this.messageHandler.getBuilder().createString(this.systemName);
        var resModelName = this.messageHandler.getBuilder().createString(this.name);
        var handshakeResult = HandshakeResult.createHandshakeResult(this.messageHandler.getBuilder(),
                resSystemName, resModelName);
        var resMessage = Message.createMessage(this.messageHandler.getBuilder(),
                MessageBody.HandshakeResult, handshakeResult);
        this.messageHandler.sendMessage(resMessage);
    }

    private void reset() {
        logger.error("PPX (Java): Error: Received an unexpected request. " +
                     "Resetting...\n");
        Reset.startReset(this.messageHandler.getBuilder());
        var reset = Reset.endReset(this.messageHandler.getBuilder());
        var resMessage = Message.createMessage(this.messageHandler.getBuilder(),
                MessageBody.Reset, reset);
        this.messageHandler.sendMessage(resMessage);
    }
}
