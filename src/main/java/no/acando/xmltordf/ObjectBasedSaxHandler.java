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
import org.openrdf.model.IRI;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

import static no.acando.xmltordf.Common.seperator;


public class ObjectBasedSaxHandler extends org.xml.sax.helpers.DefaultHandler {
    private final PrintStream out;
    final String hasChild = "http://acandonorway.github.com/ontology.ttl#" + "hasChild";
    final String hasValue = "http://acandonorway.github.com/ontology.ttl#" + "hasValue";
    final String index = "http://acandonorway.github.com/ontology.ttl#" + "index";
    final String EndOfFile = "http://acandonorway.github.com/ontology.ttl#"+"EndOfFile";


    Stack<Element> elementStack = new Stack<>();

    Builder.ObjectBased builder;


    public ObjectBasedSaxHandler(OutputStream out, Builder.ObjectBased builder) {
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

    public String createTriple(String subject, String predicate, String objectResource) {
        predicate = '<' + predicate + '>';

        if (!subject.startsWith("_:")) {
            subject = '<' + subject + '>';
        }

        if (!objectResource.startsWith("_:")) {
            objectResource = '<' + objectResource + '>';
        }
        return subject + ' ' + predicate + ' ' + objectResource + " .";
    }

    public String createTripleLiteral(String subject, String predicate, String objectLiteral) {
        if (objectLiteral == null) {
            return "";
        }

        predicate = '<' + predicate + '>';

        if (!subject.startsWith("_:")) {
            subject = '<' + subject + '>';
        }


        objectLiteral = objectLiteral.replaceAll("\\\\", "\\\\\\\\");
        objectLiteral = NodeFactory.createLiteral(objectLiteral, "", false).toString();

        return subject + ' ' + predicate + "\"\"" + objectLiteral + "\"\" .";

    }

    public String createTripleLiteral(String subject, String predicate, long objectLong) {
        predicate = '<' + predicate + '>';

        if (!subject.startsWith("_:")) {
            subject = '<' + subject + '>';
        }

        return subject + ' ' + predicate + ' ' + '"' + objectLong + "\"^^<http://www.w3.org/2001/XMLSchema#long>" + " .";

    }


    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {

        String value = new String(ch, start, length);


        if (value.length() > 0) {

            elementStack.peek().appendValue(value);

        }

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        Element pop = elementStack.pop();



        builder.doComplexTransformForClass(pop);


        if (builder.autoConvertShallowChildrenToProperties && pop.hasChild.size() == 0 && pop.parent != null) {
            pop.shallow = true;
        }

        if (builder.autoConvertShallowChildrenWithAutoDetectLiteralProperties && !pop.shallow) {
            if (pop.hasChild.stream().filter((element -> !element.autoDetectedAsLiteralProperty)).count() == 0) {
                pop.shallow = true;
            }
        }

        if (builder.autoDetectLiteralProperties && pop.hasChild.isEmpty() && pop.parent != null && pop.properties.isEmpty()) {
            //convert to literal property
            if (pop.getHasValue() != null) {
                pop.autoDetectedAsLiteralProperty = true;
                if (builder.datatypeOnElement.containsKey(pop.type)) {
                    out.println(createTripleLiteral(pop.parent.uri, pop.type, pop.getHasValue(), builder.datatypeOnElement.get(pop.type)));

                } else {
                    out.println(createTripleLiteral(pop.parent.uri, pop.type, pop.getHasValue()));

                }
            }

        } else if (pop.shallow) {

            out.println(createTriple(pop.parent.uri, pop.type, pop.uri));
            if (pop.getHasValue() != null) {
                if (builder.datatypeOnElement.containsKey(pop.uri)) {
                    out.println(createTripleLiteral(pop.uri, hasValue, pop.getHasValue(), builder.datatypeOnElement.get(pop.uri)));

                } else {
                    out.println(createTripleLiteral(pop.uri, hasValue, pop.getHasValue()));

                }
            }
            pop.properties.stream().forEach((p) -> {
                if (p.value != null) {
                    out.println(createTripleLiteral(pop.uri, p.uriAttr + p.qname, p.value));
                }
            });

            if (builder.addIndex) {
                out.println(createTripleLiteral(pop.uri, index, pop.index));
            }
        } else {
            out.println(createTriple(pop.uri, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", pop.type));
            if (pop.parent != null) {
                String prop = builder.getInsertPropertyBetween(pop.parent.type, pop.type);
                if (prop == null) {
                    prop = hasChild;
                }
                if (builder.checkInvertProperty(prop, pop.parent.type, pop.type)) {
                    out.println(createTriple(pop.uri, prop, pop.parent.uri));

                } else {
                    out.println(createTriple(pop.parent.uri, prop, pop.uri));

                }
            }
            if (pop.getHasValue() != null) {
                if (builder.datatypeOnElement.containsKey(pop.type)) {
                    out.println(createTripleLiteral(pop.uri, hasValue, pop.getHasValue(), builder.datatypeOnElement.get(pop.type)));

                } else {
                    out.println(createTripleLiteral(pop.uri, hasValue, pop.getHasValue()));

                }


            }
            pop.properties.stream().forEach((p) -> {
                if (p.value != null) {
                    out.println(createTripleLiteral(pop.uri, p.uriAttr + p.qname, p.value));
                }
            });

            if (builder.addIndex) {
                out.println(createTripleLiteral(pop.uri, index, pop.index));
            }

        }


        cleanUp(pop);

    }

    public String createTripleLiteral(String subject, String predicate, String objectLiteral, IRI datatype) {
        if (objectLiteral == null) {
            return "";
        }

        predicate = '<' + predicate + '>';

        if (!subject.startsWith("_:")) {
            subject = '<' + subject + '>';
        }


        objectLiteral = objectLiteral.replaceAll("\\\\", "\\\\\\\\");

        objectLiteral = NodeFactory.createLiteral(objectLiteral, "", false).toString();

        return subject + ' ' + predicate + ' ' + objectLiteral + "^^<"+datatype.toString()+"> .";
    }



    private void cleanUp(Element pop) {
        pop.hasChild = null;
        pop.parent = null;
        pop.properties = null;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {


        Element element = new Element();

        if (builder.autoAddSuffixToNamespace != null) {
            if (uri != null && !uri.trim().equals("") && !(uri.endsWith("/") || uri.endsWith("#"))) {
                uri += builder.autoAddSuffixToNamespace;
            }
        }


        if ((uri == null || uri.trim().isEmpty()) && builder.baseNamespace != null && (builder.baseNamespaceAppliesTo == Builder.AppliesTo.justElements || builder.baseNamespaceAppliesTo == Builder.AppliesTo.bothElementsAndAttributes)) {
            uri = builder.baseNamespace;
        }

        if (builder.overrideNamespace != null) {
            element.type = builder.overrideNamespace + localName;
        } else {
            element.type = uri + localName;
        }


        if (builder.mapForClasses != null && builder.mapForClasses.containsKey(uri + localName)) {
            element.type = builder.mapForClasses.get(uri + localName);
        }

        if (builder.uuidBasedIdInsteadOfBlankNodes) {
            String tempUri = uri;
            if (builder.overrideNamespace != null) {
                tempUri = builder.overrideNamespace;
            }
            element.uri = tempUri + UUID.randomUUID().toString();

        } else {
            element.uri = "_:" + UUID.randomUUID().toString();

        }

        Element parent = null;

        if (!elementStack.isEmpty()) {
            parent = elementStack.peek();
            element.index = parent.hasChild.size();
            parent.hasChild.add(element);
        }

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

            if (builder.useAttributedForId) {
                StringTransform stringTransform = null;
                if (builder.useAttributedForIdMap.containsKey(uri + localName + seperator + uriAttr + nameAttr)) {
                    stringTransform = builder.useAttributedForIdMap.get(uri + localName + seperator + uriAttr + nameAttr);
                } else if (builder.useAttributedForIdMap.containsKey(seperator + uriAttr + nameAttr)) {
                    stringTransform = builder.useAttributedForIdMap.get(seperator + uriAttr + nameAttr);
                }

                if (stringTransform != null) {

                    element.uri = stringTransform.transform(valueAttr);
                }
            }


            if (builder.overrideNamespace != null) {
                uriAttr = builder.overrideNamespace;
            }

            if (builder.autoAddSuffixToNamespace != null) {
                if (uriAttr != null && !uriAttr.trim().equals("") && !(uriAttr.endsWith("/") || uriAttr.endsWith("#"))) {
                    uriAttr += builder.autoAddSuffixToNamespace;
                }
            }


            if (uriAttr == null || uriAttr.trim().equals("")) {
                if (builder.autoAttributeNamespace && uri != null && !uri.isEmpty()) {
                    uriAttr = uri;
                } else if (builder.baseNamespace != null && (builder.baseNamespaceAppliesTo == Builder.AppliesTo.justAttributes || builder.baseNamespaceAppliesTo == Builder.AppliesTo.bothElementsAndAttributes)) {
                    uriAttr = builder.baseNamespace;
                }

            }

            element.properties.add(new Property(uriAttr, nameAttr, valueAttr));

        }

        element.parent = parent;


        elementStack.push(element);


    }

    @Override
    public void endDocument() throws SAXException {

        out.flush();

        out.close();
    }

    public class Element {
        public String type;
        public String uri;
        public Element parent;
        public StringBuilder hasValue;
        public List<Element> hasChild = new ArrayList<>(10);
        public List<Property> properties = new ArrayList<>(10);
        private long index = 0;
        private boolean shallow;
        private boolean autoDetectedAsLiteralProperty;



        void appendValue(String value) {
            if (hasValue == null) {
                hasValue = new StringBuilder();
            }
            hasValue.append(value);
            hasValueString = null;
        }



        public String getType() {
            return type;
        }

        public String getUri() {
            return uri;
        }

        public Element getParent() {
            return parent;
        }

        String hasValueString;
        boolean hasValueStringEmpty = false;

        public String getHasValue() {

            if(hasValue == null) return null;
            if(hasValueString == null){
                hasValueString = hasValue.toString().trim();
                hasValueStringEmpty = hasValueString.equals("");
            }

            if(hasValueStringEmpty) return null;
            return hasValueString;
        }

        public List<Element> getHasChild() {
            return hasChild;
        }

        public List<Property> getProperties() {
            return properties;
        }

        public long getIndex() {
            return index;
        }

        public boolean isShallow() {
            return shallow;
        }

        public boolean isAutoDetectedAsLiteralProperty() {
            return autoDetectedAsLiteralProperty;
        }
    }

    public class Property {
        public final String value;
        public String uriAttr;
        public String qname;

        public Property(String uriAttr, String qname, String value) {
            this.uriAttr = uriAttr;
            this.qname = qname;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String getUriAttr() {
            return uriAttr;
        }

        public String getQname() {
            return qname;
        }
    }
}
