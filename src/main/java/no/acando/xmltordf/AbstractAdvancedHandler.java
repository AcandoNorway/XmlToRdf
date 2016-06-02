package no.acando.xmltordf;

import java.util.List;

/**
 * Created by havardottestad on 02/06/16.
 */
abstract class AbstractAdvancedHandler<Datatype> extends org.xml.sax.helpers.DefaultHandler {

    abstract String createTriple(String subject, String predicate, String object);

    abstract String createTripleLiteral(String subject, String predicate, String objectLiteral);

    abstract String createTripleLiteral(String subject, String predicate, long objectLong);

    abstract String createList(String subject, String predicate, List<Object> mixedContent);

    abstract String createTripleLiteral(String subject, String predicate, String objectLiteral, Datatype datatype) ;

    }
