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

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.common.DexProjectException;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.MenuBarWithProjectController;
import org.opentravel.dex.tasks.repository.ManageLibraryTask;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLocalLibrary;
import org.opentravel.model.otmContainers.OtmMajorLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.model.otmContainers.TestProject;

/**
 * Test the base class for Dex tests requiring a repository allowing testing of major and minor versions.
 * <p>
 * SubTypes must run {@link TestAbstractDexTest#beforeClassSetup(Class)} in their @BeforeClass to set up the work area
 * and set headless geometry to prevent buffer overflow.
 * <p>
 * This @Before each method
 * <ul>
 * <li>Creates new repository in local temporary directory.
 * <li>Mimics the {@link MenuBarWithProjectController#doCloseHandler(javafx.event.ActionEvent)} behavior by:
 * <ul>
 * <li>Clearing ModelManager, ProjectManager, TLModel.
 * <li>Clearing action queue.
 * </ul>
 * </ul>
 * <p>
 * Repository setup follows example in: org.opentravel.schemacompiler.repository.TestRepositoryManager
 */
public class TestAbstractDexTest extends AbstractDexTest {
    private static Logger log = LogManager.getLogger( TestAbstractDexTest.class );


    // @Before
    // public void setup() throws Exception {
    // log.debug( "Before cleared model and created new repository." );
    // }

    @BeforeClass
    public static void beforeClassSetup() throws Exception {
        beforeClassSetup( TestAbstractDexTest.class );
        // log.debug( "BeforeClass setup work area." );
    }

    /** *********************** Repository Utilities ********************************** */

    @Test
    public void testGetMainController() {
        DexMainController mc = getMainController();
        assertTrue( mc != null );
    }

    @Test
    public void testGetModelManager() {
        OtmModelManager mgr = getModelManager();
        assertTrue( mgr != null );
    }

    @Test
    public void testPublishLibrary() throws DexProjectException {
        // OtmProject proj = TestProject.build( getModelManager() );
        // OtmLibrary lib = TestLibrary.buildOtm( getModelManager() );

        // Given a library to manage
        OtmLocalLibrary lib = buildTempLibrary( null, null, "tpl1" );
        assertTrue( lib != null );
        // Given a project
        OtmProject otmProj = TestProject.build( getModelManager() );
        otmProj.add( lib );

        if (!ManageLibraryTask.isEnabled( lib ))
            log.debug( "Task not enabled: " + ManageLibraryTask.getReason( lib ) );
        assertTrue( "Given: manage library task must be enabled for library.", ManageLibraryTask.isEnabled( lib ) );

        // When
        OtmMajorLibrary mLib = publishLibrary( lib );
        // Then
        assertTrue( "Then: Must have major library.", mLib != null );
    }

    @Test
    public void testBuildMajor() {
        OtmProject otmProj = TestProject.build( getModelManager() );
        OtmMajorLibrary mLib = buildMajor( "AbsDexTest_Major1", otmProj );
        check( mLib );
        TestProject.check( mLib, otmProj );
    }
}
