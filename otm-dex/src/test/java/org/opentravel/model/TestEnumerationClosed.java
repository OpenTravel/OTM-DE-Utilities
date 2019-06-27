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
import org.opentravel.model.otmLibraryMembers.OtmEnumerationClosed;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;

/**
 */
public class TestEnumerationClosed extends TestOtmLibraryMemberBase<OtmEnumerationClosed> {
    // private static Log log = LogFactory.getLog( TestEnumerationClosed.class );

    @Test
    public void testConstructors() {
        super.testConstructors( buildOtm( staticModelManager ) );
    }


    @Test
    public void testInheritance() {
        OtmEnumerationClosed otc_base = buildOtm( staticModelManager );
        OtmEnumerationClosed otc_ex = buildOtm( staticModelManager );

        // When extended
        extendObject( otc_base, otc_ex );

        testInheritance( otc_ex );
    }


    public static OtmEnumerationClosed buildOtm(OtmModelManager mgr) {
        OtmEnumerationClosed otc = new OtmEnumerationClosed( buildTL(), mgr );
        assertNotNull( otc );
        return otc;
    }


    public static TLClosedEnumeration buildTL() {
        TLClosedEnumeration tlc = new TLClosedEnumeration();
        tlc.setName( "TestClosedEnum" );
        tlc.addValue( getTLEnumValue( "c1" ) );
        tlc.addValue( getTLEnumValue( "c2" ) );
        tlc.addValue( getTLEnumValue( "c3" ) );
        return tlc;
    }


    public static TLEnumValue getTLEnumValue(String literal) {
        TLEnumValue tle = new TLEnumValue();
        tle.setLiteral( literal );
        return tle;
    }

}
