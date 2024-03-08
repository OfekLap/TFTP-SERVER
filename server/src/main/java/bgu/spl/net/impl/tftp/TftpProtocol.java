package bgu.spl.net.impl.tftp;

import java.nio.charset.StandardCharsets;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.ConnectionsImpl;
import bgu.spl.net.impl.tftp.TftpEncoderDecoder;

public class TftpProtocol implements BidiMessagingProtocol<byte[]> {

    private int connectionId;
    private boolean shouldTerminate = false;
    private Connections<byte[]> connections;

    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        // TODO implement this
        this.connectionId = connectionId;
        this.connections = connections;
    }

    @Override
    public void process(byte[] message) {
        // TODO implement this
        // StringBuilder sb = new StringBuilder();
        // sb.append("process got: {");
        // for (int i = 0; i < message.length; i++) {
        // sb.append(message[i]);
        // if (i != message.length - 1) {
        // sb.append(",");
        // }
        // }
        // sb.append("}");
        // System.out.println(sb.toString());

        if (message[1] == 7) {
            Connections.connect(connectionId, connections);
        }
    }

    @Override
    public boolean shouldTerminate() {
        // TODO implement this
        return shouldTerminate;
    }

}
