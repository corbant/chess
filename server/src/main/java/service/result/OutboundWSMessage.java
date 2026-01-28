package service.result;

public record OutboundWSMessage(Target target, Object payload) {
    public enum Target {
        SELF, OTHERS, ALL
    }
}
