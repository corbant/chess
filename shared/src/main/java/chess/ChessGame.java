package chess;

import java.util.ArrayList;
import java.util.Collection;

import chess.ChessPiece.PieceType;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor currentTeamTurn;
    private ChessBoard board;

    public ChessGame() {
        // white goes first
        this.currentTeamTurn = TeamColor.WHITE;
        board = new ChessBoard();
        // setup board for initial game
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.currentTeamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTeamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     *         startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null)
            return null;
        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);
        // remove any moves that put the king in danger
        possibleMoves.removeIf(move -> {
            ChessBoard resultingBoard = this.board.clone();
            resultingBoard.movePiece(move);
            return isInCheck(piece.getTeamColor(), resultingBoard);
        });

        return possibleMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null)
            throw new InvalidMoveException("No piece at start position");
        else if (piece.getTeamColor() != this.currentTeamTurn)
            throw new InvalidMoveException("Not your turn");

        Collection<ChessMove> validMoves = this.validMoves(move.getStartPosition());
        if (!validMoves.contains(move))
            throw new InvalidMoveException("Move is invalid");

        board.movePiece(move);
        this.currentTeamTurn = currentTeamTurn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(teamColor, this.board);
    }

    private boolean isInCheck(TeamColor teamColor, ChessBoard board) {
        ChessPosition kingPosition = getKingPosition(teamColor, board);
        Collection<ChessMove> allOpponentMoves = getAllTeamMoves(
                teamColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE, board);
        for (ChessMove move : allOpponentMoves) {
            if (move.getEndPosition().equals(kingPosition))
                return true;
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && !teamHasValidMoves(teamColor, this.board);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && !teamHasValidMoves(teamColor, this.board);
    }

    private boolean teamHasValidMoves(TeamColor teamColor, ChessBoard board) {
        Collection<ChessPosition> teamPiecePositions = getAllTeamPiecePositions(teamColor, board);
        for (ChessPosition position : teamPiecePositions) {
            if (!validMoves(position).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }

    /**
     * Gets the position of the specified team's king
     * 
     * @param teamColor which team's king to search for
     * @return location of the team's king
     */
    private ChessPosition getKingPosition(TeamColor teamColor, ChessBoard board) {
        ChessPosition position;
        ChessPiece piece;
        for (int row = 1; row <= ChessBoard.BOARD_ROWS; row++) {
            for (int col = 1; col <= ChessBoard.BOARD_COLS; col++) {
                position = new ChessPosition(row, col);
                piece = board.getPiece(position);
                if (piece != null && piece.getPieceType() == PieceType.KING && piece.getTeamColor() == teamColor) {
                    return position;
                }
            }
        }
        return null;
    }

    private Collection<ChessMove> getAllTeamMoves(TeamColor teamColor, ChessBoard board) {
        Collection<ChessMove> allTeamMoves = new ArrayList<>();
        Collection<ChessPosition> teamPiecePositions = getAllTeamPiecePositions(teamColor, board);
        for (ChessPosition position : teamPiecePositions) {
            ChessPiece piece = board.getPiece(position);
            for (ChessMove move : piece.pieceMoves(board, position)) {
                allTeamMoves.add(move);
            }
        }
        return allTeamMoves;
    }

    private Collection<ChessPosition> getAllTeamPiecePositions(TeamColor teamColor, ChessBoard board) {
        Collection<ChessPosition> piecePositions = board.getAllPiecePositions();
        piecePositions.removeIf(position -> {
            return board.getPiece(position).getTeamColor() != teamColor;
        });
        return piecePositions;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((currentTeamTurn == null) ? 0 : currentTeamTurn.hashCode());
        result = prime * result + ((board == null) ? 0 : board.hashCode());
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
        ChessGame other = (ChessGame) obj;
        if (currentTeamTurn != other.currentTeamTurn)
            return false;
        if (board == null) {
            if (other.board != null)
                return false;
        } else if (!board.equals(other.board))
            return false;
        return true;
    }
}
