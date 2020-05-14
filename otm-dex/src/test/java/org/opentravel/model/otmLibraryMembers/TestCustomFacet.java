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
import org.opentravel.TestDexFileHandler;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmFacets.OtmChoiceFacet;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmCustomFacet;
import org.opentravel.model.otmProperties.TestOtmPropertiesBase;
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
    // private static Log log = LogFactory.getLog( TestContextualFacet.class );
    private static final String CF_NAME = "TestCF";


    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null );
        subject = buildOtm( staticModelManager );
        baseObject = buildOtm( staticModelManager );
        // baseObject.setName( "BaseCF" );
        // Needed for contextual facet tests
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
     * Create custom facet and contribute it to the passed business object.
     * 
     * @param modelManager
     * @param bo
     * @return
     */
    public static OtmContextualFacet buildOtm(OtmModelManager modelManager, OtmBusinessObject bo) {
        OtmContextualFacet cf = buildOtm( modelManager );
        bo.add( cf );
        testContributedFacet( cf.getWhereContributed(), cf, bo );
        return cf;
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

    /** ****************************************************** **/

    @Before
    public void beforeTest() {
        member = TestBusiness.buildOtm( staticModelManager );
        cf = buildOtm( staticModelManager );
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


    @Test
    public void testDeleting() {
        super.testDeleteFromMember();
        assertFalse( ((TLBusinessObject) member.getTL()).getCustomFacets().contains( cf.getTL() ) );
    }

    @Test
    public void testDeletingAsLibraryMember() {
        // Given - a business object and contextual facet
        OtmBusinessObject bo = TestBusiness.buildOtm( staticModelManager );
        OtmContextualFacet cf = buildOtm( staticModelManager );
        OtmContributedFacet contrib = bo.add( cf );
        // Given - a choice object and contextual facet
        OtmChoiceObject co = TestChoice.buildOtm( staticModelManager );
        OtmChoiceFacet cf2 = TestChoiceFacet.buildOtm( staticModelManager, co );
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

    @Test
    public void testDeletingChildren() {
        super.testDeletingChildren();
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
    public void testFacets() {}

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
    public void testMovingFacet() {
        // Given - a cf contributed to a bo
        OtmBusinessObject bo = TestBusiness.buildOtm( staticModelManager );
        OtmBusinessObject bo2 = TestBusiness.buildOtm( staticModelManager );
        bo2.setName( "TheOtherBO" );
        OtmContextualFacet cf = buildOtm( staticModelManager );
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
    public void testNestedContributedFacets() {
        OtmContextualFacet nestedCF = buildOtm( staticModelManager );
        TestOtmPropertiesBase.buildOneOfEach2( nestedCF );

        super.testNestedContributedFacets( nestedCF );
    }

    /**
     * On load from file, the compiler delivers: LibraryMemberFactory - choiceF1 - ChoiceFacet created but no
     * contributed. TLOwner is set (FacetTestChoice). LibraryMemberFactory - choiceF1 - ChoiceFacet created but no
     * contributed. TLOwner is set (FacetTestChoice). LibraryMemberFactory - choiceF1A - ChoiceFacet created but no
     * contributed. TLOwner is set (ChoiceF1).
     * 
     * WhereContributed computed when MemberTReeTable.createTreeItem checks where contributed Factory never used when
     * parent is contextual facet
     */
    @Test
    public void testNestedContributedFacetsFromFactory() {
        OtmModelManager mgr = new OtmModelManager( null, null );

        TestDexFileHandler.loadLocalLibrary( TestDexFileHandler.FILE_TESTLOCALLIBRARY, mgr.getTlModel() );
        TestDexFileHandler.loadLocalLibrary( TestDexFileHandler.FILE_TESTLOCALLIBRARY1, mgr.getTlModel() );
        TestDexFileHandler.loadLocalLibrary( TestDexFileHandler.FILE_TESTLOCALLIBRARY2, mgr.getTlModel() );
    }

    @Test
    public void testWhenContributed() {
        // Given - a business object and contextual facet
        OtmBusinessObject bo = TestBusiness.buildOtm( staticModelManager );
        OtmContextualFacet cf = buildOtm( staticModelManager );
        // Was injected by buildOtm
        // assertTrue( "Has not been injected yet.", cf.getWhereContributed() == null );

        // When added
        OtmContributedFacet contrib = bo.add( cf );
        // Then (lazy evaluation)
        testContributedFacet( contrib, cf, bo );
    }

}
