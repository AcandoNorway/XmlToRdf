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
import org.apache.jena.update.UpdateAction;

import java.io.IOException;
import java.io.InputStream;

public class PostProcessingJena extends PostProcessing {

	private Model model;
	private Model extractedModel = ModelFactory.createDefaultModel();

	public PostProcessingJena(Model model) {
		this.model = model;
	}

	public Model getModel() {
		return model;
	}

	public Model getExtractedModel() {
		return extractedModel;
	}


	@Override
	public PostProcessingJena mustacheTransform(InputStream mustacheTemplate, Object input) throws IOException {
		String sparqlString = compileMustacheTemplate(mustacheTemplate, input);
		UpdateAction.parseExecute(sparqlString, model);
		return this;
	}

	@Override
	public PostProcessingJena mustacheExtract(InputStream mustacheTemplate, Object input) throws IOException {
		String queryString = compileMustacheTemplate(mustacheTemplate, input);
		Query read = QueryFactory.create(queryString);
		Model model = QueryExecutionFactory.create(read, this.model).execDescribe();
		extractedModel.add(model);
		return this;
	}
}
