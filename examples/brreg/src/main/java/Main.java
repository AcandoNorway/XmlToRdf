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

import no.acando.xmltordf.Builder;
import no.acando.xmltordf.ComplexClassTransform;
import org.apache.commons.io.IOUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Main {
	public static void main(String[] args) throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
		String ns = "http://brreg.no/";

		Model naeringskode = ModelFactory.createDefaultModel();

		for (int i = 1; true; i++) {
			ByteArrayInputStream naeringskodeXml = new ByteArrayInputStream(IOUtils.toString(new URI("https://hotell.difi.no/api/xml/brreg/naeringskode?page=" + i)).getBytes("UTF-8"));

			Model naeringskodeTemp = Builder.getAdvancedBuilderJena()
				.setBaseNamespace(ns, Builder.AppliesTo.bothElementsAndAttributes)

				.renameElement("http://brreg.no/naerk_tekst", RDFS.label.getURI())
				.renameElement("http://brreg.no/entry", "http://brreg.no/Naeringskode")

				.compositeId("http://brreg.no/Naeringskode")
				.fromElement("http://brreg.no/naerk")
				.mappedTo((elementMap, attributeMap) -> "http://brreg.no/naeringskode/" + elementMap.get("http://brreg.no/naerk"))

				.build().convertForPostProcessing(naeringskodeXml)
				.mustacheTransform(new ByteArrayInputStream("delete {?a ?b ?c} where {?a ?b ?c. filter(isBlank(?a))}".getBytes("UTF-8")), new Object())
				.getModel();

			naeringskode.add(naeringskodeTemp);

			if (naeringskodeTemp.isEmpty()) {
				break;
			}

		}


		ByteArrayInputStream orgFormXml = new ByteArrayInputStream(IOUtils.toString(new URI("https://hotell.difi.no/api/xml/brreg/organisasjonsform?")).getBytes("UTF-8"));

		Model enhetstyper = Builder.getAdvancedBuilderJena()
			.setBaseNamespace(ns, Builder.AppliesTo.bothElementsAndAttributes)

			.renameElement("http://brreg.no/enhetstype_tekst", RDFS.label.getURI())

			.renameElement("http://brreg.no/entry", "http://brreg.no/Enhetstype")

			.compositeId("http://brreg.no/Enhetstype")
			.fromElement("http://brreg.no/enhetstype")
			.mappedTo((elementMap, attributeMap) -> "http://brreg.no/enhetstype/" + elementMap.get("http://brreg.no/enhetstype"))

			.build().convertForPostProcessing(orgFormXml)
			.mustacheTransform(new ByteArrayInputStream("delete {?a ?b ?c} where {?a ?b ?c. filter(isBlank(?a))}".getBytes("UTF-8")), new Object())
			.getModel();


		ComplexClassTransform convertDate = e -> {
			if (e.getHasValue() != null) {
				String s = e.getHasValue();
				if (s.contains(".")) {
					String[] split = s.split("\\.");
					e.setHasValue(new StringBuilder(split[2]).append("-").append(split[1]).append("-").append(split[0]));

				}

			}

		};

		ComplexClassTransform convertBoolean = e -> {
			if (e.getHasValue() != null) {
				String s = e.getHasValue();
				if (s.equals("N")) {
					e.setHasValue("false");
				}
				if (s.equals("J")) {
					e.setHasValue("true");
				}

			}

		};

		Model all = getAllModel(naeringskode, enhetstyper);


		int badPages = 0;


		for (int i = 1; i < 99999; i++) {
			ByteArrayInputStream brregXml = new ByteArrayInputStream(IOUtils.toString(new URI("https://hotell.difi.no/api/xml/brreg/enhetsregisteret?page=" + i)).getBytes("UTF-8"));

			Builder.AdvancedJena brregXmlBuilder = Builder.getAdvancedBuilderJena()
				.setBaseNamespace(ns, Builder.AppliesTo.bothElementsAndAttributes)
				.renameElement("http://brreg.no/entry", "http://brreg.no/Enhet")
				.renameElement("http://brreg.no/nkode1", "http://brreg.no/naeringskode")
				.renameElement("http://brreg.no/nkode2", "http://brreg.no/naeringskode")


				.setDatatype("http://brreg.no/ansatte_antall", XSDDatatype.XSDinteger)
				.addComplexElementTransformAtEndOfElement("http://brreg.no/regdato", convertDate)
				.addComplexElementTransformAtEndOfElement("http://brreg.no/stiftelsesdato", convertDate)
				.addComplexElementTransformAtEndOfElement("http://brreg.no/ansatte_dato", convertDate)

				.setDatatype("http://brreg.no/regdato", XSDDatatype.XSDdate)
				.setDatatype("http://brreg.no/stiftelsesdato", XSDDatatype.XSDdate)
				.setDatatype("http://brreg.no/ansatte_dato", XSDDatatype.XSDdate)

				.addComplexElementTransformAtEndOfElement("http://brreg.no/tvangsavvikling", convertBoolean)
				.setDatatype("http://brreg.no/tvangsavvikling", XSDDatatype.XSDboolean)

				.addComplexElementTransformAtEndOfElement("http://brreg.no/konkurs", convertBoolean)
				.setDatatype("http://brreg.no/konkurs", XSDDatatype.XSDboolean)

				.addComplexElementTransformAtEndOfElement("http://brreg.no/regiaa", convertBoolean)
				.setDatatype("http://brreg.no/regiaa", XSDDatatype.XSDboolean)

				.addComplexElementTransformAtEndOfElement("http://brreg.no/regifr", convertBoolean)
				.setDatatype("http://brreg.no/regifr", XSDDatatype.XSDboolean)

				.renameElement("http://brreg.no/regifriv", "http://brreg.no/registrert-i-frivillighets-register")
				.addComplexElementTransformAtEndOfElement("http://brreg.no/registrert-i-frivillighets-register", convertBoolean)
				.setDatatype("http://brreg.no/registrert-i-frivillighets-register", XSDDatatype.XSDboolean)

				.renameElement("http://brreg.no/regimva", "http://brreg.no/registrert-i-merverdiavgiftsregisteret")
				.addComplexElementTransformAtEndOfElement("http://brreg.no/registrert-i-merverdiavgiftsregisteret", convertBoolean)
				.setDatatype("http://brreg.no/registrert-i-merverdiavgiftsregisteret", XSDDatatype.XSDboolean)

				.addComplexElementTransformAtEndOfElement("http://brreg.no/avvikling", convertBoolean)
				.setDatatype("http://brreg.no/avvikling", XSDDatatype.XSDboolean)

				.compositeId("http://brreg.no/Enhet")
				.fromElement("http://brreg.no/orgnr")
				.mappedTo((elementMap, attributeMap) -> "http://brreg.no/" + elementMap.get("http://brreg.no/orgnr"))
				.mapTextInElementToUri(ns + "hovedenhet", orgnummer -> NodeFactory.createURI(ns + orgnummer))

				.mapTextInElementToUri(ns + "forradrland", "Norge", NodeFactory.createURI("http://dbpedia.org/resource/Norway"));


			StmtIterator resIterator = enhetstyper.listStatements(null, enhetstyper.getProperty("http://brreg.no/enhetstype"), (RDFNode) null);
			while (resIterator.hasNext()) {
				Statement statement = resIterator.nextStatement();
				brregXmlBuilder.mapTextInElementToUri("http://brreg.no/organisasjonsform", statement.getObject().toString(), statement.getSubject().asNode());
			}

			StmtIterator naeringskodeIterator = naeringskode.listStatements(null, enhetstyper.getProperty("http://brreg.no/naerk"), (RDFNode) null);
			while (naeringskodeIterator.hasNext()) {
				Statement statement = naeringskodeIterator.nextStatement();
				brregXmlBuilder.mapTextInElementToUri("http://brreg.no/naeringskode", statement.getObject().toString(), statement.getSubject().asNode());

			}

			try {
				Model model = brregXmlBuilder
					.build().convertForPostProcessing(brregXml)
					.getModel();

				System.out.println(i + "   -   " + model.size());

				if (model.size() <= 4) {
					break;
				}
				all.add(model);
			} catch (SAXParseException s) {
				System.out.println(":(");
				badPages++;
			}


			if (i % 1000 == 0) {

				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream("brregOutput/brreg" + i + ".ttl"));
				all.write(bufferedOutputStream, "TTL");
				bufferedOutputStream.flush();
				bufferedOutputStream.close();

				all = getAllModel(naeringskode, enhetstyper);

			}


		}


		System.out.println("Bad pages: " + badPages);


//        all.write(System.out, "TTL");


		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream("brregOutput/brreg_final_bit.ttl"));
		all.write(bufferedOutputStream, "TTL");
		bufferedOutputStream.flush();
		bufferedOutputStream.close();

	}

	private static Model getAllModel(Model naeringskode, Model enhetstyper) {
		Model all = ModelFactory.createDefaultModel()
			.add(enhetstyper)
			.add(naeringskode);

		all.setNsPrefix("brregNaeringskode", "http://brreg.no/naeringskode/");
		all.setNsPrefix("rdfs", RDFS.uri);
		all.setNsPrefix("brreg", "http://brreg.no/");
		all.setNsPrefix("brregEnhetstype", "http://brreg.no/enhetstype/");
		return all;
	}

}
