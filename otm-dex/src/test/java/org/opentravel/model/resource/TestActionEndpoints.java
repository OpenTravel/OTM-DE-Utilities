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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestResource;

/**
 * Test class for Action Facet endpoint paths.
 * <p>
 */
public class TestActionEndpoints extends TestOtmResourceBase<OtmAction> {
    private static Logger log = LogManager.getLogger( TestActionEndpoints.class );

    public static final String THEPATH = "/MySubjectPath";
    public static final String SUBJECTNAME = "MySubject";
    public static final String TEMPLATE = "ThisIsMyTemplate";

    @BeforeClass
    public static void beforeClass() {
        OtmLibrary lib = TestLibrary.buildOtm();
        staticModelManager = lib.getModelManager();
        baseObject = TestBusiness.buildOtm( lib, SUBJECTNAME );
        testResource = TestResource.buildOtm( lib, SUBJECTNAME + "Resource" );

        subject = TestAction.buildOtm( testResource );
        log.debug( "Before class ran." );
    }

    @Before
    public void beforeTest() {
        staticModelManager.clear();
    }

    /**
     * Check that the URL both with and without request's template.
     * <p>
     * First class resources use the template, others do not.
     * 
     * @param a
     */
    public static void checkURLs(OtmAction a) {
        assertFalse( a.getEndpointURL().isEmpty() );
        // OtmBusinessObject boSubject = a.getOwningMember().getSubject();
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
            assertTrue( "Must not contain subject name.", !url.contains( subjectName ) );
        } else {
            // Then - url is fixed
            assertFalse( "Non-first class URL must not start with base url from its owning resource.",
                a.getEndpointURL().startsWith( DexParentRefsEndpointMap.getResourceBaseURL( a.getOwningMember() ) ) );
        }

        a.getRequest().setPathTemplate( initialTemplate, false );
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



    @Test
    public void testEndpointPathsNotFirstClassNoParentRefs() {
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

    @Test
    public void testEndpointPathsParents() {
        // Given a 1st class resource with ID group
        OtmResource resource = TestResource.buildFullOtm( THEPATH, SUBJECTNAME, staticModelManager );
        TestParamGroup.buildIdGroup( resource );
        // Given - a parent reference to another resource
        OtmResource parent = TestResource.buildParentResource( resource, "Parent", staticModelManager );

        // Make not 1st class.
        // Done after parent assigned to make it valid.
        // A non-first class resource without parent references is not valid
        // because no actions will be published.
        resource.setFirstClass( false );

        // Check the map used by the action.getEndpointURL method
        TestDexParentRefEndpointsMap.check( resource, false );

        // Then - the action's URLs are correct
        for (OtmAction a : resource.getActions()) {
            log.debug( "Testing action: " + a + "  Url = " + a.getEndpointURL() );
            assertTrue( "Given: ", a.getOwningMember() == resource );
            checkURLs( a );

            // Then - there is a url for all parent refs
            for (OtmParentRef pr : resource.getAllParentRefs( true )) {
                // Url created by combining these 3 string
                log.debug( "ResourceURL:    " + DexParentRefsEndpointMap.getResourceBaseURL( a.getOwningMember() ) );
                log.debug( "ParentRef:      " + a.getOwningMember().getParentRefEndpointsMap().get( pr ) );
                log.debug( "Action Contrib: " + DexParentRefsEndpointMap.getContribution( a ) );
                //
                log.debug( "EndpointURL:    " + a.getEndpointURL( pr ) );

                assertTrue( "Action endpoint must start with resouce's base URL.",
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


    /**
     * **********************************************************************************
     * 
     */
}
