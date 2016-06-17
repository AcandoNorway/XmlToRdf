package no.acando.xmltordf.doclet;

import no.acando.xmltordf.Builder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


public class Temo {

	public static void main(String[] args) {
		String xml = "<archive xmlns=\"http://example.org/\">\n" +
				" <record nr=\"0000001\">\n" +
				"       <title>Important record</title>\n" +
				"       </record>\n" +
				" <record nr=\"0000002\">\n" +
				"       <title>Other record</title>\n" +
				"       </record>\n" +
				" </people>";

		System.out.println(toString(xml));
	}

	public static String toString(String xml) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			Builder.getAdvancedBuilderStream()
					.addUseAttributeForId("http://example.org/record", "http://example.org/nr", v -> "http://acme.com/records/"+v)
					.build().convertToStream(new ByteArrayInputStream(xml.getBytes()), byteArrayOutputStream);

			String s = new String(byteArrayOutputStream.toByteArray());

			return s;
		} catch (Exception e) {
			e.printStackTrace();
		} return "";    }
}



