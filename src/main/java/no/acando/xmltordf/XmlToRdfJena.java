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

import org.apache.jena.rdf.model.Model;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;


public class XmlToRdfJena {

	Builder.Jena builder;

	public XmlToRdfJena(Builder.Jena builder) {
		this.builder = builder;
	}


	public Model convertToJenaModel(File file) throws IOException, ParserConfigurationException, SAXException {
		return convertToJenaModel(new BufferedInputStream(new FileInputStream(file), 1000000));
	}

	public Model convertToJenaModel(InputStream xmlInput) throws IOException, ParserConfigurationException, SAXException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		if (builder.autoAddNamespaceDeclarations) {
			try {
				factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXNotRecognizedException e) {
				e.printStackTrace();
			} catch (SAXNotSupportedException e) {
				e.printStackTrace();
			}
		}


		SAXParser saxParser = factory.newSAXParser();

		JenaSaxHandler handler = new JenaSaxHandler(builder);

		saxParser.parse(xmlInput, handler);

		xmlInput.close();

		return handler.m;


	}

	public PostProcessingJena convertForPostProcessing(File file) throws IOException, ParserConfigurationException, SAXException {
		return convertForPostProcessing(new BufferedInputStream(new FileInputStream(file)));
	}

	public PostProcessingJena convertForPostProcessing(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
		Model model = convertToJenaModel(inputStream);
		inputStream.close();
		return new PostProcessingJena(model);
	}


}
