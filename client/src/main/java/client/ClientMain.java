package client;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.lang.model.type.PrimitiveType;

import chess.*;
import chess.ChessGame.TeamColor;
import ui.ChessBoardPrinter;
import ui.Color;
import ui.StreamPrinter;

public class ClientMain {

    public static void main(String[] args) {

        Command[] loggedOutCommands = new Command[] {
                new Command("register", "to create an account",
                        List.of(Map.entry("username", String.class), Map.entry("password", String.class),
                                Map.entry("email", String.class))),
                new Command("login", "to play chess",
                        List.of(Map.entry("username", String.class), Map.entry("password", String.class))),
                new Command("quit", "playing chess", null),
                new Command("help", "with possible commands", null)
        };

        Command[] loggedInCommands = new Command[] {
                new Command("create", "a game", List.of(Map.entry("name", String.class))),
                new Command("list", "games", null),
                new Command("join", "a game",
                        List.of(Map.entry("ID", Integer.class), Map.entry("color", TeamColor.class))),
                new Command("observe", "a game", List.of(Map.entry("ID", Integer.class))),
                new Command("logout", "when you are done", null),
                new Command("quit", "playing chess", null),
                new Command("help", "with possible commands", null)
        };
        // var serverUrl = "http://localhost:8080";
        // if (args.length == 1) {
        // serverUrl = args[0];
        // }

        // new Repl(serverUrl).run();
        var game = new ChessGame();
        var boardDrawer = new ChessBoardPrinter(System.out);
        var textPrinter = new StreamPrinter(System.out);
        // try (var scanner = new Scanner(System.in)) {
        // scanner.nextLine();
        // }
        System.out.println("♕  Welcome to 240 Chess. Type help to get started. ♕");
        // listCommands(textPrinter, loggedInCommands);
        // boardDrawer.draw(game.getBoard());
    }

    public static void listCommands(StreamPrinter printer, Command[] commands) {
        for (var command : commands) {
            // name
            printer.setTextColor(Color.BLUE);
            printer.print(command.name() + " ");

            // args
            if (command.args() != null && !command.args().isEmpty()) {
                for (var arg : command.args()) {
                    Class<?> type = arg.getValue();

                    if (type.isEnum()) {
                        var options = type.getEnumConstants();
                        printer.print("[");
                        for (int i = 0; i < options.length; i++) {
                            printer.print(options[i].toString().toUpperCase());
                            if (i < options.length - 1) {
                                printer.print("|");
                            }
                        }
                        printer.print("] ");
                    } else {
                        printer.print("<");
                        printer.print(arg.getKey().toUpperCase());
                        printer.print("> ");
                    }
                }
            }

            // description
            printer.setTextColor(Color.MAGENTA);
            printer.print("- " + command.description() + "\n");
        }
        printer.setTextColor(Color.NONE);
    }

    public static void interpretCommand(String command, Command[] possibleCommands) {

    }
}
