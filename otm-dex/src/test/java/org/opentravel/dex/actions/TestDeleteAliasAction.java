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

package org.opentravel.dex.actions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmFacets.OtmAlias;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestLibraryMemberBase;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLLibrary;

import java.io.IOException;

/**
 * Verifies the functions of the <code>delete alias action</code> class.
 */
public class TestDeleteAliasAction {

    private static Log log = LogFactory.getLog( TestDeleteAliasAction.class );

    private static OtmModelManager staticModelManager = null;
    static OtmLibrary lib = null;
    static OtmBusinessObject globalBO = null;

    @BeforeClass
    public static void beforeClass() throws IOException {
        staticModelManager = new OtmModelManager( new DexFullActionManager( null ), null, null );
        lib = staticModelManager.add( new TLLibrary() );
        assertTrue( lib.isEditable() );
        assertTrue( lib.getActionManager() instanceof DexFullActionManager );

        globalBO = (OtmBusinessObject) lib.add( TestBusiness.buildOtm( staticModelManager, "GlobalBO" ) );
        assertTrue( globalBO != null );
        assertTrue( globalBO.getLibrary() == lib );
        assertTrue( globalBO.isEditable() );
        assertTrue( globalBO.getActionManager() == lib.getActionManager() );
        assertTrue( staticModelManager.getMembers().contains( globalBO ) );
        assertTrue( "Given: ", globalBO.getActionManager() instanceof DexFullActionManager );
    }

    @Test
    public void testGettingAction() {
        TLAlias tla = new TLAlias();
        OtmAlias alias = new OtmAlias( tla, globalBO );
        assertTrue( getAction( alias ) instanceof DeleteAliasAction );
    }

    /**
     * Get the delete alias action using the member's action manager.
     * 
     * @param member
     * @return
     */
    public static DeleteAliasAction getAction(OtmAlias alias) {
        assertTrue( "Given: ", alias.getActionManager() instanceof DexFullActionManager );
        DexAction<?> action = null;
        try {
            action = DexActions.getAction( DexActions.DELETEALIAS, alias, globalBO.getActionManager() );
        } catch (ExceptionInInitializerError | InstantiationException | IllegalAccessException e) {
            log.debug( "Error getting action. " + e.getMessage() );
        }
        return (DeleteAliasAction) action;
    }

    /**
     * Add an alias to all members of the library
     * 
     * @param lib
     * @param aliasRoot
     */
    public static void addAliases(OtmLibrary lib, String aliasRoot) {
        int i = 1;
        for (OtmLibraryMember m : lib.getMembers()) {
            assertTrue( "Must not have previous aliases.", getFirstAlias( m ) == null );
            TLAlias tla = new TLAlias();
            tla.setName( aliasRoot + i++ );
            m.addAlias( tla );
            if (AddAliasAction.isEnabled( m ))
                assertTrue( getFirstAlias( m ) != null );
        }
    }

    /**
     * Return true if a child is an alias
     */
    public static OtmAlias getFirstAlias(OtmLibraryMember member) {
        for (OtmObject m : member.getChildren())
            if (m instanceof OtmAlias)
                return (OtmAlias) m;
        return null;
    }

    @Test
    public void testNullParams() throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {
        OtmAlias alias = null;
        OtmObject obj = null;
        assertFalse( DeleteAliasAction.isEnabled( alias ) );
        assertFalse( DeleteAliasAction.isEnabled( alias, obj ) );
        assertFalse( DeleteAliasAction.isEnabled( alias, globalBO ) );

        DexAction<?> action = null;

        try {
            action = DexActions.getAction( DexActions.DELETEALIAS, alias, globalBO.getActionManager() );
        } catch (Exception e) {
            // Expect exception
        }
        assertTrue( action == null );
        try {
            action = DexActions.getAction( DexActions.DELETEALIAS, alias, obj, globalBO.getActionManager() );
        } catch (Exception e) {
            // Expect exception
        }
        assertTrue( action == null );
        try {
            action = DexActions.getAction( DexActions.DELETEALIAS, alias, globalBO, globalBO.getActionManager() );
        } catch (Exception e) {
            // Expect exception
        }
        assertTrue( action == null );
    }

    @Test
    public void testDoIt() {
        // Given - library with one of each member type in it
        TestLibraryMemberBase.buildOneOfEachWithProperties( staticModelManager, lib );
        addAliases( lib, "TestAlias" );
        // Given - initial size of library
        int initialSize = lib.getMembers().size();

        // When - For each library member delete alias
        for (OtmLibraryMember lm : lib.getMembers()) {
            OtmAlias alias = getFirstAlias( lm );
            if (alias == null)
                continue; // Not all library members can have aliases

            assertTrue( "Given: must be enabled.", DeleteAliasAction.isEnabled( alias ) );

            // When - getting the action
            DeleteAliasAction action = getAction( alias );
            assertTrue( "Must be able to get an action for this alias.", action != null );

            // When - incorrect execution
            Object a = lm.getActionManager().run( DexActions.DELETEALIAS, lm );
            assertTrue( "Must have null result because was run incorrectly.", a == null );

            // When - run correctly
            a = lm.getActionManager().run( DexActions.DELETEALIAS, alias );
            assertTrue( "Must have parent as result.", a == lm );

            // Then
            assertTrue( "Must not have an alias.", getFirstAlias( lm ) == null );
            // Then - queue is larger
            assertTrue( "Action queue must have action.",
                lm.getActionManager().getLastAction() instanceof DeleteAliasAction );

            // When - undone
            lm.getActionManager().getLastAction().undoIt();
            // Then
            assertTrue( "Must  have  alias.", getFirstAlias( lm ) == alias );
        }
        // Then - safety check
        assertTrue( "Library must be same size.", lib.getMembers().size() == initialSize );
    }

}
