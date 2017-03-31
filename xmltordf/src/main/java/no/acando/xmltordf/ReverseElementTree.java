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


import java.util.ArrayList;
import java.util.List;

class ReverseElementTree {

    List<Node> roots = new ArrayList<>();

    void insert(Builder.XmlPath path, String renameTo){


        for (Node root : roots) {
            if(root.elementName.equals(path.getTail())){
                root.add(path.shorten(), renameTo);
                return;
            }
        }

        Node e = new Node();
        roots.add(e);
        e.elementName = path.getTail();
        if(path.last()){
            e.newElementName = renameTo;
        }
        e.add(path.shorten(), renameTo);



    }


    public String get(Element element) {
        for (Node root : roots) {
            if(root.elementName.equals(element.getType())){
                if(element.parent !=null){
                    return  root.get(element.parent);
                } else{
                    return root.newElementName;
                }
            }
        }

        return null;
    }


}


class Node{
    String elementName;
    String newElementName;

    List<Node> next = new ArrayList<>();

    void add(Builder.XmlPath path, String renameTo){



        if(path.getTail() == null) return;

        for (Node node : next) {
            if(node.elementName.equals(path.getTail())){
                node.add(path.shorten(), renameTo);
                return;
            }
        }
        Node node = new Node();

        next.add(node);
        node.elementName = path.getTail();

        if(path.last()){
            node.newElementName = renameTo;
        }

        node.add(path.shorten(), renameTo);



    }

    public String get(Element parent) {


        for (Node node : next) {
            if(node.elementName.equals(parent.getType())){
                if(parent.parent == null){
                    return node.newElementName;
                }

                return node.get(parent.parent);
            }
        }

        if(newElementName != null) return newElementName;

        return null;
    }
}