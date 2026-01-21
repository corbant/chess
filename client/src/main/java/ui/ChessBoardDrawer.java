package ui;

import java.io.PrintStream;

import chess.ChessBoard;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.ChessGame.TeamColor;
import chess.ChessPiece.PieceType;

public class ChessBoardDrawer extends StreamDrawer {

    // TODO: add customization options
    public ChessBoardDrawer(PrintStream output) {
        super(output);
    }

    public void draw(ChessBoard board) {
        // starts in the top left
        for (int row = 0; row <= ChessBoard.BOARD_ROWS + 1; row++) {
            for (int col = 0; col <= ChessBoard.BOARD_COLS + 1; col++) {
                if (row < 1 || row > ChessBoard.BOARD_ROWS || col < 1 || col > ChessBoard.BOARD_COLS) {
                    // border
                    setBackgroundColor(Color.LIGHT_GREY);
                    if (col >= 1 && col <= ChessBoard.BOARD_ROWS) {
                        char colLabel = 'a';
                        colLabel += col - 1;
                        out.print(" " + colLabel + " ");
                    } else if (row >= 1 && row <= ChessBoard.BOARD_COLS) {
                        int rowLabel = 8 - row + 1;
                        out.print(" " + rowLabel + " ");
                    } else {
                        out.print("   ");
                    }
                } else {
                    // chess board
                    // determine background color
                    setBackgroundColor((row % 2 == 0 && col % 2 != 0) || (row % 2 != 0 && col % 2 == 0) ? Color.BLACK
                            : Color.WHITE);
                    ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                    if (piece != null) {
                        drawChessPiece(piece.getPieceType(), piece.getTeamColor());
                    } else {
                        out.print("   ");
                    }
                }
            }
            setBackgroundColor(Color.NONE);
            out.print('\n');
        }
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