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
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.TestOtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmContainers.TestVersionChain;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmCustomFacet;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmFacets.TestFacet;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.TestOtmPropertiesBase;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
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
     * @throws InterruptedException
     */
    @Test
    public void testInheritedCustomFacets() throws VersionSchemeException, InterruptedException {
        // Given - the unmanaged project with lots of contextual facets
        mgr.clear();
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
        assertTrue( mgr.getTlModel().getAllLibraries().size() == 6 );

        // Wait for type resolver and validation tasks to complete
        // Consider moving this into TestDexFileHandler
        int waitCount = 0;
        while (mgr.getBackgroundTaskCount() > 0) {
            // no-op
            waitCount++;
            Thread.sleep( 100 ); // doesn't matter how long this runs
        }
        log.debug( "Waited " + waitCount + " iterations." );

        // Given - list of tl contextual facets
        List<TLContextualFacet> tlCFacets = new ArrayList<>();
        for (TLLibrary tlLib : mgr.getTlModel().getUserDefinedLibraries()) {
            for (LibraryMember tlm : tlLib.getNamedMembers())
                if (tlm instanceof TLContextualFacet)
                    tlCFacets.add( (TLContextualFacet) tlm );
        }

        Collection<OtmLibrary> libs = mgr.getUserLibraries();
        Collection<OtmLibraryMember> members = mgr.getMembers();

        // Given - the list of contextual facets to test
        List<OtmContextualFacet> cFacets = new ArrayList<>();
        members.forEach( m -> {
            if (m instanceof OtmContextualFacet)
                cFacets.add( (OtmContextualFacet) m );
        } );

        // Givens
        assertTrue( libs.size() == 4 );
        assertTrue( !members.isEmpty() );
        assertTrue( tlCFacets.size() >= 24 );
        assertTrue( cFacets.size() >= 24 );
        assertTrue( cFacets.size() == tlCFacets.size() );

        // Verify the TL Contextual Facets have facade, and owning: Model, Library and Entity Name
        for (TLContextualFacet tlcf : tlCFacets) {
            assertTrue( tlcf != null );
            assertTrue( tlcf.getOwningModel() != null );
            assertTrue( tlcf.getOwningLibrary() != null );
            assertTrue( tlcf.getOwningEntityName() != null );
            assertTrue( (OtmContextualFacet) OtmModelElement.get( tlcf ) != null );
            assertTrue( tlcf.getChildFacets() != null ); // May be empty
        }

        // Then - check TL facets and add those missing owning entity to list
        List<TLContextualFacet> tlcfs_MissingOwningEntity = new ArrayList<>();
        Map<String,TLContextualFacet> knownFacets = new HashMap<>();

        for (TLContextualFacet tlcf : tlCFacets) {
            OtmContextualFacet cf = (OtmContextualFacet) OtmModelElement.get( tlcf );

            // Separate out those whose owning entity is known from those not known
            if (tlcf.getOwningEntity() == null) {
                tlcfs_MissingOwningEntity.add( tlcf );
            } else {
                // Get a prefix:localName to use in the map of known facets
                String nameWithPrefix = tlcf.getOwningLibrary().getPrefix() + ":" + tlcf.getLocalName();
                knownFacets.put( nameWithPrefix, tlcf );
            }
        }
        // Print out the two lists
        for (TLContextualFacet tlcf : knownFacets.values())
            log.debug( "Known " + tlcf.getName() + "\t LocalName = " + tlcf.getOwningLibrary().getPrefix() + " "
                + tlcf.getLocalName() );
        for (TLContextualFacet tlcf : tlcfs_MissingOwningEntity)
            log.debug( "Unknown " + tlcf.getOwningLibrary().getPrefix() + " " + tlcf.getName() + "\t Owner   = "
                + tlcf.getOwningEntityName() );

        // Debugging output
        libs.forEach( l -> log.debug( l.getPrefix() ) );
        log.debug( tlcfs_MissingOwningEntity.size() + " tl contextual facets are missing owning entity." );
        log.debug( knownFacets.size() + " tl contextual facets have owning entity." );
        log.debug( "" );

        // Then - If owning entity is missing, findWhereContributed() will try to look it up.
        for (TLContextualFacet tlcf : tlcfs_MissingOwningEntity) {
            assertTrue( "Must find facade.", OtmModelElement.get( tlcf ) instanceof OtmContextualFacet );
            OtmContextualFacet cf = (OtmContextualFacet) OtmModelElement.get( tlcf );

            log.debug( cf + " getWhereContributed() is trying to find owner named: " + tlcf.getOwningEntityName() );
            if (cf.getWhereContributed() != null) {
                log.debug( "   Found." );
                // Verify correctly assigned
                TLModelElement tlOwner = (TLModelElement) cf.getTL().getOwningEntity();
                OtmObject owner = OtmModelElement.get( (TLModelElement) cf.getTL().getOwningEntity() );
                TestContextualFacet.testContributedFacet( cf.getWhereContributed(), cf, cf.getContributedObject() );
            }
        }

        // Then - look at ALL contributed facets
        for (OtmContextualFacet cf : cFacets) {
            OtmContributedFacet contrib = cf.getWhereContributed();
            if (contrib != null) {
                OtmLibraryMember member = contrib.getOwningMember();
                OtmLibraryMember owner = cf.getContributedObject();
                assertTrue( owner != null );
                assertTrue( contrib != null );
                assertTrue( member != null );
                TestContextualFacet.testContributedFacet( contrib, cf, member );
            }
        }

        log.debug( "" );
        log.debug( "The following were never resolved." );
        for (OtmContextualFacet cf : cFacets) {
            OtmContributedFacet contrib = cf.getWhereContributed();
            if (contrib == null) {
                log.debug( "Trouble: missing contributor for: " + cf );
                assertTrue( false );
            }
        }
    }

    @Test
    public void testNestedContextualFacetPropertyCodegenUtils() {
        // Given - a business object with all types of properties in each of its native facets
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmBusinessObject baseBO = TestBusiness.buildOtm( lib, "BaseBO" );
        TestOtmPropertiesBase.buildOneOfEach2( baseBO.getSummary() );
        TestOtmPropertiesBase.buildOneOfEach2( baseBO.getIdFacet() );
        TestOtmPropertiesBase.buildOneOfEach2( baseBO.getDetail() );
        // Given - check count of children of base bo's summary facet.
        List<TLModelElement> tlBaseProps = baseBO.getSummary().getTLChildren();
        int expectedCount_Summary = baseBO.getSummary().getChildren().size();
        assertTrue( "Given - TLFacet and OtmFacet must have same child count.",
            tlBaseProps.size() == expectedCount_Summary );
        assertTrue( "Given - builder added a custom facet.", !baseBO.getChildrenContributedFacets().isEmpty() );

        // Put a custom facet on the base BO
        OtmCustomFacet baseCustom = TestCustomFacet.buildOtm( baseBO, "BaseCustom" );

        // Put a nested custom facet on the custom facet.
        OtmCustomFacet nestedCustom = TestCustomFacet.buildOtm( baseCustom, "NestedCustom" );
        // Given - check injection
        assertTrue( "Given - base custom must have nested as child.",
            baseCustom.getChildren().contains( nestedCustom.getWhereContributed() ) );
        assertTrue( "Given - nested custom is contributed to base custom.",
            nestedCustom.getContributedObject() == baseCustom );
        assertTrue( "Given - nested custom's owning entity is base custom",
            nestedCustom.getTL().getOwningEntity() == baseCustom.getTL() );

        //
        // When - accessed as done in OtmLibraryMemberBase#modelChildren
        //
        // children of business object
        List<TLFacet> baseFacets = ((TLFacetOwner) baseBO.getTL()).getAllFacets();
        assertTrue( "Then - must find base facets.", !baseFacets.isEmpty() );
        assertTrue( "Then - base facets must include the base custom.", baseFacets.contains( baseCustom.getTL() ) );
        assertTrue( "Then - all facets must be retrieved.", baseFacets.size() == baseBO.getChildren().size() );

        // QUESTION - BO reports out injected child but facet does not, why? How to find custom from base?
        // When - children of base custom
        List<TLFacet> baseFacets2 = ((TLFacetOwner) baseCustom.getTL()).getAllFacets();
        // FIXED 1/23/2021
        assertTrue( "Then - must find nested facet.", !baseFacets2.isEmpty() );
    }

    @Test
    public void testNestedInheritedContextualFacetPropertyCodegenUtils() {
        // Given - same setup as testNestedContextualFacetPropertyCodegenUtils()
        // Given - a business object with all types of properties in each of its native facets
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmBusinessObject baseBO = TestBusiness.buildOtm( lib, "BaseBO" );
        assertTrue( "Given - builder added a custom facet.", !baseBO.getChildrenContributedFacets().isEmpty() );
        // Put a custom facet on the base BO
        OtmCustomFacet baseCustom = TestCustomFacet.buildOtm( baseBO, "BaseCustom" );
        assertTrue( "Base custom must have a library.", baseCustom.getLibrary() != null );
        assertTrue( "Base custom must have a TL library.", baseCustom.getLibrary().getTL() != null );

        // Put a nested custom facet on the custom facet.
        OtmCustomFacet nestedCustom = TestCustomFacet.buildOtm( baseCustom, "NestedCustom" );

        // Given a second BO with no children
        OtmBusinessObject exBO = TestBusiness.buildOtm( lib, "ExBO" );
        List<OtmObject> iKids = new ArrayList<>();
        iKids.addAll( exBO.getSummary().getInheritedChildren() );
        assertTrue( iKids.isEmpty() );
        // Make sure there are no kids
        exBO.getSummary().deleteAll();
        assertTrue( "Given - must not have children.", exBO.getSummary().getChildren().isEmpty() );

        // Given base extended by ex
        exBO.setBaseType( baseBO );
        assertTrue( "Given - ex must extend base.", exBO.getTL().getExtension().getExtendsEntity() == baseBO.getTL() );

        //
        // When - accessed as done in OtmLibraryMemberBase#modelIhneritedChildren
        //
        TLBusinessObject extendedOwner = exBO.getTL();
        List<TLContextualFacet> ghosts = FacetCodegenUtils.findGhostFacets( extendedOwner, TLFacetType.CUSTOM );

        // Then
        assertTrue( "Must find ghost facets.", !ghosts.isEmpty() );
        TLContextualFacet inheritedCustom = ghosts.get( 0 );
        // Only the base is reported out...but it is a new instance of the facet
        assertTrue( "Must have base custom name.", inheritedCustom.getName().equals( baseCustom.getTL().getName() ) );

        // When - accessed as done in OtmLibraryMemberBase#modelIhneritedChildren
        TLContextualFacet extendedOwner2 = inheritedCustom;
        List<TLContextualFacet> ghosts2 = FacetCodegenUtils.findGhostFacets( extendedOwner2, TLFacetType.CUSTOM );
        List<TLContextualFacet> nlGhosts =
            FacetCodegenUtils.findNonLocalGhostFacets( (TLLibrary) baseCustom.getLibrary().getTL() );
        // Then
        // FIXED 1/23/2021
        assertTrue( "Must find ghost facets.", !ghosts2.isEmpty() );
    }

    /**
     * Create test Business Objects to test codegen utils against.
     */
    @Test
    public void testPropertyCodegenUtils_InheritedFacetProperties() {
        // Given - a business object with all types of properties in each of its native facets
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmBusinessObject baseBO = TestBusiness.buildOtm( lib, "BaseBO" );
        TestOtmPropertiesBase.buildOneOfEach2( baseBO.getSummary() );
        TestOtmPropertiesBase.buildOneOfEach2( baseBO.getIdFacet() );
        TestOtmPropertiesBase.buildOneOfEach2( baseBO.getDetail() );

        // Given - count of children of base bo's summary facet.
        List<TLModelElement> tlBaseProps = baseBO.getSummary().getTLChildren();
        int expectedCount_Summary = baseBO.getSummary().getChildren().size();
        assertTrue( "Given - TLFacet and OtmFacet must have same child count.",
            tlBaseProps.size() == expectedCount_Summary );

        // Given a second BO with no children
        OtmBusinessObject exBO = TestBusiness.buildOtm( lib, "ExBO" );
        List<OtmObject> iKids = new ArrayList<>();
        iKids.addAll( exBO.getSummary().getInheritedChildren() );
        assertTrue( iKids.isEmpty() );
        // Make sure there are no kids
        exBO.getSummary().deleteAll();
        assertTrue( "Given - must not have children.", exBO.getSummary().getChildren().isEmpty() );

        // Given extended
        exBO.setBaseType( baseBO );
        assertTrue( "Given - ex must extend base.", exBO.getTL().getExtension().getExtendsEntity() == baseBO.getTL() );

        // When - codegenUtils used to report out inherited properties, attributes and indicators
        TLFacet tlFacet = exBO.getSummary().getTL();
        List<TLProperty> props = PropertyCodegenUtils.getInheritedFacetProperties( tlFacet );
        List<TLAttribute> attrs = PropertyCodegenUtils.getInheritedFacetAttributes( tlFacet );
        List<TLIndicator> inds = PropertyCodegenUtils.getInheritedFacetIndicators( tlFacet );
        assertTrue( !props.isEmpty() && !attrs.isEmpty() && !inds.isEmpty() );

        // Then - all TL base BO summary properties are included in inherited properties
        List<TLProperty> baseElements = baseBO.getSummary().getTL().getElements();
        assertTrue( "All baseBO attributes must have been reported by codegen utils.",
            attrs.size() == baseBO.getSummary().getTL().getAttributes().size() );
        assertTrue( "All baseBO indicators must have been reported by codegen utils.",
            inds.size() == baseBO.getSummary().getTL().getIndicators().size() );
        // FIXED 1/23/2021
        log.debug( "Base has " + baseElements.size() + " and " + props.size()
            + " returned by PropertyCodegenUtils.getInheritedFacetProperties" );
        assertTrue( "All baseBO elements must have been reported by codegen utils.",
            props.size() == baseElements.size() );

        List<TLModelElement> tli = new ArrayList<>();
        tli.addAll( props );
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
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmBusinessObject bo = TestBusiness.buildOtm( lib, "BaseBO" );
        TestOtmPropertiesBase.buildOneOfEach2( bo.getSummary() );
        OtmCustomFacet cf = TestCustomFacet.buildOtm( bo, "CF1" );

        assertTrue( bo.getFacet( bo.getIdFacet() ) == bo.getIdFacet() );
        assertTrue( bo.getFacet( bo.getSummary() ) == bo.getSummary() );
        assertTrue( bo.getFacet( bo.getDetail() ) == bo.getDetail() );

        OtmBusinessObject exBo1 = TestBusiness.buildOtm( lib, "ExBO1" );
        exBo1.setBaseType( bo );

        OtmBusinessObject exBo = TestBusiness.buildOtm( lib, "ExBO" );
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
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmBusinessObject bo = TestBusiness.buildOtm( lib, "BaseBO" );
        TestOtmPropertiesBase.buildOneOfEach2( bo.getSummary() );
        OtmCustomFacet cf = TestCustomFacet.buildOtm( bo, "CF1" );

        OtmBusinessObject exBo1 = TestBusiness.buildOtm( lib, "ExBO1" );
        exBo1.setBaseType( bo );

        OtmBusinessObject exBo = TestBusiness.buildOtm( lib, "ExBO" );
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
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmBusinessObject baseBO = TestBusiness.buildOtm( lib, "BaseBO" );
        TestOtmPropertiesBase.buildOneOfEach2( baseBO.getSummary() );
        TestOtmPropertiesBase.buildOneOfEach2( baseBO.getIdFacet() );
        TestOtmPropertiesBase.buildOneOfEach2( baseBO.getDetail() );

        // Given - count of children of base bo's summary facet.
        List<TLModelElement> tlBaseProps = baseBO.getSummary().getTLChildren();
        int expectedCount_Summary = baseBO.getSummary().getChildren().size();
        assertTrue( "TLFacet and OtmFacet must have same child count.", tlBaseProps.size() == expectedCount_Summary );

        // Given a second BO with no children
        OtmBusinessObject exBO = TestBusiness.buildOtm( lib, "ExBO" );
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
