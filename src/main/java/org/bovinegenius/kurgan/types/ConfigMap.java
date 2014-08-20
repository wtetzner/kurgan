package org.bovinegenius.kurgan.types;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Value;

import org.bovinegenius.kurgan.ConfigTypeErrorException;
import org.bovinegenius.kurgan.yaml.YamlUtils;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;


@Value
public class ConfigMap implements ConfigType {
    ConfigType keyType;
    ConfigType valueType;

    @Override
    public Object coerce(Node node) {
        MappingNode mNode = (MappingNode)node;
        Map<Object,Object> map = new LinkedHashMap<>();
        for (NodeTuple field : mNode.getValue()) {
            map.put(keyType.coerce(field.getKeyNode()), valueType.coerce(field.getValueNode()));
        }
        return Collections.unmodifiableMap(map);
    }

    @Override
    public void typeCheck(Node node) throws ConfigTypeErrorException {
        if (YamlUtils.isNull(node)) {
            return;
        }
        if (!(node instanceof MappingNode)) {
            throw new ConfigTypeErrorException(node, String.format("Expected %s, found %s",
                    this.toString(),
                    node.getNodeId().toString()));
        }
        MappingNode mNode = (MappingNode)node;
        List<NodeTuple> fields = mNode.getValue();
        Map<Object,Node> soFar = new HashMap<>();
        for (NodeTuple field : fields) {
            Node keyNode = field.getKeyNode();
            Object key = keyType.coerce(keyNode);
            if (soFar.containsKey(key)) {
                Node valNode = soFar.get(key);
                throw new ConfigTypeErrorException(keyNode, String.format("Duplicate key %s at %s:%s:%s; already exists at %s:%s",
                        key,
                        keyNode.getStartMark().getName(),
                        keyNode.getStartMark().getLine(),
                        keyNode.getStartMark().getColumn(),
                        valNode.getStartMark().getLine(),
                        valNode.getStartMark().getColumn()));
            }
            soFar.put(key, keyNode);
            keyType.typeCheck(field.getKeyNode());
            valueType.typeCheck(field.getValueNode());
        }
    }

    @Override
    public String toString() {
        return String.format("Map<%s,%s>", keyType, valueType);
    }
}
