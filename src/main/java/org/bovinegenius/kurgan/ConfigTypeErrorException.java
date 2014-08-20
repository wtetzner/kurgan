package org.bovinegenius.kurgan;

import lombok.Getter;
import lombok.NonNull;

import org.bovinegenius.kurgan.string.StringUtils;
import org.yaml.snakeyaml.nodes.Node;


public class ConfigTypeErrorException extends ConfigException {
    @Getter @NonNull private final Node node;
    @Getter @NonNull private final String plainMessage;

    public ConfigTypeErrorException(@NonNull Node node, @NonNull String message, @NonNull Throwable cause) {
        super(fullMessage(node, message), cause);
        this.node = node;
        this.plainMessage = message;
    }

    public ConfigTypeErrorException(@NonNull Node node, @NonNull String message) {
        super(fullMessage(node, message));
        this.node = node;
        this.plainMessage = message;
    }

    private static String fullMessage(Node node, String message) {
        return String.format("%s:%s:%s Type Mismatch: %s\n%s",
                node.getStartMark().getName(),
                node.getStartMark().getLine() + 1,
                node.getStartMark().getColumn(),
                message,
                StringUtils.indent(node.getStartMark().get_snippet(), 2));
    }
}
