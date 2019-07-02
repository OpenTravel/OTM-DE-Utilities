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
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.common.DexFileHandler;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import java.io.File;
import java.util.Collection;

/**
 * Verifies the functions of the <code>Otm Model Manager</code>.
 */
public class TestOtmModelManager extends AbstractFxTest {
    private static Log log = LogFactory.getLog( TestOtmModelManager.class );

    public static final boolean RUN_HEADLESS = true;
    final int WATCH_TIME = 0; // How long to sleep so we can see what is happening. Can be 0.

    final static String FXID_PROJECTCOMBO = "#projectCombo"; // if .projectCombo that would be css selector
    final static String FILE_TESTOPENTRAVELREPO = "TestOpenTravelRepo.otp";
    final static String FILE_TESTLOCAL = "TestLocalFiles.otp";

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestOtmModelManager.class );
    }

    @Test
    public void testAddingManagedProject() throws Exception {
        DexFileHandler fileHandler = new DexFileHandler();
        OtmModelManager mgr = new OtmModelManager( null );

        // Given a project that uses the OpenTravel repository
        File repoProject = new File( wipFolder.get(), "/" + FILE_TESTOPENTRAVELREPO );
        assertNotNull( repoProject );
        ProjectManager pm = fileHandler.openProject( repoProject, mgr.getTlModel(), null );
        assertTrue( "Must have project items.", !pm.getAllProjectItems().isEmpty() );

        // When the project is added to the model manager
        mgr.add( pm );

        // Then - Expect 4 libraries and 63 members
        assertTrue( mgr.getLibraries().size() > 2 );
        assertTrue( !mgr.getMembers().isEmpty() );
        log.debug( "Read " + mgr.getMembers().size() + " members." );
        for (OtmLibraryMember m : mgr.getMembers()) {
            assertTrue( m.getTL().getOwningModel() == mgr.getTlModel() );
        }
        // Then - assure each base namespace has an non-empty chain
        assertNotNull( mgr.getBaseNamespaces() );
        assertTrue( !mgr.getBaseNamespaces().isEmpty() ); // There should be base namespaces
        mgr.getBaseNamespaces().forEach( b -> assertTrue( !mgr.getLibraryChain( b ).isEmpty() ) );

        mapTests( mgr );
    }

    private void mapTests(OtmModelManager mgr) {

        // Then - assure each library maps to the same TL as the otmLibrary's tlObject
        for (OtmLibrary otmLibrary : mgr.getLibraries()) {
            assertNotNull( mgr.get( otmLibrary ) );
            assertTrue( otmLibrary == mgr.get( mgr.get( otmLibrary ) ) );
            assertTrue( otmLibrary == mgr.get( otmLibrary.getTL() ) );
        }

        // Then - there are projects and each library is managed by a project
        Collection<OtmProject> projects = mgr.getProjects();
        assertTrue( !projects.isEmpty() );
        mgr.getLibraries().forEach( l -> assertTrue( projects.contains( mgr.getManagingProject( l ) ) ) );

    }

    @Test
    public void testAddingUnmangedProject() throws Exception {

        OtmModelManager mgr = new OtmModelManager( null );
        DexFileHandler fileHandler = new DexFileHandler();

        // Given a project that uses local library files
        File localProject = new File( wipFolder.get(), "/" + FILE_TESTLOCAL );
        assertNotNull( localProject );
        ProjectManager pm = fileHandler.openProject( localProject, mgr.getTlModel(), null );
        assertTrue( "Must have project items.", !pm.getAllProjectItems().isEmpty() );

        // When the project is added to the model manager
        mgr.add( pm );
        //
        // // Then - Expect 6 libraries and 70 members
        assertTrue( mgr.getLibraries().size() > 2 );
        assertTrue( !mgr.getMembers().isEmpty() );
        log.debug( "Read " + mgr.getMembers().size() + " members." );

        // Then - assure each base namespace has an non-empty set. Library view lists libraries by baseNS.
        assertNotNull( mgr.getBaseNamespaces() );
        assertTrue( !mgr.getBaseNamespaces().isEmpty() ); // There should be base namespaces
        mgr.getBaseNamespaces().forEach( b -> assertTrue( !mgr.getLibraryChain( b ).isEmpty() ) );

        mapTests( mgr );
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

    /**
     * Configure headless/normal mode for TestFX execution.
     */
    static {
        TestFxMode.setHeadless( RUN_HEADLESS );
    }
}

