package ui;

import java.io.PrintStream;

public class StreamPrinter {

    protected PrintStream out;

    public StreamPrinter(PrintStream output) {
        this.out = output;
    }

    public void print(String text) {
        out.print(text);
    }

    public void println(String text) {
        out.print(text + '\n');
    }

    public void newline() {
        out.print('\n');
    }

    public void setTextColor(Color color) {
        switch (color) {
            case BLACK:
                out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
                break;
            case LIGHT_GREY:
                out.print(EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY);
                break;
            case DARK_GREY:
                out.print(EscapeSequences.SET_TEXT_COLOR_DARK_GREY);
                break;
            case RED:
                out.print(EscapeSequences.SET_TEXT_COLOR_RED);
                break;
            case GREEN:
                out.print(EscapeSequences.SET_TEXT_COLOR_GREEN);
                break;
            case YELLOW:
                out.print(EscapeSequences.SET_TEXT_COLOR_YELLOW);
                break;
            case BLUE:
                out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
                break;
            case MAGENTA:
                out.print(EscapeSequences.SET_TEXT_COLOR_MAGENTA);
                break;
            case WHITE:
                out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
                break;
            case NONE:
                out.print(EscapeSequences.RESET_TEXT_COLOR);
                break;
        }
    }

    public void setBackgroundColor(Color color) {
        switch (color) {
            case BLACK:
                out.print(EscapeSequences.SET_BG_COLOR_BLACK);
                break;
            case LIGHT_GREY:
                out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
                break;
            case DARK_GREY:
                out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY);
                break;
            case RED:
                out.print(EscapeSequences.SET_BG_COLOR_RED);
                break;
            case GREEN:
                out.print(EscapeSequences.SET_BG_COLOR_GREEN);
                break;
            case YELLOW:
                out.print(EscapeSequences.SET_BG_COLOR_YELLOW);
                break;
            case BLUE:
                out.print(EscapeSequences.SET_BG_COLOR_BLUE);
                break;
            case MAGENTA:
                out.print(EscapeSequences.SET_BG_COLOR_MAGENTA);
                break;
            case WHITE:
                out.print(EscapeSequences.SET_BG_COLOR_WHITE);
                break;
            case NONE:
                out.print(EscapeSequences.RESET_BG_COLOR);
                break;
        }
    }
}
