package org.bovinegenius.kurgan.types;

import static java.lang.String.format;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * An environment to track type variables
 */
public class Types {
    private final Types parent;
    private final Map<String,Type> map;
    
    public Types() {
        this.parent = null;
        this.map = new HashMap<>();
    }
    
    public Types(Types parent, Map<String,Type> entries) {
        this.parent = parent;
        this.map = entries;
    }
    
    public boolean contains(String key) {
        if (this.map.containsKey(key)) {
            return true;
        } else if (parent != null) {
            return parent.contains(key);
        } else {
            return false;
        }
    }
    
    public Type get(String name) {
        if (map.containsKey(name)) {
            return map.get(name);
        } else if (parent != null) {
            return parent.get(name);
        } else {
            throw new UnboundTypeParamException(format("No such type variable: %s", name));
        }
    }
    
    public Types with(String name, Type type) {
        Map<String,Type> map = new HashMap<>();
        map.put(name, type);
        return new Types(this, map);
    }

    public Types with(Map<String,Type> entries) {
        if (entries.isEmpty()) {
            return this;
        } else {
            return new Types(this, entries);
        }
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", map, parent);
    }
}
