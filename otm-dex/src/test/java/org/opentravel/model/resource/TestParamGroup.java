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
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmFacets.OtmIdFacet;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestResource;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParamLocation;
import org.opentravel.schemacompiler.model.TLParameter;

/**
 * Test class for Action Facet resource descendants.
 * <p>
 */
public class TestParamGroup extends TestOtmResourceBase<OtmParameterGroup> {
    private static Log log = LogFactory.getLog( TestParamGroup.class );

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
        OtmParameterGroup p = buildOtm( testResource );
        assertTrue( p.getChildren().size() >= 2 );

        // Then - make sure there are request and responses in the children
        for (OtmParameter r : p.getParameters())
            assertTrue( p.getChildren().contains( r ) );
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

    public static OtmParameterGroup buildOtm(OtmResource testResource) {
        OtmParameterGroup af = new OtmParameterGroup( buildTL(), testResource );
        return af;
    }

    public static TLParamGroup buildTL() {
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

        return tlpg;
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
}
