/*
Copyright 2016 ACANDO AS

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package no.acando.xmltordf.doclet;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.openhft.compiler.CompilerUtils;
import no.acando.xmltordf.Builder;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class JsonJavadocExampleRunner {

    private static String classString = "package mypackage;\n" +
        "import no.acando.xmltordf.doclet.ExampleInterface;\n" +
        "import org.xml.sax.SAXException;\n" +
        "import java.io.*;\n" +
        "import no.acando.xmltordf.Builder;\n" +
        "import javax.xml.parsers.ParserConfigurationException;\n" +
        "import java.io.ByteArrayInputStream;\n" +
        "import java.io.IOException;\n" +
        "import java.io.StringWriter;\n" +
        "import no.acando.xmltordf.SimpleTypePolicy;\n" +
        "\n" +
        "\n" +
        "public class TempCOUNTER implements ExampleInterface{\n" +
        "\t\n" +
        "\tpublic String toString(String xml) {\n" +
        " ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();\n" +
        "\t\t\ttry {\n" +
        "\t\t\t\tBUILDER.convertToStream(new ByteArrayInputStream(xml.getBytes()), byteArrayOutputStream);\n" +
        "\t\t\t\tString s = new String(byteArrayOutputStream.toByteArray());\n" +
        "\t\t\t\treturn s;\n" +
        "\t\t\t} catch (Exception e) {\n" +
        "\t\t\t\te.printStackTrace();\n" +
        "\t\t\t} return \"\";" +
        "\t}\n" +
        "}\n";

    public static void main(String[] args) throws ClassNotFoundException, IOException, IllegalAccessException, InstantiationException, ParserConfigurationException, SAXException {

        String javadoc = "xmltordf/documentation/javadoc";
        String json = FileUtils.readFileToString(new File(javadoc + ".json"));
        Type listType = new TypeToken<ArrayList<Method>>() {
        }.getType();
        List<Method> list = new Gson().fromJson(json, listType);

        int counter = 0;

        String javadocMarkdown = javadoc + ".md";
        PrintWriter printWriter = new PrintWriter(new File(javadocMarkdown));


        for (Method method : list) {

            printWriter.println("## " + method.name);

            printWriter.println();
            printWriter.println(method.description);
            printWriter.println();


            for (Example example : method.examples) {


                printWriter.println("**XML example**\n```xml");
                printWriter.println(formatXml(example.xml));
                printWriter.println("```\n");

                for (Example.InnerExample innerExample : example.innerExamples) {
                    counter++;
                    String builder = classString
                        .replace("BUILDER", innerExample.exampleCommand)
                        .replace("COUNTER", "" + counter);


                    printWriter.println("### " + innerExample.exampleLabel);
                    printWriter.println("**Java code**\n```java");
                    printWriter.println(formatJava(innerExample.exampleCommand));
                    printWriter.println("```\n");


                    try {
                        Class aClass = CompilerUtils.CACHED_COMPILER.loadFromJava(Builder.getAdvancedBuilderStream().getClass().getClassLoader(), "mypackage.Temp" + counter, builder);

                        String s = ((ExampleInterface) aClass.newInstance()).toString(example.xml);


                        Model defaultModel = ModelFactory.createDefaultModel();

                        defaultModel.read(new ByteArrayInputStream(s.getBytes()), "", "TTL");

                        defaultModel.setNsPrefix("ex", "http://example.org/");
                        defaultModel.setNsPrefix("xmlToRdf", "http://acandonorway.github.com/XmlToRdf/ontology.ttl#");
                        defaultModel.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
                        StringWriter stringWriter = new StringWriter();
                        defaultModel.write(stringWriter, "TTL");

                        printWriter.println("**RDF output**\n```turtle");
                        printWriter.println(stringWriter.toString());
                        printWriter.println("```\n");

                    } catch (Exception e) {
                        System.out.println(builder);
                        throw e;
                    }

                    printWriter.println("---");

                }
                printWriter.println("<p>&nbsp;</p>");

            }


        }
        printWriter.close();


        org.jsoup.nodes.Document doc = Jsoup.parse(new File("xmltordf/pom.xml"), "utf-8");

        String version = doc.select("project > version").text();


        String javadocMarkdownString = FileUtils.readFileToString(new File(javadocMarkdown));

        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new InputStreamReader(new FileInputStream("templates/README_TEMPLATE.md")), "");
        StringWriter stringWriter = new StringWriter();
        mustache.execute(stringWriter, new Object() {
            String javadocs = javadocMarkdownString;
            String pomVersion = version;
        }).flush();
        FileUtils.write(new File("README.md"), stringWriter.toString());


    }

    private static String formatJava(String exampleCommand) {
        String[] split = exampleCommand.split("\n");
        split[0] = split[0].trim();
        for (int i = 1; i < split.length; i++) {
            split[i] = "  " + split[i];
        }
        return String.join("\n", split);

    }


    public static String formatXml(String unformattedXml) {
        try {
            final Document document = parseXmlFile(unformattedXml);

            OutputFormat format = new OutputFormat(document);
            format.setLineWidth(65);
            format.setIndenting(true);
            format.setIndent(2);
            Writer out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(document);

            return out.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "").trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Document parseXmlFile(String in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
