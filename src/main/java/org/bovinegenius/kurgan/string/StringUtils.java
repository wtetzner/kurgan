package org.bovinegenius.kurgan.string;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class StringUtils {
    public static String join(String separator, Iterable<? extends Object> strings) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object obj : strings) {
            String str = obj == null ? null : obj.toString();
            if (first)
                first = false;
            else
                sb.append(separator);

            sb.append(str);
        }
        return sb.toString();
    }

    public static String join(String separator, String[] strings) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String str : strings) {
            if (first)
                first = false;
            else
                sb.append(separator);

            sb.append(str);
        }
        return sb.toString();
    }

    public static String join(String separator, Object[] objs) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object obj : objs) {
            if (first)
                first = false;
            else
                sb.append(separator);

            sb.append(obj.toString());
        }
        return sb.toString();
    }

    public static String spaces(int num) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < num; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String indent(String text, int indent) {
        List<String> lines = new ArrayList<>();
        for (String line : text.split("\n")) {
            lines.add(spaces(indent) + line);
        }
        return join("\n", lines);
    }
    
    public static String print(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) {
            return String.format("\"%s\"", obj);
        } else if (obj instanceof Collection) {
            Collection<?> items = (Collection<?>)obj;
            List<String> strs = new ArrayList<>();
            for (Object item : items) {
                strs.add(print(item));
            }
            return String.format("[%s]", join(", ", strs));
        } else if (obj instanceof Map) {
            Map<?,?> items = (Map<?,?>)obj;
            List<String> strs = new ArrayList<>();
            for (Map.Entry<?, ?> entry : items.entrySet()) {
                strs.add(String.format("%s=%s", print(entry.getKey()), print(entry.getValue())));
            }
            return String.format("{%s}", join(", ", strs));
        } else {
            return obj.toString();
        }
    }
}
