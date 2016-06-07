package no.acando.xmltordf.doclet;

import no.acando.xmltordf.Builder;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;


public class Temp implements ExampleInterface{
	@Override
	public String toString(String xml) throws IOException, SAXException, ParserConfigurationException {
		StringWriter stringWriter = new StringWriter();
		Builder.getAdvancedBuilderJena().build()
				.convertToDataset(new ByteArrayInputStream(xml.getBytes())).getDefaultModel().write(stringWriter, "TTL");
		return stringWriter.toString();
	}
}
