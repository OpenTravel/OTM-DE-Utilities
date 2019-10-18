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

package org.opentravel.model.otmLibraryMembers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.dex.actions.AddPropertyAction;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.OtmPropertyType;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class TestEnumerationClosed extends TestOtmLibraryMemberBase<OtmEnumerationClosed> {
    // private static Log log = LogFactory.getLog( TestEnumerationClosed.class );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null );
        subject = buildOtm( staticModelManager );
        baseObject = buildOtm( staticModelManager );
        baseObject.setName( "BaseBO" );
    }

    // Tests both open and closed because add/delete are on abstract enumeration super-type
    @Test
    public void testAddingAndRemovingValues() {
        // Given - an enumeration with children
        OtmEnumerationClosed ec = buildOtm( staticModelManager );
        List<OtmObject> kids = new ArrayList<>( ec.getChildren() );
        assertTrue( "Must have children.", !kids.isEmpty() );

        // Then - delete them all
        for (OtmObject kid : kids)
            ec.delete( (OtmProperty) kid );
        assertTrue( "Must NOT have children.", ec.getChildren().isEmpty() );

        // Then - add them back
        for (OtmObject kid : kids)
            ec.add( kid );
        assertTrue( "Must have children.", !kids.isEmpty() );
    }

    @Test
    public void testAddAction() {
        // Given - an action with enumeration as subject
        OtmEnumerationClosed oe = buildOtm( staticModelManager );
        AddPropertyAction action = new AddPropertyAction();
        action.setSubject( oe );
        List<OtmObject> kids = new ArrayList<>( oe.getChildren() );
        assertTrue( "Must have children.", !kids.isEmpty() );

        // When action performed
        action.doIt( OtmPropertyType.ENUMVALUE );

        // Then - one more kid
        assertTrue( oe.getChildren().size() == kids.size() + 1 );

        action.doIt( OtmPropertyType.ENUMVALUE );
        assertTrue( oe.getChildren().size() == kids.size() + 2 );
        action.doIt( OtmPropertyType.ENUMVALUE );
        assertTrue( oe.getChildren().size() == kids.size() + 3 );
    }

    /** ****************************************************** **/

    public static OtmEnumerationClosed buildOtm(OtmModelManager mgr) {
        OtmEnumerationClosed otc = new OtmEnumerationClosed( buildTL(), mgr );
        assertNotNull( otc );
        return otc;
    }


    public static TLClosedEnumeration buildTL() {
        TLClosedEnumeration tlc = new TLClosedEnumeration();
        tlc.setName( "TestClosedEnum" );
        tlc.addValue( buildTLEnumValue( "c1" ) );
        tlc.addValue( buildTLEnumValue( "c2" ) );
        tlc.addValue( buildTLEnumValue( "c3" ) );
        return tlc;
    }


    public static TLEnumValue buildTLEnumValue(String literal) {
        TLEnumValue tle = new TLEnumValue();
        tle.setLiteral( literal );
        return tle;
    }

}
