package org.nemesis.grava.model;

import org.nemesis.grava.domain.PlayerBoardPart;
import org.nemesis.grava.domain.SessionState;

/**
 * Created by nuru on 10/1/14.
 */
public class BoardPartModel {
    private int[] pits;
    private int gravaHal;

    public BoardPartModel(SessionState state, PlayerBoardPart part) {
        pits = new int[state.getBoardSize()];
        for (int i = 0; i < part.getPits().length; i++) {
            pits[i] = part.getPits()[i].getStones();
        }
        gravaHal = part.getGravaHal().getStones();
    }

    public int[] getPits() {
        return pits;
    }

    public int getGravaHal() {
        return gravaHal;
    }
}
