package org.bovinegenius.kurgan;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.bovinegenius.kurgan.types.ConfigType;
import org.bovinegenius.kurgan.yaml.YamlUtils;
import org.yaml.snakeyaml.composer.ComposerException;
import org.yaml.snakeyaml.nodes.Node;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

@EqualsAndHashCode
@ToString
public class ConfigLoader {
    private static final ConfigLoader defaultConfigLoader = new ConfigLoader();

    private ConfigLoader() {

    }

    public static ConfigLoader getDefault() {
        return ConfigLoader.defaultConfigLoader;
    }

    public <T> ReloadableConfig<T> getReloadable(Class<T> cls, String location) {
        return new ReloadableConfigImpl<>(this, cls, location);
    }

    @SuppressWarnings("unchecked")
    public <T> Iterable<T> loadAllYaml(Class<T> cls, String location) {
        try {
            ConfigType type = ConfigType.Conversions.asConfigType(cls);
            Iterable<Node> nodes = YamlUtils.readNodes(location, location);
            List<T> results = new ArrayList<>();
            for (Node node : nodes) {
                type.typeCheck(node);
                results.add((T)type.coerce(node));
            }
            return results;
        } catch (IOException e) {
            throw new ConfigException(format("Failed to load config from %s", location), e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Iterable<T> loadAllYaml(Class<T> cls, String location, InputStream input) {
        try {
            ConfigType type = ConfigType.Conversions.asConfigType(cls);
            Iterable<Node> nodes = YamlUtils.readNodes(location, input);
            List<T> results = new ArrayList<>();
            for (Node node : nodes) {
                type.typeCheck(node);
                results.add((T)type.coerce(node));
            }
            return results;
        } catch (IOException e) {
            throw new ConfigException(format("Failed to load config from %s", location), e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T loadYaml(Class<T> cls, String location) {
        try {
            ConfigType type = ConfigType.Conversions.asConfigType(cls);
            Node node = YamlUtils.readNode(location, location);
            type.typeCheck(node);
            return (T)type.coerce(node);
        } catch (IOException e) {
            throw new ConfigException(format("Failed to load config from %s", location), e);
        } catch (ComposerException e) {
            throw new ConfigException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T loadYaml(Class<T> cls, String location, InputStream input) {
        try {
            ConfigType type = ConfigType.Conversions.asConfigType(cls);
            Node node = YamlUtils.readNode(location, input);
            type.typeCheck(node);
            return (T)type.coerce(node);
        } catch (IOException e) {
            throw new ConfigException(format("Failed to load config from %s", location), e);
        } catch (ComposerException e) {
            throw new ConfigException(e.getMessage(), e);
        }
    }

    @EqualsAndHashCode
    private static class ReloadableConfigImpl<T> implements ReloadableConfig<T> {
        private final ConfigLoader configLoader;
        private final String location;
        private final Class<T> cls;

        private final AtomicReference<Optional<ConfigData<T>>> configData = new AtomicReference<>(Optional.<ConfigData<T>>empty());

        @Value(staticConstructor="of")
        private static class ConfigData<T> {
            @NonNull String location;
            @NonNull T data;
        }

        public ReloadableConfigImpl(@NonNull ConfigLoader configLoader, @NonNull Class<T> cls, @NonNull String location) {
            this.configLoader = configLoader;
            this.cls = cls;
            this.location = location;
        }

        private ConfigData<T> load(@NonNull String location) {
            return ConfigData.of(location, this.configLoader.loadYaml(this.cls, location));
        }

        @Override
        public void reload() {
            reload(this.location);
        }

        @Override
        public T get() {
            if (!this.configData.get().isPresent()) {
                this.reload();
            }
            return this.configData.get().get().getData();
        }

        @Override
        public String toString() {
            return String.format("ReloadableConfig(%s, %s, %s)", cls.getCanonicalName(), location, this.configData.get());
        }

        @Override
        public void reload(String location) {
            Optional<ConfigData<T>> data = Optional.of(load(location));
            this.configData.set(data);
        }
    }
}
