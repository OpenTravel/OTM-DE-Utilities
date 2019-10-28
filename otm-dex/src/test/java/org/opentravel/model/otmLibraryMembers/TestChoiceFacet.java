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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmFacets.OtmChoiceFacet;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLProperty;

import java.util.List;

/**
 * Verifies the functions of the <code>OtmChoiceFacet</code> class. Very minimal testing without contributed facets or
 * owning object.
 */
public class TestChoiceFacet extends TestContextualFacet {
    // private static Log log = LogFactory.getLog( TestContextualFacet.class );
    private static final String CF_NAME = "TestCF";


    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null );
        // Needed for library member tests
        subject = buildOtm( staticModelManager );
        baseObject = buildOtm( staticModelManager );
    }

    @Before
    public void beforeTest() {
        member = TestChoice.buildOtm( staticModelManager );
        cf = buildOtm( staticModelManager );
        contrib = (OtmContributedFacet) member.add( cf );
        testContributedFacet( contrib, cf, member );
    }

    @Test
    public void testInheritance() {
        OtmChoiceObject baseBo = TestChoice.buildOtm( staticModelManager );
        baseBo.setName( "BaseBO" );
        OtmContextualFacet inheritedCf = buildOtm( staticModelManager, baseBo );
        assertTrue( "Given", !inheritedCf.isInherited() );

        OtmChoiceObject bo = TestChoice.buildOtm( staticModelManager );
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
        OtmChoiceObject co = TestChoice.buildOtm( staticModelManager );
        OtmChoiceObject co2 = TestChoice.buildOtm( staticModelManager );
        co2.setName( "TheOtherBO" );
        OtmContextualFacet cf = buildOtm( staticModelManager );
        OtmContributedFacet contrib = co.add( cf );
        testContributedFacet( contrib, cf, co );

        // When base type changed (moved)
        cf.setBaseType( co2 );
        OtmContributedFacet newContrib = cf.getWhereContributed();
        // Then
        assertTrue( cf.getBaseType() == co2 );
        assertTrue( contrib.getParent() == co2 );
        assertTrue( newContrib == contrib );
        assertTrue( cf.getWhereContributed() == newContrib );
        assertTrue( contrib.getChildren().size() == newContrib.getChildren().size() );
        testContributedFacet( cf.getWhereContributed(), cf, co2 );
    }

    @Test
    public void testWhenContributed() {
        // Given - a Choice object and contextual facet
        OtmChoiceObject bo = TestChoice.buildOtm( staticModelManager );
        OtmContextualFacet cf = buildOtm( staticModelManager );

        // When added
        OtmContributedFacet contrib = bo.add( cf );
        // Then (lazy evaluation)
        testContributedFacet( contrib, cf, bo );
    }


    @Test
    public void testDeleting() {
        super.testDeleteFromMember();
        assertFalse( ((TLChoiceObject) member.getTL()).getChoiceFacets().contains( cf.getTL() ) );
    }

    @Test
    public void testDeletingChildren() {
        super.testDeletingChildren();
    }

    @Test
    public void testDeletingWithContributedFacet() {
        // Given - a Choice object and contextual facet
        OtmChoiceObject co = TestChoice.buildOtm( staticModelManager );
        OtmContextualFacet cf = buildOtm( staticModelManager );
        OtmContributedFacet contrib = co.add( cf );
        testContributedFacet( contrib, cf, co );

        OtmContextualFacet cf2 = buildOtm( staticModelManager );
        testContributedFacet( contrib, cf, co );

        OtmContributedFacet contrib2 = co.add( cf2 );
        // testContributedFacet( contrib2, cf2, bo ); // Performs lazy-eval on contributor
        // assertTrue( "Lazy-evaluation on contributor.", contrib2.getContributor() == cf2 );
        assertTrue( "Contextual facet knows where it is contributed.", cf2.getWhereContributed() == contrib2 );
        assertTrue( contrib.getContributor() == cf );

        testContributedFacet( contrib, cf, co );
        assertTrue( co.getChildren().contains( contrib ) );
        assertTrue( co.getChildren().contains( contrib2 ) );
        assertTrue( co.getTL().getChoiceFacets().contains( cf.getTL() ) );
        assertTrue( co.getTL().getChoiceFacets().contains( cf2.getTL() ) );

        // When deleted
        co.delete( contrib );
        assertFalse( co.getChildren().contains( contrib ) );
        assertFalse( co.getTL().getChoiceFacets().contains( cf.getTL() ) );
        co.delete( contrib2 );
        assertFalse( co.getChildren().contains( contrib2 ) );
        assertFalse( co.getTL().getChoiceFacets().contains( cf2.getTL() ) );
    }

    @Test
    public void testDeletingAsLibraryMember() {
        // Given - a Choice object and contextual facet
        OtmChoiceObject co1 = TestChoice.buildOtm( staticModelManager );
        OtmContextualFacet cf = buildOtm( staticModelManager );
        OtmContributedFacet contrib = co1.add( cf );
        // Given - a choice object and contextual facet
        OtmChoiceObject co2 = TestChoice.buildOtm( staticModelManager );
        OtmChoiceFacet cf2 = buildOtm( staticModelManager );
        OtmContributedFacet contrib2 = co2.add( cf2 );

        // Given - a library for the objects
        OtmLibrary lib = TestLibrary.buildOtm( staticModelManager );
        lib.add( co1 );
        lib.add( co2 );
        lib.add( cf );
        lib.add( cf2 );
        assertTrue( cf.getLibrary() != null );
        assertTrue( cf.getModelManager().contains( cf ) );
        //
        testContributedFacet( contrib, cf, co1 );
        testContributedFacet( contrib2, cf2, co2 );

        // When deleted
        lib.delete( cf );
        assertFalse( cf.getModelManager().contains( cf ) );
        assertFalse( co1.getChildren().contains( contrib ) );
        assertFalse( co1.getTL().getChoiceFacets().contains( cf.getTL() ) );
        //
        lib.delete( cf2 );
        assertFalse( co2.getChildren().contains( contrib2 ) );
        assertFalse( co2.getTL().getChoiceFacets().contains( cf2.getTL() ) );
    }


    /**
     * @see org.opentravel.model.otmLibraryMembers.TestOtmLibraryMemberBase#testChildrenOwner(org.opentravel.model.OtmChildrenOwner)
     */
    @Override
    public void testChildrenOwner(OtmChildrenOwner otm) {
        if (member == null)
            beforeTest(); // may be invoked from test library member
        super.testAdd();
    }

    /** ****************************************************** **/

    /**
     * Create Choice facet and contribute it to the passed Choice object.
     * 
     * @param modelManager
     * @param bo
     * @return
     */
    public static OtmChoiceFacet buildOtm(OtmModelManager modelManager, OtmChoiceObject bo) {
        OtmChoiceFacet cf = buildOtm( modelManager );
        bo.add( cf );
        return cf;
    }

    /**
     * Build a choice facet. It will not have where contributed or children!
     * 
     * @param mgr
     * @return
     */
    public static OtmChoiceFacet buildOtm(OtmModelManager mgr) {
        OtmChoiceFacet choice = new OtmChoiceFacet( buildTL(), mgr );
        assertNotNull( choice );
        // choice.getTL().addAttribute( new TLAttribute() );
        // choice.getTL().addElement( new TLProperty() );

        // Will only have children when contributed is modeled.
        return choice;
    }


    public static TLContextualFacet buildTL() {
        TLContextualFacet tlcf = new TLContextualFacet();
        tlcf.setName( CF_NAME );
        tlcf.setFacetType( TLFacetType.CHOICE );
        tlcf.addAttribute( new TLAttribute() );
        tlcf.addElement( new TLProperty() );

        TLChoiceObject tlbo = TestChoice.buildTL();
        // does NOT tell BO that it has Choice facet - tlcf.setOwningEntity( tlbo );
        tlbo.addChoiceFacet( tlcf );
        return tlcf;
    }
}
