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

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.io.IOException;
import java.io.InputStream;


public class PostProcessingRDF4J extends PostProcessing {

    Repository repository;

    public PostProcessingRDF4J(Repository repository) {
        this.repository = repository;
    }


    public PostProcessingRDF4J mustacheTransform(InputStream mustacheTemplate, Object input) throws IOException {
        String stringWriter = compileMustacheTemplate(mustacheTemplate, input);

        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(stringWriter).execute();
        }

        return this;

    }

    @Override
    public PostProcessingRDF4J mustacheExtract(InputStream mustacheTemplate, Object input) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    public Repository getRepository() {
        return repository;
    }


}