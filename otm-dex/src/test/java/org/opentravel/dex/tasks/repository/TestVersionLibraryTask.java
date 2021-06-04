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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.AbstractDexTest;
import org.opentravel.dex.tasks.repository.VersionLibraryTask.VersionType;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.TestOtmModelChainsManager;
import org.opentravel.model.TestOtmProjectManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmMajorLibrary;
import org.opentravel.model.otmContainers.OtmManagedLibrary;
import org.opentravel.model.otmContainers.OtmMinorLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;

/**
 *
 */
public class TestVersionLibraryTask extends AbstractDexTest {
    private static Log log = LogFactory.getLog( TestVersionLibraryTask.class );

    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( null );
        // beforeClassSetup( TestVersionLibraryTask.class );
    }

    /**
     * Check to assure library is???
     * 
     * @param lib
     * @param mgr
     */
    public static void check(OtmManagedLibrary lib, OtmModelManager mgr) {
        // assertTrue( "Check: library is null.", lib != null );
        assertTrue( lib.getTL().getLibraryUrl().toString().contains( "junit" ) );
        RepositoryItemState state = lib.getState();
        assertTrue( lib.getState() == RepositoryItemState.MANAGED_UNLOCKED );
        // assertTrue( lib.getStatus() == TLLibraryStatus.DRAFT );

        TestLibrary.checkLibrary( lib );
    }


    /** **************************** TESTS ********************************** **/

    @Test
    public void testConstructor() {
        OtmManagedLibrary mLib = buildMajor( "TestVersionTask1" );
        VersionLibraryTask task = new VersionLibraryTask( VersionType.MINOR, mLib, null,
            getMainController().getStatusController(), null, getModelManager() );
        assertTrue( "Then: ", task != null );
    }
    // TODO - add to version task or delete this test
    // @Test(expected = IllegalArgumentException.class)
    // public void testConstructor_null() throws Exception {
    // VersionLibraryTask task = new VersionLibraryTask( VersionType.MINOR, null, null,
    // getMainController().getStatusController(), null, getModelManager() );
    // assertTrue( "Must not reach here.", false );
    // }

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
    public void testDoIt_MultipleProjects() {
        // FIXME
    }

    @Test
    public void testDoIt_Major() throws Exception {
        // Given a library to version
        OtmProject managingProject = TestOtmProjectManager.buildProject( getModelManager() );
        OtmManagedLibrary mLib = buildMajor( "TestVersionTask2", managingProject );

        rtuVersion( VersionType.MAJOR, mLib ); // Runs check
    }

    @Test
    public void testDoIt() throws Exception {
        // Given a library to version
        OtmProject managingProject = TestOtmProjectManager.buildProject( getModelManager() );
        OtmManagedLibrary lib = buildMajor( "TestVersionTask2", managingProject );
        rtuPromoteUntil( lib, TLLibraryStatus.FINAL );
        // OtmProject managingProject = getModelManager().getManagingProject( lib );

        // Given a task
        VersionLibraryTask task = new VersionLibraryTask( VersionType.MINOR, lib, null,
            getMainController().getStatusController(), null, getModelManager() );

        if (!VersionLibraryTask.isEnabled( lib ))
            log.debug( "Given error: " + VersionLibraryTask.getReason( lib ) );
        assertTrue( "Given: task is enabled for library. ", VersionLibraryTask.isEnabled( lib ) );

        // Model Manager checks
        assertTrue( getModelManager().contains( lib.getTL() ) );

        // When run
        task.doIT();

        // Get the Original and New libraries.
        OtmManagedLibrary vLib = null;
        OtmManagedLibrary mLib = null;
        for (OtmLibrary uLib : getModelManager().getUserLibraries())
            if (uLib instanceof OtmMajorLibrary)
                mLib = (OtmManagedLibrary) uLib;
            else if (uLib instanceof OtmMinorLibrary)
                vLib = (OtmManagedLibrary) uLib;

        check( mLib, vLib );
    }


    public static void check(OtmManagedLibrary mLib, OtmManagedLibrary vLib) {
        assertTrue( "Check: must have library.", mLib != null );
        assertTrue( "Check: must have library.", vLib != null );

        OtmModelManager mgr = mLib.getModelManager();
        assertTrue( "Check: Library must have model manager.", mgr != null );
        assertTrue( "Check: Library must same model manager.", vLib.getModelManager() == mgr );

        // Then - model manager
        assertTrue( "Check: Model manager still has major library.", mgr.getLibraries().contains( mLib ) );
        check( mLib, mgr );
        assertTrue( "Check: Model manager still has major library.", mgr.getLibraries().contains( vLib ) );
        check( vLib, mgr );

        // Then - project item
        assertTrue( "Check: must have only 1 project item.", mLib.getProjectItems().size() == 1 );
        ProjectItem mPI = mLib.getProjectItems().get( 0 );
        assertTrue( "Check: must be a Repository item.", mPI instanceof RepositoryItem );
        assertTrue( "Check: must have only 1 project item.", vLib.getProjectItems().size() == 1 );
        ProjectItem vPI = vLib.getProjectItems().get( 0 );
        assertTrue( "Check: must be a Repository item.", vPI instanceof RepositoryItem );
        // Then - project
        // mLib.getProjectItems().get( 0 ) == vPI;
        // assertTrue( "Check: new version must be managed by same project.", managingProject.contains( vLib.getTL() )
        // );
        // assertTrue( "Check: TL Project must contain versioned project item.",
        // managingProject.getTL().getProjectItems().contains( vPI ) );

        // Then - project managers
        ProjectManager pm = mgr.getProjectManager();
        assertTrue( "Check: TL Project Manager must find project for PI.", pm.getAssignedProjects( vPI ) != null );
        assertTrue( "Check: OtmProject Manager must find project for TLLibrary.",
            !mgr.getOtmProjectManager().getProjects( vLib.getTL() ).isEmpty() );

        // Then - chains manager
        TestOtmModelChainsManager.check( mLib, null );
        TestOtmModelChainsManager.check( vLib, null );
    }


    // @Test(expected = DexTaskException.class)
    // public void testDoIt_notInProject() throws Exception {
    // // Given a library to manage
    // OtmLibrary lib = buildTempLibrary( null, null, "DoIt2" );
    // assertTrue( "Given: unmanaged library.", lib.isUnmanaged() );
    // // Given - NO project
    //
    // // Given a task
    // ManageLibraryTask task = new ManageLibraryTask( getRepoId(), lib, null, getMainController() );
    //
    // // When run
    // task.doIT();
    // // Then
    // assertTrue( "Must not reach here.", false );
    //
    // }

}
