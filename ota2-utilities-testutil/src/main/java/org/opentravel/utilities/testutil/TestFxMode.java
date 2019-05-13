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

package org.opentravel.utilities.testutil;

import java.util.Arrays;

/**
 * Configures the system properties that determine if a JavaFX application should run in headless or normal UI mode.
 */
public class TestFxMode {

    public static final String HEADLESS_SYSPROP = "headless";

    private static final String TESTFX_ROBOT = "testfx.robot";
    private static final String TESTFX_HEADLESS = "testfx.headless";
    private static final String PRISM_ORDER = "prism.order";
    private static final String PRISM_TEXT = "prism.text";

    /**
     * Configures the system properties to run in headless or normal UI mode. If the system property 'headless' is
     * assigned, the value passed to this method is ignored in favor of the command-line directive.
     * 
     * @param headless flag indicating the JavaFX UI mode (true = headless; false = normal)
     */
    public static void setHeadless(boolean headless) {
        boolean runHeadless;

        if (System.getProperties().containsKey( HEADLESS_SYSPROP )) {
            runHeadless = Boolean.valueOf( System.getProperty( HEADLESS_SYSPROP ) );

        } else {
            runHeadless = headless;
        }

        if (runHeadless) {
            System.setProperty( TESTFX_HEADLESS, "true" );
            System.setProperty( TESTFX_ROBOT, "glass" );
            System.setProperty( PRISM_ORDER, "sw" );
            System.setProperty( PRISM_TEXT, "t2k" );

        } else {
            Arrays.asList( TESTFX_ROBOT, TESTFX_HEADLESS, PRISM_ORDER, PRISM_TEXT )
                .forEach( key -> System.clearProperty( key ) );
        }
    }

}
