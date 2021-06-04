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
import org.junit.Ignore;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.common.DexFileException;
import org.opentravel.common.DexFileHandler;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmProjectManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.utilities.testutil.TestFxMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Static utilities for loading test files.
 * <p>
 * Access via TestDexFileHandler
 */
@Ignore
public class TestDexFileHandlerUtils extends AbstractDexTest {
    private static Log log = LogFactory.getLog( TestDexFileHandlerUtils.class );

    public final static String FXID_PROJECTCOMBO = "#projectCombo"; // if .projectCombo that would be css selector

    // 27 named objects in 2 libraries. All valid, one warning.
    public final static String FILE_TESTOPENTRAVELREPO = "TestOpenTravelRepo.otp";
    public final static String FILE_TESTVERSIONS_REPO = "TestVersionsFromOpenTravelRepo.otp";
    public final static String FILE_TESTVERSIONSWITHRESOURCE_REPO = "TestVersionsFromRepoWithResource.otp";
    public final static String FILE_TESTLOCAL = "TestLocalFiles.otp";

    public final static String FILE_TESTLOCALLIBRARY = "StandAloneLibrary.otm";
    public final static String FILE_TESTLOCALLIBRARYBASE = "base_library.otm";
    public final static String FILE_TESTLOCALLIBRARY1 = "facets1_library.otm";
    public final static String FILE_TESTLOCALLIBRARY2 = "facets2_library.otm";
    public final static String FILE_TESTLIBRARYNOTINPROJECT = "LibraryNotInProject.otm";


    // /**
    // *
    // * @param subDirectory added to wipFolder
    // * @return
    // * @throws IOException
    // */
    // public static File getTempDir(String subDirectory) throws IOException {
    // assertTrue( "Given: must have subDirectory.", subDirectory != null );
    // File outputFolder = new File( wipFolder.get(), subDirectory );
    // outputFolder.mkdir();
    // outputFolder.deleteOnExit();
    // assertTrue( "Must be writable folder.", outputFolder.canWrite() );
    // return outputFolder;
    // }

    /**
     * Note, non-empty directories will not be deleted.
     * 
     * @param fileName
     * @param subDirectory added to wipFolder
     * @return
     * @throws IOException
     */
    public static File getTempFile(String fileName, String subDirectory) throws IOException {
        File outputFolder = new File( wipFolder.get(), subDirectory );
        outputFolder.mkdir();
        outputFolder.deleteOnExit();
        File file = new File( outputFolder, fileName );
        if (!file.createNewFile()) {
            log.error( "Error creating temporary file." );
        }
        file.deleteOnExit();

        assertTrue( "Must be able to make  file writeable.", file.setWritable( true ) );
        log.debug( "Created Temporary File: " + file.getCanonicalPath() );
        return file;
    }

    /**
     * 
     * load project that uses the OpenTravel repository and add to model. 27 named objects in 2 libraries. All valid,
     * one warning.
     * <p>
     * Note: calling class must extend AbstractFxTest
     */
    public static void loadAndAddManagedProject(OtmModelManager modelManager) {
        File repoProject = new File( wipFolder.get(), "/" + FILE_TESTOPENTRAVELREPO );
        assertNotNull( repoProject );

        // try {
        openProject( repoProject, modelManager );
        // DexFileHandler.openProject( repoProject, modelManager, null );
        // } catch (DexFileException e) {
        // assertTrue( "Must not throw error: " + e.getLocalizedMessage(), true );
        // }

        assertTrue( "Must have project items.", modelManager.getProjectManager().getAllProjectItems().size() > 1 );

        // TODO - where do these tests belong?
        OtmProjectManager pMgr = modelManager.getOtmProjectManager();
        assertTrue( modelManager.getProjects().size() > 0 );
        assertTrue( pMgr.getProjects().size() > 0 );
        if (modelManager.getUserSettings() != null)
            assertTrue( pMgr.getRecentlyUsedProjectFileNames().size() > 0 );

        // log.debug( "Model now has " + modelManager.getTlModel().getAllLibraries().size() + " libraries." );
        // log.debug( "Model now has " + modelManager.getTlModel().getUserDefinedLibraries().size() + " user libraries."
        // );
    }

    /**
     * Isolate production code from all the test calls.
     * 
     * @param repoProject
     * @param modelManager
     */
    public static void openProject(File repoProject, OtmModelManager modelManager) {
        // openProject( repoProject, modelManager );
        List<OtmProject> projects = modelManager.getProjects();

        try {
            DexFileHandler.openProject( repoProject, modelManager, null );
            assertTrue( "Then", projects.size() < modelManager.getProjects().size() );
        } catch (DexFileException e) {
            assertTrue( "Open Project Exception: " + e.getLocalizedMessage(), false );
        }
    }

    /**
     * 
     * load TestLocalFiles.otp project that uses local library files and add to model
     */
    public static void loadAndAddUnmanagedProject(OtmModelManager modelManager) {
        File localProject = new File( wipFolder.get(), "/" + FILE_TESTLOCAL );
        assertNotNull( localProject );
        openProject( localProject, modelManager );
        // try {
        // DexFileHandler.openProject( localProject, modelManager, null );
        // } catch (DexFileException e) {
        // assertTrue( "Given: must not throw exception: " + e.getLocalizedMessage(), true );
        // }
        log.debug( "Model now has " + modelManager.getTlModel().getAllLibraries().size() + " libraries." );
    }


    /**
     * Uses file handler to open the named file in the wipFolder. Does not change model manager.
     * 
     * @see {@link DexFileHandler#openLibrary(File, TLModel)}
     * 
     * @param path
     * @param OtmModel used to get the tlModel
     */
    public static void loadLocalLibrary(String path, OtmModelManager modelManager) {
        File localLibrary = new File( wipFolder.get(), "/" + path );
        assertTrue( localLibrary != null );
        assertTrue( localLibrary.exists() );
        // Path should be:
        // C:\Users\dmh\Git\OTM-DE-Utilities\otm-dex\target\test-workspace\TestDexFileHandler\wip\StandAloneLibrary.otm

        try {
            DexFileHandler.openLibraryFile( localLibrary, modelManager.getTlModel() );
        } catch (DexFileException e) {
            assertTrue( "Must not catch exception: " + e, true );
        }
        // log.debug( "Model now has " + tlModel.getAllLibraries().size() + " libraries." );
    }

    /**
     * Uses file handler to open the named file in the wipFolder. Does not change model manager.
     * 
     * @see {@link DexFileHandler#openLibrary(File, TLModel)}
     * 
     * @param path
     * @param OtmModel used to get the tlModel
     */
    public static void loadAndAddLocalLibrary(String path, OtmModelManager modelManager) {
        File localLibrary = new File( wipFolder.get(), "/" + path );
        assertTrue( localLibrary != null );
        assertTrue( localLibrary.exists() );
        // Path should be:
        // C:\Users\dmh\Git\OTM-DE-Utilities\otm-dex\target\test-workspace\TestDexFileHandler\wip\StandAloneLibrary.otm

        try {
            DexFileHandler.openLibrary( localLibrary, modelManager );
        } catch (DexFileException e) {
            assertTrue( "Must not catch exception: " + e, true );
        }
        // log.debug( "Model now has " + tlModel.getAllLibraries().size() + " libraries." );
    }

    /**
     * Load the project that uses local library files but do NOT add to the model.
     * <p>
     * To load into the OtmModelManager, use {@link OtmModelManager#addProjectsOLD()} or
     * {@link #loadAndAddUnmanagedProject(OtmModelManager)}
     * 
     * @param modelManager
     */
    public static void loadLocalLibrary(OtmModelManager modelManager) {
        File localProject = new File( wipFolder.get(), "/" + FILE_TESTLOCAL );
        assertNotNull( localProject );
        openProject( localProject, modelManager );
        // try {
        // DexFileHandler.openProject( localProject, modelManager, null );
        // } catch (DexFileException e) {
        // assertTrue( "Must not throw error: " + e.getLocalizedMessage(), true );
        // }
        log.debug( "Model now has " + modelManager.getTlModel().getAllLibraries().size() + " libraries." );
    }


    public static boolean loadVersionProject(File repoProject, OtmModelManager modelManager)
        throws InterruptedException {
        // File repoProject = new File( wipFolder.get(), "/" + FILE_TESTVERSIONS_REPO );
        assertNotNull( repoProject );
        assertNotNull( modelManager );
        // Check wipFolder
        log.debug( "WipFolder = " + wipFolder.get() );
        assertTrue( "Loader: must be able to read wipFolder.", repoProject.canRead() );

        // List<OtmProject> initialProjects = modelManager.getProjects();
        openProject( repoProject, modelManager );
        // try {
        // DexFileHandler.openProject( repoProject, modelManager, null );
        // } catch (DexFileException e) {
        // assertTrue( "Must not throw error: " + e.getLocalizedMessage(), true );
        // }
        assertTrue( "Must have project items.", modelManager.getProjectManager().getAllProjectItems().size() > 1 );
        // log.debug( "Model now has " + modelManager.getTlModel().getAllLibraries().size() + " libraries." );

        List<OtmLibrary> libs = new ArrayList<>( modelManager.getLibraries() );
        assertTrue( "Must have libraries.", libs.size() > 1 );
        boolean editable = false;
        for (OtmLibrary lib : libs)
            if (lib.isEditable())
                editable = true;
        if (!editable)
            log.warn( "No editable libraries. Check access to repository for libraries in " + FILE_TESTVERSIONS_REPO );

        // List<OtmProject> newProjects = new ArrayList<>();
        // modelManager.getProjects().forEach( p -> {
        // if (!initialProjects.contains( p ))
        // newProjects.add( p );
        // } );
        // log.debug( newProjects.size() + " projects loaded." );

        // Wait for the background tasks to complete
        while (modelManager.getBackgroundTaskCount() > 0)
            Thread.sleep( 100 );

        return editable;
    }

    /**
     * Load project that uses the OpenTravel repository. Note: it uses credentials so may not contain editable
     * libraries.
     * <p>
     * FILE_TESTVERSIONS_REPO = "TestVersionsFromOpenTravelRepo.otp";
     * <p>
     * 1 Library with version 0.0, 0.1, 1.0 and 1.1
     * <p>
     * 11 objects, one of each type
     * 
     * @return true if there is an editable library
     * @throws InterruptedException
     */
    public static boolean loadVersionProject(OtmModelManager modelManager) throws InterruptedException {
        File repoProject = new File( wipFolder.get(), "/" + FILE_TESTVERSIONS_REPO );
        return loadVersionProject( repoProject, modelManager );
    }

    public static boolean loadVersionProjectWithResource(OtmModelManager modelManager) throws InterruptedException {
        File repoProject = new File( wipFolder.get(), "/" + FILE_TESTVERSIONSWITHRESOURCE_REPO );
        return loadVersionProject( repoProject, modelManager );
    }


    /** ************************************************************************ */

    final int WATCH_TIME = 0; // How long to sleep so we can see what is happening. Can be 0.
    public static final boolean RUN_HEADLESS = true;
    /**
     * Configure headless/normal mode for TestFX execution.
     */
    static {
        TestFxMode.setHeadless( RUN_HEADLESS );
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
}

