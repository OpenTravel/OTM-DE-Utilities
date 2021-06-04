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

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.AbstractDexTest;
import org.opentravel.TestDexFileHandler;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.model.otmContainers.TestProject;
import org.opentravel.objecteditor.UserSettings;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.impl.BuiltInProject;

import java.io.File;
import java.util.List;

/**
 * Verifies the functions of the <code>Otm Model Manager</code>.
 */
// @Ignore
public class TestOtmProjectManager extends AbstractDexTest {
    private static Log log = LogFactory.getLog( TestOtmProjectManager.class );


    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( TestOtmProjectManager.class );
    }

    public static OtmProjectManager build(OtmModelManager mgr) {
        // Give manager user settings?
        OtmProjectManager opm = new OtmProjectManager( mgr, mgr.getProjectManager() );
        assertTrue( "Builder given: ", mgr != null );
        assertTrue( "Builder given: ", mgr.getProjectManager() != null );
        checkProjectManagers( mgr );
        return opm;
    }

    /**
     * Check the managers contain the same content
     * 
     * @param modelMgr
     */
    public static void checkProjectManagers(OtmModelManager modelMgr) {
        ProjectManager tlProjMgr = modelMgr.getProjectManager();
        assertTrue( "Check: must have TL project manager.", tlProjMgr != null );
        List<Project> tlProjList = tlProjMgr.getAllProjects();
        List<OtmProject> otmProjList = modelMgr.getProjects();

        for (OtmProject p : modelMgr.getProjects())
            assertTrue( "Check: TL project manager must contain project.",
                tlProjMgr.getAllProjects().contains( p.getTL() ) );
        for (Project tlp : tlProjMgr.getAllProjects())
            if (!(tlp instanceof BuiltInProject))
                assertTrue( "Check: Otm project manager must contain project.",
                    modelMgr.getOtmProjectManager().get( tlp ) != null );
    }

    @Test
    public void testConstructor() {
        OtmProjectManager opm = build( getModelManager() );
        assertTrue( opm != null );
    }

    public static final String PTMP = "TempProject";
    // public static final String PFNAME = "tProj.otp";
    public static final String PNAME = "testProj";
    public static final String CID = "testContext";
    public static final String PID = "http://opentravel.org/temp/projectNS1/";
    public static final String DES = "some description";

    public static OtmProject buildProject(OtmModelManager modelMgr) {
        return buildProject( modelMgr, PNAME );
    }

    public static OtmProject buildProject(OtmModelManager modelMgr, String name) {
        assertTrue( "Builder: ", modelMgr != null );
        OtmProjectManager projMgr = modelMgr.getOtmProjectManager();
        assertTrue( "Builder: ", projMgr != null );

        // List<Project> tlProjListBefore = projMgr.getTLProjectManager().getAllProjects();
        // List<OtmProject> otmProjListBefore = modelMgr.getProjects();
        TestOtmProjectManager.checkProjectManagers( modelMgr );

        // Get unique names
        int i = 1;
        // String name = PNAME + i; // Managing project uses "starts with" based logic
        while (projMgr.get( name ) != null)
            name = PNAME + i++;
        String PFName = name + ".otp";
        String Pid = PID + name + "/testing/v1";

        OtmProject otmProject = null;
        String path = "";

        // Get temporary project file
        File projectFile = null;
        try {
            projectFile = TestDexFileHandler.getTempFile( PFName, PTMP );
            path = projectFile.getCanonicalPath();
        } catch (Exception e) {
            log.debug( "Error creating temp project file." );
            assertTrue( "Exception: error creating temp project file: " + e.getLocalizedMessage(), false );
        }

        // Create project
        try {
            otmProject = projMgr.newProject( projectFile, name, CID, Pid, DES );
        } catch (Exception e) {
            assertTrue( "Exception: error creating project: " + e.getLocalizedMessage(), false );
        }

        List<Project> tlProjListAfter = projMgr.getTLProjectManager().getAllProjects();
        List<OtmProject> otmProjectsAfter = modelMgr.getProjects();

        assertTrue( "Builder: Must have built a project.", otmProject != null );
        assertTrue( "Builder: Must have TL Project.", otmProject.getTL() != null );
        assertTrue( "Builder: ", modelMgr.getProjects() != null );
        assertTrue( "Builder: Model manager must contain new project.", otmProjectsAfter.contains( otmProject ) );
        assertTrue( "Builder: TL Project manager must contain new project.",
            tlProjListAfter.contains( otmProject.getTL() ) );
        TestOtmProjectManager.checkProjectManagers( modelMgr );

        log.debug( "Built new project in file: " + path );
        return otmProject;
    }

    @Test
    public void testNewProject() {
        OtmProject otmProject = buildProject( getModelManager() );
        // OtmModelManager modelMgr = getModelManager();
        // OtmProjectManager projMgr = modelMgr.getOtmProjectManager();
        //
        // // Get temporary project file
        // File projectFile = null;
        // String path = "";
        // try {
        // projectFile = TestDexFileHandler.getTempFile( PFNAME, PTMP );
        // path = projectFile.getCanonicalPath();
        // } catch (Exception e) {
        // log.warn( "Error creating temp project file." );
        // assertTrue( "Exception: error creating temp project file: " + e.getLocalizedMessage(), false );
        // }
        //
        // // Create project
        // try {
        // otmProject = projMgr.newProject( projectFile, PNAME, CID, PID, DES );
        // } catch (Exception e) {
        // assertTrue( "Exception: error creating project: " + e.getLocalizedMessage(), false );
        // }

        TestProject.check( otmProject );

        // TODO - test with 2nd project
        // TODO - test with 2nd project with same name
    }

    // Private - only used by and tested using add(TLProject)
    // @Test
    // public void testAdd_OtmProject() {
    // // Create a project that has content
    // }

    @Test
    public void testAdd_TLProject() {
        OtmProjectManager opm = build( getModelManager() );
        UserSettings settings = getModelManager().getUserSettings();
        assertTrue( "Given: ", settings != null );

        // Given - a project to add
        Project tlProject = TestProject.buildTL( getModelManager() );
        File pFile = tlProject.getProjectFile();

        // When
        OtmProject otp = opm.add( tlProject );

        // Then
        assertTrue( "Then: manager contains project.", opm.getProjects().contains( otp ) );
        assertTrue( "Then: ", otp.getTL() == tlProject );
        assertTrue( "Then: ", settings.getRecentProjects().contains( pFile ) );

        // When - added again
        OtmProject newOpm = opm.add( tlProject );
        // Then
        assertTrue( "Then: null must be returned when added again.", newOpm == null );
    }

    @Test
    public void testGetManagingProject() {
        // FIXME
        // assertTrue( "TODO", false );
    }

    /**
     * @see TestProject#testNewProject()
     * @throws IOException
     * @throws LibrarySaveException
     */
    // @Test
    // public void testNewProject() throws IOException, LibrarySaveException {
    // final String Project_Name = "tnp";
    // OtmProjectManager opm = build( getModelManager() );
    // // Given - a temporary project file
    // File projectFile = TestDexFileHandler.getTempFile( "TNPFile", "TestProjectManager" );
    //
    // // When
    // OtmProject np =
    // opm.newProject( projectFile, Project_Name, "tnpContextId", "tnpProjectId", "Test New Project Description" );
    //
    // // Then
    // assertTrue( np != null );
    // assertTrue( "Then: Project manager must contain new project.", opm.getProjects().contains( np ) );
    // assertTrue( "Then: Project manager must find project by name.", opm.get( Project_Name ) == np );
    // }

    // @Test
    // public void testClear() {
    // //
    // }
    //
    // @Test
    // public void testClose() {
    // //
    // }
    //
    // @Test
    // public void testContains() {
    // //
    // }
    //
    // @Test
    // public void testGet_Project() {
    // //
    // }
    //
    // @Test
    // public void testGet_String() {
    // //
    // }
    //
    // @Test
    // public void testGetNamespace() {
    // //
    // }
    //
    // @Test
    // public void testGetOpenFileMap() {
    // //
    // }
    //
    // @Test
    // public void testGetProject_String() {
    // //
    // }
    //
    // @Test
    // public void testGetProjectDirectory() {
    // //
    // }
    //
    // @Test
    // public void testGetProjects() {
    // //
    // }
    //
    // @Test
    // public void testGetProjects_File() {
    // //
    // }
    //
    // @Test
    // public void testGetProjects_AbstractLibrary() {
    // //
    // }
    //
    // @Test
    // public void testGetRecentlyUsedProjectFileNames() {
    // //
    // }
    //
    // @Test
    // public void testGetRecentlyUsedProjectFiles() {
    // //
    // }
    //
    // @Test
    // public void testGetTLProjectManager() {
    // //
    // }
    //
    // @Test
    // public void testGetUserProjects() {
    // //
    // }
    //
    // @Test
    // public void testHasProjects() {
    // //
    // }

    // private, little testing needed
    // @Test
    // public void testHasSettings() {
    // OtmProjectManager opm = build( getModelManager() );
    // assertTrue( "Then: has user settings.", opm.hasSettings() );
    // }

}

