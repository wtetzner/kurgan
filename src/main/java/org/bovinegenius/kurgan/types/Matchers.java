package org.bovinegenius.kurgan.types;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;

import org.bovinegenius.kurgan.ConfigTypeErrorException;
import org.bovinegenius.kurgan.yaml.YamlUtils;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;


public class Matchers {
    public static Pattern integer = Pattern.compile("^(-?(\\d+|0x[a-fA-F0-9]+|0b[01]+))$");

    public static boolean matches(@NonNull Pattern regex, String value) {
        if (value == null) {
            return true;
        }
        Matcher matcher = regex.matcher(value);
        return matcher.matches();
    }

    public static boolean matches(@NonNull Pattern regex, Node node, String expectedType) {
        if (!(node instanceof ScalarNode)) {
            throw new ConfigTypeErrorException(node, String.format("Expected %s, found %s", expectedType, node.getNodeId()));
        } else {
            ScalarNode sNode = (ScalarNode)node;
            return matches(regex, YamlUtils.getValue(sNode));
        }
    }

    public static void validate(@NonNull Pattern regex, Node node, String expectedType) {
        if (YamlUtils.isNull(node)) return;
        if (!matches(regex, node, expectedType)) {
            ScalarNode sNode = (ScalarNode)node;
            throw new ConfigTypeErrorException(node, String.format("Expected %s, found %s", expectedType, YamlUtils.printScalar((ScalarNode)sNode)));
        }
    }

    private static BigInteger readInt(Node node) {
        String value = YamlUtils.getValue((ScalarNode)node);
        return Coercions.parseInt(value);
    }

    private static void validateIntSize(Node node, long max, long min) {
        BigInteger bigInt = readInt(node);
        if (bigInt == null) return;
        if (bigInt.compareTo(BigInteger.valueOf(max)) > 0) {
            throw new ConfigTypeErrorException(node, String.format("Value too large: %s; max value is %s", bigInt, max));
        } else if (bigInt.compareTo(BigInteger.valueOf(min)) < 0) {
            throw new ConfigTypeErrorException(node, String.format("Value too small: %s; max value is %s", bigInt, min));
        }
    }

    public static void validateInteger(Node node) {
        validate(integer, node, "Integer");
        validateIntSize(node, Integer.MAX_VALUE, Integer.MIN_VALUE);
    }
    
    public static void validateByte(Node node) {
        validate(integer, node, "Byte");
        validateIntSize(node, Byte.MAX_VALUE, Byte.MIN_VALUE);
    }

    public static void validateLong(Node node) {
        validate(integer, node, "Long");
        validateIntSize(node, Long.MAX_VALUE, Long.MIN_VALUE);
    }

    public static void validateBigInt(Node node) {
        validate(integer, node, "BigInteger");
    }
}
