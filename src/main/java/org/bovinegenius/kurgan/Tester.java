package org.bovinegenius.kurgan;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import lombok.Getter;


public class Tester {
    @KurganEnum(valueOf="of")
    public static enum Format {
        JSON("json"),
        XML("xml"),
        HTML("html");

        @Getter private final String text;

        private Format(String text) {
            this.text = text;
        }

        public static Format of(String text) {
            for (Format format : Format.values()) {
                if (format.getText().equalsIgnoreCase(text)) {
                    return format;
                }
            }
            throw new RuntimeException(String.format("Unknown %s: %s", Format.class.getSimpleName(), text));
        }
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

        @KurganField("str-coll")
        Collection<String> strColl();

        boolean readOnly();

        @KurganField("max-file-size")
        long maxFileSize();
        List<HttpResource> getResources();
    }

    public static void main(String[] args) throws IOException {
        Config config = ConfigLoader.getDefault().loadYaml(Config.class, System.getProperty("user.home") + "/temp/test.yaml");
        System.out.println(String.format("config: %s", config));
        System.out.println(String.format("config.value(): %s", config.value()));
        Config config2 = ConfigLoader.getDefault().loadYaml(Config.class, System.getProperty("user.home") + "/temp/test.yaml");
        Config config3 = ConfigLoader.getDefault().loadYaml(Config.class, System.getProperty("user.home") + "/temp/test2.yaml");
        System.out.println(String.format("config.equals(config): %s", config.equals(config)));
        System.out.println(String.format("config.equals(config2): %s", config.equals(config2)));
        System.out.println(String.format("config.equals(config3): %s", config.equals(config3)));
        System.out.println(String.format("config.hashCode(): %s", config.hashCode()));
        System.out.println(String.format("config2.hashCode(): %s", config2.hashCode()));
        System.out.println(String.format("config3.hashCode(): %s", config3.hashCode()));
    }
}
