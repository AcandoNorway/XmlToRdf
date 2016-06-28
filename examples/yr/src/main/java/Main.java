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
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.Dataset;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class Main {

	public static void main(String[] args) throws URISyntaxException, IOException, ParserConfigurationException, SAXException {

		String url = "http://www.yr.no/place/Norway/Telemark/Sauherad/Gvarv/forecast.xml";

		ByteArrayInputStream yrXml = new ByteArrayInputStream(IOUtils.toString(new URI(url)).getBytes());

		Builder.AdvancedJena builder = Builder.getAdvancedBuilderJena()
				.convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(true)
				.convertComplexElementsWithOnlyAttributesToPredicate(true)
				.setBaseNamespace("http://www.yr.no/", Builder.AppliesTo.bothElementsAndAttributes)
				.renameElement("http://www.yr.no/link", "http://www.yr.no/Link")
				.renameElement("http://www.yr.no/weatherstation", "http://www.yr.no/Weatherstation")
				.renameElement("http://www.yr.no/tabular", "http://www.yr.no/TabularForecast")
				.renameElement("http://www.yr.no/text", "http://www.yr.no/TextualForecast")


				.renameElement("http://www.yr.no/observations","http://www.yr.no/observation")
				.useElementAsPredicate("http://www.yr.no/observation")


				.renameElement("http://www.yr.no/links", "http://www.yr.no/link")
				.useElementAsPredicate("http://www.yr.no/link")

				.useElementAsPredicate("http://www.yr.no/credit")
				.useElementAsPredicate("http://www.yr.no/forecast")


				.insertPredicate("http://www.yr.no/period").betweenSpecificParentAndAnyChild("http://www.yr.no/TabularForecast")
				.insertPredicate("http://www.yr.no/location").between("http://www.yr.no/TextualForecast", "http://www.yr.no/location")

//				.renameElement("http://www.yr.no/temperature", "http://www.yr.no/Temperature")
//				.insertPredicate("http://www.yr.no/temperature").betweenAnyParentAndSpecificChild("http://www.yr.no/Temperature")
//
//				.renameElement("http://www.yr.no/windDirection", "http://www.yr.no/WindDirection")
//				.insertPredicate("http://www.yr.no/windDirection").betweenAnyParentAndSpecificChild("http://www.yr.no/WindDirection")
//
//				.renameElement("http://www.yr.no/windSpeed", "http://www.yr.no/WindSpeed")
//				.insertPredicate("http://www.yr.no/windSpeed").betweenAnyParentAndSpecificChild("http://www.yr.no/WindSpeed")
//
//				.renameElement("http://www.yr.no/precipitation", "http://www.yr.no/Precipitation")
//				.insertPredicate("http://www.yr.no/precipitation").betweenAnyParentAndSpecificChild("http://www.yr.no/Precipitation")
//
//				.renameElement("http://www.yr.no/pressure", "http://www.yr.no/Pressure")
//				.insertPredicate("http://www.yr.no/pressure").betweenAnyParentAndSpecificChild("http://www.yr.no/Pressure")
//
//				.renameElement("http://www.yr.no/symbol", "http://www.yr.no/Symbol")
//				.insertPredicate("http://www.yr.no/symbol").betweenAnyParentAndSpecificChild("http://www.yr.no/Symbol")

				;

		Dataset dataset = builder.build().convertToDataset(yrXml);

		dataset.getDefaultModel().write(System.out, "TTL");
	}
}
