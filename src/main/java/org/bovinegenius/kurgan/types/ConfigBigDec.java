package org.bovinegenius.kurgan.types;

import static java.lang.String.format;

import java.math.BigDecimal;

import org.bovinegenius.kurgan.ConfigTypeErrorException;
import org.bovinegenius.kurgan.yaml.YamlUtils;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;


public class ConfigBigDec implements ConfigType {
    public static final ConfigBigDec value = new ConfigBigDec();

    @Override
    public Object coerce(Node node) {
        String value = YamlUtils.getValue((ScalarNode)node);
        if (value == null) return null;
        return new BigDecimal(value);
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
            new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new ConfigTypeErrorException(node, format("Expected %s, found %s", this.toString(), value), e);
        }
    }

    @Override
    public String toString() {
        return "BigDecimal";
    }
}
