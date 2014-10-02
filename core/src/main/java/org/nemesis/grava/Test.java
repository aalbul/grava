package org.nemesis.grava;

import org.nemesis.grava.flow.SessionFlow;
import org.nemesis.grava.flow.listener.CountingSessionFlowListener;
import org.nemesis.grava.flow.listener.SessionFlowListener;
import org.nemesis.grava.model.BoardModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nuru on 10/1/14.
 */
public class Test {
    public static void main(String[] args) {
        CountingSessionFlowListener listener = new CountingSessionFlowListener();
        SessionFlow flow = new SessionFlow(6, listener);
        flow.registerUser(1l);
        flow.registerUser(2l);
        flow.sow(listener.getCurrentUser(), 0);
        flow.sow(listener.getCurrentUser(), 1);
        flow.toString();
//
//        PlayerBoardPart part = new PlayerBoardPart(6);
//        part.pitAt(0).set(7);
//
//        Integer last = flow.moveStones(part, 0);
//        last.toString();

    }
}

class LoggingSessionFlowListener implements SessionFlowListener {
    final Logger logger = LoggerFactory.getLogger(LoggingSessionFlowListener.class);

    public Long current;

    @Override
    public void userRegistered(Long userId) {

    }

    @Override
    public void sessionStarted() {
        logger.info("Game started.");
    }

    @Override
    public void userTurn(Long userId) {
        current = userId;
        logger.info("User turn: {}", userId);
    }

    @Override
    public void anotherTry(Long userId) {
        logger.info("Another try for: {}", userId);
    }

    @Override
    public void gameCompleted(Long winnerId) {
        logger.info("Game completed. Winner is: {}", winnerId);
    }

    @Override
    public void newBoardModel(BoardModel model) {
    }
}
