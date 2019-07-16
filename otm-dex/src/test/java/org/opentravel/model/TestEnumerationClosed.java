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

import org.junit.BeforeClass;
import org.opentravel.model.otmLibraryMembers.OtmEnumerationClosed;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;

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
