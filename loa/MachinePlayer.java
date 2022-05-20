/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import static loa.Piece.*;
import static loa.Square.*;

import java.util.List;

/** An automated Player.
 *  @author Edan Bash
 */
class MachinePlayer extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new MachinePlayer with no piece or controller (intended to produce
     *  a template). */
    MachinePlayer() {
        this(null, null);
    }

    /** A MachinePlayer that plays the SIDE pieces in GAME. */
    MachinePlayer(Piece side, Game game) {
        super(side, game);
    }

    @Override
    String getMove() {
        Move choice;

        assert side() == getGame().getBoard().turn();
        int depth;
        choice = searchForMove();
        getGame().reportMove(choice);
        return choice.toString();
    }

    @Override
    Player create(Piece piece, Game game) {
        return new MachinePlayer(piece, game);
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move after searching the game tree to DEPTH>0 moves
     *  from the current position. Assumes the game is not over. */
    private Move searchForMove() {
        Board work = new Board(getBoard());
        int value;
        assert side() == work.turn();
        _foundMove = null;
        if (side() == WP) {
            value = findMove(work, chooseDepth(), true, 1, -INFTY, INFTY);
        } else {
            value = findMove(work, chooseDepth(), true, -1, -INFTY, INFTY);
        }
        return _foundMove;
    }

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        if (depth == 0 || board.gameOver()) {
            return simpleFindMove(board, sense, alpha, beta);
        }

        int bestSoFar;
        if (sense == 1) {
            bestSoFar = -INFTY;
        } else {
            bestSoFar = INFTY;
        }

        for (Move m: board.legalMoves()) {
            board.makeMove(m);
            int next = heuristicEst(board);
            int response = findMove(board, depth - 1, false,
                    -sense, alpha, beta);
            board.retract();
            if (sense == 1) {
                if (response >= bestSoFar) {
                    if (saveMove) {
                        _foundMove = m;
                    }
                    bestSoFar = next;
                    alpha = Math.max(alpha, response);
                }
            } else {
                if (response <= bestSoFar) {
                    if (saveMove) {
                        _foundMove = m;
                    }
                    bestSoFar = next;
                    beta = Math.min(beta, response);
                }
            }
            if (beta <= alpha) {
                break;
            }
        }
        return bestSoFar;
    }

    /** Find a move from position BOARD and return a maximal value or
     * have value > BETA if SENSE==1, and minimal value or value < ALPHA
     * if SENSE==-1. Any move with value >= BETA is also "good enough". */
    private int simpleFindMove(Board board, int sense, int alpha, int beta) {
        if (board.winner() == BP) {
            return -WINNING_VALUE;
        } else if (board.winner() == WP) {
            return WINNING_VALUE;
        }

        int bestSoFar;
        if (sense == 1) {
            bestSoFar = -INFTY;
        } else {
            bestSoFar = INFTY;
        }

        for (Move m: board.legalMoves()) {
            board.makeMove(m);
            int next = heuristicEst(board);
            board.retract();
            if (sense == 1) {
                if (next >= bestSoFar) {
                    bestSoFar = next;
                    alpha = Math.max(alpha, next);
                }
            } else {
                if (next <= bestSoFar) {
                    bestSoFar = next;
                    beta = Math.min(beta, next);
                }
            }
            if (beta <= alpha) {
                break;
            }
        }
        return bestSoFar;
    }


    /** Return a search depth for the current position. */
    private int chooseDepth() {
        return 3;
    }

    /** Returns heuristic value of current BOARD. This value
     * is based on which pieces are in center of board. */
    private int heuristicEst(Board board) {
        int est = 0;
        for (int c = 2; c < 6; c += 1) {
            for (int r = 2; r < 6; r += 1) {
                if (board.get(sq(c, r)) == WP) {
                    est += 1;
                } else if (board.get(sq(c, r)) == BP) {
                    est -= 1;
                }
            }
        }

        List<Integer> whiteMoves = board.getRegionSizes(WP);
        List<Integer> blackMoves = board.getRegionSizes(BP);

        int sumWhite = 0;
        for (int i: whiteMoves) {
            sumWhite += i;\
        }
        if (sumWhite < 6) {
            est += whiteMoves.get(0);
        }

        int sumBlack = 0;
        for (int i: blackMoves) {
            sumBlack += i;
        }
        if (sumBlack < 6) {
            est -= blackMoves.get(0);
        }

        return est;
    }

    /** Used to convey moves discovered by findMove. */
    private Move _foundMove;

}
