package client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import chess.*;
import chess.ChessGame.TeamColor;
import ui.ChessBoardPrinter;
import ui.Color;
import ui.StreamPrinter;

public class ClientMain {

    public static boolean isLoggedIn = false;

    public static void main(String[] args) {

        var game = new ChessGame();
        var boardDrawer = new ChessBoardPrinter(System.out);
        var textPrinter = new StreamPrinter(System.out);
        var inputReader = new Scanner(System.in);

        List<Command> loggedOutCommands = new ArrayList<>(Arrays.asList(
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
                })));

        List<Command> loggedInCommands = new ArrayList<>(Arrays.asList(
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
                    System.out.println("Bye!");
                    System.exit(0);
                })));

        // Update help commands with proper references
        loggedOutCommands.add(new Command("help", "with possible commands", null, (commandArgs) -> {
            listCommands(textPrinter, loggedOutCommands);
        }));
        loggedInCommands.add(new Command("help", "with possible commands", null, (commandArgs) -> {
            listCommands(textPrinter, loggedInCommands);
        }));
        // var serverUrl = "http://localhost:8080";
        // if (args.length == 1) {
        // serverUrl = args[0];
        // }

        // new Repl(serverUrl).run();
        // try (var scanner = new Scanner(System.in)) {
        // scanner.nextLine();
        // }
        System.out.println("♕  Welcome to 240 Chess. Type help to get started. ♕");

        String line;
        do {
            printTerminalInterface(textPrinter);
            textPrinter.setTextColor(Color.GREEN);
            line = inputReader.nextLine();
            textPrinter.setTextColor(Color.NONE);
            try {
                interpretCommand(line, loggedOutCommands);
            } catch (InvalidCommandException e) {
                textPrinter.setTextColor(Color.RED);
                textPrinter.println("Unable to execute command: " + e.getMessage());
                textPrinter.setTextColor(Color.NONE);
            }
        } while (!line.equals("quit"));

        // listCommands(textPrinter, loggedInCommands);
        // boardDrawer.draw(game.getBoard());
    }

    public static void printTerminalInterface(StreamPrinter printer) {
        printer.print("[");
        if (isLoggedIn) {
            printer.print("LOGGED_IN");
        } else {
            printer.print("LOGGED_OUT");
        }
        printer.print("] >>> ");
    }

    public static void listCommands(StreamPrinter printer, List<Command> commands) {
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

    public static void interpretCommand(String line, List<Command> possibleCommands) throws InvalidCommandException {
        try (var lineScanner = new Scanner(line).useDelimiter(" ")) {
            var commandName = lineScanner.next();
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
                    command.handler().accept(argValues);
                    return;
                }
            }
            throw new InvalidCommandException("Unknown Command (Type \"help\" for list of available commands)");
        }
    }
}
