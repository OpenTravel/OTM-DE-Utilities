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

package org.opentravel.model.otmProperties;

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmSimpleObject;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;
import org.opentravel.model.otmLibraryMembers.TestCore;
import org.opentravel.model.otmLibraryMembers.TestOtmSimple;
import org.opentravel.model.otmLibraryMembers.TestValueWithAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class for Property Type Elements
 * <p>
 */
public class TestOtmTypeUserInterface {
    private static Log log = LogFactory.getLog( TestOtmTypeUserInterface.class );

    @BeforeClass
    public static void beforeClass() {
        // staticModelManager = new OtmModelManager( null, null, null );
        // baseObject = TestBusiness.buildOtm( staticModelManager );
        // // testResource = TestResource.buildOtm( staticModelManager );
        //
        log.debug( "Before class ran." );
    }

    // TODO - implement test for rest of interface methods
    // @Test
    // public void test_assignedTypeProperty() {}
    //
    // @Test
    // public void test_getAssignedTLType() {}
    //
    // @Test
    // public void test_getAssignedType() {}
    //
    // @Test
    // public void test_getTlAssignedTypeName() {}
    //
    // @Test
    // public void test_setAssignedTLType() {}
    //
    // @Test
    // public void test_setTLTypeName() {}


    @Test
    public void test_setAssignedType() {
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmSimpleObject provider = TestOtmSimple.buildOtm( lib, "Provider" );

        List<OtmTypeUser> users = buildOneOfEach( lib, provider, true );
        checkTypeAssignment( users, provider );

        List<OtmTypeProvider> providers = TestOtmTypeProviderInterface.buildOneOfEach( lib );
        for (OtmTypeProvider p : providers) {
            checkTypeAssignment( users, p );
        }

        // Move provider to new library then check
        OtmLibrary lib2 =
            TestLibrary.buildOtm( lib.getModelManager(), lib.getBaseNamespace() + "2", lib.getPrefix() + "2", "Lib2" );
        // Given a list of users assignable to provider
        List<OtmTypeUser> assignable = new ArrayList<>();
        for (OtmTypeUser user : users)
            if (provider == user.setAssignedType( provider ))
                assignable.add( user );
        for (OtmTypeUser user : users) {
            assertTrue( user.getAssignedType() == provider );
            assertTrue( provider.getOwningMember().getWhereUsed().contains( user.getOwningMember() ) );
            assertTrue( user.getOwningMember().getUsedTypes().contains( provider ) );
        }
        // When provider moved to new library
        lib2.add( provider );
        // Then - still assigned correctly
        for (OtmTypeUser user : users) {
            assertTrue( user.getAssignedType() == provider );
            assertTrue( provider.getOwningMember().getWhereUsed().contains( user.getOwningMember() ) );
            assertTrue( user.getOwningMember().getUsedTypes().contains( provider ) );
        }
    }

    /**
     * For each user, attempt to assign provider. If successful, check assigned type, where used and used types.
     */
    public static void checkTypeAssignment(List<OtmTypeUser> users, OtmTypeProvider provider) {
        if (provider instanceof OtmContextualFacet)
            return;

        log.debug( "Checking assignments to " + provider.getObjectTypeName() + " " + provider );
        for (OtmTypeUser user : users)
            // Not all providers can be assigned to all users
            if (provider == user.setAssignedType( provider )) {
                // Then - getAssignedType
                assertTrue( user.getAssignedType() == provider );
                // Then - where used
                assertTrue( provider.getOwningMember().getWhereUsed().contains( user.getOwningMember() ) );
                // Then - types used
                assertTrue( user.getOwningMember().getUsedTypes().contains( provider ) );
            }
    }

    /**
     * Build one of each type user in the passed library. Properties will be owned by the created core object.
     * 
     * @param lib
     * @return
     */
    public static List<OtmTypeUser> buildOneOfEach(OtmLibrary lib, OtmTypeProvider provider, boolean assignableOnly) {
        List<OtmTypeUser> typeUsers = new ArrayList<>();
        List<OtmTypeUser> assignable = new ArrayList<>();

        OtmCore core = TestCore.buildOtm( lib, "TypeUserCore" );
        OtmValueWithAttributes vwa = TestValueWithAttributes.buildOtm( lib, "TypeUserVWA" );
        OtmSimpleObject simple = TestOtmSimple.buildOtm( lib, "TypeUserSimple" );

        typeUsers.add( core );
        typeUsers.add( vwa );
        typeUsers.add( simple );

        TestOtmPropertiesBase.buildOneOfEach2( core.getSummary() );
        typeUsers.addAll( core.getDescendantsTypeUsers() );

        // TODO
        // typeUsers.add( TestActionFacet.buildOtm( null ) );
        // Rest of type hierarchy

        // Not all type users can be assigned.
        for (OtmTypeUser user : typeUsers)
            if (user.setAssignedType( provider ) == provider)
                assignable.add( user );

        return assignableOnly ? assignable : typeUsers;

    }


}
