package org.bovinegenius.kurgan.yaml;

import static java.lang.String.format;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;

@CommonsLog
public class YamlUtils {
    private static Yaml getYaml() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(options);
    }

    public static InputStream configInput(String configPath) {
        try {
            if (configPath == null)
                throw new RuntimeException("Invalid configPath: null");
            if (configPath.startsWith("classpath:")) {
                String path = configPath.substring(10);
                log.debug(format("Loading config from classpath: %s", path));
                return YamlUtils.class.getClassLoader().getResourceAsStream(path);
            } else {
                log.debug(format("Loading config from file: %s", configPath));
                return new FileInputStream(configPath);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Iterable<Node> readNodes(String name, InputStream in) throws IOException {
        try (Reader reader = new InputStreamReader(in)) {
            List<Node> results = new ArrayList<>();
            Yaml yaml = getYaml();
            yaml.setName(name);
            for (Node node : yaml.composeAll(reader)) {
                results.add(node);
            }
            return Collections.unmodifiableList(results);
        }
    }

    public static Iterable<Node> readNodes(String name, String location) throws IOException {
        return readNodes(name, configInput(location));
    }

    public static String getValue(ScalarNode node) {
        int style = (int)node.getStyle();
        String value = node.getValue();
        if (value.equals("null") && style == 0) {
            return null;
        } else {
            return value;
        }
    }

    public static boolean isNull(Node node) {
        if (node instanceof ScalarNode) {
            return getValue((ScalarNode)node) == null;
        } else {
            return false;
        }
    }

    public static String printScalar(ScalarNode node) {
        int style = (int)node.getStyle();
        String value = node.getValue();
        if (style != 0) {
            return node.getStyle() + value + node.getStyle();
        } else {
            return value;
        }
    }
}
