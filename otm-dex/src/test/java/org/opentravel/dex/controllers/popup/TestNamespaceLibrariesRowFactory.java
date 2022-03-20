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

package org.opentravel.dex.controllers.popup;

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.AbstractDexTest;
import org.opentravel.common.DexProjectException;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.TestOtmProjectManager;
import org.opentravel.model.otmContainers.OtmMajorLibrary;
import org.opentravel.model.otmContainers.OtmManagedLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.model.otmContainers.TestProject;
import org.opentravel.schemacompiler.repository.RepositoryItem;

/**
 * Verifies the functions of the action methods in <code>NamespaceLibrariesRowFactory</code>.
 */
public class TestNamespaceLibrariesRowFactory extends AbstractDexTest {
    private static Logger log = LogManager.getLogger( TestNamespaceLibrariesRowFactory.class );

    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( TestNamespaceLibrariesRowFactory.class );
    }

    /**
     * Test adding a repository item to an open project. This private method is replicated here.
     */
    @Test
    public void testAddToProject() {
        // NamespaceLibrariesRowFactory rowFactory = new NamespaceLibrariesRowFactory( null );
        // rowFactory.addToProject( null );
        //
        // Givens
        OtmModelManager mm = getModelManager();
        OtmProject proj = TestOtmProjectManager.buildProject( mm, "OriginalProject" );
        OtmMajorLibrary mLib = buildMajor( "TestNSRF", proj );
        TestProject.check( mLib, proj );
        RepositoryItem repoItem = mLib.getProjectItem();

        // Given - the model does NOT contain the library
        getModelManager().clear();
        OtmProject oProject = TestOtmProjectManager.buildProject( mm, "AddedProject" );

        assertTrue( "Given: Project to be added must NOT contain lib.", !oProject.contains( mLib.getTL() ) );

        // When - Active code
        OtmManagedLibrary newLib = null;
        if (oProject != null)
            try {
                newLib = oProject.addManaged( repoItem );
                // ProjectItem pi = mm.getProjectManager().addManagedProjectItem( repoItem, oProject.getTL() );
                // mm.addProjectsOLD();
                // mm.addOLD( pi.getContent() );
                // controller.fireEvent( new DexModelChangeEvent( mm ) );
                // } catch (LibraryLoaderException | RepositoryException | DexProjectException e1) {
            } catch (DexProjectException e1) {
                log.error( "Error opening repo item. " + e1.getLocalizedMessage() );
            }
        // Then
        assertTrue( "Given: The Project added to must contain lib.", oProject.contains( newLib.getTL() ) );
        TestProject.check( newLib, oProject );

    }
}

