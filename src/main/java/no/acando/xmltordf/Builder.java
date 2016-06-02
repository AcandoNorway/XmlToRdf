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

import org.openrdf.model.IRI;

import java.util.HashMap;
import java.util.Map;

import static no.acando.xmltordf.Common.seperator;


public class Builder {

    public enum AppliesTo {
        justAttributes, justElements, bothElementsAndAttributes
    }



    static public Fast getFastBuilder() {
        return new Fast();
    }

    static public Advanced getAdvancedBuilder() {
        return new Advanced();
    }



    static class Default<This extends Default<This>> {

         String overrideNamespace;
         Map<String, String> mapForClasses;
         boolean autoDetectLiteralProperties = true;
         boolean transformForAttributeValue = false;
        Map<String, StringTransform> transformForAttributeValueMap = new HashMapNoOverwrite<>();


        public This overrideNamespace(String ns) {
            this.overrideNamespace = ns;
            return (This) this;
        }

        public This addMapForClasses(Map<String, String> map) {

            mapForClasses = map;
            return (This) this;
        }

        public This addTransformForClass(String fullUriFrom, String fullUriTo) {

            if (mapForClasses == null) {
                mapForClasses = new HashMapNoOverwrite<>();
            }
            mapForClasses.put(fullUriFrom, fullUriTo);
            return (This) this;
        }


        public This autoDetectLiteralProperties(boolean b) {
            autoDetectLiteralProperties = b;

            return (This) this;
        }

        public This addTransformForAttributeValue(String elementName, String attributeName, StringTransform transform) {
            if (elementName == null) {
                elementName = "";
            }
            if (attributeName == null) {
                attributeName = "";
            }

            transformForAttributeValueMap.put(elementName + seperator + attributeName, transform);
            transformForAttributeValue = true;

            return (This) this;

        }


    }

    static private class DefaultWithAddIndex<This extends DefaultWithAddIndex<This>> extends Default<This> {
         boolean addIndex = false;

        Map<String, StringTransform> useAttributedForIdMap = new HashMapNoOverwrite<>();
        boolean useAttributedForId = false;
        String autoAddSuffixToNamespace = "#";

        public This addIndex(boolean b) {

            addIndex = true;
            return (This) this;
        }


        public This addUseAttributeForId(String elementName, String attributeName, StringTransform p2) {
            if (elementName == null) {
                elementName = "";
            }
            useAttributedForIdMap.put(elementName + seperator + attributeName, p2);
            useAttributedForId = true;

            return (This) this;
        }


        public This autoAddSuffixToNamespace(String sign) {
            autoAddSuffixToNamespace = sign;
            return (This) this;
        }



        public This autoAddSuffixToNamespace(boolean enabled) {
            if (!enabled) {
                autoAddSuffixToNamespace = null;
            }
            return (This) this;
        }


    }

    static public class Fast extends Default<Fast> {


        public XmlToRdfFast build() {
            return new XmlToRdfFast(this);
        }
    }

    static public class Advanced extends DefaultWithAddIndex<Advanced> {
         boolean autoConvertShallowChildrenToProperties;
         String baseNamespace;
         AppliesTo baseNamespaceAppliesTo;

         boolean autoAttributeNamespace = true;
         boolean autoConvertShallowChildrenWithAutoDetectLiteralProperties;
         boolean autoTypeLiterals;
         boolean uuidBasedIdInsteadOfBlankNodes = false;

        private Map<String, ParentChild> invertProperty = new HashMapNoOverwrite<>();
        private Map<String, String> insertPropertyBetween = new HashMapNoOverwrite<>();
         Map<String, IRI> datatypeOnElement = new HashMapNoOverwrite<>();


        public XmlToRdfAdvanced build() {
            return new XmlToRdfAdvanced(this);

        }



        public Advanced autoConvertShallowChildrenToProperties(boolean b) {
            autoConvertShallowChildrenToProperties = b;
            return this;
        }

        public Advanced autoAttributeNamespace(boolean b) {
            autoAttributeNamespace = b;
            return this;
        }

        public Advanced setBaseNamespace(String namespace, AppliesTo which) {

            baseNamespace = namespace;
            baseNamespaceAppliesTo = which;

            return this;
        }


        public Advanced autoConvertShallowChildrenWithAutoDetectLiteralProperties(boolean b) {
            autoConvertShallowChildrenWithAutoDetectLiteralProperties = b;
            return this;

        }

        public Advanced autoTypeLiterals(boolean autoTypeLiterals) {
            this.autoTypeLiterals = autoTypeLiterals;
            return this;
        }


        public Advanced insertPropertyBetween(String newProperty, String parent, String child) {
            insertPropertyBetween.put(parent + seperator + child, newProperty);
            return this;
        }

         String getInsertPropertyBetween(String parent, String child) {
            return insertPropertyBetween.get(parent + seperator + child);
        }


        public Advanced invertProperty(String property, String parent, String child) {

            invertProperty.put(property, new ParentChild(parent, child));

            return this;

        }

         boolean checkInvertProperty(String property, String parent, String child) {

            ParentChild parentChild = invertProperty.get(property);
            if (parentChild != null) {
                return parentChild.correctParentChild(parent, child);

            }
            return false;

        }

        public Advanced uuidBasedIdInsteadOfBlankNodes(boolean b) {
            uuidBasedIdInsteadOfBlankNodes = b;
            return this;
        }

        public Advanced setDatatype(String fullUriForElement, IRI datatype) {

            datatypeOnElement.put(fullUriForElement, datatype);

            return this;
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


        public Advanced addComplexTransformForClass(String className, ComplexClassTransform transform) {

            complexTransformForClass.put(className, transform);

            return this;
        }

         void doComplexTransformForClass(AdvancedSaxHandler.Element element){
            ComplexClassTransform complexClassTransform = complexTransformForClass.get(element.type);
            if(complexClassTransform != null){
                complexClassTransform.transform(element);
            }
        }
    }

    static class HashMapNoOverwrite<k,v> extends HashMap<k,v>{

        @Override
        public v put(k key, v value) {
            if(containsKey(key)){
                throw new RuntimeException("Attempted to overwrite key: '"+key.toString()+"' with value: '"+value.toString()+"'");
            }
            return super.put(key, value);
        }
    }


}

