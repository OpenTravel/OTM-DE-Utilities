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
import org.junit.Test;
import org.opentravel.model.otmFacets.OtmCustomFacet;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLProperty;

/**
 * Verifies the functions of the <code>OtmCustomFacet</code> class. Very minimal testing without contributed facets or
 * owning object.
 */
public class TestCustomFacet extends TestOtmLibraryMemberBase<OtmContextualFacet> {
    // private static Log log = LogFactory.getLog( TestContextualFacet.class );
    private static final String CF_NAME = "TestCF";


    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null );
        subject = buildOtm( staticModelManager );
        baseObject = buildOtm( staticModelManager );
        // baseObject.setName( "BaseCF" );
    }

    @Test
    public void testFacets() {}

    @Test
    public void testWhenContributed() {
        OtmBusinessObject bo = TestBusiness.buildOtm( staticModelManager );
        OtmContextualFacet cf = buildOtm( staticModelManager );
        // TODO

    }

    /**
     * @see org.opentravel.model.TestOtmLibraryMemberBase#testChildrenOwner(org.opentravel.model.OtmChildrenOwner)
     */
    @Override
    public void testChildrenOwner(OtmChildrenOwner otm) {
        // NO-OP - not contributed yet so it can't have children.
    }

    /** ****************************************************** **/

    /**
     * Build a custom facet. It will not have where contributed or children!
     * 
     * @param mgr
     * @return
     */
    public static OtmCustomFacet buildOtm(OtmModelManager mgr) {
        OtmCustomFacet custom = new OtmCustomFacet( buildTL(), mgr );
        assertNotNull( custom );
        // custom.getTL().addAttribute( new TLAttribute() );
        // custom.getTL().addElement( new TLProperty() );

        // Will only have children when contributed is modeled.
        return custom;
    }

    public static TLContextualFacet buildTL() {
        TLContextualFacet tlcf = new TLContextualFacet();
        tlcf.setName( CF_NAME );
        tlcf.setFacetType( TLFacetType.CUSTOM );
        tlcf.addAttribute( new TLAttribute() );
        tlcf.addElement( new TLProperty() );

        TLBusinessObject tlbo = TestBusiness.buildTL();
        // does NOT tell BO that it has custom facet - tlcf.setOwningEntity( tlbo );
        tlbo.addCustomFacet( tlcf );
        return tlcf;
    }
}
