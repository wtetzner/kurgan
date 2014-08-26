package org.bovinegenius.kurgan.types;

import org.bovinegenius.kurgan.ConfigTypeErrorException;
import org.yaml.snakeyaml.nodes.Node;

public class ConfigCollection implements ConfigType {
    private final ConfigList confType;
    public ConfigCollection(ConfigType innerType) {
        this.confType = new ConfigList(innerType);
    }

    @Override
    public Object coerce(Node node) {
        return confType.coerce(node);
    }
    @Override
    public void typeCheck(Node node) throws ConfigTypeErrorException {
        confType.typeCheck(node);
    }
    
    @Override
    public String toString() {
        return String.format("Collection<%s>", confType.getInnerType());
    }
}
