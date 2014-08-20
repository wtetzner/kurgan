package org.bovinegenius.kurgan.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.NonNull;
import lombok.Value;

import org.bovinegenius.kurgan.ConfigTypeErrorException;
import org.bovinegenius.kurgan.yaml.YamlUtils;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.SequenceNode;


@Value
public class ConfigList implements ConfigType {
    @NonNull ConfigType innerType;

    @Override
    public Object coerce(Node node) {
        SequenceNode listNode = (SequenceNode)node;
        List<Node> nodes = listNode.getValue();
        List<Object> results = new ArrayList<>();
        for (Node innerNode : nodes) {
            results.add(innerType.coerce(innerNode));
        }
        return Collections.unmodifiableList(results);
    }

    @Override
    public void typeCheck(Node node) throws ConfigTypeErrorException {
        if (YamlUtils.isNull(node)) {
            return;
        }
        if (!(node instanceof SequenceNode)) {
            throw new ConfigTypeErrorException(node, String.format("Expected a %s, found %s", this.toString(), node.getTag()));
        } else {
            SequenceNode listNode = (SequenceNode)node;
            for (Node innerNode : listNode.getValue()) {
                innerType.typeCheck(innerNode);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("List<%s>", innerType.toString());
    }
}
