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
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmFacets.OtmSummaryFacet;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmExtensionPointFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.OtmServiceObject;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestCore;
import org.opentravel.model.otmProperties.OtmElement;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.TestElement;
import org.opentravel.model.otmProperties.TestOtmPropertiesBase;
import org.opentravel.schemacompiler.model.TLLibrary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Verifies the functions of the <code>delete property action</code> class.
 */
public class TestDeleteLibraryMemberAction {
    private static Log log = LogFactory.getLog( TestDeleteLibraryMemberAction.class );

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

    }

    @Test
    public void testDeleteMemberAction() {
        TestLibrary.addOneOfEach( lib );
        assertTrue( "Given: ", !staticModelManager.getMembers( lib ).isEmpty() );
        int memberCount = lib.getMembers().size();

        // Given - a property in a different library with each LM assigned
        OtmLibrary tlib = TestLibrary.buildOtm( staticModelManager );
        assertTrue( tlib.isEditable() );
        OtmCore core = TestCore.buildOtm( tlib );
        OtmSummaryFacet facet = core.getSummary();
        OtmElement<?> e = null;
        ArrayList<OtmElement<?>> elements = new ArrayList<>();

        //
        // Test Deleting
        for (OtmLibraryMember member : staticModelManager.getMembers( lib )) {
            // log.debug( "Checking " + member );
            assertTrue( "Given: ", member.getLibrary() == lib );
            assertTrue( "Given: member must be editable.", member.isEditable() );
            assertTrue( "Delete must be enabled.", DeleteLibraryMemberAction.isEnabled( member ) );
            // Create property with member as the type
            if (member instanceof OtmTypeProvider && !(member instanceof OtmResource)
                && !(member instanceof OtmServiceObject) && !(member instanceof OtmExtensionPointFacet)) {
                e = TestElement.buildOtm( facet );
                e.setAssignedType( (OtmTypeProvider) member );
                e.setName( member.getName() );
                elements.add( e );
                assertTrue( "Given:", e.getName() != null );
            }
            List<OtmLibraryMember> users = member.getWhereUsed();

            // Delete
            OtmLibraryMember result =
                (OtmLibraryMember) member.getActionManager().run( DexActions.DELETELIBRARYMEMBER, member );

            // Then
            assertTrue( "Must return deleted member.", result == member );
            assertTrue( "Library must not have member.", !lib.getMembers().contains( member ) );
            // // 4/28/2020 - dmh
            // // Leave the type assigned. The member object is gone so it will be an error but at least the user
            // // will get some indication of what was assigned.
            // for (OtmLibraryMember user : users) {
            // assertTrue( "Must not use member as assigned type.", !user.getUsedTypes().contains( member ) );
            // }
        }
        // Then - all members are deleted
        assertTrue( "Given: ", staticModelManager.getMembers( lib ).isEmpty() );
        assertTrue( "Given: ", lib.getMembers().isEmpty() );

        //
        // Test Undo
        //
        DexActionManager mgr = lib.getActionManager();
        while (mgr.getQueueSize() > 0) {
            mgr.undo();
        }
        assertTrue( "All members must have been restored.", lib.getMembers().size() == memberCount );
        List<OtmLibraryMember> members = lib.getMembers();
        for (OtmElement<?> t : elements)
            assertTrue( lib.getMembers().contains( t.getAssignedType() ) );
        log.debug( "Tested deleting and un-deleting " + memberCount + " members." );
    }

    @Test
    public void testAction() {
        // Given - a business object with one of each property type
        OtmBusinessObject bo = TestBusiness.buildOtm( lib, "TestOwner" );
        assertTrue( "Given", bo.isEditable() );
        TestOtmPropertiesBase.buildOneOfEach2( bo.getSummary() );
        int initialChildCount = bo.getSummary().getChildren().size();
        assertTrue( "Given", initialChildCount > 0 );

        ArrayList<OtmObject> children = new ArrayList<>( bo.getSummary().getChildren() );
        for (OtmObject obj : children) {
            if (!(obj instanceof OtmProperty))
                continue;
            OtmProperty property = (OtmProperty) obj;
            assertTrue( "Must be enabled.", DeletePropertyAction.isEnabled( property ) );

            DeletePropertyAction action = new DeletePropertyAction();
            action.setSubject( property );
            action.doIt( property );
            List<OtmObject> newKids = bo.getSummary().getChildren();
            assertTrue( "Children array must be smaller.", bo.getSummary().getChildren().size() < initialChildCount );

            action.undoIt();
            assertTrue( "Children array must be initial size.",
                bo.getSummary().getChildren().size() == initialChildCount );
        }
    }

    // // TODO Re-factor this to have the action take place on a passed in type provider, then use library members and
    // // descendant
    // // type providers
    // @Test
    // public void testWhereUsed() throws ExceptionInInitializerError, InstantiationException, IllegalAccessException,
    // NoSuchMethodException, InvocationTargetException {
    //
    // // Givens - model manager with full action manager and built in types
    // DexFullActionManager fullMgr = new DexFullActionManager( null );
    // OtmModelManager mgr = TestOtmModelManager.buildModelManager( fullMgr );
    // OtmLibrary lib = TestLibrary.buildOtm( mgr );
    // assertTrue( "Given", lib.isEditable() );
    //
    // // Given one of each library member type in the library
    // TestLibraryMemberBase.buildOneOfEachWithProperties( mgr, lib );
    //
    // // Run test with simple object as type to assign
    // OtmSimpleObject simple = TestOtmSimple.buildOtm( lib );
    // runWhereUsedTest( simple );
    //
    // // Run test with core object as type to assign
    // OtmCore core = TestCore.buildOtm( lib );
    // runWhereUsedTest( core );
    // // Run test with core facets
    // runWhereUsedTest( core.getSummary() );
    // runWhereUsedTest( core.getSummaryList() );
    //
    // // FIXME - Run test with alias
    // // TLAlias tla = new TLAlias();
    // // tla.setName( "CoreAlias" );
    // // core.addAlias( tla );
    // // runWhereUsedTest( alias );
    // }
    //
    // private void runWhereUsedTest(OtmTypeProvider assignedType) {
    // // Keep action manager because it will not be accessible after type is deleted.
    // DexActionManager fullMgr = assignedType.getActionManager();
    // OtmModelManager mgr = assignedType.getModelManager();
    // // Where used and model manager only works on library members.
    // OtmLibraryMember typeOwner = assignedType.getOwningMember();
    // assertTrue( assignedType.getActionManager() instanceof DexFullActionManager );
    //
    // // Given - Assign everything to assignedType
    // List<OtmTypeUser> users = TestOtmModelManager.assignTypeToEveryUser( (OtmTypeProvider) assignedType, mgr );
    // List<OtmLibraryMember> userOwners = typeOwner.getWhereUsed();
    // assertFalse( userOwners.isEmpty() );
    // assertFalse( users.isEmpty() );
    //
    // //
    // // When - run create action to delete simple.
    // //
    // int queueSize = fullMgr.getQueueSize();
    // assignedType.getActionManager().run( DexActions.DELETELIBRARYMEMBER, typeOwner );
    // // Then
    // assertTrue( "Then - queue size increased.", ++queueSize == fullMgr.getQueueSize() );
    // assertTrue( assignedType.getLibrary() == null );
    // assertTrue( !mgr.contains( typeOwner ) );
    // assertTrue( "Can no longer get the full manager from object.", assignedType.getActionManager() != fullMgr );
    //
    // // Then - check type users and make sure the type has no library
    // for (OtmTypeUser user : users) {
    // assertTrue( user.getAssignedType() != null );
    // assertTrue( user.getAssignedType().getLibrary() == null );
    // assertTrue( user.getAssignedTLType().getOwningLibrary() == null );
    // // log.debug( "Type user " + user + " is valid? " + user.isValid() );
    // }
    //
    // // Then - make sure whereUsed is the same and type resolver does not change it
    // assertTrue( typeOwner.getWhereUsed() == userOwners );
    // TypeResolverTask.runResolver( mgr );
    // assertTrue( typeOwner.getWhereUsed() == userOwners );
    //
    // //
    // // When - undo action
    // //
    // fullMgr.undo();
    // assertTrue( "Then - queue size dicreased.", --queueSize == fullMgr.getQueueSize() );
    //
    // // Then - type resolver will force change in whereUsed list
    // assertTrue( typeOwner.getWhereUsed() == userOwners );
    // TypeResolverTask.runResolver( mgr );
    // ValidateModelManagerItemsTask.runValidator( mgr );
    // // assertFalse( typeOwner.getWhereUsed() == userOwners );
    // // 2/28/2020 - reuses the same array list when recomputed.
    // assertTrue( typeOwner.getWhereUsed() == userOwners );
    //
    // // Then - Check list contents to verify resolver restored the contents correctly
    // for (OtmLibraryMember owner : userOwners)
    // assertTrue( typeOwner.getWhereUsed().contains( owner ) );
    // for (OtmLibraryMember owner : typeOwner.getWhereUsed())
    // assertTrue( userOwners.contains( owner ) );
    //
    // // Then - check type users and make sure they are valid and the type has library
    // for (OtmTypeUser user : users) {
    // assertTrue( user.getAssignedType() == assignedType );
    // assertTrue( user.getAssignedType().getLibrary() != null );
    // assertTrue( user.getAssignedTLType().getOwningLibrary() != null );
    // }
    // log.debug( "Test delete and undelete done." );
    // }
    //
    // @Test
    // public void testWhereExtended() throws ExceptionInInitializerError, InstantiationException,
    // IllegalAccessException,
    // NoSuchMethodException, InvocationTargetException {
    // // Givens
    // DexFullActionManager fullMgr = new DexFullActionManager( null );
    // OtmModelManager mgr = TestOtmModelManager.buildModelManager( fullMgr );
    // OtmLibrary lib = TestLibrary.buildOtm( mgr );
    // assertTrue( "Given", lib.isEditable() );
    // // Given - one of each library member type in the library
    // TestLibrary.addOneOfEach( lib );
    // // Given - base objects created for all members that can have base types
    // Map<OtmLibraryMember,OtmLibraryMember> baseObjects = TestLibraryMemberBase.buildBaseObjectsForAll( mgr );
    //
    // // When - each extension is deleted, base type's where used is updated
    // for (Entry<OtmLibraryMember,OtmLibraryMember> entry : baseObjects.entrySet()) {
    // entry.getValue().getActionManager().run( DexActions.DELETELIBRARYMEMBER, entry.getValue() );
    // // Deleting only removes it from library
    // assertTrue( "Must still have extension in list",
    // entry.getKey().getWhereUsed().contains( entry.getValue() ) );
    // assertTrue( "Deleted object must have null library.", entry.getValue().getLibrary() == null );
    // }
    // TestLibraryMemberBase.confirmMap( baseObjects );
    //
    // // Run resolver then confirm
    // TypeResolverTask.runResolver( mgr );
    // // LINK should be broken! Type resolver must not have find the sub-type
    // // confirmMap( baseObjects );
    //
    // // Run undelete and confirm
    // while (fullMgr.getQueueSize() > 0)
    // fullMgr.undo();
    // TypeResolverTask.runResolver( mgr );
    //
    // TestLibraryMemberBase.confirmMap( baseObjects );
    // }
    //
    // @Test
    // public void testMultipleMembers() {
    // // DexFullActionManager fullMgr = new DexFullActionManager( null );
    // // OtmModelManager mgr = new OtmModelManager( fullMgr, null );
    // // OtmLibrary lib = mgr.add( new TLLibrary() );
    // // DexActionManager actionManager = lib.getActionManager();
    // // assertTrue( "Given", actionManager instanceof DexFullActionManager );
    // // assertTrue( actionManager.getQueueSize() == 0 );
    // // OtmBusinessObject testBO = TestBusiness.buildOtm( mgr, "TestBusinessObject" );
    // // assertTrue( "Given", testBO.getModelManager() == mgr );
    // // lib.add( testBO );
    // // assertTrue( "Given", testBO.getLibrary() == lib );
    // //
    // // Object result = null;
    // // OtmLibraryMember member = null;
    // // for (LibraryMemberType type : LibraryMemberType.values()) {
    // // // FIXED - DexActions no longer only uses subject's AM, not the caller
    // // result = actionManager.run( DexActions.NEWLIBRARYMEMBER, testBO, type );
    // // assertTrue( "Must create library member.", result instanceof OtmLibraryMember );
    // // member = (OtmLibraryMember) result;
    // // assertTrue( "Must only have one action in queue.", actionManager.getQueueSize() == 1 );
    // // assertTrue( "Model manager must contain new member.", mgr.contains( member ) );
    // //
    // // // When - undo
    // // actionManager.undo();
    // // assertFalse( "Model manager must not contain new member.", mgr.contains( member ) );
    // // assertTrue( "Action queue must be empty.", actionManager.getQueueSize() == 0 );
    // // }
    // // assertTrue( "Action queue must be empty.", actionManager.getQueueSize() == 0 );
    // // log.debug( "New Library Member Test complete." );
    // }
    //
    //
    // @Test
    // public void testEachMemberType() throws ExceptionInInitializerError, InstantiationException,
    // IllegalAccessException,
    // NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
    // // Givens
    // DexFullActionManager fullMgr = new DexFullActionManager( null );
    // OtmModelManager mgr = new OtmModelManager( fullMgr, null );
    // OtmLibrary lib = TestLibrary.buildOtm( mgr );
    // assertTrue( "Given", lib.isEditable() );
    //
    // // Given one of each library member type in the library
    // int i = 1;
    // for (OtmLibraryMemberType type : OtmLibraryMemberType.values()) {
    // OtmLibraryMember member = OtmLibraryMemberType.buildMember( type, "Object" + i++, mgr );
    // lib.add( member );
    // // mgr.add( member );
    // }
    //
    // i = fullMgr.getQueueSize();
    // Collection<OtmLibraryMember> members = new ArrayList<>( mgr.getMembers() );
    // for (OtmLibraryMember member : members) {
    // // log.debug( "Checking " + member );
    // if (member.getLibrary() == lib) {
    // assertTrue( "Given: member must be editable.", member.isEditable() );
    // // When deleted
    // member.getActionManager().run( DexActions.DELETELIBRARYMEMBER, member );
    // assertTrue( "Then - queue size increased.", ++i == fullMgr.getQueueSize() );
    // // Then
    // assertTrue( "Then - TL library must not contain member.",
    // !lib.getTL().getNamedMembers().contains( member.getTL() ) );
    // assertTrue( "Then - library is null.", member.getLibrary() == null );
    // assertTrue( "Then - model manager does not contain member.", !mgr.getMembers().contains( member ) );
    // // log.debug( "Deleted " + member );
    //
    // }
    // }
    // int mbrCount = mgr.getMembers().size();
    // while (i-- > 0) {
    // // When - undo
    // fullMgr.undo();
    // assertTrue( "Then - Model must be bigger.", ++mbrCount == mgr.getMembers().size() );
    // }
    // }
}
