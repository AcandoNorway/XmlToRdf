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
import org.apache.jena.rdf.model.Model;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class Main {


    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, URISyntaxException {

        String url = "http://www.psa.no/getfile.php/PDF/DDRS_example.xml";

        ByteArrayInputStream reportingHubXml = new ByteArrayInputStream(IOUtils.toString(new URI(url)).getBytes("UTF-8"));

        String witsml = "http://www.witsml.org/schemas/1series#";

        Builder.AdvancedJena builder = Builder.getAdvancedBuilderJena()
            .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(true)
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .renameElement(witsml + "stratInfo", witsml + "StratInfo")
            .insertPredicate(witsml + "info").betweenAnyParentAndSpecificChild(witsml + "StratInfo")

            .autoTypeLiterals(true)

            .renameElement(witsml + "controlIncidentInfo", witsml + "ControlIncidentInfo")
            .insertPredicate(witsml + "info").betweenAnyParentAndSpecificChild(witsml + "ControlIncidentInfo")

            .renameElement(witsml + "lithShowInfo", witsml + "LithShowInfo")
            .insertPredicate(witsml + "info").betweenAnyParentAndSpecificChild(witsml + "LithShowInfo")

            .renameElement(witsml + "wellboreInfo", witsml + "WellboreInfo")
            .insertPredicate(witsml + "info").betweenAnyParentAndSpecificChild(witsml + "WellboreInfo")

            .renameElement(witsml + "formTestInfo", witsml + "FormTestInfo")
            .insertPredicate(witsml + "info").betweenAnyParentAndSpecificChild(witsml + "FormTestInfo")

            .renameElement(witsml + "wellTestInfo", witsml + "WellTestInfo")
            .insertPredicate(witsml + "info").betweenAnyParentAndSpecificChild(witsml + "WellTestInfo")

            .renameElement(witsml + "perfInfo", witsml + "PerfInfo")
            .insertPredicate(witsml + "info").betweenAnyParentAndSpecificChild(witsml + "PerfInfo")

            .renameElement(witsml + "statusInfo", witsml + "StatusInfo")
            .insertPredicate(witsml + "info").betweenAnyParentAndSpecificChild(witsml + "StatusInfo")

            .renameElement(witsml + "logInfo", witsml + "LogInfo")
            .insertPredicate(witsml + "info").betweenAnyParentAndSpecificChild(witsml + "LogInfo")

            .renameElement(witsml + "coreInfo", witsml + "CoreInfo")
            .insertPredicate(witsml + "info").betweenAnyParentAndSpecificChild(witsml + "CoreInfo")

            .renameElement(witsml + "equipFailureInfo", witsml + "EquipFailureInfo")
            .insertPredicate(witsml + "info").betweenAnyParentAndSpecificChild(witsml + "EquipFailureInfo")

            .renameElement(witsml + "gasReadingInfo", witsml + "GasReadingInfo")
            .insertPredicate(witsml + "info").betweenAnyParentAndSpecificChild(witsml + "GasReadingInfo")


            .renameElement(witsml + "fluid", witsml + "Fluid")
            .insertPredicate(witsml + "fluid").betweenAnyParentAndSpecificChild(witsml + "Fluid")

            .renameElement(witsml + "activity", witsml + "Activity")
            .insertPredicate(witsml + "activity").betweenAnyParentAndSpecificChild(witsml + "Activity")

            .renameElement(witsml + "porePressure", witsml + "PorePressure")
            .insertPredicate(witsml + "porePressure").betweenAnyParentAndSpecificChild(witsml + "PorePressure")

            .renameElement(witsml + "bitRecord", witsml + "BitRecord")
            .insertPredicate(witsml + "bitRecord").betweenAnyParentAndSpecificChild(witsml + "BitRecord")

            .renameElement(witsml + "wellDatum", witsml + "WellDatum")
            .insertPredicate(witsml + "wellDatum").betweenAnyParentAndSpecificChild(witsml + "WellDatum")

            .renameElement(witsml + "surveyStation", witsml + "SurveyStation")
            .insertPredicate(witsml + "surveyStation").betweenAnyParentAndSpecificChild(witsml + "SurveyStation")

            .renameElement(witsml + "drillReport", witsml + "DrillReport")
            .insertPredicate(witsml + "report").betweenAnyParentAndSpecificChild(witsml + "DrillReport")

            .renameElement(witsml + "drillReports", witsml + "DrillReports")


            .mapTextInAttributeToUri(null, witsml + "uom", "m", NodeFactory.createURI("http://data.posccaesar.org/rdl/RDS1332674"))
            .mapTextInAttributeToUri(null, witsml + "uom", "bar", NodeFactory.createURI("http://data.posccaesar.org/rdl/RDS1314539"))
            .mapTextInAttributeToUri(null, witsml + "uom", "Pa", NodeFactory.createURI("http://data.posccaesar.org/rdl/RDS1338749"))
            .mapTextInAttributeToUri(null, witsml + "uom", "g/cm3", NodeFactory.createURI("http://data.posccaesar.org/rdl/RDS1325924"))
            .mapTextInAttributeToUri(null, witsml + "uom", "dega", NodeFactory.createURI("http://data.posccaesar.org/rdl/RDS43166353217"))
            .mapTextInAttributeToUri(null, witsml + "uom", "in", NodeFactory.createURI("http://data.posccaesar.org/rdl/RDS1326959"))
            .mapTextInAttributeToUri(null, witsml + "uom", "ppm", NodeFactory.createURI("http://data.posccaesar.org/rdl/RDS1333484"))
            .mapTextInAttributeToUri(null, witsml + "uom", "mm", NodeFactory.createURI("http://data.posccaesar.org/rdl/RDS1357739"))
            .mapTextInAttributeToUri(null, witsml + "uom", "MPa", NodeFactory.createURI("http://data.posccaesar.org/rdl/RDS1332404"))
            .mapTextInAttributeToUri(null, witsml + "uom", "m3", NodeFactory.createURI("http://data.posccaesar.org/rdl/RDS1349099"))
            .mapTextInAttributeToUri(null, witsml + "uom", "m3/d", NodeFactory.createURI("http://data.posccaesar.org/rdl/RDS1320839"))
            .mapTextInAttributeToUri(null, witsml + "uom", "m3/m3", NodeFactory.createURI("http://data.posccaesar.org/rdl/RDS1320794"))
            .mapTextInAttributeToUri(null, witsml + "uom", "mPa.s", NodeFactory.createURI("http://data.posccaesar.org/rdl/RDS11617155"))
            .mapTextInAttributeToUri(null, witsml + "uom", "M(m3)/d", NodeFactory.createURI("http://data.posccaesar.org/rdl/RDS17250792"))
            .mapTextInAttributeToUri(null, witsml + "uom", "degC", NodeFactory.createURI("http://data.posccaesar.org/rdl/RDS1322684"))
            .mapTextInAttributeToUri(null, witsml + "uom", "M(m3)", NodeFactory.createURI("http://data.posccaesar.org/rdl/RDS17251242"))
            .mapTextInAttributeToUri(null, witsml + "uom", "h", NodeFactory.createURI("http://data.posccaesar.org/rdl/RDS1326734"))
            .mapTextInAttributeToUri(null, witsml + "uom", "dm3", NodeFactory.createURI("http://data.posccaesar.org/rdl/RDS1319174"))
            .mapTextInAttributeToUri(null, witsml + "uom", "min", NodeFactory.createURI("http://data.posccaesar.org/rdl/RDS1336814"))
            .mapTextInAttributeToUri(null, witsml + "uom", "%", NodeFactory.createURI("http://data.posccaesar.org/rdl/RDS16226040"))
            .mapTextInAttributeToUri(null, witsml + "uom", "m/h", NodeFactory.createURI("http://data.posccaesar.org/rdl/RDS1351349"))


            .renameElement(witsml + "nameWellbore", witsml + "wellbore");

        Model m = builder.build().convertForPostProcessing(reportingHubXml)
            .mustacheTransform(new ByteArrayInputStream(String.join("\n",
                "PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#>",
                "PREFIX witsml:  <{{{witsmlNs}}}>",

                "delete { ",
                "   ?a witsml:wellbore ?wellboreString",
                "}",
                "insert {",
                "   ?a witsml:wellbore ?uri",
                "}",

                "where {",
                "   ?a witsml:wellbore ?wellboreString. ",
                "   service <http://staging.data.posccaesar.org/npd/> { ",
                "       ?uri a <http://data.posccaesar.org/npd/ontology/ilap-interface/Wellbore>; ",
                "            rdfs:label ?wellboreString . ",
                "   }",
                "}").getBytes("UTF-8")),

                new Object() {
                    String witsmlNs = witsml;
                })
            .mustacheTransform(new ByteArrayInputStream(String.join("\n", "",
                "delete{?a <http://acandonorway.github.com/XmlToRdf/ontology.ttl#hasValue> ?b}",
                "insert{?a <{{{witsmlNs}}}value> ?b}",
                "where{?a <http://acandonorway.github.com/XmlToRdf/ontology.ttl#hasValue> ?b}").getBytes("UTF-8")), new Object() {
                String witsmlNs = witsml;
            })
            .getModel();

        m.write(System.out, "TTL");

    }

}
