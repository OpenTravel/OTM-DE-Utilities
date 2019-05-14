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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.testfx.api.FxAssert.verifyThat;

import org.junit.Test;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;
import org.opentravel.utilities.testutil.TestFxUtils;
import org.testfx.api.FxRobot;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.CheckBox;
import javafx.scene.input.KeyCode;

/**
 * Verifies the correct operation of the OTM-DE-Utilities launcher application.
 */
public class TestLauncherApplication extends AbstractFxTest {

    public static final boolean RUN_HEADLESS = false;

    @Test
    public void testOpenAndClose() throws Exception {
        robot.clickOn( "File" ).clickOn( "Exit" );
    }

    @Test
    public void testAboutDialog() throws Exception {
        robot.clickOn( "Help" ).clickOn( "About" );
        robot.targetWindow( "About" ).clickOn( "Close" );
    }

    @Test
    public void testLaunchApplication() throws Exception {
        assumeFalse( TestFxMode.isCIBuildEnvironment() );
        LauncherController controller = (LauncherController) application.getController();
        Process duProcess;

        // Launch an application process
        controller.setLaunchHeadless( true );
        robot.clickOn( "Diff Utility" );
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep( 5000 );
        duProcess = controller.getProcess( "Diff Utility" );
        assertNotNull( duProcess );

        // Attempt to launch again (should not error or launch a new process)
        robot.clickOn( "Diff Utility" );
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals( duProcess, controller.getProcess( "Diff Utility" ) );
        robot.targetWindow( "Already Running" ).type( KeyCode.ENTER );

        duProcess.destroyForcibly(); // Clean up the external process that was launched
    }

    @Test
    public void testEditProxySettings() throws Exception {
        UserSettings settings;
        boolean useProxyInd;
        FxRobot dialogRobot;

        robot.clickOn( "File" ).clickOn( "Proxy Settings..." );
        WaitForAsyncUtils.waitForFxEvents();
        dialogRobot = robot.targetWindow( "Network Proxy Settings" );

        dialogRobot.clickOn( "#useProxyCB" );
        WaitForAsyncUtils.waitForFxEvents();
        useProxyInd = ((CheckBox) dialogRobot.lookup( "#useProxyCB" ).query()).isSelected();

        if (!useProxyInd) {
            dialogRobot.clickOn( "#useProxyCB" );
        }
        TestFxUtils.typeText( robot, "#proxyHostText", "proxy.opentravel.org", true );
        TestFxUtils.typeText( robot, "#proxyPortText", "8080", true );

        TestFxUtils.typeText( robot, "#nonProxyHostsText", "*.opentravel@org", true );
        verifyThat( "#okButton", NodeMatchers.isDisabled() );

        TestFxUtils.typeText( robot, "#nonProxyHostsText", "opentravel.*", true );
        verifyThat( "#okButton", NodeMatchers.isEnabled() );
        robot.clickOn( "#okButton" );
        WaitForAsyncUtils.waitForFxEvents();

        settings = UserSettings.load();
        assertTrue( settings.isUseProxy() );
        assertEquals( "proxy.opentravel.org", settings.getProxyHost() );
        assertEquals( 8080, settings.getProxyPort().intValue() );
        assertEquals( "opentravel.*", settings.getNonProxyHosts() );
    }

    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getApplicationClass()
     */
    @Override
    protected Class<? extends AbstractOTMApplication> getApplicationClass() {
        return LauncherApplication.class;
    }

    /**
     * Configure headless/normal mode for TestFX execution.
     */
    static {
        TestFxMode.setHeadless( RUN_HEADLESS );
    }

}
