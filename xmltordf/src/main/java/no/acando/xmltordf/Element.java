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

import java.util.ArrayList;
import java.util.List;


public class Element {
    public String type;
    public String uri;
    public Element parent;
    public StringBuilder hasValue;
    public List<Element> hasChild = new ArrayList<>(10);
    public List<Property> properties = new ArrayList<>(3);
    long index = 0;
    long elementIndex = 0;
    boolean shallow;
    boolean autoDetectedAsLiteralProperty;
    CountingMap indexMap = new CountingMap();


    public List<Object> mixedContent = new ArrayList<>();
    public StringBuilder tempMixedContentString = new StringBuilder("");
    public boolean useElementAsPredicate;
    public boolean containsMixedContent;


    public void appendValue(char[] ch, int start, int length) {
        if (hasValue == null) {
            hasValue = new StringBuilder(new String(ch, start, length));
        } else {
            hasValue.append(ch, start, length);
        }
        tempMixedContentString.append(ch, start, length);
        hasValueString = null;
    }


    public String getType() {
        return type;
    }

    public String getUri() {
        return uri;
    }

    public Element getParent() {
        return parent;
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

    public List<Element> getHasChild() {
        return hasChild;
    }

    public List<Property> getProperties() {
        return properties;
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


    public void addMixedContent(Element element) {
        containsMixedContent = true;
        String temp = tempMixedContentString.toString();
        if (!temp.isEmpty()) {
            mixedContent.add(temp);
        }
        tempMixedContentString = new StringBuilder("");
        mixedContent.add(element);
    }

    public void endMixedContent() {
        if (containsMixedContent) {
            if (!tempMixedContentString.toString().isEmpty()) {
                mixedContent.add(tempMixedContentString.toString());
            }
        }
    }


}

