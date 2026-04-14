package client;

import java.util.List;
import java.util.Scanner;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.ChessGame.TeamColor;
import chess.ChessPiece.PieceType;
import client.response.CreateGameResponse;
import client.response.ListGamesResponse;
import client.response.LoginResponse;
import ui.ChessBoardPrinter;
import ui.CommandPrinter;
import ui.Color;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

public class Client {
    private boolean isLoggedIn = false;
    private String authToken = null;
    private ChessGame currentGame = null;
    private int currentGameID = 0;
    private TeamColor teamColor = null;
    private ServerFacade server;
    private ChessBoardPrinter printer;

    private final List<Command> loggedOutCommands;
    private final List<Command> loggedInCommands;
    private final List<Command> gameplayCommands;
    private final List<Command> observeCommands;

    public Client(String hostname, int port, ChessBoardPrinter printer) {
        this.server = new ServerFacade(hostname, port);
        this.printer = printer;
        Command helpCommand = createHelpCommand();
        Command quitCommand = createQuitCommand();
        Command leaveCommand = createLeaveCommand();
        Command redrawCommand = createRedrawCommand();
        Command highlightCommand = createHighlightCommand();

        loggedOutCommands = createLoggedOutCommands(helpCommand, quitCommand);
        loggedInCommands = createLoggedInCommands(helpCommand, quitCommand);
        gameplayCommands = createGameplayCommands(helpCommand, leaveCommand, redrawCommand, highlightCommand);
        observeCommands = List.of(redrawCommand, leaveCommand, highlightCommand, helpCommand);
    }

    private Command createHelpCommand() {
        return new Command("help", "with possible commands", null, (commandArgs) -> {
            listCommands(printer, getAvailableCommands());
        });
    }

    private Command createQuitCommand() {
        return new Command("quit", "playing chess", null, (commandArgs) -> {
            if (isLoggedIn) {
                logout();
            }
            printer.println("Bye!");
            System.exit(0);
        });
    }

    private Command createLeaveCommand() {
        return new Command("leave", "the game", null, (commandArgs) -> {
            leaveGame();
        });
    }

    private Command createRedrawCommand() {
        return new Command("redraw", "the chess board", null, (commandArgs) -> {
            drawBoard(currentGame.getBoard());
        });
    }

    private Command createHighlightCommand() {
        return new Command("highlight", "all legal moves for piece",
                List.of(new CommandArgument("piece", String.class, true)), (commandArgs) -> {
                    String pieceLocation = (String) commandArgs[0];
                    ChessPosition position;
                    try {
                        position = ChessPosition.fromString(pieceLocation);
                    } catch (IllegalArgumentException e) {
                        printErrorMessage("Invalid position format, please use format <column><row> (e.g. a1)");
                        return;
                    }
                    printer.newline();
                    var validMoves = currentGame.validMoves(position);
                    printer.drawBoard(currentGame.getBoard(), teamColor != null ? teamColor == TeamColor.BLACK : false,
                            validMoves);
                });
    }

    private List<Command> createLoggedOutCommands(Command helpCommand, Command quitCommand) {
        return List.of(
                new Command("register", "to create an account",
                        List.of(new CommandArgument("username", String.class, true),
                                new CommandArgument("password", String.class, true),
                                new CommandArgument("email", String.class, true)),
                        (commandArgs) -> register((String) commandArgs[0], (String) commandArgs[1],
                                (String) commandArgs[2])),
                new Command("login", "to play chess",
                        List.of(new CommandArgument("username", String.class, true),
                                new CommandArgument("password", String.class, true)),
                        (commandArgs) -> login((String) commandArgs[0], (String) commandArgs[1])),
                quitCommand,
                helpCommand);
    }

    private List<Command> createLoggedInCommands(Command helpCommand, Command quitCommand) {
        return List.of(
                new Command("create", "a game", List.of(new CommandArgument("name", String.class, true)),
                        (commandArgs) -> createGame((String) commandArgs[0])),
                new Command("list", "games", null, (commandArgs) -> listGames()),
                new Command("join", "a game",
                        List.of(new CommandArgument("id", Integer.class, true),
                                new CommandArgument("color", TeamColor.class, true)),
                        (commandArgs) -> joinGame((int) commandArgs[0], (TeamColor) commandArgs[1])),
                new Command("observe", "a game", List.of(new CommandArgument("id", Integer.class, true)),
                        (commandArgs) -> observeGame((int) commandArgs[0])),
                new Command("logout", "when you are done", null, (commandArgs) -> logout()),
                quitCommand,
                helpCommand);
    }

    private List<Command> createGameplayCommands(Command helpCommand, Command leaveCommand,
            Command redrawCommand, Command highlightCommand) {
        return List.of(
                redrawCommand,
                leaveCommand,
                createMoveCommand(),
                new Command("resign", "the game", null, (commandArgs) -> resign()),
                highlightCommand,
                helpCommand);
    }

    private Command createMoveCommand() {
        return new Command("move", "a chess piece",
                List.of(new CommandArgument("from", String.class, true),
                        new CommandArgument("to", String.class, true),
                        new CommandArgument("promotion", Character.class, false)),
                (commandArgs) -> {
                    ChessMove move = parseMove(commandArgs);
                    if (move == null) {
                        return;
                    }
                    try {
                        server.sendGameCommand(new MakeMoveCommand(authToken, currentGameID, move));
                    } catch (ConnectionErrorException e) {
                        printErrorMessage("Unable to connect to server, please try again");
                    }
                });
    }

    private ChessMove parseMove(Object[] commandArgs) {
        String from = (String) commandArgs[0];
        String to = (String) commandArgs[1];

        ChessPosition startPosition;
        ChessPosition endPosition;
        try {
            startPosition = ChessPosition.fromString(from);
            endPosition = ChessPosition.fromString(to);
        } catch (IllegalArgumentException e) {
            printErrorMessage("Invalid move format, please use format <column><row> (e.g. a1)");
            return null;
        }

        if (commandArgs[2] != null) {
            char pieceType = (Character) commandArgs[2];
            PieceType promotionPiece;
            try {
                promotionPiece = ChessPiece.pieceTypeFromChar(pieceType);
            } catch (IllegalArgumentException e) {
                printErrorMessage("Invalid promotion piece type, valid types are Q, R, B, and N");
                return null;
            }
            return new ChessMove(startPosition, endPosition, promotionPiece);
        }

        return new ChessMove(startPosition, endPosition);
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
            server.connectToGame(authToken, gameID, this::handleServerMessage);
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
        teamColor = color;
        currentGameID = gameID;
    }

    private void leaveGame() {
        try {
            server.leaveGame(authToken, currentGameID);
        } catch (ConnectionErrorException e) {
            printErrorMessage("Unable to connect to server, please try again");
        }
        currentGame = null;
        currentGameID = 0;
        teamColor = null;
    }

    private void resign() {
        printer.print("Are you sure you want to resign? (y/n)");
        String response = ClientMain.scanner.nextLine();
        if (!response.equalsIgnoreCase("y")) {
            printer.println("Resignation cancelled");
            return;
        }
        try {
            server.sendGameCommand(new ResignCommand(authToken, currentGameID));
        } catch (ConnectionErrorException e) {
            printErrorMessage("Unable to connect to server, please try again");
        }
    }

    private void handleServerMessage(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME:
                currentGame = ((LoadGameMessage) message).getGame();
                drawBoard(currentGame.getBoard());
                break;
            case NOTIFICATION:
                printer.println(((NotificationMessage) message).getMessage());
                break;
            case ERROR:
                printErrorMessage(((ErrorMessage) message).getErrorMessage());
                break;
        }
    }

    private void observeGame(int gameID) {
        try {
            server.connectToGame(authToken, gameID, this::handleServerMessage);
        } catch (ConnectionErrorException e) {
            printErrorMessage("Unable to connect to server, please try again");
        }
        this.currentGameID = gameID;
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

    private void drawBoard(ChessBoard board) {
        printer.newline();
        printer.drawBoard(board, teamColor != null ? teamColor == TeamColor.BLACK : false);
        printer.newline();
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
        if (isLoggedIn) {
            if (currentGame != null) {
                if (teamColor != null) {
                    return gameplayCommands;
                }
                return observeCommands;
            }
            return loggedInCommands;
        }
        return loggedOutCommands;
    }

    public void listCommands(ChessBoardPrinter printer, List<Command> commands) {
        CommandPrinter.listCommands(printer, commands);
    }

    public void interpretCommand(String line) throws InvalidCommandException {
        if (line == null || line.isBlank()) {
            return;
        }
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
