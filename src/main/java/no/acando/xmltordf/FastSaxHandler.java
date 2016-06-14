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
import java.util.Map;
import java.util.Stack;

import static no.acando.xmltordf.Common.BLANK_NODE_PREFIX;
import static no.acando.xmltordf.Common.seperator;


public class FastSaxHandler extends org.xml.sax.helpers.DefaultHandler {

    private UndoableBufferedPrintWriter out;
    private Stack<String> nodeIdStack = new Stack<>();
    private Stack<StringBuilder> stringBuilderStack = new Stack<>();

    private Stack<String> typeStack = new Stack<>();

    private final String hasChild = "http://acandonorway.github.com/XmlToRdf/ontology.ttl#" + "hasChild";
    private final String hasValue = "http://acandonorway.github.com/XmlToRdf/ontology.ttl#" + "hasValue";

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

        String bnode = "_:index" + index++;

        if (nodeIdStack.size() > 0) {
            String parent = nodeIdStack.peek();
            out.println(createTriple(parent, hasChild, bnode));
        }

        out.println(createTriple(bnode, RDF.type.getURI(), fullyQualifiedName));

        typeStack.push(fullyQualifiedName);

        nodeIdStack.push(bnode);

        stringBuilderStack.push(new StringBuilder());

        for (int i = 0; i < attributes.getLength(); i++) {
            String uriAttr = attributes.getURI(i);
            String nameAttr = attributes.getLocalName(i);
            String valueAttr = attributes.getValue(i);

            if (builder.transformForAttributeValue) {
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

        String stringPop = nodeIdStack.pop();
        String typePop = typeStack.pop();
        String value = stringBuilderStack.pop().toString().trim();

        if (!value.isEmpty()) {
            handleTextValue(stringPop, typePop, value);
        } else if (out.peek().equals(createTriple(stringPop, RDF.type.getURI(), typePop))) {
            cleanUpEmptyTag(stringPop);
        }

    }

    private void cleanUpEmptyTag(String stringPop) {
        String outPop = out.pop();
        if (out.peek().equals(createTriple(nodeIdStack.peek(), hasChild, stringPop))) {
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
                    out.println(createTripleLiteral(stringPop, hasValue, value));
                    nodeIdStack.push(stringPop);

                } else {
                    out.pop();
                    out.pop();
                    out.println(createTripleLiteral(nodeIdStack.peek(), typePop, value));
                }

            } else {
                out.println(createTripleLiteral(stringPop, hasValue, value));
            }

        } else {
            out.println(createTripleLiteral(stringPop, hasValue, value));
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        stringBuilderStack.peek().append(ch, start, length);
    }

    String createTriple(String subject, String predicate, String object) {

        boolean subjectIsBlank = subject.startsWith(BLANK_NODE_PREFIX);
        boolean objectIsBlank = object.startsWith(BLANK_NODE_PREFIX);

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
                return '<' + subject + "> <"+ predicate + "> <" + object + ">.";

            }
        }

    }

    private String createTripleLiteral(String subject, String predicate, String literal) {
        literal = literal
            .replace("\\", "\\\\")
            .replace("\"", "\\\"");

        boolean oIsBlank = subject.startsWith(BLANK_NODE_PREFIX);
        if (oIsBlank) {
            return subject + " <" + predicate + "> \"\"\"" + literal + "\"\"\".";

        } else {
            return '<' + subject + "> <" + predicate + "> \"\"\"" + literal + "\"\"\" .";

        }

    }

}

