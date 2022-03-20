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

package org.opentravel.dex.tasks.repository;

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.AbstractDexTest;
import org.opentravel.common.DexProjectException;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.tasks.DexTaskException;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmProjectManager;
import org.opentravel.model.TestOtmProjectManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmLocalLibrary;
import org.opentravel.model.otmContainers.OtmMajorLibrary;
import org.opentravel.model.otmContainers.OtmManagedLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.model.otmContainers.OtmVersionChainVersioned;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.RepositoryManager;

/**
 *
 */
public class TestManageLibraryTask extends AbstractDexTest {
    private static Logger log = LogManager.getLogger( TestManageLibraryTask.class );

    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( null );
        // beforeClassSetup( TestManageLibraryTask.class );
    }

    /**
     * Check to assure library is managed properly.
     * <ul>
     * <li>BaseNS
     * <li>Has RepoItem which is UNLOCKED and DRAFT
     * <li>Model Manager contains library and library's NS
     * <li>Version Chain found by manager
     * 
     * @param lib
     * @param mgr
     */
    public static void check(OtmManagedLibrary lib, OtmModelManager mgr) {
        assertTrue( "Check: library is null.", lib != null );
        assertTrue( lib.getTL().getLibraryUrl().toString().contains( "junit" ) );
        String baseNS = lib.getBaseNS();
        assertTrue( "Check: must have a base namespace.", baseNS != null && !baseNS.isEmpty() );

        // Repo Item state
        RepositoryItemState state = lib.getState();
        assertTrue( lib.getState() == RepositoryItemState.MANAGED_UNLOCKED );
        assertTrue( lib.getStatus() == TLLibraryStatus.DRAFT );

        // Model Manager checks
        assertTrue( "Check: model manager must contain library.", mgr.getUserLibraries().contains( lib ) );
        assertTrue( "Check: baseNs", mgr.getBaseNamespaces().contains( baseNS ) );
        assertTrue( "Check: manager must report chain is in chains list.",
            mgr.getChains().contains( lib.getVersionChain() ) );

        // Version chain checks
        assertTrue( "Check: must have version chain.", lib.getVersionChain() instanceof OtmVersionChainVersioned );
        assertTrue( "Check: chains manager must find library.",
            mgr.getVersionChain( lib ) instanceof OtmVersionChainVersioned );
        // TODO -
        assertTrue( "Check: published library must be reported in version chain.",
            !mgr.getChainLibraries( lib ).isEmpty() );
    }

    /**
     * Use OTM project to publish the library. Simulates using ManageLibraryTask.
     * 
     * @param repoId
     * @param lib
     * @param controller
     */
    public static OtmMajorLibrary publish(RepositoryManager repository, OtmLibrary lib, DexMainController controller) {
        // public static OtmManagedLibrary publish(String repoId, OtmLibrary lib, DexMainController controller) {
        // assertTrue( "Parameter: ", repoId != null && !repoId.isEmpty() );
        TestLibrary.checkLibrary( lib );
        assertTrue( "Parameter: ", controller != null );

        if (!ManageLibraryTask.isEnabled( lib ))
            assertTrue( "Given: manage library task must be enabled for library; " + ManageLibraryTask.getReason( lib ),
                ManageLibraryTask.isEnabled( lib ) );

        // Cast and PM access checked in isEnabled()
        OtmMajorLibrary newLib = null;
        OtmProjectManager otmPM = lib.getModelManager().getOtmProjectManager();
        try {
            newLib = otmPM.publish( (OtmLocalLibrary) lib, repository );
        } catch (DexProjectException e) {
            assertTrue( "Project Exception: " + e.getLocalizedMessage(), false );
        }

        check( newLib, lib.getModelManager() );
        TestLibrary.checkLibrary( newLib );
        return newLib;
    }

    /** **************************** TESTS ********************************** **/

    // ManageLibraryTask task = new ManageLibraryTask(String repoId, OtmLibrary taskData, TaskResultHandlerI handler,
    // DexMainController mainController) {
    @Test
    public void testConstructor() {
        OtmLibrary lib = buildTempLibrary( null, null, "Constructor1" );
        ManageLibraryTask task = new ManageLibraryTask( getRepoId(), lib, null, getMainController() );
        assertTrue( "Then: ", task != null );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_null() throws Exception {
        ManageLibraryTask task = new ManageLibraryTask( getRepoId(), null, null, getMainController() );
        assertTrue( "Must not reach here.", false );
    }

    // @Test
    // public void testIsEnabled() {
    // // task.doIT();
    // }
    // @Test
    // public void testGetReason() {
    // // task.doIT();
    // }
    // @Test
    // public void testGetSelectedRepository() {
    // // task.doIT();
    // }
    // @Test
    // public void testCheckUrl() {
    // // task.doIT();
    // }

    @Test
    public void testDoIt() throws Exception {
        // Given a library to manage
        OtmLocalLibrary lib = buildTempLibrary( null, null, "DoIt1" );
        assertTrue( "Given:", lib instanceof OtmLocalLibrary );
        assertTrue( "Then: manager must report chain is in chains list.",
            getModelManager().getChains().contains( lib.getVersionChain() ) );

        // Given a project
        // OtmProject otmProj = TestProject.build( getModelManager() );
        OtmProject otmProj = TestOtmProjectManager.buildProject( getModelManager() );
        otmProj.add( lib );

        // Given a task
        ManageLibraryTask task = new ManageLibraryTask( getRepoId(), lib, null, getMainController() );
        if (!ManageLibraryTask.isEnabled( lib ))
            log.debug( "Given error: " + ManageLibraryTask.getReason( lib ) );
        assertTrue( "Given: task is enabled for library. ", ManageLibraryTask.isEnabled( lib ) );

        // When run
        task.doIT();

        // Original library is unchanged, New library is managed
        OtmManagedLibrary mLib = null;
        for (OtmLibrary uLib : getModelManager().getUserLibraries())
            if (uLib instanceof OtmMajorLibrary)
                mLib = (OtmManagedLibrary) uLib;

        // Then - new library is in the model
        check( mLib, getModelManager() );
        // Has chain reported in mgr.getChains() and is in that chain

        // Then - old library is not in model
        // Its old chain is no longer reported in mgr.getChains()
        assertTrue( "Then: manager must NOT report chain is in chains list.",
            !getModelManager().getChains().contains( lib.getVersionChain() ) );

    }


    @Test(expected = DexTaskException.class)
    public void testDoIt_notInProject() throws Exception {
        // Given a library to manage
        OtmLibrary lib = buildTempLibrary( null, null, "DoIt2" );
        assertTrue( "Given: unmanaged library.", lib.isUnmanaged() );
        // Given - NO project

        // Given a task
        ManageLibraryTask task = new ManageLibraryTask( getRepoId(), lib, null, getMainController() );

        // When run
        task.doIT();
        // Then
        assertTrue( "Must not reach here.", false );

    }
    // @Test(expected = DexTaskException.class)
    // public void testDoIt_badUrl() throws Exception {
    // When run
    // task.doIT();
    // Then
    // assertTrue( "Must not reach here.", false );
    // }
    //
    // @Test(expected = DexTaskException.class)
    // public void testDoIt_noPI() throws Exception {
    // // task.doIT();
    // }


}
