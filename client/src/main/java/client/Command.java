package client;

import java.util.List;
import java.util.Map;

public record Command(String name, String description, List<Map.Entry<String, Class<?>>> args) {

}
