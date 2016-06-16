package no.acando.xmltordf.doclet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by havardottestad on 16/06/16.
 */
public class Method {

	String name;
	String description;

	List<Example> examples = new ArrayList<>();

	public void addExample(Example currentExample) {
		examples.add(currentExample);
	}
}
