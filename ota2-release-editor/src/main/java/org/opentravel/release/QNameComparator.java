/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.release;

import java.util.Comparator;

import javax.xml.namespace.QName;

/**
 * Comparator that sorts qualified names by namespace then by local name.
 */
public class QNameComparator implements Comparator<QName> {
	
	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(QName qn1, QName qn2) {
		String ns1 = nullToBlank( (qn1 == null) ? null : qn1.getNamespaceURI() );
		String ns2 = nullToBlank( (qn2 == null) ? null : qn1.getNamespaceURI() );
		int result = ns1.compareTo( ns2 );
		
		if (result == 0) {
			String local1 = nullToBlank( (qn1 == null) ? null : qn1.getLocalPart() );
			String local2 = nullToBlank( (qn2 == null) ? null : qn1.getLocalPart() );
			
			result = local1.compareTo( local2 );
		}
		return result;
	}
	
	/**
	 * If the given string is null, returns an empty string.  Otherwise, returns
	 * the original string.
	 * 
	 * @param str  the string to be processed
	 * @return String
	 */
	private String nullToBlank(String str) {
		return (str == null) ? "" : str;
	}
	
}
