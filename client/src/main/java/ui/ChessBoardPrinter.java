package ui;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.ChessGame.TeamColor;
import chess.ChessPiece.PieceType;

public class ChessBoardPrinter extends StreamPrinter {

    public ChessBoardPrinter(PrintStream output) {
        super(output);
    }

    public void drawBoard(ChessBoard board, boolean isBlack, Collection<ChessMove> highlightedMoves) {
        int rowStart = isBlack ? ChessBoard.BOARD_ROWS + 1 : 0;
        int rowEnd = isBlack ? -1 : ChessBoard.BOARD_ROWS + 2;
        int rowDelta = isBlack ? -1 : 1;

        int colStart = isBlack ? ChessBoard.BOARD_COLS + 1 : 0;
        int colEnd = isBlack ? -1 : ChessBoard.BOARD_ROWS + 2;
        int colDelta = isBlack ? -1 : 1;
        Set<ChessPosition> highlightedPositions = getHighlightedPositions(highlightedMoves);
        // starts in the top left
        for (int row = rowStart; isBlack ? row > rowEnd : row < rowEnd; row += rowDelta) {
            for (int col = colStart; isBlack ? col > colEnd : col < rowEnd; col += colDelta) {
                if (isBorderCell(row, col)) {
                    drawBorderCell(row, col);
                    continue;
                }

                var position = new ChessPosition(ChessBoard.BOARD_ROWS - row + 1, col);
                setBoardCellBackground(board, row, col, position, highlightedPositions);

                ChessPiece piece = board.getPiece(position);
                if (piece != null) {
                    drawChessPiece(piece.getPieceType(), piece.getTeamColor());
                } else {
                    out.print("   ");
                }
            }
            setBackgroundColor(Color.NONE);
            out.print('\n');
        }
    }

    private Set<ChessPosition> getHighlightedPositions(Collection<ChessMove> highlightedMoves) {
        Set<ChessPosition> positions = new HashSet<>();
        if (highlightedMoves == null || highlightedMoves.isEmpty()) {
            return positions;
        }
        for (var move : highlightedMoves) {
            positions.add(move.getEndPosition());
        }
        return positions;
    }

    private boolean isBorderCell(int row, int col) {
        return row < 1 || row > ChessBoard.BOARD_ROWS || col < 1 || col > ChessBoard.BOARD_COLS;
    }

    private void drawBorderCell(int row, int col) {
        setBackgroundColor(Color.LIGHT_GREY);
        if (col >= 1 && col <= ChessBoard.BOARD_ROWS) {
            char colLabel = (char) ('a' + col - 1);
            out.print(" " + colLabel + " ");
            return;
        }
        if (row >= 1 && row <= ChessBoard.BOARD_COLS) {
            int rowLabel = 8 - row + 1;
            out.print(" " + rowLabel + " ");
            return;
        }
        out.print("   ");
    }

    private void setBoardCellBackground(ChessBoard board, int row, int col, ChessPosition position,
            Set<ChessPosition> highlightedPositions) {
        if (highlightedPositions.contains(position)) {
            setBackgroundColor(board.getPiece(position) != null ? Color.RED : Color.GREEN);
            return;
        }
        boolean isDarkSquare = (row + col) % 2 == 1;
        setBackgroundColor(isDarkSquare ? Color.BLACK : Color.WHITE);
    }

    public void drawBoard(ChessBoard board, boolean isBlack) {
        drawBoard(board, isBlack, null);
    }

    private void drawChessPiece(PieceType type, TeamColor color) {
        switch (color) {
            case WHITE:
                setTextColor(Color.WHITE);
                switch (type) {
                    case KING:
                        out.print(EscapeSequences.WHITE_KING);
                        break;
                    case QUEEN:
                        out.print(EscapeSequences.WHITE_QUEEN);
                        break;
                    case BISHOP:
                        out.print(EscapeSequences.WHITE_BISHOP);
                        break;
                    case KNIGHT:
                        out.print(EscapeSequences.WHITE_KNIGHT);
                        break;
                    case ROOK:
                        out.print(EscapeSequences.WHITE_ROOK);
                        break;
                    case PAWN:
                        out.print(EscapeSequences.WHITE_PAWN);
                        break;
                }
                break;
            case BLACK:
                setTextColor(Color.BLACK);
                switch (type) {
                    case KING:
                        out.print(EscapeSequences.BLACK_KING);
                        break;
                    case QUEEN:
                        out.print(EscapeSequences.BLACK_QUEEN);
                        break;
                    case BISHOP:
                        out.print(EscapeSequences.BLACK_BISHOP);
                        break;
                    case KNIGHT:
                        out.print(EscapeSequences.BLACK_KNIGHT);
                        break;
                    case ROOK:
                        out.print(EscapeSequences.BLACK_ROOK);
                        break;
                    case PAWN:
                        out.print(EscapeSequences.BLACK_PAWN);
                        break;
                }
                break;
        }
        setTextColor(Color.NONE);
    }

}