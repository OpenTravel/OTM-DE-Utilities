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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.dex.actions.resource.AssignResourceSubjectAction;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestResource;
import org.opentravel.model.resource.OtmParameterGroup;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Verifies the functions of the <code>UserSettings</code> class.
 */
public class TestAssignResourceSubjectAction {
    private static Logger log = LogManager.getLogger( TestDexActionManager.class );

    private static OtmModelManager staticModelManager = null;
    static OtmLibrary lib = null;
    static OtmBusinessObject globalBO = null;
    static OtmResource resource;
    static DexActionManager actionMgr;

    @BeforeClass
    public static void beforeClass() {
        lib = TestLibrary.buildOtm();
        staticModelManager = lib.getModelManager();
        actionMgr = lib.getActionManager();
        assertTrue( lib.isEditable() );
        assertTrue( actionMgr instanceof DexFullActionManager );
        assertNotNull( staticModelManager );

        resource = TestResource.buildFullOtm( "TheObject", "theSubject", staticModelManager );
        lib.add( resource );
        assertNotNull( resource );
        assertTrue( resource.isEditable() );

        globalBO = TestBusiness.buildOtm( lib, "GlobalBO" ); // Tested in buildOtm()
    }

    @Test
    public void testGetAction() throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {

        DexAction<?> action = DexActions.getAction( DexActions.ASSIGNSUBJECT, resource, resource.getActionManager() );
        assertNotNull( action );
        assertTrue( action instanceof AssignResourceSubjectAction );

        log.debug( "Done." );
    }

    @Test
    public void testDoAndUndo() throws ExceptionInInitializerError, InstantiationException, IllegalAccessException,
        NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {

        OtmBusinessObject otherBO = TestBusiness.buildOtm( lib, "OtherBO" );
        OtmBusinessObject originalSubject = resource.getSubject();
        // Given - the original set of parameter groups
        List<OtmParameterGroup> groups = resource.getParameterGroups();
        assertTrue( "Given", !groups.isEmpty() );
        assertTrue( "Action must be enabled.", AssignResourceSubjectAction.isEnabled( resource ) );
        assertTrue( "Action must be enabled.", AssignResourceSubjectAction.isEnabled( resource, otherBO ) );

        // When - action is done with subject.
        actionMgr.run( DexActions.ASSIGNSUBJECT, resource, otherBO );
        assertTrue( resource.getSubject() == otherBO );
        // Action used to remove parameter groups as well.
        // for (OtmObject child : resource.getChildren())
        // assertTrue( "Must not have parameter group child.", !(child instanceof OtmParameterGroup) );
        // assertTrue( "Must not have parameter groups.", resource.getParameterGroups().isEmpty() );
        // assertTrue( "Must not have TL parameter groups.", resource.getTL().getParamGroups().isEmpty() );

        actionMgr.undo();
        assertTrue( resource.getSubject() == originalSubject );
        assertTrue( "Must have parameter groups.", resource.getParameterGroups().size() == groups.size() );

        log.debug( "Set Subject Test complete." );
    }
}
