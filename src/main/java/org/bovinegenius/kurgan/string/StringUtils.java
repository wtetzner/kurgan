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

    public static String[] splitCamelCase(String s) {
        return s.split(String.format("%s|%s|%s",
                "(?<=[A-Z])(?=[A-Z][a-z])",
                "(?<=[^A-Z])(?=[A-Z])",
                "(?<=[A-Za-z])(?=[^A-Za-z])"
                ));
    }

    public static String camelCaseToHyphens(String s) {
        String[] pieces = splitCamelCase(s);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String piece : pieces) {
            if (first) {
                first = false;
            } else {
                sb.append("-");
            }
            sb.append(piece.toUpperCase().toLowerCase());
        }
        return sb.toString();
    }

    public static String print(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) {
            return String.format("\"%s\"",
                    ((String) obj)
                    .replace("\\", "\\\\")
                    .replace("\n", "\\n")
                    .replace("\t", "\\t")
                    .replace("\b", "\\b")
                    .replace("\r", "\\r")
                    .replace("\f", "\\f")
                    .replace("\"", "\\\""));
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
