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
public class TestLibrary_Major extends AbstractDexTest {
    private static Logger log = LogManager.getLogger( TestLibrary_Major.class );

    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( TestLibrary_Major.class );
    }

    @Test
    public void testConstructor() {
        OtmMajorLibrary lib = buildMajor( "T1" );
        check( lib );
    }

    @Test
    public void testGetActionManager() {
        OtmMajorLibrary lib = buildMajor( "T1" );
        assertTrue( "Then: must have read-only action manager.",
            lib.getActionManager() instanceof DexReadOnlyActionManager );
        rtuLock( lib );
        assertTrue( "Then: must have full action manager.", lib.getActionManager() instanceof DexFullActionManager );
    }

    @Test
    public void testGetActionManager_Member() {
        OtmMajorLibrary lib = buildMajor( "T1" );
        rtuLock( lib );
        TestLibrary.addOneOfEachValid( lib );

        for (OtmLibraryMember m : lib.getMembers())
            assertTrue( "Then: Locked and new so must have full action manager.",
                lib.getActionManager( m ) instanceof DexFullActionManager );

        rtuUnLock( lib );
        for (OtmLibraryMember m : lib.getMembers())
            assertTrue( "Then: Unlocked and new so must not have full action manager.",
                lib.getActionManager( m ) instanceof DexReadOnlyActionManager );

        // When an unlocked minor is created from major, then major must return read only action manager
        OtmManagedLibrary minor = buildMinor( lib );
        assertTrue( "Given: ", !lib.isEditable() );
        for (OtmLibraryMember m : lib.getMembers())
            assertTrue( "Then: Minor created so must not have full action manager.",
                lib.getActionManager( m ) instanceof DexReadOnlyActionManager );

        // When minor is locked to be editable, objects in major must return Minor Action manager.
        rtuLock( minor );
        for (OtmLibraryMember m : lib.getMembers())
            assertTrue( "Then: Minor created so must not have full action manager.",
                lib.getActionManager( m ) instanceof DexMinorVersionActionManager );

        // When minor version of member is created, then the major object then return read only action manager
        for (OtmLibraryMember m : lib.getMembers()) {
            OtmLibraryMember newMember = m.createMinorVersion( minor );
            assertTrue( "Given: new member must have been created.", newMember != null );
            assertTrue( "Then: Minor created so must not have full action manager.",
                lib.getActionManager( m ) instanceof DexReadOnlyActionManager );
            assertTrue( "Then: Minor must have minor action manager.",
                lib.getActionManager( newMember ) instanceof DexMinorVersionActionManager );
        }
    }

}
