package client;

import java.util.Scanner;

import ui.ChessBoardPrinter;
import ui.Color;

public class ClientMain {

    public static ChessBoardPrinter printer = new ChessBoardPrinter(System.out);
    public static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 8080;
        if (args.length == 2) {
            hostname = args[0];
            port = Integer.parseInt(args[1]);
        }

        var client = new Client(hostname, port, printer);

        System.out.println("♕  Welcome to 240 Chess. Type help to get started. ♕");
        while (true) {
            printPrompt(client.isLoggedIn());
            String input = input();
            try {
                client.interpretCommand(input);
            } catch (InvalidCommandException e) {
                printError(e);
            }
        }
    }

    public static void printPrompt(boolean isLoggedIn) {
        printer.print("[");
        if (isLoggedIn) {
            printer.print("LOGGED IN");
        } else {
            printer.print("LOGGED OUT");
        }
        printer.print("] >>> ");
    }

    public static void printError(Exception e) {
        printer.setTextColor(Color.RED);
        printer.println("Unable to execute command: " + e.getMessage());
        printer.setTextColor(Color.NONE);
    }

    public static String input() {
        printer.setTextColor(Color.GREEN);
        String line = scanner.nextLine();
        printer.setTextColor(Color.NONE);
        return line;
    }
}
