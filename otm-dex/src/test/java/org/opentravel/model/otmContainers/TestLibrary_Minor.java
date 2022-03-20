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
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.dex.action.manager.DexMinorVersionActionManager;
import org.opentravel.dex.action.manager.DexReadOnlyActionManager;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

/**
 *
 */
public class TestLibrary_Minor extends AbstractDexTest {
    private static Logger log = LogManager.getLogger( TestLibrary_Minor.class );

    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( TestLibrary_Minor.class );
    }

    @Test
    public void testConstructor() {
        OtmMajorLibrary major = buildMajor( "T1" );
        OtmMinorLibrary minor = buildMinor( major );
        check( minor );
    }

    @Test
    public void testGetActionManager() {
        OtmMajorLibrary major = buildMajor( "T1" );
        OtmMinorLibrary lib = buildMinor( major );
        assertTrue( "Then: must have read-only action manager.",
            lib.getActionManager() instanceof DexReadOnlyActionManager );
        rtuLock( lib );
        assertTrue( "Then: must have minor action manager.",
            lib.getActionManager() instanceof DexMinorVersionActionManager );
    }

    @Test
    public void testGetActionManager_NewMembers() {
        OtmMajorLibrary major = buildMajor( "T1" );
        OtmMinorLibrary lib = buildMinor( major );
        rtuLock( lib );
        TestLibrary.addOneOfEach( lib );

        // These are full because they are new to the chain
        for (OtmLibraryMember m : lib.getMembers())
            assertTrue( "Then: must have full action manager.",
                lib.getActionManager( m ) instanceof DexFullActionManager );
    }

    @Test
    public void testGetActionManager_ExistingMembers() {
        OtmMajorLibrary major = buildMajor( "T1" );
        rtuLock( major );
        TestLibrary.addOneOfEachValid( major );
        OtmMinorLibrary lib = buildMinor( major );

        // These are minor because they are new to the chain
        for (OtmLibraryMember m : lib.getMembers())
            assertTrue( "Then: must have full action manager.",
                lib.getActionManager( m ) instanceof DexMinorVersionActionManager );
    }

}
