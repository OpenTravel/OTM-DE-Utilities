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
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
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

import java.util.List;

/**
 * Test class for Action Facet resource descendants.
 * <p>
 */
public class TestAction extends TestOtmResourceBase<OtmAction> {
    private static Log log = LogFactory.getLog( TestAction.class );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null );
        baseObject = TestBusiness.buildOtm( staticModelManager );
        testResource = TestResource.buildOtm( staticModelManager );

        subject = buildOtm( testResource );
        log.debug( "Before class ran." );
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
        OtmBusinessObject bo = TestBusiness.buildOtm( staticModelManager );
        bo.add( TestCustomFacet.buildOtm( staticModelManager ) );
        bo.add( TestQueryFacet.buildOtm( staticModelManager ) );
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

    public static final String THEPATH = "/MySubjectPath";
    public static final String SUBJECTNAME = "MySubject";
    public static final String TEMPLATE = "ThisIsMyTemplate";

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
        resource.setFirstClass( false );
        // Given - a parent reference to another resource
        OtmResource parent = TestResource.buildParentResource( resource, "Parent", staticModelManager );

        for (OtmAction a : resource.getActions()) {
            assertFalse( a.getEndpointURL().isEmpty() );
            log.debug( "Testing action: " + a + "  Url = " + a.getEndpointURL() );
            // Then - must have static message
            assertTrue( a.getEndpointURL().equals( DexParentRefsEndpointMap.NO_PATH ) );
        }
    }

    @Test
    public void testEndpointPathsFirstClassNoParentRefs() {
        // Given one 1st class resource with ID group
        OtmResource resource = TestResource.buildFullOtm( THEPATH, SUBJECTNAME, staticModelManager );
        TestParamGroup.buildIdGroup( resource );
        DexParentRefsEndpointMap endpoints = resource.getParentRefEndpointsMap();
        assertTrue( "Must be empty when no parent references.", endpoints.size() == 0 );

        for (OtmAction a : resource.getActions()) {
            log.debug( "Testing action: " + a + "  Url = " + a.getEndpointURL() );
            checkURLs( a );
        }
    }

    /**
     * Make sure URL with and with out template on request are correct
     * 
     * @param a
     */
    public static void checkURLs(OtmAction a) {
        assertFalse( a.getEndpointURL().isEmpty() );
        String subjectName = a.getOwningMember().getSubjectName();
        String initialTemplate = a.getRequest().getPathTemplate();
        String url = "";

        if (a.getOwningMember().isFirstClass()) {
            // When - template on request
            a.getRequest().setPathTemplate( TEMPLATE, true );
            url = a.getEndpointURL();
            // Then
            assertTrue( "Must use template.", url.contains( TEMPLATE ) );
            assertFalse( "Must not have subject name.", url.contains( subjectName ) );

            // When - template is null
            a.getRequest().setPathTemplate( null, true );
            url = a.getEndpointURL();
            // Then
            assertFalse( "Must not use template.", url.contains( TEMPLATE ) );
            assertTrue( "Must have subject name.", url.contains( subjectName ) );
        } else {
            // Then - url is fixed
            assertFalse( a.getEndpointURL().startsWith( DexParentRefsEndpointMap.getResourceBaseURL() ) );
        }

        a.getRequest().setPathTemplate( initialTemplate, false );
    }

    @Test
    public void testEndpointPathsParents() {
        // Given a NOT 1st class resource with ID group
        OtmResource resource = TestResource.buildFullOtm( THEPATH, SUBJECTNAME, staticModelManager );
        TestParamGroup.buildIdGroup( resource );
        resource.setFirstClass( false );
        // Given - a parent reference to another resource
        OtmResource parent = TestResource.buildParentResource( resource, "Parent", staticModelManager );

        // Then - the action's URLs are correct
        for (OtmAction a : resource.getActions()) {
            log.debug( "Testing action: " + a + "  Url = " + a.getEndpointURL() );
            checkURLs( a );

            // Then - there is a url for all parent refs
            for (OtmParentRef pr : resource.getAllParentRefs( true )) {
                log.debug( a.getEndpointURL( pr ) );
                log.debug( DexParentRefsEndpointMap.getResourceBaseURL() );
                assertTrue( a.getEndpointURL( pr ).startsWith( DexParentRefsEndpointMap.getResourceBaseURL() ) );
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
                assertTrue( a.getEndpointURL( pr ).startsWith( DexParentRefsEndpointMap.getResourceBaseURL() ) );
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
        OtmParameterGroup group1 = TestParamGroup.buildOtm( resource );
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
        action.add( TestActionResponse.buildOtm( action, af ) );
        return action;
    }
}
