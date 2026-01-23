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
        LoginResponse response;
        try {
            response = server.register(username, password, email);
        } catch (BadRequestException e) {
            printErrorMessage("Invalid username, password, or email");
            return;
        } catch (AlreadyTakenException e) {
            printErrorMessage("The username you provided has already been registered");
            return;
        } catch (ServerErrorException e) {
            printErrorMessage("Internal server error, unable to complete command");
            return;
        } catch (ConnectionErrorException e) {
            printErrorMessage("Unable to connect to server, please try again");
            return;
        }
        authToken = response.authToken();
        isLoggedIn = true;
        printer.print("Logged in as " + response.username());
    }

    private void login(String username, String password) {
        LoginResponse response;
        try {
            response = server.login(username, password);
        } catch (BadRequestException e) {
            printErrorMessage("Invalid username or password");
            return;
        } catch (UnauthorizedException e) {
            printErrorMessage("Invalid username or password");
            return;
        } catch (ServerErrorException e) {
            printErrorMessage("Internal server error, unable to complete command");
            return;
        } catch (ConnectionErrorException e) {
            printErrorMessage("Unable to connect to server, please try again");
            return;
        }
        authToken = response.authToken();
        isLoggedIn = true;
        printer.print("Logged in as " + response.username());
    }

    private void createGame(String name) {
        CreateGameResponse response;
        try {
            response = server.createGame(authToken, name);
        } catch (UnauthorizedException e) {
            printErrorMessage("Please login before using this command");
            return;
        } catch (BadRequestException e) {
            printErrorMessage("Invalid game name");
            return;
        } catch (ServerErrorException e) {
            printErrorMessage("Internal server error, unable to complete command");
            return;
        } catch (ConnectionErrorException e) {
            printErrorMessage("Unable to connect to server, please try again");
            return;
        }
        printer.print("New game created with game ID " + response.gameID());
    }

    private void listGames() {
        ListGamesResponse response;
        try {
            response = server.listGames(authToken);
        } catch (UnauthorizedException e) {
            printErrorMessage("Please login before using this command");
            return;
        } catch (ServerErrorException e) {
            printErrorMessage("Internal server error, unable to complete command");
            return;
        } catch (ConnectionErrorException e) {
            printErrorMessage("Unable to connect to server, please try again");
            return;
        }
        printer.println("Games:");
        for (var game : response.games()) {
            printer.setTextColor(Color.BLUE);
            printer.print("" + game.gameID());
            printer.setTextColor(Color.NONE);
            printer.print(" - ");
            printer.setTextColor(Color.MAGENTA);
            printer.print(game.gameName());
            printer.setTextColor(Color.NONE);
            printer.print(" | ");
            printer.setTextColor(Color.YELLOW);
            printer.print("White: " + game.whiteUsername() != null ? game.whiteUsername() : "AVAILABLE");
            printer.println(" Black: " + game.blackUsername() != null ? game.blackUsername() : "AVAILABLE");
        }
    }

    private void joinGame(int gameID, TeamColor color) {
        try {
            server.playGame(authToken, gameID, color);
        } catch (BadRequestException e) {
            printErrorMessage("Invalid gameID or team color");
            return;
        } catch (UnauthorizedException e) {
            printErrorMessage("Please login before using this command");
            return;
        } catch (AlreadyTakenException e) {
            printErrorMessage("The color you selected has already been taken");
            return;
        } catch (ServerErrorException e) {
            printErrorMessage("Internal server error, unable to complete command");
            return;
        } catch (ConnectionErrorException e) {
            printErrorMessage("Unable to connect to server, please try again");
            return;
        }
        printer.print("Joined game");
    }

    private void observeGame(int gameID) {

    }

    private void logout() {
        try {
            server.logout(authToken);
        } catch (UnauthorizedException e) {
            printErrorMessage("Please login before using this command");
            return;
        } catch (ServerErrorException e) {
            printErrorMessage("Internal server error, unable to complete command");
            return;
        } catch (ConnectionErrorException e) {
            printErrorMessage("Unable to connect to server, please try again");
            return;
        }
        authToken = null;
        isLoggedIn = false;
        printer.print("Logged out");
    }

    private void printErrorMessage(String message) {
        printer.setTextColor(Color.RED);
        printer.println(message);
        printer.setTextColor(Color.NONE);
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
