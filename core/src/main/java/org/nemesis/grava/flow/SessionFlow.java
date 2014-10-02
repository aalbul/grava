package org.nemesis.grava.flow;

import org.nemesis.grava.domain.PlayerBoardPart;
import org.nemesis.grava.domain.SessionState;
import org.nemesis.grava.domain.SessionStep;
import org.nemesis.grava.flow.listener.SessionFlowListener;
import org.nemesis.grava.flow.listener.SessionFlowListenerAdapter;
import org.nemesis.grava.model.BoardModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nuru on 9/30/14.
 * <p/>
 * Represents game flow implementation with state, transitions and entry points
 */
public class SessionFlow {
    private final static Logger logger = LoggerFactory.getLogger(SessionFlow.class);
    protected SessionState state;
    protected SessionFlowListener listener;
    protected Integer boardSize;

    public SessionFlow(Integer boardSize, SessionFlowListener listener) {
        logger.debug("Initializing session flow with board size: {}", boardSize);
        this.boardSize = boardSize;
        state = new SessionState(boardSize);
        if (listener != null) {
            this.listener = listener;
        } else {
            this.listener = new SessionFlowListenerAdapter();
        }
    }

    /**
     * Adds user with specified id to game
     * When state reach 2 users, it moves forward
     *
     * @param id - user id
     */
    public void registerUser(Long id) {
        if (state.getStep() != SessionStep.INITIAL) {
            throw new IllegalStateException("No more user registrations allowed.");
        }
        logger.debug("Registering user with id: {}", id);
        state.registerUser(id);
        listener.userRegistered(id);
        startGame();
    }

    /**
     * Executes exact "sow" operation with such a flow:
     * <p/>
     * 1) Take stones from specified index, shift 1 item right and start sowing on the right.
     * 3) Check if other player's stones must be captured
     * 2) Check for game completion, "try again" case or turn change.
     *
     * @param userId        - acting user id
     * @param startingIndex - index from where to start sowing operation
     */
    public void sow(Long userId, Integer startingIndex) {
        if (!userId.equals(state.getCurrentUserTurn())) {
            throw new IllegalArgumentException("It's not your turn");
        }

        logger.debug("Sowing stones by user {}. Starting index is: {}", userId, startingIndex);
        PlayerBoardPart playerBoard = state.getBoardById(userId);
        PlayerBoardPart boardVersus = state.getBoardVersus(userId);

        Integer lastIndex = moveStones(playerBoard, startingIndex);
        logger.debug("Last stone's index is: {}", lastIndex);
        tryCaptureStones(playerBoard, boardVersus, lastIndex);

        printBoardsState();
        nextPhase(playerBoard, boardVersus, startingIndex);
    }

    /**
     * Moves stones from specified location on the right.
     * If amount of stones is larger then board size, this method will do as much circles, as needed.
     * It also populates grava hal during the pass through it
     *
     * @param playerBoard   - current user's board
     * @param startingIndex - index from where to start sowing operation
     * @return index of the last stone
     */
    public Integer moveStones(PlayerBoardPart playerBoard, Integer startingIndex) {
        Integer stonesToMove = playerBoard.pitAt(startingIndex).getStones();
        Integer deckSize = boardSize + 1; //with grava hal
        playerBoard.pitAt(startingIndex).clearStones();
        Integer currentPit = startingIndex;
        for (int i = 1; i <= stonesToMove; i++) {
            currentPit = (startingIndex + i) % deckSize;
            if (currentPit.equals(boardSize)) {
                playerBoard.getGravaHal().increment();
                currentPit = null;
            } else {
                playerBoard.pitAt(currentPit).increment();
            }
        }
        return currentPit;
    }

    /**
     * Transition to next phase depending on current boards state.
     * <p/>
     * There are 3 possible flows from here:
     * <p/>
     * 1) Game is completed
     * 2) Another try is given to current user
     * 3) Give turn to opposite user
     *
     * @param currentBoard  - current user's board
     * @param boardVersus   - opposite user's board
     * @param startingIndex - index from where to start sowing operation
     */
    private void nextPhase(PlayerBoardPart currentBoard, PlayerBoardPart boardVersus, Integer startingIndex) {
        sendCurrentBoardModel();
        if (currentBoard.isAllPitsEmpty()) {
            completeGame(currentBoard, boardVersus);
        } else {
            if (startingIndex.equals(0)) {
                anotherTry();
            } else {
                changeTurn(state.getUserVersus(state.getCurrentUserTurn()));
            }
        }
    }

    /**
     * Capture opposite user's stones if needed.
     * The only case when we capture them is when current user's last stone landed into his empty pit
     *
     * @param playerBoard - current user's board
     * @param boardVersus - opposite user's board
     * @param lastIndex   - index where last stone landed
     */
    private void tryCaptureStones(PlayerBoardPart playerBoard, PlayerBoardPart boardVersus, Integer lastIndex) {
        if (lastIndex != null && playerBoard.pitAt(lastIndex).getStones().equals(1)) {
            Integer stonesToCapture = boardVersus.pitAt(lastIndex).getStones();
            logger.debug("Last stone landed in own empty pit on index: {}. Capturing {} stones from opposite side.",
                    lastIndex, stonesToCapture);
            logger.debug("Boards state before capture changes is:");
            printBoardsState();
            playerBoard.getGravaHal().add(stonesToCapture);
            boardVersus.pitAt(lastIndex).clearStones();
        }
    }

    /**
     * Completes game by moving it into FINISHED state and notifying all participants
     */
    private void completeGame(PlayerBoardPart playerBoard, PlayerBoardPart boardVersus) {
        logger.debug("Current user doesn't have any more stines in his pits. Searching for winner.");
        Integer currentUserGravaHal = playerBoard.getGravaHal().getStones();
        Integer versusUserGravaHal = boardVersus.getGravaHal().getStones();
        Long winerId;
        if (currentUserGravaHal > versusUserGravaHal) {
            winerId = state.getCurrentUserTurn();
        } else {
            winerId = state.getUserVersus(state.getCurrentUserTurn());
        }
        logger.debug("Grava hals: Current user: [{}], opposite user: [{}]. Winner: {}", currentUserGravaHal,
                versusUserGravaHal, winerId);
        state.setStep(SessionStep.FINISHED);
        listener.gameCompleted(winerId);
    }

    /**
     * Begin game when session is staffed (2 users are present)
     * This method also rolling dice to understand who is moving first
     */
    private void startGame() {
        if (state.isSessionStaffed() && state.getStep() == SessionStep.INITIAL) {
            logger.debug("Session is staffed. Moving to started state.");
            state.setStep(SessionStep.STARTED);
            listener.sessionStarted();
            logger.debug("Rolling dice to understand who is first.");
            sendCurrentBoardModel();
            changeTurn(state.getRandomUserId());
        }
    }

    /**
     * Switch turn to specified user.
     * This method also notify user about his turn
     *
     * @param userId - desired user id
     */
    private void changeTurn(Long userId) {
        logger.debug("Changing user turn to user id: {}", userId);
        state.setTurn(userId);
        listener.userTurn(userId);
    }

    /**
     * Gives another try to user by notifying him and leaving flow in current transition
     */
    private void anotherTry() {
        logger.debug("Last user step is on Grava hal. Giving user {} another try.", state.getCurrentUserTurn());
        listener.anotherTry(state.getCurrentUserTurn());
    }

    /**
     * Broadcast current model state to listeners
     */
    private void sendCurrentBoardModel() {
        listener.newBoardModel(new BoardModel(state));
    }

    /**
     * Print current boards status
     */
    private void printBoardsState() {
        logger.debug("===================================================");
        logger.debug("Current user id is: {}", state.getCurrentUserTurn());
        logger.debug("Current user board state:\t{}", state.getBoardById(state.getCurrentUserTurn()));
        logger.debug("Versus user board state: \t{}", state.getBoardVersus(state.getCurrentUserTurn()));
        logger.debug("===================================================");
    }
}
