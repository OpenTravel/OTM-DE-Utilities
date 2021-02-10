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
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmProperties.OtmElement;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLComplexTypeBase;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Verifies the functions of the <code>OtmBusinessObject</code> class.
 */
public class TestBusiness extends TestOtmLibraryMemberBase<OtmBusinessObject> {

    private static Log log = LogFactory.getLog( TestBusiness.class );
    private static String BoName = "Testbo";

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null, null );
        subject = buildOtm( staticModelManager );
        baseObject = buildOtm( staticModelManager );
        baseObject.setName( "BaseBO" );
    }

    @Test
    public void testFacets() {
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmBusinessObject bo = TestBusiness.buildOtm( lib, "TestBO" );

        assertNotNull( bo.getSummary() );
        assertNotNull( bo.getDetail() );
    }

    @Test
    public void testGhostFacets() {
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmBusinessObject baseBO = TestBusiness.buildOtm( lib, "BaseBO" );
        OtmBusinessObject exBO = TestBusiness.buildOtm( lib, "ExBO" );
        TLFacetOwner exTL = (TLFacetOwner) exBO.getTL();

        // Initially, there should be no ghosts.
        Collection<OtmContributedFacet> cFacets = baseBO.getChildrenContributedFacets();
        List<TLContextualFacet> ghosts = FacetCodegenUtils.findGhostFacets( exTL, TLFacetType.CUSTOM );
        assertTrue( "Given: Extension not set yet, there must be no ghosts.", ghosts.isEmpty() );

        // When - Add another CF -before extension-
        OtmContextualFacet cf = TestCustomFacet.buildOtm( baseBO, "CF1" );
        TestContextualFacet.testContributedFacet( cf.getWhereContributed(), cf, baseBO );
        // Then - still no ghosts
        ghosts = FacetCodegenUtils.findGhostFacets( exTL, TLFacetType.CUSTOM );
        assertTrue( "Given: Extension not set yet, there must be no ghosts.", ghosts.isEmpty() );

        // When - Extension set
        exBO.setBaseType( baseBO );
        assertTrue( "When - must have extension.", exBO.getBaseType() == baseBO );
        assertTrue( "When - TL must have extension.", exBO.getTL().getExtension() != null );
        assertTrue( "When - TL extend base TL.", exBO.getTL().getExtension().getExtendsEntity() == baseBO.getTL() );
        assertTrue( "When - TL must not have extension.", baseBO.getTL().getExtension() == null );

        // Then - ??? Only finds CF1
        List<TLFacet> tlFacets = FacetCodegenUtils.getAvailableFacets( (TLComplexTypeBase) exTL );
        ghosts = FacetCodegenUtils.findGhostFacets( exTL, TLFacetType.CUSTOM );
        cFacets = baseBO.getChildrenContributedFacets();
        // assertTrue( "Given: Extension not set yet, there must be no ghosts.", ghosts.isEmpty() );

        // When - add another CF
        cf = TestCustomFacet.buildOtm( baseBO, "CF2" );
        TestContextualFacet.testContributedFacet( cf.getWhereContributed(), cf, baseBO );

        // Then - ???
        ghosts = FacetCodegenUtils.findGhostFacets( exTL, TLFacetType.CUSTOM );

    }

    @Override
    public void testCopy(OtmLibraryMember member) {
        testTLCopy( member );
        testCopySteps( member ); // Test each step in the copy process

        assertTrue( "Given: ", !member.getChildrenContributedFacets().isEmpty() );
        List<OtmObject> kids_member = member.getChildren();

        // When - copied
        OtmBusinessObject bo = (OtmBusinessObject) member;
        OtmBusinessObject copy = bo.copy();

        // Then
        List<OtmObject> copy_member = copy.getChildren();
        assertTrue( "Copy must not have contributed children.", copy.getChildrenContributedFacets().isEmpty() );
    }

    public void testCopySteps(OtmLibraryMember member) {
        assertTrue( "Given: ", !member.getChildrenContributedFacets().isEmpty() );
        OtmBusinessObject bo = (OtmBusinessObject) member;
        Collection<OtmContributedFacet> customs_member = bo.getChildrenContributedFacets();
        List<TLContextualFacet> tlCustoms_member = bo.getTL().getCustomFacets();

        // When - clone
        LibraryElement tlMember = null;
        try {
            tlMember = member.getTL().cloneElement();
        } catch (Exception e) {
            log.debug( "Error cloning." + getClass().getSimpleName() + " " + member.getName() );
        }
        assertTrue( tlMember instanceof TLBusinessObject );
        List<TLContextualFacet> tlCustoms_copy = ((TLBusinessObject) tlMember).getCustomFacets();

        // // When - factory used to create OTM facade
        OtmLibraryMember c = OtmLibraryMemberFactory.create( (LibraryMember) tlMember, member.getModelManager() );
        assertTrue( "Factory must return a business object.", c instanceof OtmBusinessObject );
        OtmBusinessObject copy = (OtmBusinessObject) c;

        // Then - check the contributed facets
        Collection<OtmContributedFacet> contributedList_copy = copy.getChildrenContributedFacets();
        assertTrue( "Must have a contributed facets.", !contributedList_copy.isEmpty() );

        for (OtmContributedFacet contrib : contributedList_copy) {
            assertTrue( "Contributed TL must be from copy.", tlCustoms_copy.contains( contrib.getTL() ) );
            assertTrue( "Contributed must not be same as in original member.", !customs_member.contains( contrib ) );
            assertTrue( "Contributed TL must not be from original member.",
                !tlCustoms_member.contains( contrib.getTL() ) );
            assertTrue( "Contributed must not have contributor.", contrib.getContributor() == null );
        }

        // When - delete contributed facets
        for (OtmContributedFacet contrib : contributedList_copy) {
            copy.delete( contrib );
        }
        // Then
        assertTrue( copy.getChildrenContributedFacets().isEmpty() );
        assertTrue( copy.getTL().getCustomFacets().isEmpty() );
    }

    /**
     * What should be inherited from base business object?
     * <p>
     * Contextual facets are inherited but the name should start with the extension object name.
     * <p>
     * Aliases are <b> not </b> inherited.
     * 
     * @see org.opentravel.model.otmLibraryMembers.TestOtmLibraryMemberBase#testInheritance(org.opentravel.model.otmLibraryMembers.OtmLibraryMember)
     */
    @Override
    public void testInheritance(OtmBusinessObject otm) {
        assertTrue( "Given: must have object to test inheritance.", otm != null );

        OtmBusinessObject base = (OtmBusinessObject) otm.getBaseType();
        assertTrue( "Given: must have base to test inheritance.", base != null );

        // Check - already has custom facet
        Collection<OtmContributedFacet> baseCFs = base.getChildrenContributedFacets();
        for (OtmContributedFacet contrib : baseCFs) {
            OtmContextualFacet cf = contrib.getContributor();
            assertTrue( "Given: must have contributor.", cf != null );
            TestContextualFacet.testContributedFacet( contrib, cf, base );
        }

        TLFacetOwner extendedOwner = (TLFacetOwner) otm.getTL();
        List<TLContextualFacet> ghosts = FacetCodegenUtils.findGhostFacets( extendedOwner, TLFacetType.CUSTOM );
        List<OtmObject> otmInherited = otm.getInheritedChildren();
        // FAILS
        // assertTrue( otmInherited.isEmpty() == baseCFs.isEmpty() );

        // Add some children to be inherited.
        OtmContextualFacet cf = TestCustomFacet.buildOtm( base, "CF1" );
        TestContextualFacet.testContributedFacet( cf.getWhereContributed(), cf, base );
        cf = TestCustomFacet.buildOtm( base, "CF2" );
        TestContextualFacet.testContributedFacet( cf.getWhereContributed(), cf, base );

        ghosts = FacetCodegenUtils.findGhostFacets( extendedOwner, TLFacetType.CUSTOM );
        // Only finds those just added, not the ones from before extended

        super.testInheritance( otm );
    }

    public void testTLCopy(OtmLibraryMember member) {

        log.debug( "Testing copy of TL business object." );
        TLBusinessObject tlMember = ((OtmBusinessObject) member).getTL();
        List<TLContextualFacet> customs_member = tlMember.getCustomFacets();
        if (customs_member.isEmpty())
            return; // Nothing to check

        // When - TL is cloned
        TLBusinessObject tlCopy = (TLBusinessObject) member.getTL().cloneElement();

        // Then
        assertTrue( tlCopy != null );
        List<TLContextualFacet> customs = new ArrayList<>( tlCopy.getCustomFacets() );
        assertTrue( "Must have same number of customs.", customs.size() == customs_member.size() );
        for (TLContextualFacet custom : customs) {
            assertTrue( "Custom must not be same as in original member.", !customs_member.contains( custom ) );
            assertTrue( "Custom copy has not be modeled.", custom.getListeners().isEmpty() );
        }

        // When - TL customs are deleted
        for (TLContextualFacet c : customs)
            tlCopy.removeCustomFacet( c );

        // Then
        assertTrue( "Copy must not have custom facets.", tlCopy.getCustomFacets().isEmpty() );
        assertTrue( "Original must still have custom facets.", !tlMember.getCustomFacets().isEmpty() );
        assertTrue( "Original must have same number of custom facets.",
            tlMember.getCustomFacets().size() == customs_member.size() );
    }

    /** ****************************************************** **/
    /**
     * Build business object with attribute and element in ID and Summary facets. *
     * 
     * @param mgr
     * @param name
     * @return
     */
    public static OtmBusinessObject buildOtm(OtmLibrary library, String name) {
        assertTrue( "Library must have model manager.", library.getModelManager() != null );
        BoName = name; // set global static
        OtmBusinessObject bo = buildOtm( library.getModelManager() );
        bo.setName( name );
        library.add( bo );

        for (OtmContributedFacet cf : bo.getChildrenContributedFacets()) {
            assertTrue( "Contributed facet must have contributor.", cf.getContributor() != null );
            library.add( cf.getContributor() );
            assertTrue( "Contributor must be in library.", library.contains( cf.getContributor() ) );
        }

        assertTrue( bo != null );
        assertTrue( bo.getLibrary() == library );
        assertTrue( bo.isEditable() );
        assertTrue( bo.getActionManager() == library.getActionManager() );
        assertTrue( library.getModelManager().getMembers().contains( bo ) );

        return bo;
    }

    // /**
    // * Build business object with attribute and element in ID and Summary facets.
    // * <p>
    // * <b>Note: </b>Contextual facet will not have a library.
    // *
    // * @param mgr
    // * @param name
    // * @return
    // */
    // @Deprecated
    // public static OtmBusinessObject buildOtm(OtmModelManager mgr, String name) {
    // BoName = name;
    // return buildOtm( mgr );
    // }

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

    /**
     * Simply create a business object and add to model manager.
     * <p>
     * <b>Note: </b>New business object and its contextual facets will not have library. Preferred builder for general
     * us is {@link #buildOtm(OtmLibrary, String)}
     * 
     * @param mgr
     * @return
     */
    public static OtmBusinessObject buildOtm(OtmModelManager mgr) {
        OtmBusinessObject bo = new OtmBusinessObject( buildTL(), mgr );
        assertNotNull( bo );
        mgr.add( bo );

        assertTrue( bo.getChildren().size() > 2 );
        assertTrue( bo.getSummary().getChildren().size() == 2 );
        assertTrue( "Must have identity listener.", OtmModelElement.get( bo.getTL() ) == bo );

        // Must have a library to build custom facets
        // OtmContextualFacet cf = TestCustomFacet.buildOtm( bo, "SomeOtherCustom" );
        // OtmContextualFacet cf2 = TestCustomFacet.buildOtm( bo, "SomeOtherCustom2" );
        // OtmContextualFacet cf3 = TestCustomFacet.buildOtm( bo, "SomeOtherCustom3" );

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
