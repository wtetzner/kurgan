package org.bovinegenius.kurgan;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.bovinegenius.kurgan.types.ConfigType;
import org.bovinegenius.kurgan.yaml.YamlUtils;
import org.yaml.snakeyaml.composer.ComposerException;
import org.yaml.snakeyaml.nodes.Node;


public class ConfigLoader {
    @SuppressWarnings("unchecked")
    public static <T> Iterable<T> loadAllYaml(Class<T> cls, String location) {
        try {
            ConfigType type = ConfigType.Conversions.asConfigType(cls);
            Iterable<Node> nodes = YamlUtils.readNodes(location, location);
            List<T> results = new ArrayList<>();
            for (Node node : nodes) {
                type.typeCheck(node);
                results.add((T)type.coerce(node));
            }
            return results;
        } catch (IOException e) {
            throw new ConfigException(format("Failed to load config from %s", location), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Iterable<T> loadAllYaml(Class<T> cls, String location, InputStream input) {
        try {
            ConfigType type = ConfigType.Conversions.asConfigType(cls);
            Iterable<Node> nodes = YamlUtils.readNodes(location, input);
            List<T> results = new ArrayList<>();
            for (Node node : nodes) {
                type.typeCheck(node);
                results.add((T)type.coerce(node));
            }
            return results;
        } catch (IOException e) {
            throw new ConfigException(format("Failed to load config from %s", location), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T loadYaml(Class<T> cls, String location) {
        try {
            ConfigType type = ConfigType.Conversions.asConfigType(cls);
            Node node = YamlUtils.readNode(location, location);
            type.typeCheck(node);
            return (T)type.coerce(node);
        } catch (IOException e) {
            throw new ConfigException(format("Failed to load config from %s", location), e);
        } catch (ComposerException e) {
            throw new ConfigException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T loadYaml(Class<T> cls, String location, InputStream input) {
        try {
            ConfigType type = ConfigType.Conversions.asConfigType(cls);
            Node node = YamlUtils.readNode(location, input);
            type.typeCheck(node);
            return (T)type.coerce(node);
        } catch (IOException e) {
            throw new ConfigException(format("Failed to load config from %s", location), e);
        } catch (ComposerException e) {
            throw new ConfigException(e.getMessage(), e);
        }
    }
}
