package chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import chess.ChessGame.TeamColor;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    public static final int BOARD_ROWS = 8;
    public static final int BOARD_COLS = 8;
    private ChessPiece[][] board;

    public ChessBoard() {
        this.board = new ChessPiece[ChessBoard.BOARD_ROWS][ChessBoard.BOARD_COLS];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     *         position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Gets the position of the specified team's king
     * 
     * @param teamColor which team's king to search for
     * @return location of the team's king
     */
    public ChessPosition getKingPosition(TeamColor teamColor) {
        ChessPiece king = new ChessPiece(teamColor, ChessPiece.PieceType.KING);
        ChessPosition position = new ChessPosition(1, 1);
        for (int row = 1; row <= ChessBoard.BOARD_ROWS; row++) {
            for (int col = 1; col <= ChessBoard.BOARD_COLS; col++) {
                position = new ChessPosition(row, col);
                if (getPiece(position) == king)
                    break;
            }
        }
        return position;
    }

    public Collection<ChessPosition> getAllPiecePositions() {
        ArrayList<ChessPosition> piecePositions = new ArrayList<>();
        ChessPosition position;
        for (int row = 1; row <= ChessBoard.BOARD_ROWS; row++) {
            for (int col = 1; col <= ChessBoard.BOARD_COLS; col++) {
                position = new ChessPosition(row, col);
                if (getPiece(position) != null) {
                    piecePositions.add(position);
                }
            }
        }
        return piecePositions;
    }

    /**
     * Removes a chess piece from the given position
     * 
     * @param position The position to remove the piece from
     */
    public void removePiece(ChessPosition position) {
        ChessPiece piece = getPiece(position);
        if (piece == null)
            return;
        addPiece(position, null);
    }

    /**
     * Moves a chess piece from one position to another
     * 
     * 
     */
    public void movePiece(ChessMove move) {
        ChessPiece piece = this.getPiece(move.getStartPosition());
        if (move.getPromotionPiece() != null) {
            piece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }
        // pick up the piece and place it in the new location, removing the piece
        // already there if there is already one
        this.removePiece(move.getStartPosition());
        this.addPiece(move.getEndPosition(), piece);
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        // create a new board
        this.board = new ChessPiece[ChessBoard.BOARD_ROWS][ChessBoard.BOARD_COLS];

        // add pieces for both teams
        for (ChessGame.TeamColor teamColor : ChessGame.TeamColor.values()) {
            boolean isWhite = teamColor == ChessGame.TeamColor.WHITE;
            int row = isWhite ? 0 : ChessBoard.BOARD_ROWS - 1;

            // rooks
            board[row][0] = new ChessPiece(teamColor, ChessPiece.PieceType.ROOK);
            board[row][ChessBoard.BOARD_COLS - 1] = new ChessPiece(teamColor, ChessPiece.PieceType.ROOK);

            // knights
            board[row][1] = new ChessPiece(teamColor, ChessPiece.PieceType.KNIGHT);
            board[row][ChessBoard.BOARD_COLS - 1 - 1] = new ChessPiece(teamColor, ChessPiece.PieceType.KNIGHT);

            // bishops
            board[row][2] = new ChessPiece(teamColor, ChessPiece.PieceType.BISHOP);
            board[row][ChessBoard.BOARD_COLS - 1 - 2] = new ChessPiece(teamColor, ChessPiece.PieceType.BISHOP);

            // royalty
            board[row][3] = new ChessPiece(teamColor, ChessPiece.PieceType.QUEEN);
            board[row][ChessBoard.BOARD_COLS - 1 - 3] = new ChessPiece(teamColor, ChessPiece.PieceType.KING);

            // pawns
            row += isWhite ? 1 : -1;
            for (int i = 0; i < ChessBoard.BOARD_COLS; i++) {
                board[row][i] = new ChessPiece(teamColor, ChessPiece.PieceType.PAWN);
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.deepHashCode(board);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ChessBoard other = (ChessBoard) obj;
        if (!Arrays.deepEquals(board, other.board))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ChessBoard [board=" + Arrays.toString(board) + "]";
    }
}
