package service.result;

import java.util.List;

public record CommandResult(int gameID, List<OutboundWSMessage> outbound) {
}