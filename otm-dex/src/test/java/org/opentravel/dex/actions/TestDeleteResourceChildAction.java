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
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.dex.actions.resource.DeleteResourceChildAction;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestResource;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Verifies the functions of the <code>delete resource child action</code> class.
 */
public class TestDeleteResourceChildAction {

    private static Logger log = LogManager.getLogger( TestDeleteResourceChildAction.class );

    private static OtmModelManager staticModelManager = null;
    static OtmLibrary lib = null;
    static OtmBusinessObject globalBO = null;
    private static OtmResource resource = null;

    @BeforeClass
    public static void beforeClass() throws IOException {
        lib = TestLibrary.buildOtm();
        staticModelManager = lib.getModelManager();
        assertTrue( lib.isEditable() );
        assertTrue( lib.getActionManager() instanceof DexFullActionManager );

        globalBO = TestBusiness.buildOtm( lib, "GlobalBO" ); // Tested in buildOtm()
    }


    @Test
    public void testDoAndUndoOnParamGroupChildren() {
        // TODO
    }

    @Test
    public void testDoAndUndoOnParentRefChildren() {
        // TODO
    }

    @Test
    public void testDoAndUndoOnActionChildren() {
        // TODO
    }

    @Test
    public void testDoAndUndoOnResourceChildren() {
        // Givens
        OtmLibrary lib = TestLibrary.buildOtm( new OtmModelManager( new DexFullActionManager( null ), null, null ) );
        OtmModelManager mgr = lib.getModelManager();
        DexActionManager actionManager = lib.getActionManager();
        assertTrue( "Given", actionManager instanceof DexFullActionManager );
        assertTrue( "Given", actionManager.getQueueSize() == 0 );

        //
        resource = TestResource.buildFullOtm( "http://example.com/TestResource", "TestResource", lib, mgr );
        assertTrue( "Given", resource != null );
        assertTrue( "Given", resource.isEditable() );
        assertTrue( "Given", !resource.getParameterGroups().isEmpty() );
        assertTrue( "Given", !resource.getChildren().isEmpty() );

        List<OtmObject> kids = new ArrayList<>( resource.getChildren() );
        for (OtmObject child : kids) {
            log.debug( "Deleting " + child );
            assertTrue( "Given", child instanceof OtmResourceChild );

            // When deleted
            Object result = actionManager.run( DexActions.DELETERESOURCECHILD, child );
            assertTrue( result == resource );
            assertTrue( "Deleted child must not be in resource.", !resource.getChildren().contains( child ) );
            // Then - queue must contain the action
            DexAction<?> lastAction = actionManager.getLastAction();
            assertTrue( "Action must be in queue.", actionManager.getQueueSize() == 1 );
            assertTrue( "Action must be in queue.", lastAction instanceof DeleteResourceChildAction );
            checkOwner( (OtmResourceChild) child, null );

            // When un-done
            actionManager.undo();
            assertTrue( "Undo must add child to resource.", resource.getChildren().contains( child ) );
            assertTrue( ((OtmResourceChild) child).getParent() == resource );
            checkOwner( (OtmResourceChild) child, resource.getTL() );
        }
    }

    public void checkOwner(OtmResourceChild child, TLResource tlOwner) {
        if (child.getTL() instanceof TLParamGroup) {
            assertTrue( "child TL must have owner", ((TLParamGroup) child.getTL()).getOwner() == tlOwner );
            if (tlOwner != null)
                assertTrue( "Owner must have child.", tlOwner.getParamGroups().contains( child.getTL() ) );
        } else if (child.getTL() instanceof TLActionFacet) {
            assertTrue( "child TL must have owner", ((TLActionFacet) child.getTL()).getOwningResource() == tlOwner );
            if (tlOwner != null)
                assertTrue( "Owner must have child.", tlOwner.getActionFacets().contains( child.getTL() ) );
        } else if (child.getTL() instanceof TLAction) {
            assertTrue( "child TL must have owner", ((TLAction) child.getTL()).getOwner() == tlOwner );
            if (tlOwner != null)
                assertTrue( "Owner must have child.", tlOwner.getActions().contains( child.getTL() ) );
        }
    }
}
