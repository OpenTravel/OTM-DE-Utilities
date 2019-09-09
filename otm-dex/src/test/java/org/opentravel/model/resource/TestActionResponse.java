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

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestCore;
import org.opentravel.model.otmLibraryMembers.TestCustomFacet;
import org.opentravel.model.otmLibraryMembers.TestQueryFacet;
import org.opentravel.model.otmLibraryMembers.TestResource;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLReferenceType;

/**
 * Test class for Action response .
 * <p>
 */
public class TestActionResponse<L extends TestOtmResourceBase<OtmActionResponse>>
    extends TestOtmResourceBase<OtmActionResponse> {
    private static Log log = LogFactory.getLog( TestActionResponse.class );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null );
        baseObject = TestBusiness.buildOtm( staticModelManager );
        testResource = TestResource.buildOtm( staticModelManager );

        OtmAction testAction = TestAction.buildOtm( testResource );
        subject = buildOtm( testAction );
        log.debug( "Before class ran." );
    }

    @Test
    public void testSetters() {
        // Given a business object
        OtmBusinessObject bo = TestBusiness.buildOtm( staticModelManager );
        bo.add( TestCustomFacet.buildOtm( staticModelManager ) );
        bo.add( TestQueryFacet.buildOtm( staticModelManager ) );
        // Given a core object to use as base payload
        OtmCore core = TestCore.buildOtm( staticModelManager );
        // Given a resource
        OtmResource resource = TestResource.buildFullOtm( "SubjectCollection", "MySubject", staticModelManager );
        resource.setSubject( bo );

        for (OtmAction action : resource.getActions())
            for (OtmActionResponse response : action.getResponses()) {
                response.getPayloadCandidates()
                    .forEach( c -> assertTrue( response.setPayloadActionFacetString( c ).getName().equals( c ) ) );
            }
    }

    /**
     * User is shown ???
     */
    @Test
    public void testResponsePayloadNoBase() {
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
        OtmActionFacet afNoRefFacet = TestActionFacet.buildOtm( resource );
        afNoRefFacet.setName( ACTIONFACETNAME + "NRF" );
        // Given - no reference facet
        afNoRefFacet.setReferenceFacet( null );
        // Given - no base payload
        afNoRefFacet.setBasePayload( null );
        assertTrue( "Must have core as base payload.", afNoRefFacet.getBasePayload() == null );

        // Given an action facet on that resource
        OtmActionFacet afWithRefFacet = TestActionFacet.buildOtm( resource );
        afWithRefFacet.setName( ACTIONFACETNAME + "WRF" );
        // Given - with a reference facet
        afWithRefFacet.setReferenceFacet( bo.getSummary() );
        // Given - no base payload
        afWithRefFacet.setBasePayload( null );
        assertTrue( afWithRefFacet.getBasePayload() == null );

        // Given at least one response on each action
        resource.getActions().forEach( a -> a.add( buildOtm( a ) ) );

        // Then
        for (OtmAction action : resource.getActions())
            for (OtmActionResponse ar : action.getResponses()) {
                // When action facet is NOT set
                ar.setPayloadActionFacet( null );
                // Then - the payload is empty
                assertTrue( ar.getPayloadName().isEmpty() );

                // When action facet has NO reference facet
                ar.setPayloadActionFacet( afNoRefFacet );
                // Then - payload name is base payload name
                log.debug( ar.getPayloadName() );
                assertTrue( afNoRefFacet.getName().equals( ar.getPayloadName() ) ); // Used in GUI

                // When action facet has reference facet
                ar.setPayloadActionFacet( afWithRefFacet );
                // Then - same payload name which is base payload name
                log.debug( ar.getPayloadName() );
                assertTrue( ar.getPayloadName().contains( resource.getSubject().getName() ) ); // Used in GUI
            }
    }

    /**
     * test with basePayload set
     */
    public static final String ACTIONFACETNAME = "AF123_MyActionFacet";

    @Test
    public void testResponsePayloadWithBasePayload() {
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
        OtmActionFacet afNoRefFacet = TestActionFacet.buildOtm( resource );
        afNoRefFacet.setName( ACTIONFACETNAME );
        // Given - no reference facet
        afNoRefFacet.setReferenceFacet( null );
        // Given - core set as base payload
        afNoRefFacet.setBasePayload( core );
        assertTrue( "Must have core as base payload.", afNoRefFacet.getBasePayload() == core );

        // Given an action facet on that resource
        OtmActionFacet afWithRefFacet = TestActionFacet.buildOtm( resource );
        afWithRefFacet.setName( ACTIONFACETNAME );
        // Given - no reference facet
        afWithRefFacet.setReferenceFacet( bo.getSummary() );
        // Given - core set as base payload
        afWithRefFacet.setBasePayload( core );
        assertTrue( "Must have core as base payload.", afWithRefFacet.getBasePayload() == core );

        // Given at least one response on each action
        resource.getActions().forEach( a -> a.add( buildOtm( a ) ) );

        for (OtmAction action : resource.getActions())
            for (OtmActionResponse ar : action.getResponses()) {

                // When action facet is NOT set
                ar.setPayloadActionFacet( null );
                assertTrue( "Payload must be empty", ar.getPayloadName().isEmpty() );

                // When - action facet is set and required
                ar.setPayloadActionFacet( afNoRefFacet );
                ar.getPayloadActionFacet().setReferenceType( TLReferenceType.REQUIRED );

                // Then - payload name is action facet name
                assertTrue( afNoRefFacet.getName().equals( ar.getPayloadName() ) ); // Used in GUI

                // When - action facet with reference facet
                ar.setPayloadActionFacet( afWithRefFacet );
                ar.getPayloadActionFacet().setReferenceType( TLReferenceType.REQUIRED );
                // TestActionFacet.print( ar.getPayloadActionFacet() );
                // log.debug( " Payload name = " + ar.getPayloadName() );
                // Then - base payload is used
                assertTrue( afWithRefFacet.getName().equals( ar.getPayloadName() ) ); // Used in GUI

                // When reference type is set to NONE
                ar.getPayloadActionFacet().setReferenceType( TLReferenceType.NONE );
                // Then - base payload is used
                assertTrue( core.getName().equals( ar.getPayloadName() ) ); // Used in GUI
            }
    }


    public static OtmActionResponse buildOtm(OtmAction testAction) {
        OtmActionResponse ar = new OtmActionResponse( buildTL( testAction.getTL() ), testAction );
        return ar;
    }

    public static TLActionResponse buildTL(TLAction tlAction) {
        TLActionResponse tlar = new TLActionResponse();
        tlAction.addResponse( tlar );
        return tlar;
    }
}
