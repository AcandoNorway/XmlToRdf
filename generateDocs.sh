#!/usr/bin/env bash
mvn install -DskipTests=true &&
mvn site -Dmpir.skip=true -DskipTests=true &&
java -jar doclet/target/xmltordf_doclet-1.0.jar



