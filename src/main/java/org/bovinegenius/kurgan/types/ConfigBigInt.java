package org.bovinegenius.kurgan.types;

import static org.bovinegenius.kurgan.types.Matchers.validateBigInt;

import org.bovinegenius.kurgan.ConfigTypeErrorException;
import org.bovinegenius.kurgan.yaml.YamlUtils;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;


public class ConfigBigInt implements ConfigType {
    public static final ConfigBigInt value = new ConfigBigInt();
    
    @Override
    public Object coerce(Node node) {
        String value = YamlUtils.getValue((ScalarNode)node);
        return Coercions.parseInt(value);
    }

    @Override
    public void typeCheck(Node node) throws ConfigTypeErrorException {
        validateBigInt(node);
    }

    @Override
    public String toString() {
        return "BigInteger";
    }
}
