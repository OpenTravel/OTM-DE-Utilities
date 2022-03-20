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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.dex.action.manager.DexReadOnlyActionManager;
import org.opentravel.dex.events.DexChangeEvent;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmEnumeration;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestEnumerationClosed;
import org.opentravel.model.otmLibraryMembers.TestResource;
import org.opentravel.model.otmLibraryMembers.TestValueWithAttributes;

import java.lang.reflect.InvocationTargetException;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Verifies the functions of the <code>UserSettings</code> class.
 */
public class TestDexActionManager {

    private static Logger log = LogManager.getLogger( TestDexActionManager.class );

    private static OtmModelManager staticModelManager = null;
    static OtmLibrary lib = null;
    static OtmBusinessObject globalBO = null;

    @BeforeClass
    public static void beforeClass() {
        lib = TestLibrary.buildOtm();
        staticModelManager = lib.getModelManager();

        assertTrue( lib.isEditable() );
        assertTrue( lib.getActionManager() instanceof DexFullActionManager );

        globalBO = TestBusiness.buildOtm( lib, "GlobalBO" ); // Tested in buildOtm()
    }

    @Test
    public void testConstructors() {
        DexActionManager am = new DexReadOnlyActionManager();
        assertNotNull( am );
        am = new DexFullActionManager( null );
        assertNotNull( am );

        log.debug( "Done." );
    }

    @Test
    public void testDexActions() throws ExceptionInInitializerError, InstantiationException, IllegalAccessException,
        NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        DexFullActionManager mgr = new DexFullActionManager( null );
        OtmResource resource = TestResource.buildOtm( staticModelManager );
        lib.add( resource );
        assertTrue( resource != null );
        assertTrue( resource.isEditable() );

        // Test getting events and actions from DexActions enum class
        for (DexActions a : DexActions.values()) {
            log.debug( "Testing: " + a.toString() );

            // Test getting event objects
            Class<?> eventClass = a.eventClass();
            DexChangeEvent event = DexActions.getEvent( a );
            if (eventClass != null) {
                assertTrue( event != null );
                assertTrue( event.getEventType() != null );
            }

            // Test getting action objects
            Class<?> actionClass = a.actionClass();
            assertTrue( actionClass != null );
            DexAction<?> action;
            try {
                action = DexActions.getAction( a, resource, resource.getActionManager() );
                // Some of these will fail because the subject is the wrong class
                log.debug( "Got action " + action + " for action type " + a );
                // Test isEnabled - must be true if there is an action
                if (action != null)
                    assertTrue( mgr.isEnabled( a, resource ) );
                else
                    assertFalse( mgr.isEnabled( a, resource ) );
            } catch (Exception e) {
                e.printStackTrace();
                log.error( "Failed to get action handler. " + e.getLocalizedMessage() );
            }
        }

        // Make sure setting listener works
        StringProperty stringProperty = new SimpleStringProperty( "Hi" );
        DexAction<?> action = mgr.setListener( stringProperty, DexActions.BASEPATHCHANGE, resource );
        assertTrue( action != null );

        // Make sure mgr.add property works
        assertTrue( mgr.getQueueSize() == 0 );
        stringProperty = mgr.add( DexActions.BASEPATHCHANGE, "SomePath", resource );
        log.debug( "should have property with listener now." );
        stringProperty.set( "New value = should trigger action." );
        assertTrue( mgr.getQueueSize() > 0 );
    }

    private static final String CHANGED_NAME1 = "VwaWithNewName1";
    private static final String CHANGED_NAME2 = "VwaWithNewName2";

    @Test
    public void testPushAction() {
        lib = TestLibrary.buildOtm();
        DexActionManager am = lib.getActionManager();
        assertTrue( am.getQueueSize() == 0 );

        // Given an OtmObject with action added
        OtmValueWithAttributes vwa = TestValueWithAttributes.buildOtm( lib, "TestPushVWA" );
        // lib.add( vwa );
        SimpleStringProperty nameProperty = (SimpleStringProperty) am.add( DexActions.NAMECHANGE, vwa.getName(), vwa );
        assertTrue( !(nameProperty instanceof ReadOnlyStringWrapper) );

        // When - action is run by changing string property
        nameProperty.set( CHANGED_NAME1 );

        // Queue must be larger
        assertTrue( am.getQueueSize() == 1 );

        // When - action is run by changing string property
        nameProperty.set( CHANGED_NAME2 );

        // Then - Queue must be larger
        // If not, action may have been duplicate and not added
        assertTrue( am.getQueueSize() == 2 );

        log.debug( "Push Test complete." );
    }

    private final static String NEW_NAME = "MyNewName";

    @Test
    public void testNameChangeAction() {
        lib = TestLibrary.buildOtm();
        OtmModelManager mgr = lib.getModelManager();
        DexFullActionManager am = (DexFullActionManager) mgr.getActionManager( true );
        assertTrue( am.getQueueSize() == 0 );

        // Given a OtmObject
        OtmEnumeration<?> closedEnum = TestEnumerationClosed.buildOtm( mgr );
        lib.add( closedEnum );
        String originalName = closedEnum.nameProperty().get();
        // Givens
        assertFalse( originalName.isEmpty() );
        assertTrue( closedEnum.getActionManager() == am );
        assertTrue( closedEnum.isEditable() );
        assertTrue( am.isEnabled( DexActions.NAMECHANGE, closedEnum ) );
        assertFalse( closedEnum.nameProperty() instanceof ReadOnlyStringWrapper );

        // When - the property changes
        closedEnum.nameProperty().set( NEW_NAME );

        // Then
        assertTrue( closedEnum.getName().equals( NEW_NAME ) );
        // Then
        assertTrue( am.getQueueSize() == 1 );

        // When - undo
        am.undo();
        // Then
        assertTrue( closedEnum.getName().equals( originalName ) );
        assertTrue( am.getQueueSize() == 0 );
    }

    @Test
    public void testDescriptionChangeAction() {
        lib = TestLibrary.buildOtm();
        OtmModelManager mgr = lib.getModelManager();
        DexFullActionManager am = (DexFullActionManager) mgr.getActionManager( true );
        assertTrue( am.getQueueSize() == 0 );

        // Given a OtmObject
        OtmEnumeration<?> closedEnum = TestEnumerationClosed.buildOtm( mgr );
        lib.add( closedEnum );
        String originalDescription = closedEnum.nameProperty().get();
        // Givens
        assertFalse( originalDescription.isEmpty() );
        assertTrue( closedEnum.getActionManager() == am );
        assertTrue( closedEnum.isEditable() );
        assertTrue( am.isEnabled( DexActions.DESCRIPTIONCHANGE, closedEnum ) );
        assertFalse( closedEnum.nameProperty() instanceof ReadOnlyStringWrapper );

        // When - the property changes
        closedEnum.nameProperty().set( NEW_NAME );

        // Then
        assertTrue( closedEnum.getName().equals( NEW_NAME ) );
        // Then
        assertTrue( am.getQueueSize() == 1 );

        // When - undo
        am.undo();
        // Then
        assertTrue( closedEnum.getName().equals( originalDescription ) );
        assertTrue( am.getQueueSize() == 0 );


    }

    // @Test
    // public void testAssignTypeAction()
    // throws ExceptionInInitializerError, InstantiationException, IllegalAccessException, NoSuchMethodException,
    // SecurityException, IllegalArgumentException, InvocationTargetException {
    // DexFullActionManager am = new DexFullActionManager( null );
    // OtmModelManager mgr = new OtmModelManager( am, null );
    // assertTrue( am.getQueueSize() == 0 );
    // lib = mgr.add( new TLLibrary() );
    //
    // // Given a OtmObjects
    // OtmEnumeration<?> closedEnum = TestEnumerationClosed.buildOtm( mgr );
    // lib.add( closedEnum );
    // OtmValueWithAttributes vwa = TestValueWithAttributes.buildOtm( mgr );
    // lib.add( vwa );
    // assertFalse( "Must not have type.", vwa.getAssignedType() == closedEnum );
    // OtmTypeProvider originalType = vwa.getAssignedType();
    // assertTrue( vwa.getActionManager() instanceof DexFullActionManager );
    //
    // // As run from the GUI, the run will launch a dialog
    // // user.getActionManager().run( DexActions.TYPECHANGE, user );
    // // The action uses doIt(otmTypeProvider);
    // // Action manager uses actionHandler = DexActions.getAction( action, subject );
    //
    // // Given - an action for type change on VWA
    // DexRunAction action = (DexRunAction) DexActions.getAction( DexActions.TYPECHANGE, vwa );
    // assertNotNull( action );
    //
    // // When - executed using action's method
    // action.doIt( closedEnum );
    //
    // // Then
    // assertTrue( "Must have type.", vwa.getAssignedType() == closedEnum );
    // assertTrue( !am.getLastActionName().isEmpty() );
    // assertTrue( am.getQueueSize() > 0 );
    //
    // // When - undo the change
    // am.undo();
    // // Then
    // assertTrue( "Undo must restore type.", vwa.getAssignedType() == originalType );
    //
    // log.debug( "Name Change Test complete." );
    // }

    @Test
    public void testReadOnlyActionManager() {
        lib = TestLibrary.buildOtm();
        OtmModelManager mgr = lib.getModelManager();
        DexFullActionManager am = (DexFullActionManager) mgr.getActionManager( true );

        OtmBusinessObject newBO = TestBusiness.buildOtm( lib, "NewBO" );
        lib.add( newBO );
        assertTrue( newBO.isEditable() );

        // Should do nothing and have no side effects
        // am.run( null, null );
        am.run( DexActions.DESCRIPTIONCHANGE, null );

        // Should always be false
        assertFalse( am.isEnabled( DexActions.DESCRIPTIONCHANGE, TestEnumerationClosed.buildOtm( mgr ) ) );
        log.debug( "Done." );
    }
}
