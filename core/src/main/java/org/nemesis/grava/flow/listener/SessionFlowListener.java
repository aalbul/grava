package org.nemesis.grava.flow.listener;

import org.nemesis.grava.model.BoardModel;

/**
 * Created by nuru on 9/30/14.
 * <p/>
 * We use listener approach to decouple flow business logic from actual presentation layer
 * This gives us protocol - agnostic capabilities
 */
public interface SessionFlowListener {
    public void userRegistered(Long userId);

    public void sessionStarted();

    public void userTurn(Long userId);

    public void anotherTry(Long userId);

    public void gameCompleted(Long winnerId);

    public void newBoardModel(BoardModel model);
}
