package org.bovinegenius.kurgan.types;

import java.math.BigInteger;

import org.bovinegenius.kurgan.ConfigTypeErrorException;
import org.bovinegenius.kurgan.yaml.YamlUtils;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;

import static org.bovinegenius.kurgan.types.Matchers.*;


import lombok.Value;

@Value
public class ConfigInt implements ConfigType {
    public static final ConfigInt value = new ConfigInt();
    
    @Override
    public Object coerce(Node node) {
        String value = YamlUtils.getValue((ScalarNode)node);
        BigInteger bigInt = Coercions.parseInt(value);
        return bigInt == null ? null : bigInt.intValue();
    }

    @Override
    public void typeCheck(Node node) throws ConfigTypeErrorException {
        validateInteger(node);
    }

    @Override
    public String toString() {
        return "Integer";
    }
}
