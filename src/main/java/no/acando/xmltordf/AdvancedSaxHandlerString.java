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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;


public class AdvancedSaxHandlerString extends AdvancedSaxHandler<String, String> {
    public static final char OPEN_CHEVRON_CHAR = '<';
    public static final String OPEN_CLOSE_CHEVRON_STRING = "> <";
    public static final String OPEN_CHEVRON_STRING = " <";
    public static final String XMLSCHEMA_LONG = "\"^^<http://www.w3.org/2001/XMLSchema#long>";
    public static final String FULL_STOP_STRING = " .";
    public static final char CLOSE_CHEVRON_CHAR = '>';
    public static final String CLOSE_CHEVRON_STRING = "> ";
    public static final char FULL_STOP_CHAR = '.';
    public static final String CLOSE_CHEVRON_FULL_STOP_STRING = ">.";
    public static final String CLOSE_CHEVRON_TRIPLE_ESCAPE_STRING = "> \"\"\"";
    public static final String TRIPLE_ESCAPE_FULL_STOP_STRING = "\"\"\" .";
    public static final String CLOSE_CHEVRON_ESCAPE_STRING = "> \"";
    public static final String CLOSE_CHEVRON_SPACE_FULL_STOP_STRING = "> .";
    public static final String TRIPLE_ESCAPE_ISTYPE_OPEN_CHEVRON_STRING = "\"\"\"^^<";
    public static final String DOUBLE_ESCAPE = "\\";
    public static final String SINGLE_ESCAPE = "\"";
    public static final String TRIPLE_ESCAPE = "\\\"";
    public static final String QUADRUPLE_ESCAPE = "\\\\";
    public static final char WHITESPACE_CHAR = ' ';
    private final PrintStream out;

    public AdvancedSaxHandlerString(OutputStream out, Builder.AdvancedStream builder) {
        super(out, builder);
        if (out != null) {
            this.out = new PrintStream(out);
        } else {
            this.out = new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {

                }
            });
        }
        this.builder = builder;
    }

    public String createTriple(String subject, String predicate, String object) {
        boolean objectIsBlank = isBlankNode(object);

        if (isBlankNode(subject)) {
            if (objectIsBlank) {
                return subject + OPEN_CHEVRON_STRING + predicate + CLOSE_CHEVRON_STRING + object + FULL_STOP_CHAR;
            } else {
                return subject + OPEN_CHEVRON_STRING + predicate + OPEN_CLOSE_CHEVRON_STRING + object + CLOSE_CHEVRON_FULL_STOP_STRING;
            }
        } else {
            if (objectIsBlank) {
                return OPEN_CHEVRON_CHAR + subject + OPEN_CLOSE_CHEVRON_STRING + predicate + CLOSE_CHEVRON_STRING + object + FULL_STOP_CHAR;
            } else {
                return OPEN_CHEVRON_CHAR + subject + OPEN_CLOSE_CHEVRON_STRING + predicate + OPEN_CLOSE_CHEVRON_STRING + object + CLOSE_CHEVRON_FULL_STOP_STRING;
            }
        }

    }

    public String createTripleLiteral(String subject, String predicate, String objectLiteral) {
        if (objectLiteral == null) {
            return "";
        }

        objectLiteral = objectLiteral
            .replace(DOUBLE_ESCAPE, QUADRUPLE_ESCAPE)
            .replace(SINGLE_ESCAPE, TRIPLE_ESCAPE);

        if (!isBlankNode(subject)) {
            return OPEN_CHEVRON_CHAR + subject + OPEN_CLOSE_CHEVRON_STRING + predicate + CLOSE_CHEVRON_TRIPLE_ESCAPE_STRING + objectLiteral + TRIPLE_ESCAPE_FULL_STOP_STRING;
        } else {
            return subject + OPEN_CHEVRON_STRING + predicate + CLOSE_CHEVRON_TRIPLE_ESCAPE_STRING + objectLiteral + TRIPLE_ESCAPE_FULL_STOP_STRING;
        }

    }

    public String createTripleLiteral(String subject, String predicate, long objectLong) {

        if (!isBlankNode(subject)) {
            return OPEN_CHEVRON_CHAR + subject + OPEN_CLOSE_CHEVRON_STRING + predicate + CLOSE_CHEVRON_ESCAPE_STRING + objectLong + XMLSCHEMA_LONG + FULL_STOP_STRING;
        } else {
            return subject + OPEN_CHEVRON_STRING + predicate + CLOSE_CHEVRON_ESCAPE_STRING + objectLong + XMLSCHEMA_LONG + FULL_STOP_STRING;
        }

    }

    public String createList(String subject, String predicate, List<Object> mixedContent) {
        predicate = OPEN_CHEVRON_CHAR + predicate + CLOSE_CHEVRON_CHAR;
        if (!isBlankNode(subject)) {
            subject = OPEN_CHEVRON_CHAR + subject + CLOSE_CHEVRON_CHAR;
        }

        StringBuilder stringBuilder = new StringBuilder(subject + WHITESPACE_CHAR + predicate + " (");

        mixedContent.forEach(content -> {
            if (content instanceof String) {
                String objectLiteral = (String) content;
                objectLiteral = objectLiteral
                    .replace(DOUBLE_ESCAPE, QUADRUPLE_ESCAPE)
                    .replace(SINGLE_ESCAPE, TRIPLE_ESCAPE);
                stringBuilder.append("\"\"\"" + objectLiteral + "\"\"\" ");
            } else if (content instanceof Element) {
                Element objectElement = (Element) content;
                if (isBlankNode(objectElement.getUri())) {
                    stringBuilder.append(objectElement.getUri() + WHITESPACE_CHAR);
                } else {
                    stringBuilder.append(OPEN_CHEVRON_CHAR + objectElement.getUri() + CLOSE_CHEVRON_STRING);
                }
            } else {
                throw new IllegalStateException("Unknown type of: " + content.getClass().toString());
            }

        });

        return stringBuilder.append(").").toString();

    }

    public String createTripleLiteral(String subject, String predicate, String objectLiteral, String dataType) {

        objectLiteral = objectLiteral
            .replace(DOUBLE_ESCAPE, QUADRUPLE_ESCAPE)
            .replace(SINGLE_ESCAPE, TRIPLE_ESCAPE);

        if (isBlankNode(subject)) {
            return subject + OPEN_CHEVRON_STRING + predicate + CLOSE_CHEVRON_TRIPLE_ESCAPE_STRING + objectLiteral + TRIPLE_ESCAPE_ISTYPE_OPEN_CHEVRON_STRING + dataType.toString() + CLOSE_CHEVRON_SPACE_FULL_STOP_STRING;
        } else {
            return OPEN_CHEVRON_CHAR + subject + OPEN_CLOSE_CHEVRON_STRING + predicate + CLOSE_CHEVRON_TRIPLE_ESCAPE_STRING + objectLiteral + TRIPLE_ESCAPE_ISTYPE_OPEN_CHEVRON_STRING + dataType.toString() + CLOSE_CHEVRON_SPACE_FULL_STOP_STRING;
        }

    }

    @Override
    public void endDocument() throws SAXException {

        out.flush();
        out.close();
    }

}
