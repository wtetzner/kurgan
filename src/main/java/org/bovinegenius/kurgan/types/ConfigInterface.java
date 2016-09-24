package org.bovinegenius.kurgan.types;

import static java.lang.String.format;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.NonNull;
import lombok.Value;

import org.bovinegenius.kurgan.ConfigException;
import org.bovinegenius.kurgan.ConfigTypeErrorException;
import org.bovinegenius.kurgan.KurganField;
import org.bovinegenius.kurgan.string.StringUtils;
import org.bovinegenius.kurgan.yaml.YamlUtils;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;


@Value
public class ConfigInterface implements ConfigType {
    private final ConfigString keyType = new ConfigString();

    private static String nameFromGetter(String methodName) {
        return String.valueOf(methodName.charAt(3)).toLowerCase() + methodName.substring(4);
    }

    private static String baseName(Method method) {
        String methodName = method.getName();
        if (methodName.length() > 3 && methodName.startsWith("get") && Character.isUpperCase(methodName.charAt(3))) {
            return nameFromGetter(methodName);
        } else {
            return methodName;
        }
    }

    public static String name(Method method) {
        KurganField fieldName = method.getAnnotation(KurganField.class);
        if (fieldName != null) {
            return fieldName.value();
        }
        return StringUtils.camelCaseToHyphens(baseName(method));
    }

    @Value
    public static class ConfigField {
        String keyName;
        Method method;
        ConfigType valueType;
    }
    Class<?> configInterface;
    Map<String,ConfigField> fields;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object coerce(Node node) {
        try {
            if (YamlUtils.isNull(node)) return null;
            MappingNode mNode = (MappingNode)node;
            Map<String,Object> values = new LinkedHashMap<>();
            for (NodeTuple field : mNode.getValue()) {
                String key = key(field);
                values.put(key, fields.get(key).getValueType().coerce(field.getValueNode()));
            }
            InvocationHandler handler = new ConfigHandler(this, values);
            Class proxyClass = Proxy.getProxyClass(
                    configInterface.getClassLoader(), new Class[] { configInterface });
            return proxyClass.
                    getConstructor(new Class[] { InvocationHandler.class }).
                    newInstance(new Object[] { handler });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigException(format("Failed to construct config object for class %s", configInterface.getCanonicalName()), e);
        }
    }

    private String key(NodeTuple field) {
        Node keyNode = field.getKeyNode();
        keyType.typeCheck(keyNode);
        String key = (String)keyType.coerce(keyNode);
        if (key == null) {
            throw new ConfigTypeErrorException(keyNode, format("Key cannot be null for type %s", configInterface.getCanonicalName()));
        }
        return key;
    }

    private boolean hasDefaultArg(ConfigField field) {
        Method method = field.getMethod();
        Class<?>[] args = method.getParameterTypes();
        return args.length == 1;
    }

    private void typeCheckField(NodeTuple field) {
        String key = key(field);
        ConfigField configField = fields.get(key);
        configField.getValueType().typeCheck(field.getValueNode());
        if (!hasDefaultArg(configField)) {
            if (YamlUtils.isNull(field.getValueNode())) {
                throw new ConfigTypeErrorException(field.getValueNode(), format("Field can't be null"));
            }
        }
    }

    private void typeCheckMethods(Node node, List<NodeTuple> tuples) {
        Map<String,Node> keys = new HashMap<>();
        for (NodeTuple tuple : tuples) {
            keys.put(key(tuple), YamlUtils.isNull(tuple.getValueNode()) ? null : tuple.getValueNode());
        }
        for (ConfigField field : fields.values()) {
            if (!hasDefaultArg(field) && keys.get(field.getKeyName()) == null) {
                throw new ConfigTypeErrorException(node, format("Field can't be null or missing: %s", field.getKeyName()));
            }
        }
    }

    @Override
    public void typeCheck(Node node) throws ConfigTypeErrorException {
        if (YamlUtils.isNull(node)) {
            return;
        }
        if (!(node instanceof MappingNode)) {
            throw new ConfigTypeErrorException(node, format("Expected %s, found %s",
                    configInterface.getCanonicalName(),
                    node.getNodeId()));
        }
        MappingNode mNode = (MappingNode)node;
        for (NodeTuple field : mNode.getValue()) {
            String key = key(field);
            if (!fields.containsKey(key)) {
                throw new ConfigTypeErrorException(field.getKeyNode(), format("Unknown field: %s", key));
            }
            typeCheckField(field);
        }
        typeCheckMethods(mNode, mNode.getValue());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(configInterface.getSimpleName());
        sb.append("(");
        boolean start = true;
        for (Map.Entry<String,ConfigField> fieldPair : fields.entrySet()) {
            ConfigField field = fieldPair.getValue();
            if (start) {
                start = false;
            } else {
                sb.append(", ");
            }
            sb.append(field.getKeyName());
            sb.append(": ");
            sb.append(field.getValueType());
        }
        sb.append(")");
        return sb.toString();
    }

    //@Value
    private static class ConfigHandler implements InvocationHandler {
        @NonNull ConfigInterface configInterface;
        @NonNull Map<String,Object> data;
        int hashCode;

        public ConfigHandler(@NonNull ConfigInterface configInterface, @NonNull Map<String,Object> data) {
            this.configInterface = configInterface;
            this.data = data;
            this.hashCode = computeHashCode(configInterface, data);
        }

        private static int computeHashCode(ConfigInterface configInterface, Map<String,Object> data) {
            return data.hashCode() ^ configInterface.getClass().hashCode();
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] argsArray)
                throws Throwable {
            Object[] args = argsArray == null ? new Object[] {} : argsArray;
            if (method.getDeclaringClass().equals(Object.class)) {
                return method.invoke(this, args);
            } else {
                String fieldName = name(method);
                Object value = data.get(fieldName);
                if (value != null) {
                    return value;
                } else if (args.length == 1) {
                    return args[0];
                } else {
                    return null;
                }
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            } else if (Proxy.isProxyClass(obj.getClass())
                    && Proxy.getInvocationHandler(obj).getClass().equals(this.getClass())) {
                InvocationHandler otherHandler = Proxy.getInvocationHandler(obj);
                ConfigHandler other = (ConfigHandler)otherHandler;
                return this.data.equals(other.data)
                        && this.configInterface.getConfigInterface().equals(other.configInterface.getConfigInterface());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return this.hashCode;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(configInterface.getConfigInterface().getSimpleName());
            sb.append("(");
            boolean start = true;
            for (Map.Entry<String,ConfigField> fieldPair : configInterface.getFields().entrySet()) {
                ConfigField field = fieldPair.getValue();
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append(field.getKeyName());
                sb.append("=");
                sb.append(StringUtils.print(data.get(field.getKeyName())));
            }
            sb.append(")");
            return sb.toString();
        }
    }
}
