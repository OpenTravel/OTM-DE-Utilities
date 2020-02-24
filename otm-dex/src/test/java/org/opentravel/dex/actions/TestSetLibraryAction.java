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
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestCore;
import org.opentravel.model.otmProperties.OtmElement;
import org.opentravel.schemacompiler.model.TLLibrary;

import java.io.IOException;

/**
 * Verifies the functions of the <code>new library member action</code> class.
 */
public class TestSetLibraryAction {

    private static Log log = LogFactory.getLog( TestSetLibraryAction.class );

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
    public void testSimpleChangeLibrary()
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, null );
        OtmLibrary lib1 = mgr.add( new TLLibrary() );
        assertTrue( "Given", lib1.isEditable() );
        OtmLibrary lib2 = mgr.add( new TLLibrary() );
        assertTrue( "Given", lib2.isEditable() );
        DexActionManager actionManager = lib1.getActionManager();
        assertTrue( "Given", actionManager instanceof DexFullActionManager );
        assertTrue( actionManager.getQueueSize() == 0 );

        OtmBusinessObject member = TestBusiness.buildOtm( mgr, "TestBusinessObject" );
        assertTrue( "Given", member.getModelManager() == mgr );
        lib1.add( member );
        assertTrue( "Given", member.getLibrary() == lib1 );

        SetLibraryAction setLibraryHandler =
            (SetLibraryAction) DexActions.getAction( DexActions.SETLIBRARY, member, actionManager );
        assertTrue( "Given", setLibraryHandler != null );

        // When
        setLibraryHandler.doIt( lib2 );
        assertTrue( "Then member's library is library 2.", member.getLibrary() == lib2 );

        // When
        setLibraryHandler.undoIt();
        assertTrue( "Then member's library must be library 1.", member.getLibrary() == lib1 );
    }

    @Test
    public void testSetWhenAssigned()
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, null );
        OtmLibrary lib1 = TestLibrary.buildOtm( mgr, "Namespace1", "p1", "Library1" );
        log.debug( "Lib 1 name is: " + lib1.getFullName() );
        OtmLibrary lib2 = TestLibrary.buildOtm( mgr, "Namespace2", "p2", "Library2" );
        DexActionManager actionManager = lib1.getActionManager();

        // Create business object and vwa. Use VWA as type on an element
        OtmBusinessObject member = TestBusiness.buildOtm( mgr, "TestBusinessObject" );
        lib1.add( member );
        assertTrue( "Given", member.getLibrary() == lib1 );
        OtmElement<?> element = TestBusiness.getElement( member );
        assertTrue( "Given", element != null );
        assertTrue( "Given", element.getOwningMember() == member );
        assertTrue( "Given", element.getLibrary() == lib1 );
        OtmCore assignedType = TestCore.buildOtm( mgr, "TestVWA" );
        lib1.add( assignedType );
        element.setAssignedType( assignedType );
        assertTrue( "Given", element.getTL().getType() == assignedType.getTL() );
        assertTrue( "Given", element.getAssignedType() == assignedType );
        assertTrue( "Given - property set.", element.assignedTypeProperty().get().equals( assignedType.getName() ) );
        log.debug(
            element.getAssignedType().getNameWithPrefix() + "  property = " + element.assignedTypeProperty().get() );

        // The action to test
        SetLibraryAction setLibraryHandler =
            (SetLibraryAction) DexActions.getAction( DexActions.SETLIBRARY, assignedType, actionManager );
        assertTrue( "Given", setLibraryHandler != null );

        // When
        setLibraryHandler.doIt( lib2 );
        assertTrue( "When assignedType's library is library 2.", assignedType.getLibrary() == lib2 );
        assertTrue( "When assignedType;s TLlib is tlLib 2.", assignedType.getTL().getOwningLibrary() == lib2.getTL() );
        assertTrue( "When assignedType;s TLlib is tlLib 2.", assignedType.getLibrary().getTL() == lib2.getTL() );

        assertTrue( "Then - TL type must still be assignedType's TL.",
            element.getTL().getType() == assignedType.getTL() );
        assertTrue( "Then - element must still be assigned to type.", element.getAssignedType() == assignedType );
        assertTrue( "Then - assigned type still has element's member in where used.",
            assignedType.getWhereUsed().contains( member ) );
        log.debug(
            element.getAssignedType().getNameWithPrefix() + "  property = " + element.assignedTypeProperty().get() );
        assertTrue( "Then - property p2 prefix.",
            element.assignedTypeProperty().get().contains( assignedType.getPrefix() ) );

        // When
        setLibraryHandler.undoIt();
        assertTrue( "When assignedType's library is library 1.", assignedType.getLibrary() == lib1 );
        assertTrue( "When assignedType;s TLlib is tlLib 1.", assignedType.getTL().getOwningLibrary() == lib1.getTL() );
        assertTrue( "When assignedType;s TLlib is tlLib 1.", assignedType.getLibrary().getTL() == lib1.getTL() );
        assertTrue( "Then - TL type must still be assignedType's TL.",
            element.getTL().getType() == assignedType.getTL() );
        assertTrue( "Then - element must still be assigned to type.", element.getAssignedType() == assignedType );
        assertTrue( "Then - assigned type still has element's member in where used.",
            assignedType.getWhereUsed().contains( member ) );
        assertTrue( "Then - property has name.",
            element.assignedTypeProperty().get().equals( assignedType.getName() ) );
    }
}
