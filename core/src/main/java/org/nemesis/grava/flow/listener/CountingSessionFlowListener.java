package org.nemesis.grava.flow.listener;

import org.nemesis.grava.model.BoardModel;

import java.util.*;

/**
 * Created by nuru on 10/1/14.
 */
public class CountingSessionFlowListener implements SessionFlowListener {
    private List<Long> userIds = new ArrayList<>(2);
    private Integer sessionStartedCount = 0;
    private Long currentUser;
    private Long initialUser;
    private Integer retryCount = 0;
    private Integer gameCompletedCount = 0;
    private Map<Long, Integer> userTurnsCount = new HashMap<>();
    private List<BoardModel> models = new ArrayList<>();

    @Override
    public void userRegistered(Long userId) {
        userIds.add(userId);
    }

    @Override
    public void sessionStarted() {
        sessionStartedCount++;
    }

    @Override
    public void userTurn(Long userId) {
        currentUser = userId;
        Integer currentCount = userTurnsCount.get(userId);
        if (currentCount == null) {
            currentCount = 0;
        }
        currentCount++;
        userTurnsCount.put(userId, currentCount);
        if (initialUser == null) {
            initialUser = userId;
        }
    }

    @Override
    public void anotherTry(Long userId) {
        retryCount++;
        userTurn(userId);
    }

    @Override
    public void gameCompleted(Long winnerId) {
        gameCompletedCount++;
    }

    @Override
    public void newBoardModel(BoardModel model) {
        models.add(model);
    }

    public List<Long> getUserIds() {
        return Collections.unmodifiableList(userIds);
    }

    public Integer getSessionStartedCount() {
        return sessionStartedCount;
    }

    public Long getCurrentUser() {
        return currentUser;
    }

    public Long getInitialUser() {
        return initialUser;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public Integer getGameCompletedCount() {
        return gameCompletedCount;
    }

    public Map<Long, Integer> getUserTurnsCount() {
        return Collections.unmodifiableMap(userTurnsCount);
    }

    public List<BoardModel> getModels() {
        return Collections.unmodifiableList(models);
    }
}
