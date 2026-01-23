package client;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
                                Map.entry("email", String.class)),
                        (commandArgs) -> {
                            String username = (String) commandArgs[0];
                            String password = (String) commandArgs[1];
                            String email = (String) commandArgs[2];
                        }),
                new Command("login", "to play chess",
                        List.of(Map.entry("username", String.class), Map.entry("password", String.class)),
                        (commandArgs) -> {
                            String username = (String) commandArgs[0];
                            String password = (String) commandArgs[1];
                        }),
                new Command("quit", "playing chess", null, (commandArgs) -> {
                    return;
                }),
                new Command("help", "with possible commands", null, (commandArgs) -> {
                    return;
                })
        };

        Command[] loggedInCommands = new Command[] {
                new Command("create", "a game", List.of(Map.entry("name", String.class)), (commandArgs) -> {
                    String name = (String) commandArgs[0];
                }),
                new Command("list", "games", null, (commandArgs) -> {
                    return;
                }),
                new Command("join", "a game",
                        List.of(Map.entry("ID", Integer.class), Map.entry("color", TeamColor.class)), (commandArgs) -> {
                            int ID = (int) commandArgs[0];
                            TeamColor color = (TeamColor) commandArgs[1];
                        }),
                new Command("observe", "a game", List.of(Map.entry("ID", Integer.class)), (commandArgs) -> {
                    int ID = (int) commandArgs[0];
                }),
                new Command("logout", "when you are done", null, (commandArgs) -> {
                    return;
                }),
                new Command("quit", "playing chess", null, (commandArgs) -> {
                    return;
                }),
                new Command("help", "with possible commands", null, (commandArgs) -> {
                    return;
                })
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

    public static void interpretCommand(String line, Command[] possibleCommands) throws InvalidCommandException {
        try (var lineScanner = new Scanner(line).useDelimiter(" ")) {
            var commandName = lineScanner.nextLine();
            for (var command : possibleCommands) {
                if (command.name().equalsIgnoreCase(commandName)) {
                    Object[] argValues = null;
                    if (command.args() != null && !command.args().isEmpty()) {
                        argValues = new Object[command.args().size()];
                        for (int i = 0; i < command.args().size(); i++) {
                            var arg = command.args().get(i);
                            Class<?> type = arg.getValue();
                            if (!lineScanner.hasNext()) {
                                throw new InvalidCommandException("Missing Argument " + arg.getKey());
                            }
                            if (type == String.class) {
                                argValues[i] = lineScanner.next();
                            } else if (type == Integer.class) {
                                if (!lineScanner.hasNextInt()) {
                                    throw new InvalidCommandException("Invalid Argument " + arg.getKey());
                                }
                                argValues[i] = lineScanner.nextInt();
                            } else if (type.isEnum()) {
                                String commandArgument = lineScanner.next();
                                var possibleInputs = type.getEnumConstants();
                                boolean isValid = false;
                                for (var possibleInput : possibleInputs) {
                                    if (possibleInput.toString().equals(commandArgument)) {
                                        argValues[i] = possibleInput;
                                        isValid = true;
                                    }
                                }
                                if (!isValid) {
                                    throw new InvalidCommandException("Invalid Argument " + arg.getKey());
                                }
                            }
                        }
                    }
                    command.handler().accept(possibleCommands);
                    return;
                }
            }
        }
    }
}
