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

package org.opentravel.model.resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.dex.actions.resource.AddResourceResponseAction;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestCore;
import org.opentravel.model.otmLibraryMembers.TestCustomFacet;
import org.opentravel.model.otmLibraryMembers.TestQueryFacet;
import org.opentravel.model.otmLibraryMembers.TestResource;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLHttpMethod;
import org.opentravel.schemacompiler.model.TLResource;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class for Action Facet resource descendants.
 * <p>
 */
public class TestAction extends TestOtmResourceBase<OtmAction> {
    private static Log log = LogFactory.getLog( TestAction.class );

    public static final String THEPATH = "/MySubjectPath";
    public static final String SUBJECTNAME = "MySubject";
    public static final String TEMPLATE = "ThisIsMyTemplate";

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null, null );
        baseObject = TestBusiness.buildOtm( staticModelManager );
        testResource = TestResource.buildOtm( staticModelManager );

        subject = buildOtm( testResource );
        log.debug( "Before class ran." );
    }

    public static void check(OtmAction a) {
        assertTrue( "Must have a request.", a.getRequest() != null );
        assertTrue( "Must have a name (actionId)", a.getName() != null );

        if (a.getOwningMember() != null) {
            assertTrue( a.getOwningMember().getActions().contains( a ) );
            // TLResource must have an action with the TLAction.actionID value
            assertTrue( a.getOwningMember().getTL().getAction( a.getTL().getActionId() ) != null );
        }
    }

    @Test
    public void testChildren() {
        OtmAction a = buildOtm( testResource );
        assertTrue( a.getChildren().size() >= 2 );
        assertTrue( a.getRequest() != null );

        // Then - make sure there are request and responses in the children
        assertTrue( a.getChildren().contains( a.getRequest() ) );
        for (OtmActionResponse r : a.getResponses())
            assertTrue( a.getChildren().contains( r ) );
    }

    // @Test
    // public void testRequestPayload() {
    // // see TestActionRequest
    // }

    @Test
    public void testGetResponses() {
        // Given a business object
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmBusinessObject bo = TestBusiness.buildOtm( lib, "TestBO" );
        TestCustomFacet.buildOtm( bo, "CF1" );
        TestQueryFacet.buildOtm( bo, "QF1" );
        // Given a core object to use as base payload
        OtmCore core = TestCore.buildOtm( staticModelManager );
        // Given a resource
        OtmResource resource = TestResource.buildFullOtm( "SubjectCollection", "MySubject", staticModelManager );
        resource.setSubject( bo );
        // Given an action facet on that resource
        OtmActionFacet af = TestActionFacet.buildOtm( resource );

        int responseCount = 0;
        for (OtmAction action : resource.getActions())
            responseCount += action.getResponses().size();

        // Given a response on the actions
        resource.getActions().forEach( a -> a.add( TestActionResponse.buildOtm( a ) ) );
        // Given - core set as base payload
        af.setBasePayload( core );
        assertTrue( "Must have core as base payload.", af.getBasePayload() == core );

        int newResponseCount = 0;
        for (OtmAction action : resource.getActions())
            newResponseCount += action.getResponses().size();
        assertTrue( newResponseCount > responseCount );

        // Then
        for (OtmAction action : resource.getActions()) {
            List<TLActionResponse> tlResponses = action.getTL().getResponses();
            List<OtmActionResponse> otmResponses = action.getResponses();
            assertTrue( tlResponses.size() == otmResponses.size() );
            int kidCount = 0;
            // Then - must have children that match
            for (OtmObject c : action.getChildren()) {
                if (c instanceof OtmActionResponse)
                    kidCount++;
            }
            assertTrue( kidCount == tlResponses.size() );

            // log.debug( "Found " + otmResponses.size() + " responses." );
        }
    }

    @Test
    public void testSetters() {
        OtmAction a = buildOtm( testResource );

        a.setCommon( true );
        assertTrue( a.isCommon() );
        a.setCommon( false );
        assertFalse( a.isCommon() );
    }

    @Test
    public void testAddingResponseWithInheritedResponses() {
        // Goal - make sure responses added in a minor version are correct and editable
        OtmResource target = TestResource.buildExtendedResource( true );
        OtmResource base = target.getBaseType();
        assertTrue( target.isEditable() );

        List<OtmActionResponse> responses = getAllResponses( base );
        assertTrue( "Given: must have responses.", !responses.isEmpty() );

        List<OtmAction> iActions = target.getInheritedActions();
        assertTrue( "Given: must have inherited actions.", !iActions.isEmpty() );

        // Given - a new action
        OtmResourceChild newRC = target.add( new TLAction() );
        newRC.setName( "TestAction" );
        assertTrue( newRC instanceof OtmAction );
        OtmAction newAction = (OtmAction) newRC;
        TestAction.check( newAction );

        // When - new response created
        OtmActionResponse newRS = newAction.add( new TLActionResponse() );
        assertTrue( newRS != null );
        assertTrue( !newRS.isInherited() );
        TestActionResponse.check( newRS );

        // When - Action is used
        // TODO - move to test AddResourceResponseAction
        try {
            assertTrue( "Action must be editable.", newAction.isEditable() );
            assertTrue( "Action must be enabled.", AddResourceResponseAction.isEnabled( newAction ) );
            AddResourceResponseAction action = (AddResourceResponseAction) DexActions
                .getAction( DexActions.ADDRESOURCERESPONSE, newAction, newAction.getActionManager() );
            assertTrue( "Must get a Dex Action.", action != null );
            OtmActionResponse newRS2 = (OtmActionResponse) action.doIt( null );
            TestActionResponse.check( newRS2, newAction );
            assertTrue( "Must not be inherited.", !newRS2.isInherited() );
        } catch (ExceptionInInitializerError | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static List<OtmActionResponse> getAllResponses(OtmResource r) {
        List<OtmActionResponse> responses = new ArrayList<>();
        r.getActions().forEach( a -> responses.addAll( a.getAllResponses() ) );
        return responses;
    }

    @Test
    public void testInheritedResponses() {
        OtmResource resource = TestResource.buildFullOtm( "Basepath", "BaseResource", staticModelManager );
        OtmResource baseResource = TestResource.buildBaseOtm( resource, staticModelManager );
        // Need to have common actions or the same actionID to have inheritance

        assertTrue( "Given", resource.getExtendedResource() == baseResource );
        assertFalse( "Given", baseResource.getActions().isEmpty() );
        TestResource.getResponses( baseResource ).forEach( r -> TestActionResponse.print( r ) );
        // TestResource.getResponses( resource ).forEach( r -> TestActionResponse.print( r ) );

        // Make sure there are NO inherited children in base resource
        // FIXME
        // for (OtmAction a : baseResource.getActions()) {
        // // log.debug( "Base action - Inherited children? " + a.getInheritedChildren() );
        // assertTrue( "Then base resource actions have no inherited children.", a.getInheritedChildren().isEmpty() );
        // for (OtmActionResponse response : a.getResponses())
        // assertFalse( "Then - this response is not inherited.", response.isInherited() );
        // for (OtmObject response : a.getInheritedChildren()) {
        // assertTrue( response instanceof OtmActionResponse );
        // assertTrue( "Then - this response is inherited.", response.isInherited() );
        // }
        // }

        for (OtmAction a : resource.getActions()) {
            log.debug( "Extended - Inherited children? " + a.getInheritedChildren() );
            for (TLActionResponse tlAR : ResourceCodegenUtils.getInheritedResponses( a.getTL() )) {
                log.debug( "Found one." );
            }
            List<OtmObject> inherited = a.getInheritedChildren();
            List<OtmObject> children = a.getChildren();
            assertTrue( "Then base resource actions has inherited children.", !a.getInheritedChildren().isEmpty() );
            for (OtmActionResponse response : a.getResponses())
                assertFalse( "Then - this response must not be inherited.", response.isInherited() );
            for (OtmObject response : a.getInheritedChildren()) {
                assertTrue( response instanceof OtmActionResponse );
                // FIXME - assertTrue( "Then - this response must be inherited.", response.isInherited() );
            }
        }

        // TODO - test deep inheritance
    }

    @Test
    public void testEndpointPathsNotFirstClass() {
        // Given one 1st class resource with ID group
        OtmResource resource = TestResource.buildFullOtm( THEPATH, SUBJECTNAME, staticModelManager );
        TestParamGroup.buildIdGroup( resource );
        resource.setFirstClass( false );
        DexParentRefsEndpointMap endpoints = resource.getParentRefEndpointsMap();
        assertTrue( "Must be empty when no parent references.", endpoints.size() == 0 );

        for (OtmAction a : resource.getActions()) {
            assertFalse( a.getEndpointURL().isEmpty() );
            log.debug( "Testing action: " + a + "  Url = " + a.getEndpointURL() );
            assertTrue( a.getEndpointURL().equals( DexParentRefsEndpointMap.NO_PATH_NOTFIRSTCLASS_AND_NOPARENTREFS ) );
        }
    }

    @Test
    public void testEndpointPathsNotFirstClassParents() {
        // Given a NOT 1st class resource with ID group
        OtmResource resource = TestResource.buildFullOtm( THEPATH, SUBJECTNAME, staticModelManager );
        TestParamGroup.buildIdGroup( resource );
        // Given - a parent reference to another resource
        OtmResource parent = TestResource.buildParentResource( resource, "Parent", staticModelManager );
        // Must be done after parent created to be valid
        resource.setFirstClass( false );

        for (OtmAction a : resource.getActions()) {
            assertFalse( a.getEndpointURL().isEmpty() );
            log.debug( "Testing action: " + a + "  Url = " + a.getEndpointURL() );
            // Then - must have static message
            assertTrue( a.getEndpointURL().equals( DexParentRefsEndpointMap.NO_PATH ) );
        }
    }



    // Used in ActionDAO
    // wrapper = new ReadOnlyStringWrapper( ((OtmAction) otmObject).getEndpointURL() );
    @Test
    public void testEndpointPathsFirstClassNoParentRefs() {
        // Given one 1st class resource with ID group
        OtmResource resource = TestResource.buildFullOtm( THEPATH, SUBJECTNAME, staticModelManager );
        OtmParameterGroup idGroup = TestParamGroup.buildIdGroup( resource );
        DexParentRefsEndpointMap endpoints = resource.getParentRefEndpointsMap();
        assertTrue( "Must be empty when no parent references.", endpoints.size() == 0 );

        for (OtmAction a : resource.getActions()) {
            OtmActionRequest rq = a.getRequest();
            assertTrue( "Given: ", rq != null );
            log.debug( "initial rq template = " + rq.getPathTemplate() );
            log.debug( "initial resource base path = " + resource.getBasePath() );

            // When - no parameter group
            a.getRequest().setParamGroup( null );
            // Then - use only the template
            log.debug( "Testing action: " + a + "  Url = " + a.getEndpointURL() );
            checkURLs( a );

            // When - param group set
            a.getRequest().setParamGroup( idGroup );
            log.debug( "Testing action with ID group: " + a + "  Url = " + a.getEndpointURL() );
            checkURLs( a );
        }
    }

    /**
     * Check that the URL both with and with out request's template. First class resources use the template, others do
     * not.
     * 
     * @param a
     */
    public static void checkURLs(OtmAction a) {
        assertFalse( a.getEndpointURL().isEmpty() );
        String subjectName = a.getOwningMember().getSubjectName();
        String initialTemplate = a.getRequest().getPathTemplate();
        String url = "";

        if (a.getOwningMember().isFirstClass()) {
            url = a.getEndpointURL();
            assertTrue( "Must not have // in url", !url.substring( 8 ).contains( "//" ) );

            // When - template on request
            a.getRequest().setPathTemplate( TEMPLATE, true );
            url = a.getEndpointURL();
            // Then
            assertTrue( "Must use template.", url.contains( TEMPLATE ) );
            assertTrue( "Must not have subject name.", !url.contains( subjectName ) );
            assertTrue( "Must not have // in url", !url.substring( 8 ).contains( "//" ) );

            // When - template is null
            a.getRequest().setPathTemplate( null, true );
            url = a.getEndpointURL();
            // Then
            assertTrue( "Must not use template.", !url.contains( TEMPLATE ) );
            assertTrue( "Must not have subject name.", !url.contains( subjectName ) );
        } else {
            // Then - url is fixed
            assertFalse(
                a.getEndpointURL().startsWith( DexParentRefsEndpointMap.getResourceBaseURL( a.getOwningMember() ) ) );
        }

        a.getRequest().setPathTemplate( initialTemplate, false );
    }

    @Test
    public void testEndpointPathsParents() {
        // Given a NOT 1st class resource with ID group
        OtmResource resource = TestResource.buildFullOtm( THEPATH, SUBJECTNAME, staticModelManager );
        TestParamGroup.buildIdGroup( resource );
        // Given - a parent reference to another resource
        OtmResource parent = TestResource.buildParentResource( resource, "Parent", staticModelManager );

        // Done after parent assigned to make it valid
        // A non-first class resource without parent references is not allowed because no actions will be published.
        resource.setFirstClass( false );

        // Then - the action's URLs are correct
        for (OtmAction a : resource.getActions()) {
            log.debug( "Testing action: " + a + "  Url = " + a.getEndpointURL() );
            checkURLs( a );

            // Then - there is a url for all parent refs
            for (OtmParentRef pr : resource.getAllParentRefs( true )) {
                log.debug( a.getEndpointURL( pr ) );
                log.debug( DexParentRefsEndpointMap.getResourceBaseURL( resource ) );
                assertTrue(
                    a.getEndpointURL( pr ).startsWith( DexParentRefsEndpointMap.getResourceBaseURL( resource ) ) );
            }
        }
    }

    // Actions can have many paths. Their own and those from all the parentRefs and their parent refs.
    @Test
    public void testEndpointURLsMultipleParents() {
        // Given one resource 1st class resource
        OtmResource resource = TestResource.buildFullOtm( THEPATH, "Subject", staticModelManager );
        TestParamGroup.buildIdGroup( resource );
        // Given - a parent resource
        OtmResource parent = TestResource.buildParentResource( resource, "Parent", staticModelManager );
        // When - there is a grand parent
        OtmResource gp = TestResource.buildParentResource( parent, "Grand", staticModelManager );
        // When - there is a grand parent
        OtmResource ggp = TestResource.buildParentResource( gp, "Agreat", staticModelManager );

        // Then - get URLs
        // Then - the action's URLs are correct
        for (OtmAction a : resource.getActions()) {
            log.debug( "Testing action: " + a + "  Url = " + a.getEndpointURL() );
            checkURLs( a );

            // Then - there is a url for all parent refs
            for (OtmParentRef pr : resource.getAllParentRefs( true )) {
                log.debug( a.getEndpointURL( pr ) );
                assertTrue(
                    a.getEndpointURL( pr ).startsWith( DexParentRefsEndpointMap.getResourceBaseURL( resource ) ) );
            }
        }
    }


    @Test
    public void testRequestContent() {
        // Get the content model of the request
        // NONE
        // Just reference facet
        // Reference facet + base payload properties
    }

    /**
     * **********************************************************************************
     * 
     */
    /**
     * Build an action with one request and response.
     * 
     * @param resource
     * @return
     */
    public static OtmAction buildOtm(OtmResource resource) {
        OtmAction action = new OtmAction( buildTL( resource.getTL() ), resource );

        assertTrue( action.getRequest() != null );
        assertTrue( action.getTL().getRequest() != null );
        assertTrue( resource.getChildren().contains( action ) );
        assertTrue( resource.getTL().getActions().contains( action.getTL() ) );

        return action;
    }

    public static TLAction buildTL(TLResource tlResource) {
        TLAction tla = new TLAction();
        tla.setActionId( "Create" );

        tla.addResponse( TestActionResponse.buildTL( tla ) );
        tla.setRequest( new TLActionRequest() );
        tla.getRequest().setHttpMethod( TLHttpMethod.POST );
        tla.getRequest().setPathTemplate( tlResource.getBasePath() );
        tlResource.addAction( tla );
        return tla;
    }

    /**
     * Build an action with request that has parameter group added to resource
     * 
     * @param resource
     */
    public static OtmAction buildFullOtm(OtmResource resource) {
        assertTrue( "Resource must have base path set.", resource.getBasePath() != null );
        assertTrue( "Resource must have base path set.", !resource.getBasePath().isEmpty() );
        OtmAction action = TestAction.buildOtm( resource );
        assertTrue( resource.getActions().contains( action ) );
        assertTrue( action.getRequest() != null );
        OtmParameterGroup group1 = null;
        if (!resource.getParameterGroups().isEmpty())
            group1 = resource.getParameterGroups().get( 0 );
        if (group1 == null)
            group1 = TestParamGroup.buildOtm( resource );
        action.getRequest().setParamGroup( group1 );
        // To do - assumes group contains a path and query parameter
        return action;
    }

    public static void printResponses(OtmAction action) {
        for (OtmActionResponse r : action.getResponses())
            TestActionResponse.print( r );
    }

    /**
     * @param resource
     * @param af
     */
    public static OtmAction buildFullOtm(OtmResource resource, OtmActionFacet af) {
        OtmAction action = buildFullOtm( resource );
        for (OtmActionResponse res : action.getResponses())
            action.delete( res );
        action.add( TestActionResponse.buildOtm( action, af ) );
        // printResponses( action );
        return action;
    }
}
