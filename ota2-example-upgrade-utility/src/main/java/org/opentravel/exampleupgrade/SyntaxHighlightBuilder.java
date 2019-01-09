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

import java.util.Collection;

import org.fxmisc.richtext.model.StyleSpans;

/**
 * Computes the highlighting spans for a BLOCK of text based upon its syntax and content.
 */
public interface SyntaxHighlightBuilder {
	
	/**
	 * Computes the highlighting spans for the given text.
	 * 
	 * @param text  the text for which to compute the highlighting
	 * @return StyleSpans<Collection<String>>
	 */
	public StyleSpans<Collection<String>> computeHighlighting(String text);
	
}
