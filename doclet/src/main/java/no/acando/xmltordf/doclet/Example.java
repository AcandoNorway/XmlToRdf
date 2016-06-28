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

package no.acando.xmltordf.doclet;

import java.util.LinkedList;


class Example {
    String xml;

    LinkedList<InnerExample> innerExamples = new LinkedList<>();

    void addExampleCommand(String s) {
        InnerExample last = innerExamples.getLast();

        if (last == null) {
            throw new RuntimeException("addExampleCommand could not add '" + s + "' to an empty list.");
        }
        if (last.exampleCommand != null) {
            throw new RuntimeException("addExampleCommand tried to overwrite'" + last.exampleCommand + "' with '" + s + "'");

        }

        last.exampleCommand = s;
    }

    void addExampleLabel(String s) {
        InnerExample innerExample = new InnerExample();
        innerExample.exampleLabel = s;
        innerExamples.addLast(innerExample);
    }

    class InnerExample {
        String exampleLabel;
        String exampleCommand;

    }
}
