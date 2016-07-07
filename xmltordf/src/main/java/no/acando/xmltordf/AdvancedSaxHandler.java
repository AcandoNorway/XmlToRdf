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

import java.io.OutputStream;
import java.util.*;


abstract class AdvancedSaxHandler<ResourceType, Datatype> extends org.xml.sax.helpers.DefaultHandler {

    private final Deque<Element<ResourceType, Datatype>> elementStack = new ArrayDeque<>(100);

    final static String XSD = "http://www.w3.org/2001/XMLSchema#";


    Builder.Advanced<ResourceType, Datatype, ? extends Builder.Advanced> builder;

    private long uriCounter = 0;
    private long index = 0;

    private Element<ResourceType, Datatype> skipElementUntil = null;
    private final Element<ResourceType, Datatype> skippableElement = new Element<>(this, builder);

    AdvancedSaxHandler(Builder.Advanced<ResourceType, Datatype, ? extends Builder.Advanced> builder) {

        this.builder = builder;
    }

    abstract void createTriple(String subject, String predicate, String object);

    abstract void createTripleLiteral(String subject, String predicate, String objectLiteral);

    abstract void createTripleLiteral(String subject, String predicate, long objectLong);

    abstract void createList(String subject, String predicate, List<Object> mixedContent);

    abstract void createTripleLiteral(String subject, String predicate, String objectLiteral, Datatype datatype);

    abstract void createTriple(String uri, String hasValue, ResourceType resourceType);

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {

        if (length > 0) {

            elementStack.peek().appendValue(ch, start, length);

        }

    }

    @Override
    public void endElement(String namespace, String localName, String qName) throws SAXException {

        Element<ResourceType, Datatype> pop = elementStack.pop();

        if (pop == skippableElement) {
            return;
        } else if (skipElementUntil != null) {
            if (pop == skipElementUntil) {
                skipElementUntil = null;
                return;
            }
        }

        pop.createTriples();



    }

     Optional<ResourceType> mapLiteralToResource(Element pop) {
        if (builder.literalMap != null) {
            Map<String, ResourceType> stringResourceTypeMap = builder.literalMap.get(pop.type);
            if (stringResourceTypeMap != null) {
                ResourceType value = stringResourceTypeMap.get(pop.getHasValue());
                if (value != null) {
                    return Optional.of(value);
                }
            }
        }
        return Optional.empty();
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

        Element<ResourceType, Datatype> element = new Element<>(this, builder);
        element.index = index++;

        namespace = calculateNamespace(namespace);

        element.type = namespace + localName;

        if (builder.skipElementMap != null && builder.skipElementMap.containsKey(element.type)) {
            skipElementUntil = element;
            elementStack.push(element);
            return;
        }



        Element<ResourceType, Datatype> parent = null;

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

        element.parent = parent;

        renameElement(namespace, localName, element);

        if (builder.forcedMixedContentMap != null &&
            builder.forcedMixedContentMap.containsKey(element.type)) {

            element.containsMixedContent = true;
        }


        if (builder.compositeIdMap != null) {

            Builder.Advanced.CompositeIdInterface compositeId = builder.compositeIdMap.get(element.type);
            if (compositeId == null) {
                calculateNodeId(namespace, element);
            } else {
                element.compositeId = compositeId;
            }

        }else{
            calculateNodeId(namespace, element);

        }


        handleAttributes(namespace, attributes, element);


        if (builder.useElementAsPredicateMap != null && builder.useElementAsPredicateMap.containsKey(element.type)) {
            element.useElementAsPredicate = true;
        }

        builder.doComplexTransformElementAtStartOfElement(element);


        elementStack.push(element);

    }

    private void handleAttributes(String elementNamespace, Attributes attributes, Element<ResourceType, Datatype> element) {
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

            builder.getIdByUseAttributeForId(element.type, uriAttr + nameAttr, valueAttr, element);

            Property property = new Property(uriAttr, nameAttr, valueAttr);
            element.properties.add(property);

           if(element.compositeId != null){
               element.compositeId.resolveAttribute(uriAttr + nameAttr, valueAttr);
           }

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

    private void calculateNodeId(String uri, Element<ResourceType, Datatype> element) {
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

    private void renameElement(String uri, String localName, Element<ResourceType, Datatype> element) {
        if (builder.renameElementPathMap != null) {
            String newElementName = builder.renameElementPathMap.get(element);
            if (newElementName != null) {
                element.type = newElementName;
                return;
            }
        }

        if (builder.renameElementMap != null) {
            String newElementName = builder.renameElementMap.get(uri + localName);
            if (newElementName != null) {
                element.type = newElementName;
                return;
            }

        }
        if (builder.renameElementFunctionMap != null) {
            StringTransformTwoValue stringTransformTwoValue = builder.renameElementFunctionMap.get(uri + localName);
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
            Element<ResourceType, Datatype> peek = elementStack.peek();

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
