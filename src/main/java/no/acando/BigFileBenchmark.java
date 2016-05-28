package no.acando;


import no.acando.xmltordf.*;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class BigFileBenchmark {

    ByteArrayInputStream byteArrayInputStream;

    @Setup(Level.Iteration)
    public void setUp() throws IOException {
        String s = IOUtils.toString(new FileInputStream("100mb.xml"));
        byteArrayInputStream = new ByteArrayInputStream(s.getBytes());
    }

    @TearDown(Level.Iteration)
    public void tearDown() throws InterruptedException {
        new File("100mb.xml.ttl").delete();
        System.gc();
        Thread.sleep(5000);
        System.gc();
        Thread.sleep(5000);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void fastConvert() throws IOException, SAXException, ParserConfigurationException {

        XmlToRdfFast build = Builder.getFastBuilder().build();
        build.convertToStream(byteArrayInputStream, new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        });


    }


    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void advancedConvert() throws IOException, SAXException, ParserConfigurationException {


        XmlToRdfAdvancedStream build = Builder.getAdvancedBuilderStream()
            .build();
        build.convertToStream(byteArrayInputStream, new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        });

    }


    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void advancedConvertWithMultipleOptions() throws IOException, SAXException, ParserConfigurationException {

        XmlToRdfAdvancedStream build = Builder.getAdvancedBuilderStream()
            .autoAddSuffixToNamespace("/")
            .setBaseNamespace("http://example.org/attributes/", Builder.AppliesTo.justAttributes)
            .convertComplexElementsWithOnlyAttributesToPredicate(true)
            .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(true)
            .addIndex(true)
            .useAttributeForId("item", "id", s -> s)
            .build();

        build.convertToStream(byteArrayInputStream, new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        });

    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void advancedConvertJena() throws IOException, SAXException, ParserConfigurationException {

        XmlToRdfAdvancedJena build = Builder.getAdvancedBuilderJena()
            .build();

        build.convertToDataset(byteArrayInputStream);

    }


    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void advancedConvertSesame() throws IOException, SAXException, ParserConfigurationException {

        XmlToRdfAdvancedSesame build = Builder.getAdvancedBuilderSesame()
            .build();

        build.convertToRepository(byteArrayInputStream);

    }

}
