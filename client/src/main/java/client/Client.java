package client;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import chess.ChessGame.TeamColor;
import ui.ChessBoardPrinter;
import ui.Color;
import ui.StreamPrinter;

public class Client {
    private boolean isLoggedIn = false;
    private String authToken = null;
    private ServerFacade server;
    private ChessBoardPrinter printer;

    private List<Command> loggedOutCommands = List.of(new Command("register", "to create an account",
            List.of(Map.entry("username", String.class), Map.entry("password", String.class),
                    Map.entry("email", String.class)),
            (commandArgs) -> {
                String username = (String) commandArgs[0];
                String password = (String) commandArgs[1];
                String email = (String) commandArgs[2];
                register(username, password, email);
            }),
            new Command("login", "to play chess",
                    List.of(Map.entry("username", String.class), Map.entry("password", String.class)),
                    (commandArgs) -> {
                        String username = (String) commandArgs[0];
                        String password = (String) commandArgs[1];
                        login(username, password);
                    }),
            new Command("quit", "playing chess", null, (commandArgs) -> {
                printer.print("Bye!");
                System.exit(0);
            }),
            new Command("help", "with possible commands", null, (commandArgs) -> {
                listCommands(printer, getAvailableCommands());
            }));

    private List<Command> loggedInCommands = List
            .of(new Command("create", "a game", List.of(Map.entry("name", String.class)), (commandArgs) -> {
                String name = (String) commandArgs[0];
                createGame(name);
            }),
                    new Command("list", "games", null, (commandArgs) -> {
                        listGames();
                    }),
                    new Command("join", "a game",
                            List.of(Map.entry("ID", Integer.class), Map.entry("color", TeamColor.class)),
                            (commandArgs) -> {
                                int ID = (int) commandArgs[0];
                                TeamColor color = (TeamColor) commandArgs[1];
                                joinGame(ID, color);
                            }),
                    new Command("observe", "a game", List.of(Map.entry("ID", Integer.class)), (commandArgs) -> {
                        int ID = (int) commandArgs[0];
                        observeGame(ID);
                    }),
                    new Command("logout", "when you are done", null, (commandArgs) -> {
                        logout();
                    }),
                    new Command("quit", "playing chess", null, (commandArgs) -> {
                        printer.println("Bye!");
                        System.exit(0);
                    }),
                    new Command("help", "with possible commands", null, (commandArgs) -> {
                        listCommands(printer, getAvailableCommands());
                    }));

    public Client(String serverUrl, ChessBoardPrinter printer) {
        this.server = new ServerFacade(serverUrl);
        this.printer = printer;
    }

    private void register(String username, String password, String email) {
        server.register(username, password, email);
    }

    private void login(String username, String password) {
        try {
            LoginResponse response = server.login(username, password);
            printer.print("Logged in as " + response.username());
            authToken = response.authToken();
            isLoggedIn = true;
        } catch (Exception e) {

        }
    }

    private void createGame(String name) {

    }

    private void listGames() {

    }

    private void joinGame(int gameID, TeamColor color) {

    }

    private void observeGame(int gameID) {

    }

    private void logout() {
        server.logout(authToken);
        authToken = null;
        isLoggedIn = false;
    }

    private void handleServerException(Exception e) {

    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public List<Command> getAvailableCommands() {
        return isLoggedIn ? loggedInCommands : loggedOutCommands;
    }

    public void listCommands(StreamPrinter printer, List<Command> commands) {
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

    public void interpretCommand(String line) throws InvalidCommandException {
        try (var lineScanner = new Scanner(line).useDelimiter(" ")) {
            var commandName = lineScanner.next();
            for (var command : getAvailableCommands()) {
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
