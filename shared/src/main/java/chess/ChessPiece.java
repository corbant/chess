package chess;

import java.util.Collection;

import java.util.ArrayList;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor teamColor;
    private ChessPiece.PieceType type;

    /**
     * Creates a chess piece with the given team and type
     * 
     * @param pieceColor color of the team that this piece is assigned to
     * @param type       type of chess piece that this is
     */
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.teamColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    private static final PieceType[] PAWN_PROMOTABLE_PIECE_TYPES = new PieceType[] { PieceType.QUEEN, PieceType.ROOK,
            PieceType.BISHOP, PieceType.KNIGHT };

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.teamColor;
    }

    /**
     * @return Which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        switch (type) {
            case PieceType.KING:
                return kingMoves(board, myPosition);

            case PieceType.QUEEN:
                return queenMoves(board, myPosition);

            case PieceType.BISHOP:
                return bishopMoves(board, myPosition);

            case PieceType.KNIGHT:
                return knightMoves(board, myPosition);

            case PieceType.ROOK:
                return rookMoves(board, myPosition);

            case PieceType.PAWN:
                return pawnMoves(board, myPosition);
        }

        return new ArrayList<ChessMove>();
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPosition possiblePosition;
        ChessPiece pieceAtLocation;
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        // can move in any direction one square
        // up
        possiblePosition = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn());
        if (isInBounds(possiblePosition)) {
            pieceAtLocation = board.getPiece(possiblePosition);
            if (pieceAtLocation == null || pieceAtLocation.teamColor != this.teamColor) {
                possibleMoves.add(new ChessMove(myPosition, possiblePosition));
            }
        }

        // up right
        possiblePosition = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1);
        if (isInBounds(possiblePosition)) {
            pieceAtLocation = board.getPiece(possiblePosition);
            if (pieceAtLocation == null || pieceAtLocation.teamColor != this.teamColor) {
                possibleMoves.add(new ChessMove(myPosition, possiblePosition));
            }
        }

        // right
        possiblePosition = new ChessPosition(myPosition.getRow(), myPosition.getColumn() + 1);
        if (isInBounds(possiblePosition)) {
            pieceAtLocation = board.getPiece(possiblePosition);
            if (pieceAtLocation == null || pieceAtLocation.teamColor != this.teamColor) {
                possibleMoves.add(new ChessMove(myPosition, possiblePosition));
            }
        }

        // down right
        possiblePosition = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1);
        if (isInBounds(possiblePosition)) {
            pieceAtLocation = board.getPiece(possiblePosition);
            if (pieceAtLocation == null || pieceAtLocation.teamColor != this.teamColor) {
                possibleMoves.add(new ChessMove(myPosition, possiblePosition));
            }
        }

        // down
        possiblePosition = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn());
        if (isInBounds(possiblePosition)) {
            pieceAtLocation = board.getPiece(possiblePosition);
            if (pieceAtLocation == null || pieceAtLocation.teamColor != this.teamColor) {
                possibleMoves.add(new ChessMove(myPosition, possiblePosition));
            }
        }

        // down left
        possiblePosition = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1);
        if (isInBounds(possiblePosition)) {
            pieceAtLocation = board.getPiece(possiblePosition);
            if (pieceAtLocation == null || pieceAtLocation.teamColor != this.teamColor) {
                possibleMoves.add(new ChessMove(myPosition, possiblePosition));
            }
        }

        // left
        possiblePosition = new ChessPosition(myPosition.getRow(), myPosition.getColumn() - 1);
        if (isInBounds(possiblePosition)) {
            pieceAtLocation = board.getPiece(possiblePosition);
            if (pieceAtLocation == null || pieceAtLocation.teamColor != this.teamColor) {
                possibleMoves.add(new ChessMove(myPosition, possiblePosition));
            }
        }

        // up left
        possiblePosition = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1);
        if (isInBounds(possiblePosition)) {
            pieceAtLocation = board.getPiece(possiblePosition);
            if (pieceAtLocation == null || pieceAtLocation.teamColor != this.teamColor) {
                possibleMoves.add(new ChessMove(myPosition, possiblePosition));
            }
        }
        return possibleMoves;
    }

    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();

        // can move in any direction any amount
        // straight moves
        // up
        possibleMoves.addAll(movesInDirection(board, myPosition, 1, 0));

        // down
        possibleMoves.addAll(movesInDirection(board, myPosition, -1, 0));

        // left
        possibleMoves.addAll(movesInDirection(board, myPosition, 0, -1));

        // right
        possibleMoves.addAll(movesInDirection(board, myPosition, 0, 1));

        // diagonal moves
        // up to the right
        possibleMoves.addAll(movesInDirection(board, myPosition, 1, 1));

        // up to the left
        possibleMoves.addAll(movesInDirection(board, myPosition, 1, -1));

        // down to the right
        possibleMoves.addAll(movesInDirection(board, myPosition, -1, 1));

        // down to the left
        possibleMoves.addAll(movesInDirection(board, myPosition, -1, -1));

        return possibleMoves;
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();

        // can move diagonally any amount
        // up to the right
        possibleMoves.addAll(movesInDirection(board, myPosition, 1, 1));

        // up to the left
        possibleMoves.addAll(movesInDirection(board, myPosition, 1, -1));

        // down to the right
        possibleMoves.addAll(movesInDirection(board, myPosition, -1, 1));

        // down to the left
        possibleMoves.addAll(movesInDirection(board, myPosition, -1, -1));

        return possibleMoves;
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPosition possiblePosition;
        ChessPiece pieceAtLocation;
        Collection<ChessMove> possibleMoves = new ArrayList<>();

        // can jump forward 2, to the side 1
        // up right
        possiblePosition = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn() + 1);
        if (isInBounds(possiblePosition)) {
            pieceAtLocation = board.getPiece(possiblePosition);
            if (pieceAtLocation == null || pieceAtLocation.teamColor != this.teamColor) {
                possibleMoves.add(new ChessMove(myPosition, possiblePosition));
            }
        }

        // right up
        possiblePosition = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 2);
        if (isInBounds(possiblePosition)) {
            pieceAtLocation = board.getPiece(possiblePosition);
            if (pieceAtLocation == null || pieceAtLocation.teamColor != this.teamColor) {
                possibleMoves.add(new ChessMove(myPosition, possiblePosition));
            }
        }

        // right down
        possiblePosition = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 2);
        if (isInBounds(possiblePosition)) {
            pieceAtLocation = board.getPiece(possiblePosition);
            if (pieceAtLocation == null || pieceAtLocation.teamColor != this.teamColor) {
                possibleMoves.add(new ChessMove(myPosition, possiblePosition));
            }
        }

        // down right
        possiblePosition = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn() + 1);
        if (isInBounds(possiblePosition)) {
            pieceAtLocation = board.getPiece(possiblePosition);
            if (pieceAtLocation == null || pieceAtLocation.teamColor != this.teamColor) {
                possibleMoves.add(new ChessMove(myPosition, possiblePosition));
            }
        }

        // down left
        possiblePosition = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn() - 1);
        if (isInBounds(possiblePosition)) {
            pieceAtLocation = board.getPiece(possiblePosition);
            if (pieceAtLocation == null || pieceAtLocation.teamColor != this.teamColor) {
                possibleMoves.add(new ChessMove(myPosition, possiblePosition));
            }
        }

        // left down
        possiblePosition = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 2);
        if (isInBounds(possiblePosition)) {
            pieceAtLocation = board.getPiece(possiblePosition);
            if (pieceAtLocation == null || pieceAtLocation.teamColor != this.teamColor) {
                possibleMoves.add(new ChessMove(myPosition, possiblePosition));
            }
        }

        // left up
        possiblePosition = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 2);
        if (isInBounds(possiblePosition)) {
            pieceAtLocation = board.getPiece(possiblePosition);
            if (pieceAtLocation == null || pieceAtLocation.teamColor != this.teamColor) {
                possibleMoves.add(new ChessMove(myPosition, possiblePosition));
            }
        }

        // up left
        possiblePosition = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn() - 1);
        if (isInBounds(possiblePosition)) {
            pieceAtLocation = board.getPiece(possiblePosition);
            if (pieceAtLocation == null || pieceAtLocation.teamColor != this.teamColor) {
                possibleMoves.add(new ChessMove(myPosition, possiblePosition));
            }
        }

        return possibleMoves;
    }

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();

        // can move in a straight line any amount
        // up
        possibleMoves.addAll(movesInDirection(board, myPosition, 1, 0));

        // down
        possibleMoves.addAll(movesInDirection(board, myPosition, -1, 0));

        // left
        possibleMoves.addAll(movesInDirection(board, myPosition, 0, -1));

        // right
        possibleMoves.addAll(movesInDirection(board, myPosition, 0, 1));

        return possibleMoves;
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPosition possiblePosition;
        ChessPiece pieceAtLocation;
        Collection<ChessMove> possibleMoves = new ArrayList<>();

        // can move forward 1, or if it has not moved then it can move 2 squares,
        // attacks on the diagonals, can be promoted as well if it reached the end of
        // the board
        boolean canPromote = false;
        final boolean IS_WHITE = this.teamColor == ChessGame.TeamColor.WHITE;
        final boolean HAS_MOVED = (IS_WHITE && myPosition.getRow() != 2)
                || (!IS_WHITE && myPosition.getRow() != ChessBoard.BOARD_ROWS - 1);
        final int DIRECTION = IS_WHITE ? 1 : -1;

        possiblePosition = new ChessPosition(myPosition.getRow() + 1 * DIRECTION, myPosition.getColumn());
        if ((this.teamColor == ChessGame.TeamColor.WHITE && possiblePosition.getRow() == ChessBoard.BOARD_ROWS)
                || (this.teamColor == ChessGame.TeamColor.BLACK && possiblePosition.getRow() == 1)) {
            // Pawn can be promoted
            canPromote = true;
        }
        pieceAtLocation = board.getPiece(possiblePosition);
        // if the space in front is empty, it can move there
        if (pieceAtLocation == null) {
            if (canPromote) {
                // add promotion types
                for (PieceType promotionType : PAWN_PROMOTABLE_PIECE_TYPES) {
                    possibleMoves.add(new ChessMove(myPosition, possiblePosition, promotionType));
                }
            } else {
                possibleMoves.add(new ChessMove(myPosition, possiblePosition));
            }

            if (!HAS_MOVED) {
                // pawn can move double since it hasn't moved yet
                possiblePosition = new ChessPosition(myPosition.getRow() + 2 * DIRECTION,
                        myPosition.getColumn());
                pieceAtLocation = board.getPiece(possiblePosition);
                if (pieceAtLocation == null) {
                    possibleMoves.add(new ChessMove(myPosition, possiblePosition));
                }
            }
        }

        // attacking
        // right diagonal
        if (myPosition.getColumn() + 1 < ChessBoard.BOARD_COLS) {
            possiblePosition = new ChessPosition(myPosition.getRow() + 1 * DIRECTION,
                    myPosition.getColumn() + 1);
            pieceAtLocation = board.getPiece(possiblePosition);
            if (pieceAtLocation != null && pieceAtLocation.teamColor != this.teamColor) {

                if (canPromote) {
                    // add promotion types
                    for (PieceType promotionType : PAWN_PROMOTABLE_PIECE_TYPES) {
                        possibleMoves.add(new ChessMove(myPosition, possiblePosition, promotionType));
                    }
                } else {
                    possibleMoves.add(new ChessMove(myPosition, possiblePosition));
                }
            }
        }

        // left diagonal
        if (myPosition.getColumn() - 1 > 0) {
            possiblePosition = new ChessPosition(myPosition.getRow() + 1 * DIRECTION,
                    myPosition.getColumn() - 1);
            pieceAtLocation = board.getPiece(possiblePosition);
            if (pieceAtLocation != null && pieceAtLocation.teamColor != this.teamColor) {
                if (canPromote) {
                    // add promotion types
                    for (PieceType promotionType : PAWN_PROMOTABLE_PIECE_TYPES) {
                        possibleMoves.add(new ChessMove(myPosition, possiblePosition, promotionType));
                    }
                } else {
                    possibleMoves.add(new ChessMove(myPosition, possiblePosition));
                }
            }
        }

        return possibleMoves;
    }

    private Collection<ChessMove> movesInDirection(ChessBoard board, ChessPosition myPosition, int rowChange,
            int colChange) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessPiece pieceAtLocation;

        ChessPosition possiblePosition = new ChessPosition(myPosition.getRow() + rowChange,
                myPosition.getColumn() + colChange);

        while (isInBounds(possiblePosition)) {
            pieceAtLocation = board.getPiece(possiblePosition);
            // if there is already a piece there
            if (pieceAtLocation != null) {
                if (pieceAtLocation.teamColor == this.teamColor) {
                    break;
                } else {
                    moves.add(new ChessMove(myPosition, possiblePosition));
                    break;
                }
            }
            // the space is empty so the piece can move there
            moves.add(new ChessMove(myPosition, possiblePosition));
            possiblePosition = new ChessPosition(possiblePosition.getRow() + rowChange,
                    possiblePosition.getColumn() + colChange);
        }
        return moves;
    }

    /**
     * Verifies whether a given position is on the chess board
     * 
     * @param position position to check
     * @return true if the position is on the board
     */
    private boolean isInBounds(ChessPosition position) {
        int col = position.getColumn();
        int row = position.getRow();
        return col > 0 && col <= ChessBoard.BOARD_COLS && row > 0 && row <= ChessBoard.BOARD_ROWS;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((teamColor == null) ? 0 : teamColor.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        ChessPiece other = (ChessPiece) obj;
        if (teamColor != other.teamColor)
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ChessPiece [teamColor=" + teamColor + ", type=" + type + "]";
    }
}
