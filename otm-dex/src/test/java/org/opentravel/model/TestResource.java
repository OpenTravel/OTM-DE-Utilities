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

package org.opentravel.model;

import static org.junit.Assert.assertNotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLResource;

/**
 * Verifies the functions of the <code>OtmResource</code> class.
 */
public class TestResource extends TestOtmLibraryMemberBase<OtmResource> {
    private static final String R_NAME = "Testbo";

    private static Log log = LogFactory.getLog( TestResource.class );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null );
        subject = buildOtm( staticModelManager );
        baseObject = buildOtm( staticModelManager );
        baseObject.setName( "BaseBO" );
    }

    @Test
    public void testFacets() {
        // OtmResourceObject bo = buildOtm( staticModelManager );
        //
        // assertNotNull( bo.getSummary() );
        // assertNotNull( bo.getDetail() );
    }



    /** ****************************************************** **/

    public static OtmResource buildOtm(OtmModelManager mgr) {
        OtmResource resource = new OtmResource( buildTL(), mgr );
        assertNotNull( resource );
        // assertTrue( resource.getChildren().size() > 2 );
        return resource;
    }

    public static TLResource buildTL() {
        TLResource tlr = new TLResource();
        TLActionFacet tlaf = new TLActionFacet();
        tlr.addActionFacet( tlaf );
        tlr.setName( R_NAME );
        return tlr;
    }
}
