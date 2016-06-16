package no.acando.xmltordf.doclet;

import no.acando.xmltordf.Builder;
import no.acando.xmltordf.SimpleTypePolicy;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


public class Temo {

	public String toString(String xml) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			Builder.getAdvancedBuilderStream()
					.setBaseNamespace("http://none/", Builder.AppliesTo.bothElementsAndAttributes)
					.autoAttributeNamespace(true)
					.build().convertToStream(new ByteArrayInputStream(xml.getBytes()), byteArrayOutputStream);

			String s = new String(byteArrayOutputStream.toByteArray());

			return s;
		} catch (Exception e) {
			e.printStackTrace();
		} return "";    }
}



