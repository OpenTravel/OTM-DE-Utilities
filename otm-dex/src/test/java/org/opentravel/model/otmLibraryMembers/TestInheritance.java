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
import org.opentravel.model.otmFacets.OtmCustomFacet;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmFacets.TestFacet;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.TestOtmPropertiesBase;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import java.util.ArrayList;
import java.util.List;

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
        mgr = new OtmModelManager( null, repoManager );
    }

    @Ignore
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
        TestDexFileHandler.loadVersionProject( mgr );

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

    public void getUnmanagedProject() {
        TestDexFileHandler.loadUnmanagedProject( mgr );
        int initialMemberCount = mgr.getMembers().size();
        TestDexFileHandler.loadLocalLibrary( TestDexFileHandler.FILE_TESTLOCALLIBRARY, mgr.getTlModel() );

        // int initialMemberCount = mgr.getMembers().size();
        log.debug( "Model size is now: " + mgr.getLibraries().size() + " libraries and " + mgr.getMembers().size()
            + " members." );
        mgr.getTlModel().getUserDefinedLibraries().forEach( tlLib -> mgr.add( tlLib ) );
        log.debug( "Model size is now: " + mgr.getLibraries().size() + " libraries and " + mgr.getMembers().size()
            + " members." );
        assertTrue( mgr.getMembers().size() > initialMemberCount );
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
