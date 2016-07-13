# XmlToRdf


Java library to convert any XML file to RDF.


## Benchmark results

| Method | File size | Time |
|--------|---|---|
|Fast convert | 100 MB | ~ 1.8 seconds |
|Advanced convert | 100 MB |  ~ 3.1 seconds |
|Jena convert | 100 MB |  ~ 11 seconds |
|Sesame convert | 100 MB |  ~ 10 seconds |


### Memory usage

| Method | File size | Memory requirement |
|--------|---|---|
|Fast convert | 100 MB | Min: 3 MB; Comfort: 20 MB |
|Advanced convert | 100 MB |  Min: 15 MB; Comfort 50MB |
|Jena convert | 100 MB |  Min: 1600 MB; Comfort  |
|Sesame convert | 100 MB | Not measured yet |

Min: Minimum required memory

Comfort: Amount of memory required to get close to benchmark speeds

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
    <version>{{{pomVersion}}}</version>
</dependency>
```

It is also possible to install the jar file in a specified local repo, for instance inside a directory in your own project.
Two steps are required for this. First you need to install the jar file in your required directory:

```
 mvn \
    install:install-file \
    -Dfile=xmltordf/target/xmltordf-{{{pomVersion}}}.jar \
    -DpomFile=xmltordf/pom.xml \
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
 
## Mixed content
 
 XML allows for mixed content where an element can contain both text and other elements. XmlToRdf detects and converts mixed content into a RDF list structure.
 
 ```xml
 <document xmlns="http://example.org/">
     <paragraph>Hello <b>World</b>!</paragraph>
 </document>
 ```
 
 Will give the following turtle output by default:
 
 ```turtle
 [ a                   :document ;
   xmlTodRdf:hasChild  [ a                          :paragraph ;
                         xmlTodRdf:hasChild         _:b0 ;
                         xmlTodRdf:hasMixedContent  ( "Hello " _:b0 "!" ) ;
                         xmlTodRdf:hasValue         "Hello !"
                       ]
 ] .
 
 _:b0    a                   :b ;
         xmlTodRdf:hasValue  "World" .
 ```

Sometimes an element will contain mixed content in some XML documents, but not in others. In this case it is possible to force an element to always be 
evaluated as mixed content by adding `.forceMixedContent("http://example.org/paragraph")` with the appropriate element name to the builder.

# Java docs

{{{javadocs}}}


# Generate documentation
Run `sh generateDocs.sh` in the terminal.
