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

import no.acando.xmltordf.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class XmlToRdfTest {

    final String RDF = org.apache.jena.vocabulary.RDF.uri;


    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void simple() throws Exception {


        testJena(Builder.getJenaBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(false).build());
        testObject(Builder.getObjectBasedBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(false).build());
        testFast(Builder.getFastBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(false).build());


    }


    @Test
    public void simpleWithAttrs() throws Exception {


        testJena(Builder.getJenaBuilder().overrideNamespace("http://test/").build());
        testObject(Builder.getObjectBasedBuilder().overrideNamespace("http://test/").build());
        testFast(Builder.getFastBuilder().overrideNamespace("http://test/").build());


    }

    @Test
    public void simpleWithAttrsUUID() throws Exception {

        boolean[] errorOccured = {false};
        collector = new ErrorCollector(){
            @Override
            public void addError(Throwable error) {
                errorOccured[0] = true;
            }
        };

            testObject(Builder.getObjectBasedBuilder()
                .overrideNamespace("http://test/")
                .uuidBasedIdInsteadOfBlankNodes(true)
                .build());


        assertTrue("This test just tests that the results are NOT isomorphic, which they will be when you don't use blank nodes anymore.", errorOccured[0]);

    }

    //  @Ignore
    @Test
    public void inLineData() throws Exception {
        testJena(Builder.getJenaBuilder().overrideNamespace("http://test/").build());
        testObject(Builder.getObjectBasedBuilder().overrideNamespace("http://test/").build());
        //testFast(Builder.getFastBuilder().overrideNamespace("http://test/").build());

    }

    @Test
    public void specialCharacters() throws Exception {
        testJena(Builder.getJenaBuilder().overrideNamespace("http://test/").build());
        testObject(Builder.getObjectBasedBuilder().overrideNamespace("http://test/").build());
        testFast(Builder.getFastBuilder().overrideNamespace("http://test/").build());


    }

    @Ignore
    @Test
    public void namespaces() throws Exception {
        testObject(Builder
            .getObjectBasedBuilder()
            .autoAttributeNamespace(false)
            .setBaseNamespace("http://test/", Builder.AppliesTo.bothElementsAndAttributes)
            .build());


    }

    @Test
    public void namespacesAutoSuffix() throws Exception {
        testObject(Builder
            .getObjectBasedBuilder()
            .build());

        testJena(Builder
            .getJenaBuilder()
            .build());

    }

    @Test
    public void namespacesAutoSuffix2() throws Exception {
        testObject(Builder
            .getObjectBasedBuilder()
            .autoAddSuffixToNamespace(false)
            .build());

        testJena(Builder
            .getJenaBuilder()
            .autoAddSuffixToNamespace(false)
            .build());

    }

    @Test
    public void namespacesAutoSuffix3() throws Exception {
        testObject(Builder
            .getObjectBasedBuilder()
            .build());

        testJena(Builder
            .getJenaBuilder()
            .build());

    }

    @Test
    public void namespacesAutoSuffixSlash() throws Exception {
        testObject(Builder
            .getObjectBasedBuilder()
            .autoAddSuffixToNamespace("/")
            .build());

    }


    @Test
    public void checkIndexes() throws Exception {

        testJena(Builder.getJenaBuilder().overrideNamespace("http://test/").addIndex(true).build());
        testObject(Builder.getObjectBasedBuilder().overrideNamespace("http://test/").addIndex(true).build());
//           testFast(Builder.getFastBuilder().overrideNamespace("http://test/").addIndex(true).build());


    }


    @Test
    public void attributeForId() throws Exception {

        testObject(Builder.getObjectBasedBuilder().overrideNamespace("http://test/")
            .addUseAttributeForId("A", "id", (var) -> "http://test/" + var)
            .addUseAttributeForId("B", "id", (var) -> "http://test/" + var)
            .build());

        testJena(Builder.getJenaBuilder().overrideNamespace("http://test/")
            .addUseAttributeForId("A", "id", (var) -> "http://test/" + var)
            .addUseAttributeForId("B", "id", (var) -> "http://test/" + var)
            .build());


    }

    @Test
    public void attributeForIdWithNs() throws Exception {

        testJena(Builder.getJenaBuilder()
            .addUseAttributeForId("http://example.com/A", "id", (var) -> "http://test/" + var)
            .addUseAttributeForId("http://example2.com/B", "id", (var) -> "http://test/" + var)
            .build());
        testObject(Builder.getObjectBasedBuilder()
            .addUseAttributeForId("http://example.com/A", "id", (var) -> "http://test/" + var)
            .addUseAttributeForId("http://example2.com/B", "id", (var) -> "http://test/" + var)
            .build());

    }

    @Test
    public void attributeForId2() throws Exception {

        testJena(Builder.getJenaBuilder()
            .addUseAttributeForId(null, "id", (var) -> "http://test/" + var)
            .build());
        testObject(Builder.getObjectBasedBuilder()
            .addUseAttributeForId(null, "id", (var) -> "http://test/" + var)
            .build());

    }

    @Test
    public void classMapping() throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("A", "http://hurra/A2");
        map.put("B", "http://hurra/B2");


        testJena(Builder.getJenaBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(false).addMapForClasses(map).build());
        testObject(Builder.getObjectBasedBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(false).addMapForClasses(map).build());
        testFast(Builder.getFastBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(false).addMapForClasses(map).build());


    }

    @Test
    public void detectLiteralProperties() throws Exception {

        testJena(Builder.getJenaBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());
        testObject(Builder.getObjectBasedBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());
        testFast(Builder.getFastBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());


    }

    @Test
    public void detectLiteralPropertiesWithAttributes() throws Exception {

        testJena(Builder.getJenaBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());
        testObject(Builder.getObjectBasedBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());
        testFast(Builder.getFastBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());


    }

    @Test
    public void detectLiteralPropertiesWithAttributesEdgeCaseWithRdfType() throws Exception {

        testJena(Builder.getJenaBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());
        testObject(Builder.getObjectBasedBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());
        testFast(Builder.getFastBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());


    }

    @Test
    public void detectLiteralPropertiesEdgeCaseWithoutParent() throws Exception {
        testJena(Builder.getJenaBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());
        testObject(Builder.getObjectBasedBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());
        testFast(Builder.getFastBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());


    }

    @Test
    public void detectLiteralPropertiesEdgeCaseWithoutParent2() throws Exception {


        testJena(Builder.getJenaBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());
        testObject(Builder.getObjectBasedBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());
        testFast(Builder.getFastBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());


    }

    @Test
    public void detectLiteralPropertiesWithAttributesEdgeCaseWithoutParent() throws Exception {


        testJena(Builder.getJenaBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());
        testObject(Builder.getObjectBasedBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());
        testFast(Builder.getFastBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());

    }

    @Test
    public void tesAautoDetectLiteralPropertiesSetToFalse() throws Exception {
        testJena(Builder.getJenaBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(false).build());
        testObject(Builder.getObjectBasedBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(false).build());
        testFast(Builder.getFastBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(false).build());


    }


    @Test
    public void withEmptyElements() throws Exception {


            testJena(Builder.getJenaBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());
          testFast(Builder.getFastBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());
        testObject(Builder.getObjectBasedBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());


    }

    @Test
    public void withSpecialAmp() throws Exception {
         testJena(Builder.getJenaBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());
        testObject(Builder.getObjectBasedBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());
       // testFast(Builder.getFastBuilder().overrideNamespace("http://test/").autoDetectLiteralProperties(true).build());

    }


    @Test
    public void attrValueTransform() throws Exception {
        testJena(Builder.getJenaBuilder().overrideNamespace("http://test/")
            .addTransformForAttributeValue(null, null, (val) -> val.replaceAll("ll", "qq"))
            .build());
        testObject(Builder.getObjectBasedBuilder().overrideNamespace("http://test/")
            .addTransformForAttributeValue(null, null, (val) -> val.replaceAll("ll", "qq"))
            .autoDetectLiteralProperties(true).build());
        testFast(Builder.getFastBuilder().overrideNamespace("http://test/")
            .addTransformForAttributeValue(null, null, (val) -> val.replaceAll("ll", "qq"))
            .autoDetectLiteralProperties(true).build());

    }

    @Test
    public void attrValueTransform2() throws Exception {
        testJena(Builder.getJenaBuilder().overrideNamespace("http://test/")
            .addTransformForAttributeValue("name", "hurra", (val) -> val.replaceAll("ll", "qq"))
            .build());
        testObject(Builder.getObjectBasedBuilder().overrideNamespace("http://test/")
            .addTransformForAttributeValue("name", "hurra", (val) -> val.replaceAll("ll", "qq"))
            .autoDetectLiteralProperties(true).build());
        testFast(Builder.getFastBuilder().overrideNamespace("http://test/")
            .addTransformForAttributeValue("name", "hurra", (val) -> val.replaceAll("ll", "qq"))
            .autoDetectLiteralProperties(true).build());

    }

    @Test
    public void attrValueTransform3() throws Exception {
        testJena(Builder.getJenaBuilder().overrideNamespace("http://test/")
            .addTransformForAttributeValue(null, "hurra", (val) -> val.replaceAll("ll", "qq"))
            .build());
        testObject(Builder.getObjectBasedBuilder().overrideNamespace("http://test/")
            .addTransformForAttributeValue(null, "hurra", (val) -> val.replaceAll("ll", "qq"))
            .autoDetectLiteralProperties(true).build());
        testFast(Builder.getFastBuilder().overrideNamespace("http://test/")
            .addTransformForAttributeValue(null, "hurra", (val) -> val.replaceAll("ll", "qq"))
            .autoDetectLiteralProperties(true).build());

    }


    @Test
    public void attrValueTransform4() throws Exception {
        testJena(Builder.getJenaBuilder().overrideNamespace("http://test/")
            .addTransformForAttributeValue("name", null, (val) -> val.replaceAll("ll", "qq"))
            .build());
        testObject(Builder.getObjectBasedBuilder().overrideNamespace("http://test/")
            .addTransformForAttributeValue("name", null, (val) -> val.replaceAll("ll", "qq"))
            .autoDetectLiteralProperties(true).build());
        testFast(Builder.getFastBuilder().overrideNamespace("http://test/")
            .addTransformForAttributeValue("name", null, (val) -> val.replaceAll("ll", "qq"))
            .autoDetectLiteralProperties(true).build());

    }

    @Test
    public void attrValueTransform5() throws Exception {
        testJena(Builder.getJenaBuilder().overrideNamespace("http://test/")
            .addTransformForAttributeValue("name", null, (val) -> val.replaceAll("ll", "qq"))
            .addTransformForAttributeValue(null, "test", (val) -> val.replaceAll("lala", "dada"))

            .build());
        testObject(Builder.getObjectBasedBuilder().overrideNamespace("http://test/")
            .addTransformForAttributeValue("name", null, (val) -> val.replaceAll("ll", "qq"))
            .addTransformForAttributeValue(null, "test", (val) -> val.replaceAll("lala", "dada"))

            .autoDetectLiteralProperties(true).build());
        testFast(Builder.getFastBuilder().overrideNamespace("http://test/")
            .addTransformForAttributeValue("name", null, (val) -> val.replaceAll("ll", "qq"))
            .addTransformForAttributeValue(null, "test", (val) -> val.replaceAll("lala", "dada"))

            .autoDetectLiteralProperties(true).build());

    }

    @Test
    public void specialShallowHandling() throws Exception {
//            XmlToRdfJena build = Builder.getJenaBuilder()
//                        .autoAddNamespaceDeclarations(true)
//                        .autoDetectLiteralProperties(false)
//                        .autoConvertShallowChildrenToProperties(true)
//                        .addTransformForAttributeValue(null, "http://www.w3.org/1999/02/22-rdf-syntax-ns#nodeID",
//                                    (val) -> val.replaceAll("%C3%B8", "ø").replaceAll("%C3%A6", "æ"))
//                        .addUseAttributeForId(null, "http://www.w3.org/1999/02/22-rdf-syntax-ns#nodeID", (val) -> "http://example.com/" + val)
//                        .build();
//            testJena(build);


        testObject(Builder.getObjectBasedBuilder()
            .autoDetectLiteralProperties(false)
            .autoConvertShallowChildrenToProperties(true)
            .addTransformForAttributeValue(null, "http://www.w3.org/1999/02/22-rdf-syntax-ns#nodeID",
                (val) -> val.replaceAll("%C3%B8", "ø").replaceAll("%C3%A6", "æ"))
            .addUseAttributeForId(null, "http://www.w3.org/1999/02/22-rdf-syntax-ns#nodeID", (val) -> "http://example.com/" + val)
            .build());

    }



    @Test
    public void skosFromLexaurus() throws Exception {
//            testJena(Builder.getJenaBuilder()
//                        .addUseAttributeForId(null, RDF + "nodeID", (var) -> "http://test/" + var)
//                        .autoConvertShallowChildrenToProperties(true).build());

        testObject(Builder.getObjectBasedBuilder()
            .addUseAttributeForId(null, RDF + "nodeID", (var) -> "http://test/" + var)
            .autoConvertShallowChildrenToProperties(true).build());
    }

    @Ignore
    @Test
    public void testSparqlTransforms() throws Exception {
        PostProcessingJena postProcessing = Builder.getJenaBuilder().build().convertForPostProcessing(new File("testFiles/testSparqlTransform/input.xml"));

        postProcessing
            .sparqlTransform(new File("testFiles/testSparqlTransform/transform.qr"))
            .sparqlTransform(new File("testFiles/testSparqlTransform/transforms/"))
            .extractConstruct(new File("testFiles/testSparqlTransform/construct.qr"))
            .extractConstruct(new File("testFiles/testSparqlTransform/constructs/"))
            .getExtractedModel();
    }

    @Test
    public void testSparqlTransforms2() throws Exception {
        PostProcessingJena postProcessing = Builder.getJenaBuilder().overrideNamespace("http://example.com/").build().convertForPostProcessing(new File("testFiles/testSparqlTransform2/input.xml"));

        Model extractedModel = postProcessing
            .outputIntermediaryModels(new File("testFiles/testSparqlTransform2/intermediary"))
            .sparqlTransform(new File("testFiles/testSparqlTransform2/transform.qr"))
            .sparqlTransform(new File("testFiles/testSparqlTransform2/transforms/"))
            .extractConstruct(new File("testFiles/testSparqlTransform2/constructs/"))
            .extractConstruct(new File("testFiles/testSparqlTransform2/construct.qr"))
            .getExtractedModel();


        Model model = FileManager.get().readModel(ModelFactory.createDefaultModel(), "testFiles/testSparqlTransform2/expected.ttl");
        if (!model.isIsomorphicWith(extractedModel)) {

            assertEquals("Something went wrong with the test. ", modelToString(extractedModel), modelToString(extractedModel));

        }


    }

    @Test
    public void convertShallowElementsToPropertiesWithAutoDetectLiteralProperties() throws Exception {

        testObject(Builder.getObjectBasedBuilder()
            .autoConvertShallowChildrenToProperties(true)
            .autoConvertShallowChildrenWithAutoDetectLiteralProperties(true)
            .setBaseNamespace("http://a/", Builder.AppliesTo.bothElementsAndAttributes)
            .build());

    }


    @Test
    public void insertPropertyBetween() throws Exception {

        testObject(Builder.getObjectBasedBuilder()
            .autoConvertShallowChildrenToProperties(true)
            .setBaseNamespace("http://a/", Builder.AppliesTo.bothElementsAndAttributes)
            .insertPropertyBetween("http://a/hasB", "http://a/A", "http://a/B")
            .build());

    }

    @Test(expected = RuntimeException.class)
    public void insertPropertyBetween2() throws Exception {

        Builder.getObjectBasedBuilder()
            .insertPropertyBetween("http://a/hasB", "http://a/A", "http://a/B")
            .insertPropertyBetween("http://a/hasC", "http://a/A", "http://a/B");
    }


    @Test
    public void invertPropertyBetween() throws Exception {

        testObject(Builder.getObjectBasedBuilder()
            .autoConvertShallowChildrenToProperties(true)
            .setBaseNamespace("http://a/", Builder.AppliesTo.bothElementsAndAttributes)
            .insertPropertyBetween("http://a/belongsToA", "http://a/A", "http://a/B")
            .invertProperty("http://a/belongsToA", "http://a/A", "http://a/B")

            .build());

    }

    @Test
    public void specifyDatatype() throws Exception {

        testObject(Builder.getObjectBasedBuilder()
            .autoConvertShallowChildrenToProperties(true)
            .setBaseNamespace("http://a/", Builder.AppliesTo.bothElementsAndAttributes)
            .setDatatype("http://a/num", XMLSchema.INTEGER)
            .setDatatype("http://a/date", XMLSchema.DATE)

            .build());

    }

    @Test
    public void specifyDatatype2() throws Exception {

        testObject(Builder.getObjectBasedBuilder()
            .autoConvertShallowChildrenWithAutoDetectLiteralProperties(false)
            .autoConvertShallowChildrenToProperties(false)
            .autoDetectLiteralProperties(false)
            .setBaseNamespace("http://a/", Builder.AppliesTo.bothElementsAndAttributes)
            .setDatatype("http://a/num", XMLSchema.INTEGER)
            .setDatatype("http://a/date", XMLSchema.DATE)

            .build());

    }

    @Test
    public void longLiteral() throws ParserConfigurationException, SAXException, IOException {
        testObject(Builder.getObjectBasedBuilder()
            .overrideNamespace("http://example.org")
            .build());

        testJena(Builder.getJenaBuilder()
            .overrideNamespace("http://example.org")
            .build());

        //@TODO add support for long literals with new lines in fast
//        testFast(Builder.getFastBuilder()
//            .overrideNamespace("http://example.org")
//            .build());
    }


    private void testObject(XmlToRdfObject build) throws IOException, ParserConfigurationException, SAXException {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String methodName = stackTrace[2].getMethodName();
        String path = "testFiles/" + methodName;
        File pathDir = new File(path);
        if (!pathDir.exists()) {
            pathDir.mkdir();
        }

        File xml = new File(path + "/input.xml");
        File expected = new File(path + "/expected.ttl");

        if (!xml.exists()) {
            xml.createNewFile();
        }

        if (!expected.exists()) {
            expected.createNewFile();
        }

        build.objectBasedConvertToStream(new FileInputStream(xml), new FileOutputStream(path + "/actualObject.n3"));
        Model actualModelJena = build.objectBasedConvertToDataset(new FileInputStream(xml)).getDefaultModel();
        Repository repository = build.objectBasedConvertToRepository(new FileInputStream(xml));


        String rdf = repositoryToString(repository, RDFFormat.TURTLE);
        System.out.println(rdf);
        Model actualModelSesame = ModelFactory.createDefaultModel().read(new ByteArrayInputStream(rdf.getBytes()), "", "TTL");

        Model actualModel = FileManager.get().readModel(ModelFactory.createDefaultModel(), path + "/actualObject.n3");

        Model expectedModel = FileManager.get().readModel(ModelFactory.createDefaultModel(), expected.getCanonicalPath());

        if (!expectedModel.isIsomorphicWith(actualModel)) {
            try {
                assertEquals("Not isomorphic for object method.", modelToString(expectedModel), modelToString(actualModel));

            } catch (AssertionError error) {
                collector.addError(error);
            }
        }

        if (!expectedModel.isIsomorphicWith(actualModelJena)) {
            try {
                assertEquals("Not isomorphic for object method with jena dataset.", modelToString(expectedModel), modelToString(actualModelJena));

            } catch (AssertionError error) {
                collector.addError(error);
            }
        }


        if (!expectedModel.isIsomorphicWith(actualModelSesame)) {
            try {
                assertEquals("Not isomorphic for object method with sesame repository.", modelToString(expectedModel), modelToString(actualModelSesame));

            } catch (AssertionError error) {
                collector.addError(error);
            }
        }
    }

    private void testFast(XmlToRdfFast build) throws IOException, ParserConfigurationException, SAXException {

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String methodName = stackTrace[2].getMethodName();
        String path = "testFiles/" + methodName;
        File pathDir = new File(path);
        if (!pathDir.exists()) {
            pathDir.mkdir();
        }

        File xml = new File(path + "/input.xml");
        File expected = new File(path + "/expected.ttl");

        if (!xml.exists()) {
            xml.createNewFile();
        }

        if (!expected.exists()) {
            expected.createNewFile();
        }

        build.fastConvertToStream(new FileInputStream(xml), new FileOutputStream(path + "/actualFast.n3"));
        Model actualModel = FileManager.get().readModel(ModelFactory.createDefaultModel(), path + "/actualFast.n3");

        Model expectedModel = FileManager.get().readModel(ModelFactory.createDefaultModel(), expected.getCanonicalPath());
        boolean isomorphicWith = expectedModel.isIsomorphicWith(actualModel);

        if (!isomorphicWith) {

            try {
                assertEquals("Not isomorphic for fast method.", modelToString(expectedModel), modelToString(actualModel));

            } catch (AssertionError error) {
                collector.addError(error);
            }
        }

    }


    private void testJena(XmlToRdfJena jena) throws IOException, ParserConfigurationException, SAXException {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String methodName = stackTrace[2].getMethodName();
        String path = "testFiles/" + methodName;
        File pathDir = new File(path);
        if (!pathDir.exists()) {
            pathDir.mkdir();
        }

        File xml = new File(path + "/input.xml");
        File expected = new File(path + "/expected.ttl");

        if (!xml.exists()) {
            xml.createNewFile();
        }

        if (!expected.exists()) {
            expected.createNewFile();
        }

        Model actualModel = jena.convertToJenaModel(xml);
        actualModel.write(new FileOutputStream(path + "/actual.ttl"), Lang.TURTLE.getLabel());

        Model expectedModel = FileManager.get().readModel(ModelFactory.createDefaultModel(), expected.getCanonicalPath());
        boolean isomorphicWith = expectedModel.isIsomorphicWith(actualModel);

        if (!isomorphicWith) {

            try {
                assertEquals("Not isomorphic for toJena method.", modelToString(expectedModel), modelToString(actualModel));

            } catch (AssertionError error) {
                collector.addError(error);
            }

        }


    }


    private String modelToString(Model createdModel) {

        createdModel.setNsPrefix("xmlToRdf", "http://acandonorway.github.com/ontology.ttl#");
        createdModel.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        createdModel.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");


        StringWriter stringWriter = new StringWriter();
        createdModel.write(stringWriter, "TTL");
        return stringWriter.toString();
    }


    private static String repositoryToString(Repository repository, RDFFormat format) {

        StringWriter stringWriter = new StringWriter();
        RDFWriter writer = Rio.createWriter(format, stringWriter);

        writer.startRDF();
        try (RepositoryConnection connection = repository.getConnection()) {
            RepositoryResult<Statement> statements = connection.getStatements(null, null, null);
            while (statements.hasNext()) {
                writer.handleStatement(statements.next());
            }
        }
        writer.endRDF();

        return stringWriter.toString();
    }
}