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

import static no.acando.xmltordf.XmlToRdfVocabulary.hasChild;
import static no.acando.xmltordf.XmlToRdfVocabulary.hasValue;


public abstract class AdvancedSaxHandler<ResourceType, Datatype> extends org.xml.sax.helpers.DefaultHandler {
    private final PrintStream out;

    private final Deque<Element> elementStack = new ArrayDeque<>(100);

    Builder.Advanced<ResourceType, Datatype, ? extends Builder.Advanced> builder;

    private long uriCounter = 0;
    private long index = 0;

    private Element skipElementUntil = null;
    static final Element skippableElement = new Element();

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

    abstract String createTripleLiteral(String subject, String predicate, String objectLiteral, Datatype datatype);

    abstract String createTriple(String uri, String hasValue, ResourceType resourceType);

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {

        if (length > 0) {

            elementStack.peek().appendValue(ch, start, length);

        }

    }

    //TODO: this method is enormous, consider breaking it down
    @Override
    public void endElement(String namespace, String localName, String qName) throws SAXException {

        Element pop = elementStack.pop();

        if (pop == skippableElement) {
            return;
        }
        else if (skipElementUntil != null) {
            if (pop == skipElementUntil) {
                skipElementUntil = null;
                return;
            }
        }

        pop.endMixedContent();

        builder.doComplexTransformElementAtEndOfElement(pop);

        if (builder.useElementAsPredicateMap != null && builder.useElementAsPredicateMap.containsKey(pop.type)) {

            pop.hasChild.stream()
                    .forEach(child -> out.println(createTriple(pop.parent.uri, pop.type, child.uri)));

            return;
        }

        if (pop.parent != null && !pop.parent.useElementAsPredicate) {
            if (builder.convertComplexElementsWithOnlyAttributesToPredicates && pop.hasChild.isEmpty()) {
                pop.shallow = true;
            } else if (builder.convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate) {
                if (pop.hasChild.stream().filter((element -> !element.autoDetectedAsLiteralProperty)).count() == 0) {
                    pop.shallow = true;
                }
            }

            if(pop.shallow){
                if(builder.getInsertPredicateBetweenOrDefaultPredicate(pop.parent.type, pop.type, hasChild) != hasChild){
                    pop.shallow = false;
                }

            }
        }

        if (builder.autoDetectLiteralProperties && pop.hasChild.isEmpty() && pop.properties.isEmpty() && pop.parent != null && pop.parent.mixedContent.isEmpty() && !pop.parent.useElementAsPredicate) {
            //convert to literal property
            if (pop.getHasValue() != null) {
                Optional<ResourceType> resourceType = mapLiteralToResource(pop);
                pop.autoDetectedAsLiteralProperty = true;
                if (builder.dataTypeOnElement != null && builder.dataTypeOnElement.containsKey(pop.type)) {
                    if (resourceType.isPresent()) {
                        throw new IllegalStateException("Can not both map literal to object and have datatype at the same time.");
                    }
                    out.println(createTripleLiteral(pop.parent.uri, pop.type, pop.getHasValue(), builder.dataTypeOnElement.get(pop.type))); //TRANSFORM
                } else {
                    if (resourceType.isPresent()) {
                        out.println(createTriple(pop.parent.uri, pop.type, resourceType.get()));
                    } else {
                        out.println(createTripleLiteral(pop.parent.uri, pop.type, pop.getHasValue())); //TRANSFORM
                    }
                }
            }

        } else if (pop.shallow) {

            //@TODO handle pop.parent == null
            out.println(createTriple(pop.parent.uri, pop.type, pop.uri));
            if (pop.getHasValue() != null) {
                Optional<ResourceType> resourceType = mapLiteralToResource(pop);
                if (builder.dataTypeOnElement != null && builder.dataTypeOnElement.containsKey(pop.uri)) {
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
                out.println(createTripleLiteral(pop.uri, XmlToRdfVocabulary.index, pop.index));
                out.println(createTripleLiteral(pop.uri, XmlToRdfVocabulary.elementIndex, pop.elementIndex));

            }
        } else {
            out.println(createTriple(pop.uri, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", pop.type));
            if (pop.parent != null && !pop.parent.useElementAsPredicate) {

                String prop = builder.getInsertPredicateBetweenOrDefaultPredicate(pop.parent.type, pop.type, hasChild);

                if (builder.checkInvertPredicate(prop, pop.parent.type, pop.type)) {
                    out.println(createTriple(pop.uri, prop, pop.parent.uri));

                } else {
                    out.println(createTriple(pop.parent.uri, prop, pop.uri));
                }
            }
            if (pop.getHasValue() != null) {
                Optional<ResourceType> resourceType = mapLiteralToResource(pop);

                if (builder.dataTypeOnElement != null && builder.dataTypeOnElement.containsKey(pop.type)) {
                    if (resourceType.isPresent()) {
                        throw new IllegalStateException("Can not both map literal to object and have datatype at the same time.");
                    } else {
                        out.println(createTripleLiteral(pop.uri, hasValue, pop.getHasValue(), builder.dataTypeOnElement.get(pop.type))); //TRANSFORM
                    }

                } else {
                    if (resourceType.isPresent()) {
                        out.println(createTriple(pop.uri, hasValue, resourceType.get())); //TRANSFORM

                    } else {
                        out.println(createTripleLiteral(pop.uri, hasValue, pop.getHasValue())); //TRANSFORM

                    }

                }




            }
            if (!pop.mixedContent.isEmpty()) {
                out.println(createList(pop.uri, XmlToRdfVocabulary.hasMixedContent, pop.mixedContent));
            }
            pop.properties.stream().forEach((property) -> {
                if (property.value != null) {
                    out.println(createTripleLiteral(pop.uri, property.uriAttr + property.qname, property.value));
                }
            });

            if (builder.addIndex) {
                out.println(createTripleLiteral(pop.uri, XmlToRdfVocabulary.index, pop.index));
                out.println(createTripleLiteral(pop.uri, XmlToRdfVocabulary.elementIndex, pop.elementIndex));

            }

        }

        cleanUp(pop);

    }

    private Optional<ResourceType> mapLiteralToResource(Element pop) {
        if (builder.literalMap != null) {
            Map<String, ResourceType> stringResourceTypeMap = builder.literalMap.get(pop.getType());
            if (stringResourceTypeMap != null) {
                ResourceType value = stringResourceTypeMap.get(pop.getHasValue());
                if (value != null) {
                    return Optional.of(value);
                }
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
    public void startElement(String namespace, String localName, String qName, Attributes attributes) throws SAXException {

        if (skipElementUntil != null) {
            elementStack.push(skippableElement);
            return;
        }

        boolean mixedContent = detectMixedContent();

        if (builder.xsiTypeSupport && attributes.getValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
            String type = attributes.getValue("http://www.w3.org/2001/XMLSchema-instance", "type");

            if (type.contains(":")) {
                String[] split = type.split(":");
                namespace = prefixUriMap.get(split[0]);
                localName = split[1];
            } else {
                localName = type;
            }
        }

        Element element = new Element();
        element.index = index++;

        namespace = calculateNamespace(namespace);

        element.type = namespace + localName;

        if (builder.skipElementMap != null && builder.skipElementMap.containsKey(element.type)) {
            skipElementUntil = element;
            elementStack.push(element);
            return;
        }

        renameElement(namespace, localName, element);

        if (builder.forcedMixedContentMap != null &&
            builder.forcedMixedContentMap.containsKey(element.type)) {

            element.containsMixedContent = true;
        }

        calculateNodeId(namespace, element);

        Element parent = null;

        if (!elementStack.isEmpty()) {
            parent = elementStack.peek();
            if (builder.addIndex) {
                element.elementIndex = parent.indexMap.plusPlus(element.type);
//                element.elementIndex =  parent.hasChild.stream().filter(e -> e.type.equals(element.type)).count();
            }
            parent.hasChild.add(element);
            if (mixedContent) {
                parent.addMixedContent(element);
            }

        }

        handleAttributes(namespace, attributes, element);

        element.parent = parent;


        if (builder.useElementAsPredicateMap != null && builder.useElementAsPredicateMap.containsKey(element.type)) {
            element.useElementAsPredicate = true;
        }

        builder.doComplexTransformElementAtStartOfElement(element);


        elementStack.push(element);

    }

    private void handleAttributes(String elementNamespace, Attributes attributes, Element element) {
        int length = attributes.getLength();
        for (int i = 0; i < length; i++) {
            String uriAttr = attributes.getURI(i);
            String nameAttr = attributes.getLocalName(i);
            String valueAttr = attributes.getValue(i);

            if (builder.xsiTypeSupport && uriAttr.equals("http://www.w3.org/2001/XMLSchema-instance") && nameAttr.equals("type")) {
                continue;
            }

            uriAttr = calculateNamespaceForAttribute(elementNamespace, uriAttr);

            valueAttr = builder.doTransformForAttribute(element.type, uriAttr + nameAttr, valueAttr);

            if (builder.resolveAsQnameInAttributeValue && valueAttr.contains(":")) {
                String[] split = valueAttr.split(":");
                split[0] = prefixUriMap.get(split[0]);
                valueAttr = String.join("", split);
            }

            builder.useAttributedForId(element.type, uriAttr + nameAttr, valueAttr, element);

            element.properties.add(new Property(uriAttr, nameAttr, valueAttr));

        }
    }

    private String calculateNamespaceForAttribute(String elementNamespace, String uriAttr) {
        if (builder.overrideNamespace != null) {
            uriAttr = builder.overrideNamespace;
        }

        if (builder.autoAddSuffixToNamespace != null) {
            if (uriAttr != null && !uriAttr.isEmpty() && !(uriAttr.endsWith("/") || uriAttr.endsWith("#"))) {
                uriAttr += builder.autoAddSuffixToNamespace;
            }
        }

        if (uriAttr == null || uriAttr.isEmpty()) {
            if (builder.autoAttributeNamespace && elementNamespace != null && !elementNamespace.isEmpty()) {
                uriAttr = elementNamespace;
            } else if (builder.baseNamespace != null && (builder.baseNamespaceAppliesTo == Builder.AppliesTo.justAttributes || builder.baseNamespaceAppliesTo == Builder.AppliesTo.bothElementsAndAttributes)) {
                uriAttr = builder.baseNamespace;
            }
        }
        return uriAttr;
    }

    private void calculateNodeId(String uri, Element element) {
        if (builder.uuidBasedIdInsteadOfBlankNodes) {
            String tempUri = uri;
            if (builder.overrideNamespace != null) {
                tempUri = builder.overrideNamespace;
            }
            element.uri = tempUri + UUID.randomUUID().toString();

        } else {
            element.uri = Common.BLANK_NODE_PREFIX + uriCounter++;

        }
    }

    private void renameElement(String uri, String localName, Element element) {
        if (builder.renameElementMap != null && builder.renameElementMap.containsKey(uri + localName)) {
            element.type = builder.renameElementMap.get(uri + localName);
        } else if (builder.renameElementFunctionMap != null) {
            StringTransformTwoValue stringTransformTwoValue = builder.renameElementFunctionMap.get(uri + localName);
            if (stringTransformTwoValue == null) {
                stringTransformTwoValue = builder.renameElementFunctionMap.get("");
            }
            if (stringTransformTwoValue != null) {
                element.type = stringTransformTwoValue.transform(uri, localName);
            }
        }
    }

    private String calculateNamespace(String uri) {
        if (builder.overrideNamespace != null) {
            return builder.overrideNamespace;
        }

        if (builder.autoAddSuffixToNamespace != null) {
            if (uri != null && !uri.isEmpty() && !(uri.endsWith("/") || uri.endsWith("#"))) {
                uri += builder.autoAddSuffixToNamespace;
            }
        }

        if ((uri == null || uri.isEmpty()) && builder.baseNamespace != null && (builder.baseNamespaceAppliesTo == Builder.AppliesTo.justElements || builder.baseNamespaceAppliesTo == Builder.AppliesTo.bothElementsAndAttributes)) {
            uri = builder.baseNamespace;
        }

        return uri;
    }

    private boolean detectMixedContent() {

        if (elementStack.size() > 0) {
            Element peek = elementStack.peek();

            if (peek.containsMixedContent) {
                return true;
            }

            if (peek.getHasValue() != null) {
                return true;
            }
        }
        return false;
    }

    boolean isBlankNode(String node) {
        return node.startsWith(Common.BLANK_NODE_PREFIX);
    }

    @Override
    public void endDocument() throws SAXException {

        out.flush();
        out.close();
    }

    HashMap<String, String> prefixUriMap = new HashMap<>();

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {

        if (builder.autoAddSuffixToNamespace != null) {
            if (uri != null && !uri.isEmpty() && !(uri.endsWith("/") || uri.endsWith("#"))) {
                uri += builder.autoAddSuffixToNamespace;
            }
        }

        if ((uri == null || uri.isEmpty()) && builder.baseNamespace != null) {
            uri = builder.baseNamespace;
        }

        if (builder.overrideNamespace != null) {
            uri = builder.overrideNamespace;
        }

        prefixUriMap.put(prefix, uri);

    }

}
