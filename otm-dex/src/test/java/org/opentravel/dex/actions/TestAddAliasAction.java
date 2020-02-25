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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmServiceObject;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestLibraryMemberBase;
import org.opentravel.schemacompiler.model.TLLibrary;

import java.io.IOException;

/**
 * Verifies the functions of the <code>copy library member action</code> class.
 */
public class TestAddAliasAction {

    private static Log log = LogFactory.getLog( TestAddAliasAction.class );

    private static OtmModelManager staticModelManager = null;
    static OtmLibrary lib = null;
    static OtmBusinessObject globalBO = null;

    @BeforeClass
    public static void beforeClass() throws IOException {
        staticModelManager = new OtmModelManager( new DexFullActionManager( null ), null );
        lib = staticModelManager.add( new TLLibrary() );
        assertTrue( lib.isEditable() );
        assertTrue( lib.getActionManager() instanceof DexFullActionManager );

        globalBO = (OtmBusinessObject) lib.add( TestBusiness.buildOtm( staticModelManager, "GlobalBO" ) );

        assertTrue( globalBO != null );
        assertTrue( globalBO.getLibrary() == lib );
        assertTrue( globalBO.isEditable() );
        assertTrue( globalBO.getActionManager() == lib.getActionManager() );
        assertTrue( staticModelManager.getMembers().contains( globalBO ) );

    }

    @Test
    public void testGettingAction() {
        assertTrue( "Given: ", globalBO.getActionManager() instanceof DexFullActionManager );
        // When - Then
        assertTrue( getAction() instanceof CopyLibraryMemberAction );
    }

    public CopyLibraryMemberAction getAction() {
        assertTrue( "Given: ", globalBO.getActionManager() instanceof DexFullActionManager );
        // When
        DexAction<?> action = null;
        try {
            action = DexActions.getAction( DexActions.COPYLIBRARYMEMBER, globalBO, globalBO.getActionManager() );
        } catch (ExceptionInInitializerError | InstantiationException | IllegalAccessException e) {
            log.debug( "Error getting action. " + e.getMessage() );
        }
        // Then
        assertTrue( action instanceof CopyLibraryMemberAction );
        return (CopyLibraryMemberAction) action;
    }

    @Test
    public void testDoIt() {
        // Given - library with one of each member type in it
        TestLibraryMemberBase.buildOneOfEachWithProperties( staticModelManager, lib );
        // Given - another library to copy to
        OtmLibrary targetLib = TestLibrary.buildOtm( staticModelManager );
        assertTrue( "Given: ", targetLib.isEditable() );
        // Given - the action
        CopyLibraryMemberAction action = getAction();

        // When - copy each member in lib to target lib
        for (OtmLibraryMember lm : staticModelManager.getMembers( lib )) {
            action.setSubject( lm );
            action.doIt( targetLib );
        }
        // Then -
        assertTrue( "targetLib has members.", !targetLib.getMembers().isEmpty() );
        assertTrue( "Both libraries must be same size.", lib.getMembers().size() == targetLib.getMembers().size() );

        //
        // When - run with copy being added to source library
        int initialSize = lib.getMembers().size();
        int currentSize = 0;
        int copies = 0;
        for (OtmLibraryMember lm : staticModelManager.getMembers( lib )) {
            action.setSubject( lm );
            if (action.doIt( lib ) != null)
                copies++;
            else
                assertTrue( "Must fail to copy service to same library.", lm instanceof OtmServiceObject );
            currentSize = lib.getMembers().size();
            assertTrue( "Must have new member count.", lib.getMembers().size() == initialSize + copies );
        }
        int finalSize = lib.getMembers().size();
        assertTrue( "Must have 2x member count.", lib.getMembers().size() == initialSize + copies );
    }

    // @Test
    // public void testObjectsWithContextualFacets() {
    // // TODO
    // }
}
