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
|Sesame convert | 100 MB |  Not measured yet |

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
    <version>1.4.11</version>
</dependency>
```

It is also possible to install the jar file in a specified local repo, for instance inside a directory in your own project.
Two steps are required for this. First you need to install the jar file in your required directory:

```
 mvn \
    install:install-file \
    -Dfile=xmltordf/target/xmltordf-1.4.11.jar \
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

## overrideNamespace(String namespace)

Override all namespaces in the XML with a new namespace.

**XML example**
```xml
<people xmlns="http://example.org/" xmlns:a="http://A.com/">
  <name a:test="hello">John Doe</name>
</people>
```

### Override all namespaces
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .overrideNamespace("http://otherNamespace.com/")
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  <http://otherNamespace.com/people> ;
  xmlToRdf:hasChild  [ a                  <http://otherNamespace.com/name> ;
                       xmlToRdf:hasValue  "John Doe" ;
                       <http://otherNamespace.com/test>
                               "hello"
                     ]
] .

```

---
### Use namespaces provided in XML
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:people ;
  xmlToRdf:hasChild  [ a                    ex:name ;
                       <http://A.com/test>  "hello" ;
                       xmlToRdf:hasValue    "John Doe"
                     ]
] .

```

---
## renameElement(String elementFrom, String to)

Change the name of an element.

**XML example**
```xml
<people xmlns="http://example.org/">
  <name>John Doe</name>
</people>
```

### Rename "people" to "PEOPLE"
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .renameElement("http://example.org/people", "http://example.org/PEOPLE")
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a        ex:PEOPLE ;
  ex:name  "John Doe"
] .

```

---
## renameElement(Builder.XmlPath path, String to)

Change the name of an element at the end of a specific path. Useful for renaming elements that .

**XML example**
```xml
<window xmlns="http://example.org/">
  <frame>
    <tittle>Main frame</tittle>
  </frame>
  <frame>
    <frame>
      <tittle>Sub frame</tittle>
    </frame>
  </frame>
</window>
```

### 
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .renameElement(Builder.createPath("http://example.org/frame","http://example.org/frame"), "http://example.org/subFrame")
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:window ;
  xmlToRdf:hasChild  [ a                  ex:frame ;
                       xmlToRdf:hasChild  [ a          ex:subFrame ;
                                            ex:tittle  "Sub frame"
                                          ]
                     ] ;
  xmlToRdf:hasChild  [ a          ex:frame ;
                       ex:tittle  "Main frame"
                     ]
] .

```

---
## renameElement(String elementFrom, StringTransformTwoValue transform)

Change the name on the fly using a function. Eg. for capitalizing element names.

**XML example**
```xml
<people xmlns="http://example.org/">
  <name>John Doe</name>
</people>
```

### Capitalize all element names
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .renameElement(null, (namespace, name) ->  namespace + name.substring(0, 1).toUpperCase() + name.substring(1))
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a        ex:People ;
  ex:Name  "John Doe"
] .

```

---
## simpleTypePolicy(SimpleTypePolicy policy)

XML elements with only text inside and no attributes (known as Simple Type elements)
 can be compacted to use the element name as the RDF predicate or be expanded to use the xmlToRdf:hasChild
 predicate

**XML example**
```xml
<people xmlns="http://example.org/">
  <name>John Doe</name>
</people>
```

### Compact
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .simpleTypePolicy(SimpleTypePolicy.compact)
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a        ex:people ;
  ex:name  "John Doe"
] .

```

---
### Expand
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .simpleTypePolicy(SimpleTypePolicy.expand)
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:people ;
  xmlToRdf:hasChild  [ a                  ex:name ;
                       xmlToRdf:hasValue  "John Doe"
                     ]
] .

```

---
## addTransformationForAttributeValue(String elementName, String attributeName, StringTransform transform)

Run a function on the value of an attribute and use the returned string as the new value.
 Take careful note of the namespaces. Unless specified, attributes inherit the namespace of their element.

**XML example**
```xml
<person age="3" xmlns="http://example.org/"/>
```

### Multiply age by 10
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .addTransformationForAttributeValue("http://example.org/person", "http://example.org/age", v -> String.valueOf(Integer.parseInt(v)*10))
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a       ex:person ;
  ex:age  "30"
] .

```

---
### Without any transformation
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a       ex:person ;
  ex:age  "3"
] .

```

---
## addIndex(boolean enabled)

Add the index of the element as a predicate to the RDF. `xmlToRdf:index` is a
 global element counter (depth-first) that keeps track of which absolute element this is. `xmlToRdf:elementIndex` is a
 relative counter that keeps track of which index this element is for the given type relative to other elements
 of that type in within the same parent.

**XML example**
```xml
<people xmlns="http://example.org/">
  <person>
    <name>person-zero : element-one</name>
  </person>
  <person>
    <name>person-one : element-three</name>
  </person>
  <ZEBRA>
    <name>ZEBRA-zero  : element-five</name>
  </ZEBRA>
  <person>
    <name>person-two  : element-seven</name>
  </person>
</people>
```

### Add index
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .addIndex(true)
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                      ex:people ;
  xmlToRdf:elementIndex  "0"^^xsd:long ;
  xmlToRdf:hasChild      [ a                      ex:person ;
                           xmlToRdf:elementIndex  "2"^^xsd:long ;
                           xmlToRdf:index         "7"^^xsd:long ;
                           ex:name                "person-two  : element-seven"
                         ] ;
  xmlToRdf:hasChild      [ a                      ex:ZEBRA ;
                           xmlToRdf:elementIndex  "0"^^xsd:long ;
                           xmlToRdf:index         "5"^^xsd:long ;
                           ex:name                "ZEBRA-zero  : element-five"
                         ] ;
  xmlToRdf:hasChild      [ a                      ex:person ;
                           xmlToRdf:elementIndex  "1"^^xsd:long ;
                           xmlToRdf:index         "3"^^xsd:long ;
                           ex:name                "person-one : element-three"
                         ] ;
  xmlToRdf:hasChild      [ a                      ex:person ;
                           xmlToRdf:elementIndex  "0"^^xsd:long ;
                           xmlToRdf:index         "1"^^xsd:long ;
                           ex:name                "person-zero : element-one"
                         ] ;
  xmlToRdf:index         "0"^^xsd:long
] .

```

---
### No index
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .addIndex(false)
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:people ;
  xmlToRdf:hasChild  [ a        ex:person ;
                       ex:name  "person-two  : element-seven"
                     ] ;
  xmlToRdf:hasChild  [ a        ex:ZEBRA ;
                       ex:name  "ZEBRA-zero  : element-five"
                     ] ;
  xmlToRdf:hasChild  [ a        ex:person ;
                       ex:name  "person-one : element-three"
                     ] ;
  xmlToRdf:hasChild  [ a        ex:person ;
                       ex:name  "person-zero : element-one"
                     ]
] .

```

---
## useAttributeForId(String elementName, String attributeName, StringTransform stringTransform)

Use an attribute on an element to generate an identifier for the RDF node.
 Any single attribute can be used, and adding a namespace or a prefix to the ID is simple
 as part of the transform.

**XML example**
```xml
<archive xmlns="http://example.org/">
  <record nr="0000001">
    <title>Important record</title>
  </record>
  <record nr="0000002">
    <title>Other record</title>
  </record>
</archive>
```

### Use the record number (nr) as the node ID in the RDF.
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .useAttributeForId("http://example.org/record", "http://example.org/nr", v -> "http://acme.com/records/"+v)
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

<http://acme.com/records/0000002>
        a         ex:record ;
        ex:nr     "0000002" ;
        ex:title  "Other record" .

[ a                  ex:archive ;
  xmlToRdf:hasChild  <http://acme.com/records/0000002> , <http://acme.com/records/0000001>
] .

<http://acme.com/records/0000001>
        a         ex:record ;
        ex:nr     "0000001" ;
        ex:title  "Important record" .

```

---
### With default blank node
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:archive ;
  xmlToRdf:hasChild  [ a         ex:record ;
                       ex:nr     "0000002" ;
                       ex:title  "Other record"
                     ] ;
  xmlToRdf:hasChild  [ a         ex:record ;
                       ex:nr     "0000001" ;
                       ex:title  "Important record"
                     ]
] .

```

---
## autoAddSuffixToNamespace(String sign)

Namespaces in RDF typically end in either `/` or `#` unlike in XML where a
 namespace often has no specific suffix. By default a `#` is added to the namespace if
 it doesn't already end in either `/` or `#`.

**XML example**
```xml
<people xmlns="http://example.org">
  <name>John Doe</name>
</people>
```

### `#` suffix
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .autoAddSuffixToNamespace("#")
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                          <http://example.org#people> ;
  <http://example.org#name>  "John Doe"
] .

```

---
### Unaltered XML namespace
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .autoAddSuffixToNamespace(false)
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                         <http://example.orgpeople> ;
  <http://example.orgname>  "John Doe"
] .

```

---
## mapTextInElementToUri(String elementName, String from, Object to)

Map the text inside an element to a URI.

**XML example**
```xml
<people xmlns="http://example.org/">
  <name>John Doe</name>
  <maritalStatus>married</maritalStatus>
</people>
```

### Map `married` to a URI
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .mapTextInElementToUri("http://example.org/maritalStatus", "married", "http://someReferenceData.org/married")
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                 ex:people ;
  ex:maritalStatus  <http://someReferenceData.org/married> ;
  ex:name           "John Doe"
] .

```

---
### No mapping
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                 ex:people ;
  ex:maritalStatus  "married" ;
  ex:name           "John Doe"
] .

```

---
## convertComplexElementsWithOnlyAttributesToPredicate(boolean enabled)

Use element name as predicate instead of the rdf:type on complex elements that only contain attributes.

**XML example**
```xml
<people xmlns="http://example.org/">
  <person age="89" name="John Doe"/>
</people>
```

### convertComplexElementsWithOnlyAttributesToPredicate enabled
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .convertComplexElementsWithOnlyAttributesToPredicate(true)
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a          ex:people ;
  ex:person  [ ex:age   "89" ;
               ex:name  "John Doe"
             ]
] .

```

---
### convertComplexElementsWithOnlyAttributesToPredicate disabled
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .convertComplexElementsWithOnlyAttributesToPredicate(false)
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:people ;
  xmlToRdf:hasChild  [ a        ex:person ;
                       ex:age   "89" ;
                       ex:name  "John Doe"
                     ]
] .

```

---
## autoAttributeNamespace(boolean enabled)

Uses the namespace for the element as the namespace for any attributes that lack namespaces. Default: true.

**XML example**
```xml
<people xmlns="http://example.org/">
  <name test="yay">John Doe</name>
</people>
```

### autoAttributeNamespace enabled
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .setBaseNamespace("http://none/", Builder.AppliesTo.bothElementsAndAttributes)
   .autoAttributeNamespace(true)
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:people ;
  xmlToRdf:hasChild  [ a                  ex:name ;
                       xmlToRdf:hasValue  "John Doe" ;
                       ex:test            "yay"
                     ]
] .

```

---
### autoAttributeNamespace disabled
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .setBaseNamespace("http://none/", Builder.AppliesTo.bothElementsAndAttributes)
   .autoAttributeNamespace(false)
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:people ;
  xmlToRdf:hasChild  [ a                   ex:name ;
                       xmlToRdf:hasValue   "John Doe" ;
                       <http://none/test>  "yay"
                     ]
] .

```

---
## setBaseNamespace(String namespace, Builder.AppliesTo which)

Sets a namespace for elements and attributes that lack their own namespace. This is recommended to use
 in order to make sure everything has a namespace in your final RDF.

**XML example**
```xml
<people xmlns:other="http://other.org/">
  <name other:age="1">John Doe</name>
  <other:name age="2">Unknown</other:name>
</people>
```

### Use example.org with elements and attributes
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .autoAttributeNamespace(false)
   .setBaseNamespace("http://example.org/", Builder.AppliesTo.bothElementsAndAttributes)
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:people ;
  xmlToRdf:hasChild  [ a                  <http://other.org/name> ;
                       xmlToRdf:hasValue  "Unknown" ;
                       ex:age             "2"
                     ] ;
  xmlToRdf:hasChild  [ a                       ex:name ;
                       xmlToRdf:hasValue       "John Doe" ;
                       <http://other.org/age>  "1"
                     ]
] .

```

---
### Use empty namespace
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  <file:///home/veronika/Projects/xmlToRdf/XmlToRdf/people> ;
  xmlToRdf:hasChild  [ a                       <http://other.org/name> ;
                       xmlToRdf:hasValue       "Unknown" ;
                       <http://other.org/age>  "2"
                     ] ;
  xmlToRdf:hasChild  [ a                       <file:///home/veronika/Projects/xmlToRdf/XmlToRdf/name> ;
                       xmlToRdf:hasValue       "John Doe" ;
                       <http://other.org/age>  "1"
                     ]
] .

```

---
## convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(boolean enabled)

Use the element name as the predicate rather than the rdf:type of elements that are complex type, but
 only contain simple type elements and/or attributes

**XML example**
```xml
<people xmlns="http://example.org/">
  <person age="89" name="John Doe">
    <maritalStatus>unknown</maritalStatus>
  </person>
</people>
```

### Use `person` as the predicate
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(true)
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a          ex:people ;
  ex:person  [ ex:age            "89" ;
               ex:maritalStatus  "unknown" ;
               ex:name           "John Doe"
             ]
] .

```

---
### Use `person` as the rdf:type
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(false)
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:people ;
  xmlToRdf:hasChild  [ a                 ex:person ;
                       ex:age            "89" ;
                       ex:maritalStatus  "unknown" ;
                       ex:name           "John Doe"
                     ]
] .

```

---
## autoTypeLiterals(boolean enabled)

Not implemented fully

**XML example**
```xml
<people xmlns="http://example.org/">
  <person idNumber="1234" married="true" weight="80.5">
    <name>John Doe</name>
    <age>99</age>
    <dateOfBirth>1900-01-01</dateOfBirth>
    <dateAndTimeOfBirth>1900-01-01T00:00:01+01:00</dateAndTimeOfBirth>
  </person>
</people>
```

### Automatically detect literal types
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .autoTypeLiterals(true)
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:people ;
  xmlToRdf:hasChild  [ a                      ex:person ;
                       ex:age                 99 ;
                       ex:dateAndTimeOfBirth  "1900-01-01T00:00:01+01:00"^^xsd:dateTime ;
                       ex:dateOfBirth         "1900-01-01"^^xsd:date ;
                       ex:idNumber            1234 ;
                       ex:married             "true" ;
                       ex:name                "John Doe" ;
                       ex:weight              80.5
                     ]
] .

```

---
### Use untyped literals
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .autoTypeLiterals(false)
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:people ;
  xmlToRdf:hasChild  [ a                      ex:person ;
                       ex:age                 "99" ;
                       ex:dateAndTimeOfBirth  "1900-01-01T00:00:01+01:00" ;
                       ex:dateOfBirth         "1900-01-01" ;
                       ex:idNumber            "1234" ;
                       ex:married             "true" ;
                       ex:name                "John Doe" ;
                       ex:weight              "80.5"
                     ]
] .

```

---
## insertPredicate(String predicate)

Uses the specified predicate between the parent and the child. Order of application:
 - between("parent", "child")
 - betweenSpecificParentAndAnyChild("parent")
 - betweenAnyParentAndSpecificChild("child")
 - betweenAny()

**XML example**
```xml
<people xmlns="http://example.org/">
  <person age="89" name="John Doe">
    <maritalStatus>unknown</maritalStatus>
  </person>
</people>
```

### Insert hasPerson predicate
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .insertPredicate("http://example.org/hasPerson").between("http://example.org/people", "http://example.org/person")
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a             ex:people ;
  ex:hasPerson  [ a                 ex:person ;
                  ex:age            "89" ;
                  ex:maritalStatus  "unknown" ;
                  ex:name           "John Doe"
                ]
] .

```

---
### Use default hasChild perdicate
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:people ;
  xmlToRdf:hasChild  [ a                 ex:person ;
                       ex:age            "89" ;
                       ex:maritalStatus  "unknown" ;
                       ex:name           "John Doe"
                     ]
] .

```

---
## invertPredicate(String predicate)

Inverts an inserted predicate between two elements, so that the inherit parent -> child relationship is reversed.
 Remember to insert a predicate before trying to invert it.

**XML example**
```xml
<person name="John Doe" xmlns="http://example.org/">
  <dog name="Woof"/>
</person>
```

### Insert and invert `ownedBy`
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .insertPredicate("http://example.org/ownedBy").between("http://example.org/person", "http://example.org/dog")
   .invertPredicate("http://example.org/ownedBy").betweenAny()
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a           ex:dog ;
  ex:name     "Woof" ;
  ex:ownedBy  [ a        ex:person ;
                ex:name  "John Doe"
              ]
] .

```

---
### Just insert `ownedBy`
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .insertPredicate("http://example.org/ownedBy").between("http://example.org/person", "http://example.org/dog")
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a           ex:person ;
  ex:name     "John Doe" ;
  ex:ownedBy  [ a        ex:dog ;
                ex:name  "Woof"
              ]
] .

```

---
## skipElement(String elementName)

Skip and element and all contained elements. Includes the element named, and continues skipping until the closing tag is reached.

**XML example**
```xml
<people xmlns="http://example.org/">
  <person>
    <name>John Doe</name>
  </person>
</people>
```

### Skip `person` with subtree.
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .skipElement("http://example.org/person")
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a       ex:people ] .

```

---
### Without skipping any elements
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:people ;
  xmlToRdf:hasChild  [ a        ex:person ;
                       ex:name  "John Doe"
                     ]
] .

```

---
## useElementAsPredicate(String elementName)

Create a predicate between the parent and the children elements of an element instead of a node. The element name is used as the
 predicate URI. Elements used as predicates should be complex elements without any attributes (the converter will skip any attributes). It is also
 recommended to only use elements as predicates where the child elements are all complex.

**XML example**
```xml
<people xmlns="http://example.org/">
  <person>
    <name>John Doe</name>
    <friends>
      <friend>
        <name>Jane Doe</name>
      </friend>
      <friend>
        <name>John Smith</name>
      </friend>
      <numberOfFriends>2</numberOfFriends>
    </friends>
  </person>
</people>
```

### Use `friends` as a predicate between `person` and `friend`
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .useElementAsPredicate("http://example.org/friends")
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:people ;
  xmlToRdf:hasChild  [ a           ex:person ;
                       ex:friends  [ a                  ex:numberOfFriends ;
                                     xmlToRdf:hasValue  "2"
                                   ] ;
                       ex:friends  [ a        ex:friend ;
                                     ex:name  "John Smith"
                                   ] ;
                       ex:friends  [ a        ex:friend ;
                                     ex:name  "Jane Doe"
                                   ] ;
                       ex:name     "John Doe"
                     ]
] .

```

---
### `friends` becomes a blank node by default
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:people ;
  xmlToRdf:hasChild  [ a                  ex:person ;
                       xmlToRdf:hasChild  [ a                   ex:friends ;
                                            xmlToRdf:hasChild   [ a        ex:friend ;
                                                                  ex:name  "John Smith"
                                                                ] ;
                                            xmlToRdf:hasChild   [ a        ex:friend ;
                                                                  ex:name  "Jane Doe"
                                                                ] ;
                                            ex:numberOfFriends  "2"
                                          ] ;
                       ex:name            "John Doe"
                     ]
] .

```

---
## forceMixedContent(String elementName)

Force mixed content handling for elements, even when they do not
 contain mixed content.

**XML example**
```xml
<document xmlns="http://example.org/">
  <paragraph>
    <b>Hello</b>
    <b>World</b>!</paragraph>
  <paragraph>Hello, World!</paragraph>
</document>
```

### Use forced mixed content on `paragraph`.
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .forceMixedContent("http://example.org/paragraph")
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

_:b0    a                  ex:b ;
        xmlToRdf:hasValue  "World" .

[ a                  ex:document ;
  xmlToRdf:hasChild  [ a                         ex:paragraph ;
                       xmlToRdf:hasMixedContent  ( "Hello, World!" ) ;
                       xmlToRdf:hasValue         "Hello, World!"
                     ] ;
  xmlToRdf:hasChild  [ a                         ex:paragraph ;
                       xmlToRdf:hasChild         _:b0 , _:b1 ;
                       xmlToRdf:hasMixedContent  ( _:b1 " " _:b0 "!" ) ;
                       xmlToRdf:hasValue         "!"
                     ]
] .

_:b1    a                  ex:b ;
        xmlToRdf:hasValue  "Hello" .

```

---
### With auto detection of mixed content.
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

_:b0    a                  ex:b ;
        xmlToRdf:hasValue  "Hello" .

_:b1    a                  ex:b ;
        xmlToRdf:hasValue  "World" .

[ a                  ex:document ;
  xmlToRdf:hasChild  [ a                         ex:paragraph ;
                       xmlToRdf:hasChild         _:b0 , _:b1 ;
                       xmlToRdf:hasMixedContent  ( _:b0 _:b1 " !" ) ;
                       xmlToRdf:hasValue         "!"
                     ] ;
  ex:paragraph       "Hello, World!"
] .

```

---
## compositeId(String elementName)

Use attributes and child elements to create a composite identifier for an element. `compositeId("elementName")` returns
 a builder to list your required elements and attributes followed by a mapping of those to a string which will be used as the
 URI for the RDF resource.

**XML example**
```xml
<documents xmlns="http://example.org/">
  <document seqnr="1">
    <organisation>Abc</organisation>
    <title>Hello</title>
  </document>
  <document seqnr="2">
    <organisation>Def</organisation>
    <title>Hi</title>
  </document>
</documents>
```

### Create composite id from `organisation` and `seqnr`
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .compositeId("http://example.org/document")
   .fromElement("http://example.org/organisation")
   .fromAttribute("http://example.org/seqnr")
   .mappedTo((elementMap, attributeMap) -> "http://acme.com/"+elementMap.get("http://example.org/organisation") + attributeMap.get("http://example.org/seqnr"))
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

<http://acme.com/Def2>
        a                ex:document ;
        ex:organisation  "Def" ;
        ex:seqnr         "2" ;
        ex:title         "Hi" .

<http://acme.com/Abc1>
        a                ex:document ;
        ex:organisation  "Abc" ;
        ex:seqnr         "1" ;
        ex:title         "Hello" .

[ a                  ex:documents ;
  xmlToRdf:hasChild  <http://acme.com/Def2> , <http://acme.com/Abc1>
] .

```

---
### Use default blank nodes
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:documents ;
  xmlToRdf:hasChild  [ a                ex:document ;
                       ex:organisation  "Def" ;
                       ex:seqnr         "2" ;
                       ex:title         "Hi"
                     ] ;
  xmlToRdf:hasChild  [ a                ex:document ;
                       ex:organisation  "Abc" ;
                       ex:seqnr         "1" ;
                       ex:title         "Hello"
                     ]
] .

```

---
## uuidBasedIdInsteadOfBlankNodes(boolean enabled)

By default or elements are converted to blank nodes. Elements can alse be converted to regular RDF nodes with a UUID as the node ID.
 Blank nodes are locally unique, while UUIDs are globally unique. UUIDs take time to generate, depending on your system, and will make the conversion
 from XML to RDF considerably slower.

**XML example**
```xml
<people xmlns="http://example.org/">
  <name>John Doe</name>
</people>
```

### Use UUIDs
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .uuidBasedIdInsteadOfBlankNodes(true)
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

ex:7c592937-f807-47de-b89c-96aabd08e402
        a        ex:people ;
        ex:name  "John Doe" .

```

---
### Use locally unique blank node
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .uuidBasedIdInsteadOfBlankNodes(false)
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a        ex:people ;
  ex:name  "John Doe"
] .

```

---
## setDatatype(String element, Object datatype)

Specify the datatype on a Simple Type element. Use a string with AdvancedBuilderStream as the datatype,
 and the respective Sesame or Jena types with AdvancedBuilderSesame and AdvancedBuilderJena.

**XML example**
```xml
<people xmlns="http://example.org/">
  <name>John Doe</name>
  <age>1</age>
</people>
```

### Make `age` an integer
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .setDatatype("http://example.org/age", "http://www.w3.org/2001/XMLSchema#integer")
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a        ex:people ;
  ex:age   1 ;
  ex:name  "John Doe"
] .

```

---
### Leave untyped
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a        ex:people ;
  ex:age   "1" ;
  ex:name  "John Doe"
] .

```

---
## addComplexElementTransformAtEndOfElement(String element, ComplexClassTransform transform)

Do any transformation on an element will full access to information about its attributes and children.
 The transformation is applied when the convertor hits the end element tag.

**XML example**
```xml
<person xmlns="http://example.org/">
  <person>
    <name>John Doe</name>
  </person>
  <person>
    <name>Other person</name>
  </person>
</person>
```

### 
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .addComplexElementTransformAtEndOfElement("http://example.org/name", element -> element.type = element.type.toUpperCase())
   .addComplexElementTransformAtEndOfElement("http://example.org/person", element -> {
   if(element.hasChild.size() > 1){
   element.type = "http://example.org/people";
   }
   })
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:people ;
  xmlToRdf:hasChild  [ a                          ex:person ;
                       <HTTP://EXAMPLE.ORG/NAME>  "Other person"
                     ] ;
  xmlToRdf:hasChild  [ a                          ex:person ;
                       <HTTP://EXAMPLE.ORG/NAME>  "John Doe"
                     ]
] .

```

---
### No transforms
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:person ;
  xmlToRdf:hasChild  [ a        ex:person ;
                       ex:name  "Other person"
                     ] ;
  xmlToRdf:hasChild  [ a        ex:person ;
                       ex:name  "John Doe"
                     ]
] .

```

---
## addComplexElementTransformAtStartOfElement(String element, ComplexClassTransform transform)

Do any transformation on an element will full access to information about its attributes but not about it's children.
 The transformation is applied when the convertor finishes processing the attributes at the start of a tag.
 <p>
 Take careful note, as shown in the examples, that transforming an element at the start is simpler to reason about that at the
 end when you are using options such as insertPredicate. In the seconds java example the transform is run at the end of the element,
 after the insertPredicate() method has run.
 <p>
 ```xml
 <people> <!-- start element transform runs now -->
 <person> <!-- start element transform runs now -->
 <name>John Doe</name>
 </person> <!-- end element transform followed by insertPredicate runs now -->
 </people> <!-- end element transform runs now -->
 ```

**XML example**
```xml
<people xmlns="http://example.org/">
  <person>
    <name>John Doe</name>
  </person>
</people>
```

### All transforms run before insertPredicate
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .addComplexElementTransformAtStartOfElement("http://example.org/people", element -> element.type = element.type.toUpperCase())
   .addComplexElementTransformAtStartOfElement("http://example.org/person", element -> element.type = element.type.toUpperCase())
   .insertPredicate("http://example.org/hasPerson").between("HTTP://EXAMPLE.ORG/PEOPLE", "HTTP://EXAMPLE.ORG/PERSON")
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a             <HTTP://EXAMPLE.ORG/PEOPLE> ;
  ex:hasPerson  [ a        <HTTP://EXAMPLE.ORG/PERSON> ;
                  ex:name  "John Doe"
                ]
] .

```

---
### Transform on `<people>` runs after insertPredicate
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .addComplexElementTransformAtEndOfElement("http://example.org/people", element -> element.type = element.type.toUpperCase())
   .addComplexElementTransformAtEndOfElement("http://example.org/person", element -> element.type = element.type.toUpperCase())
   .insertPredicate("http://example.org/hasPerson").between("HTTP://EXAMPLE.ORG/PEOPLE", "HTTP://EXAMPLE.ORG/PERSON")
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  <HTTP://EXAMPLE.ORG/PEOPLE> ;
  xmlToRdf:hasChild  [ a        <HTTP://EXAMPLE.ORG/PERSON> ;
                       ex:name  "John Doe"
                     ]
] .

```

---
## resolveAsQnameInAttributeValue(boolean enabled)

Will resolve a qname inside an attribute by expanding it to a full URI as a string.

**XML example**
```xml
<people xmlns="http://example.org/" xmlns:test="http://test.com/">
  <name age="test:old">John Doe</name>
</people>
```

### Resolve all qnames
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .resolveAsQnameInAttributeValue(true)
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:people ;
  xmlToRdf:hasChild  [ a                  ex:name ;
                       xmlToRdf:hasValue  "John Doe" ;
                       ex:age             "http://test.com/old"
                     ]
] .

```

---
### Do not resolve qnames
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .resolveAsQnameInAttributeValue(false)
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:people ;
  xmlToRdf:hasChild  [ a                  ex:name ;
                       xmlToRdf:hasValue  "John Doe" ;
                       ex:age             "test:old"
                     ]
] .

```

---
## xsiTypeSupport(boolean enabled)

Detects and uses the value in xsi:type attributes as the rdf:type.

**XML example**
```xml
<animals xmlns="http://example.org/"
  xmlns:dbpedia="http://dbpedia.org/resource/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <human xsi:type="man">
    <name>John Doe</name>
  </human>
  <bird xsi:type="dbpedia:Barn_swallow">
    <name>Big swallow</name>
  </bird>
</animals>
```

### Detect and use xsi:type references
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .xsiTypeSupport(true)
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:animals ;
  xmlToRdf:hasChild  [ a        <http://dbpedia.org/resource/Barn_swallow> ;
                       ex:name  "Big swallow"
                     ] ;
  xmlToRdf:hasChild  [ a        ex:man ;
                       ex:name  "John Doe"
                     ]
] .

```

---
### Ignore xsi:type references
**Java code**
```java
Builder.getAdvancedBuilderStream()
   .build()
```

**RDF output**
```turtle
@prefix xmlToRdf: <http://acandonorway.github.com/XmlToRdf/ontology.ttl#> .
@prefix ex:    <http://example.org/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

[ a                  ex:animals ;
  xmlToRdf:hasChild  [ a        ex:bird ;
                       ex:name  "Big swallow" ;
                       <http://www.w3.org/2001/XMLSchema-instance#type>
                               "dbpedia:Barn_swallow"
                     ] ;
  xmlToRdf:hasChild  [ a        ex:human ;
                       ex:name  "John Doe" ;
                       <http://www.w3.org/2001/XMLSchema-instance#type>
                               "man"
                     ]
] .

```

---



# Generate documentation
Run `sh generateDocs.sh` in the terminal.
