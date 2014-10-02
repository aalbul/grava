package org.nemesis.grava.flow;

import org.junit.Before;
import org.junit.Test;
import org.nemesis.grava.domain.PlayerBoardPart;
import org.nemesis.grava.flow.listener.CountingSessionFlowListener;
import org.nemesis.grava.model.BoardModel;
import org.nemesis.grava.model.BoardPartModel;

import static org.junit.Assert.*;

/**
 * Created by nuru on 10/1/14.
 */
public class SessionFlowTest {
    private SessionFlow flow;
    private CountingSessionFlowListener listener;

    @Before
    public void init() {
        listener = new CountingSessionFlowListener();
        flow = new SessionFlow(6, listener);
    }

    @Test
    public void testRegisterUsersAndStartSession() {
        registerUsers();
        assertEquals("Session isn't started", new Integer(1), listener.getSessionStartedCount());
        assertNotNull("User turn callback didn't call.", listener.getCurrentUser());
        assertNoRetries();
        assertGameNotCompleted();
        assertEquals("Model's initial state was not sent to listener.", 1, listener.getModels().size());
        BoardModel firstModel = listener.getModels().iterator().next();
        assertEquals("Invalid board size sent.", new Integer(6), firstModel.getBoardSize());
        assertBoardPartForUser(firstModel, 1l, 0, new int[]{6, 6, 6, 6, 6, 6});
        assertBoardPartForUser(firstModel, 2l, 0, new int[]{6, 6, 6, 6, 6, 6});
    }

    @Test(expected = IllegalStateException.class)
    public void testRegisterThirdUser() {
        registerUsers();
        flow.registerUser(3l);
    }

    @Test
    public void testSimpleSow() {
        registerUsers();
        flow.sow(listener.getCurrentUser(), 1);
        assertNoRetries();
        assertGameNotCompleted();
        assertModelCallbacksSize(2);
        assertUserTurnIsChanged();
        assertUserTurnCount(1l, 1);
        assertUserTurnCount(2l, 1);
        BoardModel modelAfterSow = listener.getModels().get(listener.getModels().size() - 1);
        assertBoardPartForUser(modelAfterSow, getOppositeId(), 1, new int[]{7, 0, 7, 7, 7, 7});
        assertBoardPartForUser(modelAfterSow, listener.getCurrentUser(), 0, new int[]{6, 6, 6, 6, 6, 6});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSowByWrongUser() {
        registerUsers();
        flow.sow(getOppositeId(), 1);
    }

    @Test
    public void testTryAgainCase() {
        registerUsers();
        flow.sow(listener.getCurrentUser(), 0);
        assertRetriesCount(1);
        assertGameNotCompleted();
        assertModelCallbacksSize(2);
        assertUserTurnIsNotChanged();
        assertUserTurnCount(listener.getCurrentUser(), 2);
        assertUserTurnCount(getOppositeId(), 0);
        BoardModel modelAfterRetry = listener.getModels().get(listener.getModels().size() - 1);
        assertBoardPartForUser(modelAfterRetry, listener.getCurrentUser(), 1, new int[]{0, 7, 7, 7, 7, 7});
        assertBoardPartForUser(modelAfterRetry, getOppositeId(), 0, new int[]{6, 6, 6, 6, 6, 6});
    }

    @Test
    public void testCaptureStonesCase() {
        registerUsers();
        flow.sow(listener.getCurrentUser(), 0);
        flow.sow(listener.getCurrentUser(), 1);
        assertRetriesCount(1);
        assertGameNotCompleted();
        assertModelCallbacksSize(3);
        assertUserTurnIsChanged();
        assertUserTurnCount(listener.getCurrentUser(), 1);
        assertUserTurnCount(getOppositeId(), 2);
        BoardModel modelAfterSecondSow = listener.getModels().get(listener.getModels().size() - 1);
        assertBoardPartForUser(modelAfterSecondSow, getOppositeId(), 8, new int[]{1, 1, 8, 8, 8, 8});
        assertBoardPartForUser(modelAfterSecondSow, listener.getCurrentUser(), 0, new int[]{6, 0, 6, 6, 6, 6});
    }

    @Test
    public void testCompleteGame() {
        registerUsers();
        //Emulating convenient case to complete the game
        PlayerBoardPart currentPart = flow.state.getBoardById(listener.getCurrentUser());
        for (int i = 0; i < flow.boardSize; i++) {
            currentPart.pitAt(i).clearStones();
        }
        currentPart.pitAt(5).set(1);
        PlayerBoardPart opositePart = flow.state.getBoardById(getOppositeId());
        opositePart.getGravaHal().add(100);
        flow.sow(listener.getCurrentUser(), 5);

        assertNoRetries();
        assertGameCompleted();
        assertModelCallbacksSize(2);
        assertUserTurnIsNotChanged();
        assertUserTurnCount(listener.getCurrentUser(), 1);
        assertUserTurnCount(getOppositeId(), 0);
        BoardModel completedModel = listener.getModels().get(listener.getModels().size() - 1);
        assertBoardPartForUser(completedModel, listener.getCurrentUser(), 1, new int[]{0, 0, 0, 0, 0, 0});
        assertBoardPartForUser(completedModel, getOppositeId(), 100, new int[]{6, 6, 6, 6, 6, 6});
        assertEquals(new Integer(1), listener.getSessionStartedCount());
    }

    private void registerUsers() {
        flow.registerUser(1l);
        flow.registerUser(2l);
    }

    private void assertUserTurnCount(Long userId, Integer count) {
        Integer turnCount = listener.getUserTurnsCount().get(userId);
        if (turnCount == null) {
            turnCount = 0;
        }
        assertEquals("Expected " + count + " turns of user " + userId, count, turnCount);
    }

    private void assertModelCallbacksSize(int expected) {
        assertEquals("Expected " + expected + " but found " + listener.getModels().size(), expected, listener.getModels().size());
    }

    private void assertNoRetries() {
        assertEquals("No another try callback expected but found.", new Integer(0), listener.getRetryCount());
    }

    private void assertRetriesCount(Integer count) {
        assertEquals("Invalid retries count.", count, listener.getRetryCount());
    }

    private void assertGameNotCompleted() {
        assertEquals("No game completed callback expected but found.", new Integer(0), listener.getGameCompletedCount());
    }

    private void assertGameCompleted() {
        assertEquals("Game is not completed.", new Integer(1), listener.getGameCompletedCount());
    }

    private void assertUserTurnIsChanged() {
        assertNotEquals("User turn is not changed", listener.getInitialUser(), listener.getCurrentUser());
    }

    private void assertUserTurnIsNotChanged() {
        assertEquals("User turn is changed", listener.getInitialUser(), listener.getCurrentUser());
    }

    private void assertBoardPartForUser(BoardModel model, Long user, Integer gravaHal, int[] pits) {
        BoardPartModel partModel = model.getBoardParts().get(user);
        assertNotNull("No board part found for user " + user, partModel);
        assertEquals("Grava Hal size is not equal", (int) gravaHal, partModel.getGravaHal());
        assertArrayEquals(pits, partModel.getPits());
    }

    private Long getOppositeId() {
        for (Long current : listener.getUserIds()) {
            if (!current.equals(listener.getCurrentUser())) {
                return current;
            }
        }
        return null;
    }
}

