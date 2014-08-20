package org.bovinegenius.kurgan.types;

import static org.bovinegenius.kurgan.types.Matchers.validateByte;

import java.math.BigInteger;

import org.bovinegenius.kurgan.ConfigTypeErrorException;
import org.bovinegenius.kurgan.yaml.YamlUtils;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;


public class ConfigByte implements ConfigType {
    public static final ConfigByte value = new ConfigByte();
    
    @Override
    public Object coerce(Node node) {
        String value = YamlUtils.getValue((ScalarNode)node);
        BigInteger bigInt = Coercions.parseInt(value);
        return bigInt == null ? null : bigInt.byteValue();
    }

    @Override
    public void typeCheck(Node node) throws ConfigTypeErrorException {
        validateByte(node);
    }

    @Override
    public String toString() {
        return "Byte";
    }
}
