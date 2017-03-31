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
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.util.FileManager;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.openrdf.model.Namespace;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
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
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class XmlToRdfTest {

    static final String HELLO = "hello";
    static final String HTTP_A = "http://a/";
    static final String HTTP_TEST = "http://test/";
    static final String HTTP_A_NAME = "http://a/name";
    static final String ID = "id";
    static final String LL = "ll";
    static final String QQ = "qq";
    static final String ELEMENT_NAME = "name";
    static final String RDF_NODE_ID = org.apache.jena.vocabulary.RDF.uri + "nodeID";

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Test
    public void simple() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.expand).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.expand).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.expand).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.expand).build());

    }

    @Test
    public void mixedContent() throws Exception {
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

    }


    @Test
    public void renameOnPath() throws Exception {
        testAdvancedJena(
            Builder.getAdvancedBuilderJena()
                .renameElement(Builder.createPath("http://example.org/a", "http://example.org/a", "http://example.org/b"), "http://example.org/RENAMED1")
                .renameElement(Builder.createPath("http://example.org/a", "http://example.org/c", "http://example.org/b"), "http://example.org/RENAMED2")
                .build());

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .renameElement(Builder.createPath("http://example.org/a", "http://example.org/a", "http://example.org/b"), "http://example.org/RENAMED1")
            .renameElement(Builder.createPath("http://example.org/a", "http://example.org/c", "http://example.org/b"), "http://example.org/RENAMED2")
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .renameElement(Builder.createPath("http://example.org/a", "http://example.org/a", "http://example.org/b"), "http://example.org/RENAMED1")
            .renameElement(Builder.createPath("http://example.org/a", "http://example.org/c", "http://example.org/b"), "http://example.org/RENAMED2")
            .build());

    }

    @Test
    public void renameOnPathLongest() throws Exception {
        testAdvancedJena(
            Builder.getAdvancedBuilderJena()
                .renameElement(Builder.createPath("http://example.org/a", "http://example.org/b"), "http://example.org/SHORT")
                .renameElement(Builder.createPath("http://example.org/a", "http://example.org/a", "http://example.org/b"), "http://example.org/LONG")
                .renameElement(Builder.createPath("http://example.org/a", "http://example.org/a", "http://example.org/a", "http://example.org/b"), "http://example.org/NONE")
                .build());

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .renameElement(Builder.createPath("http://example.org/a", "http://example.org/b"), "http://example.org/SHORT")
            .renameElement(Builder.createPath("http://example.org/a", "http://example.org/a", "http://example.org/b"), "http://example.org/LONG")
            .renameElement(Builder.createPath("http://example.org/a", "http://example.org/a", "http://example.org/a", "http://example.org/b"), "http://example.org/NONE")
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .renameElement(Builder.createPath("http://example.org/a", "http://example.org/b"), "http://example.org/SHORT")
            .renameElement(Builder.createPath("http://example.org/a", "http://example.org/a", "http://example.org/b"), "http://example.org/LONG")
            .renameElement(Builder.createPath("http://example.org/a", "http://example.org/a", "http://example.org/a", "http://example.org/b"), "http://example.org/NONE")
            .build());

    }

    @Test
    public void simpleWithAttrs() throws Exception {

        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).build());
        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST).build());

    }

    @Test
    public void simpleWithAttrsUUID() throws Exception {

        boolean[] errorOccured = {false};
        collector = new ErrorCollector() {
            @Override
            public void addError(Throwable error) {
                errorOccured[0] = true;
            }
        };

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .overrideNamespace(HTTP_TEST)
            .uuidBasedIdInsteadOfBlankNodes(HTTP_TEST)
            .build());

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .overrideNamespace(HTTP_TEST)
            .uuidBasedIdInsteadOfBlankNodes(HTTP_TEST)
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .overrideNamespace(HTTP_TEST)
            .uuidBasedIdInsteadOfBlankNodes(HTTP_TEST)
            .build());

        assertTrue("This test just tests that the results are NOT isomorphic, which they will be when you don't use blank nodes anymore.", errorOccured[0]);

    }

    @Test
    public void inLineData() throws Exception {
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).build());
        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).build());

        //TODO: make new test for fast that doesn't do mixed content
//        testFast(Builder.getFastBuilder().overrideNamespace("http://test/").build());

    }

    @Test
    public void specialCharacters() throws Exception {
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).build());
        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST).build());

    }

    @Test
    public void insertPredicateMatchingDefault() throws ParserConfigurationException, SAXException, IOException {
        String xmlNameSpace = "http://www.arkivverket.no/standarder/noark5/arkivstruktur/";

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .autoAddSuffixToNamespace("/")
            .renameElement(xmlNameSpace + "arkiv", xmlNameSpace + "Arkiv")
            .renameElement(xmlNameSpace + "arkivskaper", xmlNameSpace + "Arkivskaper")
            .insertPredicate(xmlNameSpace + "arkivskaper").betweenAnyParentAndSpecificChild(xmlNameSpace + "Arkivskaper")
            .insertPredicate(xmlNameSpace + "parent").between(xmlNameSpace + "Arkiv", xmlNameSpace + "Arkiv")
            .build());
    }

    @Test
    public void useElementAsPredicate() throws ParserConfigurationException, SAXException, IOException {
        testAdvancedSesame(
            Builder.getAdvancedBuilderSesame()
                .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(true)
                .convertComplexElementsWithOnlyAttributesToPredicate(true)
                .useElementAsPredicate("http://example.org/friends")
                .build()
        );

        testAdvancedJena(
            Builder.getAdvancedBuilderJena()
                .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(true)
                .convertComplexElementsWithOnlyAttributesToPredicate(true)
                .useElementAsPredicate("http://example.org/friends")
                .build()
        );

        testAdvancedStream(
            Builder.getAdvancedBuilderStream()
                .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(true)
                .convertComplexElementsWithOnlyAttributesToPredicate(true)
                .useElementAsPredicate("http://example.org/friends")
                .build()
        );
    }

    //TODO: actually create test data
    @Ignore
    @Test
    public void namespaces() throws Exception {
        testAdvancedStream(Builder
            .getAdvancedBuilderStream()
            .autoAttributeNamespace(false)
            .setBaseNamespace(HTTP_TEST, Builder.AppliesTo.bothElementsAndAttributes)
            .build());

        testAdvancedSesame(Builder
            .getAdvancedBuilderSesame()
            .autoAttributeNamespace(false)
            .setBaseNamespace(HTTP_TEST, Builder.AppliesTo.bothElementsAndAttributes)
            .build());

        testAdvancedJena(Builder
            .getAdvancedBuilderJena()
            .autoAttributeNamespace(false)
            .setBaseNamespace(HTTP_TEST, Builder.AppliesTo.bothElementsAndAttributes)
            .build());
    }

    @Test
    public void namespacesAutoSuffix() throws Exception {
        testAdvancedJena(Builder
            .getAdvancedBuilderJena()
            .build());

        testAdvancedSesame(Builder
            .getAdvancedBuilderSesame()
            .build());

        testAdvancedStream(Builder
            .getAdvancedBuilderStream()
            .build());
    }

    @Test
    public void namespacesAutoSuffix2() throws Exception {
        testAdvancedStream(Builder
            .getAdvancedBuilderStream()
            .autoAddSuffixToNamespace(false)
            .build());

        testAdvancedSesame(Builder
            .getAdvancedBuilderSesame()
            .autoAddSuffixToNamespace(false)
            .build());

        testAdvancedJena(Builder
            .getAdvancedBuilderJena()
            .autoAddSuffixToNamespace(false)
            .build());

    }

    @Test
    public void namespacesAutoSuffix3() throws Exception {
        testAdvancedJena(Builder
            .getAdvancedBuilderJena()
            .build());

        testAdvancedSesame(Builder
            .getAdvancedBuilderSesame()
            .build());

        testAdvancedStream(Builder
            .getAdvancedBuilderStream()
            .build());

    }

    @Test
    public void namespacesAutoSuffixSlash() throws Exception {
        testAdvancedStream(Builder
            .getAdvancedBuilderStream()
            .autoAddSuffixToNamespace("/")
            .build());

        testAdvancedSesame(Builder
            .getAdvancedBuilderSesame()
            .autoAddSuffixToNamespace("/")
            .build());

        testAdvancedJena(Builder
            .getAdvancedBuilderJena()
            .autoAddSuffixToNamespace("/")
            .build());
    }

    @Test
    public void checkIndexes() throws Exception {

        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).addIndex(true).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).addIndex(true).build());
        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).addIndex(true).build());

    }

    @Test
    public void checkIndexesFalse() throws Exception {

        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).addIndex(false).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).addIndex(false).build());
        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).addIndex(false).build());

    }

    @Test
    public void attributeForId() throws Exception {

        final String a = "A";
        final String b = "B";

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST)
            .useAttributeForId(HTTP_TEST + a, HTTP_TEST + ID, (var) -> HTTP_TEST + var)
            .useAttributeForId(HTTP_TEST + b, HTTP_TEST + ID, (var) -> HTTP_TEST + var)
            .build());

        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST)
            .useAttributeForId(HTTP_TEST + a, HTTP_TEST + ID, (var) -> HTTP_TEST + var)
            .useAttributeForId(HTTP_TEST + b, HTTP_TEST + ID, (var) -> HTTP_TEST + var)
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST)
            .useAttributeForId(HTTP_TEST + a, HTTP_TEST + ID, (var) -> HTTP_TEST + var)
            .useAttributeForId(HTTP_TEST + b, HTTP_TEST + ID, (var) -> HTTP_TEST + var)
            .build());

    }

    @Test
    public void attributeForIdWithNs() throws Exception {

        final String a = "http://example.com/A";
        final String b = "http://example2.com/B";

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .useAttributeForId(a, "http://example.com/" + ID, (var) -> HTTP_TEST + var)
            .useAttributeForId(b, "http://example2.com/" + ID, (var) -> HTTP_TEST + var)
            .build());

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .useAttributeForId(a, "http://example.com/" + ID, (var) -> HTTP_TEST + var)
            .useAttributeForId(b, "http://example2.com/" + ID, (var) -> HTTP_TEST + var)
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .useAttributeForId(a, "http://example.com/" + ID, (var) -> HTTP_TEST + var)
            .useAttributeForId(b, "http://example2.com/" + ID, (var) -> HTTP_TEST + var)
            .build());
    }

    @Test
    public void attributeForId2() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .useAttributeForId(null, "http://example.com/" + ID, (var) -> HTTP_TEST + var)
            .useAttributeForId(null, "http://example2.com/" + ID, (var) -> HTTP_TEST + var)

            .build());

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .useAttributeForId(null, "http://example.com/" + ID, (var) -> HTTP_TEST + var)
            .useAttributeForId(null, "http://example2.com/" + ID, (var) -> HTTP_TEST + var)
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .useAttributeForId(null, "http://example.com/" + ID, (var) -> HTTP_TEST + var)
            .useAttributeForId(null, "http://example2.com/" + ID, (var) -> HTTP_TEST + var)
            .build());
    }

    @Test
    public void classMapping() throws Exception {


        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .overrideNamespace(HTTP_TEST)
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .renameElement(HTTP_TEST + "A", "http://hurra/A2")
            .renameElement(HTTP_TEST + "B", "http://hurra/B2")
            .build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .overrideNamespace(HTTP_TEST)
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .renameElement(HTTP_TEST + "A", "http://hurra/A2")
            .renameElement(HTTP_TEST + "B", "http://hurra/B2")
            .build());
        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .overrideNamespace(HTTP_TEST)
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .renameElement(HTTP_TEST + "A", "http://hurra/A2")
            .renameElement(HTTP_TEST + "B", "http://hurra/B2")
            .build());

        testFast(Builder.getFastBuilder()
            .overrideNamespace(HTTP_TEST)
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .renameElement(HTTP_TEST + "A", "http://hurra/A2")
            .renameElement(HTTP_TEST + "B", "http://hurra/B2")
            .build());

    }

    @Test
    public void classMappingWithFunction() throws Exception {


        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .overrideNamespace(HTTP_TEST)
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .renameElement(HTTP_TEST + "A", "http://hurra/A2")
            .renameElement(null, (u, v) -> u + v.toLowerCase())
            .renameElement(HTTP_TEST + "name", (u, v) -> u + v.toUpperCase())
            .build());

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .overrideNamespace(HTTP_TEST)
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .renameElement(HTTP_TEST + "A", "http://hurra/A2")
            .renameElement(null, (u, v) -> u + v.toLowerCase())
            .renameElement(HTTP_TEST + "name", (u, v) -> u + v.toUpperCase())
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .overrideNamespace(HTTP_TEST)
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .renameElement(HTTP_TEST + "A", "http://hurra/A2")
            .renameElement(null, (u, v) -> u + v.toLowerCase())
            .renameElement(HTTP_TEST + "name", (u, v) -> u + v.toUpperCase())
            .build());

        testFast(Builder.getFastBuilder()
            .overrideNamespace(HTTP_TEST)
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .renameElement(HTTP_TEST + "A", "http://hurra/A2")
            .renameElement(null, (u, v) -> u + v.toLowerCase())
            .renameElement(HTTP_TEST + "name", (u, v) -> u + v.toUpperCase())
            .build());


    }

    @Test
    public void detectLiteralProperties() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void detectLiteralPropertiesWithAttributes() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void detectLiteralPropertiesWithAttributesEdgeCaseWithRdfType() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void detectLiteralPropertiesEdgeCaseWithoutParent() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    //TODO: make test for unmixed content
    @Test
    public void detectLiteralPropertiesEdgeCaseWithoutParent2() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

//        testFast(Builder.getFastBuilder().overrideNamespace("http://test/").simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void detectLiteralPropertiesWithAttributesEdgeCaseWithoutParent() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void tesAautoDetectLiteralPropertiesSetToFalse() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.expand).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.expand).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.expand).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.expand).build());

    }

    @Test
    public void withEmptyElements() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void withSpecialAmp() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace("http://test/").simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void attrValueTransform() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST)
            .transformAttributeValue(null, null, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST)
            .transformAttributeValue(null, null, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST)
            .transformAttributeValue(null, null, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST)
            .transformAttributeValue(null, null, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void attrValueTransform2() throws Exception {

        final String hurrah = HTTP_TEST + "hurra";

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST)
            .transformAttributeValue(HTTP_TEST + ELEMENT_NAME, hurrah, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST)
            .transformAttributeValue(HTTP_TEST + ELEMENT_NAME, hurrah, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST)
            .transformAttributeValue(HTTP_TEST + ELEMENT_NAME, hurrah, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST)
            .transformAttributeValue(HTTP_TEST + ELEMENT_NAME, hurrah, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void attrValueTransform3() throws Exception {

        final String hurrah = HTTP_TEST + "hurra";

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST)
            .transformAttributeValue(null, hurrah, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST)
            .transformAttributeValue(null, hurrah, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST)
            .transformAttributeValue(null, hurrah, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST)
            .transformAttributeValue(null, hurrah, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void attrValueTransform4() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST)
            .transformAttributeValue(HTTP_TEST + ELEMENT_NAME, null, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST)
            .transformAttributeValue(HTTP_TEST + ELEMENT_NAME, null, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST)
            .transformAttributeValue(HTTP_TEST + ELEMENT_NAME, null, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST)
            .transformAttributeValue(HTTP_TEST + ELEMENT_NAME, null, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void attrValueTransform5() throws Exception {

        final String test = HTTP_TEST + "test";
        final String lala = "lala";
        final String dada = "dada";

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST)
            .transformAttributeValue(HTTP_TEST + ELEMENT_NAME, null, (val) -> val.replaceAll(LL, QQ))
            .transformAttributeValue(null, test, (val) -> val.replaceAll(lala, dada))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST)
            .transformAttributeValue(HTTP_TEST + ELEMENT_NAME, null, (val) -> val.replaceAll(LL, QQ))
            .transformAttributeValue(null, test, (val) -> val.replaceAll(lala, dada))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST)
            .transformAttributeValue(HTTP_TEST + ELEMENT_NAME, null, (val) -> val.replaceAll(LL, QQ))
            .transformAttributeValue(null, test, (val) -> val.replaceAll(lala, dada))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST)
            .transformAttributeValue(HTTP_TEST + ELEMENT_NAME, null, (val) -> val.replaceAll(LL, QQ))
            .transformAttributeValue(null, test, (val) -> val.replaceAll(lala, dada))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

    }


    @Test
    public void elementValueTransform() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST)
            .transformElementValue(null,  (val) -> val.toUpperCase())
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST)
            .transformElementValue(null,  (val) -> val.toUpperCase())
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST)
            .transformElementValue(null,  (val) -> val.toUpperCase())
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST)
            .transformElementValue(null,  (val) -> val.toUpperCase())
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    @Ignore // not supported yet
    public void elementValueTransformMixedContent() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST)
            .transformElementValue(null,  (val) -> val.toUpperCase())
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST)
            .transformElementValue(null,  (val) -> val.toUpperCase())
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST)
            .transformElementValue(null,  (val) -> val.toUpperCase())
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST)
            .transformElementValue(null,  (val) -> val.toUpperCase())
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void specialShallowHandling() throws Exception {

        final String ø = "ø";
        final String æ = "æ";


        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .transformAttributeValue(null, RDF_NODE_ID,
                (val) -> val.replaceAll("%C3%B8", ø).replaceAll("%C3%A6", æ))
            .useAttributeForId(null, RDF_NODE_ID, (val) -> "http://example.com/" + val)
            .build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .transformAttributeValue(null, RDF_NODE_ID,
                (val) -> val.replaceAll("%C3%B8", ø).replaceAll("%C3%A6", æ))
            .useAttributeForId(null, RDF_NODE_ID, (val) -> "http://example.com/" + val)
            .build());
        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .transformAttributeValue(null, RDF_NODE_ID,
                (val) -> val.replaceAll("%C3%B8", ø).replaceAll("%C3%A6", æ))
            .useAttributeForId(null, RDF_NODE_ID, (val) -> "http://example.com/" + val)
            .build());
    }

    @Test
    public void skosFromLexaurus() throws Exception {
        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .useAttributeForId(null, RDF_NODE_ID, (var) -> HTTP_TEST + var)
            .convertComplexElementsWithOnlyAttributesToPredicate(true).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .useAttributeForId(null, RDF_NODE_ID, (var) -> HTTP_TEST + var)
            .convertComplexElementsWithOnlyAttributesToPredicate(true).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .useAttributeForId(null, RDF_NODE_ID, (var) -> HTTP_TEST + var)
            .convertComplexElementsWithOnlyAttributesToPredicate(true).build());
    }

    @Test
    public void convertShallowElementsToPropertiesWithAutoDetectLiteralProperties() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .build());
        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .build());
    }


    @Test
    public void insertPropertyBetween() throws Exception {

        final String hasB = "http://a/hasB";
        final String a = "http://a/A";
        final String aB = "http://a/B";

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .insertPredicate(hasB).between(a, aB)
            .build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .insertPredicate(hasB).between(a, aB)
            .build());
        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .insertPredicate(hasB).between(a, aB)
            .build());

    }

    @Test
    public void insertPropertyBetweenWildcard() throws Exception {

        final String hasB = "http://example.org/hasB";
        final String hasC = "http://example.org/hasC";
        final String hasD = "http://example.org/hasD";

        final String A = "http://example.org/A";
        final String B = "http://example.org/B";
        final String C = "http://example.org/C";

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .insertPredicate(hasB).betweenAnyParentAndSpecificChild(B)
            .insertPredicate(hasC).betweenSpecificParentAndAnyChild(A)
            .insertPredicate(hasD).betweenAny()
            .build());


//        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
//            .convertComplexElementsWithOnlyAttributesToPredicate(true)
//            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
//            .insertPredicate(hasB).between(a, aB)
//            .build());
//        testAdvancedJena(Builder.getAdvancedBuilderJena()
//            .convertComplexElementsWithOnlyAttributesToPredicate(true)
//            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
//            .insertPredicate(hasB).between(a, aB)
//            .build());

    }

    @Test(expected = RuntimeException.class)
    public void insertPropertyBetween2() throws Exception {

        Builder.getAdvancedBuilderJena()
            .insertPredicate("http://a/hasB").between("http://a/A", "http://a/B")
            .insertPredicate("http://a/hasC").between("http://a/A", "http://a/B");

    }


    @Test()
    public void insertPropertyBetweenWithShallow() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(true)
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .insertPredicate("http://example.org/hasB").between("http://example.org/A", "http://example.org/B")
            .build()
        );
    }

    @Test
    public void invertPropertyBetween() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .insertPredicate("http://a/belongsToA").between("http://a/A", "http://a/B")
            .invertPredicate("http://a/belongsToA").between("http://a/A", "http://a/B")
            .build());

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .insertPredicate("http://a/belongsToA").between("http://a/A", "http://a/B")
            .invertPredicate("http://a/belongsToA").between("http://a/A", "http://a/B")
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .insertPredicate("http://a/belongsToA").between("http://a/A", "http://a/B")
            .invertPredicate("http://a/belongsToA").between("http://a/A", "http://a/B")
            .build());

    }

    @Test
    public void autoTypedLiterals() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .autoTypeLiterals(true)

            .build());

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .autoTypeLiterals(true)

            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .autoTypeLiterals(true)

            .build());

    }

    @Test
    public void longLiteral() throws ParserConfigurationException, SAXException, IOException {

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .overrideNamespace("http://example.org")
            .build());

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .overrideNamespace("http://example.org")
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .overrideNamespace("http://example.org")
            .build());

        testFast(Builder.getFastBuilder()
            .overrideNamespace("http://example.org")
            .build());
    }

    @Test
    public void specifyDatatype() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .setDatatype("http://a/num", XMLSchema.INTEGER)
            .setDatatype("http://a/date", XMLSchema.DATE)
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .setDatatype("http://a/num", XSDDatatype.XSDinteger)
            .setDatatype("http://a/date", XSDDatatype.XSDdate)
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .setDatatype("http://a/num", XMLSchema.INTEGER.toString())
            .setDatatype("http://a/date", XMLSchema.DATE.toString())
            .build());

    }

    @Test
    public void specifyDatatype2() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(false)
            .convertComplexElementsWithOnlyAttributesToPredicate(false)
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .setDatatype("http://a/num", XMLSchema.INTEGER)
            .setDatatype("http://a/date", XMLSchema.DATE)
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(false)
            .convertComplexElementsWithOnlyAttributesToPredicate(false)
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .setDatatype("http://a/num", XSDDatatype.XSDinteger)
            .setDatatype("http://a/date", XSDDatatype.XSDdate)
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(false)
            .convertComplexElementsWithOnlyAttributesToPredicate(false)
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .setDatatype("http://a/num", XMLSchema.INTEGER.toString())
            .setDatatype("http://a/date", XMLSchema.DATE.toString())
            .build());

    }

    @Test
    public void literalOnPropertyTests() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, SimpleValueFactory.getInstance().createIRI(HTTP_TEST))
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, NodeFactory.createURI(HTTP_TEST))
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, HTTP_TEST)
            .build());

    }

    @Test
    public void literalOnPropertyShallowTests() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, SimpleValueFactory.getInstance().createIRI(HTTP_TEST))
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, NodeFactory.createURI(HTTP_TEST))
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, HTTP_TEST)
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .build());

    }

    @Test
    public void literalOnPropertyTestWithAutoDetectLiteral() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, SimpleValueFactory.getInstance().createIRI(HTTP_TEST))
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, NodeFactory.createURI(HTTP_TEST))
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, HTTP_TEST)
            .build());

    }

    @Test
    public void literalOnPropertyNullPointer() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, SimpleValueFactory.getInstance().createIRI(HTTP_TEST))
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, NodeFactory.createURI(HTTP_TEST))
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, HTTP_TEST)
            .build());

    }

    @Test
    public void qnameInAttributeValue() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .resolveAsQnameInAttributeValue(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .resolveAsQnameInAttributeValue(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .resolveAsQnameInAttributeValue(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .build());

    }


    @Test
    public void xsiTypeSupport() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .xsiTypeSupport(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .xsiTypeSupport(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .xsiTypeSupport(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .build());

    }

    @Test
    public void complexTransformStart() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .addComplexElementTransformAtStartOfElement("http://example.org/B", element -> {
                if (element.hasChild.size() == 0) {
                    element.setType("http://example.org/HELLO");

                }
            })
            .build());


        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .addComplexElementTransformAtStartOfElement("http://example.org/B", element -> {
                if (element.hasChild.size() == 0) {
                    element.setType("http://example.org/HELLO");

                }
            })
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .addComplexElementTransformAtStartOfElement("http://example.org/B", element -> {
                if (element.hasChild.size() == 0) {
                    element.setType("http://example.org/HELLO");

                }
            })
            .build());

    }

    @Test
    public void complexTransformEnd() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .addComplexElementTransformAtEndOfElement("http://example.org/B", element -> {
                if (element.hasChild.size() > 0) {
                    element.setType("http://example.org/HELLO");

                }
            })
            .build());


        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .addComplexElementTransformAtEndOfElement("http://example.org/B", element -> {
                if (element.hasChild.size() > 0) {
                    element.setType("http://example.org/HELLO");

                }
            })
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .addComplexElementTransformAtEndOfElement("http://example.org/B", element -> {
                if (element.hasChild.size() > 0) {
                    element.setType("http://example.org/HELLO");

                }
            })
            .build());

    }


    @Test
    public void setQueueSize() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .setBuffer(1)
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .setBuffer(1)
            .build());

    }

    @Test
    public void skipElement() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .skipElement("http://example.org/B")
            .build());


        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .skipElement("http://example.org/B")
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .skipElement("http://example.org/B")
            .build());
    }
    @Test
    public void mapAttributeTextToResource() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .mapTextInAttributeToUri("http://example.org/b", "http://example.org/c", "d", SimpleValueFactory.getInstance().createIRI("http://http://example.org/c"))
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .mapTextInAttributeToUri("http://example.org/b", "http://example.org/c", "d", NodeFactory.createURI("http://http://example.org/c"))
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .mapTextInAttributeToUri("http://example.org/b", "http://example.org/c", "d", "http://http://example.org/c")
            .build());
    }


    @Test
    public void mixedContentSpecial() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .build());


        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .build());
    }

    @Test
    public void mixedContentSpecial2() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            //.forceMixedContent("http://example.org/test")
            .build());


        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .build());
    }

    @Test
    public void forcedMixedContent() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .forceMixedContent("http://example.org/name")
            .forceMixedContent("http://example.org/name2")
            .renameElement("http://example.org/name3","http://example.org/name2")
            .build());


        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .forceMixedContent("http://example.org/name")
            .forceMixedContent("http://example.org/name2")
            .renameElement("http://example.org/name3","http://example.org/name2")

            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .forceMixedContent("http://example.org/name")
            .forceMixedContent("http://example.org/name2")
            .renameElement("http://example.org/name3","http://example.org/name2")

            .build());
    }

    @Test
    public void forcedMixedContentWithShallow() throws Exception {

        String xmlNameSpace = "http://example.org/";

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .setBaseNamespace(xmlNameSpace, Builder.AppliesTo.bothElementsAndAttributes)
            .forceMixedContent(xmlNameSpace+"b").build());

    }


    @Test
    public void compositeId() throws Exception {


        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .compositeId("http://example.org/B")
                .fromElement("http://example.org/num")
                .fromElement("http://example.org/name")
                .fromAttribute("http://example.org/localId")
                .mappedTo((elementMap, attributeMap) ->
                    "http://data.org/"+elementMap.get("http://example.org/num")+elementMap.get("http://example.org/name")+attributeMap.get("http://example.org/localId"))


            .build());

    }

    @Test
    public void compositeIdWithDelayedOutput() throws Exception {


        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .compositeId("http://example.org/B")
            .fromElement("http://example.org/num")
            .fromElement("http://example.org/name")
            .fromAttribute("http://example.org/localId")
            .mappedTo((elementMap, attributeMap) ->
                "http://data.org/"+elementMap.get("http://example.org/num")+elementMap.get("http://example.org/name")+attributeMap.get("http://example.org/localId"))

            .addComplexElementTransformAtEndOfElement(
                "http://example.org/notForCompositeId",
                e ->{
                    Element sensitiveElement = new Element(e.getHandler(), e.getBuilder());
                    sensitiveElement.setHasValue("newValue");
                    sensitiveElement.setType("http://example.org/newElement");
                    sensitiveElement.uri = "_:" + UUID.randomUUID().toString();
                    sensitiveElement.parent = e.parent;
                    e.parent.addDelayedTripleCreation(sensitiveElement);
                })

            .addComplexElementTransformAtEndOfElement(
                "http://example.org/newElement",
                e ->{
                    Element sensitiveElement = new Element(e.getHandler(), e.getBuilder());
                    sensitiveElement.setHasValue("newValue2");
                    sensitiveElement.setType("http://example.org/newNewElement");
                    sensitiveElement.uri = "_:" + UUID.randomUUID().toString();
                    sensitiveElement.parent = e.parent;
                    e.parent.addDelayedTripleCreation(sensitiveElement);
                })


            .build());

    }


    @Test
    public void mapToUriWithFunction() throws Exception {


        testAdvancedSesame(Builder.getAdvancedBuilderSesame()

            .mapTextInElementToUri("http://", "", RDF.ALT)
            .mapTextInElementToUri("http://example.org/b", string -> RDF.TYPE)

            .build());

    }


    @Test
    public void compositeIdWithDelayedOutputAndParentIdAndIndex() throws Exception {


        testAdvancedSesame(Builder.getAdvancedBuilderSesame()

            .addIndex(true)

            .compositeId("http://example.org/B")
            .fromElement("http://example.org/num")
            .fromElement("http://example.org/name")
            .fromAttribute("http://example.org/localId")
            .elementIndex()
            .mappedTo((elementMap, attributeMap) ->
                "http://data.org/"+elementMap.get("http://example.org/num")+elementMap.get("http://example.org/name")+attributeMap.get("http://example.org/localId")+"/"+elementMap.get("http://acandonorway.github.com/XmlToRdf/ontology.ttl#elementIndex"))

            .addComplexElementTransformAtEndOfElement(
                "http://example.org/notForCompositeId",
                e ->{
                    Element sensitiveElement = new Element(e.getHandler(), e.getBuilder());
                    sensitiveElement.setHasValue("newValue");
                    sensitiveElement.setType("http://example.org/newElement");
                    sensitiveElement.uri = "_:" + UUID.randomUUID().toString();
                    sensitiveElement.parent = e.parent;
                    e.parent.addDelayedTripleCreation(sensitiveElement);
                })

            .addComplexElementTransformAtEndOfElement(
                "http://example.org/newElement",
                e ->{
                    Element sensitiveElement = new Element(e.getHandler(), e.getBuilder());
                    sensitiveElement.setHasValue("newValue2");
                    sensitiveElement.setType("http://example.org/newNewElement");
                    sensitiveElement.uri = "_:" + UUID.randomUUID().toString();
                    sensitiveElement.parent = e.parent;
                    e.parent.addDelayedTripleCreation(sensitiveElement);
                })

            .compositeId("http://example.org/inner")
            .elementIndex()
            .parentId()
            .mappedTo((elementMap, attributeMap) ->
                "http://data.org/__"+elementMap.get(XmlToRdfVocabulary.parentId)+"__/"+elementMap.get(XmlToRdfVocabulary.index)+"/"+elementMap.get(XmlToRdfVocabulary.elementIndex))


            .build());

    }

	@Test
	public void compositeIdBasedOnParentsId() throws Exception {


		testAdvancedSesame(Builder.getAdvancedBuilderSesame()


			.compositeId("http://example.org/B")
			.fromElement("http://example.org/num")

			.fromParent("http://example.org/id").as("parent.id")

			.mappedTo((elementMap, attributeMap) -> "http://example.com/"+elementMap.get("http://example.org/num")+"/"+elementMap.get("parent.id"))

			.build());

	}

    @Test
    public void compositeIdBasedOnParentsIdWithRename() throws Exception {


        testAdvancedSesame(Builder.getAdvancedBuilderSesame()

            .renameElement("http://example.org/id", "http://example.org/renamedId")

            .compositeId("http://example.org/B")
            .fromElement("http://example.org/num")

            .fromParent("http://example.org/renamedId").as("parent.id")

            .mappedTo((elementMap, attributeMap) -> "http://example.com/"+elementMap.get("http://example.org/num")+"/"+elementMap.get("parent.id"))

            .build());

    }

    @Test(expected = RuntimeException.class)
    public void compositeIdBasedOnParentsIdCanNotResolveInTime() throws Exception {


        testAdvancedSesame(Builder.getAdvancedBuilderSesame()


            .compositeId("http://example.org/B")
            .fromElement("http://example.org/num")

            .fromParent("http://example.org/id3").as("parent.id")

            .mappedTo((elementMap, attributeMap) -> "http://example.com/"+elementMap.get("http://example.org/num")+"/"+elementMap.get("parent.id"))

            .build());

    }

    @Test
    public void killOnError() throws Exception{

        int threadCount = Thread.activeCount();

        try{
            testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace("http://example/").build());

        }catch (SAXException e){

        }

        try{
            testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace("http://example/").build());

        }catch (SAXException e){

        }

        try{
            testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace("http://example/").build());

        }catch (SAXException e){

        }

        assertEquals("Thread count should not increase!", threadCount, Thread.activeCount());

    }


    private void testAdvancedJena(XmlToRdfAdvancedJena build) throws IOException, ParserConfigurationException, SAXException {
        TestFiles testFiles = getTestFiles();

        Model actualModelJena = build.convertToDataset(new FileInputStream(testFiles.xml)).getDefaultModel();

        Model expectedModel = FileManager.get().readModel(ModelFactory.createDefaultModel(), testFiles.expected.getCanonicalPath());


        if (!expectedModel.isIsomorphicWith(actualModelJena)) {
            try {
                assertEquals("Not isomorphic for object method with jena dataset.", modelToString(expectedModel), modelToString(actualModelJena));

            } catch (AssertionError error) {
                collector.addError(error);
            }
        }

    }

    private void testFast(XmlToRdfFast build) throws IOException, ParserConfigurationException, SAXException {

        TestFiles testFiles = getTestFiles();

        FileOutputStream out = new FileOutputStream(testFiles.path + "/actualFast.ttl");
        build.convertToStream(new FileInputStream(testFiles.xml), out);
        out.close();
        Model actualModel = FileManager.get().readModel(ModelFactory.createDefaultModel(), testFiles.path + "/actualFast.ttl");

        Model expectedModel = FileManager.get().readModel(ModelFactory.createDefaultModel(), testFiles.expected.getCanonicalPath());
        boolean isomorphicWith = expectedModel.isIsomorphicWith(actualModel);

        if (!isomorphicWith) {

            try {
                assertEquals("Not isomorphic for fast method.", modelToString(expectedModel), modelToString(actualModel));

            } catch (AssertionError error) {
                collector.addError(error);
            }
        }

    }

    private void testAdvancedStream(XmlToRdfAdvancedStream build) throws IOException, ParserConfigurationException, SAXException {

        TestFiles testFiles = getTestFiles();

        build.convertToStream(new FileInputStream(testFiles.xml), new FileOutputStream(testFiles.path + "/actualObject.n3"));

        Model actualModel = FileManager.get().readModel(ModelFactory.createDefaultModel(), testFiles.path + "/actualObject.n3");

        Model expectedModel = FileManager.get().readModel(ModelFactory.createDefaultModel(), testFiles.expected.getCanonicalPath());

        if (!expectedModel.isIsomorphicWith(actualModel)) {
            try {
                assertEquals("Not isomorphic for object method.", modelToString(expectedModel), modelToString(actualModel));

            } catch (AssertionError error) {
                collector.addError(error);
            }
        }

    }

    private void testAdvancedSesame(XmlToRdfAdvancedSesame build) throws IOException, ParserConfigurationException, SAXException {
        TestFiles testFiles = getTestFiles();

        Repository repository = build.convertToRepository(new FileInputStream(testFiles.xml));

        String rdf = repositoryToString(repository, RDFFormat.JSONLD);
        Model actualModelSesame = ModelFactory.createDefaultModel().read(new ByteArrayInputStream(rdf.getBytes("UTF-8")), "", RDFLanguages.strLangJSONLD);

        Model expectedModel = FileManager.get().readModel(ModelFactory.createDefaultModel(), testFiles.expected.getCanonicalPath());

        if (!expectedModel.isIsomorphicWith(actualModelSesame)) {
            try {
                assertEquals("Not isomorphic for object method with sesame repository.", modelToString(expectedModel), modelToString(actualModelSesame));

            } catch (AssertionError error) {
                collector.addError(error);
            }
        }
    }

    private class TestFiles {
        String path;
        File xml;
        File expected;

        public TestFiles(String path, File xml, File expected) {
            this.path = path;
            this.xml = xml;
            this.expected = expected;
        }
    }

    private TestFiles getTestFiles() throws IOException {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String methodName = stackTrace[3].getMethodName();
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

        return new TestFiles(path, xml, expected);

    }

    private String modelToString(Model createdModel) {

        createdModel.setNsPrefix("xmlToRdf", "http://acandonorway.github.com/XmlToRdf/ontology.ttl#");
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
            RepositoryResult<Namespace> namespaces = connection.getNamespaces();

            while (namespaces.hasNext()) {
                Namespace next = namespaces.next();
                writer.handleNamespace(next.getPrefix(), next.getName());
            }

            RepositoryResult<Statement> statements = connection.getStatements(null, null, null);
            while (statements.hasNext()) {
                writer.handleStatement(statements.next());
            }
        }
        writer.endRDF();

        return stringWriter.toString();
    }
}