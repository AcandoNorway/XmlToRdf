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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

public class CompositeId<T extends UseHashmapForChildren> {

	private Set<String> requiredElement = new HashSet<>();
	private Set<String> requiredAttribute = new HashSet<>();
	private Map<String, String> resolvedElement = new HashMap<>();
	private Map<String, String> resolvedAttribute = new HashMap<>();
	Set<String> requiredElementFromParent = new HashSet<>();
	private Map<String, String> mapElementNameToOtherNameForParent = new HashMap<>();

	private BiFunction<Map<String, String>, Map<String, String>, String> mapFunction;

	private Map<String, CompositeId<T>> compositeIdMap;
	private String elementName;
	private T that;
	boolean elementIndex;
	boolean parentId;

	CompositeId(T that, String elementName, Map<String, CompositeId<T>> compositeIdMap) {
		this.that = that;
		this.elementName = elementName;
		this.compositeIdMap = compositeIdMap;
	}

	private CompositeId(CompositeId<T> from) {
		requiredAttribute = from.requiredAttribute;
		requiredElement = from.requiredElement;
		that = from.that;
		elementName = from.elementName;
		compositeIdMap = from.compositeIdMap;
		mapFunction = from.mapFunction;
		elementIndex = from.elementIndex;
		parentId = from.parentId;

		requiredElementFromParent = from.requiredElementFromParent;
		mapElementNameToOtherNameForParent = from.mapElementNameToOtherNameForParent;


	}

	void resetMaps() {
		resolvedAttribute = new HashMap<>();
		resolvedElement = new HashMap<>();
	}


	public CompositeId<T> fromElement(String elementName) {
		requiredElement.add(elementName);
		return this;
	}

	public CompositeId<T> fromAttribute(String attributeName) {
		requiredAttribute.add(attributeName);
		return this;
	}

	public CompositeId<T> elementIndex() {
		elementIndex = true;
		requiredElement.add(XmlToRdfVocabulary.index);
		requiredElement.add(XmlToRdfVocabulary.elementIndex);

		return this;
	}

	public CompositeId<T> parentId() {
		parentId = true;
		requiredElement.add(XmlToRdfVocabulary.parentId);
		return this;
	}

	public T mappedTo(BiFunction<Map<String, String>, Map<String, String>, String> mapFunction) {
		this.mapFunction = mapFunction;

		compositeIdMap.put(elementName, this);

		return (T) that;
	}

	boolean completed() {
		return (resolvedElement.size() + resolvedAttribute.size()) ==
			(requiredElement.size() + requiredAttribute.size() + requiredElementFromParent.size());
	}

	void resolveElement(String elementName, String value) {
		if (requiredElement.contains(elementName)) {
			resolvedElement.put(elementName, value);
		}
	}

	void resolveFromParent(Element parent) {
		requiredElementFromParent.forEach(elementName -> {
			Element o = (Element) parent.hasChildMap.get(elementName);
			if (o != null) {
				String key = mapElementNameToOtherNameForParent.get(elementName);
				resolvedElement.put(key, o.getHasValue());

			}
		});
	}

	void resolveAttribute(String attributeName, String value) {
		if (requiredAttribute.contains(attributeName)) {
			resolvedAttribute.put(attributeName, value);
		}
	}

	void reolveElementIndex(String indexName, Long index) {

		resolvedElement.put(indexName, index.toString());
	}

	String resolveIdentifier() {
		return mapFunction.apply(resolvedElement, resolvedAttribute);
	}

	CompositeId<T> simpleClone() {
		return new CompositeId<>(this);
	}


	public ParentId<T> fromParent(String elementName) {

		this.that.useHashmapForChildren();

		CompositeId<T> that = this;
		return newName -> {
			requiredElementFromParent.add(elementName);
			mapElementNameToOtherNameForParent.put(elementName, newName);
			return that;
		};

	}

	public interface ParentId<T extends UseHashmapForChildren> {
		CompositeId<T> as(String newName);
	}

}
