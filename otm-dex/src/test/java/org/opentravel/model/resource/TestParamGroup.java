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
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestResource;
import org.opentravel.schemacompiler.model.TLParamGroup;
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

    public static OtmParameterGroup buildOtm(OtmResource testResource) {
        OtmParameterGroup af = new OtmParameterGroup( buildTL(), testResource );
        return af;
    }

    public static TLParamGroup buildTL() {
        TLParamGroup tlpg = new TLParamGroup();
        tlpg.setName( "tlpg1" );

        TLParameter tlp = new TLParameter();
        tlpg.addParameter( tlp );
        tlp = new TLParameter();
        tlpg.addParameter( tlp );

        return tlpg;
    }
}
