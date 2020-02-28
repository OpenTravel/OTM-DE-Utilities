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

package org.opentravel.dex.controllers.member.properties;

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.TestDexFileHandler;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.TestOtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestVersionChain;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.TestLibraryMemberBase;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Verifies the functions of the <code>SelectProjectDialog</code>
 */
public class TestMemberPropertiesController extends AbstractFxTest {

    private static Log log = LogFactory.getLog( TestOtmModelManager.class );

    public static final boolean RUN_HEADLESS = true;
    final int WATCH_TIME = 5000; // How long to sleep so we can see what is happening. Can be 0.

    final String FXID_PROJECTLIST = "#projectList";
    final String FXID_LIBTREETABLE = "#librariesTreeTable";

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestMemberPropertiesController.class );
        repoManager = repositoryManager.get();
    }

    @Test
    public void testSelectProjectSetup() {
        // testSetup();
    }

    public OtmModelManager testSetup() {
        OtmModelManager mgr = new OtmModelManager( null, repoManager );
        // Givens - 2 projects and library that does not belong to project
        // Load first and second projects
        TestDexFileHandler.loadAndAddManagedProject( mgr );
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );

        assertTrue( mgr.getUserProjects().size() == 2 );
        int libraryCount = mgr.getLibraries().size();
        assertTrue( libraryCount > 0 );

        // Library
        TestDexFileHandler.loadLocalLibrary( TestDexFileHandler.FILE_TESTLIBRARYNOTINPROJECT, mgr.getTlModel() );
        mgr.add();
        assertTrue( mgr.getLibraries().size() > libraryCount );
        libraryCount = mgr.getLibraries().size();
        return mgr;
    }

    @Test
    public void testPropertiesDAO_MinorMembers() {
        OtmModelManager mgr = new OtmModelManager( new DexFullActionManager( null ), repoManager );
        // Givens - projects
        if (!TestDexFileHandler.loadVersionProject( mgr ))
            return; // No editable libraries
        assertTrue( "Given", !mgr.getMembers().isEmpty() );

        DexMainController controller = (DexMainController) application.getController();
        int bgTasks = controller.getStatusController().getQueueSize();
        // assertTrue( bgTasks == 0 );

        for (OtmLibraryMember member : mgr.getMembers()) {
            log.debug( "Testing member: " + member + "  \t" );
            TestLibraryMemberBase.checkOwnership( member );
            PropertiesDAO dao = new PropertiesDAO( member, null );
            dao.createChildrenItems( null, null );
            TestLibraryMemberBase.checkOwnership( member );
        }

        List<OtmLibrary> chainEditable = new ArrayList<>();
        for (OtmLibrary lib : mgr.getLibraries())
            if (lib.isMajorVersion() && lib.getVersionChain().isChainEditable())
                chainEditable.add( lib );
        assertTrue( "Given: must have chain editable lib to run test.", !chainEditable.isEmpty() );
        chainEditable.forEach( l -> TestVersionChain.createMinorMembers( l ) );

        for (OtmLibraryMember member : mgr.getMembers()) {
            log.debug( "Testing member: " + member + "  \t" );
            TestLibraryMemberBase.checkOwnership( member );
            PropertiesDAO dao = new PropertiesDAO( member, null );
            dao.createChildrenItems( null, null );
            TestLibraryMemberBase.checkOwnership( member );
        }
    }

    @Test
    public void testPropertiesDAO_MinorTypeUsers() throws VersionSchemeException {
        OtmModelManager mgr = new OtmModelManager( new DexFullActionManager( null ), repoManager );
        // Given - vesioned project
        if (!TestDexFileHandler.loadVersionProject( mgr ))
            return; // No editable libraries
        assertTrue( "Given", !mgr.getMembers().isEmpty() );

        DexMainController controller = (DexMainController) application.getController();
        int bgTasks = controller.getStatusController().getQueueSize();
        // assertTrue( bgTasks == 0 );

        // Get the chain with major and minor
        // String BASENS0 = "http://www.opentravel.org/Sandbox/Test/VersionTest_Unmanaged";
        OtmLibrary latestLib = null;
        int highestMajor = 0;
        for (OtmLibrary lib : mgr.getLibraries()) {
            if (lib.isBuiltIn())
                continue;
            if (lib.getMajorVersion() > highestMajor)
                highestMajor = lib.getMajorVersion();
            if (lib.isLatestVersion())
                latestLib = lib;
        }
        assertTrue( latestLib != null );
        OtmLibrary major = latestLib.getVersionChain().getMajor();
        assertTrue( major != null );

        // Pre-check
        for (OtmLibraryMember member : mgr.getMembers()) {
            // log.debug( "Testing member: " + member + " \t" );
            TestLibraryMemberBase.checkOwnership( member );
            PropertiesDAO dao = new PropertiesDAO( member, null );
            dao.createChildrenItems( null, null );
            TestLibraryMemberBase.checkOwnership( member );
        }


        for (OtmLibraryMember member : mgr.getMembers( major )) {
            // Get a type user for the member
            OtmTypeUser user = null;
            List<OtmTypeUser> users = new ArrayList<>( member.getDescendantsTypeUsers() );
            for (OtmTypeUser tu : users)
                user = tu;
            if (user == null) {
                log.debug( "Could find type user of :" + member );
                continue;
            }
            // Get a new version of the user and its owning library member
            OtmTypeUser newUser = null;
            newUser = major.getVersionChain().getNewMinorTypeUser( user );
            if (newUser == null) {
                log.debug( "Could not create new member with type user :" + member );
                continue;
            }

            // Then - test the new and old members
            OtmLibraryMember newOwner = newUser.getOwningMember();
            log.debug( "Testing new owner " + newOwner );
            TestLibraryMemberBase.checkOwnership( newOwner );
            TestLibraryMemberBase.checkOwnership( member );
            PropertiesDAO dao = new PropertiesDAO( member, null );
            dao.createChildrenItems( null, null );
            TestLibraryMemberBase.checkOwnership( newOwner );
            TestLibraryMemberBase.checkOwnership( member );


            latestLib.delete( newOwner );
            TestLibraryMemberBase.checkOwnership( member );
        }


        // Follow-up check
        for (OtmLibraryMember member : mgr.getMembers()) {
            // log.debug( "Testing member: " + member + " \t" );
            TestLibraryMemberBase.checkOwnership( member );
            PropertiesDAO dao = new PropertiesDAO( member, null );
            dao.createChildrenItems( null, null );
            TestLibraryMemberBase.checkOwnership( member );
        }
    }

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
