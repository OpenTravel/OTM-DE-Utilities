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
public class JsonHighlightBuilder implements SyntaxHighlightBuilder {

    private static final Pattern jsonTokenPattern =
        Pattern.compile( "\\s*(\\{|\\}|\\[|\\])|(\\.|,)|(\\\".*?\\\"\\s*\\:)|(\\\".*?\\\")|(-?\\d+(?:\\.\\d+)?"
            + "(?:[eE][+-]?\\d+)?)|(true|false|null)" );

    private static final int GROUP_BRACKET_OR_BRACE = 1;
    private static final int GROUP_DELIMETER = 2;
    private static final int GROUP_TERM_NAME = 3;
    private static final int GROUP_STRING_VALUE = 4;
    private static final int GROUP_NUMBER_VALUE = 5;
    private static final int GROUP_OTHER_VALUE = 6;

    /**
     * @see org.opentravel.application.common.SyntaxHighlightBuilder#computeHighlighting(java.lang.String)
     */
    @Override
    public StyleSpans<Collection<String>> computeHighlighting(String text) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        Matcher matcher = jsonTokenPattern.matcher( text );
        int lastMatchEnd = 0;

        while (matcher.find()) {
            spansBuilder.add( Collections.emptyList(), matcher.start() - lastMatchEnd );

            if (matcher.group( GROUP_BRACKET_OR_BRACE ) != null) {
                spansBuilder.add( Collections.singleton( "json_braceBracket" ), matcher.end() - matcher.start() );

            } else if (matcher.group( GROUP_DELIMETER ) != null) {
                spansBuilder.add( Collections.singleton( "json_delimeter" ), matcher.end() - matcher.start() );

            } else if (matcher.group( GROUP_TERM_NAME ) != null) {
                spansBuilder.add( Collections.singleton( "json_termName" ), matcher.end() - matcher.start() );

            } else if (matcher.group( GROUP_STRING_VALUE ) != null) {
                spansBuilder.add( Collections.singleton( "json_stringValue" ), matcher.end() - matcher.start() );

            } else if (matcher.group( GROUP_NUMBER_VALUE ) != null) {
                spansBuilder.add( Collections.singleton( "json_numberValue" ), matcher.end() - matcher.start() );

            } else if (matcher.group( GROUP_OTHER_VALUE ) != null) {
                spansBuilder.add( Collections.singleton( "json_otherValue" ), matcher.end() - matcher.start() );
            }
            lastMatchEnd = matcher.end();
        }

        spansBuilder.add( Collections.emptyList(), text.length() - lastMatchEnd );
        return spansBuilder.create();
    }

}
