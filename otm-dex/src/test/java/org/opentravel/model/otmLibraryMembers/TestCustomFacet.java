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
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmCustomFacet;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLProperty;

import java.util.List;

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
    public void testInheritance() {
        OtmBusinessObject baseBo = TestBusiness.buildOtm( staticModelManager );
        baseBo.setName( "BaseBO" );
        OtmContextualFacet inheritedCf = buildOtm( staticModelManager, baseBo );
        assertTrue( "Given", !inheritedCf.isInherited() );

        OtmBusinessObject bo = TestBusiness.buildOtm( staticModelManager );
        OtmContextualFacet cf = buildOtm( staticModelManager, bo );
        bo.setName( "SubType" );
        assertTrue( "Given", !cf.isInherited() );

        // When - bo extends baseBo
        bo.setBaseType( baseBo );
        assertTrue( "Given", bo.getBaseType() == baseBo );
        assertTrue( "Given", bo.getTL().getExtension() != null );
        assertTrue( "Given", bo.getTL().getExtension().getExtendsEntity() == baseBo.getTL() );

        // Then
        List<OtmObject> ic1 = bo.getInheritedChildren();
        List<OtmObject> ic2 = baseBo.getInheritedChildren();
        // assertTrue( "Extension must have inherited CF", bo.getInheritedChildren().contains( inheritedCf ) );
    }

    @Test
    public void testFacets() {}

    @Test
    public void testWhenContributed() {
        // Given - a business object and contextual facet
        OtmBusinessObject bo = TestBusiness.buildOtm( staticModelManager );
        OtmContextualFacet cf = buildOtm( staticModelManager );
        assertTrue( "Has not been injected yet.", cf.getWhereContributed() == null );

        // When added
        OtmContributedFacet contrib = bo.add( cf );
        // Then (lazy evaluation)
        assertTrue( "Contributor must be owned by Business Object.", contrib.getOwningMember() == bo );
        assertTrue( "Business object has contributor child.", bo.getChildren().contains( contrib ) );
        assertTrue( "Contributor linked to contextual facet.", contrib.getContributor() == cf );
        assertTrue( "Contextual facet knows where it is contributed.", cf.getWhereContributed() == contrib );
    }

    /**
     * @see org.opentravel.model.otmLibraryMembers.TestOtmLibraryMemberBase#testChildrenOwner(org.opentravel.model.OtmChildrenOwner)
     */
    @Override
    public void testChildrenOwner(OtmChildrenOwner otm) {
        // NO-OP - not contributed yet so it can't have children.
    }

    /** ****************************************************** **/

    /**
     * Create custom facet and contribute it to the passed business object.
     * 
     * @param modelManager
     * @param bo
     * @return
     */
    private static OtmContextualFacet buildOtm(OtmModelManager modelManager, OtmBusinessObject bo) {
        OtmContextualFacet cf = buildOtm( modelManager );
        bo.add( cf );
        return cf;
    }

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
