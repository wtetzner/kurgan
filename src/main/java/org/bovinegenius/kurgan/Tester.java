package org.bovinegenius.kurgan;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;


public class Tester {
    public static enum Format {
        JSON,
        XML,
        HTML
    }

    public static interface Resource<T> {
        T location();
        Format format();
    }

    public static interface HttpResource {
        Resource<File> getDiskResource();
        Resource<URI> getRestResource();
    }

    public static interface Config {
        String value();

        Set<String> strs();

        @FieldName("str-coll")
        Collection<String> strColl();

        boolean readOnly();

        @FieldName("max-file-size")
        long maxFileSize();
        List<HttpResource> getResources();
    }

    public static void main(String[] args) throws IOException {
        Config config = ConfigLoader.loadYaml(Config.class, System.getProperty("user.home") + "/temp/test.yaml");
        System.out.println(String.format("config: %s", config));
        System.out.println(String.format("config.value(): %s", config.value()));
        Config config2 = ConfigLoader.loadYaml(Config.class, System.getProperty("user.home") + "/temp/test.yaml");
        Config config3 = ConfigLoader.loadYaml(Config.class, System.getProperty("user.home") + "/temp/test2.yaml");
        System.out.println(String.format("config.equals(config): %s", config.equals(config)));
        System.out.println(String.format("config.equals(config2): %s", config.equals(config2)));
        System.out.println(String.format("config.equals(config3): %s", config.equals(config3)));
        System.out.println(String.format("config.hashCode(): %s", config.hashCode()));
        System.out.println(String.format("config2.hashCode(): %s", config2.hashCode()));
        System.out.println(String.format("config3.hashCode(): %s", config3.hashCode()));
    }
}
