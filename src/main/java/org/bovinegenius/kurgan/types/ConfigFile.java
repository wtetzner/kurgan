package org.bovinegenius.kurgan.types;

import static java.lang.String.format;

import java.io.File;

import org.bovinegenius.kurgan.ConfigTypeErrorException;
import org.bovinegenius.kurgan.yaml.YamlUtils;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;


public class ConfigFile implements ConfigType {
    public static final ConfigFile value = new ConfigFile();

    @Override
    public Object coerce(Node node) {
        String value = YamlUtils.getValue((ScalarNode)node);
        if (value == null) return null;
        return new File(value);
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
            new File(value);
        } catch (Exception e) {
            throw new ConfigTypeErrorException(node, format("Invalid File: %s", value), e);
        }
    }

    @Override
    public String toString() {
        return "File";
    }
}
