package org.bovinegenius.kurgan.types;

import static java.lang.String.format;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bovinegenius.kurgan.ConfigTypeErrorException;
import org.bovinegenius.kurgan.KurganEnum;
import org.bovinegenius.kurgan.string.StringUtils;
import org.bovinegenius.kurgan.yaml.YamlUtils;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;

import lombok.Value;

@Value
public class ConfigEnum implements ConfigType {
    Class<?> cls;

    @SuppressWarnings("rawtypes")
    private Enum valueOfForAnnotation(Node node, String name) {
        KurganEnum enumName = cls.getAnnotation(KurganEnum.class);
        if (enumName == null) {
            return null;
        } else {
            String methodName = enumName.valueOf();
            try {
                Method method = cls.getDeclaredMethod(methodName, new Class<?>[] { String.class });
                return (Enum)method.invoke(null, name);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new ConfigTypeErrorException(node, errorMessage(name), e);
            }
        }
    }

    private String errorMessage(String name) {
        return format("Failed to map %s to enum %s; allowed values are %s",
                name,
                cls.getSimpleName(),
                StringUtils.join(", ", enumNamesList()));
    }

    @SuppressWarnings("rawtypes")
    private Enum valueOf(Node node, String name) { 
        try {
            Method method = cls.getDeclaredMethod("valueOf", new Class<?>[] { String.class });
            return (Enum)method.invoke(null, name);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new ConfigTypeErrorException(node, errorMessage(name), e);
        }
    }

    @SuppressWarnings("rawtypes")
    private List<String> enumNamesList() {
        List<String> results = new ArrayList<>();
        for (Enum enumVal : (Enum[])cls.getEnumConstants()) {
            results.add(enumVal.name());
        }
        return Collections.unmodifiableList(results);
    }

    @SuppressWarnings("rawtypes")
    private Object toEnum(Node node) {
        if (!(node instanceof ScalarNode)) {
            throw new ConfigTypeErrorException(node, format("Expected %s, found %s", this.toString(), node.getNodeId()));
        }
        ScalarNode sNode = (ScalarNode)node;
        String configName = YamlUtils.getValue(sNode);
        Enum enumValue = valueOfForAnnotation(sNode, configName);
        if (enumValue != null) {
            return enumValue;
        }
        Enum value = valueOf(node, configName);
        if (value == null) {
            throw new ConfigTypeErrorException(sNode, errorMessage(configName));
        }
        return value;
    }

    @Override
    public Object coerce(Node node) {
        return toEnum(node);
    }

    @Override
    public void typeCheck(Node node) throws ConfigTypeErrorException {
        toEnum(node);
    }

    @Override
    public String toString() {
        return cls.getSimpleName();
    }
}
