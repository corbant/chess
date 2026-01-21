package ui;

import java.io.PrintStream;

import chess.ChessBoard;
import chess.ChessGame.TeamColor;
import chess.ChessPiece.PieceType;

public class ChessBoardDrawer extends StreamDrawer {

    // TODO: add customization options
    public ChessBoardDrawer(PrintStream output) {
        super(output);
    }

    public void draw(ChessBoard board) {

    }

    private void drawChessPiece(PrintStream out, PieceType type, TeamColor color) {
        switch (color) {
            case WHITE:
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
    }

}