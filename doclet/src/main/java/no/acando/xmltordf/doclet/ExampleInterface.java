package no.acando.xmltordf.doclet;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by havardottestad on 07/06/16.
 */
interface  ExampleInterface {

	String toString(String xml) throws IOException, SAXException, ParserConfigurationException;

}
