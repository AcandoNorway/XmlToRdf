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

import java.io.*;


abstract class PostProcessing {

    private File intermediaryDirectory;

    String compileMustacheTemplate(InputStream mustacheTemplate, Object input) throws IOException {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new InputStreamReader(mustacheTemplate), "");
        StringWriter stringWriter = new StringWriter();
        mustache.execute(stringWriter, input).flush();
        return stringWriter.toString();
    }

    public abstract PostProcessing mustacheTransform(InputStream mustacheTemplate, Object input) throws IOException;

    public abstract PostProcessing mustacheExtract(InputStream mustacheTemplate, Object input) throws IOException;

//    private void logBetweenTransforms(File intermediaryDirectory) {
//        this.intermediaryDirectory = intermediaryDirectory;
//    }


}
