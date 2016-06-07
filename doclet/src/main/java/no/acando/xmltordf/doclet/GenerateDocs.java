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

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import net.openhft.compiler.CompilerUtils;
import org.xml.sax.SAXException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;



public class GenerateDocs {

	static ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");




	public static boolean start(RootDoc root) throws FileNotFoundException, ScriptException, ClassNotFoundException, IllegalAccessException, InstantiationException {

		CompilerUtils.addClassPath("/Users/havardottestad/Documents/Jobb/Acando/XmlToRdf2/doclet/target/classes/no/acando/xmltordf/doclet/");
		Class aClass = CompilerUtils.CACHED_COMPILER.loadFromJava("no.acando.xmltordf.doclet.Temp", classString);
		ExampleInterface runner = (ExampleInterface) aClass.newInstance();

//		String className = "mypackage.MyClass";
//		String javaCode = "package mypackage;\n" +
//				"public class MyClass implements Runnable {\n" +
//				"    public void run() {\n" +
//				"        System.out.println(\"Hello World\");\n" +
//				"    }\n" +
//				"}\n";
//		Class aClass = CompilerUtils.CACHED_COMPILER.loadFromJava(className, javaCode);
//		Runnable runner = (Runnable) aClass.newInstance();
//		runner.run();

		File file = new File("javadoc.md");
		PrintWriter printWriter = new PrintWriter(file);
		ClassDoc[] classes = root.classes();
		for (ClassDoc classDoc : classes) {
			System.out.println(classDoc.name());
		}
		ClassDoc builder = root.classNamed("no.acando.xmltordf.Builder");
		if(builder == null) return true;

		ClassDoc[] classDocs = builder.innerClasses(false);

		for (ClassDoc classDoc : classDocs) {
			printWriter.println("## "+classDoc.name());

			MethodDoc[] methods = classDoc.methods();
			for (MethodDoc method : methods) {
				printWriter.println("### "+method.name());
				Tag[] xmls = method.tags("xml");
				String xmlT= "";
				for (Tag xml : xmls) {
					printWriter.println("\nXML input");
					printWriter.println("```xml\n"+xml.text().trim()+"\n```\n");
					xmlT = xml.text().trim();
				}

				Tag[] tags = method.tags();
				for (Tag tag : tags) {
					if(tag.name().equals("@optionLabel")){
						printWriter.println("#### "+tag.text());
					}
					if(tag.name().equals("@optionCommand")){
						printWriter.println(tag.text());
						classCounter++;
						String tempClass = classString
								.replace("CLASSCOUNTER", "" + (classCounter))
								.replace("BUILDER HERE", tag.text());

						printWriter.write(tempClass);

						System.out.println(tempClass);
//						CompilerUtils.addClassPath("~/.m2/repository/no/acando/xmlToRdf/1.0/xmltordf-1.0.jar");
//						Class aClass = CompilerUtils.CACHED_COMPILER.loadFromJava("no.acando.xmltordf.doclet.Temp"+classCounter, tempClass);
//						ExampleInterface runner = (ExampleInterface) aClass.newInstance();
//						try {
//							printWriter.write(runner.toString(xmlT));
//						} catch (IOException e) {
//							e.printStackTrace();
//						} catch (SAXException e) {
//							e.printStackTrace();
//						} catch (ParserConfigurationException e) {
//							e.printStackTrace();
//						}
					}

				}

				printWriter.println();
			}

			printWriter.println();
		}




		printWriter.close();
		System.out.println("done");

		return true;
	}

static int classCounter = 0;

	static String classString = "package no.acando.xmltordf.doclet;\n" +
			"\n" +
			"import org.xml.sax.SAXException;\n" +
			"\n" +
			"import javax.xml.parsers.ParserConfigurationException;\n" +
			"import java.io.ByteArrayInputStream;\n" +
			"import java.io.IOException;\n" +
			"import java.io.StringWriter;\n" +
			"\n" +
			"\n" +
			"public class Temp implements ExampleInterface{\n" +
			"\t@Override\n" +
			"\tpublic String toString(String xml) {\n" +
		"return \"\";"+
			"\t}\n" +
			"}\n";
}


