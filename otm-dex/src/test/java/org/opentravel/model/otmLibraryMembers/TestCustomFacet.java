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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmFacets.OtmChoiceFacet;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmCustomFacet;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLModelElement;
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
    public void testMovingFacet() {
        // Given - a cf contributed to a bo
        OtmBusinessObject bo = TestBusiness.buildOtm( staticModelManager );
        OtmBusinessObject bo2 = TestBusiness.buildOtm( staticModelManager );
        bo2.setName( "TheOtherBO" );
        OtmContextualFacet cf = buildOtm( staticModelManager );
        assertTrue( "Has not been injected yet.", cf.getWhereContributed() == null );
        OtmContributedFacet contrib = bo.add( cf );
        testContributedFacet( contrib, cf, bo );

        // When base type changed (moved)
        cf.setBaseType( bo2 );
        OtmContributedFacet newContrib = cf.getWhereContributed();
        // Then
        assertTrue( cf.getBaseType() == bo2 );
        assertTrue( contrib.getParent() == bo2 );
        assertTrue( newContrib == contrib );
        assertTrue( cf.getWhereContributed() == newContrib );
        assertTrue( contrib.getChildren().size() == newContrib.getChildren().size() );
        testContributedFacet( cf.getWhereContributed(), cf, bo2 );
    }

    @Test
    public void testWhenContributed() {
        // Given - a business object and contextual facet
        OtmBusinessObject bo = TestBusiness.buildOtm( staticModelManager );
        OtmContextualFacet cf = buildOtm( staticModelManager );
        assertTrue( "Has not been injected yet.", cf.getWhereContributed() == null );

        // When added
        OtmContributedFacet contrib = bo.add( cf );
        // Then (lazy evaluation)
        testContributedFacet( contrib, cf, bo );
    }


    @Test
    public void testDeleting() {
        // Given - a business object and contextual facet
        OtmBusinessObject bo = TestBusiness.buildOtm( staticModelManager );
        OtmContextualFacet cf = buildOtm( staticModelManager );
        OtmContributedFacet contrib = bo.add( cf );
        testContributedFacet( contrib, cf, bo );

        OtmContextualFacet cf2 = buildOtm( staticModelManager );
        testContributedFacet( contrib, cf, bo );

        OtmContributedFacet contrib2 = bo.add( cf2 );
        // testContributedFacet( contrib2, cf2, bo ); // Performs lazy-eval on contributor
        // assertTrue( "Lazy-evaluation on contributor.", contrib2.getContributor() == cf2 );
        assertTrue( "Contextual facet knows where it is contributed.", cf2.getWhereContributed() == contrib2 );
        assertTrue( contrib.getContributor() == cf );

        testContributedFacet( contrib, cf, bo );
        assertTrue( bo.getChildren().contains( contrib ) );
        assertTrue( bo.getChildren().contains( contrib2 ) );
        assertTrue( bo.getTL().getCustomFacets().contains( cf.getTL() ) );
        assertTrue( bo.getTL().getCustomFacets().contains( cf2.getTL() ) );

        // When deleted
        bo.delete( cf );
        assertFalse( bo.getChildren().contains( contrib ) );
        assertFalse( bo.getTL().getCustomFacets().contains( cf.getTL() ) );
        bo.delete( cf2 );
        assertFalse( bo.getChildren().contains( contrib2 ) );
        assertFalse( bo.getTL().getCustomFacets().contains( cf2.getTL() ) );
    }

    @Test
    public void testDeletingWithContributedFacet() {
        // Given - a business object and contextual facet
        OtmBusinessObject bo = TestBusiness.buildOtm( staticModelManager );
        OtmContextualFacet cf = buildOtm( staticModelManager );
        OtmContributedFacet contrib = bo.add( cf );
        testContributedFacet( contrib, cf, bo );

        OtmContextualFacet cf2 = buildOtm( staticModelManager );
        testContributedFacet( contrib, cf, bo );

        OtmContributedFacet contrib2 = bo.add( cf2 );
        // testContributedFacet( contrib2, cf2, bo ); // Performs lazy-eval on contributor
        // assertTrue( "Lazy-evaluation on contributor.", contrib2.getContributor() == cf2 );
        assertTrue( "Contextual facet knows where it is contributed.", cf2.getWhereContributed() == contrib2 );
        assertTrue( contrib.getContributor() == cf );

        testContributedFacet( contrib, cf, bo );
        assertTrue( bo.getChildren().contains( contrib ) );
        assertTrue( bo.getChildren().contains( contrib2 ) );
        assertTrue( bo.getTL().getCustomFacets().contains( cf.getTL() ) );
        assertTrue( bo.getTL().getCustomFacets().contains( cf2.getTL() ) );

        // When deleted
        bo.delete( contrib );
        assertFalse( bo.getChildren().contains( contrib ) );
        assertFalse( bo.getTL().getCustomFacets().contains( cf.getTL() ) );
        bo.delete( contrib2 );
        assertFalse( bo.getChildren().contains( contrib2 ) );
        assertFalse( bo.getTL().getCustomFacets().contains( cf2.getTL() ) );
    }

    @Test
    public void testDeletingAsLibraryMember() {
        // Given - a business object and contextual facet
        OtmBusinessObject bo = TestBusiness.buildOtm( staticModelManager );
        OtmContextualFacet cf = buildOtm( staticModelManager );
        OtmContributedFacet contrib = bo.add( cf );
        // Given - a choice object and contextual facet
        OtmChoiceObject co = TestChoice.buildOtm( staticModelManager );
        OtmChoiceFacet cf2 = buildOtmChoice( staticModelManager );
        OtmContributedFacet contrib2 = co.add( cf2 );

        // Given - a library for the objects
        OtmLibrary lib = TestLibrary.buildOtm( staticModelManager );
        lib.add( bo );
        lib.add( co );
        lib.add( cf );
        lib.add( cf2 );
        assertTrue( cf.getLibrary() != null );
        assertTrue( cf.getModelManager().contains( cf ) );
        //
        testContributedFacet( contrib, cf, bo );
        testContributedFacet( contrib2, cf2, co );

        // When deleted
        lib.delete( cf );
        assertFalse( cf.getModelManager().contains( cf ) );
        assertFalse( bo.getChildren().contains( contrib ) );
        assertFalse( bo.getTL().getCustomFacets().contains( cf.getTL() ) );
        //
        lib.delete( cf2 );
        assertFalse( co.getChildren().contains( contrib2 ) );
        assertFalse( co.getTL().getChoiceFacets().contains( cf2.getTL() ) );
    }


    public void testContributedFacet(OtmContributedFacet contrib, OtmContextualFacet cf, OtmLibraryMember lm) {
        assertTrue( "Contributor must be owned by Library member.", contrib.getOwningMember() == lm );
        assertTrue( " Contextual facet must have contributed owner.", cf.getContributedObject() == lm );
        assertTrue( "Library member has contributor child.", lm.getChildren().contains( contrib ) );
        assertTrue( "Contributor linked to contextual facet.", contrib.getContributor() == cf );
        assertTrue( "Contextual facet knows where it is contributed.", cf.getWhereContributed() == contrib );
        assertTrue( "Both facets have same TL facet", cf.getTL() == contrib.getTL() );
        assertTrue( "TL is a TLContextual facet", cf.getTL() instanceof TLContextualFacet );

        // Verify the contributed owner is the same as the TL contextual facet's owner
        if (cf.getTL().getOwningEntity() != null && cf.getWhereContributed() != null)
            assertTrue( lm == OtmModelElement.get( (TLModelElement) cf.getTL().getOwningEntity() ) );
        assertTrue( contrib.getParent() == lm );
        assertTrue( contrib.getActionManager() != null );
        assertTrue( contrib.getModelManager() != null );
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

    /**
     * Build a choice facet. It will not have where contributed or children!
     * 
     * @param mgr
     * @return
     */
    public static OtmChoiceFacet buildOtmChoice(OtmModelManager mgr) {
        OtmChoiceFacet choice = new OtmChoiceFacet( buildTLChoice(), mgr );
        assertNotNull( choice );
        // choice.getTL().addAttribute( new TLAttribute() );
        // choice.getTL().addElement( new TLProperty() );

        // Will only have children when contributed is modeled.
        return choice;
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

    public static TLContextualFacet buildTLChoice() {
        TLContextualFacet tlcf = new TLContextualFacet();
        tlcf.setName( CF_NAME );
        tlcf.setFacetType( TLFacetType.CHOICE );
        tlcf.addAttribute( new TLAttribute() );
        tlcf.addElement( new TLProperty() );

        TLChoiceObject tlbo = TestChoice.buildTL();
        // does NOT tell BO that it has custom facet - tlcf.setOwningEntity( tlbo );
        tlbo.addChoiceFacet( tlcf );
        return tlcf;
    }
}
