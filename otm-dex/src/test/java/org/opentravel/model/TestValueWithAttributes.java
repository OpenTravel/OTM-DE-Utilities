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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.TestUserSettings;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;

/**
 * Verifies the functions of the <code>UserSettings</code> class.
 */
public class TestValueWithAttributes {
    private static Log log = LogFactory.getLog( TestUserSettings.class );

    private static OtmModelManager staticModelManager = null;

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null );
    }

    @Test
    public void testConstructors() {
        OtmValueWithAttributes vwa = new OtmValueWithAttributes( "fred", staticModelManager );
        assertNotNull( vwa );
        // TODO - test name

        vwa = new OtmValueWithAttributes( buildTLValueWithAttributes(), staticModelManager );
        assertNotNull( vwa );

        log.debug( "Done." );
    }

    public static OtmValueWithAttributes buildOtmValueWithAttributes(OtmModelManager mgr) {
        OtmValueWithAttributes vwa = new OtmValueWithAttributes( buildTLValueWithAttributes(), mgr );
        assertNotNull( vwa );
        return vwa;
    }

    public static TLValueWithAttributes buildTLValueWithAttributes() {
        TLValueWithAttributes tlvwa = new TLValueWithAttributes();
        tlvwa.setName( "TestVWA" );
        // TODO - add attributes
        // TODO - add value
        return tlvwa;
    }
}
