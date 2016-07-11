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

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class CompositeId<T> {

    private Map<String, String> requiredElement = new HashMap<>();
    private Map<String, String> requiredAttribute = new HashMap<>();
    private Map<String, String> resolvedElement = new HashMap<>();
    private Map<String, String> resolvedAttribute = new HashMap<>();

    private BiFunction<Map<String, String>, Map<String, String>, String> mapFunction;

    private Map<String, CompositeId<T>> compositeIdMap;
    private String elementName;
    private T that;

    CompositeId(T that, String elementName, Map<String, CompositeId<T>> compositeIdMap) {
        this.that = that;
        this.elementName = elementName;
        this.compositeIdMap = compositeIdMap;
    }

    CompositeId(CompositeId<T> from) {
        requiredAttribute = from.requiredAttribute;
        requiredElement = from.requiredElement;
        that = from.that;
        elementName = from.elementName;
        compositeIdMap = from.compositeIdMap;
        mapFunction = from.mapFunction;

    }

    void resetMaps() {
        resolvedAttribute = new HashMap<>();
        resolvedElement = new HashMap<>();
    }

    public CompositeId<T> fromElement(String elementName) {
        requiredElement.put(elementName, elementName);
        return this;
    }

    public CompositeId<T> fromAttribute(String attributeName) {
        requiredAttribute.put(attributeName, attributeName);
        return this;
    }

    public T mappedTo(BiFunction<Map<String, String>, Map<String, String>, String> mapFunction) {
        this.mapFunction = mapFunction;

        compositeIdMap.put(elementName, this);

        return (T) that;
    }

    boolean completed() {
        return ( resolvedElement.size() + resolvedAttribute.size() ) ==
            ( requiredElement.size() + requiredAttribute.size() );
    }

    void resolveElement(String elementName, String value) {
        if(requiredElement.containsKey(elementName)) {
            resolvedElement.put(elementName, value);
        }
    }

    void resolveAttribute(String attributeName, String value) {
        if(requiredAttribute.containsKey(attributeName)) {
            resolvedAttribute.put(attributeName, value);
        }
    }

    String resolveIdentifier() {
        return mapFunction.apply(resolvedElement, resolvedAttribute);
    }

    public CompositeId<T> simpleClone() {
        return new CompositeId<>(this);
    }
}
