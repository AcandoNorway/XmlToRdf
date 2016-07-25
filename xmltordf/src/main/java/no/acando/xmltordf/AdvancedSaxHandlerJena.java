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

package no.acando.xmltordf;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphWithPerform;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.openrdf.model.vocabulary.RDF;
import org.xml.sax.SAXException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;


final class AdvancedSaxHandlerJena extends AdvancedSaxHandler<Node, RDFDatatype> {

    private static final Node RDF_REST = NodeFactory.createURI(RDF.REST.toString());
    private static final Node RDF_FIRST = NodeFactory.createURI(RDF.FIRST.toString());
    private static final Node RDF_NIL = NodeFactory.createURI(RDF.NIL.toString());
    private Graph graph;

    Dataset dataset;
    private ArrayBlockingQueue<Triple> queue;
    private boolean notDone = true;
    private Thread jenaThread;

    private final Triple EndOfFileTriple = new Triple(NodeFactory.createURI(XmlToRdfVocabulary.EndOfFile), NodeFactory.createURI(XmlToRdfVocabulary.EndOfFile), NodeFactory.createURI(XmlToRdfVocabulary.EndOfFile));

    AdvancedSaxHandlerJena(Builder.AdvancedJena builder) {
        super(builder);

        queue = new ArrayBlockingQueue<>(builder.buffer, false);
        dataset = DatasetFactory.createMem();
        graph = dataset.getDefaultModel().getGraph();

        this.builder = builder;
        Thread thread = Thread.currentThread();
        jenaThread = new Thread() {
            @Override
            public void run() {

                while (notDone || !queue.isEmpty()) {
                    try {
                        Triple take = queue.take();
                        if (take != EndOfFileTriple) {
                            ((GraphWithPerform) graph).performAdd(take);
                        }

                    } catch (InterruptedException e) {
                        // print and ignore
                        System.out.println(e.getMessage());
                    }

                }

                prefixUriMap.forEach(dataset.getDefaultModel()::setNsPrefix);
                dataset.getDefaultModel().setNsPrefix("xsd", XSD);
                dataset.getDefaultModel().setNsPrefix("xmlTodRdf", "http://acandonorway.github.com/XmlToRdf/ontology.ttl#");

            }
        };

        jenaThread.start();
    }


    final public void createTriple(String subject, String predicate, String object) {

        Node predicateNode = NodeFactory.createURI(predicate);
        Node subjectNode = null;
        Node objectNode = null;

        subjectNode = getNode(subject);
        objectNode = getNode(object);

        addTripleToQueue(subjectNode, predicateNode, objectNode);


    }


    final public void createTriple(String subject, String predicate, Node objectNode) {

        Node predicateNode = NodeFactory.createURI(predicate);
        Node subjectNode = null;

        subjectNode = getNode(subject);
        addTripleToQueue(subjectNode, predicateNode, objectNode);


    }


    final public void createTripleLiteral(String subject, String predicate, String objectLiteral) {
        if (objectLiteral == null) {
            return;
        }

        Node predicateNode = NodeFactory.createURI(predicate);
        Node subjectNode = null;

        subjectNode = getNode(subject);

        Node literal = null;
        if (builder.autoTypeLiterals) {

            try {
                Integer.parseInt(objectLiteral);
                literal = NodeFactory.createLiteral(objectLiteral, XSDDatatype.XSDinteger);
            } catch (NumberFormatException e) {
                try {
                    Double.parseDouble(objectLiteral);
                    literal = NodeFactory.createLiteral(objectLiteral, XSDDatatype.XSDdecimal);
                } catch (NumberFormatException e2) {
                    try {
                        LocalDateTime.parse(objectLiteral, DateTimeFormatter.ISO_DATE_TIME);
                        literal = NodeFactory.createLiteral(objectLiteral, XSDDatatype.XSDdateTime);
                    } catch (DateTimeParseException e3) {
                        try {
                            LocalDate.parse(objectLiteral, DateTimeFormatter.ISO_DATE);
                            literal = NodeFactory.createLiteral(objectLiteral, XSDDatatype.XSDdate);
                        } catch (DateTimeParseException e4) {
                            literal = NodeFactory.createLiteral(objectLiteral, null, false);
                        }
                    }
                }
            }
        } else {
            literal = NodeFactory.createLiteral(objectLiteral, null, false);
        }


        addTripleToQueue(subjectNode, predicateNode, literal);


    }


    final public void createTripleLiteral(String subject, String predicate, String objectLiteral, RDFDatatype datatype) {
        if (objectLiteral == null) {
            return;
        }

        Node predicateNode = NodeFactory.createURI(predicate);
        Node subjectNode = null;

        subjectNode = getNode(subject);

        Node literal = NodeFactory.createLiteral(objectLiteral, datatype);

        addTripleToQueue(subjectNode, predicateNode, literal);


    }


    final public void createTripleLiteral(String subject, String predicate, long objectLong) {

        Node predicateNode = NodeFactory.createURI(predicate);
        Node subjectNode = null;

        subjectNode = getNode(subject);
        Node literal = NodeFactory.createLiteral(objectLong + "", XSDDatatype.XSDlong);

        addTripleToQueue(subjectNode, predicateNode, literal);


    }

    final public void createList(String subject, String predicate, List<Object> mixedContent) {

        Node predicateNode = NodeFactory.createURI(predicate);
        Node subjectNode = null;

        subjectNode = getNode(subject);

        final Node[] head = new Node[1];
        final Node[] temporaryNode = new Node[1];

        mixedContent
            .stream()
            .map(content -> {
                if (content instanceof String) {

                    String objectLiteral = (String) content;
                    return NodeFactory.createLiteral(objectLiteral, XSDDatatype.XSDstring);

                } else if (content instanceof Element) {

                    Element objectElement = (Element) content;

                    if (isBlankNode(objectElement.uri)) {
                        return NodeFactory.createBlankNode(objectElement.uri);
                    } else {
                        return NodeFactory.createURI(objectElement.uri);
                    }

                } else {
                    throw new IllegalStateException("Unknown type of: " + content.getClass().toString());
                }

            })
            .forEachOrdered(value -> {
                Node blankNode = NodeFactory.createBlankNode();

                if (head[0] == null) {
                    head[0] = blankNode;

                    addTripleToQueue(head[0], RDF_FIRST, value);

                } else {

                    addTripleToQueue(temporaryNode[0], RDF_REST, blankNode);
                    addTripleToQueue(blankNode, RDF_FIRST, value);

                }

                temporaryNode[0] = blankNode;

            });


        addTripleToQueue(temporaryNode[0], RDF_REST, RDF_NIL);
        addTripleToQueue(subjectNode, predicateNode, head[0]);


    }

    private void addTripleToQueue(Node subjectNode, Node predicateNode, Node objectNode) {
        Triple triple = new Triple(subjectNode, predicateNode, objectNode);
        try {
            queue.put(triple);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static Node getNode(String subject) {
        if (!isBlankNode(subject)) {
            return NodeFactory.createURI(subject);
        } else {
            return NodeFactory.createBlankNode(subject);
        }
    }

    @Override
    public void endDocument() throws SAXException {

        notDone = false;

        try {
            queue.put(EndOfFileTriple);
            jenaThread.join();
        } catch (InterruptedException e) {
            // print and ignore
            System.out.println(e.getMessage());
        }
    }

}
