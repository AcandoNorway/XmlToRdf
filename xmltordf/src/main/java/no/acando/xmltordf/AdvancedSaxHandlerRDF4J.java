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

import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.NotifyingSailConnection;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.BlockingQueue;


final class AdvancedSaxHandlerRDF4J extends AdvancedSaxHandler<IRI, IRI> {

    Repository repository;
    private BlockingQueue<Statement> queue;
    private boolean notDone = true;
    private Thread repoThread;
    final static private SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();

    private static final Statement EndOfFileStatement = SimpleValueFactory.getInstance().createStatement(
        valueFactory.createIRI(XmlToRdfVocabulary.EndOfFile),
        valueFactory.createIRI(XmlToRdfVocabulary.EndOfFile),
        valueFactory.createIRI(XmlToRdfVocabulary.EndOfFile)
    );


    AdvancedSaxHandlerRDF4J(Builder.AdvancedRDF4J builder) {
        super(builder);

        queue = new CustomBlockingQueue<>(builder.buffer);

        MemoryStore memoryStore = new MemoryStore();
        memoryStore.initialize();

        this.builder = builder;
        repoThread = new Thread() {
            @Override
            public void run() {

                NotifyingSailConnection connection = memoryStore.getConnection();
                connection.begin(IsolationLevels.NONE);

                while (notDone || !queue.isEmpty()) {
                    try {
                        Statement take = queue.take();
                        if (take != EndOfFileStatement) {
                            connection.addStatement(take.getSubject(), take.getPredicate(), take.getObject());
                        }

                    } catch (InterruptedException interruptedException) {
                        //print and ignore
                        System.out.println(interruptedException.getMessage());
                    }
                }

                prefixUriMap.forEach(connection::setNamespace);
                connection.setNamespace("xsd", XMLSchema.NAMESPACE);

                connection.commit();

                connection.close();

                repository = new SailRepository(memoryStore);

            }
        };

        repoThread.start();
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {

        notDone = false;
        try {
            queue.put(EndOfFileStatement);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        repoThread.interrupt();

        super.fatalError(e);
    }

    final public void createTriple(String subject, String predicate, String object) {

        IRI predicateNode = valueFactory.createIRI(predicate);
        Resource subjectNode = getResource(subject);
        Resource objectNode = getResource(object);

        addTripleToQueue(subjectNode, predicateNode, objectNode);


    }

    final public void createTriple(String subject, String predicate, IRI objectNode) {

        IRI predicateNode = valueFactory.createIRI(predicate);
        Resource subjectNode = getResource(subject);

        addTripleToQueue(subjectNode, predicateNode, objectNode);

    }

    final public void createTripleLiteral(String subject, String predicate, String objectLiteral, IRI datatype) {
        if (objectLiteral == null) {
            return;
        }

        IRI predicateNode = valueFactory.createIRI(predicate);
        Resource subjectNode = getResource(subject);

        Literal literal = valueFactory.createLiteral(objectLiteral, datatype);

        addTripleToQueue(subjectNode, predicateNode, literal);

    }

    final public void createTripleLiteral(String subject, String predicate, String objectLiteral) {
        if (objectLiteral == null) {
            return;
        }

        IRI predicateNode = valueFactory.createIRI(predicate);
        Resource subjectNode = getResource(subject);
        Literal literal = null;

        if (builder.autoTypeLiterals) {
            try {
                Integer.parseInt(objectLiteral);
                literal = valueFactory.createLiteral(objectLiteral, XMLSchema.INTEGER);
            } catch (NumberFormatException e) {
                try {
                    Double.parseDouble(objectLiteral);
                    literal = valueFactory.createLiteral(objectLiteral, XMLSchema.DECIMAL);
                } catch (NumberFormatException e2) {
                    try {
                        LocalDateTime.parse(objectLiteral, DateTimeFormatter.ISO_DATE_TIME);
                        literal = valueFactory.createLiteral(objectLiteral, XMLSchema.DATETIME);
                    } catch (DateTimeParseException e3) {
                        try {
                            LocalDate.parse(objectLiteral, DateTimeFormatter.ISO_DATE);
                            literal = valueFactory.createLiteral(objectLiteral, XMLSchema.DATE);
                        } catch (DateTimeParseException e4) {
                            literal = valueFactory.createLiteral(objectLiteral);
                        }
                    }
                }
            }
        } else {
            literal = valueFactory.createLiteral(objectLiteral);

        }


        addTripleToQueue(subjectNode, predicateNode, literal);


    }

    final public void createTripleLiteral(String subject, String predicate, long objectLong) {

        IRI predicateNode = valueFactory.createIRI(predicate);
        Resource subjectNode = getResource(subject);

        Literal literal = valueFactory.createLiteral(objectLong);


        addTripleToQueue(subjectNode, predicateNode, literal);


    }

    final public void createList(String subject, String predicate, List<Object> mixedContent) {

        IRI predicateNode = valueFactory.createIRI(predicate);
        Resource subjectNode = getResource(subject);

        final Resource[] head = new Resource[1];
        final Resource[] temporaryNode = new Resource[1];

        mixedContent
            .stream()
            .map(content -> {
                if (content instanceof String) {
                    String objectLiteral = (String) content;

                    return valueFactory.createLiteral(objectLiteral);

                } else if (content instanceof Element) {
                    Element objectElement = (Element) content;
                    if (!objectElement.uri.startsWith("_:")) {
                        return valueFactory.createIRI(objectElement.uri);
                    } else {
                        return valueFactory.createBNode(objectElement.uri);
                    }

                } else {
                    throw new IllegalStateException("Unknown type of: " + content.getClass().toString());
                }
            })
            .forEachOrdered(value -> {
                Resource blankNode = valueFactory.createBNode();

                if (head[0] == null) {
                    head[0] = blankNode;

                    addTripleToQueue(head[0], RDF.FIRST, value);

                } else {

                    addTripleToQueue(temporaryNode[0], RDF.REST, blankNode);
                    addTripleToQueue(blankNode, RDF.FIRST, value);

                }

                temporaryNode[0] = blankNode;

            });


        addTripleToQueue(temporaryNode[0], RDF.REST, RDF.NIL);
        addTripleToQueue(subjectNode, predicateNode, head[0]);


    }

    @Override
    final public void endDocument() throws SAXException {

        notDone = false;

        try {
            queue.put(EndOfFileStatement);
        } catch (InterruptedException interruptedException) {
            throw new RuntimeException(interruptedException);
        }

        try {
            repoThread.join();
        } catch (InterruptedException interruptedException) {
            throw new RuntimeException(interruptedException);
        }
    }

    private static Resource getResource(String subject) {
        if (!isBlankNode(subject)) {
            return valueFactory.createIRI(subject);
        } else {
            return valueFactory.createBNode(subject);
        }
    }


    private void addTripleToQueue(Resource subject, IRI predicate, Value object) {
        try {
            Statement statement = valueFactory.createStatement(subject, predicate, object);
            queue.put(statement);
        } catch (InterruptedException interruptedException) {
            throw new RuntimeException(interruptedException);

        }
    }


}
