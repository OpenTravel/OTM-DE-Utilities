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
import org.opentravel.common.DexFileException;
import org.opentravel.common.DexFileHandler;
import org.opentravel.model.OtmModelManager;

import java.io.File;

/**
 * Verifies the functions of the <code>Dex File Handler</code>.
 * <p>
 * Extends TestDexFileHandlerUtils to provider file loading utilities.
 */
public class TestDexFileHandler extends TestDexFileHandlerUtils {
    private static Log log = LogFactory.getLog( TestDexFileHandler.class );


    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestDexFileHandler.class );
        // startTestServer( "versions-repository", 9480, repositoryConfig, true, false, TestDexFileHandler.class );
        // repoManager = repositoryManager.get();

        // Prevent java.nio.BufferOverflowException
        System.setProperty( "headless.geometry", "2600x2200-32" );

    }

    @Test
    public void testCheckDir() {
        // TODO
    }

    @Test
    public void testSaveLibraries() {
        // TODO
    }

    @Test
    public void testCreateFile() {
        // TODO
    }

    @Test
    public void testCreateLibrary() {
        // TODO - error cases
    }

    @Test
    public void testCanWrite() {
        // TODO
    }

    @Test
    public void testOpenProject() {
        // OtmModelManager modelManager = TestOtmModelManager.build();
        OtmModelManager modelManager = getModelManager();

        File localProject = new File( wipFolder.get(), "/" + FILE_TESTLOCAL );
        assertNotNull( localProject );
        // openProject( localProject, modelManager );
        try {
            DexFileHandler.openProject( localProject, modelManager, null );
        } catch (DexFileException e) {
            assertTrue( "Then must not throw exception." + e.getLocalizedMessage(), true );
        }
    }

    @Test(expected = DexFileException.class)
    public void testOpenProject_BadFile() throws Exception {
        // OtmModelManager modelManager = TestOtmModelManager.build();
        OtmModelManager modelManager = getModelManager();
        File localProject = new File( wipFolder.get(), "/" + FILE_TESTLOCAL + "ZZXXCCVFFDDSS" );
        assertNotNull( localProject );
        DexFileHandler.openProject( localProject, modelManager, null );
    }

    @Test(expected = DexFileException.class)
    public void testOpenProject_NoMgr() throws Exception {
        File localProject = new File( wipFolder.get(), "/" + FILE_TESTLOCAL );
        assertNotNull( localProject );
        DexFileHandler.openProject( localProject, null, null );
    }

    @Test
    public void testCreateLibraryFile() {

    }

    @Test
    public void testFileHandlerUtils() throws Exception {
        // OtmModelManager modelManager = TestOtmModelManager.build();
        OtmModelManager modelManager = getModelManager();
        // getTempFile();
        loadAndAddManagedProject( modelManager );
        loadAndAddUnmanagedProject( modelManager );
        modelManager.clear();

        loadLocalLibrary( modelManager );
        // see testOpenLocalLibrary() - loadLocalLibrary(path, modelManager);
        loadVersionProject( modelManager );
        loadVersionProjectWithResource( modelManager );
        // Used by loadVersion* - loadVersionProject(projectFile, modelManager);
    }

    @Test(expected = DexFileException.class)
    public void testOpenLibrary_NoMgr() throws Exception {
        File localLibrary = new File( wipFolder.get(), "/" + FILE_TESTLOCALLIBRARY1 );
        DexFileHandler.openLibrary( localLibrary, (OtmModelManager) null );
    }

    @Test(expected = DexFileException.class)
    public void testOpenLibrary_BadFile() throws Exception {
        // OtmModelManager mgr = TestOtmModelManager.build();
        OtmModelManager mgr = getModelManager();
        File localLibrary =
            new File( wipFolder.get(), "/" + FILE_TESTLOCALLIBRARY1 + "NOT_A_REAL_FILE_NAME_ZZZZZAAAAAQQQQQ" );
        DexFileHandler.openLibrary( localLibrary, mgr );
        // TODO - error cases
    }

    @Test
    public void testOpenLocalLibrary() {
        // OtmModelManager mgr = TestOtmModelManager.build();
        OtmModelManager mgr = getModelManager();
        assertTrue( mgr.getTlModel().getUserDefinedLibraries().size() == 0 );
        // Uses DexFileHandler#openLibrary(file, mgr.getTL());
        loadLocalLibrary( FILE_TESTLOCALLIBRARY, mgr );
        loadLocalLibrary( FILE_TESTLOCALLIBRARY1, mgr );
        loadLocalLibrary( FILE_TESTLOCALLIBRARY2, mgr );
        assertTrue( mgr.getTlModel().getUserDefinedLibraries().size() >= 3 );
    }

    @Test
    public void testOpenLocalLibrary3() throws Exception {
        // OtmModelManager mgr = TestOtmModelManager.build();
        OtmModelManager mgr = getModelManager();
        assertTrue( mgr.getUserLibraries().size() == 0 );
        // Uses DexFileHandler#openLibrary(file, mgr);
        loadAndAddLocalLibrary( FILE_TESTLOCALLIBRARY, mgr );
        loadAndAddLocalLibrary( FILE_TESTLOCALLIBRARY1, mgr );
        loadAndAddLocalLibrary( FILE_TESTLOCALLIBRARY2, mgr );
        assertTrue( mgr.getUserLibraries().size() >= 3 );
    }

    @Test
    public void testOpenLocalProject() throws Exception {
        // When the local files are used in the project
        // OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        OtmModelManager mgr = getModelManager();
        loadAndAddUnmanagedProject( mgr );
        // mgr.addProjects();

        // Then - Expect libraries and members
        assertTrue( mgr.getLibraries().size() > 2 );
        assertTrue( !mgr.getMembers().isEmpty() );
        log.debug( "Read " + mgr.getMembers().size() + " members." );
    }

    @Test
    public void testOpenOTAProject() throws Exception {
        // When the OpenTravel repository is used in the project
        // OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        OtmModelManager mgr = getModelManager();
        // When the project is loaded and added to the model manager
        loadAndAddManagedProject( mgr );

        // Then - Expect 4 libraries and 63 members
        assertTrue( mgr.getLibraries().size() > 2 );
        assertTrue( !mgr.getMembers().isEmpty() );
        log.debug( "Read " + mgr.getMembers().size() + " members." );
    }

    @Test
    public void testOpenProjectError() throws Exception {
        // TODO
    }
}

