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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

import static no.acando.xmltordf.Common.seperator;


public abstract class AdvancedSaxHandler<ResourceType, Datatype> extends org.xml.sax.helpers.DefaultHandler {
    public static final String BLANK_NODE_PREFIX = "_:";
    private final PrintStream out;
    final String hasChild = "http://acandonorway.github.com/XmlToRdf/ontology.ttl#" + "hasChild";
    final String hasValue = "http://acandonorway.github.com/XmlToRdf/ontology.ttl#" + "hasValue";
    final String index = "http://acandonorway.github.com/XmlToRdf/ontology.ttl#" + "index";
    final String EndOfFile = "http://acandonorway.github.com/XmlToRdf/ontology.ttl#" + "EndOfFile";
    private final String hasMixedContent = "http://acandonorway.github.com/XmlToRdf/ontology.ttl#" + "hasMixedContent";

    Stack<Element> elementStack = new Stack<>();

    Builder.Advanced<ResourceType, Datatype, ? extends Builder.Advanced> builder;

    private long uriCounter = 0;

    public AdvancedSaxHandler(OutputStream out, Builder.Advanced<ResourceType, Datatype, ? extends Builder.Advanced> builder) {
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

    abstract String createTriple(String subject, String predicate, String object);

    abstract String createTripleLiteral(String subject, String predicate, String objectLiteral);

    abstract String createTripleLiteral(String subject, String predicate, long objectLong);

    abstract String createList(String subject, String predicate, List<Object> mixedContent);

    abstract String createTripleLiteral(String subject, String predicate, String objectLiteral, Datatype datatype) ;

    abstract String createTriple(String uri, String hasValue, ResourceType resourceType) ;

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {

        if (length > 0) {

            elementStack.peek().appendValue(ch, start, length);

        }

    }

    //TODO: this method is enormous, consider breaking it down
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        Element pop = elementStack.pop();

        builder.doComplexTransformForClass(pop);

        if (builder.autoConvertShallowChildrenToProperties && pop.hasChild.isEmpty() && pop.parent != null) {
            pop.shallow = true;
        }

        if (builder.autoConvertShallowChildrenWithAutoDetectLiteralProperties && !pop.shallow) {
            if (pop.hasChild.stream().filter((element -> !element.autoDetectedAsLiteralProperty)).count() == 0) {
                pop.shallow = true;
            }
        }

        if (builder.autoDetectLiteralProperties && pop.hasChild.isEmpty() && pop.parent != null && pop.properties.isEmpty() && pop.parent.mixedContent.isEmpty()) {
            //convert to literal property
            if (pop.getHasValue() != null) {
                Optional<ResourceType> resourceType = mapLiteralToResource(pop);
                pop.autoDetectedAsLiteralProperty = true;
                if (builder.dataTypeOnElement.containsKey(pop.type)) {
                    if (resourceType.isPresent()) {
                        throw new IllegalStateException("Can not both map literal to object and have datatype at the same time.");
                    }
                    out.println(createTripleLiteral(pop.parent.uri, pop.type, pop.getHasValue(), builder.dataTypeOnElement.get(pop.type))); //TRANSFORM
                } else {
                    if(resourceType.isPresent()) {
                        out.println(createTriple(pop.parent.uri, pop.type, resourceType.get()));
                    } else {
                        out.println(createTripleLiteral(pop.parent.uri, pop.type, pop.getHasValue())); //TRANSFORM
                    }
                }
            }

        } else if (pop.shallow) {

            out.println(createTriple(pop.parent.uri, pop.type, pop.uri));
            if (pop.getHasValue() != null) {
                Optional<ResourceType> resourceType = mapLiteralToResource(pop);
                if (builder.dataTypeOnElement.containsKey(pop.uri)) {
                    if (resourceType.isPresent()) {
                        throw new IllegalStateException("Can not both map literal to object and have datatype at the same time.");
                    }
                    out.println(createTripleLiteral(pop.uri, hasValue, pop.getHasValue(), builder.dataTypeOnElement.get(pop.uri))); //TRANSFORM

                } else {

                    if (resourceType.isPresent()) {
                        out.println(createTriple(pop.uri, hasValue, resourceType.get()));
                    } else {
                        out.println(createTripleLiteral(pop.uri, hasValue, pop.getHasValue())); //TRANSFORM
                    }
                }
            }
            pop.properties.stream().forEach((property) -> {
                if (property.value != null) {
                    out.println(createTripleLiteral(pop.uri, property.uriAttr + property.qname, property.value));
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
                Optional<ResourceType> resourceType = mapLiteralToResource(pop);

                if (builder.dataTypeOnElement.containsKey(pop.type)) {
                   if (resourceType.isPresent()) {
                      throw new IllegalStateException("Can not both map literal to object and have datatype at the same time.");
                   } else {
                       out.println(createTripleLiteral(pop.uri, this.hasValue, pop.getHasValue(), builder.dataTypeOnElement.get(pop.type))); //TRANSFORM
                   }

                } else {
                    if (resourceType.isPresent()) {
                        out.println(createTriple(pop.uri, this.hasValue, resourceType.get())); //TRANSFORM

                    } else {
                        out.println(createTripleLiteral(pop.uri, this.hasValue, pop.getHasValue())); //TRANSFORM

                    }

                }

                if (pop.mixedContent.size() > 0) {
                    out.println(createList(pop.uri, hasMixedContent, pop.mixedContent));
                }


            }
            pop.properties.stream().forEach((property) -> {
                if (property.value != null) {
                    out.println(createTripleLiteral(pop.uri, property.uriAttr + property.qname, property.value));
                }
            });

            if (builder.addIndex) {
                out.println(createTripleLiteral(pop.uri, index, pop.index));
            }

        }

        cleanUp(pop);

    }

    private Optional<ResourceType> mapLiteralToResource(Element pop) {
        ResourceType resourceType = null;
        if (builder.literalMap != null) {
            Map<String, ResourceType> stringResourceTypeMap = builder.literalMap.get(pop.getType());
            if (stringResourceTypeMap != null) {
                return Optional.of(stringResourceTypeMap.get(pop.getHasValue()));
            }
        }
        return Optional.empty();
    }

    private void cleanUp(Element pop) {
        pop.hasChild = null;
        pop.parent = null;
        pop.properties = null;
    }

    //TODO: this method is enormous, consider breaking it down
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        boolean mixedContent = false;
        if (elementStack.size() > 0) {
            Element peek = elementStack.peek();
            if (peek.getHasValue() != null) {
                mixedContent = true;
            }
        }

        Element element = new Element();

        if (builder.autoAddSuffixToNamespace != null) {
            if (uri != null && !uri.isEmpty() && !(uri.endsWith("/") || uri.endsWith("#"))) {
                uri += builder.autoAddSuffixToNamespace;
            }
        }

        if ((uri == null || uri.isEmpty()) && builder.baseNamespace != null && (builder.baseNamespaceAppliesTo == Builder.AppliesTo.justElements || builder.baseNamespaceAppliesTo == Builder.AppliesTo.bothElementsAndAttributes)) {
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
            element.uri = BLANK_NODE_PREFIX + uriCounter++;

        }

        Element parent = null;

        if (!elementStack.isEmpty()) {
            parent = elementStack.peek();
            element.index = parent.hasChild.size();
            parent.hasChild.add(element);
            if (mixedContent) {
                parent.addMixedContent(element);
            }
        }

        int length = attributes.getLength();
        for (int i = 0; i < length; i++) {
            String uriAttr = attributes.getURI(i);
            String nameAttr = attributes.getLocalName(i);
            String valueAttr = attributes.getValue(i);

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
                if (uriAttr != null && !uriAttr.isEmpty() && !(uriAttr.endsWith("/") || uriAttr.endsWith("#"))) {
                    uriAttr += builder.autoAddSuffixToNamespace;
                }
            }

            if (uriAttr == null || uriAttr.isEmpty()) {
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

    public boolean isBlankNode(String node) {
        return node.startsWith(BLANK_NODE_PREFIX);
    }

    @Override
    public void endDocument() throws SAXException {

        out.flush();
        out.close();
    }

}
