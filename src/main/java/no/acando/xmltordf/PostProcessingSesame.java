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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;


public class PostProcessingSesame {

    Repository repository;

    public PostProcessingSesame(Repository repository) {
        this.repository = repository;
    }


    public PostProcessingSesame mustacheTransform(InputStream mustacheTemplate, Object input) throws IOException {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new InputStreamReader(mustacheTemplate), "");
        StringWriter stringWriter = new StringWriter();
        mustache.execute(stringWriter, input).flush();

        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(stringWriter.toString()).execute();
        }

        return this;

    }


    public Repository getRepository(){
        return repository;
    }



}