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
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmFacets.OtmIdFacet;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestCustomFacet;
import org.opentravel.model.otmLibraryMembers.TestQueryFacet;
import org.opentravel.model.otmLibraryMembers.TestResource;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParamLocation;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLResource;

import java.util.List;

import javafx.collections.ObservableList;

/**
 * Test class for Action Facet resource descendants.
 * <p>
 */
public class TestParamGroup extends TestOtmResourceBase<OtmParameterGroup> {
    private static Log log = LogFactory.getLog( TestParamGroup.class );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null, null );
        baseObject = TestBusiness.buildOtm( staticModelManager );
        testResource = TestResource.buildOtm( staticModelManager );

        subject = buildOtm( testResource );
        log.debug( "Before class ran." );
    }


    @Test
    public void testChildren() {
        OtmParameterGroup p = buildOtm( testResource );
        assertTrue( p.getChildren().size() >= 2 );

        // Then - make sure there are request and responses in the children
        for (OtmParameter r : p.getParameters())
            assertTrue( p.getChildren().contains( r ) );
    }

    @Test
    public void testIdGroup() {
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmBusinessObject bo = TestBusiness.buildOtm( lib, "TestBO" );
        OtmResource r = TestResource.buildOtm( bo );

        // When - static builder is called
        OtmParameterGroup idPg = buildIdGroup( r );

        assertTrue( idPg.getOwningMember() == r );
        assertTrue( idPg.getTL().isIdGroup() );

        assertTrue( r.getParameterGroups().contains( idPg ) );
    }

    @Test
    public void testParameters() {
        OtmParameterGroup p = buildOtm( testResource );
        assertTrue( p.getParameters().size() >= 2 );

        for (OtmParameter param : p.getParameters()) {
            assertNotNull( "Parameter must be non-null.", param );
            assertTrue( "Parameter must have identity listner.", param == OtmModelElement.get( param.getTL() ) );
            String pathContribution = param.getPathContribution();
            assertNotNull( pathContribution );
            if (param.isPathParam())
                assertTrue( !param.getPathContribution().isEmpty() );

            String queryContribution = param.getQueryContribution( "?" );
            assertNotNull( queryContribution );
            if (param.isQueryParam())
                assertTrue( !param.getQueryContribution( "?" ).isEmpty() );

            log.debug( pathContribution + " " + queryContribution );
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
        // Given a parameter group
        OtmParameterGroup p = buildOtm( resource );

        p.setIdGroup( false );
        assertFalse( p.isIdGroup() );
        p.setIdGroup( true );
        assertTrue( p.isIdGroup() );

        ObservableList<String> candidates = p.getReferenceFacetCandidates();
        assertFalse( candidates.isEmpty() );
        candidates.forEach( c -> assertTrue( c != null ) );
        List<OtmObject> facets = p.getFacetCandidates();
        assertFalse( facets.isEmpty() );
        facets.forEach( c -> assertTrue( c != null ) );

        for (String c : p.getReferenceFacetCandidates()) {
            OtmObject result = p.setReferenceFacetString( c );
            assertTrue( result != null );
            assertTrue( result.getName().equals( c ) );
        }

        // TODO location
    }

    public static OtmParameterGroup buildOtm(OtmResource testResource) {
        OtmParameterGroup af = new OtmParameterGroup( buildTL( testResource.getTL() ), testResource );
        return af;
    }

    public static TLParamGroup buildTL(TLResource tlResource) {
        TLParamGroup tlpg = new TLParamGroup();
        tlpg.setName( "tlpg1" );

        TLParameter tlp = new TLParameter();
        tlp.setLocation( TLParamLocation.PATH );
        tlp.setFieldRef( getMemberField() );
        tlpg.addParameter( tlp );
        tlp = new TLParameter();
        tlp.setLocation( TLParamLocation.QUERY );
        tlp.setFieldRef( getMemberField() );
        tlpg.addParameter( tlp );

        tlResource.addParamGroup( tlpg );
        return tlpg;
    }

    public static OtmParameterGroup buildIdGroup(OtmResource owner) {
        assertTrue( owner != null );
        assertTrue( owner.getSubject() != null );

        TLParamGroup tlpg = new TLParamGroup();
        tlpg.setName( "idGroup" );
        tlpg.setIdGroup( true );
        tlpg.setFacetRef( owner.getSubject().getIdFacet().getTL() );

        TLParameter tlp = new TLParameter();
        tlp.setLocation( TLParamLocation.PATH );
        tlp.setFieldRef( getMemberField( owner.getSubject() ) );
        tlpg.addParameter( tlp );
        owner.getTL().addParamGroup( tlpg );

        OtmParameterGroup pg = new OtmParameterGroup( tlpg, owner );
        assertTrue( pg.isValid() );

        return pg;
    }

    private static TLMemberField<TLFacet> getMemberField() {
        TLMemberField<TLFacet> mf = null;
        if (baseObject != null) {
            OtmIdFacet id = ((OtmBusinessObject) baseObject).getIdFacet();
            if (!id.getChildren().isEmpty()) {
                if (id.getChildren().get( 0 ).getTL() instanceof TLMemberField)
                    mf = (TLMemberField<TLFacet>) id.getChildren().get( 0 ).getTL();
            }
        }
        return mf;
    }

    /**
     * @param bo
     * @return a TLMemberField for the first child of the id facet.
     */
    public static TLMemberField<TLFacet> getMemberField(OtmBusinessObject bo) {
        TLMemberField<TLFacet> mf = null;
        if (bo != null) {
            OtmIdFacet id = ((OtmBusinessObject) bo).getIdFacet();
            if (!id.getChildren().isEmpty()) {
                if (id.getChildren().get( 0 ).getTL() instanceof TLMemberField)
                    mf = (TLMemberField<TLFacet>) id.getChildren().get( 0 ).getTL();
            }
        }
        return mf;
    }
}
