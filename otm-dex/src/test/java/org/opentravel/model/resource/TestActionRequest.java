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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestCore;
import org.opentravel.model.otmLibraryMembers.TestCustomFacet;
import org.opentravel.model.otmLibraryMembers.TestQueryFacet;
import org.opentravel.model.otmLibraryMembers.TestResource;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLHttpMethod;
import org.opentravel.schemacompiler.model.TLReferenceType;

/**
 * Test class for Action Request resource.
 * <p>
 */
public class TestActionRequest<L extends TestOtmResourceBase<OtmActionRequest>>
    extends TestOtmResourceBase<OtmActionRequest> {
    private static Log log = LogFactory.getLog( TestActionRequest.class );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null );
        baseObject = TestBusiness.buildOtm( staticModelManager );
        testResource = TestResource.buildOtm( staticModelManager );

        OtmAction testAction = TestAction.buildOtm( testResource );
        subject = buildOtm( testAction );
        log.debug( "Before class ran." );
    }

    /**
     * ActionDAO: wrapper = ((OtmActionRequest) otmObject).examplePayloadProperty();
     * 
     */
    @Test
    public void testExamplePayloadProperty() {
        // Given a business object
        OtmBusinessObject bo = TestBusiness.buildOtm( staticModelManager );
        bo.add( TestCustomFacet.buildOtm( staticModelManager ) );
        bo.add( TestQueryFacet.buildOtm( staticModelManager ) );
        // Given a resource
        OtmResource resource = TestResource.buildOtm( staticModelManager );
        resource.setSubject( bo );
        // Given - request on the resource
        OtmActionRequest request = resource.getActionRequests().get( 0 );
        assertNotNull( request );
        // Request set to: POST, uses action facet as payload type
        request.setMethod( TLHttpMethod.POST );

        // Given - AF set to: ReferenceFacet = substitution group, No base payload
        OtmActionFacet af = TestActionFacet.buildOtm( resource );
        af.setBasePayload( null );
        af.setReferenceFacet( null ); // substitution group setting
        af.setReferenceType( TLReferenceType.REQUIRED ); // only used for response
        // ? What reference type?
        // Mime type settings?

        // When - action facet is set
        request.setPayloadType( af );
        // Then - example property is subject
        String ex = request.examplePayloadProperty().get();
        assertTrue( request.examplePayloadProperty().get().contains( bo.getName() ) );

        // When - action facet RFName is empty, use subject (substitution group)
        af.setReferenceFacetName( "" );
        // Then - example property is subject
        ex = request.examplePayloadProperty().get();
        assertTrue( request.examplePayloadProperty().get().contains( bo.getName() ) );
        af.setReferenceFacetName( null );
        // Then - example property is subject
        ex = request.examplePayloadProperty().get();
        assertTrue( request.examplePayloadProperty().get().contains( bo.getName() ) );

        // When - made into non-first class sub-resource to a parent resource
        OtmResource resourceParent = TestResource.buildParentResource( resource, "ParentResorce", staticModelManager );
        resource.setFirstClass( false );
        // Then - example property is subject
        ex = request.examplePayloadProperty().get();
        assertTrue( request.examplePayloadProperty().get().contains( bo.getName() ) );

        // When - extends a baseResource
        OtmResource baseResource = TestResource.buildOtm( staticModelManager );
        resource.setExtendedResource( baseResource );
        // Then - example property is subject
        ex = request.examplePayloadProperty().get();
        assertTrue( request.examplePayloadProperty().get().contains( bo.getName() ) );

        // When - base resource has an action facet with base payload
        OtmCore basePayload = TestCore.buildOtm( staticModelManager );
        basePayload.setName( "BasePayloadCore" );
        OtmActionFacet baseAF = TestActionFacet.buildOtm( baseResource );
        baseAF.setBasePayload( basePayload );
        // Then - example property is subject
        ex = request.examplePayloadProperty().get();
        assertTrue( request.examplePayloadProperty().get().contains( bo.getName() ) );


    }

    /**
     * User is shown the a formated getName() of the request payload
     * 
     */
    @Test
    public void testRequestPayload() {
        // Given a business object
        OtmBusinessObject bo = TestBusiness.buildOtm( staticModelManager );
        bo.add( TestCustomFacet.buildOtm( staticModelManager ) );
        bo.add( TestQueryFacet.buildOtm( staticModelManager ) );
        // Given a resource
        OtmResource resource = TestResource.buildOtm( staticModelManager );
        resource.setSubject( bo );

        // Given an action facet on that resource
        OtmActionFacet af = TestActionFacet.buildOtm( resource );
        af.setBasePayload( null );

        // // When - the reference type is set to NONE
        // // Reference type is IGNORED for requests
        // af.setReferenceType( TLReferenceType.NONE );
        // // Then
        // assertTrue( "Payload must be null.", af.getRequestPayload() == null );

        // When - the reference facet is set null
        af.setReferenceType( TLReferenceType.REQUIRED );
        af.setReferenceFacet( null );
        // Then
        assertTrue( "Payload must be the object.", af.getRequestPayload() == bo );

        // When - the reference facet is set to any business object facet
        for (OtmObject facet : bo.getChildren()) {
            af.setReferenceFacet( (OtmFacet<?>) facet );

            // Then
            OtmObject rqPayload = af.getRequestPayload();
            assertTrue( "Must have facet as payload.", rqPayload == facet );

            log.debug( "Payload = " + rqPayload.getName() );
        }

    }

    @Test
    public void testSetters() {
        // Given a business object
        OtmBusinessObject bo = TestBusiness.buildOtm( staticModelManager );
        bo.add( TestCustomFacet.buildOtm( staticModelManager ) );
        bo.add( TestQueryFacet.buildOtm( staticModelManager ) );
        // Given a resource
        OtmResource resource = TestResource.buildOtm( staticModelManager );
        resource.setSubject( bo );
        // Given an action facet on that resource
        OtmActionFacet af = TestActionFacet.buildOtm( resource );
        // Given a core object set as base payload
        OtmCore core = TestCore.buildOtm( staticModelManager );
        af.setBasePayload( core );
        // Given 3 more parameter groups
        TestParamGroup.buildIdGroup( resource );
        OtmParameterGroup pg = null;
        for (int i = 1; i <= 3; i++) {
            pg = TestParamGroup.buildOtm( resource );
            pg.setName( "PG" + i );
        }
        assertTrue( resource.getParameterGroups().size() > 3 );
        // Given - an action request
        OtmActionRequest ar = resource.getActionRequests().get( 0 );
        assertTrue( ar != null );

        String value = null;
        // Assure can set null without error
        ar.setMethodString( value );
        ar.setParamGroupString( value );
        ar.setPathTemplate( value, true );
        ar.setPayloadActionFacetString( value );

        // Assure can be set to all candidate values
        ar.getMethodCandidates().forEach( c -> assertTrue( ar.setMethodString( c ).toString().equals( c ) ) );
        ar.getParameterGroupCandidates()
            .forEach( c -> assertTrue( ar.setParamGroupString( c ).getName().equals( c ) ) );
        ar.getPayloadCandidates()
            .forEach( c -> assertTrue( ar.setPayloadActionFacetString( c ).getName().equals( c ) ) );

        final String path1 = "/Path1";
        final String path2 = "/Path2";

        // Set path without parameters
        assertTrue( ar.setPathTemplate( null, false ) == null );
        assertTrue( ar.setPathTemplate( path1, false ).equals( path1 ) );
        assertTrue( ar.setPathTemplate( path2, false ).equals( path2 ) );

        // Set path with parameters
        assertTrue( ar.setPathTemplate( null, true ) == null );
        assertTrue( ar.setPathTemplate( path1, true ).startsWith( path1 ) );
        assertTrue( ar.setPathTemplate( path2, true ).startsWith( path2 ) );
    }

    /**
     * test with basePayload set
     */
    @Test
    public void testRequestPayloadWithBasePayload() {
        // Given a business object
        OtmBusinessObject bo = TestBusiness.buildOtm( staticModelManager );
        bo.add( TestCustomFacet.buildOtm( staticModelManager ) );
        bo.add( TestQueryFacet.buildOtm( staticModelManager ) );
        // Given a resource
        OtmResource resource = TestResource.buildOtm( staticModelManager );
        resource.setSubject( bo );

        // Given an action facet on that resource
        OtmActionFacet af = TestActionFacet.buildOtm( resource );
        // Given a core object to use a base payload
        OtmCore core = TestCore.buildOtm( staticModelManager );

        // When - core set as base payload
        af.setBasePayload( core );
        // Then
        assertTrue( "Must have core as base payload.", af.getBasePayload() == core );

        // When - Reference Facet Name is null it represents "SUBGROUP"
        af.setReferenceFacet( null );
        assertTrue( af.getRequestPayload() == bo );

        // When - reference type is NONE and reference facet is null (subgroup)
        af.setReferenceType( TLReferenceType.NONE );
        assert (af.getReferenceType() == TLReferenceType.NONE); // don't care! response only
        assertTrue( af.getRequestPayload() == bo );

        // When - NONE and a facet
        af.setReferenceFacet( bo.getIdFacet() );
        OtmObject foo = af.getRequestPayload();
        assertTrue( "With a facet and NONE must be null.", af.getRequestPayload() == bo.getIdFacet() );

        // When - regardless of basePayload, when required each of the facets must respond with is own facet
        af.setReferenceType( TLReferenceType.REQUIRED );
        // Then - facets become the request payload
        for (OtmObject f : bo.getChildren())
            if (f instanceof OtmFacet) {
                // When - set to facet
                af.setReferenceFacet( (OtmFacet<?>) f );
                // Then - it must return that facet
                assertTrue( af.getRequestPayload() == f );
            }
    }

    public static OtmActionRequest buildOtm(OtmAction testAction) {
        OtmActionRequest ar = new OtmActionRequest( buildTL(), testAction );
        return ar;
    }

    public static TLActionRequest buildTL() {
        TLActionRequest tlar = new TLActionRequest();
        return tlar;
    }
}
