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

package org.opentravel.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.AbstractDexTest;
import org.opentravel.TestDexFileHandler;
import org.opentravel.application.common.AbstractMainWindowController;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.common.DexProjectException;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmLocalLibrary;
import org.opentravel.model.otmContainers.OtmMajorLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.model.otmContainers.OtmVersionChainEmpty;
import org.opentravel.model.otmContainers.OtmVersionChainVersioned;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmContainers.TestOtmVersionChain;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.objecteditor.ObjectEditorController;
import org.opentravel.objecteditor.UserSettings;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.PublishWithLocalDependenciesException;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Verifies the functions of the <code>Otm Model Manager</code>.
 */
// @Ignore
public class TestOtmModelManager extends AbstractDexTest {
    private static Log log = LogFactory.getLog( TestOtmModelManager.class );

    // final static String FXID_PROJECTCOMBO = "#projectCombo"; // if .projectCombo that would be css selector
    // final static String FILE_TESTOPENTRAVELREPO = "TestOpenTravelRepo.otp";
    // final static String FILE_TESTLOCAL = "TestLocalFiles.otp";

    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( TestOtmModelManager.class );
        // setupWorkInProcessArea( TestOtmModelManager.class );
        // // Prevent java.nio.BufferOverflowException
        // System.setProperty( "headless.geometry", "2600x2200-32" );
    }

    /**
     * Build a model manager with full action manager, built in libraries and repository manager.
     * 
     * @param actionManager action manager to assign as full action manager. can be null.
     * @return
     */
    public static OtmModelManager build() {
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, repoManager, null );
        // Done in constructor - mgr.addLibraries_BuiltIn( new TLModel() );

        // Assure - manually check
        // TLModel tlModel = mgr.getTlModel();
        // tlModel.setListenersEnabled( true );
        // tlModel.addListener( new ModelIntegrityChecker() );

        return mgr;
    }

    /**
     * Build a model manager with built in libraries and repository manager.
     * 
     * @param actionManager action manager to assign as full action manager. can be null.
     * @return
     */
    public static OtmModelManager buildModelManager(DexActionManager actionManager) {
        OtmModelManager mgr = new OtmModelManager( actionManager, repoManager, null );
        mgr.addLibraries_BuiltIn( new TLModel() );
        return mgr;
    }

    /**
     * Assign type to ALL type users in the model. Note, this will likely make them invalid due to UPA and name
     * collisions.
     * 
     * @param assignedType
     * @param mgr
     * @return users that were successfully set are returned
     */
    public static List<OtmTypeUser> assignTypeToEveryUser(OtmTypeProvider assignedType, OtmModelManager mgr) {
        // Assign everything to assignedType
        List<OtmTypeUser> users = new ArrayList<>();
        for (OtmLibraryMember lm : mgr.getMembers())
            for (OtmTypeUser user : lm.getDescendantsTypeUsers()) {
                OtmTypeProvider u = user.setAssignedType( assignedType );
                if (u == assignedType)
                    users.add( user );
            }
        return users;
    }

    @Test
    public void testClose() throws Exception {
        // Given - project added to the model manager
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        TestDexFileHandler.loadAndAddManagedProject( mgr );
        // mapTests( mgr );

        // Given assertions
        assertTrue( "Given: ", !mgr.getBaseNamespaces().isEmpty() );
        int baseNSCount = mgr.getBaseNamespaces().size();
        assertNotNull( mgr.getIdType() );
        assertNotNull( mgr.getEmptyType() );


        // When - cleared
        mgr.clear();

        // Then
        assertTrue( mgr.getBaseNamespaces().isEmpty() );
        assertTrue( mgr.getProjects().isEmpty() );
        assertTrue( mgr.getProjects().isEmpty() );
        for (OtmLibraryMember mbr : mgr.getMembers())
            assertTrue( mbr.getLibrary().isBuiltIn() );
        // assertTrue( mgr.getMembers().isEmpty() );
        for (OtmLibrary lib : mgr.getLibraries())
            assertTrue( lib.isBuiltIn() );
        // assertTrue( mgr.getLibraries().isEmpty() );
        //
        assertNotNull( mgr.getIdType() );
        assertNotNull( mgr.getEmptyType() );

        // When - loaded again
        TestDexFileHandler.loadAndAddManagedProject( mgr );
        // mgr.addProjects();
        assertTrue( mgr.getBaseNamespaces().size() == baseNSCount );
    }

    @Test
    public void testContains() {
        // Given a project that uses the OpenTravel repository
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );

        // When the project is loaded and added to the model manager
        TestDexFileHandler.loadAndAddManagedProject( mgr );

        // Then - model manager contains the libraries and members
        checkContains( mgr.getProjectManager(), mgr );

        // When second project is added to the model manager
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );

        // Then - model manager contains both libraries and all their members
        checkContains( mgr.getProjectManager(), mgr );
    }

    private void checkContains(ProjectManager pm, OtmModelManager mgr) {
        for (ProjectItem pi : pm.getAllProjectItems()) {
            assertTrue( "Must contain the tlLibrary in project item.", mgr.contains( pi.getContent() ) );
            for (LibraryMember lm : pi.getContent().getNamedMembers()) {
                // log.debug( "Testing " + lm.getLocalName() );

                OtmLibraryMember otmL = (OtmLibraryMember) OtmModelElement.get( (TLModelElement) lm );
                if (otmL == null)
                    log.debug( "ERROR - missing otm from listener on a " + lm.getClass().getSimpleName() );
                OtmLibraryMember otm = (OtmLibraryMember) OtmModelElement.get( (TLModelElement) lm );
                assertTrue( "Must have Otm object from listener.", otm != null );
                assertTrue( "Must contain Otm object from named member.", mgr.contains( otm ) );

                if (!mgr.contains( lm ))
                    log.warn( "Error detected" );
                assertTrue( "Must contain each named member.", mgr.contains( lm ) );
            }
        }
    }



    /**
     * Test hasEditableLibraries() hasEditableLibraries(library) hasProjects()
     */
    @Test
    public void testHas() {
        // Given an empty model manager
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        assertTrue( mgr.getOtmProjectManager().hasProjects() == false );
        assertTrue( mgr.hasEditableLibraries() == false );

        // Given a project that uses local library files
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
        TestDexFileHandler.loadAndAddManagedProject( mgr );
        TLModel tlModel = mgr.getTlModel();
        assertNotNull( tlModel );

        assertTrue( mgr.getOtmProjectManager().hasProjects() == true );
        assertTrue( mgr.hasEditableLibraries() == true );
        for (OtmLibrary lib : mgr.getLibraries()) {
            mgr.hasEditableLibraries( lib ); // NPE check
            mgr.isLatest( lib ); // NPE check
        }
    }

    @Test
    public void testChangeToManaged()
        throws DexProjectException, RepositoryException, PublishWithLocalDependenciesException {
        // Givens - set up a library most of the way through publishing
        OtmModelManager mgr = getModelManager();
        OtmProject proj = TestOtmProjectManager.buildProject( getModelManager() );
        OtmProjectManager otmProjectManager = mgr.getOtmProjectManager();
        ProjectManager tlProjectManager = mgr.getProjectManager();
        RepositoryManager repository = getRepository();
        assertTrue( "Given: ", repository != null );

        OtmLocalLibrary localLib = buildTempLibrary( null, null, "TMMCTM" );
        proj.add( localLib );

        // Do part of the publish process.
        // Get and check the project item
        ProjectItem item = otmProjectManager.getProjectItem( localLib.getTL() ); // Throws exception
        tlProjectManager.publish( item, repository );
        OtmMajorLibrary newLib = new OtmMajorLibrary( localLib );

        assertTrue( "Given: ", mgr.getLibraries().contains( localLib ) );
        assertTrue( "Given: ", !mgr.getLibraries().contains( newLib ) );
        assertTrue( "Given: ", mgr.getVersionChain( localLib ) instanceof OtmVersionChainEmpty );
        assertTrue( "Given: ", mgr.getVersionChain( newLib ) == null );

        // When
        mgr.changeToManaged( localLib, newLib );

        // Then
        assertTrue( "Then: ", !mgr.getLibraries().contains( localLib ) );
        assertTrue( "Then: ", mgr.getLibraries().contains( newLib ) );
        assertTrue( "Then: ", mgr.getVersionChain( localLib ) == null );
        assertTrue( "Then: ", mgr.getVersionChain( newLib ) instanceof OtmVersionChainVersioned );

    }

    /**
     * Don't fix problems here, fix in {@link TestOtmVersionChain#testIsLatest()}
     */
    @Test
    public void testIsLatest() {
        OtmModelManager mgr = getModelManager();

        OtmLibrary localLib = TestLibrary.buildOtm( mgr );
        OtmLibrary majorLib = buildMajor( "TMM1" );
        OtmLibrary minor1 = buildMinor( majorLib );
        OtmLibrary minor2 = buildMinor( minor1 );
        OtmLibrary majorLib2 = buildMajor( "TMM2" );

        assertTrue( "Then: ", mgr.isLatest( localLib ) == true );
        assertTrue( "Then: ", mgr.isLatest( majorLib ) == false );
        assertTrue( "Then: ", mgr.isLatest( minor1 ) == false );
        assertTrue( "Then: ", mgr.isLatest( minor2 ) == true );
        assertTrue( "Then: ", mgr.isLatest( majorLib2 ) == true );
    }

    @Test
    public void testUserSettings() {
        // Given
        UserSettings settings = getUserSettings( application );
        assertNotNull( settings );
        OtmModelManager mgr = new OtmModelManager( null, repoManager, settings );
        OtmProjectManager pMgr = mgr.getOtmProjectManager();
        assertNotNull( pMgr );

        // there may be pre-existing projects
        if (!settings.getRecentProjects().isEmpty())
            log.debug(
                "Settings are loaded from your .ota2 directory. Save the file and remove recent projects to run this test." );
        else {
            assertTrue( "Given: ", settings.getRecentProjects().isEmpty() );

            // Test recent projects in user settings
            //
            // When 1 project added
            TestDexFileHandler.loadAndAddManagedProject( mgr );
            // Then
            assertTrue( settings.getRecentProjects().size() == 1 );
            // When second project added
            TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
            // Then
            assertTrue( settings.getRecentProjects().size() == 2 );
            // When - projects are closed
            mgr.clear();
            // Then - still 2 projects remembered
            assertTrue( settings.getRecentProjects().size() == 2 );
            // Then - project manager also reports projects
            assertTrue( pMgr.getRecentProjects().size() == 2 );
        }

    }

    /**
     * Get the settings from the test's application.
     * 
     * @param application
     * @return settings or null
     */
    public static UserSettings getUserSettings(AbstractOTMApplication application) {
        if (application == null)
            return null;
        UserSettings settings = null;
        AbstractMainWindowController controller = application.getController();
        if (controller instanceof ObjectEditorController) {
            settings = ((ObjectEditorController) controller).getUserSettings();
            assertNotNull( settings );
        }

        return settings;
    }

}

