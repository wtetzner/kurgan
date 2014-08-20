package org.bovinegenius.kurgan.types;

import static java.lang.String.format;

import java.net.URI;
import java.net.URISyntaxException;

import org.bovinegenius.kurgan.ConfigException;
import org.bovinegenius.kurgan.ConfigTypeErrorException;
import org.bovinegenius.kurgan.yaml.YamlUtils;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;


public class ConfigUri implements ConfigType {
    public static final ConfigUri value = new ConfigUri();

    @Override
    public Object coerce(Node node) {
        try {
            String value = YamlUtils.getValue((ScalarNode)node);
            if (value == null) return null;
            return new URI(value);
        } catch (URISyntaxException e) {
            throw new ConfigException(format("Failed to coerce URI: %s", value), e);
        }
    }

    @Override
    public void typeCheck(Node node) throws ConfigTypeErrorException {
        if (YamlUtils.isNull(node)) return;
        if (!(node instanceof ScalarNode)) {
            throw new ConfigTypeErrorException(node, format("Expected %s, found %s", this.toString(), node.getNodeId()));
        }
        ScalarNode sNode = (ScalarNode)node;
        String value = YamlUtils.getValue(sNode);
        try {
            new URI(value);
        } catch (URISyntaxException e) {
            throw new ConfigTypeErrorException(node, format("Invalid URI: %s", value), e);
        }
    }

    @Override
    public String toString() {
        return "URI";
    }
}
