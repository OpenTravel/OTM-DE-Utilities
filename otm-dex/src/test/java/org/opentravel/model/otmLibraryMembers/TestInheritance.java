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

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opentravel.TestDexFileHandler;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.TestOtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestVersionChain;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmCustomFacet;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmFacets.TestFacet;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.TestOtmPropertiesBase;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Verifies the functions related to inheritance.
 * <p>
 * {@link TestFacet#testModelInheritedChildren()}
 */
public class TestInheritance extends AbstractFxTest {
    private static Log log = LogFactory.getLog( TestInheritance.class );
    public static final boolean RUN_HEADLESS = true;
    static OtmModelManager mgr = null;

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestOtmModelManager.class );
        repoManager = repositoryManager.get();
        mgr = new OtmModelManager( null, repoManager, null );
    }

    /**
     * Uses {@link TestResource#hasCustomFacets()}
     * 
     * @throws VersionSchemeException
     */
    @Test
    public void testInheritedCustomFacets() throws VersionSchemeException {
        // Given - the unmanaged project with lots of contextual facets
        mgr.clear();
        TestDexFileHandler.loadUnmanagedProject( mgr );
        assertTrue( mgr.getTlModel().getAllLibraries().size() == 6 );

        // Given - list of tl contextual facets
        List<TLContextualFacet> tlCFacets = new ArrayList<>();
        for (TLLibrary tlLib : mgr.getTlModel().getUserDefinedLibraries()) {
            for (LibraryMember tlm : tlLib.getNamedMembers())
                if (tlm instanceof TLContextualFacet)
                    tlCFacets.add( (TLContextualFacet) tlm );
        }

        // Given - tl projects added to manager
        mgr.addProjects();
        Collection<OtmLibrary> libs = mgr.getUserLibraries();
        Collection<OtmLibraryMember> members = mgr.getMembers();

        // Given - the list of contextual facets to test
        List<OtmContextualFacet> cFacets = new ArrayList<>();
        members.forEach( m -> {
            if (m instanceof OtmContextualFacet)
                cFacets.add( (OtmContextualFacet) m );
        } );
        // Collection<OtmLibraryMember> cFacets = mgr.getMembersContextualFacets();

        // Givens
        assertTrue( !libs.isEmpty() );
        assertTrue( !members.isEmpty() );
        assertTrue( tlCFacets.size() >= 24 );
        assertTrue( cFacets.size() >= 24 );

        // Then - check TL facets and add those missing owning entity to list
        List<TLContextualFacet> tlcfs_MissingOwningEntity = new ArrayList<>();
        List<OtmContextualFacet> cfs_MissingOwningEntity = new ArrayList<>();
        Map<String,TLContextualFacet> knownFacets = new HashMap<>();

        for (TLContextualFacet tlcf : tlCFacets) {
            assertTrue( tlcf != null );
            assertTrue( tlcf.getOwningModel() != null );
            assertTrue( tlcf.getOwningLibrary() != null );
            assertTrue( tlcf.getOwningEntityName() != null );

            OtmContextualFacet cf = (OtmContextualFacet) OtmLibraryMemberBase.get( tlcf );
            String localName = tlcf.getOwningLibrary().getPrefix() + ":" + tlcf.getLocalName();
            String otmName = cf.getName();
            assertTrue( cf != null );

            // Separate out those whose owning entity is known from those not known
            if (tlcf.getOwningEntity() == null) {
                // log.debug( tlcf.getName() + " is missing Owning entity named: " + tlcf.getOwningEntityName() );
                tlcfs_MissingOwningEntity.add( tlcf );
                cfs_MissingOwningEntity.add( cf );
                // assertTrue( localName.startsWith( "UNKNOWN" ) );
            } else {
                // assertTrue( !localName.startsWith( "UNKNOWN" ) );
                knownFacets.put( localName, tlcf );
                log.debug( "Added " + localName + " to knownFacets map." );
            }
            assertTrue( tlcf.getChildFacets() != null );
        }
        libs.forEach( l -> log.debug( l.getPrefix() ) );
        log.debug( tlcfs_MissingOwningEntity.size() + " tl contextual facets are missing owning entity." );

        // See if any of the missing owning entities are in the map
        for (TLContextualFacet tlcf : tlcfs_MissingOwningEntity) {
            log.debug( tlcf.getName() + " is trying to find owner named: " + tlcf.getOwningEntityName() );
            TLContextualFacet candidate = knownFacets.get( tlcf.getOwningEntityName() );
            if (candidate != null)
                log.debug( "Candidate: " + candidate.getName() );
        }



        // Then - If owning entity is missing, findWhereContributed() will try to look it up.
        for (TLContextualFacet tlcf : tlcfs_MissingOwningEntity) {
            OtmLibraryMember candidate = mgr.getMember( tlcf.getOwningEntityName() );
            if (candidate == null) {
                log.debug( "Model manager can not find owner." );
                // All the missing owners are facets
                for (TLContextualFacet f : tlCFacets) {
                    String name = f.getName();
                    if (name.equals( tlcf.getOwningEntityName() ))
                        log.debug( "Found facet with name: " + f.getName() );
                }
            }
        }
        log.debug( "" );

        // Then - look at contributed facets
        for (OtmContextualFacet cf : cFacets) {
            OtmContributedFacet contrib = cf.getWhereContributed();
            if (contrib == null) {
                log.debug( "Trouble: missing contributor for: " + cf );
            } else {
                OtmLibraryMember member = contrib.getOwningMember();
                assertTrue( contrib != null );
                assertTrue( member != null );
                TestContextualFacet.testContributedFacet( contrib, cf, member );
            }
        }
    }

    // @Ignore
    @Test
    public void testFacetPropertyCodegenUtils() {
        OtmBusinessObject baseBO = TestBusiness.buildOtm( mgr, "BaseBO" );
        TestOtmPropertiesBase.buildOneOfEach2( baseBO.getSummary() );
        TestOtmPropertiesBase.buildOneOfEach2( baseBO.getIdFacet() );
        TestOtmPropertiesBase.buildOneOfEach2( baseBO.getDetail() );

        // Given - count of children of base bo's summary facet.
        List<TLModelElement> tlBaseProps = baseBO.getSummary().getTLChildren();
        int expectedCount_Summary = baseBO.getSummary().getChildren().size();
        assertTrue( "TLFacet and OtmFacet must have same child count.", tlBaseProps.size() == expectedCount_Summary );

        // Given a second BO with no children
        OtmBusinessObject exBO = TestBusiness.buildOtm( mgr, "ExBO" );
        List<OtmObject> iKids = new ArrayList<>();
        iKids.addAll( exBO.getSummary().getInheritedChildren() );
        assertTrue( iKids.isEmpty() );
        // Make sure there are no kids
        exBO.getSummary().deleteAll();
        assertTrue( exBO.getSummary().getChildren().isEmpty() );

        // When extended
        exBO.setBaseType( baseBO );
        assertTrue( exBO.getTL().getExtension().getExtendsEntity() == baseBO.getTL() );

        // When - codegenUtils used to report out inherited properties, attributes and indicators
        TLFacet tlFacet = exBO.getSummary().getTL();
        List<TLModelElement> tli = new ArrayList<>();
        List<TLProperty> props = PropertyCodegenUtils.getInheritedFacetProperties( tlFacet );
        List<TLAttribute> attrs = PropertyCodegenUtils.getInheritedFacetAttributes( tlFacet );
        List<TLIndicator> inds = PropertyCodegenUtils.getInheritedFacetIndicators( tlFacet );
        assertTrue( !props.isEmpty() && !attrs.isEmpty() && !inds.isEmpty() );
        tli.addAll( props );
        // FAILS
        assertTrue( "Must have same element count.", props.size() == baseBO.getSummary().getTL().getElements().size() );
        tli.addAll( attrs );
        tli.addAll( inds );

        // Then - this should be true
        assertTrue( "The inherited properties must equal base property count.", tli.size() == expectedCount_Summary );

        // These methods deliver children from XSD hierarchy which adds ID properties to summary, etc.
        // List<TLProperty> props1 = PropertyCodegenUtils.getInheritedProperties( tlFacet );
        // List<TLAttribute> attrs1 = PropertyCodegenUtils.getInheritedAttributes( tlFacet );
        // List<TLIndicator> inds1 = PropertyCodegenUtils.getInheritedIndicators( tlFacet );
    }

    @Test
    public void testFacetFinding() {
        OtmBusinessObject bo = TestBusiness.buildOtm( mgr, "BaseBO" );
        TestOtmPropertiesBase.buildOneOfEach2( bo.getSummary() );
        OtmCustomFacet cf = TestCustomFacet.buildOtm( mgr );
        bo.add( cf );

        assertTrue( bo.getFacet( bo.getIdFacet() ) == bo.getIdFacet() );
        assertTrue( bo.getFacet( bo.getSummary() ) == bo.getSummary() );
        assertTrue( bo.getFacet( bo.getDetail() ) == bo.getDetail() );

        OtmBusinessObject exBo1 = TestBusiness.buildOtm( mgr, "ExBO1" );
        exBo1.setBaseType( bo );

        OtmBusinessObject exBo = TestBusiness.buildOtm( mgr, "ExBO" );
        exBo.setBaseType( exBo1 );

        // Test a private method
        // List<OtmFacet<TLFacet>> a = exBo.getIdFacet().getAncestors();
        // assertTrue( a.contains( bo.getIdFacet() ) );
        // a = exBo.getSummary().getAncestors();
        // assertTrue( a.contains( bo.getSummary() ) );
        // a = exBo.getDetail().getAncestors();
        // assertTrue( a.contains( bo.getDetail() ) );
    }

    @Test
    public void testFacetsAndObjects() {
        OtmBusinessObject bo = TestBusiness.buildOtm( mgr, "BaseBO" );
        TestOtmPropertiesBase.buildOneOfEach2( bo.getSummary() );
        OtmCustomFacet cf = TestCustomFacet.buildOtm( mgr );
        bo.add( cf );

        OtmBusinessObject exBo1 = TestBusiness.buildOtm( mgr, "ExBO1" );
        exBo1.setBaseType( bo );

        OtmBusinessObject exBo = TestBusiness.buildOtm( mgr, "ExBO" );
        exBo.setBaseType( exBo1 );

        // Modeling done via lazy evaluation when getInheritedChildren() invoked.
        // exBo.getSummary().modelInheritedChildren();
        // exBo.modelInheritedChildren();

        assertTrue( "Must have facet I-children.", !exBo.getSummary().getInheritedChildren().isEmpty() );
        assertTrue( "Must have inherited custom facet.", !exBo.getInheritedChildren().isEmpty() );
    }

    // Complex objects can have inherited contextual facets and the facets can have inherited properties
    @Ignore
    @Test
    public void testFacetModelInheritedChildren() {
        OtmBusinessObject baseBO = TestBusiness.buildOtm( mgr, "BaseBO" );
        TestOtmPropertiesBase.buildOneOfEach2( baseBO.getSummary() );
        TestOtmPropertiesBase.buildOneOfEach2( baseBO.getIdFacet() );
        TestOtmPropertiesBase.buildOneOfEach2( baseBO.getDetail() );

        // Given - count of children of base bo's summary facet.
        List<TLModelElement> tlBaseProps = baseBO.getSummary().getTLChildren();
        int expectedCount_Summary = baseBO.getSummary().getChildren().size();
        assertTrue( "TLFacet and OtmFacet must have same child count.", tlBaseProps.size() == expectedCount_Summary );

        // Given a second BO with no children
        OtmBusinessObject exBO = TestBusiness.buildOtm( mgr, "ExBO" );
        List<OtmObject> iKids = new ArrayList<>();
        iKids.addAll( exBO.getSummary().getInheritedChildren() );
        assertTrue( iKids.isEmpty() );
        // Make sure there are no kids
        exBO.getSummary().deleteAll();
        assertTrue( exBO.getSummary().getChildren().isEmpty() );

        // When extended
        exBO.setBaseType( baseBO );
        assertTrue( exBO.getTL().getExtension().getExtendsEntity() == baseBO.getTL() );

        exBO.getSummary().modelInheritedChildren();
        iKids.addAll( exBO.getSummary().getInheritedChildren() );
        assertTrue( !iKids.isEmpty() );

        assertTrue( exBO.getSummary().getInheritedChildren() != null );
        assertTrue( !exBO.getSummary().getInheritedChildren().isEmpty() );
    }

    @Test
    public void testInheritanceInMinorVersion() throws VersionSchemeException {
        mgr.clear();
        if (!TestDexFileHandler.loadVersionProject( mgr ))
            return; // No editable libraries

        OtmLibrary minorLibrary = TestVersionChain.getMinorInChain( mgr );
        assertTrue( "Given", minorLibrary != null );
        assertTrue( "Given", minorLibrary.isEditable() );
        assertTrue( "Given - minor is empty.", mgr.getMembers( minorLibrary ).isEmpty() );

        for (OtmLibraryMember member : mgr.getMembers( minorLibrary.getVersionChain().getMajor() )) {
            List<OtmObject> kids = member.getChildren();
            List<OtmObject> iKids = member.getInheritedChildren();

            if (member instanceof OtmComplexObjects) {
                // Business, choice and core object tests
                log.debug( "Testing " + member );

                // Create a minor version
                OtmLibraryMember minor = member.createMinorVersion( minorLibrary );
                assertTrue( "Must have created a minor object.", minor != null );

                // Get a representative facet
                OtmFacet<?> memberSummary = ((OtmComplexObjects<?>) member).getSummary();
                assertTrue( "Given.", !memberSummary.isInherited() );
                kids = memberSummary.getChildren();
                TestFacet.checkFacetChildren( memberSummary );
                OtmProperty memberProperty = (OtmProperty) kids.get( 0 );
                assertTrue( !memberProperty.isInherited() );
                assertTrue( memberProperty.getParent() == memberSummary );

                OtmFacet<?> minorSummary = ((OtmComplexObjects<?>) minor).getSummary();
                iKids = minorSummary.getInheritedChildren();

                // Assure all properties are inherited
                for (OtmObject p : ((OtmComplexObjects<?>) member).getSummary().getChildren())
                    assertTrue( "Given", TestFacet.getInheritedProperty( minorSummary, p.getTL() ) != null );

                // When - Adding properties to minor
                //
                TLProperty tlProp = new TLProperty();
                tlProp.setName( "NewInMinor" );
                tlProp.setType( mgr.getIdType().getTL() );
                tlProp.setOwner( minorSummary.getTL() );
                // Add one property
                OtmProperty newProperty = minorSummary.add( tlProp );
                assertTrue( "New property must not be inherited.", !newProperty.isInherited() );
                // get() will model the inherited children
                assertTrue( "Must have same number of inherited properties.",
                    minorSummary.getInheritedChildren().size() == iKids.size() );
                assertTrue( "Must not change base facet.", !memberSummary.isInherited() );
                assertTrue( "Must not change base property.", !memberProperty.isInherited() );
                assertTrue( "Must not change base property.", memberProperty.getParent() == memberSummary );

                // Add all property types
                TestOtmPropertiesBase.buildOneOfEach2( minorSummary );
                for (OtmObject c : minorSummary.getChildren())
                    assertTrue( "Children must not be inherited.", !c.isInherited() );
                assertTrue( "Must have same number of inherited properties.",
                    minorSummary.getInheritedChildren().size() == iKids.size() );

                //
                // Delete the new minor so it will not interfere with further tests
                minorLibrary.delete( minor );
                // Assure member kids are unchanged
                assertTrue( "Must have removed all kids from list.",
                    kids.size() == memberSummary.getChildren().size() );
                TestFacet.checkFacetChildren( memberSummary );
            }

            // change type ???
            // assure the new property with changed type is not inherited
            // assure the property in the major that had type change is still has same parent as before
        }
    }



    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getApplicationClass()
     */
    @Override
    protected Class<? extends AbstractOTMApplication> getApplicationClass() {
        return ObjectEditorApp.class;
    }

    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getBackgroundTaskNodeQuery()
     */
    @Override
    protected String getBackgroundTaskNodeQuery() {
        return "#libraryText";
    }

    /**
     * Configure headless/normal mode for TestFX execution.
     */
    static {
        TestFxMode.setHeadless( RUN_HEADLESS );
    }

}
