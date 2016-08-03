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

import org.xml.sax.SAXException;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;


final class AdvancedSaxHandlerString extends AdvancedSaxHandler<String, String> {
    private final PrintStream out;


    AdvancedSaxHandlerString(OutputStream out, Builder.AdvancedStream builder) {
        super(builder);

        try {
            this.out = new PrintStream(out, false, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("utf8 encoding could niot be found");
        }

    }

    final public void createTriple(String subject, String predicate, String object) {
        boolean objectIsBlank = isBlankNode(object);

        if (isBlankNode(subject)) {
            if (objectIsBlank) {
                out.println(subject + " <" + predicate + "> " + object + '.');
            } else {
                out.println(subject + " <" + predicate + "> <" + object + ">.");
            }
        } else {
            if (objectIsBlank) {
                out.println('<' + subject + "> <" + predicate + "> " + object + '.');
            } else {
                out.println('<' + subject + "> <" + predicate + "> <" + object + ">.");
            }
        }

    }

    final public void createTripleLiteral(String subject, String predicate, String objectLiteral) {

        objectLiteral = objectLiteral
            .replace("\\", "\\\\")
            .replace("\"", "\\\"");

        String datatype = "";

        if (builder.autoTypeLiterals) {
            try {
                Integer.parseInt(objectLiteral);
                datatype = "^^<" + XSD + "integer>";
            } catch (NumberFormatException e) {
                try {
                    Double.parseDouble(objectLiteral);
                    datatype = "^^<" + XSD + "decimal>";
                } catch (NumberFormatException e2) {
                    try {
                        LocalDateTime.parse(objectLiteral, DateTimeFormatter.ISO_DATE_TIME);
                        datatype = "^^<" + XSD + "dateTime>";
                    } catch (DateTimeParseException e3) {
                        try {
                            LocalDate.parse(objectLiteral, DateTimeFormatter.ISO_DATE);
                            datatype = "^^<" + XSD + "date>";
                        } catch (DateTimeParseException e4) {
                            //this catch block should be empty!
                        }
                    }
                }
            }
        }

        if (!isBlankNode(subject)) {
            out.println('<' + subject + "> <" + predicate + "> \"\"\"" + objectLiteral + "\"\"\"" + datatype + " .");
        } else {
            out.println(subject + " <" + predicate + "> \"\"\"" + objectLiteral + "\"\"\"" + datatype + " .");
        }

    }

    final public void createTripleLiteral(String subject, String predicate, long objectLong) {

        if (!isBlankNode(subject)) {
            out.println('<' + subject + "> <" + predicate + "> \"" + objectLong + "\"^^<http://www.w3.org/2001/XMLSchema#long>" + " .");
        } else {
            out.println(subject + " <" + predicate + "> \"" + objectLong + "\"^^<http://www.w3.org/2001/XMLSchema#long>" + " .");
        }

    }

    final public void createList(String subject, String predicate, List<Object> mixedContent) {
        predicate = '<' + predicate + '>';
        if (!isBlankNode(subject)) {
            subject = '<' + subject + '>';
        }

        StringBuilder stringBuilder = new StringBuilder(subject + ' ' + predicate + " (");

        mixedContent.forEach(content -> {
            if (content instanceof String) {
                String objectLiteral = (String) content;
                objectLiteral = objectLiteral
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"");
                stringBuilder.append("\"\"\"" + objectLiteral + "\"\"\" ");
            } else if (content instanceof Element) {
                Element objectElement = (Element) content;
                if (isBlankNode(objectElement.uri)) {
                    stringBuilder.append(objectElement.uri + ' ');
                } else {
                    stringBuilder.append('<' + objectElement.uri + "> ");
                }
            } else {
                throw new IllegalStateException("Unknown type of: " + content.getClass().toString());
            }

        });

        out.println(stringBuilder.append(").").toString());

    }

    final public void createTripleLiteral(String subject, String predicate, String objectLiteral, String dataType) {

        objectLiteral = objectLiteral
            .replace("\\", "\\\\")
            .replace("\"", "\\\"");

        if (isBlankNode(subject)) {
            out.println(subject + " <" + predicate + "> \"\"\"" + objectLiteral + "\"\"\"^^<" + dataType + "> .");
        } else {
            out.println('<' + subject + "> <" + predicate + "> \"\"\"" + objectLiteral + "\"\"\"^^<" + dataType + "> .");
        }

    }

    @Override
    final public void endDocument() throws SAXException {

        out.flush();
        out.close();
    }

}
