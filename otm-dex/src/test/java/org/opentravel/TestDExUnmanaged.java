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

package org.opentravel;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.member.MemberAndProvidersDAO;
import org.opentravel.dex.controllers.member.properties.PropertiesDAO;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;

/**
 * Verifies the functions of the <code>ObjectEditorApp</code> application when working with unmanaged OTM libraries and
 * projects.
 */
public class TestDExUnmanaged extends AbstractFxTest {
    private static Log log = LogFactory.getLog( TestDExUnmanaged.class );

    public static final boolean RUN_HEADLESS = false;
    final int WATCH_TIME = 0; // How long to sleep so we can see what is happening.

    final String FXID_PROJECTCOMBO = "#projectCombo"; // if .projectCombo that would be css selector
    final String FILE_TESTPROJECT2 = "TestProject2.otp";
    final String FXID_PROPERTIESTABLE = "#propertiesTable";
    final String FXID_MEMBERTREE = "#memberTree";

    private ApplicationTest at;

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestDExUnmanaged.class );
    }

    @Test
    public void tabTest() throws Exception {
        robot.clickOn( "Properties" );
        robot.clickOn( "Where Used" );
        robot.clickOn( "Libraries" );
        robot.clickOn( "Repository" );
        robot.clickOn( "Repository Login" );
        robot.targetWindow( "Login Dialog" ).clickOn( "Cancel" );
    }

    @Test
    public void projectTest() throws Exception {
        DexMainController controller = (DexMainController) application.getController();
        int bgTasks = controller.getStatusController().getQueueSize();
        assertTrue( bgTasks == 0 );

        robot.clickOn( FXID_PROJECTCOMBO );
        robot.clickOn( FILE_TESTPROJECT2 );

        do {
            robot.sleep( 100 );
            bgTasks = controller.getStatusController().getQueueSize();
        } while (bgTasks > 0);

        robot.sleep( 5 * WATCH_TIME );
        WaitForAsyncUtils.waitForFxEvents(); // make sure the event queue is empty

        Node price = robot.lookup( "Price" ).query();
        WaitForAsyncUtils.waitForFxEvents(); // make sure the event queue is empty
        assertNotNull( "Must find Price column.", price );

        // verifyThat( FXID_MEMBERTREE, (TreeTableView<MemberAndProvidersDAO> treeTableView) -> {
        // return true;
        // } );
        // robot.verifyThat( FXID_MEMBERTREE, NodeMatchers.hasChild( "Price" ) );

        WaitForAsyncUtils.waitForFxEvents(); // make sure the event queue is empty
        TreeTableView<MemberAndProvidersDAO> members = robot.lookup( FXID_MEMBERTREE ).query();
        assertNotNull( members );
        assertTrue( "Must have children in the members.", members.getRoot().getChildren().size() > 4 );
        members.getSelectionModel().select( 1 );
        robot.sleep( WATCH_TIME );
        members.getSelectionModel().select( 4 );

        robot.sleep( WATCH_TIME );

        WaitForAsyncUtils.waitForFxEvents(); // make sure the event queue is empty
        TreeTableView<PropertiesDAO> properties = robot.lookup( FXID_PROPERTIESTABLE ).query();
        assertNotNull( properties );
        assertTrue( "Must have children in the tree.", !properties.getRoot().getChildren().isEmpty() );
        for (TreeItem<PropertiesDAO> c : properties.getRoot().getChildren()) {
            properties.getSelectionModel().select( c );
            robot.sleep( WATCH_TIME );
            log.debug( properties.getSelectionModel().getSelectedItem().getValue().nameProperty().getValue() );
        }
        // properties.getSelectionModel().select( 1 );
        // properties.getSelectionModel().select( 3 );
        // robot.sleep( WATCH_TIME );

        log.debug( "Done" );
    }

    // private void selectFirstEntity(String fxid) {
    // robot.clickOn( fxid );
    // robot.type( KeyCode.DOWN );
    // // robot.type( KeyCode.ENTER );
    // WaitForAsyncUtils.waitForFxEvents();
    // }

    @Test
    public void exitTest() throws Exception {
        exit();
    }

    public void exit() {
        // Click on the file menu's exit entry
        robot.clickOn( "File" ).clickOn( "Exit" );
        // Click on popup dialog's exit button
        robot.targetWindow( "Exit" ).clickOn( "Exit" );
    }

    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getApplicationClass()
     */
    @Override
    protected Class<? extends AbstractOTMApplication> getApplicationClass() {
        return ObjectEditorApp.class;
    }

    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getBackgroundTaskNodeQuery()
     */
    @Override
    protected String getBackgroundTaskNodeQuery() {
        return "#libraryText";
    }

    // Suggested by: https://www.youtube.com/watch?v=NG03nNpSmgU
    // Assumes class extends "ApplicationTest" private ApplicationTest at;
    //
    // @After
    // public void afterEachTest() throws TimeoutException {
    // FxToolkit.hideStage();
    //// release(new KeyCode[]{});
    //// release(new MouseButton[]{});
    // }
    // public <T extends Node> T find(final String query) {
    // return (T) lookup(query).queryAll().iterator().next();
    // }


    /**
     * Configure headless/normal mode for TestFX execution.
     */
    static {
        TestFxMode.setHeadless( RUN_HEADLESS );
    }
}

