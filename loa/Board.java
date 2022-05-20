/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import java.util.regex.Pattern;

import static loa.Piece.*;
import static loa.Square.*;

/** Represents the state of a game of Lines of Action.
 *  @author Edan Bash
 */
class Board {

    /** Default number of moves for each side that results in a draw. */
    static final int DEFAULT_MOVE_LIMIT = 60;

    /** Pattern describing a valid square designator (cr). */
    static final Pattern ROW_COL = Pattern.compile("^[a-h][1-8]$");

    /** A Board whose initial contents are taken from INITIALCONTENTS
     *  and in which the player playing TURN is to move. The resulting
     *  Board has
     *        get(col, row) == INITIALCONTENTS[row][col]
     *  Assumes that PLAYER is not null and INITIALCONTENTS is 8x8.
     *
     *  CAUTION: The natural written notation for arrays initializers puts
     *  the BOTTOM row of INITIALCONTENTS at the top.
     */
    Board(Piece[][] initialContents, Piece turn) {
        initialize(initialContents, turn);
    }

    /** A new board in the standard initial position. */
    Board() {
        this(INITIAL_PIECES, BP);
    }

    /** A Board whose initial contents and state are copied from
     *  BOARD. */
    Board(Board board) {
        this();
        copyFrom(board);
    }

    /** Set my state to CONTENTS with SIDE to move. */
    void initialize(Piece[][] contents, Piece side) {
        int boardIndex = 0;
        for (int r = 0; r < contents.length; r += 1) {
            for (int c = 0; c < contents[0].length; c += 1) {
                _board[boardIndex] = contents[r][c];
                boardIndex += 1;
            }
        }
        _turn = side;
        setMoveLimit(DEFAULT_MOVE_LIMIT);
        _moves.clear();
        _subsetsInitialized = false;
    }

    /** Set me to the initial configuration. */
    void clear() {
        initialize(INITIAL_PIECES, BP);
    }

    /** Set my state to a copy of BOARD. */
    void copyFrom(Board board) {
        if (board == this) {
            return;
        }
        for (int i = 0; i < board._board.length; i += 1) {
            _board[i] = board._board[i];
        }
        _moves.addAll(board._moves);
        _turn = board._turn;
        _moveLimit = board._moveLimit;
    }

    /** Return the contents of the square at SQ. */
    Piece get(Square sq) {
        return _board[sq.index()];
    }

    /** Set the square at SQ to V and set the side that is to move next
     *  to NEXT, if NEXT is not null. */
    void set(Square sq, Piece v, Piece next) {
        _board[sq.index()] = v;
        if (next != null) {
            _turn = next;
        }
        _subsetsInitialized = false;
        _winnerKnown = false;
    }

    /** Set the square at SQ to V, without modifying the side that
     *  moves next. */
    void set(Square sq, Piece v) {
        set(sq, v, null);
    }

    /** Set limit on number of moves (before tie results) to LIMIT. */
    void setMoveLimit(int limit) {
        _moveLimit = limit;
        _winnerKnown = false;
    }

    /** Assuming isLegal(MOVE), make MOVE. Assumes MOVE.isCapture()
     *  is false. */
    void makeMove(Move move) {
        assert isLegal(move);
        if (get(move.getTo()) != EMP) {
            _moves.add(move.captureMove());
        } else {
            _moves.add(move);
        }
        set(move.getFrom(), EMP);
        set(move.getTo(), _turn);
        _turn = _turn.opposite();
        _subsetsInitialized = false;
        _winnerKnown = false;
    }

    /** Retract (unmake) one move, returning to the state immediately before
     *  that move.  Requires that movesMade () > 0. */
    void retract() {
        assert movesMade() > 0;
        Move lastMove = _moves.remove(_moves.size() - 1);
        if (lastMove.isCapture()) {
            set(lastMove.getTo(), _turn);
        } else {
            set(lastMove.getTo(), EMP);
        }
        set(lastMove.getFrom(), _turn.opposite());
        _turn = _turn.opposite();
        _subsetsInitialized = false;
        _winnerKnown = false;
    }

    /** Return the Piece representing who is next to move. */
    Piece turn() {
        return _turn;
    }

    /** Return true iff FROM - TO is a legal move for the player currently on
     *  move. */
    boolean isLegal(Square from, Square to) {
        if (!from.isValidMove(to) || blocked(from, to)) {
            return false;
        }
        int dir = from.direction(to);
        return to == from.moveDest(dir, loa(from, dir));
    }

    /** Returns number of squares on loa defined by FROM and DIR. */
    private int loa(Square from, int dir) {
        return countSqs(from, dir)
                + countSqs(from, (dir + 4) % 8) - 1;
    }

    /** Returns number of squares on board in DIR of FROM. */
    private int countSqs(Square from, int dir) {
        Square sq = from;
        int count = 0;
        while (sq != null) {
            if (get(sq) != EMP) {
                count += 1;
            }
            sq = sq.adjacent()[dir];
        }
        return count;
    }

    /** Return true iff MOVE is legal for the player currently on move.
     *  The isCapture() property is ignored. */
    boolean isLegal(Move move) {
        return isLegal(move.getFrom(), move.getTo());
    }

    /** Return a sequence of all legal moves from this position. */
    List<Move> legalMoves() {
        List<Move> moves = new ArrayList<>();
        for (Square from: ALL_SQUARES) {
            if (get(from) == _turn) {
                for (Square to: ALL_SQUARES) {
                    if (isLegal(from, to)) {
                        moves.add(Move.mv(from, to));
                    }
                }
            }
        }
        return moves;

    }

    /** Return true iff the game is over (either player has all his
     *  pieces continguous or there is a tie). */
    boolean gameOver() {
        return winner() != null;
    }

    /** Return true iff SIDE's pieces are continguous. */
    boolean piecesContiguous(Piece side) {
        return getRegionSizes(side).size() == 1;
    }

    /** Return the winning side, if any.  If the game is not over, result is
     *  null.  If the game has ended in a tie, returns EMP. */
    Piece winner() {
        if (!_winnerKnown) {
            boolean bCon = piecesContiguous(BP);
            boolean wCon = piecesContiguous(WP);
            if (movesMade() >= _moveLimit && !bCon && !wCon) {
                _winner = EMP;
            } else if (bCon && wCon) {
                _winner =  _turn.opposite();
            } else if (bCon) {
                _winner = BP;
            } else if (wCon) {
                _winner =  WP;
            } else {
                return null;
            }
            _winnerKnown = true;
        }
        return _winner;
    }

    /** Return the total number of moves that have been made (and not
     *  retracted).  Each valid call to makeMove with a normal move increases
     *  this number by 1. */
    int movesMade() {
        return _moves.size();
    }

    @Override
    public boolean equals(Object obj) {
        Board b = (Board) obj;
        return Arrays.deepEquals(_board, b._board) && _turn == b._turn;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(_board) * 2 + _turn.hashCode();
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===%n");
        for (int r = BOARD_SIZE - 1; r >= 0; r -= 1) {
            out.format("    ");
            for (int c = 0; c < BOARD_SIZE; c += 1) {
                out.format("%s ", get(sq(c, r)).abbrev());
            }
            out.format("%n");
        }
        out.format("Next move: %s%n===", turn().fullName());
        return out.toString();
    }

    /** Return true if a move from FROM to TO is blocked by an opposing
     *  piece or by a friendly piece on the target square. */
    private boolean blocked(Square from, Square to) {
        if (get(to) == _turn) {
            return true;
        }
        int dir = from.direction(to);
        Square sq = from;
        while (sq != to) {
            if (get(sq) == _turn.opposite()) {
                return true;
            }
            sq = sq.adjacent()[dir];
        }
        return false;
    }

    /** Return the size of the as-yet unvisited cluster of squares
     *  containing P at and adjacent to SQ.  VISITED indicates squares that
     *  have already been processed or are in different clusters.  Update
     *  VISITED to reflect squares counted. */
    private int numContig(Square sq, boolean[][] visited, Piece p) {
        visited[sq.row()][sq.col()] = true;
        if (p == EMP) {
            return 0;
        }
        int count = 1;
        for (Square adj: sq.adjacent()) {
            if (adj == null) {
                continue;
            } else if (!visited[adj.row()][adj.col()] && get(adj) == p) {
                count += numContig(adj, visited, get(adj));
            }
        }
        return count;
    }

    /** Set the values of _whiteRegionSizes and _blackRegionSizes. */
    private void computeRegions() {
        if (_subsetsInitialized) {
            return;
        }
        _whiteRegionSizes.clear();
        _blackRegionSizes.clear();

        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
        for (Square sq: ALL_SQUARES) {
            if (!visited[sq.row()][sq.col()]) {
                if (get(sq) == BP) {
                    _blackRegionSizes.add(numContig(sq, visited, get(sq)));
                } else if (get(sq) == WP) {
                    _whiteRegionSizes.add(numContig(sq, visited, get(sq)));
                }
            }
        }

        Collections.sort(_whiteRegionSizes, Collections.reverseOrder());
        Collections.sort(_blackRegionSizes, Collections.reverseOrder());
        _subsetsInitialized = true;
    }

    /** Return the sizes of all the regions in the current union-find
     *  structure for side S. */
    List<Integer> getRegionSizes(Piece s) {
        computeRegions();
        if (s == WP) {
            return _whiteRegionSizes;
        } else {
            return _blackRegionSizes;
        }
    }

    /** The standard initial configuration for Lines of Action (bottom row
     *  first). */
    static final Piece[][] INITIAL_PIECES = {
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP }
    };

    /** Current contents of the board.  Square S is at _board[S.index()]. */
    private final Piece[] _board = new Piece[BOARD_SIZE  * BOARD_SIZE];

    /** List of all unretracted moves on this board, in order. */
    private final ArrayList<Move> _moves = new ArrayList<>();
    /** Current side on move. */
    private Piece _turn;
    /** Limit on number of moves before tie is declared.  */
    private int _moveLimit;
    /** True iff the value of _winner is known to be valid. */
    private boolean _winnerKnown;
    /** Cached value of the winner (BP, WP, EMP (for tie), or null (game still
     *  in progress).  Use only if _winnerKnown. */
    private Piece _winner;

    /** True iff subsets computation is up-to-date. */
    private boolean _subsetsInitialized;

    /** List of the sizes of continguous clusters of pieces, by color. */
    private final ArrayList<Integer>
        _whiteRegionSizes = new ArrayList<>(),
        _blackRegionSizes = new ArrayList<>();
}
