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

package no.acando.xmltordf;

import org.apache.jena.graph.NodeFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

import static no.acando.xmltordf.Common.seperator;


public class AdvancedSaxHandlerString extends AdvancedSaxHandler<String, String> {
    private final PrintStream out;
    final String hasChild = "http://acandonorway.github.com/XmlToRdf/ontology.ttl#" + "hasChild";
    final String hasValue = "http://acandonorway.github.com/XmlToRdf/ontology.ttl#" + "hasValue";
    final String index = "http://acandonorway.github.com/XmlToRdf/ontology.ttl#" + "index";
    final String EndOfFile = "http://acandonorway.github.com/XmlToRdf/ontology.ttl#" + "EndOfFile";
    private final String hasMixedContent = "http://acandonorway.github.com/XmlToRdf/ontology.ttl#" + "hasMixedContent";


    Stack<Element> elementStack = new Stack<>();


    public AdvancedSaxHandlerString(OutputStream out, Builder.AdvancedStream builder) {
        super(out, builder);
        if (out != null) {
            this.out = new PrintStream(out);
        } else {
            this.out = new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {

                }
            });
        }
        this.builder = builder;
    }

    public String createTriple(String subject, String predicate, String object) {
        boolean subjectIsBlank = subject.startsWith("_:");
        boolean objectIsBlank = object.startsWith("_:");

        if (subjectIsBlank) {
            if (objectIsBlank) {
                return subject + " <" + predicate + "> " + object + '.';

            } else {
                return subject + " <" + predicate + "> <" + object + ">.";

            }
        } else {
            if (objectIsBlank) {
                return '<' + subject + "> <" + predicate + "> " + object + '.';

            } else {
                return '<' + subject + "> " + '<' + predicate + "> <" + object + ">.";

            }
        }

    }

    public String createTripleLiteral(String subject, String predicate, String objectLiteral) {
        if (objectLiteral == null) {
            return "";
        }


        objectLiteral = objectLiteral
            .replace("\\", "\\\\")
            .replace("\"", "\\\"");


        if (!subject.startsWith("_:")) {
            return '<' + subject + "> <" + predicate + "> \"\"\"" + objectLiteral + "\"\"\" .";

        } else {
            return subject + " <" + predicate + "> \"\"\"" + objectLiteral + "\"\"\" .";

        }


    }

    public String createTripleLiteral(String subject, String predicate, long objectLong) {

        if (!subject.startsWith("_:")) {
            return '<' + subject + "> <" + predicate + "> " + '"' + objectLong + "\"^^<http://www.w3.org/2001/XMLSchema#long>" + " .";
        } else {
            return subject + " <" + predicate + "> " + '"' + objectLong + "\"^^<http://www.w3.org/2001/XMLSchema#long>" + " .";
        }


    }

    public String createList(String subject, String predicate, List<Object> mixedContent) {
        predicate = '<' + predicate + '>';
        if (!subject.startsWith("_:")) {
            subject = '<' + subject + '>';
        }

        StringBuilder stringBuilder = new StringBuilder(subject + ' ' + predicate + " (");

        mixedContent.forEach(content -> {
            if (content instanceof String) {
                String objectLiteral = (String) content;
                objectLiteral = objectLiteral
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"");
                stringBuilder.append("\"\"\"" + objectLiteral + "\"\"\" ");

            } else if (content instanceof Element) {
                Element objectElement = (Element) content;
                if (objectElement.getUri().startsWith("_:")) {
                    stringBuilder.append(objectElement.getUri() + ' ');
                } else {
                    stringBuilder.append('<' + objectElement.getUri() + "> ");

                }
            } else {
                throw new IllegalStateException("Unknown type of: " + content.getClass().toString());
            }

        });

        return stringBuilder.append(").").toString();

    }

    public String createTripleLiteral(String subject, String predicate, String objectLiteral, String datatype) {
        if (objectLiteral == null) {
            return "";
        }

        predicate = '<' + predicate + '>';

        if (!subject.startsWith("_:")) {
            subject = '<' + subject + '>';
        }


        objectLiteral = objectLiteral.replaceAll("\\\\", "\\\\\\\\");

        objectLiteral = NodeFactory.createLiteral(objectLiteral, "", false).toString();

        return subject + ' ' + predicate + ' ' + objectLiteral + "^^<" + datatype.toString() + "> .";
    }


    @Override
    public void endDocument() throws SAXException {

        out.flush();

        out.close();
    }


}
