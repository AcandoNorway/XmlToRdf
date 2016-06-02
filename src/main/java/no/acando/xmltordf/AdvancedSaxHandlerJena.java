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
import org.apache.jena.graph.*;
import org.apache.jena.graph.impl.GraphWithPerform;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.openrdf.model.IRI;
import org.openrdf.model.vocabulary.RDF;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;


public class AdvancedSaxHandlerJena extends AdvancedSaxHandler<RDFDatatype> {


    private static final Node RDF_REST = NodeFactory.createURI(RDF.REST.toString());
    private static final Node RDF_FIRST = NodeFactory.createURI(RDF.FIRST.toString());
    private static final Node RDF_NIL = NodeFactory.createURI(RDF.NIL.toString());
    private Graph g;

    Dataset dataset;
    private ArrayBlockingQueue<Triple> queue;
    private boolean notDone = true;
    private Thread jenaThread;


    private final Triple EndOfFileTriple = new Triple(NodeFactory.createURI(EndOfFile), NodeFactory.createURI(EndOfFile), NodeFactory.createURI(EndOfFile));


    public AdvancedSaxHandlerJena(Builder.AdvancedJena builder) {
        super(new NullOutputStream(), builder);

        queue = new ArrayBlockingQueue<>(builder.buffer, false);
        dataset = DatasetFactory.createMem();
        g = dataset.getDefaultModel().getGraph();

        this.builder =  builder;
        Thread thread = Thread.currentThread();
        jenaThread = new Thread() {
            @Override
            public void run() {

                while (notDone || !queue.isEmpty()) {
                    try {
                        Triple take = queue.take();
                        if (take != EndOfFileTriple) {
                            ((GraphWithPerform) g).performAdd(take);
                        }

                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }

                }

            }
        };

        jenaThread.start();
    }


    public String createTriple(String subject, String predicate, String object) {


        Node predicateNode = NodeFactory.createURI(predicate);
        Node subjectNode = null;
        Node objectNode = null;


        if (!subject.startsWith("_:")) {
            subjectNode = NodeFactory.createURI(subject);

        } else {
            subjectNode = NodeFactory.createBlankNode(subject);

        }

        if (!object.startsWith("_:")) {
            objectNode = NodeFactory.createURI(object);

        } else {
            objectNode = NodeFactory.createBlankNode(object);

        }

        Triple triple = new Triple(subjectNode, predicateNode, objectNode);
        try {
            queue.put(triple);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;

    }

    public String createTripleLiteral(String subject, String predicate, String objectLiteral) {
        if (objectLiteral == null) {
            return null;
        }

        Node predicateNode = NodeFactory.createURI(predicate);
        Node subjectNode = null;


        if (!subject.startsWith("_:")) {
            subjectNode = NodeFactory.createURI(subject);

        } else {
            subjectNode = NodeFactory.createBlankNode(subject);
        }

        Node literal = NodeFactory.createLiteral(objectLiteral, null, false);

        Triple triple = new Triple(subjectNode, predicateNode, literal);
        try {
            queue.put(triple);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;


    }

    public String createTripleLiteral(String subject, String predicate, String objectLiteral, RDFDatatype datatype) {
        if (objectLiteral == null) {
            return null;
        }

        Node predicateNode = NodeFactory.createURI(predicate);
        Node subjectNode = null;

        if (!subject.startsWith("_:")) {
            subjectNode = NodeFactory.createURI(subject);

        } else {
            subjectNode = NodeFactory.createBlankNode(subject);
        }


        Node literal = NodeFactory.createLiteral(objectLiteral, datatype);

        Triple triple = new Triple(subjectNode, predicateNode, literal);
        try {
            queue.put(triple);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;


    }


    public String createTripleLiteral(String subject, String predicate, long objectLong) {


        Node predicateNode = NodeFactory.createURI(predicate);
        Node subjectNode = null;


        if (!subject.startsWith("_:")) {
            subjectNode = NodeFactory.createURI(subject);

        } else {
            subjectNode = NodeFactory.createBlankNode(subject);

        }
        Node literal = NodeFactory.createLiteral(objectLong+"", XSDDatatype.XSDlong);

        Triple triple = new Triple(subjectNode, predicateNode, literal);
        try {
            queue.put(triple);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;


    }

    public String createList(String subject, String predicate, List<Object> mixedContent) {


        Node predicateNode = NodeFactory.createURI(predicate);
        Node subjectNode = null;


        if (!subject.startsWith("_:")) {
            subjectNode = NodeFactory.createURI(subject);

        } else {
            subjectNode = NodeFactory.createBlankNode(subject);
        }

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

                    if (objectElement.getUri().startsWith("_:")) {
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
                        e.printStackTrace();
                    }

                } else {
                    try {
                        queue.put(new Triple(temporaryNode[0], RDF_REST, blankNode));
                        queue.put(new Triple(blankNode, RDF_FIRST, value));

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                temporaryNode[0] = blankNode;


            });

        try {
            queue.put(new Triple(temporaryNode[0], RDF_REST, RDF_NIL));
            queue.put(new Triple(subjectNode, predicateNode, head[0]));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }


//
//        try {
//            queue.put(new Triple(subjectNode, predicateNode, head[0]));
//
//
//            queue.put(new Triple(head[0], RDF_FIRST, collect.get(0)));
//            Node parent = head[0];
//            for (int i = 1; i < collect.size(); i++) {
//                Node blankNode = NodeFactory.createBlankNode();
//                parent = blankNode;
//            }
//
//
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


        return null;
    }

    @Override
    public void endDocument() throws SAXException {

        notDone = false;

        try {
            queue.put(EndOfFileTriple);
            jenaThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
