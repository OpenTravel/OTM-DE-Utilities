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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.dex.action.manager.DexWizardActionManager;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMemberType;
import org.opentravel.model.otmLibraryMembers.TestBusiness;

import java.io.IOException;

/**
 * Verifies the functions of the <code>new library member action</code> class.
 */
public class TestNewLibraryMemberAction {

    private static Logger log = LogManager.getLogger( TestNewLibraryMemberAction.class );

    private static OtmModelManager staticModelManager = null;
    static OtmLibrary lib = null;
    static OtmBusinessObject globalBO = null;

    @BeforeClass
    public static void beforeClass() throws IOException {
        lib = TestLibrary.buildOtm();
        staticModelManager = lib.getModelManager();

        // staticModelManager = new OtmModelManager( new DexFullActionManager( null ), null );
        // lib = staticModelManager.addOLD( new TLLibrary() );
        assertTrue( lib.isEditable() );
        assertTrue( lib.getActionManager() instanceof DexFullActionManager );

        globalBO = TestBusiness.buildOtm( lib, "GlobalBO" ); // Tested in buildOtm()
    }


    @Test
    public void testMultipleMembers() {
        // DexFullActionManager fullMgr = new DexFullActionManager( null );
        // OtmModelManager mgr = new OtmModelManager( fullMgr, null , null);
        // OtmLibrary lib = mgr.add( new TLLibrary() );

        OtmLibrary lib = TestLibrary.buildOtm();
        OtmModelManager mgr = lib.getModelManager();
        DexActionManager actionManager = lib.getActionManager();
        assertTrue( "Given", actionManager instanceof DexFullActionManager );
        assertTrue( actionManager.getQueueSize() == 0 );

        OtmBusinessObject testBO = TestBusiness.buildOtm( lib, "TestBusinessObject" );

        Object result = null;
        OtmLibraryMember member = null;
        for (OtmLibraryMemberType type : OtmLibraryMemberType.values()) {
            if (type == OtmLibraryMemberType.EXTENSIONPOINTFACET)
                continue;
            // FIXED - DexActions no longer only uses subject's AM, not the caller
            result = actionManager.run( DexActions.NEWLIBRARYMEMBER, testBO, type );
            assertTrue( "Must create library member.", result instanceof OtmLibraryMember );
            member = (OtmLibraryMember) result;
            assertTrue( "Must only have one action in queue.", actionManager.getQueueSize() == 1 );
            assertTrue( "Model manager must contain new member.", mgr.contains( member ) );

            // When - undo
            actionManager.undo();
            assertFalse( "Model manager must not contain new member.", mgr.contains( member ) );
            assertTrue( "Action queue must be empty.", actionManager.getQueueSize() == 0 );
        }
        assertTrue( "Action queue must be empty.", actionManager.getQueueSize() == 0 );
        log.debug( "New Library Member Test complete." );
    }

    @Test
    public void testUsingWizardActionManager() {
        // OtmModelManager mgr = new OtmModelManager( null, null, null );
        // OtmLibrary lib = mgr.addLibrary( new TLLibrary() );
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmModelManager mgr = lib.getModelManager();
        DexActionManager actionManager = lib.getActionManager();
        assertTrue( actionManager.getQueueSize() == 0 );
        DexActionManager wizardAM = new DexWizardActionManager( null );

        OtmBusinessObject testBO = TestBusiness.buildOtm( lib, "TestBusinessObject" );
        assertTrue( "Given", testBO.getModelManager() == mgr );
        lib.add( testBO );
        assertTrue( "Given", testBO.getLibrary() == lib );

        Object result = null;
        OtmLibraryMember member = null;
        for (OtmLibraryMemberType type : OtmLibraryMemberType.values()) {
            if (type == OtmLibraryMemberType.EXTENSIONPOINTFACET)
                continue;
            // FIXED - DexActions no longer only uses subject's AM, not the caller
            result = wizardAM.run( DexActions.NEWLIBRARYMEMBER, testBO, type );
            assertTrue( "Must create library member.", result instanceof OtmLibraryMember );
            member = (OtmLibraryMember) result;
            // assertTrue( "Must only have one action in queue.", actionManager.getQueueSize() == 1 );
            assertTrue( "Action queue must be empty.", actionManager.getQueueSize() == 0 );
            assertTrue( "Wizards queue must not be empty.", wizardAM.getQueueSize() == 1 );
            assertTrue( "Model manager must contain new member.", mgr.contains( member ) );

            // When - undo
            wizardAM.undo();
            assertFalse( "Model manager must not contain new member.", mgr.contains( member ) );
            assertTrue( "Wizards queue must be empty.", wizardAM.getQueueSize() == 0 );
            assertTrue( "Action queue must be empty.", actionManager.getQueueSize() == 0 );
        }
        assertTrue( "Action queue must be empty.", actionManager.getQueueSize() == 0 );
        log.debug( "New Library Member Test complete." );
    }

    @Test
    public void testAssignTypeAction() {}

}
