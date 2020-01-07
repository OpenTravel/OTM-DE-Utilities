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
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
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
        staticModelManager = new OtmModelManager( null, null );
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
}
