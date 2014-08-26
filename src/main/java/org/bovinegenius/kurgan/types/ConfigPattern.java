package org.bovinegenius.kurgan.types;

import static java.lang.String.format;

import java.util.regex.Pattern;

import org.bovinegenius.kurgan.ConfigTypeErrorException;
import org.bovinegenius.kurgan.yaml.YamlUtils;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;

public class ConfigPattern implements ConfigType {
    public static final ConfigPattern value = new ConfigPattern();

    @Override
    public Object coerce(Node node) {
        try {
            String value = YamlUtils.getValue((ScalarNode)node);
            return Pattern.compile(value);
        } catch (Exception e) {
            throw new ConfigTypeErrorException(node, format("Invalid regex - %s", value), e);
        }
    }

    @Override
    public void typeCheck(Node node) throws ConfigTypeErrorException {
        if (!(node instanceof ScalarNode)) {
            throw new ConfigTypeErrorException(node, String.format("Expected %s, found %s", this.toString(), node.getNodeId()));
        }
        coerce(node);
    }

    @Override
    public String toString() {
        return "Pattern";
    }
}
