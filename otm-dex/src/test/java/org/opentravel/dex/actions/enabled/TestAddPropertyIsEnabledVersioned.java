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

package org.opentravel.dex.actions.enabled;

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.AbstractDexTest;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.otmContainers.OtmMajorLibrary;
import org.opentravel.model.otmContainers.OtmMinorLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmEnumeration;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

/**
 * AddPropertyAction
 */
public class TestAddPropertyIsEnabledVersioned extends AbstractDexTest {
    private static Logger log = LogManager.getLogger( TestAddPropertyIsEnabledVersioned.class );

    // static OtmLibrary lib = null;
    // static OtmBusinessObject globalBO = null;
    // static OtmResource resource;
    // static DexActionManager actionMgr;
    static TestAddPropertyIsEnabled tester;

    @BeforeClass
    public static void beforeClass() throws Exception {
        beforeClassSetup( null );
        tester = new TestAddPropertyIsEnabled();
    }


    /**
     * Test AddProperty when:
     * <li>The minor library is editable
     * <li>The member in minor library must be new to the chain.
     */
    @Test
    public void testIsEnabled_NewInMinor() {
        OtmMajorLibrary majorLib = buildMajor( "TestIsEnabledMajor" );
        OtmMinorLibrary minorLib = buildMinor( majorLib );
        rtuLock( minorLib );

        tester.setLibrary( minorLib );
        tester.testMembers();
        tester.testProperties();
    }

    /**
     * For AddProperty to be enabled:
     * <li>the minor library must be editable
     * <li>Most property owners will enable adding properties (not enumerations)
     */
    @Test
    public void testIsEnabled_Minor() {
        OtmMajorLibrary majorLib = buildMajor( "TestIsEnabledMajor" );
        rtuLock( majorLib );
        TestLibrary.addOneOfEachValid( majorLib );

        OtmMinorLibrary minorLib = buildMinor( majorLib );
        rtuLock( minorLib );
        tester.setLibrary( minorLib );
        assertTrue( "Given: Chain must be editable.", minorLib.isChainEditable() );

        // Test each member in the major library and its children owners
        // Can't use testMembers() because that creates new members. - tester.testMembers();
        for (OtmLibraryMember member : majorLib.getMembers()) {
            tester.testMember( member, isEnabledIfVersioned( member ) );

            for (OtmChildrenOwner child : member.getDescendantsChildrenOwners())
                tester.testChildrenOwner( child, isEnabledIfVersioned( child ) );
        }

        // tester.testProperties();

    }

    public boolean isEnabledIfVersioned(OtmObject obj) {
        boolean enabled = obj instanceof OtmPropertyOwner;
        // Disable member types that are not allowed to have added properties.
        if (obj.getOwningMember() instanceof OtmEnumeration)
            enabled = false;
        return enabled;
    }

    @Test
    public void testIsEnabled_Major() {
        OtmMajorLibrary majorLib = buildMajor( "TestIsEnabledMajor" );
        rtuLock( majorLib );

        tester.setLibrary( majorLib );
        tester.testMembers();
        tester.testProperties();
    }


}
