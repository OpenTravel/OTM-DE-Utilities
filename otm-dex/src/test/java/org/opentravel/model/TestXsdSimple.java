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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.opentravel.model.otmLibraryMembers.OtmXsdSimple;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.w3._2001.xmlschema.TopLevelSimpleType;

/**
 * Verifies the functions of the <code>UserSettings</code> class.
 */
public class TestXsdSimple extends TestOtmLibraryMemberBase<OtmXsdSimple> {
    private static Log log = LogFactory.getLog( TestXsdSimple.class );


    @Test
    public void testConstructors() {
        super.testConstructors( buildOtm( staticModelManager ) );
    }


    /** ****************************************************** **/

    public static OtmXsdSimple buildOtm(OtmModelManager mgr) {
        OtmXsdSimple simple = new OtmXsdSimple( buildTL(), mgr );
        return simple;
    }

    /**
     * Build an TL xsd simple type named "simpleString".
     * 
     * @return
     */
    public static XSDSimpleType buildTL() {
        TopLevelSimpleType jaxbType = null;
        XSDSimpleType xsdSimple = new XSDSimpleType( "simpleString", jaxbType );
        return xsdSimple;
    }
}
