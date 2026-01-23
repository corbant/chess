package client;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public record Command(String name, String description, List<CommandArgument> args,
                Consumer<Object[]> handler) {

}
