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
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.TestOtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestCore;
import org.opentravel.model.otmProperties.OtmElement;
import org.opentravel.model.otmProperties.TestElement;
import org.opentravel.model.otmProperties.TestOtmTypeUserInterface;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryMember;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;

import java.io.IOException;

/**
 * Verifies the functions of the <code>new library member action</code> class.
 */
public class TestSetLibraryAction {

    private static Logger log = LogManager.getLogger( TestSetLibraryAction.class );

    private static OtmModelManager staticModelManager = null;
    static OtmLibrary lib = null;
    static OtmBusinessObject globalBO = null;

    @BeforeClass
    public static void beforeClass() throws IOException {
        lib = TestLibrary.buildOtm();
        staticModelManager = lib.getModelManager();
        // staticModelManager = new OtmModelManager( new DexFullActionManager( null ), null, null );
        // lib = staticModelManager.addOLD( new TLLibrary() );
        assertTrue( lib.isEditable() );
        assertTrue( lib.getActionManager() instanceof DexFullActionManager );

        globalBO = TestBusiness.buildOtm( lib, "GlobalBO" ); // Tested in buildOtm()
    }

    /**
     * Build the Action with subject set to the member. Check the lib2
     * 
     * @param member
     * @param targetLib
     * @return action
     * @throws Exception
     */
    public static SetLibraryAction build(OtmLibraryMember member, OtmLibrary targetLib)
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {
        // Check the libraries
        OtmLibrary sourceLib = member.getLibrary();
        assertTrue( "Builder", sourceLib.isEditable() );
        assertTrue( "Builder", targetLib.isEditable() );

        // Check the action manager
        assertTrue( "Builder", sourceLib.getActionManager() instanceof DexFullActionManager );
        assertTrue( "Builder", sourceLib.getActionManager().getQueueSize() == 0 );

        // Build the Action handler with subject set to the member
        SetLibraryAction setLibraryAction =
            (SetLibraryAction) DexActions.getAction( DexActions.SETLIBRARY, member, sourceLib.getActionManager() );
        assertTrue( "Builder", setLibraryAction != null );

        return setLibraryAction;
    }

    /**
     * Check the member is correctly in its library and <b>not</b> in the old library.
     * <ul>
     * <li>member has a new library
     * <li>new library (otm and tl) contains member
     * <li>old library (otm and tl) does not contain member
     * </ul>
     * 
     * @param member
     * @param oldLibrary can be null
     */
    public static void check(OtmLibraryMember member, OtmLibrary oldLibrary) {
        OtmLibrary newLib = member.getLibrary();
        assertTrue( "Check: Member has a new library.", newLib != null && newLib != oldLibrary );
        assertTrue( "Check: New library must contain member.", newLib.contains( member ) );
        assertTrue( "Check: New TL Library must contain named member.",
            newLib.getTL().getNamedMember( member.getName() ) != null );

        // Then - member is not in the original library
        if (oldLibrary != null) {
            assertTrue( "Check: Original library must not contain member.", !oldLibrary.contains( member ) );
            assertTrue( "Check: Original TL Library must not contain named member.",
                oldLibrary.getTL().getNamedMember( member.getName() ) == null );
        }
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
        // Givens - model manager and 2 libraries
        OtmModelManager mgr = TestOtmModelManager.build();
        OtmLibrary sourceLib = TestLibrary.buildOtm( mgr );
        OtmLibrary targetLib = TestLibrary.buildOtm( mgr );

        // Given - a member to move to new library
        OtmBusinessObject member = TestBusiness.buildOtm( sourceLib, "TestBusinessObject" );
        assertTrue( "Given", member.getModelManager() == mgr );
        assertTrue( "Given", member.getLibrary() == sourceLib );

        // Given - an action to move the member
        SetLibraryAction setLibraryAction = build( member, targetLib );
        assertTrue( "Given", setLibraryAction != null );

        // When
        OtmLibrary result = setLibraryAction.doIt( targetLib );

        // Then - member is in the new library (lib2)
        assertTrue( "Then: doIt must return the new library.", result == targetLib );
        assertTrue( "Then: member's library must be target library.", member.getLibrary() == targetLib );
        check( member, sourceLib );

        // When
        setLibraryAction.undoIt();
        assertTrue( "Then member's library must be source library.", member.getLibrary() == sourceLib );
        check( member, targetLib );
    }

    @Test
    public void testDoIt_TL() {
        OtmModelManager mgr = TestOtmModelManager.build();
        OtmLibrary sourceLib = TestLibrary.buildOtm( mgr );
        OtmLibrary targetLib = TestLibrary.buildOtm( mgr );
        // Given - provider and dependents
        OtmTypeProvider provider = TestBusiness.buildOtm( sourceLib, "SetLibMember" );
        OtmCore core = TestCore.buildOtm( sourceLib, "SetLibCore" );
        OtmElement<?> user = TestElement.buildOtm( core.getSummary(), provider );
        OtmBusinessObject extendedBO = TestBusiness.buildOtm( sourceLib, "SetLibEx" );
        extendedBO.setBaseType( provider );

        //
        TLLibrary srcTL = (TLLibrary) sourceLib.getTL();
        TLLibrary targetTL = (TLLibrary) targetLib.getTL();
        TLLibraryMember providerTL = (TLLibraryMember) provider.getTL();
        TLProperty userTL = user.getTL();
        assertTrue( "Given: base type", extendedBO.getTL().getExtension().getExtendsEntity() == providerTL );

        // When - as done in action's doIt() method
        mgr.getTlModel().moveToLibrary( providerTL, targetTL );

        // Then - library ownership
        assertTrue( "Then: ", targetTL.getNamedMembers().contains( providerTL ) );
        assertTrue( "Then: ", !srcTL.getNamedMembers().contains( providerTL ) );
        assertTrue( "Then: ", providerTL.getOwningLibrary() == targetTL );

        // Then - type assignment
        TLPropertyType type = userTL.getType();
        assertTrue( "Then: ", type == providerTL );
        assertTrue( "Then: base type", extendedBO.getTL().getExtension().getExtendsEntity() == providerTL );

        check( (OtmLibraryMember) provider, sourceLib );
        TestOtmTypeUserInterface.check( user, provider );
    }

    @Test
    public void testDoIt() throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {
        // Givens - model manager and 2 libraries
        OtmModelManager mgr = TestOtmModelManager.build();
        OtmLibrary sourceLib = TestLibrary.buildOtm( mgr );
        OtmLibrary targetLib = TestLibrary.buildOtm( mgr );

        // Given - a member with element to assign the object to be moved.
        OtmBusinessObject member = TestBusiness.buildOtm( sourceLib, "TestBusinessObject" );
        OtmElement<?> element = TestBusiness.getElement( member );

        // Given - the object to move assigned to the member's element
        OtmCore provider = TestCore.buildOtm( sourceLib, "AssignedCore" );
        element.setAssignedType( provider );
        TestOtmTypeUserInterface.check( element, provider );

        assertTrue( "Given - assignedType's library must be source library.", provider.getLibrary() == sourceLib );
        check( provider, targetLib );

        // List<OtmLibraryMember> initialFoundUsers = mgr.findUsersOf( provider );

        // Given - The action to test with subject set to assignedType object
        SetLibraryAction setLibraryAction = build( provider, targetLib );

        // When
        OtmLibrary result = setLibraryAction.doIt( targetLib );
        assertTrue( "Then: action result must be target library. ", result == targetLib );

        // Then - library
        assertTrue( "Then - assignedType's library must be target library.", provider.getLibrary() == targetLib );
        check( provider, sourceLib );

        // Then - assignment
        TestOtmTypeUserInterface.check( element, provider );


        // When
        setLibraryAction.undoIt();
        // Then
        assertTrue( "When assignedType's library is source library.", provider.getLibrary() == sourceLib );
        check( provider, targetLib );
        TestOtmTypeUserInterface.check( element, provider );
    }
}
