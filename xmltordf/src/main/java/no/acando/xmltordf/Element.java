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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


public class Element<ResourceType, Datatype> {

    private final AdvancedSaxHandler<ResourceType, Datatype> handler;
    private final Builder.Advanced<ResourceType, Datatype, ? extends Builder.Advanced> builder;

    private String type;
    public String uri;
    public Element<ResourceType, Datatype> parent;
    StringBuilder hasValue;
    public ArrayList<Element<ResourceType, Datatype>> hasChild = new ArrayList<>(10);
    public Map<String, Element<ResourceType, Datatype>> hasChildMap = new HashMap<>();

    public ArrayList<Property> properties = new ArrayList<>(3);
    long index = 0;
    long elementIndex = 0;
    public boolean shallow;
    private boolean autoDetectedAsLiteralProperty;
    CountingMap indexMap = new CountingMap();



    public ArrayList<Object> mixedContent = new ArrayList<>();
    public StringBuilder tempMixedContentString;
    public boolean useElementAsPredicate;
    boolean containsMixedContent;
    private boolean delayedOutput;
    //private Runnable delayedCreateTripleCallback;
    private ArrayDeque<Element> delayedCreateTripleCallback = new ArrayDeque<>();

    private int childrenWithAutoDetectedAsLiteralProperty;
    public CompositeId compositeId;


    public Element(AdvancedSaxHandler<ResourceType, Datatype> handler, Builder.Advanced<ResourceType, Datatype, ? extends Builder.Advanced> builder) {
        this.handler = handler;
        this.builder = builder;
        tempMixedContentString = handler.getStringBuilder();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if(builder.useHashmapForChildren && parent != null){
            parent.hasChildMap.remove(this.type);
            parent.hasChildMap.put(type, this);
        }


        this.type = type;
    }

    public boolean getContainsMixedContent() {
        return containsMixedContent;
    }

    public void appendValue(char[] ch, int start, int length) {

        if (!hasChild.isEmpty()) {
            if (!containsMixedContent && !new String(ch, start, length).trim().isEmpty()) {
                containsMixedContent = true;
                hasChild.forEach(e -> mixedContent.add(e));
            }
        }

        if (hasValue == null) {
            hasValue = handler.getStringBuilder().append(new String(ch, start, length));
        } else {
            hasValue.append(ch, start, length);
        }
        tempMixedContentString.append(ch, start, length);
        hasValueString = null;
    }


    String hasValueString;
    boolean hasValueStringEmpty = false;

    public void setHasValue(String s){
        setHasValue(new StringBuilder(s));
    }

    public void setHasValue(StringBuilder s){
        hasValue = s;
        hasValueString = null;
    }

    public String getHasValue() {

        if (hasValue == null) {
            return null;
        }
        if (hasValueString == null) {
            hasValueString = hasValue.toString();//.trim();
            if(hasValueString.trim().isEmpty()){
                hasValueString = "";
                hasValueStringEmpty = hasValueString.isEmpty();
            }
        }

        if (hasValueStringEmpty) {
            return null;
        }
        return hasValueString;
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


    void addMixedContent(Element<ResourceType, Datatype> element) {
        if (!containsMixedContent && hasChild.size() > 1) {
            for (int i = 0; i < hasChild.size() - 1; i++) {
                mixedContent.add(hasChild.get(i));
            }
        }

        containsMixedContent = true;
        String temp = tempMixedContentString.toString();
        if (!temp.isEmpty()) {
            mixedContent.add(temp);
        }
        tempMixedContentString = new StringBuilder("");
        mixedContent.add(element);
    }

    private void endMixedContent() {
        if (containsMixedContent) {
            if (!tempMixedContentString.toString().isEmpty()) {
                mixedContent.add(tempMixedContentString.toString());
            }
        }
    }


    void createTriples() {

        if (compositeId != null && compositeId.requiredElementFromParent.size() > 0) {
            compositeId.resolveFromParent(parent);
        }

        if (parent != null && parent.parent != null && parent.parent.uri != null && parent.compositeId != null && parent.compositeId.parentId) {
            parent.compositeId.resolveElement(XmlToRdfVocabulary.parentId, parent.parent.uri);
        }

        if (parent != null && parent.uri == null) {
            // resolve
            parent.compositeId.resolveElement(type, getHasValue());

            // delay
            parent.addDelayedTripleCreation(this);

            if (parent.compositeId.completed()) {
                parent.uri = parent.compositeId.resolveIdentifier();
            }

            if (parent != null && parent.parent != null && parent.parent.uri != null && parent.compositeId != null && parent.compositeId.parentId) {
                parent.compositeId.resolveElement(XmlToRdfVocabulary.parentId, parent.parent.uri);
            }

            return;
        }

        endMixedContent();

        if (!delayedCreateTripleCallback.isEmpty()) {

            List<Element> cleanUpList = new ArrayList<>();
            int counter = delayedCreateTripleCallback.size();
            Element prev = null;
            while (!delayedCreateTripleCallback.isEmpty()) {
                Element element = delayedCreateTripleCallback.pop();

                if(prev != null && element == prev){
                    throw new RuntimeException("Could not resolve identifier for an element on the following path in time: " + element.getPath());
                }

                element.createTriples();
                cleanUpList.add(element);
                if (counter-- < -10) {
                    // start infinite loop detection for elements that repeatedly can't resolve their parents IRI
                    prev = element;
                }
            }
            cleanUpList.forEach(Element::cleanUp);

        }

        builder.doComplexTransformElementAtEndOfElement(this);

        if (getHasValue() != null) {
            hasValue = new StringBuilder(builder.doTransformForElementValue(type, getHasValue()));
            hasValueString = null;
        }


        if (builder.useElementAsPredicateMap != null && builder.useElementAsPredicateMap.containsKey(type)) {

            hasChild.forEach(child -> handler.createTriple(parent.uri, type, child.uri));

            return;
        }

        if (parent != null && !parent.useElementAsPredicate) {
            if (builder.convertComplexElementsWithOnlyAttributesToPredicates && hasChild.isEmpty()) {
                shallow = true;
            } else if (builder.convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate) {
                if (childrenWithAutoDetectedAsLiteralProperty == hasChild.size()) {
                    shallow = true;
                }
            }

            if (shallow) {
                if (builder.getInsertPredicateBetweenOrDefaultPredicate(parent.type, type, XmlToRdfVocabulary.hasChild) != XmlToRdfVocabulary.hasChild) {
                    shallow = false;
                }

            }
        }

        boolean shouldConvertToLiteralProperty = builder.autoDetectLiteralProperties && hasChild.isEmpty() && properties.isEmpty() && parent != null && parent.mixedContent.isEmpty() && !parent.useElementAsPredicate && !containsMixedContent;
        if (shouldConvertToLiteralProperty) {

            if (!parent.containsMixedContent && !delayedOutput && parent.getHasValue() == null) {
                if (getHasValue() != null) {
                    parent.addDelayedTripleCreation(this);
                }
            } else {
                if (getHasValue() != null) {
                    createTriplesForHasValue(parent.uri, type, type);
                }
            }


        } else if (shallow) {

            //@TODO handle parent == null
            handler.createTriple(parent.uri, type, uri);
            if (getHasValue() != null) {
                createTriplesForHasValue(uri, XmlToRdfVocabulary.hasValue, uri);
            }

            addIndexTriples();

            if (!mixedContent.isEmpty()) {
                handler.createList(uri, XmlToRdfVocabulary.hasMixedContent, mixedContent);
            }


        } else {
            handler.createTriple(uri, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", type);
            if (parent != null && !parent.useElementAsPredicate) {

                String prop = builder.getInsertPredicateBetweenOrDefaultPredicate(parent.type, type, XmlToRdfVocabulary.hasChild);

                if (builder.checkInvertPredicate(prop, parent.type, type)) {
                    handler.createTriple(uri, prop, parent.uri);
                } else {
                    handler.createTriple(parent.uri, prop, uri);
                }
            }

            if (getHasValue() != null) {
                createTriplesForHasValue(uri, XmlToRdfVocabulary.hasValue, type);
            }

            if (!mixedContent.isEmpty()) {
                handler.createList(uri, XmlToRdfVocabulary.hasMixedContent, mixedContent);
            }

            addIndexTriples();

        }

        properties.stream().filter(Objects::nonNull).forEach((property) -> {

            ResourceType uriForTextInAttribute = builder.getUriForTextInAttribute(type, property.uriAttr + property.qname, property.value);
            if (uriForTextInAttribute != null) {
                handler.createTriple(uri, property.uriAttr + property.qname, uriForTextInAttribute);

            } else {
                handler.createTripleLiteral(uri, property.uriAttr + property.qname, property.value);

            }


        });

        if (!delayedOutput) {
            cleanUp();
        }

    }

    private void createTriplesForHasValue(final String subject, final String predicates, final String dataTypeLookup) {
        Optional<ResourceType> resourceType = handler.mapLiteralToResource(this);
        autoDetectedAsLiteralProperty = true;
        if(parent != null){
            parent.childrenWithAutoDetectedAsLiteralProperty++;
        }
        if (builder.dataTypeOnElement != null && builder.dataTypeOnElement.containsKey(dataTypeLookup)) {
            if (resourceType.isPresent()) {
                throw new IllegalStateException("Can not both map literal to object and have datatype at the same time.");
            }
            handler.createTripleLiteral(subject, predicates, getHasValue(), builder.dataTypeOnElement.get(dataTypeLookup)); //TRANSFORM
        } else {
            if (resourceType.isPresent()) {
                handler.createTriple(subject, predicates, resourceType.get());
            } else {
                handler.createTripleLiteral(subject, predicates, getHasValue()); //TRANSFORM
            }
        }
    }

    private void addIndexTriples() {
        if (builder.addIndex) {
            handler.createTripleLiteral(uri, XmlToRdfVocabulary.index, index);
            handler.createTripleLiteral(uri, XmlToRdfVocabulary.elementIndex, elementIndex);
        }
    }

	private void cleanUp() {
    	if(hasChild != null){
			hasChild.forEach(child -> {
				handler.returnStringBuilder(child.tempMixedContentString);
				handler.returnStringBuilder(child.hasValue);
			});
		}

		hasChild = null;
		hasChildMap = null;
		parent = null;
		properties = null;

	}

    public void addDelayedTripleCreation(Element element) {

        element.delayedOutput = true;

        delayedCreateTripleCallback.push( element);

//        if (delayedCreateTripleCallback == null) {
//            delayedCreateTripleCallback = () -> {
//                element.createTriples();
//                element.cleanUp();
//            };
//
//        } else {
//
//            Runnable delayedCallback = delayedCreateTripleCallback;
//
//            delayedCreateTripleCallback = () -> {
//                element.createTriples();
//                element.cleanUp();
//                delayedCallback.run();
//            };
//        }
    }

    public AdvancedSaxHandler<ResourceType, Datatype> getHandler() {
        return handler;
    }

    public Builder.Advanced<ResourceType, Datatype, ? extends Builder.Advanced> getBuilder() {
        return builder;
    }

    public String getPath() {

        Element temp = this;
        StringBuilder path = new StringBuilder();
        do{
            path.append(temp.type).append(" (").append(temp.uri).append(") --> ");
            temp = temp.parent;
        }while (temp != null);

        return path.substring(0, path.length()-5);
    }
}

