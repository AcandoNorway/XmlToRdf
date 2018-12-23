package no.acando.xmltordf;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

public class Security {

	// https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#JAXP_DocumentBuilderFactory.2C_SAXParserFactory_and_DOM4J
	public static void secureSaxParser(SAXParserFactory factory) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
		String FEATURE = null;

		// This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are prevented
		// Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
		FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
		factory.setFeature(FEATURE, true);

		// If you can't completely disable DTDs, then at least do the following:
		// Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
		// Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
		// JDK7+ - http://xml.org/sax/features/external-general-entities
		FEATURE = "http://xml.org/sax/features/external-general-entities";
		factory.setFeature(FEATURE, false);

		// Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
		// Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
		// JDK7+ - http://xml.org/sax/features/external-parameter-entities
		FEATURE = "http://xml.org/sax/features/external-parameter-entities";
		factory.setFeature(FEATURE, false);

		// Disable external DTDs as well
		FEATURE = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
		factory.setFeature(FEATURE, false);

		// and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
		factory.setXIncludeAware(false);
//		factory.setExpandEntityReferences(false); // not supported by sax factory

		// And, per Timothy Morgan: "If for some reason support for inline DOCTYPEs are a requirement, then
		// ensure the entity settings are disabled (as shown above) and beware that SSRF attacks
		// (http://cwe.mitre.org/data/definitions/918.html) and denial
		// of service attacks (such as billion laughs or decompression bombs via "jar:") are a risk."
		// remaining parser logic


		factory.setValidating(false);

	}

}
