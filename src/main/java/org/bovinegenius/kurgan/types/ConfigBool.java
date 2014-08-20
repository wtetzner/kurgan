package org.bovinegenius.kurgan.types;

import org.bovinegenius.kurgan.ConfigTypeErrorException;
import org.bovinegenius.kurgan.yaml.YamlUtils;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;


public class ConfigBool implements ConfigType {
    public static final ConfigBool value = new ConfigBool();
    
    @Override
    public Object coerce(Node node) {
        String value = YamlUtils.getValue((ScalarNode)node);
        if (value == null) {
            return null;
        } else {
            return value.equalsIgnoreCase("true");
        }
    }

    @Override
    public void typeCheck(Node node) throws ConfigTypeErrorException {
        if (!(node instanceof ScalarNode)) {
            throw new ConfigTypeErrorException(node, String.format("Expected %s, found %s",
                    this.toString(),
                    node.getNodeId()));
        }
        String value = YamlUtils.getValue((ScalarNode)node);
        if (value != null) {
            if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                throw new ConfigTypeErrorException(node, String.format("Expected %s, found %s",
                        this.toString(),
                        value));
            }
        }
    }

    @Override
    public String toString() {
        return "Boolean";
    }
}
