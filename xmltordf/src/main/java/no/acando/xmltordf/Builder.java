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

        public T overrideNamespace(String namespace) {
            this.overrideNamespace = namespace;
            return (T) this;
        }

        public T addMapForClasses(Map<String, String> map) {

            mapForClasses = map;
            return (T) this;
        }

        public T addTransformForClass(String fullUriFrom, String fullUriTo) {

            if (mapForClasses == null) {
                mapForClasses = new HashMapNoOverwrite<>();
            }
            mapForClasses.put(fullUriFrom, fullUriTo);
            return (T) this;
        }


        /**
         * @param enable auto detection of literal properties default true
         * @return abc
         * @xml <people xmlns="http://example.org/">
         * <name>John Doe</name>
         * </people>
         * @exampleLabel Auto detect literal properties enabled
         * @exampleCommand Builder.getAdvancedBuilderStream().autoDetectLiteralProperties(true).build()
         * @exampleLabel Auto detect literal properties disabled
         * @exampleCommand Builder.getAdvancedBuilderStream().autoDetectLiteralProperties(false).build()
         */
        public T simpleTypePolicy(SimpleTypePolicy policy) {
            if(policy.equals(SimpleTypePolicy.compact)){
                autoDetectLiteralProperties = true;
            }else{
                autoDetectLiteralProperties = false;
            }

            return (T) this;
        }

        public enum SimpleTypePolicy{
            compact, expand
        }

        public T addTransformForAttributeValue(String elementName, String attributeName, StringTransform transform) {
            elementName = nullValueCheck(elementName);
            attributeName = nullValueCheck(attributeName);

            if (transformForAttributeValueMap == null) {
                transformForAttributeValueMap = new HashMapNoOverwrite<>();
            }

            transformForAttributeValueMap.put(elementName + seperator + attributeName, transform);

            return (T) this;

        }

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
        boolean addIndex = false;

        Map<String, StringTransform> useAttributedForIdMap = new HashMapNoOverwrite<>();
        String autoAddSuffixToNamespace = "#";

        public T addIndex(boolean b) {

            addIndex = true;
            return (T) this;
        }

        public T addUseAttributeForId(String elementName, String attributeName, StringTransform p2) {
            elementName = nullValueCheck(elementName);
            useAttributedForIdMap.put(elementName + seperator + attributeName, p2);

            return (T) this;
        }

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
        boolean autoConvertShallowChildrenToProperties;
        String baseNamespace;
        AppliesTo baseNamespaceAppliesTo;

        boolean autoAttributeNamespace = true;
        boolean autoConvertShallowChildrenWithAutoDetectLiteralProperties;
        boolean autoTypeLiterals;
        boolean uuidBasedIdInsteadOfBlankNodes = false;

        private Map<String, ParentChild> invertProperty = null;
        private Map<String, String> insertPropertyBetween = null;
        Map<String, DataType> dataTypeOnElement = null;
        Map<String, Map<String, ResourceType>> literalMap = null;
        boolean resolveAsQnameInAttributeValue;
        boolean xsiTypeSupport;

        public T mapLiteralOnProperty(String property, String value, ResourceType resource) {
            if (literalMap == null) {
                literalMap = new HashMapNoOverwrite<>();
            }

            if (!literalMap.containsKey(property)) {
                literalMap.put(property, new HashMapNoOverwrite<String, ResourceType>());
            }
            literalMap.get(property).put(value, resource);
            return (T) this;
        }

        /**
         * @param b
         * @return abc
         * @description Converts elements that only have attributes to a property with the element name and a node with the attributes.
         * @xml <people xmlns="http://example.org/">
         * <person name="John Doe" age="89"  />
         * </people>
         * @exampleLabel autoConvertShallowChildrenToProperties enabled
         * @exampleCommand Builder.getAdvancedBuilderStream().autoConvertShallowChildrenToProperties(true).build()
         * @exampleLabel autoConvertShallowChildrenToProperties disabled
         * @exampleCommand Builder.getAdvancedBuilderStream().autoConvertShallowChildrenToProperties(false).build()
         */
        public T autoConvertShallowChildrenToProperties(boolean b) {
            autoConvertShallowChildrenToProperties = b;
            return (T) this;
        }

        public T autoAttributeNamespace(boolean b) {
            autoAttributeNamespace = b;
            return (T) this;
        }

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

        public T autoTypeLiterals(boolean autoTypeLiterals) {
            this.autoTypeLiterals = autoTypeLiterals;
            return (T) this;
        }


        /**
         * @param newProperty
         * @param parent
         * @param child
         * @return abc
         * @description Converts elements ?
         * @xml
         * <people xmlns="http://example.org/">
         * 		<person name="John Doe" age="89" >
         * 		      <maritalStatus>unknown</maritalStatus>
         *       </person>
         * </people>
         * @exampleLabel insertPropertyBetween enabled
         * @exampleCommand Builder.getAdvancedBuilderStream().insertPropertyBetween("http://example.org/hasPerson", "http://example.org/people", "http://example.org/person").build()
         * @exampleLabel insertPropertyBetween disabled
         * @exampleCommand Builder.getAdvancedBuilderStream().build()
         */
        public T insertPropertyBetween(String newProperty, String parent, String child) {
            if (insertPropertyBetween == null) {
                insertPropertyBetween = new HashMapNoOverwrite<>();

            }
            insertPropertyBetween.put(parent + seperator + child, newProperty);
            return (T) this;
        }

        public Between<T> insertProperty(String newProperty) {
            Advanced<Datatype, T> that = this;
            return new Between<T>() {
                @Override
                public T between(String parent, String child) {
                    insertPropertyBetween.put(parent + seperator + child, newProperty);
                    return (T) that;
                }
            };

        }

        public interface Between<TT>{
            TT between(String parent, String child);
        }

        String getInsertPropertyBetween(String parent, String child) {
            if (insertPropertyBetween == null) {
                insertPropertyBetween = new HashMapNoOverwrite<>();
            }
            return insertPropertyBetween.get(parent + seperator + child);
        }

        /**
         * @param newProperty
         * @param parent
         * @param child
         * @return abc
         * @description Converts elements ?
         * @xml
         * <person xmlns="http://example.org/"  name="John Doe">
         * 		<dog name="Woof"  >
         *       </dog>
         * </person>
         * @exampleLabel invertProperty
         * @exampleCommand
         * Builder.getAdvancedBuilderStream()
         *      .insertPropertyBetween("http://example.org/ownedBy", "http://example.org/person", "http://example.org/dog")
         *      .invertProperty("http://example.org/ownedBy", null, null)
         *  .build()
         * @exampleLabel invertProperty
         * @exampleCommand
         * Builder.getAdvancedBuilderStream()
         *      .insertPropertyBetween("http://example.org/ownedBy", "http://example.org/person", "http://example.org/dog")
         *      .build()
         */
        public T invertPropertyBetween(String property, String parent, String child) {
            if (invertProperty == null) {
                invertProperty = new HashMapNoOverwrite<>();
            }
            invertProperty.put(property, new ParentChild(parent, child));
            return (T) this;
        }

        public BetweenWithWildcard<T> invertProperty(String property){

            Advanced<Datatype, T> that = this;

            return new BetweenWithWildcard<T>(){
                @Override
                public T between(String parent, String child) {
                    invertProperty.put(property, new ParentChild(parent, child));
                    return (T) that;
                }

                @Override
                public T betweenAny() {
                    invertProperty.put(property, new ParentChild(null, null));
                    return (T) that;
                }

                @Override
                public T fromAnyParentToChild(String child) {
                    invertProperty.put(property, new ParentChild(null, child));
                    return (T) that;
                }

                @Override
                public T fromParentToAnyChild(String parent) {
                    invertProperty.put(property, new ParentChild(parent, null));
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

        boolean checkInvertProperty(String property, String parent, String child) {
            if (invertProperty == null) {
                return false;
            }

            ParentChild parentChild = invertProperty.get(property);
            if (parentChild != null) {
                return parentChild.correctParentChild(parent, child);

            }
            return false;

        }

        public T uuidBasedIdInsteadOfBlankNodes(boolean b) {
            uuidBasedIdInsteadOfBlankNodes = b;
            return (T) this;
        }

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

        public T resolveAsQnameInAttributeValue(boolean enabled) {
            resolveAsQnameInAttributeValue = enabled;
            return (T) this;
        }

        public T xsiTypeSupport(boolean enabled) {
            this.xsiTypeSupport = enabled;
            return (T) this;
        }

    }

    static public class AdvancedWithBuffer<ResourceType, Datatype, T extends AdvancedWithBuffer<ResourceType, Datatype, T>> extends Advanced<ResourceType, Datatype, T> {
        int buffer = 1000;

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

