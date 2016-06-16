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

import com.sun.org.apache.regexp.internal.RE;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.openrdf.model.IRI;

import java.util.HashMap;
import java.util.Map;

import static no.acando.xmltordf.Common.seperator;

public class Builder {

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

        String overrideNamespace;
        Map<String, String> mapForClasses;
        boolean autoDetectLiteralProperties = true;
        Map<String, StringTransform> transformForAttributeValueMap = null;

        /**
         * @param
         * @return
         * @description Override all namespaces in the xml with a new namespace
         * @description
         * @xml <people xmlns="http://example.org/" xmlns:a="http://A.com/">
         * <name a:test="hello">John Doe</name>
         * </people>
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .overrideNamespace("http://otherNamespace.com/")
         * .build()
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public T overrideNamespace(String namespace) {
            this.overrideNamespace = namespace;
            return (T) this;
        }

        /**
         * @param
         * @return
         * @description Change the name of an element.
         * @xml <people xmlns="http://example.org/">
         * <name>John Doe</name>
         * </people>
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .renameElement("http://example.org/people", "http://example.org/PEOPLE")
         * .build()
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public T renameElement(String fullUriFrom, String fullUriTo) {

            if (mapForClasses == null) {
                mapForClasses = new HashMapNoOverwrite<>();
            }
            mapForClasses.put(fullUriFrom, fullUriTo);
            return (T) this;
        }


        /**
         * @param policy
         * @return
         * @description XML elements with only text inside and no attributes (known as Simple Type elements)
         * can be compacted to use the element name as the RDF predicate or be expanded to use the xmlToRdf:hasChild
         * predicate
         * @xml <people xmlns="http://example.org/">
         * <name>John Doe</name>
         * </people>
         * @exampleLabel Auto detect literal properties enabled
         * @exampleCommand Builder.getAdvancedBuilderStream().simpleTypePolicy(SimpleTypePolicy.compact).build()
         * @exampleLabel Auto detect literal properties disabled
         * @exampleCommand Builder.getAdvancedBuilderStream().simpleTypePolicy(SimpleTypePolicy.expand).build()
         */
        public T simpleTypePolicy(SimpleTypePolicy policy) {
            if(policy.equals(SimpleTypePolicy.compact)){
                autoDetectLiteralProperties = true;
            }else{
                autoDetectLiteralProperties = false;
            }

            return (T) this;
        }


        /**
         * @param
         * @return
         * @description
         * @xml <people xmlns="http://example.org/">
         * <name>John Doe</name>
         * </people>
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public T addTransformForAttributeValue(String elementName, String attributeName, StringTransform transform) {
            elementName = nullValueCheck(elementName);
            attributeName = nullValueCheck(attributeName);

            if (transformForAttributeValueMap == null) {
                transformForAttributeValueMap = new HashMapNoOverwrite<>();
            }

            transformForAttributeValueMap.put(elementName + seperator + attributeName, transform);

            return (T) this;

        }

        /**
         * @param
         * @return
         * @description
         * @xml <people xmlns="http://example.org/">
         * <name>John Doe</name>
         * </people>
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        String doTransformForAttribute(String element, String attribute, String value) {

            if (transformForAttributeValueMap == null) {
                return value;
            }


            if (transformForAttributeValueMap.containsKey(element + seperator + attribute)) {
                return transformForAttributeValueMap.get(element + seperator + attribute).transform(value);
            } else if (transformForAttributeValueMap.containsKey(element + seperator)) {
                return transformForAttributeValueMap.get(element + seperator).transform(value);
            } else if (transformForAttributeValueMap.containsKey(seperator + attribute)) {
                return transformForAttributeValueMap.get(seperator + attribute).transform(value);
            } else if (transformForAttributeValueMap.containsKey(seperator)) {
                return transformForAttributeValueMap.get(seperator).transform(value);
            }

            return value;

        }
    }

    static private class DefaultWithAddIndex<T extends DefaultWithAddIndex<T>> extends Default<T> {
        boolean addIndex;

        Map<String, StringTransform> useAttributedForIdMap = new HashMapNoOverwrite<>();
        String autoAddSuffixToNamespace = "#";

        /**
         * @param
         * @return
         * @description
         * @xml <people xmlns="http://example.org/">
         * <person>
         *       <name>element-zero</name>
         * </person>
         *  <person>
         *       <name>element-one</name>
         * </person>
         *  <person>
         *       <name>element-two</name>
         * </person>
         * </people>
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .addIndex(true)
         * .build()
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .addIndex(false)
         * .build()
         */
        public T addIndex(boolean enabled) {
            addIndex = enabled;
            return (T) this;
        }

        /**
         * @param
         * @return
         * @description
         * @xml <people xmlns="http://example.org/">
         * <name>John Doe</name>
         * </people>
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public T addUseAttributeForId(String elementName, String attributeName, StringTransform p2) {
            elementName = nullValueCheck(elementName);
            useAttributedForIdMap.put(elementName + seperator + attributeName, p2);

            return (T) this;
        }

        /**
         * @param
         * @return
         * @description
         * @xml <people xmlns="http://example.org/">
         * <name>John Doe</name>
         * </people>
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public T autoAddSuffixToNamespace(String sign) {
            autoAddSuffixToNamespace = sign;
            return (T) this;
        }

        /**
         * @param
         * @return
         * @description
         * @xml <people xmlns="http://example.org/">
         * <name>John Doe</name>
         * </people>
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
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
        boolean autoConvertShallowChildrenToProperties;
        String baseNamespace;
        AppliesTo baseNamespaceAppliesTo;

        boolean autoAttributeNamespace = true;
        boolean autoConvertShallowChildrenWithAutoDetectLiteralProperties;
        boolean autoTypeLiterals;
        boolean uuidBasedIdInsteadOfBlankNodes = false;

        private Map<String, ParentChild> invertPredicate = null;
        private Map<String, String> insertPredicateBetween = null;
        Map<String, DataType> dataTypeOnElement = null;
        Map<String, Map<String, ResourceType>> literalMap = null;
        boolean resolveAsQnameInAttributeValue;
        boolean xsiTypeSupport;

        /**
         * @param
         * @return
         * @description
         * @xml <people xmlns="http://example.org/">
         * <name>John Doe</name>
         * <maritalStatus>married</maritalStatus>
         * </people>
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .mapTextInElementToUri("http://example.org/maritalStatus", "married", "http://someReferenceData.org/married")
         * .build()
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public T mapTextInElementToUri(String elementName, String from, ResourceType to) {
            if (literalMap == null) {
                literalMap = new HashMapNoOverwrite<>();
            }

            if (!literalMap.containsKey(elementName)) {
                literalMap.put(elementName, new HashMapNoOverwrite<String, ResourceType>());
            }

            literalMap.get(elementName).put(from, to);


            return (T) this;
        }


        /**
         * @param b
         * @return abc
         * @description Converts elements that only have attributes to a predicate with the element name and a node with the attributes.
         * @description
         * @xml <people xmlns="http://example.org/">
         * <person name="John Doe" age="89"  />
         * </people>
         * @exampleLabel autoConvertShallowChildrenToProperties enabled
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .autoConvertShallowChildrenToProperties(true)
         * .build()
         * @exampleLabel autoConvertShallowChildrenToProperties disabled
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .autoConvertShallowChildrenToProperties(false)
         * .build()
         */
        public T autoConvertShallowChildrenToProperties(boolean b) {
            autoConvertShallowChildrenToProperties = b;
            return (T) this;
        }

        /**
         * @param
         * @return
         * @description Uses the namespace for the element as the namespace for any attributes that lack namespaces. Default: true.
         * @xml <people xmlns="http://example.org/">
         * <name test="yay">John Doe</name>
         * </people>
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .setBaseNamespace("http://none/", Builder.AppliesTo.bothElementsAndAttributes)
         * .autoAttributeNamespace(true)
         * .build()
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .setBaseNamespace("http://none/", Builder.AppliesTo.bothElementsAndAttributes)
         * .autoAttributeNamespace(false)
         * .build()
         */
        public T autoAttributeNamespace(boolean b) {
            autoAttributeNamespace = b;
            return (T) this;
        }

        /**
         * @param
         * @return
         * @description Sets a namespace for elements and attributes that lack their own namespace. This is recommended to use
         * in order to make sure everything has a namespace in your final RDF.
         * @xml <people xmlns:other="http://other.org/">
         * <name other:age="1">John Doe</name>
         * <other:name age="2">Unknown</other:name>
         * </people>
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .autoAttributeNamespace(false)
         * .setBaseNamespace("http://example.org/", Builder.AppliesTo.bothElementsAndAttributes)
         * .build()
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public T setBaseNamespace(String namespace, AppliesTo which) {
            baseNamespace = namespace;
            baseNamespaceAppliesTo = which;
            return (T) this;
        }

        /**
         * @param b
         * @return abc
         * @description Converts elements ?
         * @xml
	   * <people xmlns="http://example.org/">
	   * 		<person name="John Doe" age="89" >
         * 		      <maritalStatus>unknown</maritalStatus>
	   *       </person>
	   * </people>
	   * @exampleLabel autoConvertShallowChildrenWithAutoDetectLiteralProperties enabled
         * @exampleCommand Builder.getAdvancedBuilderStream().autoConvertShallowChildrenWithAutoDetectLiteralProperties(true).build()
         * @exampleLabel autoConvertShallowChildrenWithAutoDetectLiteralProperties disabled
         * @exampleCommand Builder.getAdvancedBuilderStream().autoConvertShallowChildrenWithAutoDetectLiteralProperties(false).build()
         */
        public T autoConvertShallowChildrenWithAutoDetectLiteralProperties(boolean b) {
            autoConvertShallowChildrenWithAutoDetectLiteralProperties = b;
            return (T) this;
        }

        /**
         * @param
         * @return
         * @description
         * @xml <people xmlns="http://example.org/">
         * <name>John Doe</name>
         * </people>
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public T autoTypeLiterals(boolean autoTypeLiterals) {
            this.autoTypeLiterals = autoTypeLiterals;
            return (T) this;
        }


        /**
         * @param predicate
         * @return abc
         * @description Uses the specified predicate between the parent and the child
         * @xml
         * <people xmlns="http://example.org/">
         * 		<person name="John Doe" age="89" >
         * 		      <maritalStatus>unknown</maritalStatus>
         *       </person>
         * </people>
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .insertPredicate("http://example.org/hasPerson").between("http://example.org/people", "http://example.org/person")
         * .build()
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public Between<T> insertPredicate(String predicate) {
            Advanced<ResourceType, DataType, T> that = this;
            return new Between<T>() {
                @Override
                public T between(String parent, String child) {
                    if (insertPredicateBetween == null) {
                        insertPredicateBetween = new HashMapNoOverwrite<>();
                    }
                    insertPredicateBetween.put(parent + seperator + child, predicate);
                    return (T) that;
                }
            };

        }

        public interface Between<TT>{
            TT between(String parent, String child);
        }

        String getInsertPredicateBetween(String parent, String child) {
            if (insertPredicateBetween == null) {
                insertPredicateBetween = new HashMapNoOverwrite<>();
            }
            return insertPredicateBetween.get(parent + seperator + child);
        }


        /**
         * @param predicate
         * @return abc
         * @description Inverts an inserted predicate between two elements, so that the inherit parent -> child relationship is reversed.
         * @xml
         * <person xmlns="http://example.org/"  name="John Doe">
         * 		<dog name="Woof"  >
         *       </dog>
         * </person>
         * @exampleLabel invertPredicate
         * @exampleCommand
         * Builder.getAdvancedBuilderStream()
         *      .insertPredicate("http://example.org/ownedBy").between("http://example.org/person", "http://example.org/dog")
         *      .invertPredicate("http://example.org/ownedBy").betweenAny()
         *  .build()
         * @exampleLabel invertPredicate
         * @exampleCommand
         * Builder.getAdvancedBuilderStream()
         *      .insertPredicate("http://example.org/ownedBy").between("http://example.org/person", "http://example.org/dog")
         *      .build()
         */
        public BetweenWithWildcard<T> invertPredicate(String predicate){

            if(invertPredicate == null) invertPredicate = new HashMapNoOverwrite<>();

            Advanced<ResourceType, DataType, T> that = this;

            return new BetweenWithWildcard<T>(){
                @Override
                public T between(String parent, String child) {
                    invertPredicate.put(predicate, new ParentChild(parent, child));
                    return (T) that;
                }

                @Override
                public T betweenAny() {
                    invertPredicate.put(predicate, new ParentChild(null, null));
                    return (T) that;
                }

                @Override
                public T fromAnyParentToChild(String child) {
                    invertPredicate.put(predicate, new ParentChild(null, child));
                    return (T) that;
                }

                @Override
                public T fromParentToAnyChild(String parent) {
                    invertPredicate.put(predicate, new ParentChild(parent, null));
                    return (T) that;
                }
            };

        }

        public interface BetweenWithWildcard<TT>{

            TT between(String parent, String child);
            TT betweenAny();
            TT fromAnyParentToChild(String child);
            TT fromParentToAnyChild(String parent);
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
         * @param
         * @return
         * @description By default or elements are converted to blank nodes. Elements can alse be converted to regular RDF nodes with a UUID as the node ID.
         * Blank nodes are locally unique, while UUIDs are globally unique. UUIDs take time to generate, depending on your system, and will make the conversion
         * from XML to RDF considerably slower.
         * @xml <people xmlns="http://example.org/">
         * <name>John Doe</name>
         * </people>
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .uuidBasedIdInsteadOfBlankNodes(true)
         * .build()
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .uuidBasedIdInsteadOfBlankNodes(false)
         * .build()
         */
        public T uuidBasedIdInsteadOfBlankNodes(boolean enabled) {
            uuidBasedIdInsteadOfBlankNodes = enabled;
            return (T) this;
        }

        /**
         * @param
         * @return
         * @description Specify the datatype on a Simple Type element. Use a string with AdvancedBuilderStream as the datatype,
         * and the respective Sesame or Jena types with AdvancedBuilderSesame and AdvancedBuilderJena.
         * @xml <people xmlns="http://example.org/">
         * <name>John Doe</name>
         * <age>1</age>
         * </people>
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .setDatatype("http://example.org/age", "http://www.w3.org/2001/XMLSchema#integer")
         * .build()
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public T setDatatype(String fullUriForElement, DataType datatype) {
            if (dataTypeOnElement == null) {
                dataTypeOnElement = new HashMapNoOverwrite<>();
            }
            dataTypeOnElement.put(fullUriForElement, datatype);
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

        private Map<String, ComplexClassTransform> complexTransformForClassAtElementEnd = new HashMapNoOverwrite<>();

        /**
         * @param
         * @return
         * @description
         * @xml <people xmlns="http://example.org/">
         * <name>John Doe</name>
         * </people>
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         */
        public T addComplexTransformForClassAtElementEnd(String className, ComplexClassTransform transform) {

            complexTransformForClassAtElementEnd.put(className, transform);

            return (T) this;
        }

        void doComplexTransformForClassAtElementEnd(Element element) {
            ComplexClassTransform complexClassTransform = complexTransformForClassAtElementEnd.get(element.type);
            if (complexClassTransform != null) {
                complexClassTransform.transform(element);
            }
        }

        /**
         * @param
         * @return
         * @description Will resolve a qname inside an attribute by expanding it to a full URI as a string.
         * @xml <people xmlns="http://example.org/" xmlns:test="http://test.com/">
         * <name age="test:old">John Doe</name>
         * </people>
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .resolveAsQnameInAttributeValue(true)
         * .build()
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .resolveAsQnameInAttributeValue(false)
         * .build()
         */
        public T resolveAsQnameInAttributeValue(boolean enabled) {
            resolveAsQnameInAttributeValue = enabled;
            return (T) this;
        }

        /**
         * @param
         * @return
         * @description
         * @xml <people xmlns="http://example.org/">
         * <name>John Doe</name>
         * </people>
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         * @exampleLabel
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
         * @param
         * @return
         * @description
         * @xml <people xmlns="http://example.org/">
         * <name>John Doe</name>
         * </people>
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
         * @exampleLabel
         * @exampleCommand Builder.getAdvancedBuilderStream()
         * .build()
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

    private static String nullValueCheck(String value) {
        if (value == null) {
            value = "";
        }
        return value;
    }

}

