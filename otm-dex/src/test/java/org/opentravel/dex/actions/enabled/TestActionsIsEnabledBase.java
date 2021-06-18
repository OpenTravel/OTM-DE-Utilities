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

package org.opentravel.dex.actions.enabled;

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.actions.DexAction;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmChoiceObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;
import org.opentravel.model.otmLibraryMembers.TestChoice;
import org.opentravel.model.otmLibraryMembers.TestValueWithAttributes;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.TestOtmPropertiesBase;

/**
 * Base class for testing if actions are enabled.
 * <p>
 * Tests read only and full action manager, not minor action manager.
 * <p>
 * This does not require AbstractFxTest
 */
public abstract class TestActionsIsEnabledBase {
    private static Log log = LogFactory.getLog( TestActionsIsEnabledBase.class );

    protected static OtmModelManager modelManager = null;
    protected DexActionManager actionManager = null;
    DexAction<?> action = null;
    protected DexActions actionEnum = null;
    protected OtmLibrary lib = null;

    public TestActionsIsEnabledBase(DexActions actionEnum) {
        lib = TestLibrary.buildOtm();
        modelManager = lib.getModelManager();
        actionManager = lib.getActionManager();
        this.actionEnum = actionEnum;
        // this.action = getAction( actionEnum );

        assertTrue( "Given:", lib != null );
        assertTrue( "Given:", modelManager != null );
        assertTrue( "Given:", actionManager != null );
        assertTrue( "Given:", actionEnum != null );
    }

    public void setLibrary(OtmLibrary lib) {
        this.lib = lib;
    }

    /**
     * Add one of each to the library then test each member.
     */
    public void testMembers() {
        TestLibrary.addOneOfEach( lib );
        // assertTrue( "Given:", !lib.getMembers().isEmpty() );

        for (OtmLibraryMember member : lib.getMembers()) {
            testMember( member );
        }
    }

    public abstract void testMember(OtmLibraryMember member);

    public void testMember(OtmLibraryMember member, boolean expected) {
        // log.debug( "Testing if " + actionEnum + " is enabled for " + member.getObjectTypeName() + " " + member );
        assertTrue( "Test: ", expected == actionManager.isEnabled( actionEnum, member ) );
        testMember( member, null, expected );
    }

    // TODO - Use this for facets. They are not currently tested.
    public void testChildrenOwner(OtmChildrenOwner child, boolean expected) {
        testMember( child, null, expected );
    }

    /**
     * Override if two parameter isEnabled uses second parameter.
     */
    public void testMember(OtmChildrenOwner member, OtmObject param, boolean expected) {
        if (expected)
            log.debug(
                "Test member if " + actionEnum + " is enabled for " + member.getObjectTypeName() + " " + member );
        else
            log.debug(
                "Test member if " + actionEnum + " is NOT enabled for " + member.getObjectTypeName() + " " + member );
        assertTrue( "Test: ", expected == actionManager.isEnabled( actionEnum, member ) );
    }

    /**
     * Properties
     * 
     * Create a choice and VWA. For children/descendants, if a property,
     * {@linkplain TestActionsIsEnabledBase#testProperty(OtmProperty)}
     */
    public void testProperties() {
        OtmChoiceObject choice = TestChoice.buildOtm( lib, "TestChoice" );
        TestOtmPropertiesBase.buildOneOfEach2( choice.getShared() );
        for (OtmObject obj : choice.getShared().getChildren())
            if (obj instanceof OtmProperty) {
                testProperty( (OtmProperty) obj );
            }

        OtmValueWithAttributes vwa = TestValueWithAttributes.buildOtm( lib, "TestVWA" );
        TestOtmPropertiesBase.buildOneOfEach2( vwa );
        for (OtmObject obj : vwa.getDescendants())
            if (obj instanceof OtmProperty) {
                testProperty( (OtmProperty) obj );
            }
    }

    /**
     * Using the global action manager test if the property is enabled for the global actionEnum action.
     * 
     * @param property
     */
    public abstract void testProperty(OtmProperty property);

    /**
     * Assert the action manager isEnabled for the property will return the expected value.
     * <p>
     * Override if two parameter isEnabled uses second parameter.
     */
    public void testProperty(OtmProperty property, boolean expected) {
        // log.debug( "Testing if " + actionEnum + " is enabled for " + property.getObjectTypeName() + " " + property );
        assertTrue( "Test: ", expected == actionManager.isEnabled( actionEnum, property ) );
        testProperty( property, null, expected );
    }

    /**
     * Assert the action manager isEnabled for the property and parameter will return the expected value.
     * <p>
     * Override if two parameter isEnabled uses second parameter.
     */
    public void testProperty(OtmProperty property, OtmObject param, boolean expected) {
        if (expected)
            log.debug( "Testing if " + actionEnum + " is enabled " + expected + " for " + property.getObjectTypeName()
                + " for " + param );
        else
            log.debug( "Testing if " + actionEnum + " is NOT enabled " + expected + " for "
                + property.getObjectTypeName() + " for " + param );
        assertTrue( "Test: ", expected == actionManager.isEnabled( actionEnum, property, param ) );
    }
}

