package pyprob;

import ch.ethz.idsc.tensor.Tensor;
import com.github.ahmadsalim.pyprob_java.BuildConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ppx.*;
import pyprob.distributions.Distribution;

import java.util.Collections;
import java.util.stream.Collectors;


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

    abstract Tensor call();

    public void run(String serverAddress) throws InterruptedException {
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
                    var systemName = ((Handshake) message.body(message)).systemName();
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
        var walker = StackWalker.getInstance();
        var nameList = walker.walk(stack ->
                        stack.takeWhile(sf -> !sf.getMethodName().equals("call") &&
                                       Model.class.isAssignableFrom(sf.getDeclaringClass()))
                              .map(sf -> String.format("%s.%s", sf.getClassName(), sf.getMethodName()))
                              .collect(Collectors.toList())
                     );
        Collections.reverse(nameList);
        return String.format("[%s]", String.join(",", nameList));
    }

    public Tensor sample(Distribution dist) {
        var address = this.extractAddress();
        return dist.sample(this.messageHandler, this.defaultControl,
                           this.defaultReplace, address, "");
    }

    public Tensor sample(Distribution dist, String name) {
        var address = this.extractAddress();
        return dist.sample(this.messageHandler, this.defaultControl,
                           this.defaultReplace, address, name);
    }

    public Tensor sample(Distribution dist, boolean control, boolean replace) {
        var address = this.extractAddress();
        return dist.sample(this.messageHandler, control, replace, address, "");
    }

    public Tensor sample(Distribution dist, boolean control, boolean replace, String name) {
        var address = this.extractAddress();
        return dist.sample(this.messageHandler, control, replace, address, name);
    }

    public void observe(Distribution dist, Tensor value) {
        var address = this.extractAddress();
        dist.observe(this.messageHandler, value, address, "");
    }

    public void observe(Distribution dist, String name) {
        var address = this.extractAddress();
        dist.observe(this.messageHandler, null, address, name);
    }

    public void observe(Distribution dist, Tensor value, String name) {
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

    private void runModel(int numTraces) {
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
