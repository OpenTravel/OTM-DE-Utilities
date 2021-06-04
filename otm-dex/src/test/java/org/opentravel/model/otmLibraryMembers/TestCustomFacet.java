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
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmCustomFacet;
import org.opentravel.model.otmFacets.OtmFacetFactory;
import org.opentravel.schemacompiler.model.AbstractLibrary;
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
public class TestCustomFacet extends TestContextualFacet {
    // Extends TestOtmLibraryMemberBase
    // private static Log log = LogFactory.getLog( TestContextualFacet.class );
    private static final String CF_NAME = "TestCF";


    @BeforeClass
    public static void beforeClass() {
        staticLib = TestLibrary.buildOtm();
        staticModelManager = staticLib.getModelManager();
        subject = buildOtm( staticLib, "SubjectCF" );
        baseObject = buildOtm( staticLib, "BaseCF" );
    }

    @Before
    public void beforeMethods() {
        staticModelManager.clear();

        staticLib = TestLibrary.buildOtm();
        subject = buildOtm( staticLib, "SubjectCF" );
        baseObject = buildOtm( staticLib, "BaseCF" );
    }

    /** ******************* Static Builders **************************************/

    /**
     * Build a custom facet. It will not have where contributed or children!
     * 
     * @param mgr
     * @return
     */
    private static OtmCustomFacet buildOtm(OtmModelManager mgr) {
        OtmCustomFacet custom = new OtmCustomFacet( buildTL(), mgr );
        assertNotNull( custom );
        mgr.add( custom );
        return custom;
    }

    /**
     * Build a custom facet and add to library. It will not have where contributed or children!
     * 
     * @param mgr
     * @return
     */
    public static OtmCustomFacet buildOtm(OtmLibrary lib, String name) {
        assertTrue( "Builder - must not already have member with name.", lib.getMember( name ) == null );
        OtmCustomFacet custom = buildOtm( lib.getModelManager() );
        assertNotNull( custom );
        custom.setName( name );
        lib.add( custom );

        assertTrue( "Builder error: wrong library", custom.getLibrary() == lib );

        // Will only have children when contributed is modeled.
        return custom;
    }

    /**
     * Create tested custom facet and contribute it to the passed business object.
     * 
     * @param name
     * @param bo must have model manager
     * @return
     */
    public static OtmCustomFacet buildOtm(OtmBusinessObject bo, String name) {
        assertTrue( "Illegal arguement - must have model manager.", bo.getModelManager() != null );
        assertTrue( "Illegal arguement - must have library.", bo.getLibrary() != null );

        // OtmLibrary#add() uses factory
        TLContextualFacet tlCF = buildTL( bo.getLibrary().getTL(), name );
        OtmCustomFacet custom = (OtmCustomFacet) OtmLibraryMemberFactory.create( tlCF, bo.getModelManager() );
        // Contribute the custom to the business object
        OtmContributedFacet contrib = bo.add( custom );

        assertTrue( "Builder - new facet must get correct contributor.", custom.getWhereContributed() == contrib );
        testContributedFacet( custom.getWhereContributed(), custom, bo );
        return custom;
    }

    /**
     * Create tested custom facet and contribute it to the passed custom facet.
     * 
     * @param name
     * @param bcf custom facet must have model manager
     * @return
     */
    public static OtmCustomFacet buildOtm(OtmCustomFacet bcf, String name) {
        assertTrue( "Illegal arguement - must have model manager.", bcf.getModelManager() != null );
        TLContextualFacet tl = buildTL( null, null, name );

        OtmLibraryMember cf2 = OtmFacetFactory.create( tl, bcf.getModelManager() );
        assertTrue( cf2 != null ); // custom facet, no where contributed
        assertTrue( cf2 instanceof OtmCustomFacet );
        OtmCustomFacet cf = (OtmCustomFacet) cf2;
        cf.setBaseType( bcf );
        if (bcf.getLibrary() != null)
            bcf.getLibrary().add( cf );

        testContributedFacet( cf.getWhereContributed(), cf, bcf );
        return cf;
    }

    /**
     * Create TLCustomFacet and add to newly created TLBusiness
     * 
     * @return
     */
    public static TLContextualFacet buildTL() {
        return buildTL( null, null, CF_NAME );
    }

    public static TLContextualFacet buildTL(AbstractLibrary abstractLibrary, String name) {
        return buildTL( abstractLibrary, null, name );
    }

    /**
     * Create facet. set name, facet type, add attribute and element
     * 
     * @param abstractLibrary - add facet to library if not null
     * @param tlBO add facet to BO if not null
     * @param name
     * @return
     */
    public static TLContextualFacet buildTL(AbstractLibrary abstractLibrary, TLBusinessObject tlBO, String name) {
        TLContextualFacet tlcf = new TLContextualFacet();
        tlcf.setName( name );
        tlcf.setFacetType( TLFacetType.CUSTOM );
        tlcf.addAttribute( new TLAttribute() );
        tlcf.addElement( new TLProperty() );

        if (abstractLibrary != null)
            abstractLibrary.addNamedMember( tlcf );

        // // setOwningEntity does NOT tell BO that it has custom facet - tlcf.setOwningEntity( tlbo );
        if (tlBO != null)
            tlBO.addCustomFacet( tlcf );

        return tlcf;
    }

    /** ****************************************************** **/

    @Before
    public void beforeTest() {
        staticLib = TestLibrary.buildOtm( staticModelManager );
        member = TestBusiness.buildOtm( staticLib, "BeforeBO" );
        cf = buildOtm( (OtmBusinessObject) member, "CF1" );
        contrib = (OtmContributedFacet) member.add( cf );
        testContributedFacet( contrib, cf, member );
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


    /**
     * ******************************************************
     * 
     * Facet Specific Tests
     * 
     **/
    @Test
    public void testDeleting() {
        super.testDeleteFromMember();
        assertFalse( ((TLBusinessObject) member.getTL()).getCustomFacets().contains( cf.getTL() ) );
    }

    @Test
    public void testDeletingAsLibraryMember() {
        // Given - a business object and contextual facet
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmBusinessObject bo = TestBusiness.buildOtm( lib, "TestBO" );
        OtmCustomFacet cf = buildOtm( bo, "CF1" );
        OtmContributedFacet contrib = cf.getWhereContributed();

        // Given - a 2nd custom facet
        OtmBusinessObject bo2 = TestBusiness.buildOtm( lib, "TestBO2" );
        OtmCustomFacet cf2 = buildOtm( bo2, "CF2" );
        OtmContributedFacet contrib2 = cf2.getWhereContributed();

        // Givens Tests
        assertTrue( cf.getLibrary() != null );
        assertTrue( cf.getModelManager().contains( cf ) );
        testContributedFacet( contrib, cf, bo );
        testContributedFacet( contrib2, cf2, bo2 );

        // When deleted
        lib.delete( cf );
        assertFalse( cf.getModelManager().contains( cf ) );
        assertFalse( bo.getChildren().contains( contrib ) );
        assertFalse( bo.getTL().getCustomFacets().contains( cf.getTL() ) );
        //
        lib.delete( cf2 );
        assertFalse( bo2.getChildren().contains( contrib2 ) );
        assertFalse( bo2.getTL().getCustomFacets().contains( cf2.getTL() ) );
    }

    @Test
    public void testDeletingChildren() {
        super.testDeletingChildren();
    }

    @Test
    public void testDeletingWithContributedFacet() {
        // Given - a business object and contextual facet
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmBusinessObject bo = TestBusiness.buildOtm( lib, "TestBO" );
        OtmContextualFacet cf = buildOtm( bo, "CF1" );
        OtmContributedFacet contrib = cf.getWhereContributed();
        testContributedFacet( contrib, cf, bo );

        OtmContextualFacet cf2 = buildOtm( bo, "CF2" );
        OtmContributedFacet contrib2 = cf2.getWhereContributed();

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
    public void testFacets() {}

    @Test
    public void testInheritance() {
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmBusinessObject baseBo = TestBusiness.buildOtm( lib, "BaseBO" );
        OtmContextualFacet inheritedCf = buildOtm( baseBo, "CF1" );
        assertTrue( "Given", !inheritedCf.isInherited() );

        OtmBusinessObject bo = TestBusiness.buildOtm( lib, "TestBO" );
        OtmContextualFacet cf = buildOtm( bo, "SubType" );
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
    public void testMovingFacet() {
        // Given - a cf contributed to a bo
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmBusinessObject bo = TestBusiness.buildOtm( lib, "TheBO" );
        OtmBusinessObject bo2 = TestBusiness.buildOtm( lib, "TheOtherBO" );
        OtmContextualFacet cf = buildOtm( bo, "TheCF" );
        OtmContributedFacet contrib = cf.getWhereContributed();
        // Done in builder: testContributedFacet( contrib, cf, bo );

        // When base type changed (moved)
        cf.setBaseType( bo2 );
        OtmContributedFacet newContrib = cf.getWhereContributed();
        // Then
        assertTrue( "Old object must NOT have new contributor as child.", !bo.getChildren().contains( newContrib ) );
        assertTrue( "Old object must NOT have old contributor as child.", !bo.getChildren().contains( contrib ) );
        assertTrue( "New object must have child.", bo2.getChildren().contains( newContrib ) );
        assertTrue( "getBaseType needs correct TL owner.", cf.getTL().getOwningEntity() == bo2.getTL() );
        //
        assertTrue( cf.getBaseType() == bo2 );
        assertTrue( contrib.getParent() == bo2 ); // SEE CHOICE FACET
        assertTrue( newContrib == contrib );
        assertTrue( cf.getWhereContributed() == newContrib );
        assertTrue( contrib.getChildren().size() == newContrib.getChildren().size() );
        testContributedFacet( cf.getWhereContributed(), cf, bo2 );
    }

    // TODO - move to testContextualFacet
    @Test
    public void testNestedContributedFacets() {}

    @Test
    public void testWhenContributed() {
        // Given - a business object and contextual facet
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmBusinessObject bo = TestBusiness.buildOtm( lib, "TestBO" );
        OtmContextualFacet cf = buildOtm( bo, "CF1" );
        // Was injected by buildOtm
        // assertTrue( "Has not been injected yet.", cf.getWhereContributed() == null );

        // When added
        OtmContributedFacet contrib = bo.add( cf );
        // Then (lazy evaluation)
        testContributedFacet( contrib, cf, bo );
    }

}
