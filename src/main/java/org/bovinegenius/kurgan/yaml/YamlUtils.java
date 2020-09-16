package org.bovinegenius.kurgan.yaml;

import static java.lang.String.format;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.parser.ParserImpl;
import org.yaml.snakeyaml.reader.StreamReader;

@CommonsLog
public class YamlUtils {
    private static class NYaml extends Yaml {
        public Node compose(StreamReader yaml) {
            Composer composer = new Composer(new ParserImpl(yaml), resolver);
            constructor.setComposer(composer);
            return composer.getSingleNode();
        }

        public Iterable<Node> composeAll(StreamReader yaml) {
            final Composer composer = new Composer(new ParserImpl(yaml), resolver);
            constructor.setComposer(composer);
            final Iterator<Node> result = new Iterator<Node>() {
                public boolean hasNext() {
                    return composer.checkNode();
                }

                public Node next() {
                    return composer.getNode();
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
            return new Iterable<Node>() {
                public Iterator<Node> iterator() {
                    return result;
                }
            };
        }
    }

    private static NYaml getYaml() {
        return new NYaml();
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
            StreamReader streamReader = new NamedStreamReader(name, reader);
            List<Node> results = new ArrayList<>();
            NYaml yaml = getYaml();
            yaml.setName(name);
            for (Node node : yaml.composeAll(streamReader)) {
                results.add(node);
            }
            return Collections.unmodifiableList(results);
        }
    }

    public static Iterable<Node> readNodes(String name, String location) throws IOException {
        return readNodes(name, configInput(location));
    }

    private static class NamedStreamReader extends StreamReader {
        public NamedStreamReader(String name, Reader reader) {
            super(reader);
            try {
                Field field = StreamReader.class.getDeclaredField("name");
                field.setAccessible(true);
                field.set(this, name);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    public static Node readNode(String name, InputStream in) throws IOException {
        try (Reader reader = new InputStreamReader(in)) {
            StreamReader streamReader = new NamedStreamReader(name, reader);
            NYaml yaml = getYaml();
            yaml.setName(name);
            return yaml.compose(streamReader);
        }
    }

    public static Node readNode(String name, String location) throws IOException {
        return readNode(name, configInput(location));
    }

    public static String getValue(ScalarNode node) {
        String value = node.getValue();
        if (value.equals("null") && node.isPlain()) {
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
        String value = node.getValue();
        if (!node.isPlain()) {
            return node.getScalarStyle() + value + node.getScalarStyle();
        } else {
            return value;
        }
    }
}
