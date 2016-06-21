# XmlToRdf


Java library to convert any XML file to RDF.


## Benchmark results

| Method | File size | Time |
|--------|---|---|
|Fast convert | 100 MB | 1.836 seconds |
|Advanced convert | 100 MB |  2.711 seconds |
|Jena convert | 100 MB |  9.744 seconds |
|Sesame convert | 100 MB |  9.811 seconds |


Benchmark information
 - *JDK*: JDK 1.8.0_65, VM 25.65-b01
 - *Machine*: Macbook Pro 15" Mid 2015
 - *CPU*:  2.8 GHz (i7-4980HQ) with 6 MB on-chip L3 cache and 128 MB L4 cache (Crystalwell)
 - *RAM*: 16 GB
 - *SSD*: 512 GB


## Installing
To install you can either just use `mvn install` to install the artifact in your local repo and add a dependecy in your project:

```
<dependency>
    <groupId>no.acando</groupId>
    <artifactId>xmltordf</artifactId>
    <version>1.0.1</version>
</dependency>
```

It is also possible to install the jar file in a specified local repo, for instance inside a directory in your own project.
Two steps are required for this. First you need to install the jar file in your required directory:

```
 mvn \
    install:install-file \
    -Dfile=target/xmltordf-1.1.0.jar \
    -DpomFile=pom.xml \
    -DlocalRepositoryPath=/INSTALL_DIRECTORY

```

And then you need to use that directory as a local repository in your project:

```
<repositories>
    <repository>
        <id>local-repo</id>
        <url>file://${basedir}/libs</url>
    </repository>
</repositories>
```

# Example

```java

import no.acando.xmltordf.Builder;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

public class Convert {

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream("data.xml"));
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("data.ttl"));

        Builder.getAdvancedBuilderStream().build().convertToStream(in, out);

    }

}

```


With the following XML file:

```xml
<data xmlns="http://example.org">
    <item>
        <name>Hello</name>
    </item>
    <item>
        <name>Hello</name>
    </item>
</data>
```


You should get the equivalent (though not as pretty) output as follows:

```turtle
@prefix :      <http://example.org#> .
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .

[ a                  :data ;
  xmlToRdf:hasChild  [ a       :item ;
                       :name   "Hello"
                     ] ;
  xmlToRdf:hasChild  [ a       :item ;
                       :name   "Hello"
                     ]
] .

```

If you want to keep working with the RDF data you can choose between Jena or Sesame.
Both of these methods are somewhat slower than using direct to stream, however they are faster
 than first outputting to stream and then parsing back in again.

 For Jena you can do as follows:

 ```java
BufferedInputStream in = new BufferedInputStream(new FileInputStream("data.xml"));
Dataset dataset = Builder.getAdvancedBuilderJena().build().convertToDataset(in);
 ```

 And for Sesame you can do like this:

 ```java
BufferedInputStream in = new BufferedInputStream(new FileInputStream("data.xml"));
Repository repository = Builder.getAdvancedBuilderSesame().build().convertToRepository(in);
 ```


# Java docs

{{{javadocs}}}


# Generate documentation
Run `sh generateDocs.sh` in the terminal.