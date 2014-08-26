package org.bovinegenius.kurgan.types;

import static java.lang.String.format;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.NonNull;
import lombok.Value;

import org.bovinegenius.kurgan.ConfigTypeErrorException;
import org.bovinegenius.kurgan.yaml.YamlUtils;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.SequenceNode;


@Value
public class ConfigSet implements ConfigType {
    @NonNull ConfigType innerType;

    @Override
    public Object coerce(Node node) {
        SequenceNode listNode = (SequenceNode)node;
        List<Node> nodes = listNode.getValue();
        Set<Object> results = new HashSet<>();
        for (Node innerNode : nodes) {
            results.add(innerType.coerce(innerNode));
        }
        return Collections.unmodifiableSet(results);
    }

    @Override
    public void typeCheck(Node node) throws ConfigTypeErrorException {
        if (YamlUtils.isNull(node)) {
            return;
        }
        if (!(node instanceof SequenceNode)) {
            throw new ConfigTypeErrorException(node, format("Expected a %s, found %s", this.toString(), node.getTag()));
        } else {
            SequenceNode listNode = (SequenceNode)node;
            Set<Object> results = new HashSet<>();
            for (Node innerNode : listNode.getValue()) {
                innerType.typeCheck(innerNode);
                Object obj = innerType.coerce(innerNode);
                if (results.contains(obj)) {
                    throw new ConfigTypeErrorException(innerNode, "Duplicate entry in set");
                }
                results.add(obj);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Set<%s>", innerType.toString());
    }
}
