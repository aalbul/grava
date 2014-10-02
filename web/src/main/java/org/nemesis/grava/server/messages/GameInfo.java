package org.nemesis.grava.server.messages;

/**
 * Created by nuru on 10/2/14.
 */
public class GameInfo {
    private Long yourId;
    private Long opponentId;

    public GameInfo(Long yourId, Long opponentId) {
        this.yourId = yourId;
        this.opponentId = opponentId;
    }

    public Long getYourId() {
        return yourId;
    }

    public Long getOpponentId() {
        return opponentId;
    }
}
