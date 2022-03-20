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

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmFacets.OtmAlias;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestLibraryMemberBase;

import java.io.IOException;

/**
 * Verifies the functions of the <code>copy library member action</code> class.
 */
public class TestAddAliasAction {

    private static Logger log = LogManager.getLogger( TestAddAliasAction.class );

    private static OtmModelManager staticModelManager = null;
    static OtmLibrary lib = null;
    static OtmBusinessObject globalBO = null;

    @BeforeClass
    public static void beforeClass() throws IOException {
        lib = TestLibrary.buildOtm();
        staticModelManager = lib.getModelManager();
        assertTrue( lib.isEditable() );
        assertTrue( lib.getActionManager() instanceof DexFullActionManager );

        globalBO = TestBusiness.buildOtm( lib, "GlobalBO" );

        // Tested in buildOtm()
        // assertTrue( globalBO != null );
        // assertTrue( globalBO.getLibrary() == lib );
        // assertTrue( globalBO.isEditable() );
        // assertTrue( globalBO.getActionManager() == lib.getActionManager() );
        // assertTrue( staticModelManager.getMembers().contains( globalBO ) );

    }

    // FIXME - i am getting NPE when validating a deleted alias.
    //
    @Test
    public void testGettingAction() {
        assertTrue( "Given: ", globalBO.getActionManager() instanceof DexFullActionManager );
        // When - Then
        assertTrue( getAction( globalBO ) instanceof AddAliasAction );
    }

    public AddAliasAction getAction(OtmLibraryMember member) {
        assertTrue( "Given: ", member.getActionManager() instanceof DexFullActionManager );
        // When
        DexAction<?> action = null;
        try {
            action = DexActions.getAction( DexActions.ADDALIAS, member, globalBO.getActionManager() );
        } catch (ExceptionInInitializerError | InstantiationException | IllegalAccessException e) {
            log.debug( "Error getting action. " + e.getMessage() );
        }
        // Then
        // assertTrue( action instanceof AddAliasAction );
        return (AddAliasAction) action;
    }

    @Test
    public void testDoIt() {
        // Given - library with one of each member type in it
        TestLibraryMemberBase.buildOneOfEachWithProperties( lib );
        // Given - initial size of library
        int initialSize = lib.getMembers().size();

        // When - For each library member type, add then remove alias
        for (OtmLibraryMember lm : staticModelManager.getMembers( lib )) {
            // Given - the action
            // AddAliasAction action = getAction( lm );

            // Skip those that are not enabled and do not return an action
            if (!AddAliasAction.isEnabled( lm ))
                continue; // Not all library members can have aliases

            // When - executed
            // Object a = action.doIt();
            Object a = lm.getActionManager().run( DexActions.ADDALIAS, lm );
            // Then
            // FIXME - alias has right TLAlias but is not the child of lm
            assertTrue( "Must have an alias.", a instanceof OtmAlias );
            assertTrue( lm.getChildren().contains( a ) );
            // Then - queue is larger
            log.debug( "Last Action: " + lm.getActionManager().getLastActionName() );
            assertTrue( "Action queue must have action.", lm.getActionManager().getLastAction() != null );

            // When - undone
            lm.getActionManager().getLastAction().undoIt();
            // Then
            assertTrue( "Must not have  alias.", !lm.getChildren().contains( a ) );
        }
        // Then -
        assertTrue( "Library must be same size.", lib.getMembers().size() == initialSize );
    }

}
