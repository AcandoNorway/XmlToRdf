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
import org.apache.jena.graph.*;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.openrdf.model.IRI;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;


public class ObjectBasedSaxHandlerJena extends ObjectBasedSaxHandler {


      private Graph g;

      Dataset dataset;
      ArrayBlockingQueue<Triple> queue = new ArrayBlockingQueue<>(10000, false);
      boolean notDone = true;
      boolean graphDone = false;
      Thread jenaThread;

     CountDownLatch latch =  new CountDownLatch(1);

      final Triple EndOfFileTriple = new Triple(NodeFactory.createURI(EndOfFile), NodeFactory.createURI(EndOfFile), NodeFactory.createURI(EndOfFile));


      public ObjectBasedSaxHandlerJena(Builder.ObjectBased builder) {
            super(new NullOutputStream(), builder);
            dataset = DatasetFactory.createMem();
            g = dataset.getDefaultModel().getGraph();

            this.builder = builder;
            Thread thread = Thread.currentThread();
            jenaThread = new Thread() {
                  @Override
                  public void run() {

                        int size = 10000;
                        ArrayList<Triple> buff = new ArrayList<>(size);

                        int i = 0;
                        while (notDone || !queue.isEmpty()) {
                              try {
                                    Triple take = queue.take();
                                    if(take != EndOfFileTriple){
                                          buff.add(take);
                                          i++;
                                    }

                              } catch (InterruptedException e) {
                                    //e.printStackTrace();
                              }

                              if (i >= size - 10) {
                                    GraphUtil.add(g, buff);
                                    buff = new ArrayList<>();
                                    i = 0;
                              }


                        }

                        if (i > 0) {
                              GraphUtil.add(g, buff);
                        }

                        latch.countDown();

                  }
            };

            jenaThread.start();
      }


      public String createTriple(String subject, String predicate, String objectResource) {


            Node pNode = NodeFactory.createURI(predicate);
            Node oNode = null;
            Node rNode = null;


            if (!subject.startsWith("_:")) {
                  oNode = NodeFactory.createURI(subject);

            } else {
                  oNode = NodeFactory.createBlankNode(subject);

            }

            if (!objectResource.startsWith("_:")) {
                  rNode = NodeFactory.createURI(objectResource);

            } else {
                  rNode = NodeFactory.createBlankNode(objectResource);

            }

            Triple triple = new Triple(oNode, pNode, rNode);
            try {
                  queue.put(triple);
            } catch (InterruptedException e) {
                  e.printStackTrace();
            }

            return null;

      }

      public String createTripleLiteral(String subject, String predicate, String objectLiteral) {
            if(objectLiteral == null) return null;

            Node pNode = NodeFactory.createURI(predicate);
            Node oNode = null;


            if (!subject.startsWith("_:")) {
                  oNode = NodeFactory.createURI(subject);

            } else {
                  oNode = NodeFactory.createBlankNode(subject);

            }



            Node literal = NodeFactory.createLiteral(objectLiteral, "", false);
            Triple triple = new Triple(oNode, pNode, literal);
            try {
                  queue.put(triple);
            } catch (InterruptedException e) {
                  e.printStackTrace();
            }

            return null;


      }

      public String createTripleLiteral(String subject, String predicate, String objectLiteral, IRI datatype) {
            if(objectLiteral == null) return null;

            Node pNode = NodeFactory.createURI(predicate);
            Node oNode = null;


            if (!subject.startsWith("_:")) {
                  oNode = NodeFactory.createURI(subject);

            } else {
                  oNode = NodeFactory.createBlankNode(subject);
            }

            Node literal = NodeFactoryExtra.createLiteralNode(objectLiteral, null, datatype.toString());

            Triple triple = new Triple(oNode, pNode, literal);
            try {
                  queue.put(triple);
            } catch (InterruptedException e) {
                  e.printStackTrace();
            }

            return null;


      }


      public String createTripleLiteral(String subject, String predicate, long objectLong) {



            Node pNode = NodeFactory.createURI(predicate);
            Node oNode = null;


            if (!subject.startsWith("_:")) {
                  oNode = NodeFactory.createURI(subject);

            } else {
                  oNode = NodeFactory.createBlankNode(subject);

            }


            Node literal = ResourceFactory.createTypedLiteral(new Long(objectLong)).asNode();

            Triple triple = new Triple(oNode, pNode, literal);
            try {
                  queue.put(triple);
            } catch (InterruptedException e) {
                  e.printStackTrace();
            }

            return null;



      }

      @Override
      public void endDocument() throws SAXException {

            notDone = false;


            queue.add(EndOfFileTriple);


            try {
                  latch.await();
            } catch (InterruptedException e) {
                  e.printStackTrace();
            }
      }

}
