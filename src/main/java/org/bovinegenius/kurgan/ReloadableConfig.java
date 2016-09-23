package org.bovinegenius.kurgan;

public interface ReloadableConfig<T> {
    public void reload();
    public void reload(String location);
    public T get();
}
