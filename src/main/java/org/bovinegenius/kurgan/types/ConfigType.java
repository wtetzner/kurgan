package org.bovinegenius.kurgan.types;

import static java.lang.String.format;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bovinegenius.kurgan.ConfigException;
import org.bovinegenius.kurgan.ConfigTypeErrorException;
import org.bovinegenius.kurgan.types.ConfigInterface.ConfigField;
import org.yaml.snakeyaml.nodes.Node;


public interface ConfigType {
    public Object coerce(Node node);
    public void typeCheck(Node node) throws ConfigTypeErrorException;

    public static class Conversions {
        public static ConfigType asConfigType(Type type) {
            return toConfigType(type, new Types());
        }

        public static ConfigType toConfigType(Type inType, Types types) {
            Type type = resolve(inType, types);
            Class<?> cls = classFromType(type);
            if (type instanceof ParameterizedType) {
                if (cls.equals(List.class)) {
                    ParameterizedType ptype = (ParameterizedType)type;
                    return new ConfigList(toConfigType(resolve(ptype.getActualTypeArguments()[0], types), types));
                } else if (cls.equals(Map.class)) {
                    ParameterizedType ptype = (ParameterizedType)type;
                    return new ConfigMap(
                            toConfigType(resolve(ptype.getActualTypeArguments()[0], types), types),
                            toConfigType(resolve(ptype.getActualTypeArguments()[1], types), types));
                } else {
                    return fromClass(cls, types);
                }
            } else if (type instanceof Class) {
                return fromClass(cls, types);
            } else {
                throw new ConfigException(format("Can't map %s to a config type", type));
            }
        }

        public static ConfigType fromClass(Class<?> cls, Types types) {
            if (cls.equals(String.class)) {
                return ConfigString.value;
            } else if (cls.equals(Integer.class) || cls.equals(Integer.TYPE)) {
                return ConfigInt.value;
            } else if (cls.equals(Byte.class) || cls.equals(Byte.TYPE)) {
                return ConfigByte.value;
            } else if (cls.equals(Long.class) || cls.equals(Long.TYPE)) {
                return ConfigLong.value;
            } else if (cls.equals(Boolean.class) || cls.equals(Boolean.TYPE)) {
                return ConfigBool.value;
            } else if (cls.equals(Character.class) || cls.equals(Character.TYPE)) {
                return ConfigChar.value;
            } else if (cls.equals(BigInteger.class)) {
                return ConfigBigInt.value;
            } else if (cls.equals(Float.class) || cls.equals(Float.TYPE)) {
                return ConfigFloat.value;
            } else if (cls.equals(Double.class) || cls.equals(Double.TYPE)) {
                return ConfigDouble.value;
            } else if (cls.equals(BigDecimal.class)) {
                return ConfigBigDec.value;
            } else if (cls.equals(URI.class)) {
                return ConfigUri.value;
            } else if (cls.equals(URL.class)) {
                return ConfigUrl.value;
            } else if (cls.equals(File.class)) {
                return ConfigFile.value;
            } else if (cls.equals(Path.class)) {
                return ConfigPath.value;
            } else if (cls.isEnum()) {
                return new ConfigEnum(cls);
            } else if (cls.isInterface()) {
                return interfaceToConfigType(cls, types);
            } else {
                throw new ConfigException(format("Can't convert %s to config type", cls.getCanonicalName()));
            }
        }

        public static ConfigType interfaceToConfigType(Type inType, Types types) {
            try {
                Type type = resolve(inType, types);
                Class<?> cls = classFromType(type);
                if (!cls.isInterface()) {
                    throw new ConfigException(format("Can only load configuration into an interface; found %s", cls.getCanonicalName()));
                }
                Method[] methods = cls.getDeclaredMethods();
                Map<String,ConfigField> fields = new LinkedHashMap<>();
                for (Method method : methods) {
                    Type[] params = method.getGenericParameterTypes();
                    Type returnType = method.getGenericReturnType();
                    if (params.length > 1) {
                        throw new ConfigException(format("Config interface can't contain a method with more than one argument; found %s", method));
                    }
                    if (params.length == 1 && !params[0].equals(method.getGenericReturnType())) {
                        throw new ConfigException(format("Config interface can't contain a method of one arg where the type doesn't match the return type: %s", method));
                    }
                    ConfigType fieldType = toConfigType(returnType, typeMapping(returnType, types));
                    String fieldName = ConfigInterface.name(method);
                    fields.put(fieldName, new ConfigField(fieldName, method, fieldType));
                }
                return new ConfigInterface(cls, fields);
            } catch (UnboundTypeParamException e) {
                throw new ConfigException(format("Can't have unresolved type parameters in a config type: %s", inType), e);
            }
        }

        public static Class<?> classFromType(Type type) {
            if (type instanceof ParameterizedType) {
                ParameterizedType ptype = (ParameterizedType)type;
                return (Class<?>)ptype.getRawType();
            } else if (type instanceof Class) {
                return (Class<?>)type;
            } else {
                throw new ConfigException(format("Can't map %s to a config type", type));
            }
        }

        public static Type resolve(Type type, Types types) {
            if (type instanceof TypeVariable) {
                return types.get(((TypeVariable<?>) type).getName());
            } else {
                return type;
            }
        }

        @SuppressWarnings("unchecked")
        public static List<Type> typeArgs(Type type, Types types) {
            if (type instanceof ParameterizedType) {
                ParameterizedType ptype = (ParameterizedType)type;
                Type[] args = ptype.getActualTypeArguments();
                List<Type> results = new ArrayList<>();
                for (Type arg : args) {
                    results.add(resolve(arg, types));
                }
                return results;
            } else {
                return (List<Type>)Collections.EMPTY_LIST;
            }
        }

        public static List<String> typeParameters(Type type, Types types) {
            Class<?> cls = classFromType(resolve(type, types));
            TypeVariable<?>[] params = cls.getTypeParameters();
            List<String> results = new ArrayList<>();
            for (TypeVariable<?> param : params) {
                results.add(param.getName());
            }
            return results;
        }

        public static Types typeMapping(Type type, Types types) {
            if (type instanceof TypeVariable) return types;
            List<String> params = typeParameters(type, types);
            List<Type> args = typeArgs(type, types);
            if (params.size() != args.size()) {
                throw new ConfigException(format("Size of type params doesn't match size of type args for type %s; params: %s, args: %s", type, params, args));
            }
            if (args.isEmpty()) return types;
            Types result = types;
            for (Type arg : args) {
                result = typeMapping(arg, result);
            }
            Map<String,Type> mapping = new HashMap<>();
            for (int i = 0; i < params.size(); i++) {
                mapping.put(params.get(i), args.get(i));
            }
            return result.with(mapping);
        }
    }
}
