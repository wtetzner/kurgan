[![Build Status](https://travis-ci.org/wtetzner/kurgan.png?branch=master)](https://travis-ci.org/wtetzner/kurgan)

Kurgan
======

Kurgan is a Java library for loading YAML configuration files. It is
designed to be as easy as possible to use, while helping to ensure
correctness.

Usage
=====

To use with maven, add

```XML
<dependency>
  <groupId>org.bovinegenius</groupId>
  <artifactId>kurgan</artifactId>
  <version>0.2.0</version>
</dependency>
```

to your pom.xml.

Example
=======

This is an example of a config for a server that serves files from the filesystem over HTTP, but it knows how to convert between various formats.

To load a YAML file, you first need to define one or more interfaces to represent your configuration.

You can then load the config by using

```java
ConfigLoader.loadYaml(ConfigInterface.class, "/path/to/config.yaml")
```

to load a file, or

```java
ConfigLoader.loadYaml(ConfigInterface.class, "classpath:/path/to/config.yaml")
```

to load from the classpath.

You can also use `ConfigLoader.loadAllYaml` to load a list of objects out of a YAML file that has multiple documents in it.

```java
package example;

import org.bovinegenius.kurgan.ConfigLoader;
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
        Config config = ConfigLoader.loadYaml(Config.class, System.getProperty("user.home") + "/temp/test.yaml");
        System.out.println(config);
    }
}
```

Here's an example of what a valid config file might look like.

    
```yaml
read-only: true
max-file-size: 1000000

resources:
  - disk-resource:
      location: /www/files/numbers.json
      format: JSON
    rest-resource:
      location: http://example.com/numbers.xml
      format: XML
  - disk-resource:
      location: /www/files/users.xml
      format: XML
    rest-resource:
      location: http://example.com/users.html
      format: HTML
```

