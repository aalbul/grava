package org.nemesis.grava.server;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import org.nemesis.grava.flow.SessionFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Created by aalbul on 02.10.14.
 */
@Service
@SuppressWarnings("unused")
public class GravaHalServer {
    private static final Logger logger = LoggerFactory.getLogger(GravaHalServer.class);
    private SocketIOServer server;
    private SessionFlow flow;
    private GameRepository repository;

    @PostConstruct
    private void init() {
        Configuration configuration = new Configuration();
        configuration.setPort(3001);
        server = new SocketIOServer(configuration);
        repository = new GameRepository(this);
        GameConversationHandler.apply(this);
        startFlow();
        server.start();
    }

    @PreDestroy
    private void cleanUp() {
        for (SocketIOClient client : repository.getUserSessions().values()) {
            if (client.isChannelOpen()) {
                client.disconnect();
            }
        }
        server.stop();
    }

    public SocketIOServer getSocketServer() {
        return server;
    }

    public GameRepository getRepository() {
        return repository;
    }

    public SessionFlow getFlow() {
        return flow;
    }

    public void startFlow() {
        flow = new SessionFlow(6, new GameFlowListener(this));
    }
}
