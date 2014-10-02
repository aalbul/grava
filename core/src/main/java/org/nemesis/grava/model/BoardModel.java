package org.nemesis.grava.model;

import org.nemesis.grava.domain.PlayerBoardPart;
import org.nemesis.grava.domain.SessionState;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nuru on 10/1/14.
 */
public class BoardModel {
    private Integer boardSize;
    private Map<Long, BoardPartModel> boardParts;

    public BoardModel(SessionState state) {
        boardSize = state.getBoardSize();
        boardParts = new HashMap<>(2);
        for (Map.Entry<Long, PlayerBoardPart> part : state.getBoards().entrySet()) {
            boardParts.put(part.getKey(), new BoardPartModel(state, part.getValue()));
        }
    }

    public Integer getBoardSize() {
        return boardSize;
    }

    public Map<Long, BoardPartModel> getBoardParts() {
        return Collections.unmodifiableMap(boardParts);
    }
}
