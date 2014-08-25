package org.bovinegenius.kurgan;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;


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
        @FieldName("disk-resource")
        Resource<File> getDiskResource();
        
        @FieldName("rest-resource")
        Resource<URI> getRestResource();
    }
    
    public static interface Config {
        @FieldName("read-only")
        boolean readOnly();
        
        @FieldName("max-file-size")
        long maxFileSize();
        List<HttpResource> getResources();
    }

    public static void main(String[] args) throws IOException {
        //Config config = ConfigLoader.loadYaml(Config.class, System.getProperty("user.home") + "/temp/test.yaml");
        //System.out.println(config);
        Iterable<Config> configs = ConfigLoader.loadAllYaml(Config.class, System.getProperty("user.home") + "/temp/test.yaml");
        System.out.println(configs);
    }
}
