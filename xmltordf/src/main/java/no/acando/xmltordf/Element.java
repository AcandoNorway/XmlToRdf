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

import java.util.*;


public class Element<ResourceType, Datatype> {

    private final AdvancedSaxHandler<ResourceType, Datatype> handler;
    private final Builder.Advanced<ResourceType, Datatype, ? extends Builder.Advanced> builder;

    public String type;
    public String uri;
    public Element parent;
    public StringBuilder hasValue;
    public List<Element> hasChild = new ArrayList<>(10);
    public List<Property> properties = new ArrayList<>(3);
    long index = 0;
    long elementIndex = 0;
    private boolean shallow;
    private boolean autoDetectedAsLiteralProperty;
    CountingMap indexMap = new CountingMap();


    public List<Object> mixedContent = new ArrayList<>();
    public StringBuilder tempMixedContentString = new StringBuilder("");
    public boolean useElementAsPredicate;
    boolean containsMixedContent;
    private boolean delayedOutput;
    private Runnable delayedCallback;

    public Element(AdvancedSaxHandler<ResourceType, Datatype> handler, Builder.Advanced<ResourceType, Datatype, ? extends Builder.Advanced> builder) {
        this.handler = handler;
        this.builder = builder;

    }

    public void appendValue(char[] ch, int start, int length) {
        if (!hasChild.isEmpty()) {
            if (!containsMixedContent && !new String(ch, start, length).trim().isEmpty()) {
                containsMixedContent = true;
                hasChild.forEach(e -> mixedContent.add(e));
            }
        }

        if (hasValue == null) {
            hasValue = new StringBuilder(new String(ch, start, length));
        } else {
            hasValue.append(ch, start, length);
        }
        tempMixedContentString.append(ch, start, length);
        hasValueString = null;
    }


    String hasValueString;
    boolean hasValueStringEmpty = false;

    public String getHasValue() {

        if (hasValue == null) {
            return null;
        }
        if (hasValueString == null) {
            hasValueString = hasValue.toString().trim();
            hasValueStringEmpty = hasValueString.isEmpty();
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


    void addMixedContent(Element element) {
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


        endMixedContent();

        if (delayedCallback != null) {
            delayedCallback.run();
        }

        builder.doComplexTransformElementAtEndOfElement(this);

        if (builder.useElementAsPredicateMap != null && builder.useElementAsPredicateMap.containsKey(type)) {

            hasChild.stream()
                .forEach(child -> handler.createTriple(parent.uri, type, child.uri));

            return;
        }

        if (parent != null && !parent.useElementAsPredicate) {
            if (builder.convertComplexElementsWithOnlyAttributesToPredicates && hasChild.isEmpty()) {
                shallow = true;
            } else if (builder.convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate) {
                if (hasChild.stream().filter((element -> !element.autoDetectedAsLiteralProperty)).count() == 0) {
                    shallow = true;
                }
            }

            if (shallow) {
                if (builder.getInsertPredicateBetweenOrDefaultPredicate(parent.type, type, XmlToRdfVocabulary.hasChild) != XmlToRdfVocabulary.hasChild) {
                    shallow = false;
                }

            }
        }

        boolean shouldConvertToLiteralProperty = builder.autoDetectLiteralProperties && hasChild.isEmpty() && properties.isEmpty() && parent != null && parent.mixedContent.isEmpty() && !parent.useElementAsPredicate;
        if (shouldConvertToLiteralProperty) {

            if (!parent.containsMixedContent && !delayedOutput && parent.getHasValue() == null) {
                if (getHasValue() != null) {
                    delayedOutput = true;
                    if (parent.delayedCallback == null) {
                        parent.delayedCallback = () -> {
                            createTriples();
                            cleanUp();
                        };

                    } else {

                        Runnable delayedCallback = parent.delayedCallback;

                        parent.delayedCallback = () -> {
                            createTriples();
                            cleanUp();
                            delayedCallback.run();
                        };

                    }

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

        properties.stream().filter(property -> property != null).forEach((property) -> {

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
        hasChild = null;
        parent = null;
        properties = null;
    }

}

