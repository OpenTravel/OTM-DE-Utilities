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

package org.opentravel.model.otmLibraryMembers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.OtmModelManager;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLProperty;

/**
 *
 */
public class TestChoice extends TestOtmLibraryMemberBase<OtmChoiceObject> {
    private static final String CH_NAME = "TestChoice";

    private static Log log = LogFactory.getLog( TestChoice.class );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null );
        subject = buildOtm( staticModelManager );
        baseObject = buildOtm( staticModelManager );
        baseObject.setName( "BaseCH" );
    }

    @Test
    public void testFacets() {
        OtmChoiceObject ch = buildOtm( staticModelManager );

        assertNotNull( ch.getShared() );
    }


    /** ****************************************************** **/

    /**
     * Create a choice object. Add element and attribute to shared facet.
     * 
     * @param mgr
     * @return
     */
    public static OtmChoiceObject buildOtm(OtmModelManager mgr) {
        OtmChoiceObject ch = new OtmChoiceObject( buildTL(), mgr );
        assertNotNull( ch );
        ch.getTL().getSharedFacet().addAttribute( new TLAttribute() );
        ch.getTL().getSharedFacet().addElement( new TLProperty() );

        assertTrue( !ch.getChildren().isEmpty() );
        assertTrue( ch.getShared().getChildren().size() == 2 );
        return ch;
    }

    public static TLChoiceObject buildTL() {
        TLChoiceObject tlch = new TLChoiceObject();
        tlch.setName( CH_NAME );
        return tlch;
    }
}
