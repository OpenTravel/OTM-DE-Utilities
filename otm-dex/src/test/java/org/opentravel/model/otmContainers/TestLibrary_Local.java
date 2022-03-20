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
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryItemState;

/**
 *
 */
public class TestLibrary_Local extends AbstractDexTest {
    private static Logger log = LogManager.getLogger( TestLibrary_Local.class );

    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( TestLibrary_Local.class );
    }

    @Test
    public void testConstructor() {
        OtmLocalLibrary lib = TestLibrary.buildOtm();
        check( lib );
    }

    @Test
    public void testGetActionManager() {
        OtmLocalLibrary lLib = TestLibrary.buildOtm();
        assertTrue( "Then: must have full action manager.", lLib.getActionManager() instanceof DexFullActionManager );
    }

    @Test
    public void testGetActionManager_Member() {
        OtmLocalLibrary lib = TestLibrary.buildOtm();
        TestLibrary.addOneOfEach( lib );

        for (OtmLibraryMember m : lib.getMembers())
            assertTrue( "Then: must have full action manager.",
                lib.getActionManager( m ) instanceof DexFullActionManager );
    }

    @Test
    public void testGetState() {
        OtmLocalLibrary lib = TestLibrary.buildOtm();
        assertTrue( "Then: ", lib.getState() == RepositoryItemState.UNMANAGED );
    }

    @Test
    public void testStatus() {
        OtmLocalLibrary lib = TestLibrary.buildOtm();
        assertTrue( "Then: ", lib.getStatus() == TLLibraryStatus.DRAFT );
    }

    @Test
    public void testIsEditable() {
        OtmLocalLibrary lib = TestLibrary.buildOtm();
        assertTrue( "Then: ", lib.isEditable() );
    }

    // Deprecated
    // @Test
    // public void testIsUnmanaged() {
    // OtmLocalLibrary lib = TestLibrary.buildOtm();
    // assertTrue( "Then: ", lib.isUnmanaged() );
    // }
}
