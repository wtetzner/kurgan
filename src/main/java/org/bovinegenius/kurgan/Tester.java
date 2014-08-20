package org.bovinegenius.kurgan;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.bovinegenius.kurgan.types.FieldName;


public class Tester {
    public static interface Thingy<T> {
        T baz();
    }

    public static interface Config {
        String foo(String def);
        String bar();
        List<String> items();
        List<Thingy<BigDecimal>> getThingies();

        int number();

        @FieldName("thingy map")
        Map<String,Thingy<Boolean>> thingyMap();

        URI uri();
        URL url();
        File file();
        Path path();
    }

    public static void main(String[] args) throws IOException {
        Config config = ConfigLoader.loadYaml(Config.class, System.getProperty("user.home") + "/temp-crap/test.yaml");
        System.out.println(config);
        System.out.println(config.getThingies().get(0).baz().getClass());
    }
}
