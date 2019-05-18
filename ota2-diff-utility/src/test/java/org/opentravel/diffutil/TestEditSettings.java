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

package org.opentravel.diffutil;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;
import org.testfx.api.FxRobot;

import javafx.scene.control.CheckBox;

/**
 * Verifies the ability to edit the application's <code>UserSettings</code> through the UI preferences.
 */
public class TestEditSettings extends AbstractFxTest {

    public static final boolean RUN_HEADLESS = false;

    @Test
    public void testCompareUnmanagedLibraries() throws Exception {
        boolean suppressFieldVersionChanges;
        boolean suppressLibraryPropertyChanges;
        boolean suppressDocumentationChanges;
        UserSettings settings;
        FxRobot dialogRobot;

        robot.clickOn( "#settingsButton" );

        dialogRobot = robot.targetWindow( "Model Comparison Options" );
        dialogRobot.clickOn( "#suppressFieldVersionChangesCB" );
        dialogRobot.clickOn( "#suppressLibraryPropertyChangesCB" );
        dialogRobot.clickOn( "#suppressDocumentationChangesCB" );
        suppressFieldVersionChanges =
            ((CheckBox) dialogRobot.lookup( "#suppressFieldVersionChangesCB" ).query()).isSelected();
        suppressLibraryPropertyChanges =
            ((CheckBox) dialogRobot.lookup( "#suppressLibraryPropertyChangesCB" ).query()).isSelected();
        suppressDocumentationChanges =
            ((CheckBox) dialogRobot.lookup( "#suppressDocumentationChangesCB" ).query()).isSelected();
        dialogRobot.clickOn( "Ok" );

        settings = UserSettings.load();
        assertEquals( suppressFieldVersionChanges, settings.getCompareOptions().isSuppressFieldVersionChanges() );
        assertEquals( suppressLibraryPropertyChanges, settings.getCompareOptions().isSuppressLibraryPropertyChanges() );
        assertEquals( suppressDocumentationChanges, settings.getCompareOptions().isSuppressDocumentationChanges() );
    }

    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getApplicationClass()
     */
    @Override
    protected Class<? extends AbstractOTMApplication> getApplicationClass() {
        return OTMDiffApplication.class;
    }

    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getBackgroundTaskNodeQuery()
     */
    @Override
    protected String getBackgroundTaskNodeQuery() {
        return null;
    }

    /**
     * Configure headless/normal mode for TestFX execution.
     */
    static {
        TestFxMode.setHeadless( RUN_HEADLESS );
    }

}
