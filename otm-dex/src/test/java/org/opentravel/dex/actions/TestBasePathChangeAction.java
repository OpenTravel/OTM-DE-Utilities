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
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestResource;
import org.opentravel.schemacompiler.model.TLLibrary;

import java.io.IOException;

/**
 * Verifies the functions of the <code>base path change action</code> class.
 */
public class TestBasePathChangeAction {

    private static Log log = LogFactory.getLog( TestBasePathChangeAction.class );

    private static OtmModelManager staticModelManager = null;
    static OtmLibrary lib = null;
    static OtmBusinessObject globalBO = null;
    // private static OtmResource resource = null;

    @BeforeClass
    public static void beforeClass() throws IOException {
        staticModelManager = new OtmModelManager( new DexFullActionManager( null ), null, null );
        lib = staticModelManager.add( new TLLibrary() );
        assertTrue( lib.isEditable() );
        assertTrue( lib.getActionManager() instanceof DexFullActionManager );

        globalBO = TestBusiness.buildOtm( lib, "GlobalBO" );
        // Tested in buildOtm()
    }

    private static final String INITIALBASEPATH = "/";
    private static final String BASEPATH1 = "/MyRecordCollection";
    private static final String BASEPATH2 = "/Sams/Records";


    @Test
    public void testDoAndUndo() {
        // Givens
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmModelManager modelMgr = lib.getModelManager();
        DexActionManager fullActionMgr = lib.getActionManager();
        // Given - no actions in the queue
        assertTrue( "Given", fullActionMgr.getQueueSize() == 0 );

        // Given - resource is editable
        OtmResource r = TestResource.buildOtm( modelMgr );
        lib.add( r );
        assertTrue( "Given", r.getLibrary() == lib );
        assertTrue( "Given", r.getActionManager() != null );
        assertTrue( "Given", r.isEditable() );
        assertTrue( "Given", r.getActionManager().isEnabled( DexActions.BASEPATHCHANGE, r ) );

        // Given - an initial base path
        r.setBasePath( INITIALBASEPATH );

        // When - action invoked by changing the observable property
        r.basePathProperty().set( BASEPATH1 );
        assertTrue( r.getBasePath().equals( BASEPATH1 ) );

        // When - action invoked by changing the observable property
        r.basePathProperty().set( BASEPATH2 );
        assertTrue( r.getBasePath().equals( BASEPATH2 ) );

        // Then - queue must have both actions
        assertTrue( "Two actions must be in queue.", fullActionMgr.getQueueSize() == 2 );

        // When - action is undone
        fullActionMgr.undo();
        assertTrue( r.getBasePath().equals( BASEPATH1 ) );
        fullActionMgr.undo();
        assertTrue( r.getBasePath().equals( INITIALBASEPATH ) );
        assertTrue( fullActionMgr.getQueueSize() == 0 );

        // Side-effect? changes to action requests?
    }
}

