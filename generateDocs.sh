#!/usr/bin/env bash
rm -r ~/.m2/repository/no/acando
rm -r xmltordf/documentation
mkdir xmltordf/documentation

mvn clean
mvn install -DskipTests=true &&
mvn install -DskipTests=true &&
mvn site -Dmpir.skip=true -DskipTests=true
javadoc -doclet no.acando.xmltordf.doclet.GenerateDocs -docletpath doclet/target/xmltordf_doclet-1.0.jar  -sourcepath xmltordf/src/main/java -subpackages no.acando.xmltordf
mv javadoc.json xmltordf/documentation/javadoc.json
java -jar doclet/target/xmltordf_doclet-1.0.jar

