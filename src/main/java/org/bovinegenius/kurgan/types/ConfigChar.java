package org.bovinegenius.kurgan.types;

import org.bovinegenius.kurgan.ConfigTypeErrorException;
import org.bovinegenius.kurgan.yaml.YamlUtils;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;


public class ConfigChar implements ConfigType {
    public static final ConfigChar value = new ConfigChar();
    
    @Override
    public Object coerce(Node node) {
        String value = YamlUtils.getValue((ScalarNode)node);
        if (value == null) {
            return null;
        } else {
            return value.charAt(0);
        }
    }

    @Override
    public void typeCheck(Node node) throws ConfigTypeErrorException {
        String value = YamlUtils.getValue((ScalarNode)node);
        if (value != null && value.length() != 1) {
            throw new ConfigTypeErrorException(node, String.format("Expected %s, found %s",
                    this.toString(),
                    YamlUtils.printScalar((ScalarNode)node)));
        }
    }

    @Override
    public String toString() {
        return "Character";
    }
}
