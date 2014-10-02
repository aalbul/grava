package org.nemesis.grava.domain;

/**
 * Created by nuru on 9/30/14.
 */
public class BoardPit {
    private Integer stones;

    public BoardPit(Integer stones) {
        this.stones = stones;
    }

    public Integer getStones() {
        return stones;
    }

    public void clearStones() {
        stones = 0;
    }

    public void increment() {
        stones++;
    }

    public void add(Integer amount) {
        stones += amount;
    }

    public void set(Integer amount) {
        stones = amount;
    }
}
