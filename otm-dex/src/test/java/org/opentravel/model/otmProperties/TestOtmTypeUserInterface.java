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
import org.opentravel.dex.actions.TestSetLibraryAction;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.OtmTypeUserUtils;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmSimpleObject;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestChoice;
import org.opentravel.model.otmLibraryMembers.TestCore;
import org.opentravel.model.otmLibraryMembers.TestOtmSimple;
import org.opentravel.model.otmLibraryMembers.TestValueWithAttributes;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class for methods in {@linkplain OtmTypeUser} interface.
 * 
 * <p>
 * SetTypeAssignment tested in {@linkplain TestOtmTypeProviderInterface}
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


    /**
     * Check assignment when member is moved using {@linkplain OtmLibrary#add(OtmLibraryMember)}
     * <p>
     * {@linkplain TestSetLibraryAction#testDoIt()}
     */
    @Test
    public void testAssignedTypeWhenProviderLibraryMoved() {
        // OtmLibrary lib = TestLibrary.buildOtm();
        // OtmSimpleObject provider = TestOtmSimple.buildOtm( lib, "Provider" );
        //
        // List<OtmTypeUser> users = buildOneOfEach( lib, provider, true );
        // testAndCheckTypeAssignment( users, provider );
        //
        // List<OtmTypeProvider> providers = TestOtmTypeProviderInterface.buildOneOfEach( lib );
        // for (OtmTypeProvider p : providers) {
        // testAndCheckTypeAssignment( users, p );
        // }
        //
        // // Move provider to new library then check
        // OtmLibrary lib2 =
        // TestLibrary.buildOtm( lib.getModelManager(), lib.getBaseNS() + "2/v1", lib.getPrefix() + "2", "Lib2" );
        // // Given a list of users assignable to provider
        // List<OtmTypeUser> assignable = new ArrayList<>();
        // for (OtmTypeUser user : users)
        // if (provider == user.setAssignedType( provider ))
        // assignable.add( user );
        // for (OtmTypeUser user : assignable) {
        // check( user, provider );
        // }
        // // When provider moved to new library
        // SetLibraryAction action = new SetLibraryAction();
        // action.setSubject( provider );
        // action.doIt( lib2 );
        // // lib2.add( provider );
        //
        // // Then - still assigned correctly
        // for (OtmTypeUser user : users) {
        // check( user, provider );
        // }
    }

    /**
     * Check assignment when member is moved using {@linkplain OtmLibrary#add(OtmLibraryMember)}
     */
    @Test
    public void testAssignedTypeWhenProviderLibraryAdded() {
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmSimpleObject provider = TestOtmSimple.buildOtm( lib, "Provider" );

        List<OtmTypeUser> users = buildOneOfEach( lib, provider, true );
        testAndCheckTypeAssignment( users, provider );

        List<OtmTypeProvider> providers = TestOtmTypeProviderInterface.buildOneOfEach( lib );
        for (OtmTypeProvider p : providers) {
            testAndCheckTypeAssignment( users, p );
        }

        // Move provider to new library then check
        OtmLibrary lib2 =
            TestLibrary.buildOtm( lib.getModelManager(), lib.getBaseNS() + "2/v1", lib.getPrefix() + "2", "Lib2" );
        // Given a list of users assignable to provider
        List<OtmTypeUser> assignable = new ArrayList<>();
        for (OtmTypeUser user : users)
            if (provider == user.setAssignedType( provider ))
                assignable.add( user );
        for (OtmTypeUser user : assignable) {
            check( user, provider );
        }
        // When provider moved to new library
        lib2.add( provider );

        // Then - still assigned correctly
        for (OtmTypeUser user : users) {
            check( user, provider );
        }
    }

    /**
     * For each user, attempt to assign provider. If successful, check assigned type, where used and used types.
     */
    public static void testAndCheckTypeAssignment(List<OtmTypeUser> users, OtmTypeProvider provider) {
        if (provider instanceof OtmContextualFacet)
            return;

        log.debug( "Checking assignments to " + provider.getObjectTypeName() + " " + provider );
        for (OtmTypeUser user : users)
            // Not all providers can be assigned to all users
            if (provider == user.setAssignedType( provider )) {
                check( user, provider );
            }
    }

    /**
     * Check to assure any descendant of user's owner does not use any descendant in provider's owner as a type.
     * <ul>
     * <li>owner's whereUsed
     * <li>owner's typesUsed
     * 
     * @param user
     * @param oldProvider can be null
     */
    public static void checkNotAssigned(OtmTypeUser user, OtmTypeProvider oldProvider) {
        assertTrue( "Check: Must NOT be assignedType ", user.getAssignedType() != oldProvider );
        if (user.getAssignedType().getOwningMember() == oldProvider.getOwningMember()) {
            log.debug( "Skipping tests: checking not assigned, but old provider has same owner as assigned type." );
            return;
        }
        if (oldProvider != null) {
            OtmLibraryMember usersOwner = user.getOwningMember();
            OtmLibraryMember providersOwner = oldProvider.getOwningMember();
            assertTrue( "Check: must have owners.", usersOwner != null && providersOwner != null );
            // Lists
            assertTrue( "Check: Must NOT be in old provider's where used.",
                !providersOwner.getWhereUsed().contains( usersOwner ) );
            assertTrue( "Check: Must NOT be in user's type users list for the old provider.",
                usersOwner.getTypeUsers( oldProvider ).isEmpty() );
            assertTrue( "Check: Must NOT be in user's type users list for the old provider.",
                usersOwner.getTypeUsers( oldProvider ).isEmpty() );
            // Model Manager
            assertTrue( "Check: Model Manager must NOT find user.",
                !oldProvider.getModelManager().findUsersOf( oldProvider ).contains( user ) );
        }
    }

    /**
     * Check:
     * <ul>
     * <li>user is assigned provider
     * <li>AssignedTypeProperty has name of provider
     * <li>TL assignment,
     * <li>owner's used types and
     * <li>provider's where used.
     * <li>Manager must find the user
     * </ul>
     * 
     * @param user
     * @param provider can be null
     */
    public static void check(OtmTypeUser user, OtmTypeProvider provider) {
        // Then - getAssignedType
        assertTrue( "Check: Must be assignedType ", user.getAssignedType() == provider );

        if (provider == null) {
            assertTrue( "Check: Must have null TL assigned type.", user.getAssignedTLType() == null );
        } else {
            // Formatting of prefix and name may not be same as getName or getName
            assertTrue( "Check: FX property must be set.",
                user.assignedTypeProperty().get().contains( provider.getName() ) );
            assertTrue( "Check: FX property must be set.",
                user.assignedTypeProperty().get().equals( OtmTypeUserUtils.formatAssignedType( user ) ) );

            assertTrue( "Check: TL be assignedType.", user.getAssignedTLType() == provider.getTL() );
            assertTrue( "Check: Owner's used types must include provider.",
                user.getOwningMember().getUsedTypes().contains( provider ) );

            assertTrue( "Check: ", provider.getOwningMember().getWhereUsed().contains( user.getOwningMember() ) );

            if (user instanceof OtmElement && provider instanceof NamedEntity) {
                assertTrue( "Check - element TL type name must be assignedType's name.", ((TLProperty) user.getTL())
                    .getTypeName().equals( ((NamedEntity) provider.getTL()).getLocalName() ) );
                assertTrue( "Check - element's TL type must be assignedType's TL.",
                    ((TLProperty) user.getTL()).getType() == provider.getTL() );
            }

            List<OtmLibraryMember> foundUsers = provider.getModelManager().findUsersOf( provider );
            assertTrue( "Check: Manager must include user when finding users of provider.",
                foundUsers.contains( user.getOwningMember() ) );

            // log.debug( "Checked assignment of " + provider.getObjectTypeName() + " " + provider + " to "
            // + user.getObjectTypeName() + " " + user );
        }
    }

    /**
     * Build one of each type user in the passed library. Properties will be owned by the created core object.
     * 
     * @param lib
     * @param provider if null, null will be assigned as type (some users, such as ID Attributes, can't be set to null)
     * @param assignable if true and if provider is non-null, only the successfully assigned users are returned.
     * @return
     */
    public static List<OtmTypeUser> buildOneOfEach(OtmLibrary lib, OtmTypeProvider provider, boolean assignableOnly) {
        List<OtmTypeUser> typeUsers = new ArrayList<>();
        List<OtmTypeUser> assignable = new ArrayList<>();

        // OtmLibraryMembers that are type users
        OtmCore core = TestCore.buildOtm( lib, "TypeUserCore" );
        OtmValueWithAttributes vwa = TestValueWithAttributes.buildOtm( lib, "TypeUserVWA" );
        OtmSimpleObject simple = TestOtmSimple.buildOtm( lib, "TypeUserSimple" );
        typeUsers.add( core );
        typeUsers.add( vwa );
        typeUsers.add( simple );

        // Properties that are type users
        TestOtmPropertiesBase.buildOneOfEach2( core.getSummary() );
        typeUsers.addAll( core.getDescendantsTypeUsers() );

        // TODO - all other type users (Rest of type hierarchy)
        // typeUsers.add( TestActionFacet.buildOtm( null ) );

        // Not all type users can be assigned.
        for (OtmTypeUser user : typeUsers) {
            if (user instanceof OtmIdAttribute)
                continue;
            if (user.setAssignedType( provider ) == provider)
                assignable.add( user );
        }

        for (OtmTypeUser user : assignable)
            check( user, provider );
        assertTrue( "Builder: ", !typeUsers.isEmpty() );
        assertTrue( "Builder: ", !assignable.isEmpty() );

        return assignableOnly ? assignable : typeUsers;

    }

    /**
     * Build business, choice, vwa, core and simple objects.
     * 
     * @param lib
     */
    public static void buildOneOfEachTypeUser(OtmLibrary lib) {
        // attributes
        // elements
        TestBusiness.buildOtm( lib, "UserBO" );
        TestChoice.buildOtm( lib, "UserChoice" );
        TestValueWithAttributes.buildOtm( lib, "UserVWA" );
        TestCore.buildOtm( lib, "UserCore" );
        TestOtmSimple.buildOtm( lib, "UserSimple" );

        // reference elements and attributes
        // core and vwa
        // simple
        // Resource and action facet
    }

}
