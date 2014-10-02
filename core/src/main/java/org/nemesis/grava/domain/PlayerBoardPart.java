package org.nemesis.grava.domain;

/**
 * Created by nuru on 9/30/14.
 */
public class PlayerBoardPart {
    private BoardPit[] pits;
    private BoardPit gravaHal = new BoardPit(0);

    public PlayerBoardPart(Integer size) {
        pits = new BoardPit[size];
        for (int i = 0; i < pits.length; i++) {
            pits[i] = new BoardPit(size);
        }
    }

    public BoardPit pitAt(Integer index) {
        return pits[index];
    }

    public BoardPit getGravaHal() {
        return gravaHal;
    }

    public Boolean isAllPitsEmpty() {
        for (BoardPit pit : pits) {
            if (!pit.getStones().equals(0)) {
                return false;
            }
        }
        return true;
    }

    public BoardPit[] getPits() {
        return pits;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (BoardPit pit : pits) {
            str.append("|\t").append(pit.getStones()).append("\t|");
        }
        str.append(">>").append(gravaHal.getStones()).append("<<");
        return str.toString();
    }
}
