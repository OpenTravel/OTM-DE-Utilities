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

package org.opentravel.launcher;

import org.junit.Rule;
import org.junit.Test;
import org.opentravel.utilities.testutil.TestFxMode;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationRule;

import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Verifies the correct operation of the OTM-DE-Utilities launcher application.
 */
public class TestLauncherApplication {

    public static final boolean RUN_HEADLESS = true;

    @Rule
    public ApplicationRule robot = new ApplicationRule( stage -> {
        new LauncherApplication().start( stage );
        primaryStage = stage;
    } );

    private Stage primaryStage;

    /**
     * Configure headless/normal mode for TestFX execution.
     */
    static {
        TestFxMode.setHeadless( RUN_HEADLESS );
    }

    @Test
    public void testOpenAndClose() throws Exception {
        // TODO: Assert that correct appliation buttons exist
        FxToolkit.setupFixture(
            () -> primaryStage.fireEvent( new WindowEvent( primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST ) ) );
    }

}
