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
import org.opentravel.dex.actions.BaseTypeChangeAction;
import org.opentravel.dex.actions.SetAssignedTypeAction;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;

import java.util.Collection;

/**
 * Verifies the functions of the <code>UserSettings</code> class.
 */
public class TestValueWithAttributes extends TestOtmLibraryMemberBase<OtmValueWithAttributes> {
    // private static Log log = LogFactory.getLog( TestValueWithAttributes.class );

    static String NAME = "TestVWA";

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null, null );
        subject = buildOtm( staticModelManager );
        baseObject = buildOtm( staticModelManager );
        baseObject.setName( "BaseVWA" );
    }

    public static OtmValueWithAttributes buildOtm(OtmModelManager mgr) {
        OtmTypeProvider simple = TestXsdSimple.buildOtm( mgr );
        OtmValueWithAttributes vwa = new OtmValueWithAttributes( buildTL( (TLAttributeType) simple.getTL() ), mgr );
        assertNotNull( vwa );


        OtmTypeProvider p = vwa.getAssignedType();
        return vwa;
    }

    /** ******************************************************** **/

    /**
     * @param lib
     * @param name
     * @return
     */
    public static OtmValueWithAttributes buildOtm(OtmLibrary lib, String name) {
        assertTrue( lib.isEditable() );
        OtmValueWithAttributes vwa = buildOtm( lib.getModelManager() );
        vwa.setName( name );
        lib.add( vwa );
        assertTrue( lib.contains( vwa ) );
        return vwa;
    }

    /**
     * @param mgr
     * @param string
     * @return
     */
    public static OtmValueWithAttributes buildOtm(OtmModelManager mgr, String string) {
        OtmValueWithAttributes vwa = buildOtm( mgr );
        vwa.setName( string );
        return vwa;
    }


    public static TLValueWithAttributes buildTL() {
        TLValueWithAttributes tlvwa = new TLValueWithAttributes();
        tlvwa.setName( NAME );
        // tlvwa.setParentType( TestXsdSimple.buildTL() );

        // add attributes
        int i = 1;
        while (i < 5) {
            TLAttribute tla = new TLAttribute();
            tla.setName( NAME + i );
            // tla.setType( TestXsdSimple.buildTL() );
            tlvwa.addAttribute( tla );
            i++;
        }

        // assertNotNull( tlvwa.getParentType() );
        assertTrue( tlvwa.getAttributes().size() == i - 1 );
        return tlvwa;
    }

    public static TLValueWithAttributes buildTL(TLAttributeType type) {
        TLValueWithAttributes tlvwa = new TLValueWithAttributes();
        tlvwa.setName( NAME );
        tlvwa.setParentType( type );

        // add attributes
        int i = 1;
        while (i < 5) {
            TLAttribute tla = new TLAttribute();
            tla.setName( NAME + i );
            tla.setType( type );
            tlvwa.addAttribute( tla );
            i++;
        }

        assertNotNull( tlvwa.getParentType() );
        assertTrue( tlvwa.getAttributes().size() == i - 1 );
        return tlvwa;
    }

    /** ****************************************************** **/

    @Test
    public void testDescendentsTypeUsers() {
        OtmValueWithAttributes vwa = buildOtm( staticModelManager, "TestVwa" );
        Collection<OtmTypeUser> d = vwa.getDescendantsTypeUsers();
        assertTrue( !d.isEmpty() );
    }

    @Test
    public void testBaseType() {
        OtmValueWithAttributes vwa = buildOtm( staticModelManager, "TestVwa" );
        OtmValueWithAttributes baseVwa = buildOtm( staticModelManager, "BaseVwa" );

        OtmValueWithAttributes base = vwa.getBaseType();
        vwa.setBaseType( baseVwa );
        base = vwa.getBaseType();
        assertTrue( base == baseVwa );
    }

    @Test
    public void TestVWAasBaseAndValueType() {
        // Determine and assure Value and Base type are correct.
        // OtmModelManager mgr = TestOtmModelManager.build();
        OtmLibrary lib = TestLibrary.buildOtm( staticModelManager, "http://example.com", "ex", "lib1" );
        OtmValueWithAttributes vwa1 = buildOtm( lib, "TestVWA1" );
        OtmValueWithAttributes vwa2 = buildOtm( lib, "TestVWA2" );
        OtmValueWithAttributes vwa3 = buildOtm( lib, "TestVWA3" );
        OtmValueWithAttributes vwa4 = buildOtm( lib, "TestVWA4" );

        assertTrue( "Given", vwa1.isEditable() );
        assertTrue( "Given", BaseTypeChangeAction.isEnabled( vwa1 ) == true );
        assertTrue( "Given", SetAssignedTypeAction.isEnabled( vwa1 ) == true );

        vwa1.setAssignedType( staticModelManager.getXsdMember( "decimal" ) );
        vwa2.setAssignedType( staticModelManager.getXsdMember( "date" ) );
        vwa3.setAssignedType( staticModelManager.getXsdMember( "float" ) );
        vwa4.setAssignedType( staticModelManager.getXsdMember( "time" ) );
        assertTrue( vwa1.getAssignedType() == staticModelManager.getXsdMember( "decimal" ) );
        assertTrue( vwa2.getAssignedType() == staticModelManager.getXsdMember( "date" ) );
        assertTrue( vwa3.getAssignedType() == staticModelManager.getXsdMember( "float" ) );
        assertTrue( vwa4.getAssignedType() == staticModelManager.getXsdMember( "time" ) );

        OtmTypeProvider base = vwa1.getBaseType();
        OtmTypeProvider value = vwa1.getAssignedType();
        assertTrue( "Given", base == null );
        // When
        vwa1.setBaseType( vwa2 );
        // Then
        base = vwa1.getBaseType();
        value = vwa1.getAssignedType();
        assertTrue( base == vwa2 );
        assertTrue( value == vwa2.getAssignedType() );

        base = vwa3.getBaseType();
        assertTrue( "Given", base == null );
        value = vwa3.getAssignedType();

        // When - assigned
        vwa3.setAssignedType( vwa4 );
        // Then
        base = vwa3.getBaseType();
        value = vwa3.getAssignedType();
        assertTrue( base == vwa4 ); // Not sure why
        assertTrue( value == vwa4.getAssignedType() );
    }



}
