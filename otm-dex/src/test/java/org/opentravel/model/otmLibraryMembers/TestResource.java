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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.common.ValidationUtils;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmCustomFacet;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.resource.OtmAction;
import org.opentravel.model.resource.OtmActionFacet;
import org.opentravel.model.resource.OtmActionRequest;
import org.opentravel.model.resource.OtmActionResponse;
import org.opentravel.model.resource.OtmParameter;
import org.opentravel.model.resource.OtmParameterGroup;
import org.opentravel.model.resource.OtmParentRef;
import org.opentravel.model.resource.RestStatusCodes;
import org.opentravel.model.resource.TestAction;
import org.opentravel.model.resource.TestActionFacet;
import org.opentravel.model.resource.TestActionResponse;
import org.opentravel.model.resource.TestParamGroup;
import org.opentravel.model.resource.TestParentRef;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLHttpMethod;
import org.opentravel.schemacompiler.model.TLMimeType;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLReferenceType;
import org.opentravel.schemacompiler.model.TLResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Verifies the functions of the <code>OtmResource</code> class.
 */
public class TestResource extends TestOtmLibraryMemberBase<OtmResource> {
    private static final String R_NAME = "Testbo";

    private static Log log = LogFactory.getLog( TestResource.class );
    private static OtmBusinessObject exposedObject;

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null, null );
        subject = buildOtm( staticModelManager );
        baseObject = buildOtm( staticModelManager );
        baseObject.setName( "BaseR" );

        exposedObject = TestBusiness.buildOtm( staticModelManager );
    }

    @Test
    public void testGetSubjectFacets() {
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmBusinessObject bbo = TestBusiness.buildOtm( lib, "BaseBo" );
        OtmContextualFacet cf = TestCustomFacet.buildOtm( lib.getModelManager(), bbo );
        OtmBusinessObject ebo = TestBusiness.buildOtm( lib, "ExtendedBo" );
        ebo.setBaseType( bbo );
        assertTrue( "Given", ebo.getBaseType() == bbo );
        // Remove all custom facets from extended business object.
        Collection<OtmContributedFacet> cfs = new ArrayList<>( ebo.getChildrenContributedFacets() );
        cfs.forEach( c -> ebo.delete( c ) );
        assertTrue( "Given: must not have custom facets.", !hasCustomFacet( ebo.getChildren() ) );

        // When - resource assigned base object
        OtmResource r = buildFullOtm( "http://example.com", "TestResource", lib, lib.getModelManager() );
        r.setSubject( bbo );
        List<OtmObject> subjectFacets = r.getSubjectFacets();
        assertTrue( "Given", !subjectFacets.isEmpty() );
        assertTrue( "Must have contributed facets.", hasCustomFacet( subjectFacets ) );

        // When - resource created with extended BO
        OtmResource r2 = buildFullOtm( "http://example.com", "TestResource2", lib, lib.getModelManager() );
        r2.setSubject( ebo );
        // Then
        subjectFacets = r2.getSubjectFacets();
        assertTrue( "Given", !subjectFacets.isEmpty() );
        assertTrue( "Must have custom facets.", hasCustomFacet( subjectFacets ) );
    }


    /**
     * @param subjectFacets
     * @param result
     * @return
     */
    public static boolean hasCustomFacet(List<OtmObject> subjectFacets) {
        boolean result = false;
        for (OtmObject f : subjectFacets)
            if (f instanceof OtmCustomFacet)
                result = true;
        return result;
    }


    @Test
    public void testBuildResource() {
        OtmResource r = buildOtm( staticModelManager );

        // Then
        assertNotNull( r );
        assertTrue( !r.getChildren().isEmpty() );
    }

    @Test
    public void testBuildValidResource() {
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmResource resource =
            TestResource.buildFullValidOtm( "http://example.com", "TestResource", lib, lib.getModelManager() );
        assertTrue( ": ", resource != null );
    }

    @Override
    @Test
    public void testTypeUser() {
        OtmResource r = buildOtm( staticModelManager );

        assertTrue( "No business object assigned yet.", r.getAssignedType() == null );

        // run base tests
        r.setAssignedType( (OtmTypeProvider) exposedObject );
        assertTrue( r.getAssignedType() == exposedObject );

        super.testTypeUser( r );
    }

    @Test
    public void testFacets() {
        OtmResource testResource = buildOtm( staticModelManager );
        int kidCount = testResource.getChildren().size();

        TestAction.buildOtm( testResource );
        TestActionFacet.buildOtm( testResource );
        TestParamGroup.buildOtm( testResource );
        TestParentRef.buildOtm( testResource );

        assertTrue( kidCount + 4 == testResource.getChildren().size() );
    }

    @Test
    public void testActions() {
        OtmResource r = buildOtm( staticModelManager );
        assertTrue( r.getActions().size() >= 1 );
    }

    @Test
    public void testParameterGroups() {
        // Given a new resource with one parameter group
        OtmResource r = buildOtm( staticModelManager );
        assertTrue( r.getParameterGroups().size() >= 1 );
        int groupCount = r.getParameterGroups().size();

        // Given - two parameter groups
        OtmParameterGroup group1 = new OtmParameterGroup( new TLParamGroup(), null );
        OtmParameterGroup group2 = new OtmParameterGroup( new TLParamGroup(), null );

        // When - one group is added
        addParameterGroup( r, group1 );
        assertTrue( r.getParameterGroups().size() == groupCount + 1 );
        assertTrue( r.getParameterGroups().contains( group1 ) );
        assertTrue( group1.getOwningMember() == r );
        groupCount++;

        // When - one group is added
        addParameterGroup( r, group2 );
        assertTrue( r.getParameterGroups().size() == groupCount + 1 );
        groupCount++;
    }

    public void addParameterGroup(OtmResource r, OtmParameterGroup group) {
        if (group != null) {
            r.getTL().addParamGroup( group.getTL() );
            r.add( group ); // Add to children list
            group.setParent( r );
        }
    }

    @Test
    public void testSubject() {
        OtmResource resource = buildOtm( staticModelManager );
        OtmBusinessObject testBO = TestBusiness.buildOtm( staticModelManager );

        // Initially, no subject is set
        assertTrue( resource.getSubject() == null );
        assertTrue( resource.getSubjectName().isEmpty() );

        // When set
        resource.setAssignedType( testBO );
        assertTrue( resource.getAssignedType() == testBO );
        assertTrue( resource.getSubject() == testBO );
        assertFalse( resource.getSubjectName().isEmpty() );
    }

    // Test one parent
    @Test
    public void testParentRefs() {
        // Given - two resources
        OtmResource resource = buildOtm( staticModelManager );
        OtmResource parent = buildOtm( staticModelManager );
        assertTrue( staticModelManager.getResources( false ).contains( resource ) );
        assertTrue( staticModelManager.getResources( false ).contains( parent ) );

        // When - parent ref is added
        OtmParentRef parentRef = TestParentRef.buildOtm( resource, parent );
        assertTrue( parent.getAllSubResources().contains( resource ) );

        // Then - the parent ref check is OK
        TestParentRef.check( parentRef, resource, parent );
    }

    // Test parent and grandparent
    @Test
    public void testParentAndGrandparentRefs() {
        // Given - 4 resources
        OtmResource resource = buildOtm( staticModelManager );
        OtmResource parent1 = buildOtm( staticModelManager );
        OtmResource parent2 = buildOtm( staticModelManager );
        OtmResource parent3 = buildOtm( staticModelManager );

        // When - parent ref is added
        OtmParentRef parentRef3 = TestParentRef.buildOtm( parent2, parent3 );
        OtmParentRef parentRef2 = TestParentRef.buildOtm( parent1, parent2 );
        OtmParentRef parentRef1 = TestParentRef.buildOtm( resource, parent1 );

        assertTrue( resource.getParentRefs().size() == 1 );
        assertTrue( parent1.getParentRefs().size() == 1 );
        assertTrue( parent2.getParentRefs().size() == 1 );

        // Then - sub-resources can be found
        assertTrue( parent1.getAllSubResources().contains( resource ) );
        assertTrue( parent2.getAllSubResources().contains( resource ) );

        // Then - the parent ref check is OK
        TestParentRef.check( parentRef1, resource, parent1 );
        TestParentRef.check( parentRef2, parent1, parent2 );
        TestParentRef.check( parentRef3, parent2, parent3 );
    }

    // Test multiple parents
    @Test
    public void testParentsRefs() {
        // Given - 4 resources
        OtmResource resource = buildOtm( staticModelManager );
        OtmResource parent1 = buildOtm( staticModelManager );
        OtmResource parent2 = buildOtm( staticModelManager );
        OtmResource parent3 = buildOtm( staticModelManager );

        // When - parent ref is added
        OtmParentRef parentRef1 = TestParentRef.buildOtm( resource, parent1 );
        OtmParentRef parentRef2 = TestParentRef.buildOtm( resource, parent2 );
        OtmParentRef parentRef3 = TestParentRef.buildOtm( resource, parent3 );

        // Then - 3 parent refs
        assertTrue( resource.getParentRefs().size() == 3 );
        // Then - the parent ref check is OK
        TestParentRef.check( parentRef1, resource, parent1 );
        TestParentRef.check( parentRef2, resource, parent2 );
        TestParentRef.check( parentRef3, resource, parent3 );
    }

    @Test
    public void testActionRequests() {
        OtmResource testResource = buildOtm( staticModelManager );

        // Given - how many requests have been created
        List<OtmActionRequest> requests = testResource.getActionRequests();
        int initialRQsize = requests.size();

        // When - two actions are added
        OtmAction action1 = TestAction.buildOtm( testResource );
        OtmAction action2 = TestAction.buildOtm( testResource );

        requests = testResource.getActionRequests();
        // Then there must be 2 more requests
        assertTrue( requests.size() == initialRQsize + 2 );
    }


    /** ****************************************************** **/

    /**
     * Create a "Parent", "Base" and "Target" resources in a new library and model. If deleteKids, Target has no
     * children. Target extends Base. Base has parent reference to Parent.
     * 
     * @return
     */
    public static OtmResource buildExtendedResource(boolean deleteKids) {

        // Given - a base resource with a parent
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmResource rBase = buildFullOtm( "http://example.com", "Base", lib, lib.getModelManager() );
        OtmResource rParent = buildParentResource( rBase, "Parent", lib.getModelManager() );

        // Given - a resource that will extend the base.
        OtmResource r = TestResource.buildOtm( lib, "Target" );

        if (deleteKids) {
            // Make sure the tested resource start with no children to eliminate any name contention.
            // TODO - test with a same name Action to simulate being in an edited minor.
            List<OtmObject> rKids = new ArrayList<>( r.getChildren() );
            rKids.forEach( k -> r.delete( k ) );
            assertTrue( "Given: resource does NOT have kids.", r.getChildren().isEmpty() );
        } else {
            assertTrue( "Given: resource does have kids.", !r.getChildren().isEmpty() );

        }

        // Set base type
        r.setBaseType( rBase );

        assertTrue( "Given: ", r.getTL().getExtension() != null );
        assertTrue( "Given: has base type.", r.getBaseType() == rBase );
        assertTrue( "Given: base has kids.", !rBase.getChildren().isEmpty() );

        return r;
    }

    /**
     * Return list of responses accumulated from each action.getResponses()
     * 
     * @param resource
     * @return
     */
    public static List<OtmActionResponse> getResponses(OtmResource resource) {
        List<OtmActionResponse> responses = new ArrayList<>();
        resource.getActions().forEach( a -> responses.addAll( a.getResponses() ) );
        return responses;
    }

    /**
     * Create OtmResource with one action facet, one action and one parameter group
     * 
     * @param mgr
     * @return
     */
    public static OtmResource buildOtm(OtmModelManager mgr) {
        OtmResource resource = new OtmResource( buildTL(), mgr );
        mgr.add( resource );
        assertNotNull( resource );
        assertTrue( resource.getChildren().size() == 3 );
        return resource;
    }

    /**
     * @param lib
     * @param name
     * @return
     */
    public static OtmResource buildOtm(OtmLibrary lib, String name) {
        OtmResource resource = buildOtm( lib.getModelManager() );
        resource.setName( name );
        lib.add( resource );
        assertTrue( lib.contains( resource ) );
        return resource;
    }


    public static final String BASEPATH = "/SomeBasePath";

    public static TLResource buildTL() {
        TLResource tlr = new TLResource();
        tlr.setName( R_NAME );
        tlr.setBasePath( BASEPATH );

        TLActionFacet tlaf = new TLActionFacet();
        tlr.addActionFacet( tlaf );
        tlaf.setName( R_NAME + "af1" );

        TLAction tla = new TLAction();
        tla.setActionId( R_NAME + "a1" );
        tlr.addAction( tla );
        // All actions must have a request
        TLActionRequest tlar = new TLActionRequest();
        tlar.setHttpMethod( TLHttpMethod.GET );
        tla.setRequest( tlar );
        tlar.setPathTemplate( BASEPATH );

        TLParamGroup tlpg = new TLParamGroup();
        tlpg.setName( R_NAME + "pg1" );
        tlr.addParamGroup( tlpg );

        // No parent Ref
        return tlr;
    }
    // TestResource [DEBUG] A facet reference is required for all parameter groups.
    // At least one parameter must be declared or inherited for a parameter group.
    // A facet reference is required for all parameter groups.
    // ** At least one parameter must be declared or inherited for a parameter group.
    // A field reference is required for all parameters.
    // A field reference is required for all parameters.
    // The action facet reference type field is a required value.
    // The action facet reference type field is a required value.
    // At least one response must be declared or inherited for a resource action.
    // The path template is not properly formatted: "http://example.com"
    // The specified MIME type(s) will be ignored since the an action facet is not referenced.

    /**
     * Create 1st class resource with its own Subject, ID parameter group and make it a parent to the passed resource.
     * Set ID Group in the parent resource reference.
     * 
     * @param r resource to make into sub-resource
     * @param name to give parent resource and its subject
     * @param mgr
     * @return
     */
    public static OtmResource buildParentResource(OtmResource r, String name, OtmModelManager mgr) {
        // Given a subject for the resource
        OtmBusinessObject parentBO = TestBusiness.buildOtm( mgr, name );
        // String parentNameString = name + "BO";
        // parentBO.setName( parentNameString );

        // Create the parent resource with path
        String parentPathString = "/" + name + "Path";
        OtmResource parentR = TestResource.buildOtm( mgr );
        parentR.setName( name );
        parentR.setAssignedType( parentBO );
        parentR.setBasePath( parentPathString );
        parentR.getTL().setFirstClass( true );
        OtmParameterGroup idGroup = TestParamGroup.buildIdGroup( parentR );

        OtmParentRef parentRef = TestParentRef.buildOtm( r, parentR );
        parentRef.getTL().setParentParamGroup( idGroup.getTL() );
        parentRef.getTL().setPathTemplate( null ); // do NOT use the override

        assertTrue( parentRef.getParentResource() == parentR );
        assertTrue( parentRef.getParameterGroup() == idGroup );
        assertTrue( idGroup.getOwningMember() == parentR );

        return parentR;
    }

    /**
     * Resource and constructed subject are placed into the passed library.
     * 
     * @param pathString
     * @param subjectName
     * @param lib
     * @param modelManager
     * @return
     */
    public static OtmResource buildFullOtm(String pathString, String subjectName, OtmLibrary lib, OtmModelManager mgr) {
        OtmResource r = buildFullOtm( pathString, subjectName, mgr );
        lib.add( r );
        assertTrue( r.getLibrary() == lib );
        lib.add( r.getSubject() );
        if (lib.isEditable())
            assertTrue( r.isEditable() );
        else
            assertTrue( !r.isEditable() );
        return r;
    }

    /**
     * Resource and constructed subject are placed into the passed library.
     * 
     * @param pathString
     * @param subjectName
     * @param lib
     * @param modelManager
     * @return
     */
    public static OtmResource buildFullValidOtm(String pathString, String subjectName, OtmLibrary lib,
        OtmModelManager mgr) {
        OtmResource r = buildFullOtm( pathString, subjectName, mgr );
        lib.add( r );
        assertTrue( r.getLibrary() == lib );
        lib.add( r.getSubject() );
        if (lib.isEditable())
            assertTrue( r.isEditable() );
        else
            assertTrue( !r.isEditable() );

        r.isValid( true );
        log.debug( "Validation Results.\n" + ValidationUtils.getMessagesAsString( r.getFindings() ) );
        assertTrue( "Must be valid.", r.isValid() );

        return r;
    }

    /**
     * Build a fully structured resource with:
     * <ul>
     * <li>subject
     * <li>base path
     * <li>First class set to true
     * <li>actions with payload
     * <li>an action with request that has parameter group added to resource
     * <li>an action facet named "af1"
     * </ul>
     * 
     * @param pathString
     * @return
     */
    public static OtmResource buildFullOtm(String pathString, String subjectName, OtmModelManager mgr) {
        OtmResource resource = TestResource.buildOtm( mgr );
        resource.setName( subjectName + "Resource" );
        resource.setFirstClass( true );

        OtmBusinessObject testBO = TestBusiness.buildOtm( mgr );
        testBO.setName( subjectName );
        resource.setAssignedType( testBO );

        // Update parameter group for the BO
        OtmParameterGroup pg = resource.getParameterGroups().get( 0 );
        pg.setReferenceFacet( testBO.getIdFacet() );
        pg.setIdGroup( true );
        OtmParameter param = pg.add( new TLParameter() );
        param.setFieldRef( (OtmProperty) testBO.getIdFacet().getChildren().get( 0 ) );
        assertTrue( "Given: one parameter group.", resource.getParameterGroups().size() == 1 );

        // Action Facets
        OtmActionFacet af = null;
        if (!resource.getActionFacets().isEmpty())
            af = resource.getActionFacets().get( 0 );
        else
            af = TestActionFacet.buildOtm( resource );
        af.setReferenceFacet( testBO.getIdFacet() );
        af.setReferenceType( TLReferenceType.OPTIONAL );
        assertTrue( "Given: must have reference facet.", af.getReferenceFacet() != null );
        assertTrue( "Given: one action facet.", resource.getActionFacets().size() == 1 );
        assertTrue( af.isValid() );

        // Actions
        List<OtmAction> actions = resource.getActions();
        for (OtmAction a : actions)
            resource.delete( a );
        OtmAction action = TestAction.buildFullOtm( resource, af );
        OtmActionRequest req = action.getRequest();
        String defaultTemplate = req.getPathTemplateDefault();
        req.setPathTemplate( defaultTemplate, true );
        req.setMimeTypes( null );
        assertTrue( "Given: action must have response.", !action.getResponses().isEmpty() );
        assertTrue( "Given: one action.", resource.getActions().size() == 1 );
        log.debug( ValidationUtils.getMessagesAsString( action.getFindings() ) );
        assertTrue( action.isValid() );

        // resource.getBasePath()
        // How to get the default base path?
        resource.setBasePath( pathString + "/{idAttr_Testbo}" );
        resource.setBasePath( "/" );

        assertTrue( "Given: one parameter group.", resource.getParameterGroups().size() == 1 );
        return resource;
    }


    /**
     * Build an abstract base resource with common actions, 1 action facet and action response for each status code.
     * 
     * @param resource
     * @param staticModelManager
     * @return
     */
    public static OtmResource buildBaseOtm(OtmResource resource, OtmModelManager mgr) {
        OtmResource base = TestResource.buildOtm( mgr );
        base.setBasePath( "BasePath" );
        base.setFirstClass( false );
        base.setAbstract( true );
        OtmActionFacet af = TestActionFacet.buildOtm( base );
        af.setName( "BaseAF" );

        // Make all the actions common actions
        OtmAction action = TestAction.buildOtm( base );
        base.getActions().forEach( a -> a.setCommon( true ) );
        // Make responses
        for (RestStatusCodes statusCode : RestStatusCodes.values()) {
            OtmActionResponse response = TestActionResponse.buildOtm( action, af );
            List<Integer> codes = new ArrayList<>( response.getTL().getStatusCodes() );
            codes.forEach( c -> response.getTL().removeStatusCode( c ) );
            response.getTL().addStatusCode( statusCode.value() );
            response.getTL().addMimeType( TLMimeType.APPLICATION_JSON );
            response.getTL().addMimeType( TLMimeType.APPLICATION_XML );
        }
        resource.setExtendedResource( base );

        assertTrue( "Given", resource.getExtendedResource() == base );
        assertFalse( "Given", base.getActions().isEmpty() );

        return base;
    }

}
