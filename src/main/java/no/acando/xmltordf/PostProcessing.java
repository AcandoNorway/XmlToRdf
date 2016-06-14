package no.acando.xmltordf;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.*;

/**
 * Created by sebastienmuller on 14/06/16.
 */
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
