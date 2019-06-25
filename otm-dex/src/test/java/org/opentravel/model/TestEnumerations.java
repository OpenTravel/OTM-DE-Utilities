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
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.opentravel.model.otmLibraryMembers.OtmEnumeration;
import org.opentravel.model.otmLibraryMembers.OtmEnumerationClosed;
import org.opentravel.model.otmLibraryMembers.OtmEnumerationOpen;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;

/**
 * Verifies the functions of the <code>UserSettings</code> class.
 */
public class TestEnumerations {
    private static Log log = LogFactory.getLog( TestEnumerations.class );

    @Test
    public void testConstructors() {
        OtmModelManager mgr = new OtmModelManager( null );

        OtmEnumeration<?> otc = new OtmEnumerationClosed( buildTLClosedEnumeration(), mgr );
        assertNotNull( otc );
        OtmEnumeration<?> oto = new OtmEnumerationOpen( buildTLOpenEnumeration(), mgr );
        assertNotNull( oto );

        log.debug( "Done." );
    }

    @Test
    @Ignore
    public void testInheritance() {
        OtmModelManager mgr = new OtmModelManager( null );
        OtmEnumerationClosed otc_base = buildOtmEnumerationClosed( mgr );

        // When extended
        OtmEnumerationClosed otc_ex = buildOtmEnumerationClosed( otc_base, mgr );
        assertTrue( !otc_ex.getChildren().isEmpty() );
        assertTrue( !otc_ex.getInheritedChildren().isEmpty() );
        otc_ex.getInheritedChildren().forEach( i -> assertTrue( i.isInherited() ) );
    }

    public static OtmEnumerationClosed buildOtmEnumerationClosed(OtmEnumerationClosed base, OtmModelManager mgr) {
        TLExtension tlex = new TLExtension();
        tlex.setExtendsEntity( base.getTL() );

        TLClosedEnumeration tlc = buildTLClosedEnumeration();
        tlc.setExtension( tlex );

        OtmEnumerationClosed otc_ex = new OtmEnumerationClosed( tlc, mgr );
        assertNotNull( otc_ex );
        assertTrue( otc_ex.getBaseType() != null );
        return otc_ex;
    }

    public static OtmEnumerationClosed buildOtmEnumerationClosed(OtmModelManager mgr) {
        OtmEnumerationClosed otc = new OtmEnumerationClosed( buildTLClosedEnumeration(), mgr );
        assertNotNull( otc );
        return otc;
    }

    public static OtmEnumerationOpen buildOtmEnumerationOpen(OtmModelManager mgr) {
        OtmEnumerationOpen oto = new OtmEnumerationOpen( buildTLOpenEnumeration(), mgr );
        assertNotNull( oto );
        return oto;
    }

    public static TLClosedEnumeration buildTLClosedEnumeration() {
        TLClosedEnumeration tlc = new TLClosedEnumeration();
        tlc.setName( "TestClosedEnum" );
        tlc.addValue( getTLEnumValue( "c1" ) );
        tlc.addValue( getTLEnumValue( "c2" ) );
        tlc.addValue( getTLEnumValue( "c3" ) );
        return tlc;
    }

    public static TLOpenEnumeration buildTLOpenEnumeration() {
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
