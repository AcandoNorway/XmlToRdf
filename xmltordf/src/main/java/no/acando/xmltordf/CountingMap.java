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

import java.util.HashMap;
import java.util.Map;

class CountingMap {

    Map<String, Integer> internalMap = new HashMap<>();

    int plusPlus(String element){
        Integer integer = internalMap.get(element);
        if(integer == null){
            internalMap.put(element, 0);
            return 0;
        }
        integer++;
        internalMap.put(element, integer);
        return integer;
    }

}
