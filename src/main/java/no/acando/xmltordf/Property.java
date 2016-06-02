package no.acando.xmltordf;

/**
 * Created by havardottestad on 02/06/16.
 */
public class Property {
    public final String value;
    public String uriAttr;
    public String qname;

    public Property(String uriAttr, String qname, String value) {
        this.uriAttr = uriAttr;
        this.qname = qname;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getUriAttr() {
        return uriAttr;
    }

    public String getQname() {
        return qname;
    }
}