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

import com.google.gson.GsonBuilder;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public class GenerateDocs {


    public static boolean start(RootDoc root) throws FileNotFoundException, ScriptException, ClassNotFoundException, IllegalAccessException, InstantiationException {


        File file = new File("javadoc.json");
        PrintWriter printWriter = new PrintWriter(file);

        ClassDoc builder = root.classNamed("no.acando.xmltordf.Builder");
        if (builder == null) {
            return true;
        }

        ClassDoc[] classDocs = builder.innerClasses(false);

        List<Method> documentation = new ArrayList<>();

        for (ClassDoc classDoc : classDocs) {

            MethodDoc[] methods = classDoc.methods();


            for (MethodDoc method : methods) {
                Example currentExample = null;
                Method currentMethod = new Method();
                Optional<String> reduce = Arrays.stream(method.parameters()).map(p -> p.typeName() + " " + p.name()).reduce((p1, p2) -> p1 + ", " + p2);
                String sig = "()";
                if (reduce.isPresent()) {
                    sig = "(" + reduce.get() + ")";
                }
                currentMethod.name = method.name() + sig;
                Tag[] tags = method.tags();
                for (Tag tag : tags) {

                    if (tag.name().equals("@description")) {
                        currentMethod.description = tag.text().trim();
                    }

                    if (tag.name().equals("@xml")) {
                        currentExample = new Example();
                        currentMethod.addExample(currentExample);
                        currentExample.xml = tag.text().trim();
                    }

                    if (tag.name().equals("@exampleLabel")) {
                        currentExample.addExampleLabel(tag.text().trim());
                    }
                    if (tag.name().equals("@exampleCommand")) {
                        currentExample.addExampleCommand(tag.text().trim());
                    }
                }
                if (!currentMethod.examples.isEmpty()) {
                    documentation.add(currentMethod);
                }

            }
        }


        printWriter.println(new GsonBuilder().setPrettyPrinting().create().toJson(documentation));
        printWriter.close();
        System.out.println("done");

        return true;
    }


}


