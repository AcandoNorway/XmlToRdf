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

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;


public class UndoableBufferedPrintWriter {

    private final PrintStream out;
    private final int SIZE = 10;

    private int counter = 0;
    private final Deque<String> deque = new ArrayDeque<>(12);

    public UndoableBufferedPrintWriter(PrintStream out) {
        this.out = out;
    }


    void println(String s) {

        deque.push(s);

        counter++;
        while (counter > SIZE) {
            out.println(deque.removeLast());
            counter--;
        }
    }

    String peek() {

        return deque.peek();
    }


    String pop() {
        counter--;

        return deque.pop();
    }

    void flush() {
        try {
            while (true) {
                out.println(deque.pop());
            }
        } catch (NoSuchElementException e) {
            // done with loop
        }

        out.flush();
    }


}
