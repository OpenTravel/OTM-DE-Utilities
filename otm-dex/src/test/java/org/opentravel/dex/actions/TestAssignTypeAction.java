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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmEnumeration;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMemberType;
import org.opentravel.model.otmLibraryMembers.OtmSimpleObject;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;
import org.opentravel.model.otmLibraryMembers.OtmXsdSimple;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestCore;
import org.opentravel.model.otmLibraryMembers.TestEnumerationClosed;
import org.opentravel.model.otmLibraryMembers.TestOtmSimple;
import org.opentravel.model.otmLibraryMembers.TestValueWithAttributes;
import org.opentravel.model.otmLibraryMembers.TestXsdSimple;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.OtmPropertyType;
import org.opentravel.schemacompiler.model.TLLibrary;

import java.lang.reflect.InvocationTargetException;

/**
 * Verifies the functions of the <code>UserSettings</code> class.
 */
public class TestAssignTypeAction {

    private static Log log = LogFactory.getLog( TestAssignTypeAction.class );

    private static OtmModelManager staticModelManager = null;
    static OtmLibrary lib = null;
    static OtmBusinessObject globalBO = null;

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( new DexFullActionManager( null ), null, null );
        lib = staticModelManager.add( new TLLibrary() );
        assertTrue( lib.isEditable() );
        assertTrue( lib.getActionManager() instanceof DexFullActionManager );

        globalBO = TestBusiness.buildOtm( lib, "GlobalBO" );

        // Tested in buildOtm()
        // assertTrue( globalBO != null );
        // assertTrue( globalBO.getLibrary() == lib );
        // assertTrue( globalBO.isEditable() );
        // assertTrue( globalBO.getActionManager() == lib.getActionManager() );
        // assertTrue( staticModelManager.getMembers().contains( globalBO ) );
    }

    @Test
    public void testMultipleAssignments() {
        // Given - action manager with an empty queue
        DexFullActionManager am = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( am, null, null );
        lib = mgr.add( new TLLibrary() );
        assertTrue( am.getQueueSize() == 0 );

        // Given - a type user
        OtmBusinessObject thisBO = TestBusiness.buildOtm( lib, "ThisBO" );
        OtmTypeUser child = (OtmTypeUser) thisBO.getSummary().getChildren().get( 0 );
        assertNotNull( child );

        // Given assertions: Actions get their action managers from the otmObject
        assertTrue( thisBO.getLibrary() == lib );
        assertTrue( lib.getActionManager() == am );
        assertTrue( thisBO.getActionManager() == am );
        assertTrue( child.getOwningMember().getActionManager() == am );
        // OtmTypeProvider startingType = child.getAssignedType();

        // Given - types to assign
        OtmXsdSimple type1 = TestXsdSimple.buildOtm( staticModelManager );
        OtmXsdSimple type2 = TestXsdSimple.buildOtm( staticModelManager );
        OtmXsdSimple type3 = TestXsdSimple.buildOtm( staticModelManager );
        OtmXsdSimple type4 = TestXsdSimple.buildOtm( staticModelManager );

        log.debug( "Starting type assignment action tests.\n" );
        am.run( DexActions.TYPECHANGE, child, type1 );
        assertTrue( child.getAssignedType() == type1 );
        assertTrue( am.getQueueSize() == 1 );

        am.run( DexActions.TYPECHANGE, child, type2 );
        assertTrue( child.getAssignedType() == type2 );
        assertTrue( am.getQueueSize() == 2 );

        am.run( DexActions.TYPECHANGE, child, type3 );
        assertTrue( child.getAssignedType() == type3 );
        assertTrue( am.getQueueSize() == 3 );

        am.run( DexActions.TYPECHANGE, child, type4 );
        assertTrue( child.getAssignedType() == type4 );
        assertTrue( am.getQueueSize() == 4 );

        am.undo();
        assertTrue( child.getAssignedType() == type3 );
        assertTrue( am.getQueueSize() == 3 );

        am.undo();
        assertTrue( child.getAssignedType() == type2 );
        assertTrue( am.getQueueSize() == 2 );

        am.undo();
        assertTrue( child.getAssignedType() == type1 );
        assertTrue( am.getQueueSize() == 1 );
    }

    @Test
    public void testAssignToNewProperty() {
        DexFullActionManager am = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( am, null, null );
        lib = TestLibrary.buildOtm( mgr );
        OtmCore core = TestCore.buildOtm( lib, "TestCore" );
        OtmFacet<?> facet = core.getSummary();

        // Given - objects to assign
        OtmEnumeration<?> closedEnum = TestEnumerationClosed.buildOtm( lib );
        OtmSimpleObject simple = TestOtmSimple.buildOtm( lib );

        // Given - a newly created Attribute property
        Object result = am.run( DexActions.ADDPROPERTY, facet, OtmPropertyType.ATTRIBUTE );
        assertTrue( "Given - must be a property.", result instanceof OtmProperty );
        assertTrue( "Given - must be a type user.", result instanceof OtmTypeUser );
        OtmTypeUser property = (OtmTypeUser) result;
        assertTrue( "Given - the property has no assigned type.", property.getAssignedTLType() == null );
        assertTrue( "Given - the property has no assigned type.", property.getAssignedType() == null );

        assertTrue( "The action must be enabled.", SetAssignedTypeAction.isEnabled( property ) );
        assertTrue( "The action must be enabled.", SetAssignedTypeAction.isEnabled( property, closedEnum ) );

        // When - type is changed
        am.run( DexActions.TYPECHANGE, property, closedEnum );
        assertTrue( "Then - the property has assigned type.", property.getAssignedType() == closedEnum );
        assertTrue( "Then - vwa has property in where used list.", closedEnum.getWhereUsed().contains( core ) );
        // When - undone
        am.undo();
        assertTrue( "Then - the property has no assigned type.", property.getAssignedType() == null );
        assertTrue( "Then - enum does not have property in where used list.",
            !closedEnum.getWhereUsed().contains( core ) );

        // Given - a newly created Element property
        result = am.run( DexActions.ADDPROPERTY, facet, OtmPropertyType.ELEMENT );
        property = (OtmTypeUser) result;
        am.run( DexActions.TYPECHANGE, property, closedEnum );
        assertTrue( "Then - the property has assigned type.", property.getAssignedType() == closedEnum );
        assertTrue( "Then - vwa has property in where used list.", closedEnum.getWhereUsed().contains( core ) );
        // When - undone
        am.undo();
        assertTrue( "Then - the property has no assigned type.", property.getAssignedType() == null );
        assertTrue( "Then - enum does not have property in where used list.",
            !closedEnum.getWhereUsed().contains( core ) );


        // Given - a newly created VWA
        result = am.run( DexActions.NEWLIBRARYMEMBER, core, OtmLibraryMemberType.VWA );
        assertTrue( "Given - must have created a vwa", result instanceof OtmValueWithAttributes );
        OtmValueWithAttributes vwa = (OtmValueWithAttributes) result;
        lib.add( vwa );
        mgr.add( vwa );
        OtmTypeProvider initialProvider = vwa.getAssignedType();
        assertTrue( "Given - no initial assigned type.", initialProvider == null );

        am.run( DexActions.TYPECHANGE, vwa, simple );
        assertTrue( "Then - the vwa has assigned type.", vwa.getAssignedType() == simple );
        assertTrue( "Then - simple has vwa in where used list.", simple.getWhereUsed().contains( vwa ) );
        // When - undone
        am.undo();
        assertTrue( "Then - the vwa has no assigned type.", vwa.getAssignedType() == null );
        assertTrue( "Then - simple does not have vwa in where used list.", !closedEnum.getWhereUsed().contains( vwa ) );
    }

    @Test
    public void testAssignTypeAction()
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException, NoSuchMethodException,
        SecurityException, IllegalArgumentException, InvocationTargetException {
        DexFullActionManager am = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( am, null );
        assertTrue( am.getQueueSize() == 0 );
        lib = TestLibrary.buildOtm( mgr );

        // Given a OtmObjects
        OtmEnumeration<?> closedEnum = TestEnumerationClosed.buildOtm( mgr );
        lib.add( closedEnum );
        OtmValueWithAttributes vwa = TestValueWithAttributes.buildOtm( mgr );
        lib.add( vwa );
        assertFalse( "Must not have type.", vwa.getAssignedType() == closedEnum );
        OtmTypeProvider originalType = vwa.getAssignedType();
        assertTrue( vwa.getActionManager() instanceof DexFullActionManager );

        // As run from the GUI, the run will launch a dialog
        // user.getActionManager().run( DexActions.TYPECHANGE, user );
        // The action uses doIt(otmTypeProvider);
        // Action manager uses actionHandler = DexActions.getAction( action, subject );

        // Given - an action for type change on VWA
        DexRunAction action = (DexRunAction) DexActions.getAction( DexActions.TYPECHANGE, vwa, vwa.getActionManager() );
        assertNotNull( action );

        // When - executed using action's method
        // action.doIt( closedEnum );
        am.run( DexActions.TYPECHANGE, vwa, closedEnum );
        // Then
        assertTrue( "Must have type.", vwa.getAssignedType() == closedEnum );
        assertTrue( !am.getLastActionName().isEmpty() );
        assertTrue( am.getQueueSize() > 0 );

        // When - undo the change
        am.undo();
        // Then
        assertTrue( "Undo must restore type.", vwa.getAssignedType() == originalType );

        log.debug( "Assigned Type Change Test complete." );
    }

}
