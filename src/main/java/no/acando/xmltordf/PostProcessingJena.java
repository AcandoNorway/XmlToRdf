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

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.update.UpdateAction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;


public class PostProcessingJena {

      private Model m;
      private Model extractedModel = ModelFactory.createDefaultModel();
      private File intermediaryDirectory;
      private String intermediaryOutputFormat;

      public PostProcessingJena(Model m) {
            this.m = m;
      }

      public Model getModel() {
            return m;
      }

      public PostProcessingJena sparqlTransform(String updateQuery) {

            UpdateAction.parseExecute(updateQuery, m);
            return this;
      }

      public PostProcessingJena sparqlTransform(File fileOrDir) throws IOException {

            if (fileOrDir.isDirectory()) {
                  if (fileOrDir.listFiles(name -> name.toString().endsWith(".qr")).length == 0) {
                        System.err.println("No .qr SPARQL update query files found in: " + fileOrDir);
                  }
                  Files.list(fileOrDir.toPath())

                              .filter((name) -> name.toString().endsWith(".qr"))
                              .sorted()
                              .forEach((file) ->{
                                    try {
                                          UpdateAction.readExecute(file.toAbsolutePath().toString(), m);
                                          logIntermediaryModels(file.toFile().getName());
                                    } catch (RuntimeException e) {
                                          System.err.println(file.toAbsolutePath().toString());
                                          throw e;
                                    }
                              });

            } else {

                  UpdateAction.readExecute(fileOrDir.getAbsolutePath(), m);
                  logIntermediaryModels(fileOrDir.getName());


            }


            return this;

      }

      public PostProcessingJena extractConstruct(String constructQuery) {

            Model model = QueryExecutionFactory.create(constructQuery, m).execConstruct();
            extractedModel.add(model);

            return this;

      }

      public PostProcessingJena extractConstruct(File fileOrDir) throws IOException {

            if (fileOrDir.isDirectory()) {
                  if (fileOrDir.listFiles(name -> name.toString().endsWith(".qr")).length == 0) {
                        System.err.println("No .qr SPARQL construct query files found in: " + fileOrDir);
                  }

                  Files.list(fileOrDir.toPath())
                              .filter((name) -> name.toString().endsWith(".qr"))
                              .sorted()
                              .forEach((file) ->{
                                    try {
                                          Query read = QueryFactory.read(file.toAbsolutePath().toString());
                                          Model model = QueryExecutionFactory.create(read, m).execConstruct();
                                          extractedModel.add(model);
                                    } catch (RuntimeException e) {
                                          System.err.println(file.toAbsolutePath().toString());
                                          throw e;
                                    }
                              });

            } else {
                  Query read = QueryFactory.read(fileOrDir.getAbsolutePath());
                  Model model = QueryExecutionFactory.create(read, m).execConstruct();
                  extractedModel.add(model);

            }

            return this;

      }

      public Model getExtractedModel() {
            return extractedModel;
      }

      public PostProcessingJena extractDescribe(File fileOrDir) throws IOException {
            if (fileOrDir.isDirectory()) {
                  if (fileOrDir.listFiles(name -> name.toString().endsWith(".qr")).length == 0) {
                        System.err.println("No .qr SPARQL describe query files found in: " + fileOrDir);
                  }

                  Files.list(fileOrDir.toPath())
                              .filter((name) -> name.toString().endsWith(".qr"))
                              .sorted()
                              .forEach((file) ->{
                                    try {
                                          Query read = QueryFactory.read(file.toAbsolutePath().toString());
                                          Model model = QueryExecutionFactory.create(read, m).execDescribe();
                                          extractedModel.add(model);
                                    } catch (RuntimeException e) {
                                          System.err.println(file.toAbsolutePath().toString());
                                          throw e;
                                    }
                              });

            } else {
                  Query read = QueryFactory.read(fileOrDir.getAbsolutePath());
                  Model model = QueryExecutionFactory.create(read, m).execDescribe();
                  extractedModel.add(model);

            }

            return this;

      }

      public PostProcessingJena outputIntermediaryModels(File directory) {
            if (!directory.exists()) {
                  directory.mkdir();
            }

            if (!directory.isDirectory()) {
                  throw new RuntimeException("outputIntermediaryModels needs a directory.");
            }


            intermediaryDirectory = directory;

            return this;
      }

      private void logIntermediaryModels(String transformName) {
            if (intermediaryDirectory != null) {
                  try {
                        m.write(new FileWriter(intermediaryDirectory.getAbsolutePath() + "/" + transformName + ".ttl"), Lang.TURTLE.getLabel());
                  } catch (IOException e) {
                        e.printStackTrace();
                  }
            }
      }
}