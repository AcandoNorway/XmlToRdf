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
import java.util.Vector;


public class UndoableBufferedPrintWriter {

        PrintStream out;
        int vectorSize = 10;

        Vector<String> vector = new Vector<>();

        public UndoableBufferedPrintWriter(PrintStream out) {
                this.out = out;
        }


        void println(String s){
                vector.add(s);

                while(vector.size() > vectorSize){
                        out.println(vector.remove(0));
                }
        }

        String peek(){

                return vector.get(vector.size()-1);
        }


        String pop(){


                return vector.remove(vector.size()-1);
        }

        void flush(){
                while(vector.size() > 0){
                        out.println(vector.remove(0));
                }

                out.flush();
        }



}
