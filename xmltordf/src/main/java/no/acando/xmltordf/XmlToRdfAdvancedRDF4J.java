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

import org.eclipse.rdf4j.repository.Repository;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;


public class XmlToRdfAdvancedRDF4J {

    Builder.AdvancedRDF4J builder;

    public XmlToRdfAdvancedRDF4J(Builder.AdvancedRDF4J builder) {
        this.builder = builder;
    }

    public Repository convertToRepository(InputStream in) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        Security.secureSaxParser(factory);

        SAXParser saxParser = factory.newSAXParser();

        AdvancedSaxHandlerRDF4J handler = new AdvancedSaxHandlerRDF4J(builder);

        saxParser.parse(in, handler);
        return handler.repository;
    }

    public PostProcessingRDF4J convertForPostProcessing(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        Repository repository = convertToRepository(inputStream);
        inputStream.close();
        return new PostProcessingRDF4J(repository);
    }
}
