package org.nemesis.grava.flow.listener;

import org.nemesis.grava.model.BoardModel;

/**
 * Created by nuru on 9/30/14.
 */
public class SessionFlowListenerAdapter implements SessionFlowListener {
    @Override
    public void userRegistered(Long userId) {
    }

    @Override
    public void sessionStarted() {
    }

    @Override
    public void userTurn(Long userId) {
    }

    @Override
    public void anotherTry(Long userId) {
    }

    @Override
    public void gameCompleted(Long winnerId) {
    }

    @Override
    public void newBoardModel(BoardModel model) {
    }
}
