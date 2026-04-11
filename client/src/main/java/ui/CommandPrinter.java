package ui;

import java.util.List;

import client.Command;
import client.CommandArgument;

public class CommandPrinter {

    private CommandPrinter() {
    }

    public static void listCommands(StreamPrinter printer, List<Command> commands) {
        for (var command : commands) {
            listCommand(printer, command);
        }
        printer.setTextColor(Color.NONE);
    }

    private static void listCommand(StreamPrinter printer, Command command) {
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

    private static void printArgumentFormat(StreamPrinter printer, CommandArgument arg) {
        Class<?> type = arg.type();
        if (type.isEnum()) {
            printEnumOptions(printer, type);
        } else if (!arg.required()) {
            printer.print("[" + arg.name().toUpperCase() + "] ");
        } else {
            printer.print("<" + arg.name().toUpperCase() + "> ");
        }
    }

    private static void printEnumOptions(StreamPrinter printer, Class<?> enumType) {
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
}
