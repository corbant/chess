package ui;

import java.io.PrintStream;

public class StreamDrawer {

    protected PrintStream out;

    public StreamDrawer(PrintStream output) {
        this.out = output;
    }

    protected void setTextColor(Color color) {
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

    protected void setBackgroundColor(Color color) {
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

    protected void setTextEffect(TextEffect effect) {
        switch (effect) {
            case BOLD:
                out.print(EscapeSequences.SET_TEXT_BOLD);
                break;
            case FAINT:
                out.print(EscapeSequences.SET_TEXT_FAINT);
                break;
            case ITALIC:
                out.print(EscapeSequences.SET_TEXT_ITALIC);
                break;
            case UNDERLINE:
                out.print(EscapeSequences.SET_TEXT_UNDERLINE);
                break;
            case BLINKING:
                out.print(EscapeSequences.SET_TEXT_BLINKING);
                break;
            case NONE:
                out.print(EscapeSequences.RESET_TEXT_BOLD_FAINT);
                out.print(EscapeSequences.RESET_TEXT_ITALIC);
                out.print(EscapeSequences.RESET_TEXT_UNDERLINE);
                out.print(EscapeSequences.RESET_TEXT_BLINKING);
                break;
        }
    }

    protected void eraseScreen() {
        out.print(EscapeSequences.ERASE_SCREEN);
    }

    protected void eraseLine() {
        out.print(EscapeSequences.ERASE_LINE);
    }
}
