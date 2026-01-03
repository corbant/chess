package chess;

import java.util.Arrays;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private static final int BOARD_ROWS = 8;
    private static final int BOARD_COLS = 8;
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
     * Removes a chess piece from the given position
     * 
     * @param position The position to remove the piece from
     */
    public void removePiece(ChessPosition position) {
        board[position.getRow() - 1][position.getColumn() - 1] = null;
    }

    /**
     * Moves a chess piece from one position to another
     * 
     * @param startPosition position of the piece to move
     * @param endPosition   location to move the piece to
     */
    public void movePiece(ChessPosition startPosition, ChessPosition endPosition) {
        ChessPiece piece = this.getPiece(startPosition);
        // pick up the piece and place it in the new location
        this.removePiece(endPosition);
        this.addPiece(endPosition, piece);
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        // create a new board
        this.board = new ChessPiece[ChessBoard.BOARD_ROWS][ChessBoard.BOARD_COLS];

        // add pieces
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
