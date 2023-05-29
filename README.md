# Preface
This article provides a technical guide on setting up a project to develop a plugin for Graylog, a popular frontend to Elasticsearch that offers document streams, permissions, dashboards, and more.

Since Graylog is written in Java, it is natural that the plugin will be JVM-based. For this guide, we will use Scala. The ultimate goal is to have the message "Hello World!" appear in the Graylog logs during startup. This plugin will not have any user interface components.


# Plan
1. Initialize the SBT project.
1. Implement the plugin entry point.
1. Implement the HelloWorld service and add it to the plugin.
1. Build the plugin JAR.
1. Run Graylog with the new plugin.




## Initialize the SBT project
First, let's create a new project named `graylog-plugin-hello-world` using the Scala `hello-world` template:
```
sbt new scala/hello-world.g8
```

Add [dependency on the latest graylog](https://mvnrepository.com/artifact/org.graylog2/graylog2-server).
Next, add the [dependency on the latest version of Graylog ](https://mvnrepository.com/artifact/org.graylog2/graylog2-server) to the build.sbt file:



```scala
libraryDependencies += "org.graylog2" % "graylog2-server" % "5.1.1" % "provided"
```



## Implement the plugin entry point

Every Graylog plugin must have an entry point. This entry point should be a class that extends the `org.graylog2.plugin.Plugin` interface, which has two methods:

```java
    PluginMetaData metadata();

    Collection<PluginModule> modules();
```

The `metadata()` method should return an object with generic plugin information. The `modules()` method should return the [Guice](https://github.com/google/guice) module for the plugin, including all necessary initializations.


Let's start by implementing the `metadata()` method. Create a new class named `HelloWorldMeta` in the `com.graylog.plugin.helloworld` package:
```
package com.graylog.plugin.helloworld

import org.graylog2.plugin.{PluginMetaData, ServerStatus, Version}

import java.net.URI
import java.util
import java.util.Collections

class HelloWorldMeta extends PluginMetaData {

  // Set the plugin ID to the meta class name
  override def getUniqueId: String = "com.graylog.plugin.helloworld.HelloWorldMeta"

  override def getName: String = "Hello World"

  override def getAuthor: String = "Vladimir Penkov"

  override def getURL: URI = URI.create("https://google.com");

  override def getVersion: Version = Version.from(1, 0, 0, "unknown")

  override def getDescription: String = "Plugin example"

  // Specify the minimum required version
  override def getRequiredVersion: Version = Version.from(5, 0, 0, "unknown")

  override def getRequiredCapabilities: util.Set[ServerStatus.Capability] = Collections.emptySet()
}
```


Next, let's create the Guice module for the plugin. Create a new class named `HelloWorldModule` in the `com.graylog.plugin.helloworld` package:

```scala
package com.graylog.plugin.helloworld

class HelloWorldModule extends org.graylog2.plugin.PluginModule {

  override def configure(): Unit = {
    // Initialize your services here
  }

}

```


Finally, create the plugin entry point. Create a new class named `HelloWorldPlugin` in the `com.graylog.plugin.helloworld` package::
```scala
package com.graylog.plugin.helloworld

import org.graylog2.plugin.{Plugin, PluginMetaData, PluginModule}

import java.util

class HelloWorldPlugin extends Plugin {
  override def metadata(): PluginMetaData = new HelloWorldMeta

  override def modules(): util.Collection[PluginModule] = util.Collections.singleton(new HelloWorldModule)
}

```





Now we need to help graylog to find the plugin. Graylog uses [ServiceLoader](https://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html) for this.
Create a new file named `org.graylog2.plugin.Plugin` in the `src/main/resources/META-INF/services` directory and add the following content:
```
com.graylog.plugin.helloworld.HelloWorldPlugin
```


## Implement the HelloWorld service

Now let's implement the `HelloWorldService` that will print "Hello World!" to the Graylog logs during startup.


We will create a service and print the message in it's constructor:

```scala
package com.graylog.plugin.helloworld

import org.slf4j.{Logger, LoggerFactory}

class HelloWorldService {

  private val log: Logger = LoggerFactory.getLogger(getClass)
  
  log.info("Hello World!")

}
```

We print the message using slf4j, which comes as a dependency to `org.graylog2.graylog2-server`.

Now let's add this service to the guice module:


```scala
package com.graylog.plugin.helloworld

class HelloWorldModule extends org.graylog2.plugin.PluginModule {

  override def configure(): Unit = {
    bind(classOf[HelloWorldService]).asEagerSingleton()
  }

}
```




## Build the plugin JAR
To build the plugin JAR, run the following command in the project's root directory:


```bash
> sbt clean compile package
> ls -la target/scala-2.13/*.jar
-rw-r--r--  1 penkov  staff  5631 May 29 20:47 target/scala-2.13/hello-world_2.13-1.0.jar
```


The problem at this point is that the jar doesn't contain all scala dependencies. For this we need to create a fat
jar using [assembly plugin](https://github.com/sbt/sbt-assembly).
Add this plugin to `project/plugins.sbt` (you need to create this file):
```
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "1.2.0")
```

We need to configure it in `build.sbt`:
```
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs@_*) =>
    xs.map(_.toLowerCase) match {
      case ("manifest.mf" :: Nil) | ("notice.txt" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
        MergeStrategy.discard
      case ("license.txt" :: Nil) =>
        MergeStrategy.discard
      case ps@(x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
        MergeStrategy.discard
      case "maven" :: xs =>
        MergeStrategy.discard
      case "pom.properties" :: xs =>
        MergeStrategy.discard
      case "pom.xml" :: Nil =>
        MergeStrategy.discard
      case "plexus" :: Nil =>
        MergeStrategy.discard
      case "io.netty.versions.properties" :: xs =>
        MergeStrategy.first
      case ("license" :: Nil) | ("notice" :: Nil) =>
        MergeStrategy.first
      case "services" :: xs =>
        MergeStrategy.filterDistinctLines
      case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) | ("spring.tooling" :: Nil) =>
        MergeStrategy.filterDistinctLines
      case _ => MergeStrategy.deduplicate
    }
  case _ => MergeStrategy.first
}

assembly / assemblyJarName := s"${name.value}-${version.value}.jar"
```



Let's check the jar. Please notice, we use `assembly` to build a jar file:
```bash
> sbt clean compile assembly
> ls -la target/scala-2.13/*.jar                                            
-rw-r--r--  1 penkov  staff  5993596 May 29 20:52 target/scala-2.13/hello-world-1.0.jar
```
The size has increased, so everything is correct.



We are almost done. Now we need to actually run the graylog and check it's logs. We will use docker compose for this.

## Run Graylog with the new plugin

Let's use this docker compose config:
```yaml
version: '2'
services:
  # MongoDB: https://hub.docker.com/_/mongo/
  mongodb: # Mongodb service
    image: mongo:5 # Version of Mongodb docker image
    volumes:
      - mongo_data:/data/db # Persisted mongodb data

  # https://opensearch.org/docs/latest/install-and-configure/install-opensearch/docker/
  opensearch:
    image: opensearchproject/opensearch:latest
    volumes:
      - os_data:/usr/share/opensearch/data # Persisted elasticsearch data
    environment:
      - bootstrap.memory_lock=true # Disable JVM heap memory swapping
      - discovery.type=single-node
      - plugins.security.disabled=true
      - "OPENSEARCH_JAVA_OPTS=-Xms512m -Xmx512m" # Set min and max JVM heap sizes to at least 50% of system RAM
    ulimits:
      memlock:
        soft: -1 # Set memlock to unlimited (no soft or hard limit)
        hard: -1
      nofile:
        soft: 65536 # Maximum number of open files for the opensearch user - set to at least 65536
        hard: 65536
    ports:
      - 9200:9200 # REST API
      - 9600:9600 # Performance Analyzer


  # Graylog: https://hub.docker.com/r/graylog/graylog/
  graylog:
    image: graylog/graylog:5.1.0 # Version of Graylog docker image
    volumes:
      - graylog_data:/usr/share/graylog/data # Persisted Graylog data
      - ./graylog/plugin/:/usr/share/graylog/plugin
    environment:
      # CHANGE ME (must be at least 16 characters)!
      - GRAYLOG_PASSWORD_SECRET=asdasd12dasasasads332dwqdasdasdqq3
      # Password: admin
      - GRAYLOG_ROOT_PASSWORD_SHA2=65e84be33532fb784c48129675f9eff3a682b27168c0ea744b2cf58ee02337c5
      - GRAYLOG_HTTP_EXTERNAL_URI=http://127.0.0.1:9000/
      - GRAYLOG_WEB_ENDPOINT_URI=http://127.0.0.1:9000/api
      - GRAYLOG_ELASTICSEARCH_HOSTS=http://opensearch:9200
    # Command to run as soon as components are started
    entrypoint: /usr/bin/tini -- wait-for-it opensearch:9200 --  /docker-entrypoint.sh
    # Containers that Graylog depends on
    links:
      - mongodb:mongo
      - opensearch
    restart: always # Graylog container set to always restart when stopped
    depends_on:
      - mongodb
      - opensearch
    ports:
      # Graylog web interface and REST API
      - "9000:9000"
      # Syslog TCP
      - "1514:1514"
      # Syslog UDP
      - "1514:1514/udp"
      # GELF TCP
      - "12201:12201"
      # GELF UDP
      - "12201:12201/udp"
# Volumes for persisting data, see https://docs.docker.com/engine/admin/volumes/volumes/
volumes:
  mongo_data:
    driver: local
  os_data:
    driver: local
  graylog_data:
    driver: local

```

`GRAYLOG_ROOT_PASSWORD_SHA2` is the hash of the password `qwerty` for user `admin`, which we will use to login into graylog.

Make sure you use the correct version of the graylog in `image: graylog/graylog:5.1.0`.
We mount the `graylog/plugin` folder with our plugin as `/usr/share/graylog/plugin`. Make sure this folder exists and contains the plugin jar before running graylog:
```shell
mkdir -p graylog/plugin
```

Now let's build our project, copy the output file to the plugins folder and start graylog:
```bash
sbt clean compile assembly
cp target/scala-2.13/*.jar graylog/plugin 
docker-compose up

```

You should see in logs:
```
INFO : com.graylog.plugin.helloworld.HelloWorldService - Hello World!
```


Congratulations! You have successfully created a basic Graylog plugin that prints "Hello World!" during startup. This serves as a starting point for building more advanced plugins with additional functionality.

