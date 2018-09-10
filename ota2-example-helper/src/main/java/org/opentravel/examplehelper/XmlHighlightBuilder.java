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

package org.opentravel.examplehelper;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

/**
 * Syntax highlighting builder for XML documents.
 */
public class XmlHighlightBuilder implements SyntaxHighlightBuilder {
	
    private static final Pattern xmlTagPattern = Pattern.compile( "(?<ELEMENT>(</?\\h*)(\\w+)([^<>]*)(\\h*/?>))|(?<COMMENT><!--[^<>]+-->)" );
    private static final Pattern attributesPattern = Pattern.compile( "(\\w+\\h*)(=)(\\h*\"[^\"]+\")" );
    
    private static final int GROUP_OPEN_BRACKET       = 2;
    private static final int GROUP_ELEMENT_NAME       = 3;
    private static final int GROUP_ATTRIBUTES_SECTION = 4;
    private static final int GROUP_CLOSE_BRACKET      = 5;
    private static final int GROUP_ATTRIBUTE_NAME     = 1;
    private static final int GROUP_EQUAL_SYMBOL       = 2;
    private static final int GROUP_ATTRIBUTE_VALUE    = 3;
    
	/**
	 * @see org.opentravel.examplehelper.SyntaxHighlightBuilder#computeHighlighting(java.lang.String)
	 */
	@Override
	public StyleSpans<Collection<String>> computeHighlighting(String text) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        Matcher matcher = xmlTagPattern.matcher( text );
        int lastMatchEnd = 0;
        
        while (matcher.find()) {
        	spansBuilder.add( Collections.emptyList(), matcher.start() - lastMatchEnd );
        	
        	if (matcher.group( "COMMENT" ) != null) {
        		spansBuilder.add( Collections.singleton( "xml_comment" ), matcher.end() - matcher.start() );
        		
        	} else {
        		if (matcher.group( "ELEMENT" ) != null) {
        			String attributesText = matcher.group( GROUP_ATTRIBUTES_SECTION );
        			
        			spansBuilder.add( Collections.singleton( "xml_tagmark" ), matcher.end( GROUP_OPEN_BRACKET ) - matcher.start( GROUP_OPEN_BRACKET ) );
        			spansBuilder.add(Collections.singleton( "xml_anytag" ), matcher.end( GROUP_ELEMENT_NAME ) - matcher.end( GROUP_OPEN_BRACKET ) );

        			if (!attributesText.isEmpty()) {
        				Matcher amatcher = attributesPattern.matcher( attributesText );
        				
        				lastMatchEnd = 0;
        				
        				while (amatcher.find()) {
        					spansBuilder.add( Collections.emptyList(), amatcher.start() - lastMatchEnd );
        					spansBuilder.add( Collections.singleton( "xml_attribute" ), amatcher.end( GROUP_ATTRIBUTE_NAME ) - amatcher.start( GROUP_ATTRIBUTE_NAME ) );
        					spansBuilder.add( Collections.singleton( "xml_tagmark" ), amatcher.end( GROUP_EQUAL_SYMBOL ) - amatcher.end( GROUP_ATTRIBUTE_NAME ) );
        					spansBuilder.add( Collections.singleton( "xml_avalue" ), amatcher.end( GROUP_ATTRIBUTE_VALUE ) - amatcher.end( GROUP_EQUAL_SYMBOL ) );
        					lastMatchEnd = amatcher.end();
        				}
        				if (attributesText.length() > lastMatchEnd) {
        					spansBuilder.add( Collections.emptyList(), attributesText.length() - lastMatchEnd );
        				}
        			}
        			lastMatchEnd = matcher.end( GROUP_ATTRIBUTES_SECTION );
        			spansBuilder.add( Collections.singleton("xml_tagmark"), matcher.end( GROUP_CLOSE_BRACKET ) - lastMatchEnd );
        		}
        	}
        	lastMatchEnd = matcher.end();
        }
        spansBuilder.add( Collections.emptyList(), text.length() - lastMatchEnd );
        return spansBuilder.create();
	}
	
}
