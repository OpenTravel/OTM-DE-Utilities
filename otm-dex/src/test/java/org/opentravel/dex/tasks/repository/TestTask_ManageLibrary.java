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
import org.opentravel.TestDexFileHandler;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import java.util.List;

/**
 *
 */
public class TestTask_ManageLibrary extends AbstractFxTest {
    private static Log log = LogFactory.getLog( TestTask_ManageLibrary.class );

    public static final boolean RUN_HEADLESS = true;
    final int WATCH_TIME = 0; // How long to sleep so we can see what is happening. Can be 0.

    final static String FXID_PROJECTCOMBO = "#projectCombo"; // if .projectCombo that would be css selector
    final static String FILE_TESTOPENTRAVELREPO = "TestOpenTravelRepo.otp";
    final static String FILE_TESTLOCAL = "TestLocalFiles.otp";

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestTask_ManageLibrary.class );
        assertTrue( "Setup: ", repositoryManager != null );

        // Caused by: java.lang.ClassNotFoundException: org.opentravel.schemacompiler.repository.RepositoryServlet
        // startTestServer( "versions-repository", 9482, repositoryConfig, true, false, TestTask_ManageLibrary.class );
        // repoManager = repositoryManager.get();

        // These are null!
        // repoManager = repositoryManager.get();
        // assertTrue( "Setup: ", repoManager != null );
        // RepositoryManager rm = repositoryManager.get();
        // Repository tm = testRepository.get();
        // assertTrue( "Setup: ", tm != null );
        // log.debug( "Test Repo ID = " + tm.getId() );

        log.debug( "Before class setup tests ran." );
    }

    // @AfterClass
    // public static void tearDownTests() throws Exception {
    // shutdownTestServer();
    // }

    // @Before
    // public void beforeTest() {
    // modelManager.clear();
    // }

    @Test
    public void testDoIt() throws Exception {
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmModelManager mgr = lib.getModelManager();

        TestDexFileHandler.loadVersionProject( mgr );
        List<OtmLibrary> libs = mgr.getUserLibraries();

        // TODO
        // These tests could be run against OpenTravel Repo by using the projectItem in the loaded libraries
        // DexTask task = new ManageLibraryTask( repoManager.getLocalRepositoryId(), lib, null, null );

        // task.doIT();
    }


    /** ****************************************************** **/


    /**
     * **********************************************************************
     * 
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
