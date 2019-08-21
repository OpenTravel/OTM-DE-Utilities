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
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestResource;

/**
 * Test class for
 * <p>
 */
public class TestDexParentRefEndpointsMap extends TestOtmResourceBase<OtmAction> {
    private static Log log = LogFactory.getLog( TestDexParentRefEndpointsMap.class );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null );
        baseObject = TestBusiness.buildOtm( staticModelManager );
        testResource = TestResource.buildOtm( staticModelManager );

        subject = null;
        log.debug( "Before class ran." );
    }


    @Test
    public void testConstructor() {
        DexParentRefsEndpointMap endpoints = new DexParentRefsEndpointMap( testResource );
        endpoints.print();

        OtmResource resource = TestResource.buildFullOtm( "MySubjectPath", "MySubject", staticModelManager );
        endpoints = new DexParentRefsEndpointMap( resource );
        endpoints.print();
    }

    @Test
    public void testParentRefPaths() {
        // DexActionEndpointMap endpoints = null;
        // Given - one resource
        String resourcePath = "MyCollection";
        String subjectName = "MySubject";
        OtmResource resource = TestResource.buildFullOtm( resourcePath, subjectName, staticModelManager );
        check( resource, 0 );

        // When - multiple levels and multiple parents
        OtmResource parent1 = TestResource.buildParentResource( resource, "Parent1", staticModelManager );
        check( resource, 1 );
        OtmResource gp1 = TestResource.buildParentResource( parent1, "GP_Parent1", staticModelManager );
        check( parent1, 1 );
        check( resource, 2 );
        OtmResource parent2 = TestResource.buildParentResource( resource, "Parent2", staticModelManager );
        OtmResource gp2 = TestResource.buildParentResource( parent2, "GP_Parent2", staticModelManager );
        OtmResource ggp2 = TestResource.buildParentResource( gp2, "GGP_Parent2", staticModelManager );
        check( resource, 5 );
        check( parent2, 2 );
        check( gp2, 1 );
        check( ggp2, 0 );
        OtmResource parent3 = TestResource.buildParentResource( resource, "Parent3", staticModelManager );
        check( resource, 6 );
        // When - an ancestor is made not first class
        gp2.setFirstClass( false );
        // Then - it no longer has its own path
        check( resource, 5 );
        // log.debug( "done" );
    }

    /**
     * NOTE -- use the log print to visually check
     */
    public static void check(OtmResource resource, int expectedPathCount) {
        String pathTemplate = "ThisIsFromParentRefTemplate_";
        String initialTemplate = null;
        DexParentRefsEndpointMap endpoints = resource.getParentRefEndpointsMap();

        // Then - we have the expected number of paths
        log.debug( "***Testing endpoint paths on " + resource );
        endpoints.print();
        assertTrue( resource.getAllParentRefs( true ).size() == expectedPathCount );
        assertTrue( endpoints.size() == expectedPathCount );


        // Then - if there are no parentRefs, then the map is empty
        if (resource.getParentRefs().isEmpty()) {
            assertTrue( endpoints.size() == 0 );
            assertTrue( endpoints.get( resource ).isEmpty() );
        } else {
            // Then - all 1st class parentRefs have an entry in the map
            for (OtmParentRef pr : resource.getAllParentRefs( true )) {
                // for (OtmParentRef pr : resource.getParentRefs()) {
                assertFalse( endpoints.get( pr ).isEmpty() );
                assertFalse( endpoints.get( pr.getParentResource() ).isEmpty() );
                initialTemplate = pr.getPathTemplate();

                // When - the template is empty
                pr.setPathTemplate( null );
                endpoints.build().print();
                // Then - the path contains the parent resource's base path
                assertTrue( endpoints.get( pr ).contains( pr.getParentResource().getBasePath() ) );

                // When - the template is set
                pr.setPathTemplate( pathTemplate + pr.getName() );
                endpoints.build().print();
                // Then - the path contains the resource path
                assertTrue( endpoints.get( pr ).contains( pathTemplate ) );

                pr.setPathTemplate( initialTemplate ); // Restore the template
            }
        }
    }
}
