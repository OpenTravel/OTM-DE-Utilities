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

package org.opentravel.model;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opentravel.model.otmLibraryMembers.OtmEnumerationOpen;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;

/**
 */
public class TestEnumerationOpen extends TestOtmLibraryMemberBase<OtmEnumerationOpen> {
    // private static Log log = LogFactory.getLog( TestEnumerationOpen.class );

    @Test
    public void testConstructors() {
        super.testConstructors( buildOtm( staticModelManager ) );
    }


    @Test
    public void testInheritance() {
        OtmEnumerationOpen oto_base = buildOtm( staticModelManager );
        OtmEnumerationOpen oto_ex = buildOtm( staticModelManager );

        // When extended
        extendObject( oto_base, oto_ex );

        testInheritance( oto_ex );
    }


    public static OtmEnumerationOpen buildOtm(OtmModelManager mgr) {
        OtmEnumerationOpen oto = new OtmEnumerationOpen( buildTL(), mgr );
        assertNotNull( oto );
        return oto;
    }

    public static TLOpenEnumeration buildTL() {
        TLOpenEnumeration tlo = new TLOpenEnumeration();
        tlo.setName( "TestOpenEnum" );
        tlo.addValue( getTLEnumValue( "o1" ) );
        tlo.addValue( getTLEnumValue( "o2" ) );
        tlo.addValue( getTLEnumValue( "o3" ) );
        return tlo;
    }

    public static TLEnumValue getTLEnumValue(String literal) {
        TLEnumValue tle = new TLEnumValue();
        tle.setLiteral( literal );
        return tle;
    }

}
