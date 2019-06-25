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
import org.opentravel.dex.actions.DexActionManager.DexActions;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.TestEnumerations;
import org.opentravel.model.TestValueWithAttributes;
import org.opentravel.model.otmLibraryMembers.OtmEnumeration;
import org.opentravel.model.otmLibraryMembers.OtmEnumerationOpen;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;

/**
 * Verifies the functions of the <code>UserSettings</code> class.
 */
public class TestDexActionManager {
    private static Log log = LogFactory.getLog( TestDexActionManager.class );

    private static OtmModelManager staticModelManager = null;

    @BeforeClass
    public static void beforeClass() {
        // staticModelManager = new OtmModelManager(null);
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
    public void testActionFactory() {
        DexActionManager am = new DexReadOnlyActionManager();
        OtmModelManager mgr = new OtmModelManager( am );

        // Given an OtmObject
        OtmEnumerationOpen openEnum = TestEnumerations.buildOtmEnumerationOpen( mgr );
        OtmValueWithAttributes vwa = TestValueWithAttributes.buildOtmValueWithAttributes( mgr );
        // Action Factory - type users all return actions
        for (DexActions action : DexActions.values()) {
            assertNotNull( "Must return an action for " + action.toString() + ".", am.actionFactory( action, vwa ) );
        }
        log.debug( "Factory Test complete." );
    }

    @Test
    public void testNameChangeAction() {
        DexActionManager am = new DexReadOnlyActionManager();
        OtmModelManager mgr = new OtmModelManager( am );
        assertTrue( am.getQueueSize() == 0 );

        // Given a OtmObjects
        OtmEnumeration<?> closedEnum = TestEnumerations.buildOtmEnumerationClosed( mgr );
        OtmValueWithAttributes vwa = TestValueWithAttributes.buildOtmValueWithAttributes( mgr );

        // Actions are atomic but hidden from controller API
        //
        DexAction<?> action = vwa.getActionManager().actionFactory( DexActions.TYPECHANGE, vwa );
        action.doIt( closedEnum );
        assertTrue( "Must have type.", vwa.getAssignedType() == closedEnum );
        assertTrue( !am.getLastActionName().isEmpty() );
        assertTrue( am.getQueueSize() > 0 );
        // TODO assertTrue(am.undo();

        log.debug( "Factory Test complete." );
    }

    @Test
    public void testReadOnlyActionManager() {
        DexActionManager am = new DexReadOnlyActionManager();
        OtmModelManager mgr = new OtmModelManager( am );

        // Should do nothing and have no side effects
        am.run( null, null, null );
        am.run( DexActions.DESCRIPTIONCHANGE, null, null );

        // Should always be false
        assertFalse( am.isEnabled( DexActions.DESCRIPTIONCHANGE, TestEnumerations.buildOtmEnumerationClosed( mgr ) ) );
        log.debug( "Done." );
    }
}
