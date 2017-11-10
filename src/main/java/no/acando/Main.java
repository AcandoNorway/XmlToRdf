

package no.acando;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class Main {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(BigFileBenchmark.class.getSimpleName())
            .warmupIterations(1)
            .measurementIterations(1)
            .forks(1)
            .addProfiler("stack", "lines=20;period=1;top=20")
            .build();

        new Runner(opt).run();
    }

}
