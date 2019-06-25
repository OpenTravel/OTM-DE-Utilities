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
import org.opentravel.utilities.testutil.OtmFxRobot;
import org.opentravel.utilities.testutil.TestFxMode;
import org.testfx.matcher.base.NodeMatchers;

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
        robot.sleep( 5000 );
        duProcess = controller.getProcess( "Diff Utility" );
        assertNotNull( duProcess );

        // Attempt to launch again (should not error or launch a new process)
        robot.clickOn( "Diff Utility" );
        assertEquals( duProcess, controller.getProcess( "Diff Utility" ) );
        robot.targetWindow( "Already Running" ).type( KeyCode.ENTER );

        duProcess.destroyForcibly(); // Clean up the external process that was launched
    }

    @Test
    public void testEditProxySettings() throws Exception {
        UserSettings settings = UserSettings.load();
        boolean origUseProxy = settings.isUseProxy();
        String origProxyHost = settings.getProxyHost();
        Integer origProxyPort = settings.getProxyPort();
        String origNonProxyHosts = settings.getNonProxyHosts();

        try {
            OtmFxRobot dialogRobot;
            boolean useProxyInd;

            robot.clickOn( "File" ).clickOn( "Proxy Settings..." );
            dialogRobot = robot.targetWindow( "Network Proxy Settings" );

            dialogRobot.clickOn( "#useProxyCB" );
            useProxyInd = ((CheckBox) dialogRobot.lookup( "#useProxyCB" ).query()).isSelected();

            if (!useProxyInd) {
                dialogRobot.clickOn( "#useProxyCB" );
            }
            dialogRobot.write( "#proxyHostText", "proxy.opentravel.org" );
            dialogRobot.write( "#proxyPortText", "8080" );

            dialogRobot.write( "#nonProxyHostsText", "*.opentravel@org" );
            verifyThat( "#okButton", NodeMatchers.isDisabled() );

            dialogRobot.write( "#nonProxyHostsText", "opentravel.*" );
            verifyThat( "#okButton", NodeMatchers.isEnabled() );
            dialogRobot.clickOn( "#okButton" );

            settings = UserSettings.load();
            assertTrue( settings.isUseProxy() );
            assertEquals( "proxy.opentravel.org", settings.getProxyHost() );
            assertEquals( 8080, settings.getProxyPort().intValue() );
            assertEquals( "opentravel.*", settings.getNonProxyHosts() );

        } finally {
            settings.setUseProxy( origUseProxy );
            settings.setProxyHost( origProxyHost );
            settings.setProxyPort( origProxyPort );
            settings.setNonProxyHosts( origNonProxyHosts );
            settings.save();
        }
    }

    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getApplicationClass()
     */
    @Override
    protected Class<? extends AbstractOTMApplication> getApplicationClass() {
        return LauncherApplication.class;
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
