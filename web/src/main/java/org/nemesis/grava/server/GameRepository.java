package org.nemesis.grava.server;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by aalbul on 02.10.14.
 */
public class GameRepository implements ConnectListener, DisconnectListener {
    private static final Logger logger = LoggerFactory.getLogger(GameRepository.class);
    private GravaHalServer server;
    private Map<Long, SocketIOClient> userSessions = new HashMap<>();

    public GameRepository(GravaHalServer server) {
        this.server = server;
        server.getSocketServer().addConnectListener(this);
        server.getSocketServer().addDisconnectListener(this);
    }

    @Override
    public void onConnect(SocketIOClient client) {
        logger.debug("Client {} is trying to connect. Checking for free slots.", client.getSessionId());
        if (userSessions.size() >= 2) {
            logger.debug("Not more free slots. Dropping connection.");
            client.disconnect();
        }
    }

    @Override
    public void onDisconnect(SocketIOClient client) {
        logger.debug("Client {} disconnected. Checking if he was a participant.", client.getSessionId());
        if (userIdByClient(client) != null) {
            logger.debug("Participant disconnected. Resetting game.");
            Iterator<SocketIOClient> iterator = userSessions.values().iterator();
            SocketIOClient c = iterator.next();
            if (c.isChannelOpen()) {
                c.disconnect();
            }
            iterator.remove();
            server.startFlow();
        }
    }

    public Long registerSession(SocketIOClient client) {
        Long userId = System.currentTimeMillis();
        userSessions.put(userId, client);
        return userId;
    }

    public SocketIOClient get(Long id) {
        return userSessions.get(id);
    }

    public Map<Long, SocketIOClient> getUserSessions() {
        return Collections.unmodifiableMap(userSessions);
    }

    public Long userIdByClient(SocketIOClient client) {
        for (Map.Entry<Long, SocketIOClient> c : userSessions.entrySet()) {
            if (c.getValue().getSessionId().equals(client.getSessionId())) {
                return c.getKey();
            }
        }
        return null;
    }

    public Long oppositeUserId(Long userId) {
        for (Long current : userSessions.keySet()) {
            if (!current.equals(userId)) {
                return current;
            }
        }
        return null;
    }
}
