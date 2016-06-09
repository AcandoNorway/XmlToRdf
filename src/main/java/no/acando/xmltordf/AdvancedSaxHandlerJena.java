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

import org.apache.jena.atlas.io.NullOutputStream;
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

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;


public class AdvancedSaxHandlerJena extends AdvancedSaxHandler<Node, RDFDatatype> {

    private static final Node RDF_REST = NodeFactory.createURI(RDF.REST.toString());
    private static final Node RDF_FIRST = NodeFactory.createURI(RDF.FIRST.toString());
    private static final Node RDF_NIL = NodeFactory.createURI(RDF.NIL.toString());
    private Graph graph;

    Dataset dataset;
    private ArrayBlockingQueue<Triple> queue;
    private boolean notDone = true;
    private Thread jenaThread;

    private final Triple EndOfFileTriple = new Triple(NodeFactory.createURI(EndOfFile), NodeFactory.createURI(EndOfFile), NodeFactory.createURI(EndOfFile));

    public AdvancedSaxHandlerJena(Builder.AdvancedJena builder) {
        super(new NullOutputStream(), builder);

        queue = new ArrayBlockingQueue<>(builder.buffer, false);
        dataset = DatasetFactory.createMem();
        graph = dataset.getDefaultModel().getGraph();

        this.builder =  builder;
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
                        //TODO: handle this or throw it up the stack
                    }

                }

            }
        };

        jenaThread.start();
    }


    //TODO: this always returns null, what up?
    public String createTriple(String subject, String predicate, String object) {

        Node predicateNode = NodeFactory.createURI(predicate);
        Node subjectNode = null;
        Node objectNode = null;

        subjectNode = getNode(subject);
        objectNode = getNode(object);

        addTripleToQueue(predicateNode, subjectNode, objectNode);

        return null;

    }



    //TODO: this always returns null, what up?
    public String createTriple(String subject, String predicate, Node objectNode) {

        Node predicateNode = NodeFactory.createURI(predicate);
        Node subjectNode = null;

        subjectNode = getNode(subject);
        addTripleToQueue(predicateNode, subjectNode, objectNode);

        return null;

    }


    //TODO: this always returns null, what up?
    public String createTripleLiteral(String subject, String predicate, String objectLiteral) {
        if (objectLiteral == null) {
            return null;
        }

        Node predicateNode = NodeFactory.createURI(predicate);
        Node subjectNode = null;

        subjectNode = getNode(subject);

        Node literal = NodeFactory.createLiteral(objectLiteral, null, false);

        addTripleToQueue(predicateNode, subjectNode, literal);

        return null;

    }

    //TODO: this always returns null, what up?
    public String createTripleLiteral(String subject, String predicate, String objectLiteral, RDFDatatype datatype) {
        if (objectLiteral == null) {
            return null;
        }

        Node predicateNode = NodeFactory.createURI(predicate);
        Node subjectNode = null;

        subjectNode = getNode(subject);

        Node literal = NodeFactory.createLiteral(objectLiteral, datatype);

        addTripleToQueue(predicateNode, subjectNode, literal);

        return null;

    }

    //TODO: this always returns null, what up?
    public String createTripleLiteral(String subject, String predicate, long objectLong) {

        Node predicateNode = NodeFactory.createURI(predicate);
        Node subjectNode = null;

        subjectNode = getNode(subject);
        Node literal = NodeFactory.createLiteral(objectLong + "", XSDDatatype.XSDlong);

        addTripleToQueue(predicateNode, subjectNode, literal);

        return null;

    }

    public String createList(String subject, String predicate, List<Object> mixedContent) {

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

                    if (objectElement.getUri().startsWith(BLANK_NODE_PREFIX)) {
                        return NodeFactory.createBlankNode(objectElement.getUri());
                    } else {
                        return NodeFactory.createURI(objectElement.getUri());
                    }

                } else {
                    throw new IllegalStateException("Unknown type of: " + content.getClass().toString());
                }

            })
            .forEachOrdered(value -> {
                Node blankNode = NodeFactory.createBlankNode();

                if (head[0] == null) {
                    head[0] = blankNode;
                    try {
                        queue.put(new Triple(head[0], RDF_FIRST, value));
                    } catch (InterruptedException e) {
                        //TODO: handle or throw up the stack
                        e.printStackTrace();
                    }

                } else {
                    try {
                        queue.put(new Triple(temporaryNode[0], RDF_REST, blankNode));
                        queue.put(new Triple(blankNode, RDF_FIRST, value));

                    } catch (InterruptedException e) {
                        //TODO: handle or throw up the stack
                        e.printStackTrace();
                    }

                }

                temporaryNode[0] = blankNode;

            });

        try {
            queue.put(new Triple(temporaryNode[0], RDF_REST, RDF_NIL));
            queue.put(new Triple(subjectNode, predicateNode, head[0]));

        } catch (InterruptedException e) {
            //TODO: handle or throw up the stack
            e.printStackTrace();
        }

        return null;
    }

    private void addTripleToQueue(Node predicateNode, Node subjectNode, Node objectNode) {
        Triple triple = new Triple(subjectNode, predicateNode, objectNode);
        try {
            queue.put(triple);
        } catch (InterruptedException e) {
            //TODO: handle this or throw it up the stack
            e.printStackTrace();
        }
    }

    private Node getNode(String subject) {
        if (!subject.startsWith(BLANK_NODE_PREFIX)) {
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
            //TODO: handle or throw up the stack
            e.printStackTrace();
        }
    }

}
