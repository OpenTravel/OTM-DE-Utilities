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

package org.opentravel.dex.controllers.member;

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.dex.controllers.member.filters.NameFilterWidget;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import javafx.scene.control.TextField;

/**
 * Verifies the functions of the <code>SelectProjectDialog</code>
 */
public class TestMemberFilterController extends AbstractFxTest {
    private static Log log = LogFactory.getLog( TestMemberFilterController.class );

    public static final boolean RUN_HEADLESS = true;
    // final int WATCH_TIME = 5000; // How long to sleep so we can see what is happening. Can be 0.

    // final String FXID_PROJECTLIST = "#projectList";
    // final String FXID_LIBTREETABLE = "#librariesTreeTable";

    @BeforeClass
    public static void setupTests() throws Exception {
        // setupWorkInProcessArea( TestMemberFilterController.class );
        // repoManager = repositoryManager.get();
    }

    // @Test
    // public void testSelectProjectSetup() {
    // // testSetup();
    // }

    // public OtmModelManager testSetup() {
    // // OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
    // // // Givens - 2 projects and library that does not belong to project
    // // // Load first and second projects
    // // TestDexFileHandler.loadAndAddManagedProject( mgr );
    // // TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
    // //
    // // assertTrue( mgr.getProjects().size() == 2 );
    // // int libraryCount = mgr.getLibraries().size();
    // // assertTrue( libraryCount > 0 );
    // //
    // // // Library
    // // TestDexFileHandler.loadLocalLibrary( TestDexFileHandler.FILE_TESTLIBRARYNOTINPROJECT, mgr );
    // // mgr.add();
    // // assertTrue( mgr.getLibraries().size() > libraryCount );
    // // libraryCount = mgr.getLibraries().size();
    // // return mgr;
    // }

    @Test
    public void testNameFilterWidget() {
        // MemberFilterController controller = new MemberFilterController();
        TextField memberNameFilter = new TextField();
        NameFilterWidget filter = new NameFilterWidget( null, memberNameFilter );
        assertTrue( filter != null );

        // Given a library with objects
        OtmLibrary lib = TestLibrary.buildOtm();
        TestLibrary.addOneOfEach( lib );

        // When the filter disabled
        for (OtmLibraryMember member : lib.getMembers()) {
            assertTrue( "Object must be selected.", filter.isSelected( member ) );
        }

        // When - the filter is enabled with common string
        memberNameFilter.setText( "t" );
        for (OtmLibraryMember member : lib.getMembers()) {
            assertTrue( "Object must be selected.", filter.isSelected( member ) );
        }

        // When - the filter is enabled with "strange" name
        memberNameFilter.setText( "SomethingXYZZXYABCCBA" );
        for (OtmLibraryMember member : lib.getMembers()) {
            if (member.getName() != null) // Extension point does not have name and is selected
                assertTrue( "Object must NOT be selected.", !filter.isSelected( member ) );
        }

        // OtmModelManager mgr = new OtmModelManager( new DexFullActionManager( null ), repoManager, null );
        // // Givens - projects
        // if (!TestDexFileHandler.loadVersionProject( mgr ))
        // return; // No editable libraries
        // assertTrue( "Given", !mgr.getMembers().isEmpty() );
        //
        // DexMainController controller = (DexMainController) application.getController();
        // int bgTasks = controller.getStatusController().getQueueSize();
        // // assertTrue( bgTasks == 0 );
        //
        // for (OtmLibraryMember member : mgr.getMembers()) {
        // log.debug( "Testing member: " + member + " \t" );
        // TestLibraryMemberBase.checkOwnership( member );
        // PropertiesDAO dao = new PropertiesDAO( member, null );
        // dao.createChildrenItems( null, null );
        // TestLibraryMemberBase.checkOwnership( member );
        // }
        //
        // List<OtmLibrary> chainEditable = new ArrayList<>();
        // for (OtmLibrary lib : mgr.getLibraries())
        // if (lib.isMajorVersion() && lib.getVersionChain().isChainEditable())
        // chainEditable.add( lib );
        // assertTrue( "Given: must have chain editable lib to run test.", !chainEditable.isEmpty() );
        // chainEditable.forEach( l -> TestVersionChain.createMinorMembers( l ) );
        //
        // for (OtmLibraryMember member : mgr.getMembers()) {
        // log.debug( "Testing member: " + member + " \t" );
        // TestLibraryMemberBase.checkOwnership( member );
        // PropertiesDAO dao = new PropertiesDAO( member, null );
        // dao.createChildrenItems( null, null );
        // TestLibraryMemberBase.checkOwnership( member );
        // }
    }

    /** *************************************************************** */
    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getApplicationClass()
     */
    @Override
    protected Class<? extends AbstractOTMApplication> getApplicationClass() {
        return ObjectEditorApp.class;
    }

    /**
     * Configure headless/normal mode for TestFX execution.
     */
    static {
        TestFxMode.setHeadless( RUN_HEADLESS );
    }

    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getBackgroundTaskNodeQuery()
     */
    @Override
    protected String getBackgroundTaskNodeQuery() {
        return "#libraryText";
    }


}
