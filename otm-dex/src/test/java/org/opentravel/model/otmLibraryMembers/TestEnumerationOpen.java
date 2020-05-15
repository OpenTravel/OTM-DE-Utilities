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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmFacets.OtmAbstractDisplayFacet;
import org.opentravel.model.otmFacets.OtmEnumerationOtherFacet;
import org.opentravel.model.otmFacets.OtmEnumerationValueFacet;
import org.opentravel.model.otmProperties.OtmEnumerationImpliedValue;
import org.opentravel.model.otmProperties.OtmPropertyFactory;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;

import java.util.Collection;

/**
 */
public class TestEnumerationOpen extends TestOtmLibraryMemberBase<OtmEnumerationOpen> {
    // private static Log log = LogFactory.getLog( TestEnumerationOpen.class );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null, null );
        // subject = buildOtm( staticModelManager );
        // baseObject = buildOtm( staticModelManager );
        // baseObject.setName( "BaseBO" );
    }

    @Before
    public void beforeEach() {
        subject = buildOtm( staticModelManager );
        baseObject = buildOtm( staticModelManager );
        baseObject.setName( "BaseEO" );
    }


    @Test
    public void testAddingValues() {
        OtmPropertyFactory.create( getTLEnumValue( "oe1" ), (OtmEnumerationOpen) subject );
        OtmPropertyFactory.create( getTLEnumValue( "oe2" ), (OtmEnumerationOpen) subject );
        OtmPropertyFactory.create( getTLEnumValue( "oe3" ), (OtmEnumerationOpen) subject );

        assertTrue( !subject.getChildren().isEmpty() );
    }

    @Test
    public void testGetChildrenHierarchy() {
        // Given
        OtmLibrary lib = TestLibrary.buildOtm();
        subject = buildOtm( lib.getModelManager() );
        lib.add( subject );
        assertTrue( "Given", subject.isEditable() );

        // When - children hierarchy accessed
        Collection<OtmObject> ch = subject.getChildrenHierarchy();
        assertTrue( "Must have only 2 children.", ch.size() == 2 );

        // Check the facets
        for (OtmObject c : ch) {

            assertTrue( c instanceof OtmAbstractDisplayFacet );
            OtmAbstractDisplayFacet adf = (OtmAbstractDisplayFacet) c;
            // List<OtmObject> kids = adf.getChildren();
            // assertTrue( !kids.isEmpty() );
            assertTrue( adf.getParent() == subject );

            if (c instanceof OtmEnumerationOtherFacet) {
                OtmEnumerationOtherFacet eof = (OtmEnumerationOtherFacet) c;
                // kids = eof.getChildren(); // gets parent's children
                assertTrue( eof.getChildren().isEmpty() );
                assertTrue( eof.getChildrenHierarchy().size() == 1 );
                for (OtmObject k : eof.getChildrenHierarchy()) {
                    assertTrue( "Child must NOT be editable.", !k.isEditable() );
                    assertTrue( k instanceof OtmEnumerationImpliedValue );
                }
            } else if (c instanceof OtmEnumerationValueFacet) {
                OtmEnumerationValueFacet evf = (OtmEnumerationValueFacet) c;
                assertTrue( evf.getChildrenHierarchy().size() == LITERALCOUNT );
                for (OtmObject k : evf.getChildren())
                    assertTrue( "Child must be editable.", k.isEditable() );
            }
        }
    }

    /** ****************************************************** **/

    public static int LITERALCOUNT = 3; // How many literals created by builders

    public static OtmEnumerationOpen buildOtm(OtmModelManager mgr) {
        OtmEnumerationOpen oto = new OtmEnumerationOpen( buildTL(), mgr );
        assertNotNull( oto );
        assertTrue( "Given", oto.getChildren().size() == LITERALCOUNT );
        return oto;
    }

    public static TLOpenEnumeration buildTL() {
        TLOpenEnumeration tlo = new TLOpenEnumeration();
        tlo.setName( "TestOpenEnum" );
        tlo.addValue( getTLEnumValue( "o1" ) );
        tlo.addValue( getTLEnumValue( "o2" ) );
        tlo.addValue( getTLEnumValue( "o3" ) );
        return tlo;
    }

    public static TLEnumValue getTLEnumValue(String literal) {
        TLEnumValue tle = new TLEnumValue();
        tle.setLiteral( literal );
        return tle;
    }

}
