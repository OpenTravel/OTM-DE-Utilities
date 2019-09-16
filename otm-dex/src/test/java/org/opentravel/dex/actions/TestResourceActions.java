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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestResource;
import org.opentravel.schemacompiler.model.TLLibrary;

import java.lang.reflect.InvocationTargetException;

/**
 * Verifies the functions of the <code>UserSettings</code> class.
 */
public class TestResourceActions {
    private static Log log = LogFactory.getLog( TestDexActionManager.class );

    private static OtmModelManager staticModelManager = null;
    static OtmLibrary lib = null;
    static OtmBusinessObject globalBO = null;
    static OtmResource resource;
    static DexActionManager actionMgr;

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( new DexFullActionManager( null ), null );
        lib = staticModelManager.add( new TLLibrary() );
        assertTrue( lib.isEditable() );
        assertTrue( lib.getActionManager() instanceof DexFullActionManager );

        actionMgr = staticModelManager.getActionManager( true );
        assertNotNull( actionMgr );

        resource = TestResource.buildFullOtm( "TheObject", "theSubject", staticModelManager );
        lib.add( resource );
        assertNotNull( resource );
        assertTrue( resource.isEditable() );

        globalBO = (OtmBusinessObject) lib.add( TestBusiness.buildOtm( staticModelManager, "GlobalBO" ) );
        assertTrue( globalBO != null );
        assertTrue( globalBO.getLibrary() == lib );
        assertTrue( globalBO.isEditable() );
        assertTrue( staticModelManager.getMembers().contains( globalBO ) );
    }

    @Test
    public void testConstructors() throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {

        DexAction<?> action = DexActions.getAction( DexActions.ASSIGNSUBJECT, resource, resource.getActionManager() );
        assertNotNull( action );

        log.debug( "Done." );
    }

    @Test
    public void testAssignSubjectAction()
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException, NoSuchMethodException,
        SecurityException, IllegalArgumentException, InvocationTargetException {

        OtmBusinessObject otherBO = TestBusiness.buildOtm( staticModelManager, "OtherBO" );
        OtmBusinessObject originalSubject = resource.getSubject();

        // When - action is done with subject
        actionMgr.run( DexActions.ASSIGNSUBJECT, resource, otherBO );
        assertTrue( resource.getSubject() == otherBO );

        actionMgr.undo();
        assertTrue( resource.getSubject() == originalSubject );

        log.debug( "Set Subject Test complete." );
    }
}
