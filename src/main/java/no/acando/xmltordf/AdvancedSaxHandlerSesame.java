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
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.memory.MemoryStore;
import org.xml.sax.SAXException;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;


public class AdvancedSaxHandlerSesame extends AdvancedSaxHandler<IRI, IRI> {

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

    public AdvancedSaxHandlerSesame(Builder.AdvancedSesame builder) {
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

                connection.commit();

                connection.close();

                repository = new SailRepository(memoryStore);

            }
        };

        repoThread.start();
    }

    public String createTriple(String subject, String predicate, String object) {

        IRI predicateNode = valueFactory.createIRI(predicate);
        Resource subjectNode = getResource(subject);
        Resource objectNode = getResource(object);

        try {
            queue.put(valueFactory.createStatement(subjectNode, predicateNode, objectNode));
        } catch (InterruptedException interruptedException) {
            //TODO: handle or throw this
            interruptedException.printStackTrace();
        }

        return null;

    }

    public String createTriple(String subject, String predicate, IRI objectNode) {

        IRI predicateNode = valueFactory.createIRI(predicate);
        Resource subjectNode = getResource(subject);

        try {
            queue.put(valueFactory.createStatement(subjectNode, predicateNode, objectNode));
        } catch (InterruptedException interruptedException) {
            //TODO: handle or throw this
            interruptedException.printStackTrace();
        }

        return null;

    }

    public String createTripleLiteral(String subject, String predicate, String objectLiteral, IRI datatype) {
        if (objectLiteral == null) {
            return null;
        }

        IRI predicateNode = valueFactory.createIRI(predicate);
        Resource subjectNode = getResource(subject);

        Literal literal = valueFactory.createLiteral(objectLiteral, datatype);

        try {
            queue.put(valueFactory.createStatement(subjectNode, predicateNode, literal));
        } catch (InterruptedException interruptedException) {
            //TODO: handle or throw this
            interruptedException.printStackTrace();
        }

        return null;
    }

    public String createTripleLiteral(String subject, String predicate, String objectLiteral) {
        if (objectLiteral == null) {
            return null;
        }

        IRI predicateNode = valueFactory.createIRI(predicate);
        Resource subjectNode = getResource(subject);

        Literal literal = valueFactory.createLiteral(objectLiteral);

        //TODO: do we care about other literal types? If it's neither int nor long, then what?
        if (builder.autoTypeLiterals) {
            try {
                literal = valueFactory.createLiteral(Integer.parseInt(objectLiteral));
            } catch (Exception e) {
                //TODO: handle or throw this
                //TODO: consider using a specific exception
                //TODO: consider using a better exception variable name
                try {
                    literal = valueFactory.createLiteral(Long.parseLong(objectLiteral));
                } catch (Exception ee) {
                    //TODO: handle or throw this
                    //TODO: consider using a specific exception
                    //TODO: consider using a better exception variable name
                }
            }

        }

        try {
            queue.put(valueFactory.createStatement(subjectNode, predicateNode, literal));
        } catch (InterruptedException e) {
            //TODO: handle or throw this
            e.printStackTrace();
        }

        return null;

    }

    public String createTripleLiteral(String subject, String predicate, long objectLong) {

        IRI predicateNode = valueFactory.createIRI(predicate);
        Resource subjectNode = getResource(subject);

        Literal literal = valueFactory.createLiteral(objectLong);

        try {
            queue.put(valueFactory.createStatement(subjectNode, predicateNode, literal));
        } catch (InterruptedException e) {
            //TODO: handle or throw this
            e.printStackTrace();
        }

        return null;

    }

    public String createList(String subject, String predicate, List<Object> mixedContent) {

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

        try {
            queue.put(valueFactory.createStatement(subjectNode, predicateNode, head));
        } catch (InterruptedException interruptedException) {
            //TODO: handle or throw this
            interruptedException.printStackTrace();
        }
        mixedContentModel.forEach(statement -> {
            try {
                queue.put(statement);
            } catch (InterruptedException interruptedException) {
                //TODO: handle or throw this
                interruptedException.printStackTrace();
            }
        });

        return null;
    }

    @Override
    public void endDocument() throws SAXException {

        notDone = false;

        try {
            queue.put(EndOfFileStatement);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }

        try {
            repoThread.join();
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }

    private Resource getResource(String subject) {
        if (!isBlankNode(subject)) {
            return valueFactory.createIRI(subject);
        } else {
            return valueFactory.createBNode(subject);
        }
    }

}
