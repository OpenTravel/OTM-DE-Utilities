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
import org.opentravel.schemacompiler.model.AbstractLibrary;
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
    // private static Logger log = LogManager.getLogger( TestContextualFacet.class );
    public static final String CF_NAME = "TestCF";


    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null, null );
        staticLib = TestLibrary.buildOtm( staticModelManager );

        // Needed for library member tests
        subject = buildOtm( staticLib );
        baseObject = buildOtm( staticLib );
    }

    @Before
    public void beforeTest() {
        member = TestChoice.buildOtm( staticModelManager );
        staticLib = TestLibrary.buildOtm( staticModelManager );
        subject = buildOtm( staticLib );
        baseObject = buildOtm( staticLib );
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
        // Given - a cf contributed to a choice
        OtmChoiceObject co = TestChoice.buildOtm( staticLib, "FirstChoice" );
        OtmChoiceObject co2 = TestChoice.buildOtm( staticLib, "SecondChoice" );
        OtmContextualFacet cf = buildOtm( co, "CHF1" );
        OtmContributedFacet contrib = cf.getWhereContributed();
        testContributedFacet( contrib, cf, co );

        // When base type changed (moved)
        cf.setBaseType( co2 );
        OtmContributedFacet newContrib = cf.getWhereContributed();
        // Then
        assertTrue( "Old object must NOT have new contributor as child.", !co.getChildren().contains( newContrib ) );
        assertTrue( "Old object must NOT have old contributor as child.", !co.getChildren().contains( contrib ) );
        assertTrue( "New object must have child.", co2.getChildren().contains( newContrib ) );
        assertTrue( "getBaseType needs correct TL owner.", cf.getTL().getOwningEntity() == co2.getTL() );
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
        OtmChoiceObject co = TestChoice.buildOtm( staticLib, "TestCH" );
        OtmContextualFacet cf = buildOtm( co, "CHF1" );

        // OtmChoiceObject co = TestChoice.buildOtm( staticModelManager );
        // OtmContextualFacet cf = buildOtm( staticModelManager );

        // When added
        OtmContributedFacet contrib = co.add( cf );
        // Then (lazy evaluation)
        testContributedFacet( contrib, cf, co );
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
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmChoiceObject co1 = TestChoice.buildOtm( lib, "CH1" );
        OtmContextualFacet cf1 = buildOtm( co1, "CHF1" );
        OtmContributedFacet contrib = cf1.getWhereContributed();
        // OtmContributedFacet contrib = co1.add( cf1 );

        // Given - a second choice object and contextual facet
        OtmChoiceObject co2 = TestChoice.buildOtm( lib, "CH2" );
        // co2.setName( co2.getName() + "2" );
        OtmChoiceFacet cf2 = buildOtm( co2, "CHF2" );
        OtmContributedFacet contrib2 = cf2.getWhereContributed();
        // OtmContributedFacet contrib2 = co2.add( cf2 );
        // cf2.setName( cf2.getName() + "2" );

        // Given - a library for the objects
        // OtmLibrary lib = TestLibrary.buildOtm( staticModelManager );
        // addAndTest( co1, lib );
        // addAndTest( co2, lib );
        // addAndTest( cf1, lib );
        // addAndTest( cf2, lib );
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
     * @param co
     * @param name
     * @return
     */
    public static OtmChoiceFacet buildOtm(OtmChoiceObject co, String name) {
        assertTrue( "Builder - parameter must have model manager.", co.getModelManager() != null );
        assertTrue( "Builder - parameter must have library.", co.getLibrary() != null );

        // OtmLibrary#add() uses factory
        TLContextualFacet tlCF = buildTL( co.getLibrary().getTL(), name );
        OtmChoiceFacet choice = (OtmChoiceFacet) OtmLibraryMemberFactory.create( tlCF, co.getModelManager() );
        // Contribute the custom to the business object
        OtmContributedFacet contrib = co.add( choice );

        assertTrue( "Builder - new facet must have library.", choice.getLibrary() != null );
        assertTrue( "Builder - new facet must have model manager.", choice.getModelManager() != null );
        assertTrue( "Builder - new facet must be managed.", choice.getModelManager().getMembers().contains( choice ) );
        assertTrue( "Builder - new facet must have contributor.", choice.getWhereContributed() != null );
        assertTrue( "Builder - new facet must get correct contributor.", choice.getWhereContributed() == contrib );

        TestContextualFacet.testContributedFacet( choice.getWhereContributed(), choice, co );
        TestContextualFacet.testContributedFacet( contrib, choice, co );
        return choice;
    }

    // /**
    // * Create Choice facet and add() it to the passed Choice object. Creates contributor.
    // * <p>
    // * No tests!
    // *
    // * @param modelManager
    // * @param co
    // * @return
    // */
    // public static OtmChoiceFacet buildOtm(OtmModelManager modelManager, OtmChoiceObject co) {
    // OtmChoiceFacet cf = buildOtm( modelManager );
    // co.add( cf );
    // return cf;
    // }

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
     * Build a choice facet and add to manager.
     * <p>
     * It is not in a library. It will not have where contributed or children!
     * 
     * @param mgr
     * @return
     */
    public static OtmChoiceFacet buildOtm(OtmLibrary lib) {
        assertTrue( "Builder - parameter must have library.", lib != null );
        assertTrue( "Builder - parameter must have model manager.", lib.getModelManager() != null );
        assertTrue( "Builder - libary must be in model.", lib.getModelManager().contains( lib.getTL() ) );

        // Create a unique name
        String name = "TestCF";
        name = OtmLibraryMemberFactory.getUniqueName( lib, name );

        OtmChoiceFacet choice = new OtmChoiceFacet( buildTL( lib.getTL(), name ), lib.getModelManager() );
        // Already added to library
        lib.getModelManager().add( choice );

        assertTrue( "Builder: ", lib.getMembers().contains( choice ) );
        assertNotNull( choice );
        return choice;
    }

    /**
     * Build a choice facet and add to manager.
     * <p>
     * It is not in a library. It will not have where contributed or children!
     * 
     * @param mgr
     * @return
     */
    public static OtmChoiceFacet buildOtm(OtmModelManager mgr) {
        assertTrue( "Builder - parameter must have model manager.", mgr != null );
        OtmChoiceFacet choice = new OtmChoiceFacet( buildTL(), mgr );
        mgr.add( choice );

        assertNotNull( choice );
        return choice;
    }


    public static TLContextualFacet buildTL() {
        return buildTL( null, null, CF_NAME );
    }

    /**
     * Build TL facet and add to abstract library.
     * 
     * @param abstractLibrary
     * @param name
     * @return
     */
    public static TLContextualFacet buildTL(AbstractLibrary abstractLibrary, String name) {
        return buildTL( abstractLibrary, null, name );
    }

    /**
     * Build TL facet and add to abstract library.
     * 
     * @param abstractLibrary
     * @param tlCO
     * @param name
     * @return
     */
    public static TLContextualFacet buildTL(AbstractLibrary abstractLibrary, TLChoiceObject tlCO, String name) {
        TLContextualFacet tlcf = new TLContextualFacet();
        tlcf.setName( name );
        tlcf.setFacetType( TLFacetType.CHOICE );
        tlcf.addAttribute( new TLAttribute() );
        tlcf.addElement( new TLProperty() );

        if (abstractLibrary != null)
            abstractLibrary.addNamedMember( tlcf );

        // // setOwningEntity does NOT tell CO that it has choice facet - tlcf.setOwningEntity( tlbo );
        if (tlCO != null)
            tlCO.addChoiceFacet( tlcf );

        return tlcf;
    }

}
