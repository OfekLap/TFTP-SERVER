package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T> {

    private ConcurrentHashMap<Integer, ConnectionHandler<T>> connectionsMap;

    public ConnectionsImpl() {
        this.connectionsMap = new ConcurrentHashMap();
    }

    @Override
    public void connect(int connectionId, ConnectionHandler<T> handler) {
        if (!connectionsMap.containsKey(connectionId)) {
            connectionsMap.put(connectionId, handler);
        }
    }

    @Override
    public boolean send(int connectionId, T msg) {
        ConnectionHandler<T> handler = connectionsMap.get(connectionId);
        if (handler != null) {
            handler.send(msg);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void disconnect(int connectionId) {
        if (connectionsMap.containsKey(connectionId)) {
            connectionsMap.remove(connectionId);
        }
    }
}
