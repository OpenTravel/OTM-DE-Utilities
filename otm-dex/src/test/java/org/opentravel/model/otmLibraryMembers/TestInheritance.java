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
import org.opentravel.common.ValidationUtils;
import org.opentravel.model.OtmModelElement;
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
import org.opentravel.model.resource.OtmAction;
import org.opentravel.model.resource.OtmActionFacet;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
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
        mgr = new OtmModelManager( null, repoManager, null );
    }

    /**
     * Uses {@link TestResource#hasCustomFacets()}
     * 
     * @throws VersionSchemeException
     */
    @Test
    public void testResourceActionFacets() throws VersionSchemeException {
        mgr.clear();
        if (!TestDexFileHandler.loadVersionProject( mgr ))
            return; // No editable libraries

        OtmLibrary minorLibrary = TestVersionChain.getMinorInChain( mgr );
        assertTrue( "Given", minorLibrary != null );
        assertTrue( "Given", minorLibrary.isEditable() );
        assertTrue( "Given - minor is empty.", mgr.getMembers( minorLibrary ).isEmpty() );

        OtmLibrary majorLibrary = minorLibrary.getVersionChain().getMajor();
        assertTrue( "Given", majorLibrary != null );
        assertTrue( "Given", !majorLibrary.isEditable() );
        List<OtmLibraryMember> members = majorLibrary.getMembers();
        assertTrue( "Given - major is not empty.", !members.isEmpty() );
        OtmBusinessObject bo = null;
        for (OtmLibraryMember member : members) {
            if (member instanceof OtmBusinessObject)
                bo = (OtmBusinessObject) member;
        }
        assertTrue( "Given - business object must be found.", bo != null );
        assertTrue( "Business object must be valid.", bo.isValid( true ) );

        // Given - a minor version of the business object
        OtmBusinessObject minorBO = (OtmBusinessObject) bo.createMinorVersion( minorLibrary );
        assertTrue( "Must have a minor business object.", minorBO != null );

        // Given - a resource in the minor library
        OtmResource resource = TestResource.buildFullOtm( "http://example.com", "TestResource", minorLibrary, mgr );
        resource.setSubject( bo );
        assertTrue( "Given: ", resource.getSubject() == bo );
        resource.isValid( true );
        // log.debug( ValidationUtils.getMessagesAsString( resource.getFindings() ) );
        // assertTrue( "Must be valid.", resource.isValid() );

        // Given - BO must have custom facets
        OtmContextualFacet cf = TestCustomFacet.buildOtm( mgr, bo );
        minorLibrary.add( cf );
        assertTrue( "Given: ", cf.getWhereContributed().getOwningMember() == bo );
        // TODO - move test into BO and allow for contributed facets
        // TestBusiness.
        // assertTrue( "Given: ", TestResource.hasCustomFacet( bo.getChildren() ) );

        List<OtmObject> baseBoFacets = resource.getSubjectFacets();
        resource.setSubject( minorBO );
        List<OtmObject> minorBoFacets = resource.getSubjectFacets();
        OtmCustomFacet mcf = null;
        for (OtmObject f : minorBoFacets)
            if (f instanceof OtmCustomFacet)
                mcf = (OtmCustomFacet) f;
        assertTrue( "Must have custom facet.", mcf != null );
        assertTrue( "Must have facets from minor bo.", !minorBoFacets.isEmpty() );
        assertTrue( "Must have custom facet.", TestResource.hasCustomFacet( minorBoFacets ) );

        // When - action facet is assigned the custom facet from minor version.
        OtmActionFacet af = resource.getActionFacets().get( 0 );
        af.setReferenceFacet( mcf );
        // Then
        minorBO.refresh();
        List<OtmObject> ik = minorBO.getInheritedChildren();
        List<OtmObject> minorBoFacets2 = resource.getSubjectFacets();
        assertTrue( "Must have facets from minor bo.", !minorBoFacets2.isEmpty() );
        assertTrue( "Must have custom facet.", TestResource.hasCustomFacet( minorBoFacets2 ) );

        // Then - resource is still valid
        resource.isValid( true );
        log.debug( "Validation Results\n" + ValidationUtils.getMessagesAsString( resource.getFindings() ) );

        // FIXME - should be valid, awaiting fix to compiler/tlModel
        // assertTrue( "Must be valid.", resource.isValid( true ) );

        // TODO
        // Test if minor version of BO has inherited custom facets.
        // Then create resource that uses BO and assure it has custom subject facets
        // Then set resource to minor version of BO
        // Assure it has custom facets in subjectFacets list
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

    // Model inherited children depends on codegen util behavior from compiler
    @Test
    public void testInheritedResourceCodegenUtils() {
        // Givens
        OtmResource r = TestResource.buildExtendedResource( true );
        OtmResource rBase = r.getBaseType();
        List<OtmObject> rKids = r.getChildren();
        List<OtmObject> bKids = rBase.getChildren();

        // When - base type set in builder

        // Then - Returns r and rBase TLResources
        List<TLResource> ex = ResourceCodegenUtils.getInheritanceHierarchy( r.getTL() );
        assertTrue( "Codegen Utils must find inheritance hierarchy.", !ex.isEmpty() );

        // Then - actions using the codegenUtils
        for (TLAction tlA : ResourceCodegenUtils.getInheritedActions( r.getTL() )) {
            // Returns both inherited and locally owned actions
            assertTrue( tlA != null );
            OtmAction action = (OtmAction) OtmModelElement.get( tlA );
            TLResource tlOwner = tlA.getOwner();
            assertTrue( tlOwner != null );
            assertTrue( action != null );

            if (rKids.contains( action )) {
                // Locally Owned
                assertTrue( !bKids.contains( action ) );
                assertTrue( action.getOwningMember() == r );
                assertTrue( tlOwner == r.getTL() );
            } else {
                // Inherited
                assertTrue( "Inherited action must be owned by base resource.", action.getOwningMember() == rBase );
                assertTrue( "Inherited action must be owned by base TL resource.", rBase.getTL() == tlOwner );
                assertTrue( "TL Owner must be the resource.", OtmModelElement.get( tlOwner ) == rBase );
                // 1/12/2021 - bKids contains a different action with the same name
                // assertTrue( "Base resource must own inherited action.", bKids.contains( action ) );
            }

        }

        // Then - Action facets, param groups and parent refs will return from hierarchy, filtered to just one of each
        // name
        List<TLModelElement> inheritedList = new ArrayList<>();
        for (TLActionFacet tlAf : ResourceCodegenUtils.getInheritedActionFacets( r.getTL() ))
            inheritedList.add( tlAf );
        for (TLParamGroup tlPG : ResourceCodegenUtils.getInheritedParamGroups( r.getTL() ))
            inheritedList.add( tlPG );
        for (TLResourceParentRef tlPR : ResourceCodegenUtils.getInheritedParentRefs( r.getTL() ))
            inheritedList.add( tlPR );
        // Because the target resource has no children, all the inherited ones must be reported out.
        assertTrue( "Codegen Utils must find 3 inherited children.", inheritedList.size() == 3 );

    }

    @Test
    public void testInheritedResourceChildren() {

        // Givens
        OtmResource target = TestResource.buildExtendedResource( true );
        OtmResource rBase = target.getBaseType();

        // When - base type set in builder

        // Then - base kids are not inherited
        List<OtmObject> biKids = rBase.getInheritedChildren();
        assertTrue( "Given: base does not have inherited kids.", biKids.isEmpty() );
        rBase.getInheritedChildren().forEach( i -> assertTrue( !i.isInherited() ) );

        // Then - target has inherited children
        List<OtmObject> iKids = target.getInheritedChildren();
        assertTrue( "Must have inherited kids.", !iKids.isEmpty() );
        target.getInheritedChildren().forEach( i -> assertTrue( i.isInherited() ) );
        assertTrue( "Must inherit all children.", rBase.getChildren().size() == target.getInheritedChildren().size() );

        // Then - each type of getInherited* returns only inherited
        target.getInheritedActions().forEach( ic -> assertTrue( ic.isInherited() ) );
        target.getInheritedActionFacets().forEach( ic -> assertTrue( ic.isInherited() ) );
        target.getInheritedParameterGroups().forEach( ic -> assertTrue( ic.isInherited() ) );
        target.getInheritedParentRefs().forEach( ic -> assertTrue( ic.isInherited() ) );

        // // Inheritance is set up when modeled by setting the inheritedFrom field
        // // Inheritance test used for properties does not work. If it does, we should use it.
        // for (OtmAction action : r.getInheritedActions()) {
        //
        // // Using these as the test confuses the constructor uses isInherited to add to correct list.
        // boolean test = action.getTL().getOwner() != action.getParent().getTL();
        // // assertTrue( "This is a viable inheritance test for resource children.", test );
        // boolean test2 = ((OtmChildrenOwner) action.getParent()).getChildren().contains( action );
        // // assertTrue( "This test works.", test2 );
        //
        // boolean test3 = action.getInheritedFrom() != null;
        // boolean inherited = action.isInherited();
        // log.debug( "Must be inherited: " + inherited + test + test2 + test3 );
        // assertTrue( "Must be inherited.", inherited );
        // }
    }

    // // Unused. Not sure the logic is correct.
    // public void getUnmanagedProject() {
    // TestDexFileHandler.loadUnmanagedProject( mgr );
    // int initialMemberCount = mgr.getMembers().size();
    // TestDexFileHandler.loadLocalLibrary( TestDexFileHandler.FILE_TESTLOCALLIBRARY, mgr.getTlModel() );
    //
    // // int initialMemberCount = mgr.getMembers().size();
    // log.debug( "Model size is now: " + mgr.getLibraries().size() + " libraries and " + mgr.getMembers().size()
    // + " members." );
    // mgr.getTlModel().getUserDefinedLibraries().forEach( tlLib -> mgr.add( tlLib ) );
    // log.debug( "Model size is now: " + mgr.getLibraries().size() + " libraries and " + mgr.getMembers().size()
    // + " members." );
    // assertTrue( mgr.getMembers().size() > initialMemberCount );
    // }


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
