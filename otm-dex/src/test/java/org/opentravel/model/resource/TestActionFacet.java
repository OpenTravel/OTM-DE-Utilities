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
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestCustomFacet;
import org.opentravel.model.otmLibraryMembers.TestQueryFacet;
import org.opentravel.model.otmLibraryMembers.TestResource;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLReferenceType;

/**
 * Test class for Action Facet resource descendants.
 * <p>
 */
public class TestActionFacet<L extends TestOtmResourceBase<OtmActionFacet>>
    extends TestOtmResourceBase<OtmActionFacet> {
    private static Log log = LogFactory.getLog( TestActionFacet.class );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null );
        baseObject = TestBusiness.buildOtm( staticModelManager );
        testResource = TestResource.buildOtm( staticModelManager );

        subject = buildOtm( testResource );
        log.debug( "Before class ran." );
    }

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
        OtmActionFacet af = buildOtm( resource );
        af.getTL().setBasePayload( null ); // FIXME

        // When the action facet is set to NONE
        af.setReferenceType( TLReferenceType.NONE );
        // Then
        assertTrue( "Payload must be null.", af.getRequestPayload() == null );

        // When the action facet is set null reference facet
        af.setReferenceFacet( null );
        // Then
        assertTrue( "Payload must be the object.", af.getRequestPayload() == bo );

        // When the action facet is set to the business object facet
        for (OtmObject facet : bo.getChildren()) {
            af.setReferenceFacet( (OtmFacet<?>) facet );
            af.setReferenceType( TLReferenceType.REQUIRED );

            // Then
            OtmObject rqPayload = af.getRequestPayload();
            assertTrue( "Must have facet as payload.", rqPayload == facet );

            log.debug( "Payload = " + rqPayload.getName() );
        }

        // TODO - test with basePayload set
    }

    public static OtmActionFacet buildOtm(OtmResource testResource) {
        OtmActionFacet af = new OtmActionFacet( buildTL(), testResource );
        return af;
    }

    public static TLActionFacet buildTL() {
        TLActionFacet tlaf = new TLActionFacet();
        tlaf.setName( "af1" );
        return tlaf;
    }
}
