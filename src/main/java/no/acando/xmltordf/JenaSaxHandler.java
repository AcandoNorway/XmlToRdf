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

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Map;
import java.util.Stack;

import static no.acando.xmltordf.Common.seperator;


public class JenaSaxHandler extends org.xml.sax.helpers.DefaultHandler {

      private Stack<Resource> resourceStack = new Stack<>();
      private Stack<String> valueStack = new Stack<>();
      private Stack<Integer> indexStack = new Stack<>();

      Model m;
      private final Property hasChild;
      private final Property hasValue;
      private final Property index;


//      String baseNameSpace;
//      Map<String, String> mapForClasses;
//      boolean autoDetectLiteralProperties;
//      boolean autoAddNamespaceDeclarations;
//      public boolean autoConvertShallowChildrenToProperties;
//      public boolean addIndex;

      int currentIndex = 0;

      Builder.Jena builder;


      public JenaSaxHandler(Builder.Jena builder) {
            m = ModelFactory.createDefaultModel();
            hasChild = m.createProperty("http://acandonorway.github.com/ontology.ttl#" + "hasChild");
            hasValue = m.createProperty("http://acandonorway.github.com/ontology.ttl#" + "hasValue");
            index = m.createProperty("http://acandonorway.github.com/ontology.ttl#" + "index");

            m.setNsPrefix("xmlToRdf", "http://acandonorway.github.com/ontology.ttl#");
            this.builder = builder;
      }

      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {


            valueStack.push(characters);
            characters = "";

            String ns = builder.overrideNamespace;
            if (ns == null) {
                  ns = uri;


                  if(builder.autoAddSuffixToNamespace != null){
                        if(!(ns.endsWith("#") || ns.endsWith("/"))) {
                              ns += builder.autoAddSuffixToNamespace;

                        }
                  }

            }

            String fullyQualifiedName = ns + localName;

            if (builder.mapForClasses != null && builder.mapForClasses.containsKey(uri + localName)) {
                  fullyQualifiedName = builder.mapForClasses.get(uri + localName);
            }

            Resource parent = null;

            if (resourceStack.size() > 0) {
                  parent = resourceStack.peek();
            }

            Resource resource = m.createResource();
            resource.addProperty(RDF.type, m.createResource(fullyQualifiedName));
            if (builder.addIndex) {
                  if (indexStack.isEmpty()) {
                        currentIndex = 0;
                  } else {
                        currentIndex = indexStack.pop();
                  }

                  resource.addLiteral(index, currentIndex);

                  currentIndex++;

                  indexStack.push(currentIndex);

            }
            if (parent != null) {
                  parent.addProperty(hasChild, resource);
            }

            for (int i = 0; i < attributes.getLength(); i++) {
                  String uriAttr = attributes.getURI(i);
                  String nameAttr = attributes.getLocalName(i);
                  String valueAttr = attributes.getValue(i);
                  String qname = attributes.getQName(i);
                  if (builder.autoAddNamespaceDeclarations) {
                        if (qname.equals("xmlns")) {
                              m.setNsPrefix("", valueAttr);
                              continue;
                        } else if (qname.contains("xmlns:")) {
                              m.setNsPrefix(qname.replace("xmlns:", ""), valueAttr);
                              continue;

                        }

                  }

                  if (builder.transformForAttributeValue) {
                        StringTransform stringTransform = null;

                        Map<String, StringTransform> map = builder.transformForAttributeValueMap;

                        if (map.containsKey(uri + localName + seperator + uriAttr + nameAttr)) {
                              stringTransform = map.get(uri + localName + seperator + uriAttr + nameAttr);
                        } else if (map.containsKey(uri + localName + seperator)) {
                              stringTransform = map.get(uri + localName + seperator);
                        } else if (map.containsKey(seperator + uriAttr + nameAttr)) {
                              stringTransform = map.get(seperator + uriAttr + nameAttr);
                        } else if (map.containsKey(seperator)) {
                              stringTransform = map.get(seperator);
                        }

                        if (stringTransform != null) {
                              valueAttr = stringTransform.transform(valueAttr);
                        }

                  }

                  if (builder.useAttributedForId) {
                        StringTransform stringTransform = null;
                        if (builder.useAttributedForIdMap.containsKey(uri + localName + seperator + uriAttr + nameAttr)) {
                              stringTransform = builder.useAttributedForIdMap.get(uri + localName + seperator + uriAttr + nameAttr);
                        } else if (builder.useAttributedForIdMap.containsKey(seperator + uriAttr + nameAttr)) {
                              stringTransform = builder.useAttributedForIdMap.get(seperator + uriAttr + nameAttr);
                        }

                        if (stringTransform != null) {

                              String newUri = stringTransform.transform(valueAttr);
                              Resource resource1 = m.createResource(newUri);
                              StmtIterator stmtIterator = resource.listProperties();
                              while (stmtIterator.hasNext()) {
                                    Statement statement = stmtIterator.nextStatement();
                                    resource1.addProperty(statement.getPredicate(), statement.getObject());
                              }


                              try {
                                    Resource peek = resourceStack.peek();
                                    m.remove(peek, hasChild, resource);
                                    peek.addProperty(hasChild, resource1);
                              } catch (Exception e) {

                              }

                              resource.removeProperties();


                              resource = resource1;
                        }
                  }


                  if (builder.overrideNamespace != null) {
                        uriAttr = builder.overrideNamespace;
                  }

                  if (uriAttr == null || uriAttr.trim().equals("")) {

                        uriAttr = uri;

                  }

                  if(builder.overrideNamespace == null && builder.autoAddSuffixToNamespace != null){
                        if(!(uriAttr.endsWith("#") || uriAttr.endsWith("/"))){
                              uriAttr += builder.autoAddSuffixToNamespace;

                        }
                  }

                  resource.addLiteral(m.createProperty(uriAttr, nameAttr), valueAttr);


            }

            indexStack.push(0);
            resourceStack.push(resource);


      }

      @Override
      public void endElement(String uri, String localName, String qName) throws SAXException {
            if (characters.length() > 0) {
                  handleLiteral(characters);
            }



            indexStack.pop();
            Resource pop = resourceStack.pop();



            // delete empty nodes
            if (resourceStack.size() > 0) {
                  StmtIterator stmtIterator = pop.listProperties();

                  int numberOfStatements = 0;
                  boolean rdfType = false;

                  if (stmtIterator.hasNext()) {
                        Statement next = stmtIterator.next();
                        numberOfStatements++;
                        if (next.getPredicate().equals(RDF.type)) {
                              rdfType = true;
                        }
                        if (stmtIterator.hasNext()) {
                              numberOfStatements++;
                        }
                  }

                  if (rdfType && numberOfStatements == 1) {
                        pop.removeAll(RDF.type);

                        m.remove(resourceStack.peek(), hasChild, pop);
                        if (builder.addIndex) {
                              m.remove(resourceStack.peek(), index, pop);
                        }


                  }
            }


            if (builder.autoConvertShallowChildrenToProperties) {

                  while (true) {

                        boolean done = true;
                        StmtIterator stmtIterator = pop.listProperties(hasChild);

                        if (stmtIterator.hasNext()) {
                              Statement statement = stmtIterator.nextStatement();
                              RDFNode object = statement.getObject();
                              if (object.isResource()) {

                                    if (isShallow(object.asResource())) {
                                          m.add(
                                                      pop,
                                                      m.createProperty(object.asResource().getProperty(RDF.type).getObject().asResource().getURI()),
                                                      object
                                          );

                                          object.asResource().removeAll(RDF.type);
                                          m.remove(statement);
                                          done = false;


                                    }
                              }

                        }

                        if (done) {
                              break;
                        }
                  }
            }


            characters = valueStack.pop();


      }

      private boolean isShallow(Resource resource) {
            StmtIterator stmtIterator = resource.listProperties();
            while (stmtIterator.hasNext()) {
                  Statement statement = stmtIterator.nextStatement();

                  if (!statement.getPredicate().equals(RDF.type) && statement.getObject().isResource()) {
                        return false;
                  }
            }
            return true;

      }

      @Override
      public void endDocument() throws SAXException {

      }

      String characters = "";

      @Override
      public void characters(char[] ch, int start, int length) throws SAXException {
            String value = new String(ch, start, length);


            if (value.length() > 0) {

                  characters += value;

            }


      }


      private void handleLiteral(String value) {
            value = value.trim();
            if(value.equals("")) return;

            if (builder.autoDetectLiteralProperties) {

                  Resource pop = resourceStack.pop();
                  if (resourceStack.isEmpty()) {
                        pop.addLiteral(hasValue, value);
                        resourceStack.push(pop);
                        return;
                  }

                  Resource peek = resourceStack.peek();


                  StmtIterator stmtIterator = pop.listProperties();

                  // Edge case. Detect if the resource has other properties, because of xml attributes
                  while (stmtIterator.hasNext()) {
                        Statement next = stmtIterator.next();
                        if (!next.getPredicate().equals(RDF.type) && !next.getPredicate().equals(index)) {
                              pop.addLiteral(hasValue, value);
                              resourceStack.push(pop);
                              return;
                        }
                  }

                  if (pop.getProperty(RDF.type) == null) {
                        pop.addLiteral(hasValue, value);
                        resourceStack.push(pop);
                        return;
                  }
                  String uri = pop.getProperty(RDF.type).getResource().getURI();
                  pop.removeAll(RDF.type);

                  m.remove(peek, hasChild, pop);
                  if (builder.addIndex) {
                        pop.removeAll(index);
                  }


                  peek.addLiteral(m.createProperty(uri), value);
                  resourceStack.push(pop);


            } else {

                  resourceStack.peek().addLiteral(hasValue, value);

            }
      }
}
