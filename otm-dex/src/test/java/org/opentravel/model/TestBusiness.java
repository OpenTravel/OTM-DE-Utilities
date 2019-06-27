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
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLProperty;

/**
 * Verifies the functions of the <code>UserSettings</code> class.
 */
public class TestBusiness extends TestOtmLibraryMemberBase<OtmBusinessObject> {
    private static final String BO_NAME = "Testbo";

    private static Log log = LogFactory.getLog( TestBusiness.class );

    @Test
    public void testConstructors() {
        super.testConstructors( buildOtm( staticModelManager ) );
        log.debug( "Done." );
    }


    @Test
    public void testChildrenOwner() {
        super.testChildrenOwner( buildOtm( staticModelManager ) );
    }


    @Test
    public void testFacets() {
        OtmBusinessObject bo = buildOtm( staticModelManager );

        assertNotNull( bo.getSummary() );
        assertNotNull( bo.getDetail() );
    }


    @Test
    public void testInheritance() {
        OtmBusinessObject bo_base = buildOtm( staticModelManager );
        OtmBusinessObject bo_ex = buildOtm( staticModelManager );

        extendObject( bo_base, bo_ex );
        super.testInheritance( bo_ex );
    }

    /** ****************************************************** **/

    public static OtmBusinessObject buildOtm(OtmModelManager mgr) {
        OtmBusinessObject bo = new OtmBusinessObject( buildTL(), mgr );
        assertNotNull( bo );
        bo.getTL().getSummaryFacet().addAttribute( new TLAttribute() );
        bo.getTL().getSummaryFacet().addElement( new TLProperty() );

        assertTrue( bo.getChildren().size() > 2 );
        assertTrue( bo.getSummary().getChildren().size() == 2 );
        return bo;
    }

    public static TLBusinessObject buildTL() {
        TLBusinessObject tlbo = new TLBusinessObject();
        tlbo.setName( BO_NAME );
        return tlbo;
    }
}
