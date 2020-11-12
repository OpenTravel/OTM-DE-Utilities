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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelManager;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.w3._2001.xmlschema.TopLevelSimpleType;

/**
 * Verifies the functions of the <code>UserSettings</code> class.
 */
public class TestXsdSimple extends TestOtmLibraryMemberBase<OtmXsdSimple> {
    private static Log log = LogFactory.getLog( TestXsdSimple.class );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null, null );
        subject = buildOtm( staticModelManager );
        baseObject = buildOtm( staticModelManager );
        baseObject.setName( "BaseSimple" );
    }


    /**
     * {@inheritDoc}
     * <p>
     * Must override the base assumption that object will have children because this is the only library member that
     * does not have children.
     * 
     * @see org.opentravel.model.otmLibraryMembers.TestOtmLibraryMemberBase#testChildrenOwner(org.opentravel.model.OtmChildrenOwner)
     */
    @Override
    public void testChildrenOwner(OtmChildrenOwner otm) {
        // NO-OP
    }

    /** ****************************************************** **/

    public static OtmXsdSimple buildOtm(OtmModelManager mgr) {
        OtmXsdSimple simple = new OtmXsdSimple( buildTL(), mgr );
        return simple;
    }

    public static OtmXsdSimple getString(OtmModelManager mgr) {
        return mgr.getIdType();
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
