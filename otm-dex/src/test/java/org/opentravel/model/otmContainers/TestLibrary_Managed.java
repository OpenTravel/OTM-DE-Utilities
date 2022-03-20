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
import org.opentravel.model.TestOtmProjectManager;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryItemState;

/**
 *
 */
public class TestLibrary_Managed extends AbstractDexTest {
    private static Logger log = LogManager.getLogger( TestLibrary_Managed.class );

    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( TestLibrary_Managed.class );
    }

    @Test
    public void testConstructor() {
        OtmManagedLibrary lib = buildMajor( "T1" );
        check( lib );
    }

    @Test
    public void testCanBeLocked() {
        OtmProject proj = TestOtmProjectManager.buildProject( getModelManager() );
        assertTrue( "Given : Project must have write permission.", proj.getPermission() == RepositoryPermission.WRITE );

        OtmManagedLibrary lib = buildMajor( "T1", proj );
        assertTrue( "Then: is not locked so it can be locked.", lib.canBeLocked() );
        rtuLock( lib );
        assertTrue( "Then: is locked, so it can NOT be locked ", !lib.canBeLocked() );

        OtmManagedLibrary minor = buildMinor( lib );
        assertTrue( "Given: minor is unlocked.", minor.getState() == RepositoryItemState.MANAGED_UNLOCKED );
        assertTrue( "Then: is not locked so it can be locked.", minor.canBeLocked() );
        rtuLock( minor );
        assertTrue( "Then: is locked, so it can NOT be locked ", !minor.canBeLocked() );

        // TODO - how to test when permission != WRITE?
    }

    @Test
    public void testCanBeUnlocked() {
        OtmManagedLibrary lib = buildMajor( "T1" );
        assertTrue( "Then: ", !lib.canBeUnlocked() );
        rtuLock( lib );
        assertTrue( "Then: ", lib.canBeUnlocked() );

        OtmManagedLibrary minor = buildMinor( lib );
        assertTrue( "Then: is not locked so it can NOT be unlocked.", !minor.canBeUnlocked() );
        rtuLock( minor );
        assertTrue( "Then: is locked, so it can be unlocked ", minor.canBeUnlocked() );

    }

    @Test
    public void testGetLockedBy() {
        OtmManagedLibrary lib = buildMajor( "T1" );
        String user = lib.getLockedBy();
        assertTrue( "Then: ", lib.getLockedBy().isEmpty() );
    }

    // WIP, LOCKED, UNLOCKED
    @Test
    public void testGetState() {
        OtmManagedLibrary lib = buildMajor( "T1" );
        log.debug( lib.getState() );
        assertTrue( "Then: ", lib.getState() == RepositoryItemState.MANAGED_UNLOCKED );

        // When locked
        rtuLock( lib );
        log.debug( lib.getState() );
        assertTrue( "Then: ", lib.getState() == RepositoryItemState.MANAGED_WIP );

        // When unlocked
        rtuUnLock( lib );
        log.debug( lib.getState() );
        assertTrue( "Then: ", lib.getState() == RepositoryItemState.MANAGED_UNLOCKED );

        // When minor version created
        lib = buildMinor( lib );
        log.debug( lib.getState() );
        assertTrue( "Then: ", lib.getState() == RepositoryItemState.MANAGED_UNLOCKED );

        // When locked
        rtuLock( lib );
        log.debug( lib.getState() );
        assertTrue( "Then: ", lib.getState() == RepositoryItemState.MANAGED_WIP );

        // When unlocked
        rtuUnLock( lib );
        log.debug( lib.getState() );
        assertTrue( "Then: ", lib.getState() == RepositoryItemState.MANAGED_UNLOCKED );
    }

    @Test
    public void testGetStatus() {
        OtmManagedLibrary lib = buildMajor( "T1" );
        assertTrue( "Then: ", lib.getStatus() == TLLibraryStatus.DRAFT );
    }

    // @Test
    // public void testGetVersionChainName() {
    // OtmManagedLibrary lib = buildMajor( "T1" );
    // assertTrue( "Then: ", lib.);
    // }
    //
    // @Test
    // public void testIsChainEditable() {
    // OtmManagedLibrary lib = buildMajor( "T1" );
    // assertTrue( "Then: ", lib.);
    // }

    @Test
    public void testIsEditable() {
        OtmManagedLibrary major = buildMajor( "T1" );
        assertTrue( "Then: must not be editable ", !major.isEditable() );
        rtuLock( major );
        assertTrue( "Then: must be editable.", major.isEditable() );

        // When - a minor is created from the major
        OtmManagedLibrary minor = buildMinor( major );

        assertTrue( "Then: major must not be editable ", !major.isEditable() );
        assertTrue( "Then: minor must not be editable ", !minor.isEditable() );

        // When unlocked
        rtuLock( minor );
        assertTrue( "Then: minor must be editable ", minor.isEditable() );

        // When promoted
        rtuPromoteUntil( minor, TLLibraryStatus.FINAL );
        assertTrue( "Then: minor must have unlocked state.", minor.getState() == RepositoryItemState.MANAGED_UNLOCKED );
        assertTrue( "Then: minor must have final status", minor.getStatus() == TLLibraryStatus.FINAL );
        assertTrue( "Then: minor must not be editable ", !minor.isEditable() );
    }

    // @Test
    // public void testIsLatestVersion() {
    // OtmManagedLibrary lib = buildMajor( "T1" );
    // assertTrue( "Then: ", lib.isEditable() );
    // }


}
