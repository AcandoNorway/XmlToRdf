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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import static no.acando.xmltordf.Common.BLANK_NODE_PREFIX;
import static no.acando.xmltordf.Common.seperator;


public class FastSaxHandler extends org.xml.sax.helpers.DefaultHandler {

    private final UndoableBufferedPrintWriter out;

    // A Deque used as a stack to keep track of the URI/Bnode ID of the parent elements
    private final Deque<String> nodeIdStack = new ArrayDeque<>(100);

    // A Deque used as a stack to hold the string builders that keep all the characters in the value of the xml element
    private final Deque<StringBuilder> stringBuilderStack = new ArrayDeque<>(100);

    // A Deque used as a stack to keep track of what are essentially the element names, which are used for rdf:type
    private final Deque<String> typeStack = new ArrayDeque<>(100);

    private long index = 0;

    Builder.Fast builder;

    public FastSaxHandler(OutputStream out, Builder.Fast builder) {
        this.out = new UndoableBufferedPrintWriter(new PrintStream(out, false));
        this.builder = builder;
    }

    @Override
    public void endDocument() throws SAXException {
        out.flush();

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

        final String bnode = "_:index" + index++;

        if (nodeIdStack.size() > 0) {
            String parent = nodeIdStack.peek();
            out.println(createTriple(parent, XmlToRdfVocabulary.hasChild, bnode));
        }

        out.println(createTriple(bnode, RDF.type.getURI(), fullyQualifiedName));

        typeStack.push(fullyQualifiedName);

        nodeIdStack.push(bnode);

        stringBuilderStack.push(new StringBuilder());

        for (int i = 0; i < attributes.getLength(); i++) {
            String uriAttr = attributes.getURI(i);
            final String nameAttr = attributes.getLocalName(i);
            String valueAttr = attributes.getValue(i);

            if (builder.transformForAttributeValueMap != null) {
                StringTransform stringTransform = null;

                Map<String, StringTransform> map = builder.transformForAttributeValueMap;

                // TODO: fix me after open source o'clock
                // Handles support for wildcard search
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

        final String stringPop = nodeIdStack.pop();
        final String typePop = typeStack.pop();
        final String value = stringBuilderStack.pop().toString().trim();

        if (!value.isEmpty()) {
            handleTextValue(stringPop, typePop, value);
        } else if (out.peek().equals(createTriple(stringPop, RDF.type.getURI(), typePop))) {
            cleanUpEmptyTag(stringPop);
        }

    }

    private void cleanUpEmptyTag(String stringPop) {
        String outPop = out.pop();
        if (out.peek().equals(createTriple(nodeIdStack.peek(), XmlToRdfVocabulary.hasChild, stringPop))) {
            out.pop();
        } else {
            out.println(outPop);
        }
    }

    //TODO: Comment me
    private void handleTextValue(String stringPop, String typePop, String value) {
        if (builder.autoDetectLiteralProperties) {

            if (out.peek().equals(createTriple(stringPop, RDF.type.getURI(), typePop))) {

                if (nodeIdStack.isEmpty()) {
                    out.println(createTripleLiteral(stringPop, XmlToRdfVocabulary.hasValue, value));
                    nodeIdStack.push(stringPop);

                } else {
                    out.pop();
                    out.pop();
                    out.println(createTripleLiteral(nodeIdStack.peek(), typePop, value));
                }

            } else {
                out.println(createTripleLiteral(stringPop, XmlToRdfVocabulary.hasValue, value));
            }

        } else {
            out.println(createTripleLiteral(stringPop, XmlToRdfVocabulary.hasValue, value));
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        stringBuilderStack.peek().append(ch, start, length);
    }

    String createTriple(String subject, String predicate, String object) {

        final boolean subjectIsBlank = subject.startsWith(BLANK_NODE_PREFIX);
        final boolean objectIsBlank = object.startsWith(BLANK_NODE_PREFIX);

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
                return '<' + subject + "> <" + predicate + "> <" + object + ">.";

            }
        }

    }

    private String createTripleLiteral(String subject, String predicate, String literal) {
        literal = literal
            .replace("\\", "\\\\")
            .replace("\"", "\\\"");

        final boolean oIsBlank = subject.startsWith(BLANK_NODE_PREFIX);
        if (oIsBlank) {
            return subject + " <" + predicate + "> \"\"\"" + literal + "\"\"\".";

        } else {
            return '<' + subject + "> <" + predicate + "> \"\"\"" + literal + "\"\"\" .";

        }

    }

}

