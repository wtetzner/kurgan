package org.bovinegenius.kurgan.types;

import static org.bovinegenius.kurgan.types.Matchers.*;

import java.math.BigInteger;

import org.bovinegenius.kurgan.ConfigTypeErrorException;
import org.bovinegenius.kurgan.yaml.YamlUtils;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;


public class ConfigLong implements ConfigType {
    public static final ConfigLong value = new ConfigLong();
    
    @Override
    public Object coerce(Node node) {
        String value = YamlUtils.getValue((ScalarNode)node);
        BigInteger bigInt = Coercions.parseInt(value);
        return bigInt == null ? null : bigInt.longValue();
    }

    @Override
    public void typeCheck(Node node) throws ConfigTypeErrorException {
        validateLong(node);
    }

    @Override
    public String toString() {
        return "Long";
    }
}
