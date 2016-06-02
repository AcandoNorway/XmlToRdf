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

import org.apache.jena.vocabulary.RDF;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Stack;

import static no.acando.xmltordf.Common.seperator;


public class FastSaxHandler extends org.xml.sax.helpers.DefaultHandler {

    private UndoableBufferedPrintWriter out;
    private Stack<String> stringStack = new Stack<>();
    private Stack<StringBuilder> stringBuilderStack = new Stack<>();

    private Stack<String> typeStack = new Stack<>();


    private final String hasChild = "http://acandonorway.github.com/ontology.ttl#" + "hasChild";
    private final String hasValue = "http://acandonorway.github.com/ontology.ttl#" + "hasValue";


    private long index = 0;

    Builder.Fast builder;
    BufferedOutputStream buff;

    public FastSaxHandler(OutputStream out, Builder.Fast builder) {
        buff = new BufferedOutputStream(out, 1000000);
        this.out = new UndoableBufferedPrintWriter(new PrintStream(buff, false));
        this.builder = builder;
    }


    @Override
    public void endDocument() throws SAXException {
        out.flush();
        try {
            buff.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {


        String ns = builder.overrideNamespace;
        if (ns == null) {
            ns = uri;
        }

        String fullyQualifiedName = ns + qName;

        if (builder.mapForClasses != null && builder.mapForClasses.containsKey(uri + qName)) {
            fullyQualifiedName = builder.mapForClasses.get(uri + qName);
        }

        String bnode = "_:index" + index++;


        if (stringStack.size() > 0) {
            String parent = stringStack.peek();
            out.println(createTriple(parent, hasChild, bnode));
        }

        out.println(createTriple(bnode, RDF.type.getURI(), fullyQualifiedName));

        typeStack.push(fullyQualifiedName);


        stringStack.push(bnode);

        stringBuilderStack.push(new StringBuilder());

        for (int i = 0; i < attributes.getLength(); i++) {
            String uriAttr = attributes.getURI(i);
            String nameAttr = attributes.getLocalName(i);
            String valueAttr = attributes.getValue(i);
            String qname = attributes.getQName(i);

            if (builder.transformForAttributeValue) {
                StringTransform stringTransform = null;

                Map<String, StringTransform> map = builder.transformForAttributeValueMap;

                if (map.containsKey(uri + localName + seperator + uriAttr + nameAttr)) {
                    stringTransform = map.get(uri + localName + seperator + uriAttr + nameAttr);
                } else if (map.containsKey(uri + localName + seperator)) {
                    stringTransform = map.get(uri + localName + seperator);
                } else if (map.containsKey(seperator + uriAttr + nameAttr)) {
                    stringTransform = map.get(seperator + uriAttr + nameAttr);
                } else if (map.containsKey(seperator)) {
                    stringTransform = map.get(seperator);
                }

                if (stringTransform != null) {
                    valueAttr = stringTransform.transform(valueAttr);
                }

            }

            if (builder.overrideNamespace != null) {
                uriAttr = builder.overrideNamespace;
            }

            if (uriAttr == null || uriAttr.trim().isEmpty()) {
                uriAttr = uri;
            }

            out.println(createTripleLiteral(bnode, uriAttr + nameAttr, valueAttr));


        }


    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {


        String stringPop = stringStack.pop();
        String typePop = typeStack.pop();
        String value = stringBuilderStack.pop().toString().trim();

        if (!value.isEmpty()) {
            if (builder.autoDetectLiteralProperties) {


                if (out.peek().equals(createTriple(stringPop, RDF.type.getURI(), typePop))) {

                    if (stringStack.isEmpty()) {
                        out.println(createTripleLiteral(stringPop, hasValue, value));
                        stringStack.push(stringPop);

                    } else {
                        out.pop();
                        out.pop();
                        out.println(createTripleLiteral(stringStack.peek(), typePop, value));
                    }

                } else {
                    out.println(createTripleLiteral(stringPop, hasValue, value));
                }


            } else {
                out.println(createTripleLiteral(stringPop, hasValue, value));
            }

        } else if (out.peek().equals(createTriple(stringPop, RDF.type.getURI(), typePop))) {
            String outPop = out.pop();
            if (out.peek().equals(createTriple(stringStack.peek(), hasChild, stringPop))) {
                out.pop();
            } else {
                out.println(outPop);
            }
        }

        // @TODO consider using hashing here.
        // Handle empty elements


    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {

        stringBuilderStack.peek().append(ch, start, length);

    }


    String createTriple(String subject, String predicate, String object) {

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

    private String createTripleLiteral(String subject, String predicate, String literal) {
        literal = literal
            .replace("\\", "\\\\")
            .replace("\"", "\\\"");

        boolean oIsBlank = subject.startsWith("_:");
        if (oIsBlank) {
            return subject + " <" + predicate + "> \"\"\"" + literal + "\"\"\".";

        } else {
            return '<' + subject + "> <" + predicate + "> \"\"\"" + literal + "\"\"\" .";

        }

    }


}

