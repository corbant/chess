package client;

import chess.*;
import ui.ChessBoardDrawer;

public class ClientMain {
    public static void main(String[] args) {
        // var serverUrl = "http://localhost:8080";
        // if (args.length == 1) {
        // serverUrl = args[0];
        // }

        // new Repl(serverUrl).run();
        var game = new ChessGame();
        var boardDrawer = new ChessBoardDrawer(System.out);
        System.out.println("♕ 240 Chess Client:");
        boardDrawer.draw(game.getBoard());
    }
}
