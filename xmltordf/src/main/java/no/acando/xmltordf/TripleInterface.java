package no.acando.xmltordf;

import java.util.List;

/**
 * Created by havardottestad on 02/07/16.
 */
public interface TripleInterface<ResourceType, Datatype> {

	String createTriple(String subject, String predicate, String object);

	String createTripleLiteral(String subject, String predicate, String objectLiteral);

	String createTripleLiteral(String subject, String predicate, long objectLong);

	String createList(String subject, String predicate, List<Object> mixedContent);

	String createTripleLiteral(String subject, String predicate, String objectLiteral, Datatype datatype);

	String createTriple(String uri, String hasValue, ResourceType resourceType);

	void endElement();

}
