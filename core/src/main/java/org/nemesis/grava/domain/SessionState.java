package org.nemesis.grava.domain;

import java.util.*;

/**
 * Created by nuru on 9/30/14.
 */
public class SessionState {
    private Integer boardSize;
    private Map<Long, PlayerBoardPart> boards = new HashMap<>();
    private SessionStep step = SessionStep.INITIAL;
    private Long currentUserTurn;

    public SessionState(Integer boardSize) {
        this.boardSize = boardSize;
    }

    public PlayerBoardPart getBoardById(Long userId) {
        return boards.get(userId);
    }

    public Long getUserVersus(Long userId) {
        for (Long user : boards.keySet()) {
            if (!user.equals(userId)) {
                return user;
            }
        }
        return null;
    }

    public PlayerBoardPart getBoardVersus(Long userId) {
        return boards.get(getUserVersus(userId));
    }

    public void registerUser(Long id) {
        if (boards.size() == 2) {
            throw new IllegalStateException("Session is already staffed.");
        }
        boards.put(id, new PlayerBoardPart(boardSize));
    }

    public boolean isSessionStaffed() {
        return boards.size() == 2;
    }

    public void setStep(SessionStep step) {
        this.step = step;
    }

    public SessionStep getStep() {
        return step;
    }

    public void setTurn(Long userId) {
        currentUserTurn = userId;
    }

    public Long getCurrentUserTurn() {
        return currentUserTurn;
    }

    public Long getRandomUserId() {
        Integer index = new Random().nextInt(boards.size());
        return new ArrayList<>(boards.keySet()).get(index);
    }

    public Map<Long, PlayerBoardPart> getBoards() {
        return Collections.unmodifiableMap(boards);
    }

    public Integer getBoardSize() {
        return boardSize;
    }
}
