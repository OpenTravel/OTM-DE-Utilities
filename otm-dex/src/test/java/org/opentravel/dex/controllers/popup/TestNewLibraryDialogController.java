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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.AbstractDexTest;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLocalLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.model.otmContainers.TestProject;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryException;

import java.io.IOException;

/**
 * Verifies the functions of the <code>New Library Dialog Controller</code>.
 */
public class TestNewLibraryDialogController extends AbstractDexTest {
    private static Log log = LogFactory.getLog( TestNewLibraryDialogController.class );

    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( TestNewLibraryDialogController.class );
    }

    @Test
    public void testDoOK() throws RepositoryException, IOException {
        String ns = "http://example.com/test";
        String name = "TestLib1";
        String subDir = "NewLibraryDialogTest";

        // Create an otm file and library
        OtmLocalLibrary otmLibrary = buildTempLibrary( ns, subDir, name );
        // Create project then add library
        OtmProject selectedProject = TestProject.build( getModelManager() );
        ProjectItem pi = null;
        try {
            pi = selectedProject.add( otmLibrary );
        } catch (Exception e) {
            assertTrue( "OtmProject.add exception: " + e.getLocalizedMessage(), false );
        }
        OtmModelManager modelMgr = getModelManager();
        TLLibrary tlLib = (TLLibrary) otmLibrary.getTL();

        // Then
        assertTrue( "Must have created project item.", pi != null );
        assertTrue( "Then: ", modelMgr.get( tlLib ) == otmLibrary );
        assertTrue( "Then: ", modelMgr.get( otmLibrary ) == tlLib );
        assertTrue( "Then: ", selectedProject.getProjectItem( tlLib ) == pi );
        assertTrue( "Then: ", selectedProject.getProjectItem( otmLibrary ) == pi );
        assertTrue( "Then: ", otmLibrary.getProjectItems().contains( pi ) );

        ProjectItem pi2 = modelMgr.getProjectManager().getProjectItem( tlLib );
        assertTrue( pi2 != null );
        assertTrue( "Then: ", pi2 == pi );
    }

}

