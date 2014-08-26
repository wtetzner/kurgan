package org.bovinegenius.kurgan.types;

import static java.lang.String.format;

import org.bovinegenius.kurgan.ConfigTypeErrorException;
import org.bovinegenius.kurgan.yaml.YamlUtils;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;

public class ConfigClass implements ConfigType {
    public static ConfigClass value = new ConfigClass();

    private Class<?> lookupClass(Node node) {
        if (!(node instanceof ScalarNode)) {
            throw new ConfigTypeErrorException(node, format("Expected %s, found %s", this.toString(), node.getNodeId()));
        }
        ScalarNode sNode = (ScalarNode)node;
        String value = YamlUtils.getValue(sNode);
        try {
            return Class.forName(value);
        } catch (Exception e) {
            throw new ConfigTypeErrorException(sNode, format("Unknown class %s", value), e);
        }
    }

    @Override
    public Object coerce(Node node) {
        return lookupClass(node);
    }

    @Override
    public void typeCheck(Node node) throws ConfigTypeErrorException {
        lookupClass(node);
    }

    @Override
    public String toString() {
        return "Class";
    }
}
