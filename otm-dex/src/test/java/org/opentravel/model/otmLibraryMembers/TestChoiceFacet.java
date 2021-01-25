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
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmFacets.OtmChoiceFacet;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLProperty;

/**
 * Verifies the functions of the <code>OtmChoiceFacet</code> class. Very minimal testing without contributed facets or
 * owning object.
 */
public class TestChoiceFacet extends TestContextualFacet {
    // private static Log log = LogFactory.getLog( TestContextualFacet.class );
    public static final String CF_NAME = "TestCF";


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
        cf = buildOtm( staticModelManager, "AnotherCF" );
        contrib = (OtmContributedFacet) member.add( cf );
        testContributedFacet( contrib, cf, member );
    }

    @Test
    public void testInheritance() {

        OtmChoiceObject extension = TestChoice.buildOtm( staticModelManager );
        extension.setName( extension.getName() + "x" );
        extension.getChildrenContributedFacets().forEach( c -> c.setName( c.getName() + "x" ) );

        super.testCFInheritance( extension );
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

    public void addAndTest(OtmLibraryMember m, OtmLibrary lib) {
        assertTrue( "Add must return member.", lib.add( m ) == m );
        assertTrue( m.getLibrary() == lib );
        assertTrue( m.getModelManager().contains( m ) );
    }

    @Test
    public void testDeletingAsLibraryMember() {
        // Given - a Choice object and contextual facet
        OtmChoiceObject co1 = TestChoice.buildOtm( staticModelManager );
        OtmContextualFacet cf1 = buildOtm( staticModelManager );
        OtmContributedFacet contrib = co1.add( cf1 );

        // Given - a second choice object and contextual facet
        OtmChoiceObject co2 = TestChoice.buildOtm( staticModelManager );
        co2.setName( co2.getName() + "2" );
        OtmChoiceFacet cf2 = buildOtm( staticModelManager );
        OtmContributedFacet contrib2 = co2.add( cf2 );
        cf2.setName( cf2.getName() + "2" );

        // Given - a library for the objects
        OtmLibrary lib = TestLibrary.buildOtm( staticModelManager );
        addAndTest( co1, lib );
        addAndTest( co2, lib );
        addAndTest( cf1, lib );
        addAndTest( cf2, lib );
        //
        testContributedFacet( contrib, cf1, co1 );
        testContributedFacet( contrib2, cf2, co2 );

        // When deleted
        lib.delete( cf1 );
        assertFalse( "Library member must be removed from model manager.", cf1.getModelManager().contains( cf ) );
        assertFalse( "Choice object must not have the removed contributed facet.",
            co1.getChildren().contains( contrib ) );
        assertFalse( "TL Choice must not contain the tl Facet.",
            co1.getTL().getChoiceFacets().contains( cf1.getTL() ) );
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
     * Build a choice facet. It will not have where contributed or children! Contributed to a new choice object.
     * 
     * @param mgr
     * @param name
     * @return
     */
    public static OtmChoiceFacet buildOtm(OtmModelManager mgr, String name) {
        OtmChoiceFacet newFacet = buildOtm( mgr );
        newFacet.setName( name );
        return newFacet;
    }

    /**
     * Build a choice facet. It will not have where contributed or children! Contributed to a new choice object.
     * 
     * @param mgr
     * @return
     */
    public static OtmChoiceFacet buildOtm(OtmModelManager mgr) {
        OtmChoiceFacet choice = new OtmChoiceFacet( buildTL(), mgr );
        assertNotNull( choice );
        mgr.add( choice );

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
