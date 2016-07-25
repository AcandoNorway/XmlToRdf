/*
Copyright 816 ACANDO AS

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

import no.acando.xmltordf.Builder;
import no.acando.xmltordf.XmlToRdfAdvancedJena;
import no.acando.xmltordf.XmlToRdfAdvancedSesame;
import org.apache.jena.query.Dataset;
import org.junit.Test;
import org.openrdf.repository.Repository;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

public class ConcurrencyTests {
    private List<Dataset> datasets = new ArrayList<>();
    private List<Repository> repositories = new ArrayList<>();

    private synchronized void addDataset(Dataset dataset) {
        datasets.add(dataset);
    }

    final static int NUMBER_OF_THREADS = 8;


    @Test
    public void testJena() {

        final int TRIPLES_PER_FILE = 217872;

        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        XmlToRdfAdvancedJena build = Builder.getAdvancedBuilderJena().build();

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            executor.execute(() -> {
                try {
                    Dataset dataset = build.convertToDataset(new BufferedInputStream(new FileInputStream("testFiles/mediumLargeFile/input.xml")));
                    addDataset(dataset);
                    System.out.println("Done");
                } catch (ParserConfigurationException | SAXException | IOException e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();

        while (!executor.isTerminated()) {
            Thread.yield();
        }

        long actualSum = datasets.stream().map(d -> d.getDefaultModel().size()).reduce((a, b) -> a + b).get();

        assertEquals("Sum is wrong", TRIPLES_PER_FILE*NUMBER_OF_THREADS, actualSum);

    }

    @Test
    public void testSesame() {
        final int TRIPLES_PER_FILE = 217872;

        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        XmlToRdfAdvancedSesame build = Builder.getAdvancedBuilderSesame().build();

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            executor.execute(() -> {
                try {
                    Repository repo = build.convertToRepository(new BufferedInputStream(new FileInputStream("testFiles/mediumLargeFile/input.xml")));
                    addRepository(repo);
                    System.out.println("Done");
                } catch (ParserConfigurationException | SAXException | IOException e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();

        while (!executor.isTerminated()) {
            Thread.yield();
        }

        long actualSum = repositories.stream().map(d -> d.getConnection().size()).reduce((a, b) -> a + b).get();

        assertEquals("Sum is wrong", TRIPLES_PER_FILE*NUMBER_OF_THREADS, actualSum);

    }

    synchronized private void addRepository(Repository repo) {
        repositories.add(repo);
    }



}
