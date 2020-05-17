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
