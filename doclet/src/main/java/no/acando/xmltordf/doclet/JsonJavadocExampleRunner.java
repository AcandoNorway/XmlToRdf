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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.openhft.compiler.CompilerUtils;
import no.acando.xmltordf.Builder;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class JsonJavadocExampleRunner {

	private static String classString = "package mypackage;\n" +
			"import no.acando.xmltordf.doclet.ExampleInterface;\n" +
			"import org.xml.sax.SAXException;\n" +
			"import java.io.*;\n" +
			"import no.acando.xmltordf.Builder;\n" +
			"import javax.xml.parsers.ParserConfigurationException;\n" +
			"import java.io.ByteArrayInputStream;\n" +
			"import java.io.IOException;\n" +
			"import java.io.StringWriter;\n" +
			"import no.acando.xmltordf.SimpleTypePolicy;\n" +
			"\n" +
			"\n" +
			"public class TempCOUNTER implements ExampleInterface{\n" +
			"\t\n" +
			"\tpublic String toString(String xml) {\n" +
			" ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();\n" +
			"\t\t\ttry {\n" +
			"\t\t\t\tBUILDER.convertToStream(new ByteArrayInputStream(xml.getBytes()), byteArrayOutputStream);\n" +
			"\t\t\t\tString s = new String(byteArrayOutputStream.toByteArray());\n" +
			"\t\t\t\treturn s;\n" +
			"\t\t\t} catch (Exception e) {\n" +
			"\t\t\t\te.printStackTrace();\n" +
			"\t\t\t} return \"\";" +
			"\t}\n" +
			"}\n";

	public static void main(String[] args) throws ClassNotFoundException, IOException, IllegalAccessException, InstantiationException, ParserConfigurationException, SAXException {

		String javadoc = "xmltordf/documentation/javadoc";
		File file1 = new File(javadoc + ".json");
		String json = FileUtils.readFileToString(file1);
		Type listType = new TypeToken<ArrayList<Method>>() {
		}.getType();
		List<Method> list = new Gson().fromJson(json, listType);

		int counter = 0;

		PrintWriter printWriter = new PrintWriter(new File(javadoc + ".md"));


		for (Method method : list) {

			printWriter.println("## " + method.name);

			printWriter.println();
			printWriter.println(method.description);
			printWriter.println();


			for (Example example : method.examples) {




			printWriter.println("**XML example**\n```xml");
			printWriter.println(example.xml);
			printWriter.println("```\n");

			for (Example.InnerExample innerExample : example.innerExamples) {
				counter++;
				String builder = classString
						.replace("BUILDER", innerExample.exampleCommand.replace(";", ""))
						.replace("COUNTER", "" + counter);



				printWriter.println("### " + innerExample.exampleLabel);
				printWriter.println("**Java code**\n```java");
				printWriter.println(innerExample.exampleCommand);
				printWriter.println("```\n");


				try {
					Class aClass = CompilerUtils.CACHED_COMPILER.loadFromJava(Builder.getAdvancedBuilderStream().getClass().getClassLoader(), "mypackage.Temp" + counter, builder);

					String s = ((ExampleInterface) aClass.newInstance()).toString(example.xml);


					Model defaultModel = ModelFactory.createDefaultModel();

					defaultModel.read(new ByteArrayInputStream(s.getBytes()), "", "TTL");

					defaultModel.setNsPrefix("ex", "http://example.org/");
					defaultModel.setNsPrefix("xmlToRdf", "http://acandonorway.github.com/XmlToRdf/ontology.ttl#");
					StringWriter stringWriter = new StringWriter();
					defaultModel.write(stringWriter, "TTL");

					printWriter.println("**RDF output**\n```turtle");
					printWriter.println(stringWriter.toString());
					printWriter.println("```\n");

				}catch (Exception e){
					System.out.println(builder);
					throw e;
				}



			}
			}


		}
		printWriter.close();


	}

}
