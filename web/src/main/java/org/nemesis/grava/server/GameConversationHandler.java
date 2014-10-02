package org.nemesis.grava.server;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by aalbul on 02.10.14.
 */
public final class GameConversationHandler {
    private GameConversationHandler() {
    }

    private static final Logger logger = LoggerFactory.getLogger(GameRepository.class);

    public static void apply(final GravaHalServer server) {
        server.getSocketServer().addEventListener("ready", Object.class, new DataListener<Object>() {
            @Override
            public void onData(SocketIOClient client, Object data, AckRequest ackSender) throws Exception {
                logger.debug("Client {} sent 'ready' event. Registering him to game.", client.getSessionId());
                Long userId = server.getRepository().registerSession(client);
                server.getFlow().registerUser(userId);
            }
        });
        server.getSocketServer().addEventListener("sow", Integer.class, new DataListener<Integer>() {
            @Override
            public void onData(SocketIOClient client, Integer index, AckRequest ackSender) throws Exception {
                Long userId = server.getRepository().userIdByClient(client);
                logger.debug("Client {} send sow request. Starting index is: {}", userId, index);
                server.getFlow().sow(userId, index);
            }
        });
    }
}
