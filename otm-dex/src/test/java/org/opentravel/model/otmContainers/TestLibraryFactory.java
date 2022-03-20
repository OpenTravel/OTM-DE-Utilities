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

package org.opentravel.model.otmContainers;

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.AbstractDexTest;
import org.opentravel.common.DexLibraryException;
import org.opentravel.model.TestOtmProjectManager;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.ProjectItem;

/**
 *
 */
public class TestLibraryFactory extends AbstractDexTest {
    private static Logger log = LogManager.getLogger( TestLibraryFactory.class );

    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( TestLibraryFactory.class );
    }

    // TODO - build statics to isolate the newLibrary calls in testing

    @Test
    public void testNewLibrary_BuiltIns() throws DexLibraryException {
        TLModel tlModel = getModelManager().getTlModel();
        OtmLibrary lib;
        for (AbstractLibrary absLib : tlModel.getBuiltInLibraries()) {
            lib = OtmLibraryFactory.newLibrary( absLib, getModelManager() );
            assertTrue( "Then - must have built in library.", lib instanceof OtmBuiltInLibrary );
        }
    }

    @Test
    public void testNewLibrary_Local() throws DexLibraryException {
        TLLibrary tlLib = new TLLibrary();

        OtmLibrary lib = OtmLibraryFactory.newLibrary( tlLib, getModelManager() );
        assertTrue( "Then - must have local library.", lib instanceof OtmLocalLibrary );

    }

    @Test
    public void testNewLibrary_Major() throws DexLibraryException {
        OtmProject otmProj = TestOtmProjectManager.buildProject( getModelManager() );
        OtmLibrary major = buildMajor( "TestMajorLibraryFactory", otmProj );
        ProjectItem pi = major.getProjectItem();
        OtmLibrary lib = OtmLibraryFactory.newLibrary( pi, getModelManager() );
        assertTrue( "Then - must have major library.", lib instanceof OtmMajorLibrary );
        ProjectItem npi = lib.getProjectItem();
        assertTrue( "Then - must have Project Item.", lib.getProjectItem() == pi );
    }

    @Test
    public void testNewLibrary_Minor() throws DexLibraryException {
        OtmLibrary major = buildMajor( "TestMinorLibraryFactory" );
        OtmLibrary minor = buildMinor( major );
        ProjectItem pi = minor.getProjectItem();

        OtmLibrary lib = OtmLibraryFactory.newLibrary( pi, getModelManager() );
        assertTrue( "Then - must have minor library.", lib instanceof OtmMinorLibrary );
        assertTrue( "Then - must have Project Item.", lib.getProjectItem() == pi );
    }
}
