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
        boolean transformForAttributeValue = false;
        Map<String, StringTransform> transformForAttributeValueMap = new HashMapNoOverwrite<>();

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

        public T autoDetectLiteralProperties(boolean b) {
            autoDetectLiteralProperties = b;

            return (T) this;
        }

        public T addTransformForAttributeValue(String elementName, String attributeName, StringTransform transform) {
            elementName = nullValueCheck(elementName);
            attributeName = nullValueCheck(attributeName);

            transformForAttributeValueMap.put(elementName + seperator + attributeName, transform);
            transformForAttributeValue = true;

            return (T) this;

        }

    }

    static private class DefaultWithAddIndex<T extends DefaultWithAddIndex<T>> extends Default<T> {
        boolean addIndex = false;

        Map<String, StringTransform> useAttributedForIdMap = new HashMapNoOverwrite<>();
        boolean useAttributedForId = false;
        String autoAddSuffixToNamespace = "#";

        public T addIndex(boolean b) {

            addIndex = true;
            return (T) this;
        }

        public T addUseAttributeForId(String elementName, String attributeName, StringTransform p2) {
            elementName = nullValueCheck(elementName);
            useAttributedForIdMap.put(elementName + seperator + attributeName, p2);
            useAttributedForId = true;

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

        private Map<String, ParentChild> invertProperty = new HashMapNoOverwrite<>();
        private Map<String, String> insertPropertyBetween = new HashMapNoOverwrite<>();
        Map<String, DataType> dataTypeOnElement = new HashMapNoOverwrite<>();
        Map<String, Map<String, ResourceType>> literalMap = null;

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


        public T autoConvertShallowChildrenWithAutoDetectLiteralProperties(boolean b) {
            autoConvertShallowChildrenWithAutoDetectLiteralProperties = b;
            return (T) this;
        }

        public T autoTypeLiterals(boolean autoTypeLiterals) {
            this.autoTypeLiterals = autoTypeLiterals;
            return (T) this;
        }


        public T insertPropertyBetween(String newProperty, String parent, String child) {
            insertPropertyBetween.put(parent + seperator + child, newProperty);
            return (T) this;
        }

        String getInsertPropertyBetween(String parent, String child) {
            return insertPropertyBetween.get(parent + seperator + child);
        }

        public T invertProperty(String property, String parent, String child) {
            invertProperty.put(property, new ParentChild(parent, child));
            return (T) this;
        }

        boolean checkInvertProperty(String property, String parent, String child) {

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

        private Map<String, ComplexClassTransform> complexTransformForClass = new HashMapNoOverwrite<>();

        public T addComplexTransformForClass(String className, ComplexClassTransform transform) {

            complexTransformForClass.put(className, transform);

            return (T) this;
        }

        void doComplexTransformForClass(Element element) {
            ComplexClassTransform complexClassTransform = complexTransformForClass.get(element.type);
            if (complexClassTransform != null) {
                complexClassTransform.transform(element);
            }
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

    static class HashMapNoOverwrite<Key, Value> extends HashMap<Key, Value> {

        @Override
        public Value put(Key key, Value value) {
            if (containsKey(key)) {
                throw new RuntimeException("Attempted to overwrite key: '" + key.toString() + "' with value: '" + value.toString() + "'");
            }
            return super.put(key, value);
        }
    }

    public static String nullValueCheck(String value) {
        if (value == null) {
            value = "";
        }
        return value;
    }

}

