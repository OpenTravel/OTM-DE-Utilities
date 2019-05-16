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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;
import org.opentravel.utilities.testutil.TestFxUtils;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;

import javafx.scene.input.KeyCode;

/**
 * Verifies the functions of the <code>ExampleHelper</code> application that require artifacts managed by a remote OTM
 * repository.
 */
public class TestExampleHelperManaged extends AbstractFxTest {

    public static final boolean RUN_HEADLESS = false;

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestExampleHelperManaged.class );
        startTestServer( "examples-repository", 9481, repositoryConfig, true, false, TestExampleHelperManaged.class );
        repoManager = repositoryManager.get();
    }

    @AfterClass
    public static void tearDownTests() throws Exception {
        shutdownTestServer();
    }

    @Test
    public void testLoadManagedLibrary() throws Exception {
        robot.clickOn( "#libraryRepoButton" );
        TestFxUtils.selectTreeItem( robot.targetWindow( "Open Library or Release" ), "#repositoryTreeView",
            "OTM Repositories", "OTA2.0 Test Repository", "http://www.OpenTravel.org",
            "/ns/OTA2/SchemaCompiler/example-test", "Example_Test_1_0_0.otm (1.0.0)" );
        robot.targetWindow( "Open Library or Release" ).clickOn( "#okButton" );
        TestFxUtils.waitUntilEnabled( robot, "#libraryText", 10 );

        selectFirstEntity();
    }

    @Test
    public void testLoadManagedRelease() throws Exception {
        robot.clickOn( "#libraryRepoButton" );
        TestFxUtils.selectTreeItem( robot.targetWindow( "Open Library or Release" ), "#repositoryTreeView",
            "OTM Repositories", "OTA2.0 Test Repository", "http://www.OpenTravel.org",
            "/ns/OTA2/SchemaCompiler/example-test", "Example_Release_1_0_0.otr (1.0.0)" );
        robot.targetWindow( "Open Library or Release" ).clickOn( "#okButton" );
        TestFxUtils.waitUntilEnabled( robot, "#libraryText", 10 );

        selectFirstEntity();
    }

    @Test
    public void testLoadUnmanagedRelease() throws Exception {
        File projectFile = new File( wipFolder.get(), "/test-release.otr" );

        when( mockFileChooser.showOpenDialog( any() ) ).thenReturn( projectFile );

        robot.clickOn( "#libraryFileButton" );
        WaitForAsyncUtils.waitForFxEvents();
        TestFxUtils.waitUntilEnabled( robot, "#libraryText", 10 );

        selectFirstEntity();
    }

    /**
     * After a model has been loaded, this method selects the first available entity from the available list.
     */
    private void selectFirstEntity() {
        robot.clickOn( "#entityChoice" );
        robot.type( KeyCode.DOWN );
        robot.type( KeyCode.ENTER );
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getApplicationClass()
     */
    @Override
    protected Class<? extends AbstractOTMApplication> getApplicationClass() {
        return ExampleHelperApplication.class;
    }

    /**
     * Configure headless/normal mode for TestFX execution.
     */
    static {
        TestFxMode.setHeadless( RUN_HEADLESS );
    }

}
