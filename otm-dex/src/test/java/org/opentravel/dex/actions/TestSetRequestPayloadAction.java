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
import org.opentravel.dex.actions.resource.SetRequestPayloadAction;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestResource;
import org.opentravel.model.resource.OtmAction;
import org.opentravel.model.resource.OtmActionRequest;
import org.opentravel.schemacompiler.model.TLLibrary;

import java.io.IOException;
import java.lang.reflect.Constructor;

/**
 * Verifies the functions of the <code>delete alias action</code> class.
 */
public class TestSetRequestPayloadAction {

    private static Log log = LogFactory.getLog( TestSetRequestPayloadAction.class );

    private static OtmModelManager staticModelManager = null;
    static OtmLibrary lib = null;
    static OtmBusinessObject globalBO = null;

    @BeforeClass
    public static void beforeClass() throws IOException {
        staticModelManager = new OtmModelManager( new DexFullActionManager( null ), null, null );
        lib = staticModelManager.add( new TLLibrary() );
        assertTrue( lib.isEditable() );
        assertTrue( lib.getActionManager() instanceof DexFullActionManager );

        globalBO = TestBusiness.buildOtm( lib, "GlobalBO" ); // Tested in buildOtm()
    }

    @Test
    public void testGettingAction() {
        staticModelManager.clear();
        OtmLibrary lib1 = TestLibrary.buildOtm();
        OtmResource r = TestResource.buildOtm( lib1, "TheResource" );
        OtmAction action = r.getActions().get( 0 );
        assertTrue( action != null );

        OtmActionRequest aReq = action.getRequest();
        assertTrue( getAction( aReq ) instanceof SetRequestPayloadAction );
    }


    /**
     * Get the delete alias action using the member's action manager.
     * 
     * @param member
     * @return
     */
    // TODO - move this to TestDexActions
    public static DexAction<?> getAction(OtmActionRequest aReq) {
        assertTrue( "Given: ", aReq.getActionManager() instanceof DexFullActionManager );
        DexActionManager actionManager = aReq.getActionManager();


        Constructor<?> constructor = null;
        try {
            constructor = DexActions.getConstructor( DexActions.SETREQUESTPAYLOAD );
        } catch (NoSuchMethodException | SecurityException e1) {
            e1.printStackTrace();
            log.debug( "Error getting action. " + e1.getMessage() );
        }
        assertTrue( constructor != null );

        DexAction<?> action = null;
        try {
            action = DexActions.getAction( DexActions.SETREQUESTPAYLOAD, aReq, actionManager );
        } catch (ExceptionInInitializerError | InstantiationException | IllegalAccessException e) {
            log.debug( "Error getting action. " + e.getMessage() );
        }
        return action;
    }

    @Test
    public void testIsEnabled() {
        OtmResource r = TestResource.buildOtm( lib, "TheResource" );
        OtmAction action = r.getActions().get( 0 );
        assertTrue( action != null );

        OtmActionRequest aReq = action.getRequest();
        assertTrue( isEnabled( aReq ) );
    }

    public static boolean isEnabled(OtmActionRequest aReq) {
        assertTrue( "Given: ", aReq.getActionManager() instanceof DexFullActionManager );
        DexActionManager actionManager = aReq.getActionManager();
        boolean result = actionManager.isEnabled( DexActions.SETREQUESTPAYLOAD, aReq, null );
        return result;
    }

    @Test
    public void testSet() {
        // TODO
    }
}
