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
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.event.NotifyingRepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.memory.MemoryStore;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;


public class ObjectBasedSaxHandlerSesame extends ObjectBasedSaxHandler {


    Repository repository;
    ArrayBlockingQueue<Statement> queue = new ArrayBlockingQueue<>(10000, false);
    boolean notDone = true;
    boolean graphDone = false;
    Thread repoThread;



    SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();

    public ObjectBasedSaxHandlerSesame(Builder.ObjectBased builder) {
        super(null, builder);



        MemoryStore memoryStore = new MemoryStore();
        memoryStore.initialize();

        this.builder = builder;
        Thread thread = Thread.currentThread();
        repoThread = new Thread() {
            @Override
            public void run() {

                NotifyingSailConnection connection = memoryStore.getConnection();
                connection.begin(IsolationLevels.NONE);


                    while (notDone || !queue.isEmpty()) {
                        try {
                            Statement take = queue.take();
                            if(take.getSubject() != null) {
                                connection.addStatement(take.getSubject(), take.getPredicate(), take.getObject());
                            }

                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                        }
                    }


                    connection.commit();

                connection.close();

                repository = new SailRepository(memoryStore);

                graphDone = true;



            }
        };

        repoThread.start();
    }


    public String createTriple(String subject, String predicate, String objectResource) {


        IRI pNode = valueFactory.createIRI(predicate);
        Resource oNode = null;
        Resource rNode = null;


        if (!subject.startsWith("_:")) {
            oNode = valueFactory.createIRI(subject);

        } else {
            oNode = valueFactory.createBNode(subject);

        }

        if (!objectResource.startsWith("_:")) {
            rNode = valueFactory.createIRI(objectResource);

        } else {
            rNode = valueFactory.createBNode(objectResource);

        }


        try {
            queue.put(valueFactory.createStatement(oNode, pNode, rNode));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;

    }

    public String createTripleLiteral(String subject, String predicate, String objectLiteral, IRI datatype) {
        if (objectLiteral == null) {
            return null;
        }

        IRI pNode = valueFactory.createIRI(predicate);
        Resource oNode;


        if (!subject.startsWith("_:")) {
            oNode = valueFactory.createIRI(subject);

        } else {
            oNode = valueFactory.createBNode(subject);

        }


        Literal literal = valueFactory.createLiteral(objectLiteral, datatype);

        try {
            queue.put(valueFactory.createStatement(oNode, pNode, literal));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String createTripleLiteral(String subject, String predicate, String objectLiteral) {
        if (objectLiteral == null) {
            return null;
        }

        IRI pNode = valueFactory.createIRI(predicate);
        Resource oNode = null;


        if (!subject.startsWith("_:")) {
            oNode = valueFactory.createIRI(subject);

        } else {
            oNode = valueFactory.createBNode(subject);

        }


        Literal literal = valueFactory.createLiteral(objectLiteral);

        if (builder.autoTypeLiterals) {
            try {
                literal = valueFactory.createLiteral(Integer.parseInt(objectLiteral));
            } catch (Exception e) {
                try {
                    literal = valueFactory.createLiteral(Long.parseLong(objectLiteral));
                } catch (Exception ee) {
                }
            }

        }

        try {
            queue.put(valueFactory.createStatement(oNode, pNode, literal));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;


    }

    public String createTripleLiteral(String subject, String predicate, long objectLong) {

        IRI pNode = valueFactory.createIRI(predicate);
        Resource oNode = null;


        if (!subject.startsWith("_:")) {
            oNode = valueFactory.createIRI(subject);

        } else {
            oNode = valueFactory.createBNode(subject);

        }


        Literal literal = valueFactory.createLiteral(objectLong);


        try {
            queue.put(valueFactory.createStatement(oNode, pNode, literal));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;


    }

    @Override
    public void endDocument() throws SAXException {

        notDone = false;

        queue.add(new Statement() {
            @Override
            public Resource getSubject() {
                return null;
            }

            @Override
            public IRI getPredicate() {
                return null;
            }

            @Override
            public Value getObject() {
                return null;
            }

            @Override
            public Resource getContext() {
                return null;
            }
        });

        while (!graphDone) {
            try {
                Thread.sleep(10);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
