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

package org.opentravel.dex.actions.constraints;

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.dex.actions.DexRunAction;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmSimpleObject;
import org.opentravel.model.otmLibraryMembers.OtmXsdSimple;
import org.opentravel.schemacompiler.model.TLSimple;

import java.io.IOException;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Verifies the functions of the <code>new library member action</code> class.
 */
public class TestSetConstraintsAction {

    private static Log log = LogFactory.getLog( TestSetConstraintsAction.class );

    private static OtmModelManager staticModelManager = null;
    static OtmLibrary lib = null;

    private static OtmXsdSimple decimal;
    private static OtmXsdSimple integer;
    private static OtmXsdSimple string;
    private static OtmXsdSimple id;

    @BeforeClass
    public static void beforeClass() throws IOException {
        lib = TestLibrary.buildOtm();
        staticModelManager = lib.getModelManager();
        assertTrue( lib.getTL().getOwningModel() == staticModelManager.getTlModel() );
        lib.getTL().setOwningModel( staticModelManager.getTlModel() );
        lib.getTL().setNamespace( "http://example.com/testNs" );

        // Library must be in TL Model for validation to be accurate.
        // Library must have namespace for simple assignments to work correctly.
        assertTrue( lib.isEditable() );
        assertTrue( lib.getActionManager() instanceof DexFullActionManager );
        assertTrue( lib.getTL().getOwningModel() != null );
        assertTrue( lib.getBaseNS() != null );

        // globalBO = (OtmBusinessObject) lib.add( TestBusiness.buildOtm( staticModelManager, "GlobalBO" ) );
        //
        // assertTrue( globalBO != null );
        // assertTrue( globalBO.getLibrary() == lib );
        // assertTrue( globalBO.isEditable() );
        // assertTrue( globalBO.getActionManager() == lib.getActionManager() );
        // assertTrue( staticModelManager.getMembers().contains( globalBO ) );

        decimal = staticModelManager.getXsdMember( OtmModelManager.XSD_DECIMAL_NAME );
        integer = staticModelManager.getXsdMember( OtmModelManager.XSD_INTEGER_NAME );
        id = staticModelManager.getXsdMember( OtmModelManager.XSD_ID_NAME );
        string = staticModelManager.getXsdMember( OtmModelManager.XSD_STRING_NAME );
        assertTrue( decimal != null );
        assertTrue( integer != null );
        assertTrue( id != null );
        assertTrue( string != null );


    }

    public OtmSimpleObject buildSimple() {
        return buildSimple( string );
    }

    public OtmSimpleObject buildSimple(OtmXsdSimple type) {
        TLSimple tlSimple = new TLSimple();
        tlSimple.setOwningLibrary( lib.getTL() );
        tlSimple.setName( "Simple1" );
        OtmSimpleObject simple = new OtmSimpleObject( tlSimple, staticModelManager );
        simple.setAssignedType( type );
        return simple;
    }

    @Test
    public void testDoIt() throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {
        // Strings use the string property
        OtmSimpleObject s1 = buildSimple(); // make it easier to see while debugging
        s1.setAssignedType( string );
        DexActionManager am = s1.getActionManager();
        assertTrue( am instanceof DexFullActionManager );

        String start = "Start";
        String changeTo = "NewStringValue";
        String changeTo2 = "AnotherNewStringValue";

        //
        // Full test with all the inner workings.
        //
        SetConstraintPatternAction action = new SetConstraintPatternAction();
        assertTrue( SetConstraintPatternAction.isEnabled( s1 ) );

        assertTrue( "Given", am.isEnabled( DexActions.SETCONSTRAINT_PATTERN, s1 ) );
        s1.getTL().setPattern( start );
        assertTrue( "Given", s1.getTL().getPattern().equals( start ) );

        // When - using the action directly
        action.setSubject( s1 );
        action.set( changeTo2 );
        assertTrue( "Must have changed.", s1.getTL().getPattern().equals( changeTo2 ) );

        // Using the string property
        StringProperty sp = am.add( DexActions.SETCONSTRAINT_PATTERN, s1.getTL().getPattern(), s1 );
        assertTrue( "Given", sp instanceof SimpleStringProperty );
        // When
        sp.set( changeTo );
        assertTrue( "Must have changed.", s1.getTL().getPattern().equals( changeTo ) );

        s1.setAssignedType( decimal );
        assertTrue( SetConstraintMaxExclusiveAction.isEnabled( s1 ) );

        s1.getTL().setMinInclusive( "" );
        s1.getTL().setMaxInclusive( "" );
        s1.getTL().setMinExclusive( start );
        s1.getTL().setMaxExclusive( start );
        am.add( DexActions.SETCONSTRAINT_MINEXCLUSIVE, s1.getTL().getMinExclusive(), s1 ).set( changeTo );
        am.add( DexActions.SETCONSTRAINT_MAXEXCLUSIVE, s1.getTL().getMaxExclusive(), s1 ).set( changeTo2 );
        assertTrue( "Must have changed.", s1.getTL().getMinExclusive().equals( changeTo ) );
        assertTrue( "Must have changed.", s1.getTL().getMaxExclusive().equals( changeTo2 ) );

        s1.getTL().setMinInclusive( start );
        s1.getTL().setMaxInclusive( start );
        s1.getTL().setMinExclusive( "" );
        s1.getTL().setMaxExclusive( "" );
        am.add( DexActions.SETCONSTRAINT_MININCLUSIVE, s1.getTL().getMinInclusive(), s1 ).set( changeTo );
        am.add( DexActions.SETCONSTRAINT_MAXINCLUSIVE, s1.getTL().getMaxInclusive(), s1 ).set( changeTo2 );
        assertTrue( "Must have changed.", s1.getTL().getMinInclusive().equals( changeTo ) );
        assertTrue( "Must have changed.", s1.getTL().getMaxInclusive().equals( changeTo2 ) );
    }

    @Test
    public void testRun() throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {
        OtmSimpleObject s1 = buildSimple(); // make it easier to see while debugging
        // s1.setAssignedType( string );
        DexActionManager am = s1.getActionManager();
        assertTrue( am instanceof DexFullActionManager );

        s1.setAssignedType( decimal );
        assertTrue( SetConstraintTotalDigitsAction.isEnabled( s1 ) );
        int startV = 2;
        int changedV = 4;

        s1.getTL().setTotalDigits( startV );
        assertTrue( s1.getTL().getTotalDigits() == startV );
        DexRunAction action = new SetConstraintTotalDigitsAction();
        action.setSubject( s1 );
        am.run( action, changedV );
        assertTrue( s1.getTL().getTotalDigits() == changedV++ );

        s1.getTL().setFractionDigits( startV );
        assertTrue( s1.getTL().getFractionDigits() == startV );
        action = new SetConstraintFractionDigitsAction();
        action.setSubject( s1 );
        am.run( action, changedV );
        assertTrue( s1.getTL().getFractionDigits() == changedV++ );

        // Only works on strings
        s1 = buildSimple();
        s1.setAssignedType( string );
        assertTrue( am.isEnabled( DexActions.SETCONSTRAINT_MINLENGTH, s1 ) );
        s1.getTL().setMinLength( startV );
        assertTrue( s1.getTL().getMinLength() == startV );
        action = new SetConstraintMinLengthAction();
        action.setSubject( s1 );
        am.run( action, changedV );
        assertTrue( s1.getTL().getMinLength() == changedV++ );

        s1.getTL().setMaxLength( startV );
        assertTrue( s1.getTL().getMaxLength() == startV );
        action = new SetConstraintMaxLengthAction();
        action.setSubject( s1 );
        am.run( action, changedV );
        assertTrue( s1.getTL().getMaxLength() == changedV );
    }


    @Test
    public void testIsEnabled_SetConstraintMaxExclusiveAction()
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {

        OtmSimpleObject s1 = buildSimple(); // make it easier to see while debugging

        s1.setAssignedType( string );
        assertTrue( !SetConstraintMaxExclusiveAction.isEnabled( s1 ) );

        s1.setList( true );
        assertTrue( !SetConstraintMaxExclusiveAction.isEnabled( s1 ) );
        s1.setList( false );

        s1.setAssignedType( decimal );
        assertTrue( SetConstraintMaxExclusiveAction.isEnabled( s1 ) );

        s1.setAssignedType( integer );
        assertTrue( SetConstraintMaxExclusiveAction.isEnabled( s1 ) );
    }

    @Test
    public void testIsEnabled_SetConstraintMaxInclusiveAction()
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {

        OtmSimpleObject s1 = buildSimple(); // make it easier to see while debugging

        s1.setAssignedType( string );
        assertTrue( !SetConstraintMaxInclusiveAction.isEnabled( s1 ) );

        s1.setList( true );
        assertTrue( !SetConstraintMaxInclusiveAction.isEnabled( s1 ) );
        s1.setList( false );

        s1.setAssignedType( decimal );
        assertTrue( SetConstraintMaxInclusiveAction.isEnabled( s1 ) );

        s1.setAssignedType( integer );
        assertTrue( SetConstraintMaxInclusiveAction.isEnabled( s1 ) );
    }

    @Test
    public void testIsEnabled_SetConstraintMinExclusiveAction()
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {

        OtmSimpleObject s1 = buildSimple(); // make it easier to see while debugging

        s1.setAssignedType( string );
        assertTrue( !SetConstraintMinExclusiveAction.isEnabled( s1 ) );

        s1.setList( true );
        assertTrue( !SetConstraintMinExclusiveAction.isEnabled( s1 ) );
        s1.setList( false );

        s1.setAssignedType( decimal );
        assertTrue( SetConstraintMinExclusiveAction.isEnabled( s1 ) );

        s1.setAssignedType( integer );
        assertTrue( SetConstraintMinExclusiveAction.isEnabled( s1 ) );
    }

    @Test
    public void testIsEnabled_SetConstraintMinInclusiveAction()
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {

        OtmSimpleObject s1 = buildSimple(); // make it easier to see while debugging

        s1.setAssignedType( string );
        assertTrue( !SetConstraintMinInclusiveAction.isEnabled( s1 ) );

        s1.setList( true );
        assertTrue( !SetConstraintMinInclusiveAction.isEnabled( s1 ) );
        s1.setList( false );

        s1.setAssignedType( decimal );
        assertTrue( SetConstraintMinInclusiveAction.isEnabled( s1 ) );

        s1.setAssignedType( integer );
        assertTrue( SetConstraintMinInclusiveAction.isEnabled( s1 ) );
    }

    @Test
    public void testIsEnabled_SetConstraintTotalDigitsAction()
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {
        OtmSimpleObject s1 = buildSimple(); // make it easier to see while debugging

        // Vetoed
        s1.setAssignedType( string );
        assertTrue( !SetConstraintTotalDigitsAction.isEnabled( s1 ) );
        s1.setList( true );
        assertTrue( !SetConstraintTotalDigitsAction.isEnabled( s1 ) );
        s1.setList( false );
        s1.setAssignedType( staticModelManager.getXsdMember( "id" ) );
        assertTrue( !SetConstraintTotalDigitsAction.isEnabled( s1 ) );

        // Enabled
        s1.setAssignedType( decimal );
        assertTrue( SetConstraintTotalDigitsAction.isEnabled( s1 ) );
        s1.setAssignedType( integer );
        assertTrue( SetConstraintTotalDigitsAction.isEnabled( s1 ) );
    }

    @Test
    public void testIsEnabled_SetConstraintFractionDigitsAction()
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {
        OtmSimpleObject s1 = buildSimple(); // make it easier to see while debugging
        // Vetoed
        s1.setAssignedType( string );
        assertTrue( !SetConstraintFractionDigitsAction.isEnabled( s1 ) );
        s1.setList( true );
        assertTrue( !SetConstraintFractionDigitsAction.isEnabled( s1 ) );
        s1.setList( false );
        s1.setAssignedType( staticModelManager.getXsdMember( "id" ) );
        assertTrue( !SetConstraintFractionDigitsAction.isEnabled( s1 ) );

        // Enabled
        s1.setAssignedType( decimal );
        assertTrue( SetConstraintFractionDigitsAction.isEnabled( s1 ) );
        s1.setAssignedType( integer );
        assertTrue( SetConstraintFractionDigitsAction.isEnabled( s1 ) );
    }

    @Test
    public void testIsEnabled_SetConstraintMinLengthAction()
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {
        OtmSimpleObject s1 = buildSimple(); // make it easier to see while debugging
        // Vetoed
        s1.setAssignedType( string );
        s1.setList( true );
        assertTrue( !SetConstraintMinLengthAction.isEnabled( s1 ) );
        s1.setList( false );
        s1.setAssignedType( integer );
        assertTrue( !SetConstraintMinLengthAction.isEnabled( s1 ) );
        s1.setAssignedType( decimal );
        assertTrue( !SetConstraintMinLengthAction.isEnabled( s1 ) );
        // should be enabled, but validation findings veto it
        s1.setAssignedType( staticModelManager.getXsdMember( "id" ) );
        assertTrue( !SetConstraintMinLengthAction.isEnabled( s1 ) );
        // Enabled
        s1.setAssignedType( string );
        assertTrue( SetConstraintMinLengthAction.isEnabled( s1 ) );
    }

    @Test
    public void testIsEnabled_SetConstraintMaxLengthAction()
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {
        OtmSimpleObject s1 = buildSimple(); // make it easier to see while debugging
        // Vetoed
        s1 = buildSimple( string );
        s1.setList( true );
        assertTrue( !SetConstraintMaxLengthAction.isEnabled( s1 ) );
        s1.setList( false );
        s1.setAssignedType( decimal );
        assertTrue( !SetConstraintMaxLengthAction.isEnabled( s1 ) );
        s1.setAssignedType( integer );
        assertTrue( !SetConstraintMaxLengthAction.isEnabled( s1 ) );

        // Should be enabled, but validation creates veto findings.
        s1.setAssignedType( staticModelManager.getXsdMember( "id" ) );
        assertTrue( !SetConstraintMaxLengthAction.isEnabled( s1 ) );

        // Enabled
        s1 = buildSimple( string );
        assertTrue( SetConstraintMaxLengthAction.isEnabled( s1 ) );

    }

    @Test
    public void testIsEnabled_SetConstraintPatternAction()
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {

        OtmSimpleObject s1 = buildSimple(); // make it easier to see while debugging

        s1.setAssignedType( string );
        assertTrue( SetConstraintPatternAction.isEnabled( s1 ) );

        s1.setList( true );
        assertTrue( !SetConstraintPatternAction.isEnabled( s1 ) );
        s1.setList( false );

        s1.setAssignedType( decimal );
        assertTrue( SetConstraintPatternAction.isEnabled( s1 ) );

        s1.setAssignedType( integer );
        assertTrue( SetConstraintPatternAction.isEnabled( s1 ) );
    }

    @Test
    public void testUndo() {}
}
