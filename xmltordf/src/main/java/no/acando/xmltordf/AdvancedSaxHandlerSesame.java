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

import org.openrdf.IsolationLevels;
import org.openrdf.model.*;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.util.RDFCollections;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.memory.MemoryStore;
import org.xml.sax.SAXException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;


class AdvancedSaxHandlerSesame extends AdvancedSaxHandler<IRI, IRI> {

    Repository repository;
    private LinkedBlockingDeque<Statement> queue;
    private boolean notDone = true;
    private Thread repoThread;

    private final Statement EndOfFileStatement = SimpleValueFactory.getInstance().createStatement(
        SimpleValueFactory.getInstance().createIRI(XmlToRdfVocabulary.EndOfFile),
        SimpleValueFactory.getInstance().createIRI(XmlToRdfVocabulary.EndOfFile),
        SimpleValueFactory.getInstance().createIRI(XmlToRdfVocabulary.EndOfFile)
    );

    private SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();

    AdvancedSaxHandlerSesame(Builder.AdvancedSesame builder) {
        super(null, builder);

        queue = new LinkedBlockingDeque<>(builder.buffer);

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

    public void createTriple(String subject, String predicate, String object) {

        IRI predicateNode = valueFactory.createIRI(predicate);
        Resource subjectNode = getResource(subject);
        Resource objectNode = getResource(object);

        putTripleOnQueue(subjectNode, predicateNode, objectNode);


    }

    public void createTriple(String subject, String predicate, IRI objectNode) {

        IRI predicateNode = valueFactory.createIRI(predicate);
        Resource subjectNode = getResource(subject);

        putTripleOnQueue(subjectNode, predicateNode, objectNode);

    }

    public void createTripleLiteral(String subject, String predicate, String objectLiteral, IRI datatype) {
        if (objectLiteral == null) {
            return;
        }

        IRI predicateNode = valueFactory.createIRI(predicate);
        Resource subjectNode = getResource(subject);

        Literal literal = valueFactory.createLiteral(objectLiteral, datatype);

        putTripleOnQueue(subjectNode, predicateNode, literal);

    }

    public void createTripleLiteral(String subject, String predicate, String objectLiteral) {
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


        putTripleOnQueue(subjectNode, predicateNode, literal);


    }

    public void createTripleLiteral(String subject, String predicate, long objectLong) {

        IRI predicateNode = valueFactory.createIRI(predicate);
        Resource subjectNode = getResource(subject);

        Literal literal = valueFactory.createLiteral(objectLong);


        putTripleOnQueue(subjectNode, predicateNode, literal);


    }

    public void createList(String subject, String predicate, List<Object> mixedContent) {

        IRI predicateNode = valueFactory.createIRI(predicate);
        Resource subjectNode = getResource(subject);

        Resource head = valueFactory.createBNode();

        List<Value> collect = mixedContent.stream().map(content -> {
            if (content instanceof String) {
                String objectLiteral = (String) content;

                return valueFactory.createLiteral(objectLiteral);

            } else if (content instanceof Element) {
                Element objectElement = (Element) content;
                if (!objectElement.getUri().startsWith("_:")) {
                    return valueFactory.createIRI(objectElement.getUri());
                } else {
                    return valueFactory.createBNode(objectElement.getUri());
                }

            } else {
                throw new IllegalStateException("Unknown type of: " + content.getClass().toString());
            }
        }).collect(Collectors.toList());

        Model mixedContentModel = RDFCollections.asRDF(collect, head, new LinkedHashModel());

        putTripleOnQueue(subjectNode, predicateNode, head);

        mixedContentModel.forEach(statement -> {
            try {
                queue.put(statement);
            } catch (InterruptedException interruptedException) {
                throw new RuntimeException(interruptedException);

            }
        });

    }

    @Override
    public void endDocument() throws SAXException {

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

    private Resource getResource(String subject) {
        if (!isBlankNode(subject)) {
            return valueFactory.createIRI(subject);
        } else {
            return valueFactory.createBNode(subject);
        }
    }


    private void putTripleOnQueue(Resource subject, IRI predicate, Value object) {
        try {
            Statement statement = valueFactory.createStatement(subject, predicate, object);
            queue.put(statement);
        } catch (InterruptedException interruptedException) {
            throw new RuntimeException(interruptedException);

        }
    }


}
