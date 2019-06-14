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

package org.opentravel.application.common;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Syntax highlighting builder for XML documents.
 */
public class XmlHighlightBuilder implements SyntaxHighlightBuilder {

    private static final Pattern xmlTagPattern =
        Pattern.compile( "(?<ELEMENT>(</?\\h*)(\\w+)([^<>]*)(\\h*/?>))|(?<COMMENT><!--[^<>]+-->)" );
    private static final Pattern attributesPattern = Pattern.compile( "(\\w+\\h*)(=)(\\h*\"[^\"]+\")" );

    private static final String XML_AVALUE = "xml_avalue";
    private static final String XML_ATTRIBUTE = "xml_attribute";
    private static final String XML_ANYTAG = "xml_anytag";
    private static final String XML_TAGMARK = "xml_tagmark";
    private static final String XML_COMMENT = "xml_comment";

    private static final int GROUP_OPEN_BRACKET = 2;
    private static final int GROUP_ELEMENT_NAME = 3;
    private static final int GROUP_ATTRIBUTES_SECTION = 4;
    private static final int GROUP_CLOSE_BRACKET = 5;
    private static final int GROUP_ATTRIBUTE_NAME = 1;
    private static final int GROUP_EQUAL_SYMBOL = 2;
    private static final int GROUP_ATTRIBUTE_VALUE = 3;

    /**
     * @see org.opentravel.application.common.SyntaxHighlightBuilder#computeHighlighting(java.lang.String)
     */
    @Override
    public StyleSpans<Collection<String>> computeHighlighting(String text) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        Matcher matcher = xmlTagPattern.matcher( text );
        int lastMatchEnd = 0;

        while (matcher.find()) {
            spansBuilder.add( Collections.emptyList(), matcher.start() - lastMatchEnd );

            if (matcher.group( "COMMENT" ) != null) {
                spansBuilder.add( Collections.singleton( XML_COMMENT ), matcher.end() - matcher.start() );

            } else {
                if (matcher.group( "ELEMENT" ) != null) {
                    computeElementHighlighting( matcher, spansBuilder );
                }
            }
            lastMatchEnd = matcher.end();
        }
        spansBuilder.add( Collections.emptyList(), text.length() - lastMatchEnd );
        return spansBuilder.create();
    }

    /**
     * Computes highlighting for an XML element.
     * 
     * @param matcher the matcher that identified the XML element
     * @param spansBuilder builder for the highlighted sections of text
     */
    private void computeElementHighlighting(Matcher matcher, StyleSpansBuilder<Collection<String>> spansBuilder) {
        String attributesText = matcher.group( GROUP_ATTRIBUTES_SECTION );
        int lastMatchEnd;

        spansBuilder.add( Collections.singleton( XML_TAGMARK ),
            matcher.end( GROUP_OPEN_BRACKET ) - matcher.start( GROUP_OPEN_BRACKET ) );
        spansBuilder.add( Collections.singleton( XML_ANYTAG ),
            matcher.end( GROUP_ELEMENT_NAME ) - matcher.end( GROUP_OPEN_BRACKET ) );

        if (!attributesText.isEmpty()) {
            Matcher amatcher = attributesPattern.matcher( attributesText );

            lastMatchEnd = 0;

            while (amatcher.find()) {
                spansBuilder.add( Collections.emptyList(), amatcher.start() - lastMatchEnd );
                spansBuilder.add( Collections.singleton( XML_ATTRIBUTE ),
                    amatcher.end( GROUP_ATTRIBUTE_NAME ) - amatcher.start( GROUP_ATTRIBUTE_NAME ) );
                spansBuilder.add( Collections.singleton( XML_TAGMARK ),
                    amatcher.end( GROUP_EQUAL_SYMBOL ) - amatcher.end( GROUP_ATTRIBUTE_NAME ) );
                spansBuilder.add( Collections.singleton( XML_AVALUE ),
                    amatcher.end( GROUP_ATTRIBUTE_VALUE ) - amatcher.end( GROUP_EQUAL_SYMBOL ) );
                lastMatchEnd = amatcher.end();
            }
            if (attributesText.length() > lastMatchEnd) {
                spansBuilder.add( Collections.emptyList(), attributesText.length() - lastMatchEnd );
            }
        }
        lastMatchEnd = matcher.end( GROUP_ATTRIBUTES_SECTION );
        spansBuilder.add( Collections.singleton( XML_TAGMARK ), matcher.end( GROUP_CLOSE_BRACKET ) - lastMatchEnd );
    }

}
