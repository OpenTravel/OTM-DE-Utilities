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

package org.opentravel.model.otmLibraryMembers;

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.dex.actions.SetAssignedTypeAction;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmProperties.OtmElement;
import org.opentravel.model.otmProperties.OtmIdAttribute;
import org.opentravel.model.otmProperties.TestElement;
import org.opentravel.model.otmProperties.TestOtmTypeProviderInterface;
import org.opentravel.model.otmProperties.TestOtmTypeUserInterface;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;

import java.util.Collection;
import java.util.List;

/**
 * Verifies the functions related to {@linkplain OtmTypeUser#setAssignedType(OtmTypeProvider)}.
 */
public class TestTypeAssignmentAndWhereUsed {
    private static Log log = LogFactory.getLog( TestTypeAssignmentAndWhereUsed.class );

    public static void buildOneOfEachTypeProvider(OtmLibrary lib) {
        TestOtmTypeProviderInterface.buildOneOfEachTypeProvider( lib );

    }

    public static void buildOneOfEachTypeUser(OtmLibrary lib) {
        TestOtmTypeUserInterface.buildOneOfEachTypeUser( lib );
    }

    @BeforeClass
    public static void setupTests() throws Exception {}

    /**
     * Start with all users that can accept complex choice object, then assign other complex objects.
     */
    @Test
    public void testSetAssignedType() {
        OtmLibrary lib = TestLibrary.buildOtm();
        List<OtmTypeProvider> providers = TestOtmTypeProviderInterface.buildOneOfEach( lib );

        // Given - users that can be assigned to complex choice object (don't use core because it has a simple provider)
        OtmChoiceObject choice = TestChoice.buildOtm( lib, "ChoiceProvider" );
        List<OtmTypeUser> users = TestOtmTypeUserInterface.buildOneOfEach( lib, choice, true );

        for (OtmTypeProvider provider : providers) {
            log.debug( "Assigning new provider: " + provider );
            OtmTypeProvider oldProvider = null;
            for (OtmTypeUser user : users) {
                oldProvider = user.getAssignedType();
                OtmTypeProvider type = user.setAssignedType( provider );
                assertTrue( "Then: must return the assigned type.", provider == type );
                TestOtmTypeUserInterface.check( user, provider );
            }
            // A lot of the users have the same owner, check after they have all be re-assigned.
            for (OtmTypeUser user : users)
                TestOtmTypeUserInterface.checkNotAssigned( user, oldProvider );
        }

    }

    /**
     * Start with assigned to XSD String then set to null.
     */
    @Test
    public void testSetAssignedType_Cleared() {
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmXsdSimple provider = lib.getModelManager().getStringType();

        // Given - all users assigned to the string type
        List<OtmTypeUser> users = TestOtmTypeUserInterface.buildOneOfEach( lib, provider, true );

        for (OtmTypeUser user : users) {
            user.setAssignedType( null );
            assertTrue( "Then: ", user.getAssignedType() == null );
            TestOtmTypeUserInterface.check( user, null );
        }
    }

    /**
     * Test with a null initially assigned.
     */
    @Test
    public void testSetAssignedType_null() {
        OtmLibrary lib = TestLibrary.buildOtm();
        List<OtmTypeUser> users = TestOtmTypeUserInterface.buildOneOfEach( lib, null, false );

        for (OtmTypeUser user : users) {
            if (user instanceof OtmIdAttribute)
                continue;
            OtmTypeProvider type = user.getAssignedType();
            assertTrue( "Then: ", user.getAssignedType() == null );
            TestOtmTypeUserInterface.check( user, null );
        }

    }

    /**
     * Check starting with a detail facet assigned.
     */
    @Test
    public void testSetAssignedType_DetailFacet() {
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmCore userCore = TestCore.buildOtm( lib, "UserCore" );
        assertTrue( "Given: ", lib.getModelManager().getMembers().contains( userCore ) );
        OtmElement<?> ele = new OtmElement<TLProperty>(
            TestElement.buildTL( (TLPropertyOwner) userCore.getSummary().getTL() ), userCore.getSummary() );
        // Where used depends on element be listed in descendants type users
        assertTrue( "Given: new element must be child of summary facet.",
            userCore.getSummary().getChildren().contains( ele ) );
        Collection<OtmTypeUser> des = userCore.getDescendantsTypeUsers();
        assertTrue( "Then: Core must find new element as a type user.",
            userCore.getDescendantsTypeUsers().contains( ele ) );

        OtmLibrary lib2 = TestLibrary.buildOtm( lib.getModelManager() );
        OtmCore providerCore = TestCore.buildOtm( lib2, "ProviderCore" );
        OtmTypeProvider assignedType = providerCore.getDetail();
        // getWhereUsed depends on getDescendantsTypeProviders
        assertTrue( "Given: must have detail facet in type provider list.",
            providerCore.getDescendantsTypeProviders().contains( providerCore.getDetail() ) );


        // When assignment is made
        // Skip builder to avoid checks that are failing.
        // OtmElement<?> ele = TestElement.buildOtm( userCore.getSummary(), providerCore.getDetail() );
        ele.setAssignedType( assignedType );
        assertTrue( "Then: ", ele.getAssignedType() == providerCore.getDetail() );

        assertTrue( "Then: user's used types must include provider detail facet.",
            userCore.getUsedTypes().contains( assignedType ) );

        // getDescendantsTypeProviders().forEach( p -> whereUsed.addAll( mgr.findUsersOf( p ) ) );
        List<OtmLibraryMember> found = providerCore.getModelManager().findUsersOf( providerCore.getDetail() );
        assertTrue( " Then: manager must find users of the detail facet.", found.contains( userCore ) );

        // Finally, check the where used
        assertTrue( "Then: ", providerCore.getWhereUsed().contains( userCore ) );
    }

    /**
     * Check starting with an XSD String assigned.
     */
    @Test
    public void testSetAssignedType_XsdString() {
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmXsdSimple provider = lib.getModelManager().getStringType();
        List<OtmTypeUser> users = TestOtmTypeUserInterface.buildOneOfEach( lib, provider, true );

        // Given - all users are not assigned.
        for (OtmTypeUser user : users) {
            assertTrue( "Given: ", user.getAssignedType() == provider );
            TestOtmTypeUserInterface.check( user, provider );
        }
    }

    /**
     * Uses setAssignedTypeAction
     * 
     */
    @Test
    public void testSetAssignedTypeAction() {
        OtmLibrary lib = TestLibrary.buildOtm();
        List<OtmTypeProvider> providers = TestOtmTypeProviderInterface.buildOneOfEach( lib );
        // Given - users that can be assigned to complex choice object (don't use core because it has a simple provider)
        OtmChoiceObject choice = TestChoice.buildOtm( lib, "ChoiceProvider" );
        List<OtmTypeUser> users = TestOtmTypeUserInterface.buildOneOfEach( lib, choice, true );

        SetAssignedTypeAction action = new SetAssignedTypeAction();

        // Given - all other providers can also be assigned.
        for (OtmTypeProvider provider : providers) {
            for (OtmTypeUser user : users) {
                // OtmTypeProvider type = user.setAssignedType( provider );
                action.setSubject( user );
                OtmTypeProvider type = (OtmTypeProvider) action.doIt( provider );
                // Shared facet is forced to be owner
                assertTrue( "Then: must return the assigned type.", provider == type );
                assertTrue( "Then: ", user.getAssignedType() == provider );
                TestOtmTypeUserInterface.check( user, provider );
            }
        }
    }
}
