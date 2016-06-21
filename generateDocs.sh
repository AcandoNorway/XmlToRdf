#!/usr/bin/env bash
rm -r ~/.m2/repository/no/acando
mvn clean
mvn install -DskipTests=true &&
mvn site -Dmpir.skip=true -DskipTests=true &&
java -jar doclet/target/xmltordf_doclet-1.0.jar



