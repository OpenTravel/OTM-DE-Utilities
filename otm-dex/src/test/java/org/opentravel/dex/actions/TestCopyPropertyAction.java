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
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmFacets.OtmAlias;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestCore;
import org.opentravel.model.otmLibraryMembers.TestLibraryMemberBase;
import org.opentravel.model.otmProperties.OtmProperty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Verifies the functions of the <code>copy property action</code> class.
 */
public class TestCopyPropertyAction {

    private static Log log = LogFactory.getLog( TestCopyPropertyAction.class );

    private static OtmModelManager staticModelManager = null;
    static OtmLibrary lib = null;
    static OtmBusinessObject globalBO = null;

    @BeforeClass
    public static void beforeClass() throws IOException {
        lib = TestLibrary.buildOtm();
        staticModelManager = lib.getModelManager();

        globalBO = TestBusiness.buildOtm( lib, "GlobalBO" );

        // assertTrue( globalBO != null );
        // assertTrue( globalBO.getLibrary() == lib );
        // assertTrue( globalBO.isEditable() );
        // assertTrue( globalBO.getActionManager() == lib.getActionManager() );
        // assertTrue( staticModelManager.getMembers().contains( globalBO ) );
        //
        TestLibrary.checkContentsAreEditable( lib );
    }

    @Test
    public void testGettingAction() {
        assertTrue( "Given: ", globalBO.getActionManager() instanceof DexFullActionManager );
        assertTrue( "Given ", globalBO.getSummary().getChildren().get( 0 ) instanceof OtmProperty );
        // When
        DexRunAction action = getAction( (OtmProperty) globalBO.getSummary().getChildren().get( 0 ) );
        // Then
        assertTrue( action instanceof CopyPropertyAction );
    }

    public CopyPropertyAction getAction(OtmProperty property) {
        assertTrue( "Given: ", property.getActionManager() instanceof DexFullActionManager );
        // When
        DexAction<?> action = null;
        try {
            action = DexActions.getAction( DexActions.COPYPROPERTY, property, globalBO.getActionManager() );
        } catch (ExceptionInInitializerError | InstantiationException | IllegalAccessException e) {
            log.debug( "Error getting action. " + e.getMessage() );
        }
        // Then
        // assertTrue( action instanceof AddAliasAction );
        return (CopyPropertyAction) action;
    }

    @Test
    public void testNullParams() throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {
        OtmAlias alias = null;
        OtmObject obj = null;
        assertFalse( CopyPropertyAction.isEnabled( alias ) );
        assertFalse( CopyPropertyAction.isEnabled( alias, obj ) );
        assertFalse( CopyPropertyAction.isEnabled( alias, globalBO ) );

        DexAction<?> action = null;

        try {
            action = DexActions.getAction( DexActions.COPYPROPERTY, alias, globalBO.getActionManager() );
        } catch (Exception e) {
            // Expect exception
        }
        assertTrue( action == null );
        try {
            action = DexActions.getAction( DexActions.COPYPROPERTY, alias, obj, globalBO.getActionManager() );
        } catch (Exception e) {
            // Expect exception
        }
        assertTrue( action == null );
        try {
            action = DexActions.getAction( DexActions.COPYPROPERTY, alias, globalBO, globalBO.getActionManager() );
        } catch (Exception e) {
            // Expect exception
        }
        assertTrue( action == null );
    }

    @Test
    public void testDoIt() {
        // Given - library with one of each member type in it
        TestLibraryMemberBase.buildOneOfEachWithProperties( lib );
        for (OtmLibraryMember lm : lib.getMembers()) {
            assertTrue( lm.isEditable() );
            for (OtmObject d : lm.getDescendants())
                assertTrue( d.isEditable() );
        }

        // Given - a second propertyOwner
        OtmCore core = TestCore.buildOtm( lib );
        OtmPropertyOwner secondOwner = core.getSummary();
        int secondOwnerSize = secondOwner.getChildren().size();
        assertTrue( "Given ", secondOwner.isEditable() );

        // Given - initial size of library
        int initialSize = lib.getMembers().size();


        // When - For each library member type
        for (OtmLibraryMember member : lib.getMembers()) {
            assertTrue( "Given ", member.isEditable() );

            for (OtmPropertyOwner propertyOwner : member.getDescendantsPropertyOwners()) {
                assertTrue( "Given ", propertyOwner.isEditable() );

                List<OtmObject> initialKids = new ArrayList<>( propertyOwner.getChildren() );
                for (OtmObject p : initialKids) {
                    Object a = null;
                    assertTrue( "Given ", p.isEditable() );
                    assertTrue( "Must have copy enabled.", CopyPropertyAction.isEnabled( p ) );

                    //
                    // Test with same parent
                    //
                    log.debug( "Test " + p );
                    // When - executed
                    assertTrue( ((OtmProperty) p).getParent().isEditable() );
                    a = member.getActionManager().run( DexActions.COPYPROPERTY, p );
                    assertTrue( propertyOwner.getChildren().size() > initialKids.size() );

                    // When - undone
                    DexAction<?> action = member.getActionManager().getLastAction();
                    action.undoIt();
                    List<OtmObject> kids = propertyOwner.getChildren();
                    assertTrue( propertyOwner.getChildren().size() == initialKids.size() );

                    //
                    // Test with different parent
                    //
                    if (CopyPropertyAction.isEnabled( p, secondOwner )) {

                        // When - executed
                        a = member.getActionManager().run( DexActions.COPYPROPERTY, p, secondOwner );
                        assertTrue( secondOwner.getChildren().size() > secondOwnerSize );

                        // When - undone
                        member.getActionManager().getLastAction().undoIt();
                        assertTrue( secondOwner.getChildren().size() == secondOwnerSize );
                    }
                }
            }
        }
        // Then -
        assertTrue( "Library must be same size.", lib.getMembers().size() == initialSize );
    }

}
