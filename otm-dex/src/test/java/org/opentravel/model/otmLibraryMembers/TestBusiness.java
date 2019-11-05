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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmProperties.OtmElement;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLProperty;

/**
 * Verifies the functions of the <code>OtmBusinessObject</code> class.
 */
public class TestBusiness extends TestOtmLibraryMemberBase<OtmBusinessObject> {
    private static String BoName = "Testbo";

    private static Log log = LogFactory.getLog( TestBusiness.class );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null );
        subject = buildOtm( staticModelManager );
        baseObject = buildOtm( staticModelManager );
        baseObject.setName( "BaseBO" );
    }

    @Test
    public void testFacets() {
        OtmBusinessObject bo = buildOtm( staticModelManager );

        assertNotNull( bo.getSummary() );
        assertNotNull( bo.getDetail() );
    }

    /** ****************************************************** **/
    /**
     * Build business object with attribute and element in ID and Summary facets.
     * 
     * @param mgr
     * @param name
     * @return
     */
    public static OtmBusinessObject buildOtm(OtmModelManager mgr, String name) {
        BoName = name;
        return buildOtm( mgr );
    }

    /**
     * Get an element from the summary facet
     * 
     * @param member
     * @return
     */
    public static OtmElement<?> getElement(OtmBusinessObject member) {
        for (OtmObject child : member.getSummary().getChildren())
            if (child instanceof OtmElement)
                return (OtmElement<?>) child;
        return null;
    }

    public static OtmBusinessObject buildOtm(OtmModelManager mgr) {
        OtmBusinessObject bo = new OtmBusinessObject( buildTL(), mgr );
        assertNotNull( bo );
        mgr.add( bo );

        // TestCustomFacet.buildOtm( staticModelManager );
        assertTrue( bo.getChildren().size() > 2 );
        assertTrue( bo.getSummary().getChildren().size() == 2 );
        assertTrue( "Must have identity listener.", OtmModelElement.get( bo.getTL() ) == bo );
        return bo;
    }

    public static TLBusinessObject buildTL() {
        TLBusinessObject tlbo = new TLBusinessObject();
        tlbo.setName( BoName );
        TLAttribute tla = new TLAttribute();
        tla.setName( "idAttr_" + BoName );
        buildExample( tla );
        tlbo.getIdFacet().addAttribute( tla );

        TLProperty tlp = new TLProperty();
        tlp.setName( "idProp_" + BoName );
        buildExample( tlp );
        tlbo.getIdFacet().addElement( tlp );

        tla = new TLAttribute();
        tla.setName( "sumAttr_" + BoName );
        tlbo.getSummaryFacet().addAttribute( tla );
        tlp = new TLProperty();
        tlp.setName( "sumProp_" + BoName );
        tlbo.getSummaryFacet().addElement( tlp );
        return tlbo;
    }

    public static void buildExample(TLExampleOwner tleo) {
        TLExample tle = new TLExample();
        tle.setValue( "ExampleValue123" );
        tleo.addExample( tle );
        assertTrue( tleo.getExamples().size() >= 1 );
    }
}
