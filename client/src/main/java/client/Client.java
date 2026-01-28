package client;

import java.util.List;
import java.util.Scanner;

import chess.ChessGame;
import chess.ChessGame.TeamColor;
import ui.ChessBoardPrinter;
import ui.Color;
import ui.StreamPrinter;

public class Client {
    private boolean isLoggedIn = false;
    private String authToken = null;
    private boolean isPlayingGame = false;
    private int gameID = 0;
    private ServerFacade server;
    private ChessBoardPrinter printer;

    private List<Command> loggedOutCommands = List.of(new Command("register", "to create an account",
            List.of(new CommandArgument("username", String.class, true),
                    new CommandArgument("password", String.class, true),
                    new CommandArgument("email", String.class, true)),
            (commandArgs) -> {
                String username = (String) commandArgs[0];
                String password = (String) commandArgs[1];
                String email = (String) commandArgs[2];
                register(username, password, email);
            }),
            new Command("login", "to play chess",
                    List.of(new CommandArgument("username", String.class, true),
                            new CommandArgument("password", String.class, true)),
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
            .of(new Command("create", "a game", List.of(new CommandArgument("name", String.class, true)),
                    (commandArgs) -> {
                        String name = (String) commandArgs[0];
                        createGame(name);
                    }),
                    new Command("list", "games", null, (commandArgs) -> {
                        listGames();
                    }),
                    new Command("join", "a game",
                            List.of(new CommandArgument("id", Integer.class, true),
                                    new CommandArgument("color", TeamColor.class, true)),
                            (commandArgs) -> {
                                int id = (int) commandArgs[0];
                                TeamColor color = (TeamColor) commandArgs[1];
                                joinGame(id, color);
                            }),
                    new Command("observe", "a game", List.of(new CommandArgument("id", Integer.class, true)),
                            (commandArgs) -> {
                                int id = (int) commandArgs[0];
                                observeGame(id);
                            }),
                    new Command("logout", "when you are done", null, (commandArgs) -> {
                        logout();
                    }),
                    new Command("quit", "playing chess", null, (commandArgs) -> {
                        logout();
                        printer.println("Bye!");
                        System.exit(0);
                    }),
                    new Command("help", "with possible commands", null, (commandArgs) -> {
                        listCommands(printer, getAvailableCommands());
                    }));
    private List<Command> gameplayCommands = List.of(
            new Command("redraw", "the chess board", null, (commandArgs) -> {

            }),
            new Command("leave", "the game", null, (commandArgs) -> {

            }),
            new Command("move", "a chess piece",
                    List.of(new CommandArgument("from", String.class, true),
                            new CommandArgument("to", String.class, true),
                            new CommandArgument("promotion", Character.class, false)),
                    (commandArgs) -> {
                        String from = (String) commandArgs[0];
                        String to = (String) commandArgs[1];
                        char pieceType;
                        if (commandArgs[2] != null) {
                            pieceType = (Character) commandArgs[2];
                        }
                    }),
            new Command("resign", "the game", null, (commandArgs) -> {

            }),
            new Command("highlight", "all legal moves for piece",
                    List.of(new CommandArgument("piece", String.class, true)), (commandArgs) -> {
                        String pieceLocation = (String) commandArgs[0];
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
        printer.println("Logged in as " + response.username());
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
        printer.println("Logged in as " + response.username());
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
        printer.println("New game created with game ID " + response.gameID());
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
            printer.print("[" + game.gameID() + "] ");
            printer.setTextColor(Color.NONE);
            printer.print(game.gameName());
            printer.setTextColor(Color.NONE);
            printer.print(" - ");
            printer.setTextColor(Color.WHITE);
            printer.print("white: ");
            if (game.whiteUsername() != null) {
                printer.print(game.whiteUsername());
            } else {
                printer.setTextColor(Color.GREEN);
                printer.print("(available)");
                printer.setTextColor(Color.NONE);
            }
            printer.setTextColor(Color.BLACK);
            printer.print("  black: ");
            if (game.blackUsername() != null) {
                printer.print(game.blackUsername());
            } else {
                printer.setTextColor(Color.GREEN);
                printer.print("(available)");
                printer.setTextColor(Color.NONE);
            }
            printer.setTextColor(Color.NONE);
            printer.newline();
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
        printer.newline();
        printer.newline();

        isPlayingGame = true;
        this.gameID = gameID;
    }

    private void observeGame(int gameID) {
        printer.newline();
        printer.drawBoard(new ChessGame().getBoard(), false);
        printer.newline();
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
        printer.println("Logged out");
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
        return isLoggedIn ? isPlayingGame ? gameplayCommands : loggedInCommands : loggedOutCommands;
    }

    public void listCommands(StreamPrinter printer, List<Command> commands) {
        for (var command : commands) {
            listCommand(printer, command);
        }
        printer.setTextColor(Color.NONE);
    }

    private void listCommand(StreamPrinter printer, Command command) {
        printer.setTextColor(Color.BLUE);
        printer.print(command.name() + " ");

        if (command.args() != null && !command.args().isEmpty()) {
            for (var arg : command.args()) {
                printArgumentFormat(printer, arg);
            }
        }

        printer.setTextColor(Color.MAGENTA);
        printer.print("- " + command.description() + "\n");
    }

    private void printArgumentFormat(StreamPrinter printer, CommandArgument arg) {
        Class<?> type = arg.type();
        if (type.isEnum()) {
            printEnumOptions(printer, type);
        } else if (!arg.required()) {
            printer.print("[" + arg.name().toUpperCase() + "] ");
        } else {
            printer.print("<" + arg.name().toUpperCase() + "> ");
        }
    }

    private void printEnumOptions(StreamPrinter printer, Class<?> enumType) {
        var options = enumType.getEnumConstants();
        printer.print("[");
        for (int i = 0; i < options.length; i++) {
            printer.print(options[i].toString().toUpperCase());
            if (i < options.length - 1) {
                printer.print("|");
            }
        }
        printer.print("] ");
    }

    public void interpretCommand(String line) throws InvalidCommandException {
        try (var lineScanner = new Scanner(line).useDelimiter(" ")) {
            var commandName = lineScanner.next();
            var command = findCommand(commandName, getAvailableCommands());
            if (command == null) {
                throw new InvalidCommandException("Unknown Command (Type \"help\" for list of available commands)");
            }
            Object[] argValues = parseArguments(lineScanner, command);
            command.handler().accept(argValues);
        }
    }

    private Command findCommand(String name, List<Command> commands) {
        for (var command : commands) {
            if (command.name().equalsIgnoreCase(name)) {
                return command;
            }
        }
        return null;
    }

    private Object[] parseArguments(Scanner lineScanner, Command command) throws InvalidCommandException {
        if (command.args() == null || command.args().isEmpty()) {
            return null;
        }

        Object[] argValues = new Object[command.args().size()];
        for (int i = 0; i < command.args().size(); i++) {
            argValues[i] = parseArgument(lineScanner, command.args().get(i));
        }
        return argValues;
    }

    private Object parseArgument(Scanner scanner, CommandArgument arg) throws InvalidCommandException {
        Class<?> type = arg.type();
        if (!scanner.hasNext()) {
            if (arg.required()) {
                throw new InvalidCommandException("Missing Argument " + arg.name());
            } else {
                return null;
            }
        }

        if (type == String.class) {
            return scanner.next();
        } else if (type == Character.class) {
            return scanner.next().charAt(0);
        } else if (type == Integer.class) {
            if (!scanner.hasNextInt()) {
                throw new InvalidCommandException("Invalid Argument " + arg.name());
            }
            return scanner.nextInt();
        } else if (type.isEnum()) {
            return parseEnumArgument(scanner, type, arg.name());
        }
        throw new InvalidCommandException("Unknown argument type");
    }

    private Object parseEnumArgument(Scanner scanner, Class<?> enumType, String argName)
            throws InvalidCommandException {
        String value = scanner.next();
        for (var option : enumType.getEnumConstants()) {
            if (option.toString().equalsIgnoreCase(value)) {
                return option;
            }
        }
        throw new InvalidCommandException("Invalid Argument " + argName);
    }
}
