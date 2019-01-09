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

package org.opentravel.exampleupgrade;

import java.util.Arrays;
import java.util.List;

/**
 * Used to specify the type of match between an existing EXAMPLE DOM element
 * and an associated OTM model entity or field.
 */
public enum ExampleMatchType {
	
	/**
	 * Indicates that an exact match was found between the OTM entity/field and the
	 * DOM element in the existing DOM document.  Exact matches exist under
	 * the following conditions:
	 * <ul>
	 *   <li>XML Elements - both the namespace and local names of the DOM element exactly match that of the OTM model.</li>
	 *   <li>JSON Elements - the local name of the DOM element matches that of the OTM model.</li>
	 *   <li>XML/JSON Attributes &amp; Indicators - the name of the attribute or indicator matches that of the OTM model.</li>
	 * </ul>
	 */
	EXACT( 0 ),
	
	/**
	 * Same as the <code>EXACT</code> match, but for the substitutable element of an OTM facet.
	 */
	EXACT_SUBSTITUTABLE( 0 ),
	
	/**
	 * Indicates that an partial match was found between the OTM entity/field and the
	 * DOM element in the existing DOM document.  Partial matches exist under the
	 * following conditions:
	 * <ul>
	 *   <li>XML Elements - the base namespaces (version independent) and local names of the DOM element exactly match that of the OTM model.</li>
	 *   <li>JSON Elements - due to the lack of a namespace designation, a partial match for JSON examples is not possible</li>
	 *   <li>XML/JSON Attributes &amp; Indicators - the name of the attribute or indicator matches that of the OTM model.</li>
	 * </ul>
	 */
	PARTIAL( 1 ),
	
	/**
	 * Same as the <code>PARTIAL</code> match, but for the substitutable element of an OTM facet.
	 */
	PARTIAL_SUBSTITUTABLE( 1 ),
	
	/**
	 * Indicates that no associated DOM element could be identified for the OTM model
	 * element/field, and that EXAMPLE content has been generated automatically.
	 */
	NONE( 2 ),
	
	/**
	 * Indicates that no associated DOM element could be identified for the OTM model
	 * element/field, and no EXAMPLE content was generated.
	 */
	MISSING( 3 ),
	
	/**
	 * Indicates that the associated DOM element is only considered a match because the
	 * user explicitly selected it.
	 */
	MANUAL( 4 );
	
	private static List<String> styleClasses = Arrays.asList(
			"exact-match", "partial-match", "no-match", "missing-match", "manual-match" );
	
	private int styleIdx;
	
	/**
	 * Returns the index of the CSS style that corresponds to the match type.
	 * 
	 * @param styleIdx  the list index of the style class for this value
	 */
	private ExampleMatchType(int styleIdx) {
		this.styleIdx = styleIdx;
	}
	
	/**
	 * Returns the list of all possible style classes associated with this enumeration.
	 * 
	 * @return List<String>
	 */
	public static List<String> getAllStyleClasses() {
		return styleClasses;
	}
	
	/**
	 * Returns the CSS style that corresponds to the match type.
	 * 
	 * @return String
	 */
	public String getStyleClass() {
		return styleClasses.get( styleIdx );
	}
	
	/**
	 * Returns true as long as the given match type is not null, <code>NONE</code>,
	 * or <code>MISSING</code>.
	 * 
	 * @param matchType  the match type to analyze
	 * @return boolean
	 */
	public static boolean isMatch(ExampleMatchType matchType) {
		return (matchType != null) && (matchType != NONE) && (matchType != MISSING);
	}
	
}
