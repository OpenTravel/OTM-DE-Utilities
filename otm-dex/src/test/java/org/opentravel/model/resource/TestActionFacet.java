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
import org.opentravel.schemacompiler.model.TLResource;

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
    public void testName() {
        OtmBusinessObject bo = TestBusiness.buildOtm( staticModelManager );
        bo.add( TestCustomFacet.buildOtm( staticModelManager ) );
        bo.add( TestQueryFacet.buildOtm( staticModelManager ) );
        // Given a resource
        OtmResource resource = TestResource.buildOtm( staticModelManager );
        resource.setSubject( bo );
        // Given an action facet on that resource
        OtmActionFacet af = buildOtm( resource );

        String[] names = {"Foo", "Bar", "foo", "bar", "Foo_Bar"};
        for (String name : names) {
            af.setName( name );
            af.getName();
            assertTrue( af.getName().equals( name ) );
            log.debug( "Tested name: " + name );
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
        OtmActionFacet af = buildOtm( resource );

        af.getReferenceTypeCandidates().forEach( c -> af.setReferenceFacetName( c ) );

        for (TLReferenceType t : TLReferenceType.values()) {
            af.setReferenceTypeString( t.toString() );
        }
    }

    @Test
    public void testGetReferenceFacet() {
        // Given a business object
        OtmBusinessObject bo = TestBusiness.buildOtm( staticModelManager );
        bo.add( TestCustomFacet.buildOtm( staticModelManager ) );
        bo.add( TestQueryFacet.buildOtm( staticModelManager ) );
        // Given a resource
        OtmResource resource = TestResource.buildOtm( staticModelManager );
        resource.setSubject( bo );

        // Given an action facet on that resource
        OtmActionFacet af = buildOtm( resource );
        assertTrue( resource.getActionFacets().contains( af ) );
        assertTrue( "TLResource must contain tlActionFacet.",
            resource.getTL().getActionFacets().contains( af.getTL() ) );
        af.setReferenceType( TLReferenceType.REQUIRED );

        for (OtmObject f : bo.getChildren())
            if (f instanceof OtmFacet) {
                // When - set to facet
                af.setReferenceFacet( (OtmFacet<?>) f );
                // String rf = af.getTL().getReferenceFacetName();
                // OtmFacet<?> facet = af.getReferenceFacet();
                // log.debug( "RF = " + rf + " Referenced Facet is: " + facet );
                // Then - it must return that facet
                assertTrue( "Must find reference facet.", af.getReferenceFacet() == f );
            }
    }

    public static void print(OtmActionFacet af) {
        log.debug(
            af.getName() + " base= " + af.getTL().getBasePayloadName() + " repeats= " + af.getTL().getReferenceRepeat()
                + " rf= " + af.getTL().getReferenceFacetName() + " type= " + af.getTL().getReferenceType() );
    }

    public static OtmActionFacet buildOtm(OtmResource testResource) {
        OtmActionFacet af = new OtmActionFacet( buildTL( testResource.getTL() ), testResource );

        if (testResource.getSubject() != null) {
            af.setReferenceFacet( testResource.getSubject().getIdFacet() );
            assertTrue( "Given: must have reference facet.", af.getReferenceFacet() != null );
        }
        return af;
    }

    public static TLActionFacet buildTL(TLResource tlr) {
        TLActionFacet tlaf = new TLActionFacet();
        tlr.addActionFacet( tlaf );
        tlaf.setName( "af1" );
        return tlaf;
    }

    public static TLActionFacet buildTL() {
        TLActionFacet tlaf = new TLActionFacet();
        tlaf.setName( "af1" );
        return tlaf;
    }
}
