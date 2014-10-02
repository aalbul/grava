package org.nemesis.grava.server;

import com.corundumstudio.socketio.SocketIOClient;
import org.nemesis.grava.flow.listener.SessionFlowListener;
import org.nemesis.grava.model.BoardModel;
import org.nemesis.grava.server.messages.GameInfo;

/**
 * Created by aalbul on 02.10.14.
 */
public class GameFlowListener implements SessionFlowListener {
    private GravaHalServer server;

    public GameFlowListener(GravaHalServer server) {
        this.server = server;
    }

    @Override
    public void userRegistered(Long userId) {
        server.getRepository().get(userId).sendEvent("registered", userId);
    }

    @Override
    public void sessionStarted() {
        for (SocketIOClient client : server.getRepository().getUserSessions().values()) {
            Long currentUser = server.getRepository().userIdByClient(client);
            Long oppositeUser = server.getRepository().oppositeUserId(currentUser);
            client.sendEvent("game-started", new GameInfo(currentUser, oppositeUser));
        }
    }

    @Override
    public void userTurn(Long userId) {
        server.getRepository().getUserSessions().get(userId).sendEvent("your-turn");
    }

    @Override
    public void anotherTry(Long userId) {
        server.getRepository().getUserSessions().get(userId).sendEvent("another-try");
    }

    @Override
    public void gameCompleted(Long winnerId) {
        for (SocketIOClient client : server.getRepository().getUserSessions().values()) {
            client.sendEvent("game-completed", winnerId);
        }
    }

    @Override
    public void newBoardModel(BoardModel model) {
        for (SocketIOClient client : server.getRepository().getUserSessions().values()) {
            client.sendEvent("board-update", model);
        }
    }
}
