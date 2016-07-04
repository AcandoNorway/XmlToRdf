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

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.ext.com.google.common.cache.CacheBuilder;
import org.apache.jena.graph.Node;
import org.openrdf.model.IRI;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Builder {
    public static XmlPath  createPath(String ... path) {
        XmlPath xmlPath = new XmlPath(path);


        return xmlPath;
    }

     static class XmlPath{
         String[] path;

         int lastElement;

         public XmlPath(String[] path) {
             this.path = path;
             lastElement = path.length -1;
         }

         boolean equals(Element tailElemenet) {

            Element current = tailElemenet;

            for (int i = path.length-1; i >= 0; i--) {
               if(current == null) return false;

                if(!current.type.equals(path[i])){
                    return false;
                }

                current = current.parent;

            }

            return true;

        }


        public String getTail() {
            if(lastElement < 0) return null;
            return path[lastElement];
        }

         public XmlPath shorten() {
             lastElement--;

             return this;
         }

         public boolean last() {
             return lastElement == 0;
         }
     }

    //TODO: consider abstracting some of this out to their own class files

    public enum AppliesTo {
        justAttributes, justElements, bothElementsAndAttributes
    }

    static public AdvancedJena getAdvancedBuilderJena() {
        return new AdvancedJena();
    }

    static public AdvancedSesame getAdvancedBuilderSesame() {
        return new AdvancedSesame();
    }

    static public AdvancedStream getAdvancedBuilderStream() {
        return new AdvancedStream();
    }

    static public Fast getFastBuilder() {
        return new Fast();
    }

    static class Default<T extends Default<T>> {

        String overrideNamespace = null;
        Map<String, String> renameElementMap = null;
        boolean autoDetectLiteralProperties = true;
        HashMapNoOverwriteWithDefaultTwoLevels<String, String, StringTransform> transformForAttributeValueMap = null;
        Map<String, StringTransformTwoValue> renameElementFunctionMap = null;
        ReverseElementTree renameElementPathMap = null;


        /**
         * @param namespace Override all namespaces with this namespace
         * @return
         * @description Override all namespaces in the XML with a new namespace.
         * @xml <people xmlns="http://example.org/" xmlns:a="http://A.com/">
         * <name a:test="hello">John Doe</name>
         * </people>
         * @exampleLabel Override all namespaces
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .overrideNamespace("http://otherNamespace.com/")
         * .build()
         * @exampleLabel Use namespaces provided in XML
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public T overrideNamespace(String namespace) {
            this.overrideNamespace = namespace;
            return (T) this;
        }

        /**
         * @param elementFrom The full URI of the element in the XML file
         * @param to          The new full URI
         * @return
         * @description Change the name of an element.
         * @xml <people xmlns="http://example.org/">
         * <name>John Doe</name>
         * </people>
         * @exampleLabel Rename "people" to "PEOPLE"
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .renameElement("http://example.org/people", "http://example.org/PEOPLE")
         * .build()
         */
        public T renameElement(String elementFrom, String to) {

            elementFrom = elementFrom.intern();
            to = to.intern();
            if (renameElementMap == null) {
                renameElementMap = new HashMapNoOverwrite<>();
            }
            renameElementMap.put(elementFrom, to);
            return (T) this;
        }

        /**
         * @param path The path, where the last element is the one to rename. Create a path with Builder.createPath("", "", ...)
         * @param to          The new full URI
         * @return
         * @description Change the name of an element at the end of a specific path. Useful for renaming elements that .
         * @xml <window xmlns="http://example.org/">
         *  <frame>
         *         <tittle>Main frame</tittle>
         *  </frame>
         * <frame>
         *     <frame>
         *         <tittle>Sub frame</tittle>
         *         </frame>
         * </frame>
         * </window>
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .renameElement(Builder.createPath("http://example.org/frame","http://example.org/frame"), "http://example.org/subFrame")
         * .build()
         */
        public T renameElement(XmlPath path, String to) {


            if (renameElementPathMap == null) {
                renameElementPathMap = new ReverseElementTree();
            }
            renameElementPathMap.insert(path, to);

            return (T) this;
        }





        /**
         * @param elementFrom The full URI of the element in the XML file
         * @param transform   a function that takes the namespace and element name as attributes and returns a new string.
         * @return
         * @description Change the name on the fly using a function. Eg. for capitalizing element names.
         * @xml <people xmlns="http://example.org/">
         * <name>John Doe</name>
         * </people>
         * @exampleLabel Capitalize all element names
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .renameElement(null, (namespace, name) ->  namespace + name.substring(0, 1).toUpperCase() + name.substring(1))
         * .build()
         */
        public T renameElement(String elementFrom, StringTransformTwoValue transform) {

            if (renameElementFunctionMap == null) {
                renameElementFunctionMap = new HashMapNoOverwriteWithDefault<>();
            }

            renameElementFunctionMap.put(elementFrom, transform);
            return (T) this;
        }


        /**
         * @param policy Either SimpleTypePolicy.compact or SimpleTypePolicy.expand
         * @return
         * @description XML elements with only text inside and no attributes (known as Simple Type elements)
         * can be compacted to use the element name as the RDF predicate or be expanded to use the xmlToRdf:hasChild
         * predicate
         * @xml <people xmlns="http://example.org/">
         * <name>John Doe</name>
         * </people>
         * @exampleLabel Compact
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .simpleTypePolicy(SimpleTypePolicy.compact)
         * .build()
         * @exampleLabel Expand
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .simpleTypePolicy(SimpleTypePolicy.expand)
         * .build()
         */
        public T simpleTypePolicy(SimpleTypePolicy policy) {
            if (policy.equals(SimpleTypePolicy.compact)) {
                autoDetectLiteralProperties = true;
            } else {
                autoDetectLiteralProperties = false;
            }

            return (T) this;
        }


        /**
         * @param elementName   The element name (full URI)
         * @param attributeName The attribute name (full URI)
         * @param transform     A function for transforming the value. Eg v -> v.toUpperCase()
         * @return
         * @description Run a function on the value of an attribute and use the returned string as the new value.
         * Take careful note of the namespaces. Unless specified, attributes inherit the namespace of their element.
         * @xml <person xmlns="http://example.org/" age="3" />
         * @exampleLabel Multiply age by 10
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .addTransformationForAttributeValue("http://example.org/person", "http://example.org/age", v -> String.valueOf(Integer.parseInt(v)*10))
         * .build()
         * @exampleLabel Without any transformation
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public T addTransformationForAttributeValue(String elementName, String attributeName, StringTransform transform) {

            if (transformForAttributeValueMap == null) {
                transformForAttributeValueMap = new HashMapNoOverwriteWithDefaultTwoLevels<>();
            }

            transformForAttributeValueMap.put(elementName, attributeName, transform);

            return (T) this;

        }


        String doTransformForAttribute(String element, String attribute, String value) {

            if (transformForAttributeValueMap != null) {
                StringTransform stringTransform = transformForAttributeValueMap.get(element, attribute);
                if (stringTransform != null) {
                    return stringTransform.transform(value);
                }

            }

            return value;

        }
    }

    static private class DefaultWithAddIndex<T extends DefaultWithAddIndex<T>> extends Default<T> {
        boolean addIndex;

        HashMapNoOverwriteWithDefaultTwoLevels<String, String, StringTransform> useAttributedForIdMap;
        String autoAddSuffixToNamespace = "#";

        /**
         * @param enabled true for enabled
         * @return
         * @description Add the index of the element as a predicate to the RDF. `xmlToRdf:index` is a
         * global element counter (depth-first) that keeps track of which absolute element this is. `xmlToRdf:elementIndex` is a
         * relative counter that keeps track of which index this element is for the given type relative to other elements
         * of that type in within the same parent.
         * @xml <people xmlns="http://example.org/">
         * <person>
         * <name>person-zero : element-one</name>
         * </person>
         * <person>
         * <name>person-one : element-three</name>
         * </person>
         * <ZEBRA>
         * <name>ZEBRA-zero  : element-five</name>
         * </ZEBRA>
         * <person>
         * <name>person-two  : element-seven</name>
         * </person>
         * </people>
         * @exampleLabel Add index
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .addIndex(true)
         * .build()
         * @exampleLabel No index
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .addIndex(false)
         * .build()
         */
        public T addIndex(boolean enabled) {
            addIndex = enabled;
            return (T) this;
        }

        /**
         * @param elementName     Full URI of element name
         * @param attributeName   Full URI og attribute name
         * @param stringTransform Function for transforming the string value
         * @return
         * @description Use an attribute on an element to generate an identifier for the RDF node.
         * Any single attribute can be used, and adding a namespace or a prefix to the ID is simple
         * as part of the transform.
         * @xml <archive xmlns="http://example.org/">
         * <record nr="0000001">
         * <title>Important record</title>
         * </record>
         * <record nr="0000002">
         * <title>Other record</title>
         * </record>
         * </archive>
         * @exampleLabel Use the record number (nr) as the node ID in the RDF.
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .useAttributeForId("http://example.org/record", "http://example.org/nr", v -> "http://acme.com/records/"+v)
         * .build()
         * @exampleLabel With default blank node
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public T useAttributeForId(String elementName, String attributeName, StringTransform stringTransform) {
            if (useAttributedForIdMap == null) {
                useAttributedForIdMap = new HashMapNoOverwriteWithDefaultTwoLevels<>();

            }
            useAttributedForIdMap.put(elementName, attributeName, stringTransform);

            return (T) this;
        }


        void getIdByUseAttributeForId(String type, String s, String value, Element element) {
            if (useAttributedForIdMap != null) {

                StringTransform stringTransform = useAttributedForIdMap.get(type, s);
                if (stringTransform != null) {
                    element.uri = stringTransform.transform(value);
                }
            }
        }

        /**
         * @param sign the sign (eg. "/") to suffix the namespace with
         * @return
         * @description Namespaces in RDF typically end in either `/` or `#` unlike in XML where a
         * namespace often has no specific suffix. By default a `#` is added to the namespace if
         * it doesn't already end in either `/` or `#`.
         * @xml <people xmlns="http://example.org">
         * <name>John Doe</name>
         * </people>
         * @exampleLabel `#` suffix
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .autoAddSuffixToNamespace("#")
         * .build()
         * @exampleLabel Unaltered XML namespace
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .autoAddSuffixToNamespace(false)
         * .build()
         */
        public T autoAddSuffixToNamespace(String sign) {
            autoAddSuffixToNamespace = sign;
            return (T) this;
        }

        public T autoAddSuffixToNamespace(boolean enabled) {
            if (!enabled) {
                autoAddSuffixToNamespace = null;
            }
            return (T) this;
        }

    }

    static public class Fast extends Default<Fast> {

        public XmlToRdfFast build() {
            return new XmlToRdfFast(this);
        }
    }

    static public class Advanced<ResourceType, DataType, T extends Advanced<ResourceType, DataType, T>> extends DefaultWithAddIndex<T> {
        boolean convertComplexElementsWithOnlyAttributesToPredicates;
        String baseNamespace;
        AppliesTo baseNamespaceAppliesTo;

        boolean autoAttributeNamespace = true;
        boolean convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate;
        boolean autoTypeLiterals;
        boolean uuidBasedIdInsteadOfBlankNodes;

        private Map<String, ParentChild> invertPredicate = null;
        private HashMapNoOverwriteWithDefaultTwoLevels<String, String, String> insertPredicateBetween = null;
        Map<String, DataType> dataTypeOnElement = null;
        Map<String, Map<String, ResourceType>> literalMap = null;
        private HashMapNoOverwriteWithDefaultTwoLevels<String, String, HashMapNoOverwrite<String, ResourceType>> elementAttributeTextToUriMap = null;

        boolean resolveAsQnameInAttributeValue;
        boolean xsiTypeSupport;

        private Map<String, ComplexClassTransform> complexElementTransformAtEndOfElement = null;
        private Map<String, ComplexClassTransform> complexElementTransformAtStartOfElement = null;
        Map<String, String> useElementAsPredicateMap = null;

        Map<String, String> skipElementMap = null;
        Map<String, String> forcedMixedContentMap = null;


        /**
         * @param elementName Full URI of element name
         * @param from        Original text inside element
         * @param to          New resource
         * @return
         * @description Map the text inside an element to a URI.
         * @xml <people xmlns="http://example.org/">
         * <name>John Doe</name>
         * <maritalStatus>married</maritalStatus>
         * </people>
         * @exampleLabel Map `married` to a URI
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .mapTextInElementToUri("http://example.org/maritalStatus", "married", "http://someReferenceData.org/married")
         * .build()
         * @exampleLabel No mapping
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public T mapTextInElementToUri(String elementName, String from, ResourceType to) {
            if (literalMap == null) {
                literalMap = new HashMapNoOverwriteWithDefault<>();
            }

            if (!literalMap.containsKey(elementName)) {
                literalMap.put(elementName, new HashMapNoOverwrite<String, ResourceType>());
            }

            literalMap.get(elementName).put(from, to);


            return (T) this;
        }

        public T mapTextInAttributeToUri(String elementName, String attributeName, String from, ResourceType to) {

                ;
            if (elementAttributeTextToUriMap == null) {
                elementAttributeTextToUriMap = new HashMapNoOverwriteWithDefaultTwoLevels<>();
            }

            if (!elementAttributeTextToUriMap.containsKey(elementName, attributeName)) {
                elementAttributeTextToUriMap.put(elementName, attributeName, new HashMapNoOverwrite<String, ResourceType>());
            }

            elementAttributeTextToUriMap.get(elementName, attributeName).put(from, to);

            return (T) this;
        }

        ResourceType getUriForTextInAttribute(String elementName, String attributeName, String text) {
            if (elementAttributeTextToUriMap == null) {
                return null;
            }

            HashMapNoOverwrite<String, ResourceType> innerMap = elementAttributeTextToUriMap.get(elementName, attributeName);

            return innerMap != null ? innerMap.get(text) : null;
        }


        /**
         * @param enabled true for enabled
         * @return
         * @description Use element name as predicate instead of the rdf:type on complex elements that only contain attributes.
         * @xml <people xmlns="http://example.org/">
         * <person name="John Doe" age="89"  />
         * </people>
         * @exampleLabel convertComplexElementsWithOnlyAttributesToPredicate enabled
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .convertComplexElementsWithOnlyAttributesToPredicate(true)
         * .build()
         * @exampleLabel convertComplexElementsWithOnlyAttributesToPredicate disabled
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .convertComplexElementsWithOnlyAttributesToPredicate(false)
         * .build()
         */
        public T convertComplexElementsWithOnlyAttributesToPredicate(boolean enabled) {
            convertComplexElementsWithOnlyAttributesToPredicates = enabled;
            return (T) this;
        }

        /**
         * @param enabled true for enabled *default: true*
         * @return
         * @description Uses the namespace for the element as the namespace for any attributes that lack namespaces. Default: true.
         * @xml <people xmlns="http://example.org/">
         * <name test="yay">John Doe</name>
         * </people>
         * @exampleLabel autoAttributeNamespace enabled
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .setBaseNamespace("http://none/", Builder.AppliesTo.bothElementsAndAttributes)
         * .autoAttributeNamespace(true)
         * .build()
         * @exampleLabel autoAttributeNamespace disabled
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .setBaseNamespace("http://none/", Builder.AppliesTo.bothElementsAndAttributes)
         * .autoAttributeNamespace(false)
         * .build()
         */
        public T autoAttributeNamespace(boolean enabled) {
            autoAttributeNamespace = enabled;
            return (T) this;
        }

        /**
         * @param namespace The namespace
         * @param which     Should the namespace apply to element, attributes or both
         * @return
         * @description Sets a namespace for elements and attributes that lack their own namespace. This is recommended to use
         * in order to make sure everything has a namespace in your final RDF.
         * @xml <people xmlns:other="http://other.org/">
         * <name other:age="1">John Doe</name>
         * <other:name age="2">Unknown</other:name>
         * </people>
         * @exampleLabel Use example.org with elements and attributes
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .autoAttributeNamespace(false)
         * .setBaseNamespace("http://example.org/", Builder.AppliesTo.bothElementsAndAttributes)
         * .build()
         * @exampleLabel Use empty namespace
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public T setBaseNamespace(String namespace, AppliesTo which) {
            baseNamespace = namespace;
            baseNamespaceAppliesTo = which;
            return (T) this;
        }

        /**
         * @param enabled true for enabled
         * @return
         * @description Use the element name as the predicate rather than the rdf:type of elements that are complex type, but
         * only contain simple type elements and/or attributes
         * @xml <people xmlns="http://example.org/">
         * <person name="John Doe" age="89" >
         * <maritalStatus>unknown</maritalStatus>
         * </person>
         * </people>
         * @exampleLabel Use `person` as the predicate
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(true)
         * .build()
         * @exampleLabel Use `person` as the rdf:type
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(false)
         * .build()
         */
        public T convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(boolean enabled) {
            convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate = enabled;
            return (T) this;
        }

        /**
         * @param enabled true for enabled
         * @return
         * @description Not implemented fully
         * @xml <people xmlns="http://example.org/">
         * <person idNumber="1234" married="true" weight="80.5">
         * <name>John Doe</name>
         * <age>99</age>
         * <dateOfBirth>1900-01-01</dateOfBirth>
         * <dateAndTimeOfBirth>1900-01-01T00:00:01+01:00</dateAndTimeOfBirth>
         * </person>
         * </people>
         * @exampleLabel Automatically detect literal types
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .autoTypeLiterals(true)
         * .build()
         * @exampleLabel Use untyped literals
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .autoTypeLiterals(false)
         * .build()
         */
        public T autoTypeLiterals(boolean enabled) {
            this.autoTypeLiterals = enabled;
            return (T) this;
        }


        /**
         * @param predicate The string value of the predicate to insert between a give parent and child.
         * @return
         * @description Uses the specified predicate between the parent and the child. Order of application:
         * - between("parent", "child")
         * - betweenSpecificParentAndAnyChild("parent")
         * - betweenAnyParentAndSpecificChild("child")
         * - betweenAny()
         * @xml <people xmlns="http://example.org/">
         * <person name="John Doe" age="89" >
         * <maritalStatus>unknown</maritalStatus>
         * </person>
         * </people>
         * @exampleLabel Insert hasPerson predicate
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .insertPredicate("http://example.org/hasPerson").between("http://example.org/people", "http://example.org/person")
         * .build()
         * @exampleLabel Use default hasChild perdicate
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public BetweenWithWildcard<T> insertPredicate(String predicate) {

            if (insertPredicateBetween == null) {
                insertPredicateBetween = new HashMapNoOverwriteWithDefaultTwoLevels<>();
            }

            Advanced<ResourceType, DataType, T> that = this;
            return new BetweenWithWildcard<T>() {
                @Override
                public T between(String parent, String child) {
                    if (parent == null || child == null) {
                        throw new IllegalArgumentException("parent or child can not be null, use betweenAny() or betweenAnyParentAndSpecificChild(child) or betweenSpecificParentAndAnyChild(parent)");
                    }

                    insertPredicateBetween.put(parent, child, predicate);
                    return (T) that;
                }

                @Override
                public T betweenAny() {
                    insertPredicateBetween.put(null, null, predicate);

                    return (T) that;
                }

                @Override
                public T betweenAnyParentAndSpecificChild(String child) {
                    if (child == null) {
                        throw new IllegalArgumentException("child can not be null, use betweenAny()");
                    }

                    insertPredicateBetween.put(null, child, predicate);

                    return (T) that;
                }

                @Override
                public T betweenSpecificParentAndAnyChild(String parent) {
                    if (parent == null) {
                        throw new IllegalArgumentException("parent can not be null, use betweenAny()");
                    }

                    insertPredicateBetween.put(parent, null, predicate);

                    return (T) that;
                }
            };

        }


        String getInsertPredicateBetweenOrDefaultPredicate(String parent, String child, String defaultPredicate) {
            if (insertPredicateBetween == null) {
                return defaultPredicate;
            }
            String s = insertPredicateBetween.get(parent, child);

            return s != null ? s : defaultPredicate;
        }


        /**
         * @param predicate The fully URI of the predicate to be inverted.
         * @return
         * @description Inverts an inserted predicate between two elements, so that the inherit parent -> child relationship is reversed.
         * Remember to insert a predicate before trying to invert it.
         * @xml <person xmlns="http://example.org/"  name="John Doe">
         * <dog name="Woof"  >
         * </dog>
         * </person>
         * @exampleLabel Insert and invert `ownedBy`
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .insertPredicate("http://example.org/ownedBy").between("http://example.org/person", "http://example.org/dog")
         * .invertPredicate("http://example.org/ownedBy").betweenAny()
         * .build()
         * @exampleLabel Just insert `ownedBy`
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .insertPredicate("http://example.org/ownedBy").between("http://example.org/person", "http://example.org/dog")
         * .build()
         */
        public BetweenWithWildcard<T> invertPredicate(String predicate) {

            if (invertPredicate == null) {
                invertPredicate = new HashMapNoOverwrite<>();
            }

            Advanced<ResourceType, DataType, T> that = this;

            return new BetweenWithWildcard<T>() {
                @Override
                public T between(String parent, String child) {
                    if (parent == null || child == null) {
                        throw new IllegalArgumentException("parent or child can not be null, use betweenAny() or betweenAnyParentAndSpecificChild(child) or betweenSpecificParentAndAnyChild(parent)");
                    }
                    invertPredicate.put(predicate, new ParentChild(parent, child));
                    return (T) that;
                }

                @Override
                public T betweenAny() {
                    invertPredicate.put(predicate, new ParentChild(null, null));
                    return (T) that;
                }

                @Override
                public T betweenAnyParentAndSpecificChild(String child) {
                    if (child == null) {
                        throw new IllegalArgumentException("child can not be null, use betweenAny()");
                    }
                    invertPredicate.put(predicate, new ParentChild(null, child));
                    return (T) that;
                }

                @Override
                public T betweenSpecificParentAndAnyChild(String parent) {
                    if (parent == null) {
                        throw new IllegalArgumentException("parent can not be null, use betweenAny()");
                    }
                    invertPredicate.put(predicate, new ParentChild(parent, null));
                    return (T) that;
                }
            };

        }

        /**
         * @param elementName The fully URI of the element.
         * @return
         * @description Skip and element and all contained elements. Includes the element named, and continues skipping until the closing tag is reached.
         * @xml <people xmlns="http://example.org/">
         * <person>
         * <name>John Doe</name>
         * </person>
         * </people>
         * @exampleLabel Skip `person` with subtree.
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .skipElement("http://example.org/person")
         * .build()
         * @exampleLabel Without skipping any elements
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public T skipElement(String elementName) {

            if (skipElementMap == null) {
                skipElementMap = new HashMap<>();
            }

            skipElementMap.put(elementName, elementName);

            return (T) this;
        }

        /**
         * @param elementName The fully URI of the element.
         * @return
         * @description Create a predicate between the parent and the children elements of an element instead of a node. The element name is used as the
         * predicate URI. Elements used as predicates should be complex elements without any attributes (the converter will skip any attributes). It is also
         * recommended to only use elements as predicates where the child elements are all complex.
         * @xml <people xmlns="http://example.org/">
         * <person>
         * <name>John Doe</name>
         * <friends>
         * <friend>
         * <name>Jane Doe</name>
         * </friend>
         * <friend>
         * <name>John Smith</name>
         * </friend>
         * <numberOfFriends>2</numberOfFriends>
         * </friends>
         * </person>
         * </people>
         * @exampleLabel Use `friends` as a predicate between `person` and `friend`
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .useElementAsPredicate("http://example.org/friends")
         * .build()
         * @exampleLabel `friends` becomes a blank node by default
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public T useElementAsPredicate(String elementName) {
            if (useElementAsPredicateMap == null) {
                useElementAsPredicateMap = new HashMap<>();
            }

            useElementAsPredicateMap.put(elementName, elementName);

            return (T) this;
        }

        /**
         * @param elementName The fully URI of the element.
         * @return
         * @description Force mixed content handling for elements, even when they do not
         * contain mixed content.
         * @xml <document xmlns="http://example.org/">
         * <paragraph><b>Hello</b> <b>World</b>!</paragraph>
         * <paragraph>Hello, World!</paragraph>
         * </document>
         * @exampleLabel Use forced mixed content on `paragraph`.
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .forceMixedContent("http://example.org/paragraph")
         * .build()
         * @exampleLabel With auto detection of mixed content.
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public T forceMixedContent(String elementName) {

            if (forcedMixedContentMap == null) {
                forcedMixedContentMap = new HashMap<>();
            }

            forcedMixedContentMap.put(elementName, elementName);

            return (T) this;
        }




        public interface BetweenWithWildcard<TT> {

            TT between(String parent, String child);

            TT betweenAny();

            TT betweenAnyParentAndSpecificChild(String child);

            TT betweenSpecificParentAndAnyChild(String parent);
        }

        boolean checkInvertPredicate(String predicate, String parent, String child) {
            if (invertPredicate == null) {
                return false;
            }

            ParentChild parentChild = invertPredicate.get(predicate);
            if (parentChild != null) {
                return parentChild.correctParentChild(parent, child);

            }
            return false;

        }

        /**
         * @param enabled true for enabled
         * @return
         * @description By default or elements are converted to blank nodes. Elements can alse be converted to regular RDF nodes with a UUID as the node ID.
         * Blank nodes are locally unique, while UUIDs are globally unique. UUIDs take time to generate, depending on your system, and will make the conversion
         * from XML to RDF considerably slower.
         * @xml <people xmlns="http://example.org/">
         * <name>John Doe</name>
         * </people>
         * @exampleLabel Use UUIDs
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .uuidBasedIdInsteadOfBlankNodes(true)
         * .build()
         * @exampleLabel Use locally unique blank node
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .uuidBasedIdInsteadOfBlankNodes(false)
         * .build()
         */
        public T uuidBasedIdInsteadOfBlankNodes(boolean enabled) {
            uuidBasedIdInsteadOfBlankNodes = enabled;
            return (T) this;
        }

        /**
         * @param element  Full URI of element
         * @param datatype Datatype to use. A string when using getAdvancedBuilderStream(), RDFDatatype for Jena and IRI for Sesame.
         * @return
         * @description Specify the datatype on a Simple Type element. Use a string with AdvancedBuilderStream as the datatype,
         * and the respective Sesame or Jena types with AdvancedBuilderSesame and AdvancedBuilderJena.
         * @xml <people xmlns="http://example.org/">
         * <name>John Doe</name>
         * <age>1</age>
         * </people>
         * @exampleLabel Make `age` an integer
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .setDatatype("http://example.org/age", "http://www.w3.org/2001/XMLSchema#integer")
         * .build()
         * @exampleLabel Leave untyped
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public T setDatatype(String element, DataType datatype) {
            if (dataTypeOnElement == null) {
                dataTypeOnElement = new HashMapNoOverwrite<>();
            }
            dataTypeOnElement.put(element, datatype);
            return (T) this;
        }

        class ParentChild {
            String parent;
            String child;

            public ParentChild(String parent, String child) {
                this.parent = parent;
                this.child = child;
            }

            public boolean correctParentChild(String parent, String child) {
                if (this.parent != null && !this.parent.equals(parent)) {
                    return false;
                }

                if (this.child != null && !this.child.equals(child)) {
                    return false;
                }
                return true;
            }


        }

        /**
         * @param element   Full URI of element
         * @param transform Function that can transform an Element
         * @return
         * @description Do any transformation on an element will full access to information about its attributes and children.
         * The transformation is applied when the convertor hits the end element tag.
         * @xml <person xmlns="http://example.org/" >
         * <person><name>John Doe</name></person>
         * <person><name>Other person</name></person>
         * </person>
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .addComplexElementTransformAtEndOfElement("http://example.org/name", element -> element.type = element.type.toUpperCase())
         * .addComplexElementTransformAtEndOfElement("http://example.org/person", element -> {
         * if(element.hasChild.size() > 1){
         * element.type = "http://example.org/people";
         * }
         * })
         * .build()
         * @exampleLabel No transforms
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public T addComplexElementTransformAtEndOfElement(String element, ComplexClassTransform transform) {

            if (complexElementTransformAtEndOfElement == null) {
                complexElementTransformAtEndOfElement = new HashMapNoOverwrite<>();
            }

            complexElementTransformAtEndOfElement.put(element, transform);

            return (T) this;
        }

        void doComplexTransformElementAtEndOfElement(Element element) {
            if (complexElementTransformAtEndOfElement == null) {
                return;
            }
            ComplexClassTransform complexClassTransform = complexElementTransformAtEndOfElement.get(element.type);
            if (complexClassTransform != null) {
                complexClassTransform.transform(element);
            }
        }


        /**
         * @param element   Full URI of element
         * @param transform Function that can transform an Element
         * @return
         * @description Do any transformation on an element will full access to information about its attributes but not about it's children.
         * The transformation is applied when the convertor finishes processing the attributes at the start of a tag.
         * <p>
         * Take careful note, as shown in the examples, that transforming an element at the start is simpler to reason about that at the
         * end when you are using options such as insertPredicate. In the seconds java example the transform is run at the end of the element,
         * after the insertPredicate() method has run.
         * <p>
         * ```xml
         * <people> <!-- start element transform runs now -->
         * <person> <!-- start element transform runs now -->
         * <name>John Doe</name>
         * </person> <!-- end element transform followed by insertPredicate runs now -->
         * </people> <!-- end element transform runs now -->
         * ```
         * @xml <people xmlns="http://example.org/">
         * <person>
         * <name>John Doe</name>
         * </person>
         * </people>
         * @exampleLabel All transforms run before insertPredicate
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .addComplexElementTransformAtStartOfElement("http://example.org/people", element -> element.type = element.type.toUpperCase())
         * .addComplexElementTransformAtStartOfElement("http://example.org/person", element -> element.type = element.type.toUpperCase())
         * .insertPredicate("http://example.org/hasPerson").between("HTTP://EXAMPLE.ORG/PEOPLE", "HTTP://EXAMPLE.ORG/PERSON")
         * .build()
         * @exampleLabel Transform on `<people>` runs after insertPredicate
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .addComplexElementTransformAtEndOfElement("http://example.org/people", element -> element.type = element.type.toUpperCase())
         * .addComplexElementTransformAtEndOfElement("http://example.org/person", element -> element.type = element.type.toUpperCase())
         * .insertPredicate("http://example.org/hasPerson").between("HTTP://EXAMPLE.ORG/PEOPLE", "HTTP://EXAMPLE.ORG/PERSON")
         * .build()
         */
        public T addComplexElementTransformAtStartOfElement(String element, ComplexClassTransform transform) {

            if (complexElementTransformAtStartOfElement == null) {
                complexElementTransformAtStartOfElement = new HashMapNoOverwrite<>();
            }

            complexElementTransformAtStartOfElement.put(element, transform);

            return (T) this;
        }

        void doComplexTransformElementAtStartOfElement(Element element) {
            if (complexElementTransformAtStartOfElement == null) {
                return;
            }
            ComplexClassTransform complexClassTransform = complexElementTransformAtStartOfElement.get(element.type);
            if (complexClassTransform != null) {
                complexClassTransform.transform(element);
            }
        }

        /**
         * @param enabled true for enabled
         * @return
         * @description Will resolve a qname inside an attribute by expanding it to a full URI as a string.
         * @xml <people xmlns="http://example.org/" xmlns:test="http://test.com/">
         * <name age="test:old">John Doe</name>
         * </people>
         * @exampleLabel Resolve all qnames
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .resolveAsQnameInAttributeValue(true)
         * .build()
         * @exampleLabel Do not resolve qnames
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .resolveAsQnameInAttributeValue(false)
         * .build()
         */
        public T resolveAsQnameInAttributeValue(boolean enabled) {
            resolveAsQnameInAttributeValue = enabled;
            return (T) this;
        }

        /**
         * @param enabled true for enabled
         * @return
         * @description Detects and uses the value in xsi:type attributes as the rdf:type.
         * @xml <animals xmlns="http://example.org/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:dbpedia="http://dbpedia.org/resource/">
         * <human xsi:type="man">
         * <name >John Doe</name>
         * </human>
         * <bird xsi:type="dbpedia:Barn_swallow">
         * <name>Big swallow</name>
         * </bird>
         * </animals>
         * @exampleLabel Detect and use xsi:type references
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .xsiTypeSupport(true)
         * .build()
         * @exampleLabel Ignore xsi:type references
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public T xsiTypeSupport(boolean enabled) {
            this.xsiTypeSupport = enabled;
            return (T) this;
        }

    }

    static public class AdvancedWithBuffer<ResourceType, Datatype, T extends AdvancedWithBuffer<ResourceType, Datatype, T>> extends Advanced<ResourceType, Datatype, T> {
        int buffer = 1000;

        /**
         * @param size size of buffer *default: 1000*
         * @return
         * @description Set the size of the buffer used to write RDF statements into a Jena Dataset or Sesame Repository.
         * Adjusting the buffer size may affect performance.
         */
        public T setBuffer(int size) {
            this.buffer = size;
            return (T) this;
        }

    }

    static public class AdvancedJena extends AdvancedWithBuffer<Node, RDFDatatype, AdvancedJena> {
        public XmlToRdfAdvancedJena build() {
            return new XmlToRdfAdvancedJena(this);
        }
    }

    static public class AdvancedSesame extends AdvancedWithBuffer<IRI, IRI, AdvancedSesame> {
        public XmlToRdfAdvancedSesame build() {
            return new XmlToRdfAdvancedSesame(this);
        }
    }

    static public class AdvancedStream extends Advanced<String, String, AdvancedStream> {
        public XmlToRdfAdvancedStream build() {
            return new XmlToRdfAdvancedStream(this);
        }
    }

    private static class HashMapNoOverwrite<Key, Value> extends HashMap<Key, Value> {

        @Override
        public Value put(Key key, Value value) {
            if (containsKey(key)) {
                throw new RuntimeException("Attempted to overwrite key: '" + key.toString() + "' with value: '" + value.toString() + "'");
            }
            return super.put(key, value);
        }
    }

    private static class HashMapNoOverwriteWithDefaultTwoLevels<Key1, Key2, Value> {

        HashMapNoOverwriteWithDefault<Key1, HashMapNoOverwriteWithDefault<Key2, Value>> internalMap = new HashMapNoOverwriteWithDefault<>();

        Value get(Key1 key1, Key2 key2) {
            HashMapNoOverwriteWithDefault<Key2, Value> firstLevel = internalMap.get(key1);

            if (firstLevel != null) {
                Value value = firstLevel.get(key2);
                if (value == null && internalMap.defaultValue != null) {
                    return internalMap.defaultValue.get(key2);
                }
                return value;
            }

            return null;
        }

        void put(Key1 key1, Key2 key2, Value value) {
            HashMapNoOverwriteWithDefault<Key2, Value> firstLevel = internalMap.getWithoutDefault(key1);
            if (firstLevel == null) {
                firstLevel = new HashMapNoOverwriteWithDefault<>();
                internalMap.put(key1, firstLevel);
            }

            firstLevel.put(key2, value);

        }

        boolean containsKey(Key1 key1, Key2 key2) {
            return get(key1, key2) != null;
        }

    }

    private static class HashMapNoOverwriteWithDefault<Key, Value> extends HashMapNoOverwrite<Key, Value> {

        Value defaultValue;

        @Override
        public boolean containsKey(Object key) {

            if (key == null) {
                return defaultValue != null;
            }
            return super.containsKey(key);
        }

        @Override
        public Value get(Object key) {
            if (key == null) {
                return defaultValue;
            }

            Value value = super.get(key);

            return value != null ? value : defaultValue;
        }

        @Override
        public Value put(Key key, Value value) {
            if (key == null) {
                if (defaultValue == null) {
                    defaultValue = value;
                } else {
                    throw new RuntimeException("Attempted to overwrite defaultValue with value: '" + value.toString() + "'");
                }

                return defaultValue;
            } else {
                return super.put(key, value);
            }

        }

        public Value getWithoutDefault(Key key1) {
            if (key1 == null) {
                return defaultValue;
            }
            return super.get(key1);
        }
    }

    private static String nullValueCheck(String value) {
        if (value == null) {
            value = "";
        }
        return value;
    }

}

