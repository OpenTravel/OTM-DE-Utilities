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
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmProperties.OtmAttribute;
import org.opentravel.model.otmProperties.TestOtmPropertiesBase;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLExtensionOwner;
import org.opentravel.schemacompiler.model.TLModelElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Verifies the functions of the <code>OtmContextualFacet</code> class.
 */
public class TestContextualFacet extends TestOtmLibraryMemberBase<OtmContextualFacet> {
    private static Log log = LogFactory.getLog( TestContextualFacet.class );
    private static final String CF_NAME = "TestCF";

    protected static OtmLibraryMember member = null;
    protected static OtmContextualFacet cf = null;
    protected static OtmContributedFacet contrib = null;
    // protected static OtmModelManager staticModelManager = new OtmModelManager( null, null );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null );
        // Needed for library member tests
        subject = TestChoiceFacet.buildOtm( staticModelManager );
        baseObject = TestChoice.buildOtm( staticModelManager );
    }


    public static void testContributedFacet(OtmContributedFacet contrib, OtmContextualFacet cf, OtmLibraryMember lm) {
        log.debug( "Testing contributed facet: " + cf );
        assertTrue( contrib != null );
        assertTrue( cf != null );
        assertTrue( lm != null );
        //
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

    public void testAdd() {
        int childCount = cf.getChildren().size();
        // When - children added using factory
        TestOtmPropertiesBase.buildOneOfEach2( contrib );
        // Then - both contextual and contrib must report having children
        assertTrue( cf.getChildren().size() > childCount );
        assertFalse( contrib.getChildren().isEmpty() );
        assertFalse( cf.getChildren().isEmpty() );
        testContributedFacet( contrib, cf, member );


        // When - adding a TL directly
        childCount = cf.getChildren().size();
        TLAttribute tla = new TLAttribute();
        tla.setOwner( cf.getTL() );
        tla.setName( "attrX" );
        cf.add( tla );
        // Then
        assertTrue( tla.getListeners().size() == 1 );
        assertTrue( cf.getChildren().size() > childCount );
        OtmObject p = OtmModelElement.get( tla );
        assertTrue( p instanceof OtmAttribute );
        assertTrue( cf.getChildren().contains( p ) );
        assertTrue( contrib.getChildren().contains( p ) );
        testContributedFacet( contrib, cf, member );

    }

    public void testCFInheritance(OtmLibraryMember extension) {
        // Given - a member to use as base type with CF and contrib
        assertTrue( member != null );
        assertTrue( extension != null );
        assertTrue( member.getClass() == extension.getClass() );
        assertTrue( member.getTL() instanceof TLExtensionOwner );

        // When - extension extends base
        extension.setBaseType( member );

        // Then - Member TL extension is null; Extension's TL has extension.
        assertTrue( "Given", extension.getBaseType() == member );
        assertTrue( "Given", ((TLExtensionOwner) extension.getTL()).getExtension() != null );
        assertTrue( "Given",
            ((TLExtensionOwner) extension.getTL()).getExtension().getExtendsEntity() == member.getTL() );

        // Then
        // non-contextual facets will not be inherited.
        List<OtmObject> cfKids = new ArrayList<>();
        member.getChildren().forEach( c -> {
            if (c instanceof OtmContributedFacet)
                cfKids.add( c );
        } );
        List<OtmObject> iKids = extension.getInheritedChildren();
        assertTrue( cfKids.size() == iKids.size() );
        // ?? These are not the same contributed facet or TL object
    }

    public void testNestedContributedFacets(OtmContextualFacet nestedCF) {
        // Given - a cf with children contributed to a member
        // Given - a second CF with children

        // When - injection point set
        OtmObject result = nestedCF.setBaseType( cf );
        // Then
        assertTrue( "Non-null result.", result == cf );
        assertTrue( "TL Owning entity is set.", nestedCF.getTL().getOwningEntity() == cf.getTL() );
        // Then - cf has nested as child
        List<OtmObject> kids = cf.getChildren();
        assertTrue( "Nested CF is a child of CF.", kids.contains( nestedCF.getWhereContributed() ) );
        assertTrue( "Nested CF is contributed to CF.", nestedCF.getContributedObject() == cf );
        testContributedFacet( nestedCF.getWhereContributed(), nestedCF, cf );
    }
    // Test via OtmFacetFactory

    public void testDeletingChildren() {
        // When - children added using factory
        int childCount = cf.getChildren().size();
        ArrayList<OtmObject> children = new ArrayList<>( cf.getChildren() );
        List<TLModelElement> tlKids = getTLChildren();
        assertTrue( tlKids.size() == children.size() );
        TestOtmPropertiesBase.buildOneOfEach2( contrib );
        assertFalse( cf.getChildren().isEmpty() );

        children = new ArrayList<>( cf.getChildren() );
        tlKids = getTLChildren();
        assertTrue( tlKids.size() == children.size() );

        children.forEach( c -> cf.delete( c ) );
        // Then
        assertTrue( cf.getChildren().isEmpty() );
        assertTrue( cf.getTL().getIndicators().isEmpty() );
        assertTrue( cf.getTL().getAttributes().isEmpty() );
        assertTrue( cf.getTL().getElements().isEmpty() );
    }

    public List<TLModelElement> getTLChildren() {
        ArrayList<TLModelElement> tlKids = new ArrayList<>();
        tlKids.addAll( cf.getTL().getAttributes() );
        tlKids.addAll( cf.getTL().getIndicators() );
        tlKids.addAll( cf.getTL().getElements() );
        return tlKids;
    }

    public void testDeleteFromMember() {
        // Given
        testContributedFacet( contrib, cf, member );
        assertTrue( member.getChildren().contains( contrib ) );

        // When deleted
        member.delete( cf );
        assertFalse( member.getChildren().contains( contrib ) );
    }

    //
    // @Test
    // public void testDeletingAsLibraryMember() {
    // // Given - a Choice object and contextual facet
    // OtmChoiceObject co1 = TestChoice.buildOtm( staticModelManager );
    // OtmContextualFacet cf = buildOtm( staticModelManager );
    // OtmContributedFacet contrib = co1.add( cf );
    // // Given - a choice object and contextual facet
    // OtmChoiceObject co2 = TestChoice.buildOtm( staticModelManager );
    // OtmChoiceFacet cf2 = buildOtm( staticModelManager );
    // OtmContributedFacet contrib2 = co2.add( cf2 );
    //
    // // Given - a library for the objects
    // OtmLibrary lib = TestLibrary.buildOtm( staticModelManager );
    // lib.add( co1 );
    // lib.add( co2 );
    // lib.add( cf );
    // lib.add( cf2 );
    // assertTrue( cf.getLibrary() != null );
    // assertTrue( cf.getModelManager().contains( cf ) );
    // //
    // testContributedFacet( contrib, cf, co1 );
    // testContributedFacet( contrib2, cf2, co2 );
    //
    // // When deleted
    // lib.delete( cf );
    // assertFalse( cf.getModelManager().contains( cf ) );
    // assertFalse( co1.getChildren().contains( contrib ) );
    // assertFalse( co1.getTL().getChoiceFacets().contains( cf.getTL() ) );
    // //
    // lib.delete( cf2 );
    // assertFalse( co2.getChildren().contains( contrib2 ) );
    // assertFalse( co2.getTL().getChoiceFacets().contains( cf2.getTL() ) );
    // }
    //
    //
    // @Test
    // public void testDeletingWithContributedFacet() {
    // // Given - a Choice object and contextual facet
    // OtmChoiceObject co = TestChoice.buildOtm( staticModelManager );
    // OtmContextualFacet cf = buildOtm( staticModelManager );
    // OtmContributedFacet contrib = co.add( cf );
    // testContributedFacet( contrib, cf, co );
    //
    // OtmContextualFacet cf2 = buildOtm( staticModelManager );
    // testContributedFacet( contrib, cf, co );
    //
    // OtmContributedFacet contrib2 = co.add( cf2 );
    // // testContributedFacet( contrib2, cf2, bo ); // Performs lazy-eval on contributor
    // // assertTrue( "Lazy-evaluation on contributor.", contrib2.getContributor() == cf2 );
    // assertTrue( "Contextual facet knows where it is contributed.", cf2.getWhereContributed() == contrib2 );
    // assertTrue( contrib.getContributor() == cf );
    //
    // testContributedFacet( contrib, cf, co );
    // assertTrue( co.getChildren().contains( contrib ) );
    // assertTrue( co.getChildren().contains( contrib2 ) );
    // assertTrue( co.getTL().getChoiceFacets().contains( cf.getTL() ) );
    // assertTrue( co.getTL().getChoiceFacets().contains( cf2.getTL() ) );
    //
    // // When deleted
    // co.delete( contrib );
    // assertFalse( co.getChildren().contains( contrib ) );
    // assertFalse( co.getTL().getChoiceFacets().contains( cf.getTL() ) );
    // co.delete( contrib2 );
    // assertFalse( co.getChildren().contains( contrib2 ) );
    // assertFalse( co.getTL().getChoiceFacets().contains( cf2.getTL() ) );
    // }
    //
    // @Test
    // public void testFacets() {}
    //
    // /** ****************************************************** **/
    //
    // @Test
    // public void testInheritance() {
    // OtmChoiceObject baseBo = TestChoice.buildOtm( staticModelManager );
    // baseBo.setName( "BaseBO" );
    // OtmContextualFacet inheritedCf = buildOtm( staticModelManager, baseBo );
    // assertTrue( "Given", !inheritedCf.isInherited() );
    //
    // OtmChoiceObject bo = TestChoice.buildOtm( staticModelManager );
    // OtmContextualFacet cf = buildOtm( staticModelManager, bo );
    // bo.setName( "SubType" );
    // assertTrue( "Given", !cf.isInherited() );
    //
    // // When - bo extends baseBo
    // bo.setBaseType( baseBo );
    // assertTrue( "Given", bo.getBaseType() == baseBo );
    // assertTrue( "Given", bo.getTL().getExtension() != null );
    // assertTrue( "Given", bo.getTL().getExtension().getExtendsEntity() == baseBo.getTL() );
    //
    // // Then
    // List<OtmObject> ic1 = bo.getInheritedChildren();
    // List<OtmObject> ic2 = baseBo.getInheritedChildren();
    // // assertTrue( "Extension must have inherited CF", bo.getInheritedChildren().contains( inheritedCf ) );
    // }
    //
    // @Test
    // public void testMovingFacet() {
    // // Given - a cf contributed to a bo
    // OtmChoiceObject co = TestChoice.buildOtm( staticModelManager );
    // OtmChoiceObject co2 = TestChoice.buildOtm( staticModelManager );
    // co2.setName( "TheOtherBO" );
    // OtmContextualFacet cf = buildOtm( staticModelManager );
    // assertTrue( "Has not been injected yet.", cf.getWhereContributed() == null );
    // OtmContributedFacet contrib = co.add( cf );
    // testContributedFacet( contrib, cf, co );
    //
    // // When base type changed (moved)
    // cf.setBaseType( co2 );
    // OtmContributedFacet newContrib = cf.getWhereContributed();
    // // Then
    // assertTrue( cf.getBaseType() == co2 );
    // assertTrue( contrib.getParent() == co2 );
    // assertTrue( newContrib == contrib );
    // assertTrue( cf.getWhereContributed() == newContrib );
    // assertTrue( contrib.getChildren().size() == newContrib.getChildren().size() );
    // testContributedFacet( cf.getWhereContributed(), cf, co2 );
    // }
    //
    //
    // @Test
    // public void testWhenContributed() {
    // // Given - a Choice object and contextual facet
    // OtmChoiceObject bo = TestChoice.buildOtm( staticModelManager );
    // OtmContextualFacet cf = buildOtm( staticModelManager );
    // assertTrue( "Has not been injected yet.", cf.getWhereContributed() == null );
    //
    // // When added
    // OtmContributedFacet contrib = bo.add( cf );
    // // Then (lazy evaluation)
    // testContributedFacet( contrib, cf, bo );
    // }
}
