import no.acando.xmltordf.Builder;
import org.apache.commons.io.IOUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.Dataset;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class Main {


    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, URISyntaxException {

        String url = "http://www.psa.no/getfile.php/PDF/DDRS_example.xml";

        ByteArrayInputStream reportingHubXml = new ByteArrayInputStream(IOUtils.toString(new URI(url)).getBytes());

        String witsml = "http://www.witsml.org/schemas/1series#";

        Builder.AdvancedJena builder = Builder.getAdvancedBuilderJena()
            .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(true)
            .convertComplexElementsWithOnlyAttributesToPredicates(true)
            .renameElement(witsml+"stratInfo", witsml+"StratInfo")
            .insertPredicate(witsml+"info").betweenAnyParentAndSpecificChild(witsml+"StratInfo")

            .renameElement(witsml+"controlIncidentInfo", witsml+"ControlIncidentInfo")
            .insertPredicate(witsml+"info").betweenAnyParentAndSpecificChild(witsml+"ControlIncidentInfo")

            .renameElement(witsml+"lithShowInfo", witsml+"LithShowInfo")
            .insertPredicate(witsml+"info").betweenAnyParentAndSpecificChild(witsml+"LithShowInfo")

            .renameElement(witsml+"wellboreInfo", witsml+"WellboreInfo")
            .insertPredicate(witsml+"info").betweenAnyParentAndSpecificChild(witsml+"WellboreInfo")

            .renameElement(witsml+"formTestInfo", witsml+"FormTestInfo")
            .insertPredicate(witsml+"info").betweenAnyParentAndSpecificChild(witsml+"FormTestInfo")

            .renameElement(witsml+"wellTestInfo", witsml+"WellTestInfo")
            .insertPredicate(witsml+"info").betweenAnyParentAndSpecificChild(witsml+"WellTestInfo")

            .renameElement(witsml+"perfInfo", witsml+"PerfInfo")
            .insertPredicate(witsml+"info").betweenAnyParentAndSpecificChild(witsml+"PerfInfo")

            .renameElement(witsml+"statusInfo", witsml+"StatusInfo")
            .insertPredicate(witsml+"info").betweenAnyParentAndSpecificChild(witsml+"StatusInfo")

            .renameElement(witsml+"logInfo", witsml+"LogInfo")
            .insertPredicate(witsml+"info").betweenAnyParentAndSpecificChild(witsml+"LogInfo")

            .renameElement(witsml+"coreInfo", witsml+"CoreInfo")
            .insertPredicate(witsml+"info").betweenAnyParentAndSpecificChild(witsml+"CoreInfo")

            .renameElement(witsml+"equipFailureInfo", witsml+"EquipFailureInfo")
            .insertPredicate(witsml+"info").betweenAnyParentAndSpecificChild(witsml+"EquipFailureInfo")

            .renameElement(witsml+"gasReadingInfo", witsml+"GasReadingInfo")
            .insertPredicate(witsml+"info").betweenAnyParentAndSpecificChild(witsml+"GasReadingInfo")


            .renameElement(witsml+"fluid", witsml+"Fluid")
            .insertPredicate(witsml+"fluid").betweenAnyParentAndSpecificChild(witsml+"Fluid")

            .renameElement(witsml+"activity", witsml+"Activity")
            .insertPredicate(witsml+"activity").betweenAnyParentAndSpecificChild(witsml+"Activity")

            .renameElement(witsml+"porePressure", witsml+"PorePressure")
            .insertPredicate(witsml+"porePressure").betweenAnyParentAndSpecificChild(witsml+"PorePressure")

            .renameElement(witsml+"bitRecord", witsml+"BitRecord")
            .insertPredicate(witsml+"bitRecord").betweenAnyParentAndSpecificChild(witsml+"BitRecord")

            .renameElement(witsml+"wellDatum", witsml+"WellDatum")
            .insertPredicate(witsml+"wellDatum").betweenAnyParentAndSpecificChild(witsml+"WellDatum")

            .renameElement(witsml+"surveyStation", witsml+"SurveyStation")
            .insertPredicate(witsml+"surveyStation").betweenAnyParentAndSpecificChild(witsml+"SurveyStation")

            .setDatatype(witsml + "dTim", XSDDatatype.XSDdateTime)
            .setDatatype(witsml + "createDate", XSDDatatype.XSDdateTime)
            .setDatatype(witsml + "dTimStart", XSDDatatype.XSDdateTime)
            .setDatatype(witsml + "dTimEnd", XSDDatatype.XSDdateTime)

            .setDatatype(witsml + "defaultMeasuredDepth", XSDDatatype.XSDboolean)
            .setDatatype(witsml + "defaultVerticalDepth", XSDDatatype.XSDboolean)


            ;

        Dataset dataset = builder.build().convertToDataset(reportingHubXml);

        dataset.getDefaultModel().write(System.out, "TTL");

    }

}
