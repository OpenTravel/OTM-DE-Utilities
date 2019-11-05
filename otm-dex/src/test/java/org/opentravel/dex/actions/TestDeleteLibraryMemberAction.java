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
import org.opentravel.dex.tasks.model.TypeResolverTask;
import org.opentravel.dex.tasks.model.ValidateModelManagerItemsTask;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMemberType;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestCore;
import org.opentravel.model.otmLibraryMembers.TestLibraryMemberBase;
import org.opentravel.schemacompiler.model.TLLibrary;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Verifies the functions of the <code>delete library member action</code> class.
 */
public class TestDeleteLibraryMemberAction {

    private static Log log = LogFactory.getLog( TestDeleteLibraryMemberAction.class );

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

    // TODO Re-factor this to have the action take place on a passed in type provider, then use library members and
    // descendant
    // type providers
    @Test
    public void testWhereUsed() throws ExceptionInInitializerError, InstantiationException, IllegalAccessException,
        NoSuchMethodException, InvocationTargetException {
        // Givens
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, null );
        OtmLibrary lib = TestLibrary.buildOtm( mgr );
        assertTrue( "Given", lib.isEditable() );

        // Given a simple type
        // OtmSimpleObject simple = TestOtmSimple.buildOtm( lib );
        OtmCore simple = TestCore.buildOtm( lib );
        // assertTrue( "Given", simple.isValid() );
        assertTrue( simple.getWhereUsed().isEmpty() );
        assertTrue( simple.getActionManager() instanceof DexFullActionManager );

        // Given one of each library member type in the library
        TestLibraryMemberBase.buildOneOfEachWithProperties( mgr, lib );

        // Assign everything to simple
        List<OtmTypeUser> users = new ArrayList<>();
        for (OtmLibraryMember lm : mgr.getMembers())
            for (OtmTypeUser user : lm.getDescendantsTypeUsers()) {
                OtmTypeProvider u = user.setAssignedType( simple );
                if (u == simple)
                    users.add( user );
            }
        assertFalse( simple.getWhereUsed().isEmpty() );
        List<OtmLibraryMember> userOwners = simple.getWhereUsed();
        assertFalse( users.isEmpty() );
        // FIXME - many, many duplicates in whereUsed list

        // When - run create action to delete simple.
        int queueSize = fullMgr.getQueueSize();
        simple.getActionManager().run( DexActions.DELETELIBRARYMEMBER, simple );
        assertTrue( "Then - queue size increased.", ++queueSize == fullMgr.getQueueSize() );
        assertTrue( simple.getLibrary() == null );
        assertTrue( !mgr.contains( simple ) );

        // OtmTypeProvider type = null;
        // boolean valid;
        // Then - check type users and make sure they are invalid and the type has no library
        for (OtmTypeUser user : users) {
            // type = user.getAssignedType();
            assertTrue( user.getAssignedType() != null );
            assertTrue( user.getAssignedType().getLibrary() == null );

            // assertFalse( user.isValid() );
            // log.debug( "Type user " + user + " is valid? " + user.isValid() );
        }

        userOwners = simple.getWhereUsed();
        assertTrue( "Can no longer get the full manager from object.", simple.getActionManager() != fullMgr );

        // Then - make sure where used is the same and type resolver did not change it
        assertTrue( simple.getWhereUsed() == userOwners );
        TypeResolverTask.runResolver( mgr );
        assertTrue( simple.getWhereUsed() == userOwners );

        //
        // When - undo action
        //
        fullMgr.undo();
        assertTrue( "Then - queue size dicreased.", --queueSize == fullMgr.getQueueSize() );

        // Then - type resolver will force change in whereUsed list
        assertTrue( simple.getWhereUsed() == userOwners );
        TypeResolverTask.runResolver( mgr );
        ValidateModelManagerItemsTask.runValidator( mgr );
        assertFalse( simple.getWhereUsed() == userOwners );
        // Then - Check list contents to verify resolver restored the contents correctly
        for (OtmLibraryMember owner : userOwners)
            assertTrue( simple.getWhereUsed().contains( owner ) );
        for (OtmLibraryMember owner : simple.getWhereUsed())
            assertTrue( userOwners.contains( owner ) );

        // Then - check type users and make sure they are valid and the type has library
        for (OtmTypeUser user : users) {
            // type = user.getAssignedType();
            assertTrue( user.getAssignedType() == simple );
            assertTrue( user.getAssignedType().getLibrary() != null );

            // log.debug( "Findings on " + user + " = " + ValidationUtils.getMessagesAsString( user.getFindings() ) );
            boolean valid = user.isValid();
            // log.debug( "Findings on " + user + " = "
            // + ValidationUtils.getMessagesAsString( OtmModelElement.isValid( user.getTL() ) ) );
            // assertTrue( user.isValid() );
            // log.debug( "Type user " + user + " is valid? " + user.isValid() );
        }


        log.debug( "Test delete and undelete done." );
    }

    @Test
    public void testWhereExtended() {
        // Givens
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, null );
        OtmLibrary lib = TestLibrary.buildOtm( mgr );
        assertTrue( "Given", lib.isEditable() );

        // Given one of each library member type in the library
        TestLibrary.addOneOfEach( lib );

        boolean valid = lib.isValid();
        assertTrue( valid );
    }

    @Test
    public void testMultipleMembers() {
        // DexFullActionManager fullMgr = new DexFullActionManager( null );
        // OtmModelManager mgr = new OtmModelManager( fullMgr, null );
        // OtmLibrary lib = mgr.add( new TLLibrary() );
        // DexActionManager actionManager = lib.getActionManager();
        // assertTrue( "Given", actionManager instanceof DexFullActionManager );
        // assertTrue( actionManager.getQueueSize() == 0 );
        // OtmBusinessObject testBO = TestBusiness.buildOtm( mgr, "TestBusinessObject" );
        // assertTrue( "Given", testBO.getModelManager() == mgr );
        // lib.add( testBO );
        // assertTrue( "Given", testBO.getLibrary() == lib );
        //
        // Object result = null;
        // OtmLibraryMember member = null;
        // for (LibraryMemberType type : LibraryMemberType.values()) {
        // // FIXED - DexActions no longer only uses subject's AM, not the caller
        // result = actionManager.run( DexActions.NEWLIBRARYMEMBER, testBO, type );
        // assertTrue( "Must create library member.", result instanceof OtmLibraryMember );
        // member = (OtmLibraryMember) result;
        // assertTrue( "Must only have one action in queue.", actionManager.getQueueSize() == 1 );
        // assertTrue( "Model manager must contain new member.", mgr.contains( member ) );
        //
        // // When - undo
        // actionManager.undo();
        // assertFalse( "Model manager must not contain new member.", mgr.contains( member ) );
        // assertTrue( "Action queue must be empty.", actionManager.getQueueSize() == 0 );
        // }
        // assertTrue( "Action queue must be empty.", actionManager.getQueueSize() == 0 );
        // log.debug( "New Library Member Test complete." );
    }


    @Test
    public void testEachMemberType() throws ExceptionInInitializerError, InstantiationException, IllegalAccessException,
        NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        // Givens
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, null );
        OtmLibrary lib = TestLibrary.buildOtm( mgr );
        assertTrue( "Given", lib.isEditable() );

        // Given one of each library member type in the library
        int i = 1;
        for (OtmLibraryMemberType type : OtmLibraryMemberType.values()) {
            OtmLibraryMember member = OtmLibraryMemberType.buildMember( type, "Object" + i++, mgr );
            lib.add( member );
            // mgr.add( member );
        }

        i = fullMgr.getQueueSize();
        Collection<OtmLibraryMember> members = new ArrayList<>( mgr.getMembers() );
        for (OtmLibraryMember member : members) {
            // log.debug( "Checking " + member );
            if (member.getLibrary() == lib) {
                assertTrue( "Given: member must be editable.", member.isEditable() );
                // When deleted
                member.getActionManager().run( DexActions.DELETELIBRARYMEMBER, member );
                assertTrue( "Then - queue size increased.", ++i == fullMgr.getQueueSize() );
                // Then
                assertTrue( "Then - TL library must not contain member.",
                    !lib.getTL().getNamedMembers().contains( member.getTL() ) );
                assertTrue( "Then - library is null.", member.getLibrary() == null );
                assertTrue( "Then - model manager does not contain member.", !mgr.getMembers().contains( member ) );
                // log.debug( "Deleted " + member );

            }
        }
        int mbrCount = mgr.getMembers().size();
        while (i-- > 0) {
            // When - undo
            fullMgr.undo();
            assertTrue( "Then - Model must be bigger.", ++mbrCount == mgr.getMembers().size() );
        }
    }
}
