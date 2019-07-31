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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestResource;

import java.util.List;

/**
 * Test class for Action Facet resource descendants.
 * <p>
 */
public class TestDexResourcePathHandler extends TestOtmResourceBase<OtmAction> {
    private static Log log = LogFactory.getLog( TestDexResourcePathHandler.class );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null );
        baseObject = TestBusiness.buildOtm( staticModelManager );
        testResource = TestResource.buildOtm( staticModelManager );

        subject = null;
        log.debug( "Before class ran." );
    }


    @Test
    public void testHandlerConstructor() {
        DexResourcePathHandler pathHandler = new DexResourcePathHandler( testResource );

        log.debug( pathHandler );

        testResource.getActions().forEach( a -> {
            log.debug( pathHandler.get( a ) );
        } );
    }

    @Test
    public void testGetURL() {
        // Given a new resource and path handler
        OtmResource resource = TestResource.buildOtm( staticModelManager );
        DexResourcePathHandler pathHandler = new DexResourcePathHandler( resource );
        // Given a subject
        OtmBusinessObject testBO = TestBusiness.buildOtm( staticModelManager );
        resource.setAssignedType( testBO );
        // Given an action with request that has parameter group added to resource
        buildActionsWithParameters( resource );
        // Given a base path
        String pathString = "base/path";
        resource.setBasePath( pathString );
        // Given an action facet named "af1"
        OtmActionFacet af = TestActionFacet.buildOtm( resource );

        resource.getActions().forEach( a -> {
            if (a.getRequest() != null)
                a.getRequest().setPayloadType( af );

            String url = pathHandler.get( a );
            // Then - all actions will have collection and base path
            assertFalse( url.isEmpty() );
            assertTrue( url.contains( pathString ) );
            assertTrue( url.contains( testBO.getName() ) );

            log.debug( url );
        } );
    }

    @Test
    public void testPayloadExample() {
        OtmResource resource = TestResource.buildOtm( staticModelManager );
        DexResourcePathHandler pathHandler = new DexResourcePathHandler( resource );
        buildActionsWithParameters( resource );
        // Given an action facet named "af1"
        OtmActionFacet af = TestActionFacet.buildOtm( resource );

        for (OtmActionRequest request : resource.getActionRequests()) {
            request.setPayloadType( af );
            String ex = pathHandler.getPayloadExample( request );
            assertFalse( ex.isEmpty() );
            assertTrue( ex.contains( af.getName() ) );
            log.debug( ex );
        }
    }

    @Test
    public void testCollectionContribution() {
        // Given a new resource and path handler
        OtmBusinessObject testBO = TestBusiness.buildOtm( staticModelManager );
        OtmResource resource = TestResource.buildOtm( staticModelManager );
        DexResourcePathHandler pathHandler = new DexResourcePathHandler( resource );
        buildActionsWithParameters( resource );
        resource.setAssignedType( testBO );

        // When - action requests do not have path templates
        String newCollectionContribution = "From/The/Requst/Template/{someID}";
        for (OtmActionRequest request : resource.getActionRequests()) {
            String c = pathHandler.getCollectionContribution( request );
            assertNotNull( c );
            assertTrue( c.startsWith( "/" ) );
            log.debug( c );

            // When the request path template is set
            request.getTL().setPathTemplate( newCollectionContribution );
            // Then the contribution changes
            String c2 = pathHandler.getCollectionContribution( request );
            assertNotNull( c2 );
            assertTrue( c2.startsWith( "/" ) );
            assertFalse( c2.endsWith( "}" ) );
            assertTrue( c != c2 );
            log.debug( c2 );
        }
    }

    @Test
    public void testParameters() {
        // Given a new resource and path handler
        OtmResource resource = TestResource.buildOtm( staticModelManager );
        DexResourcePathHandler pathHandler = new DexResourcePathHandler( resource );

        // Given an action with request that has parameter group added to resource
        buildActionsWithParameters( resource );

        // Path parameters come from the parameter group associated with an action request
        List<OtmActionRequest> requests = resource.getActionRequests();
        for (OtmActionRequest r : requests) {
            String pathContribution = pathHandler.getPathParameterContributions( r );
            assertTrue( pathContribution != null );
            String queryContribution = pathHandler.getQueryParameterContributions( r );
            assertTrue( queryContribution != null );
        }
        log.debug( "Done testing parameter contributions." );
    }

    /**
     * Build an action with request that has parameter group added to resource
     * 
     * @param resource
     */
    public void buildActionsWithParameters(OtmResource resource) {
        OtmAction action = TestAction.buildOtm( resource );
        assertTrue( resource.getActions().contains( action ) );
        assertTrue( action.getRequest() != null );
        OtmParameterGroup group1 = TestParamGroup.buildOtm( resource );
        action.getRequest().setParamGroup( group1 );
        // To do - assumes group contains a path and query parameter
    }

    @Test
    public void testBasePath() {
        // Given a new resource and path handler
        OtmResource resource = TestResource.buildOtm( staticModelManager );
        DexResourcePathHandler pathHandler = new DexResourcePathHandler( resource );

        // When base path is null
        resource.setBasePath( null );
        // then it returns empty string
        assertNotNull( pathHandler.getBasePathContribution() );

        // When path starts with non-slash
        String pathString = "foo/bar";
        resource.setBasePath( pathString );
        // Then
        assertTrue( pathHandler.getBasePathContribution().startsWith( "/" ) );
        assertTrue( pathHandler.getBasePathContribution().contains( pathString ) );

        // When path ends with slash
        String pathString2 = pathString + "/";
        resource.setBasePath( pathString2 );
        // Then
        assertTrue( pathHandler.getBasePathContribution().startsWith( "/" ) );
        assertTrue( pathHandler.getBasePathContribution().contains( pathString ) );
        assertFalse( pathHandler.getBasePathContribution().endsWith( "/" ) );

    }
}
