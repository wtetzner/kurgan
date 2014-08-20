package org.bovinegenius.kurgan.types;

import org.bovinegenius.kurgan.ConfigTypeErrorException;
import org.bovinegenius.kurgan.yaml.YamlUtils;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;


public class ConfigString implements ConfigType {
    public static final ConfigString value = new ConfigString();
    
    @Override
    public Object coerce(Node node) {
        return YamlUtils.getValue((ScalarNode)node);
    }

    @Override
    public void typeCheck(Node node) throws ConfigTypeErrorException {
        if (!(node instanceof ScalarNode)) {
            throw new ConfigTypeErrorException(node, String.format("Expected %s, found %s", this.toString(), node.getNodeId()));
        }
    }
    
    @Override
    public String toString() {
        return "String";
    }
}
