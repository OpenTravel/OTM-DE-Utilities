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
 * Indicates whether or not a node in the original DOM tree was referenced for content in the upgrade tree.
 */
public enum ReferenceStatus {

    REFERENCED(0),

    NOT_REFERENCED(1);

    private static List<String> styleClasses = Arrays.asList( "is-referenced", "not-referenced" );

    private int styleIdx;

    /**
     * Returns the index of the CSS style that corresponds to the status value.
     * 
     * @param styleIdx the list index of the style class for this value
     */
    private ReferenceStatus(int styleIdx) {
        this.styleIdx = styleIdx;
    }

    /**
     * Returns the list of all possible style classes associated with this enumeration.
     * 
     * @return List&lt;String&gt;
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

}
