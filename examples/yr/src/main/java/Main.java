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
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class Main {

    private static final String YR_NS = "http://www.yr.no/";

    public static void main(String[] args) throws URISyntaxException, IOException, ParserConfigurationException, SAXException {

        String url = YR_NS + "place/Norway/Telemark/Sauherad/Gvarv/forecast.xml";

//        System.out.println(IOUtils.toString(new URI(url)));

        ByteArrayInputStream yrXml = new ByteArrayInputStream(IOUtils.toString(new URI(url)).getBytes("UTF-8"));

        Builder.AdvancedJena builder = Builder.getAdvancedBuilderJena()
            .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(true)
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .autoTypeLiterals(true)
            .setBaseNamespace(YR_NS, Builder.AppliesTo.bothElementsAndAttributes)
            .renameElement(YR_NS + "link", YR_NS + "Link")
            .renameElement(YR_NS + "weatherstation", YR_NS + "Weatherstation")
            .renameElement(YR_NS + "tabular", YR_NS + "TabularForecast")
            .renameElement(YR_NS + "text", YR_NS + "TextualForecast")
            .renameElement(YR_NS + "weatherdata", YR_NS + "Weatherdata")
            .renameElement(Builder.createPath("http://www.yr.no/Weatherdata", "http://www.yr.no/location"), "http://www.yr.no/Location")

            .insertPredicate(YR_NS + "data").between(YR_NS + "Weatherdata", YR_NS+"Location")

            .renameElement(YR_NS + "observations", YR_NS + "observation")
            .useElementAsPredicate(YR_NS + "observation")


            .renameElement(YR_NS + "links", YR_NS + "link")
            .useElementAsPredicate(YR_NS + "link")

            .useElementAsPredicate(YR_NS + "credit")
            .useElementAsPredicate(YR_NS + "forecast")


            .insertPredicate(YR_NS + "period").betweenSpecificParentAndAnyChild(YR_NS + "TabularForecast")
            .insertPredicate(YR_NS + "location").between(YR_NS + "TextualForecast", YR_NS + "location")

            .mapTextInElementToUri(YR_NS + "country", "Norway", NodeFactory.createURI("http://dbpedia.org/resource/Norway"))

            .useAttributeForId(null, YR_NS+"geobaseid", v -> YR_NS+"location/"+v)

            ;

        Dataset dataset = builder.build().convertToDataset(yrXml);

        dataset.getDefaultModel().write(System.out, "TTL");
    }
}
