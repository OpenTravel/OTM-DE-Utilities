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

package org.opentravel.model.otmProperties;

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmFacets.OtmSummaryFacet;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;
import org.opentravel.schemacompiler.model.TLPropertyType;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class for Property Type Elements
 * <p>
 */
public class TestElement extends TestOtmPropertiesBase<OtmElement<?>> {
    private static Log log = LogFactory.getLog( TestElement.class );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null, null );
        baseObject = TestBusiness.buildOtm( staticModelManager );
        // testResource = TestResource.buildOtm( staticModelManager );

        log.debug( "Before class ran." );
    }

    @Test
    public void testChildren() {}

    @Test
    public void testMove() {
        // Given - a facet with many objects inserted in alphabetical order
        OtmSummaryFacet facet = baseObject.getSummary();
        // Given - no previous children
        List<OtmObject> kids = new ArrayList<>( facet.getChildren() );
        kids.forEach( k -> facet.delete( k ) );
        // Given - many elements inserted in alphabetical order
        String BASE = "Element";
        OtmElement<?> e = null;
        for (int i = 1; i < 10; i++) {
            e = TestElement.buildOtm( facet );
            e.setName( BASE + i );
        }
        kids = new ArrayList<>( facet.getChildren() );
        List<TLProperty> tlKids = facet.getTL().getElements();
        assertTrue( "Given:", !kids.isEmpty() );
        assertTrue( "Given: ", kids.size() == tlKids.size() );

        // When moved
        boolean result;
        for (OtmObject child : kids)
            if (child instanceof OtmElement) {
                result = ((OtmElement<?>) child).moveDown();
                // log.debug( "element " + child + " i = " + facet.getChildren().indexOf( child ) + " ==? "
                // + facet.getTL().getElements().indexOf( child.getTL() ) );
                assertTrue( "Must have same index.",
                    facet.getChildren().indexOf( child ) == facet.getTL().getElements().indexOf( child.getTL() ) );
                // When moved back
                result = ((OtmElement<?>) child).moveUp();
                // log.debug( "element : " + child + " i = " + facet.getTL().getElements().indexOf( child.getTL() ) );
                assertTrue( "Must have same index.",
                    facet.getChildren().indexOf( child ) == facet.getTL().getElements().indexOf( child.getTL() ) );

            }
        // Manually inspect to assure still in sort order
        kids = facet.getChildren();
        // log.debug( "Done" );
    }


    /**
     * **********************************************************************************
     * 
     */

    /**
     * @param facet
     * @param cm
     * @return
     */
    public static OtmElement<?> buildOtm(OtmPropertyOwner parent, OtmTypeProvider assignedType) {
        assert (parent.getTL() instanceof TLPropertyOwner);
        OtmElement<?> e = new OtmElement<TLProperty>( buildTL( (TLPropertyOwner) parent.getTL() ), parent );
        if (assignedType != null && assignedType.getTL() instanceof TLPropertyType) {
            e.setName( "E_" + assignedType.getName() );
            e.setAssignedType( assignedType );
            assertTrue( "Builder: must have assigned type.", e.getAssignedType() == assignedType );
            assertTrue( "Builder: must have this owner in assigned types.",
                assignedType.getOwningMember().getWhereUsed().contains( parent.getOwningMember() ) );
        }
        return e;
    }

    /**
     * Build an element.
     * 
     * @param parent is a TLPropertyOwner
     * @return
     */
    public static OtmElement<?> buildOtm(OtmPropertyOwner parent) {
        return buildOtm( parent, null );
        // assert (parent.getTL() instanceof TLPropertyOwner);
        // return new OtmElement<TLProperty>( buildTL( (TLPropertyOwner) parent.getTL() ), parent );
    }

    public static TLProperty buildTL(TLPropertyOwner owner) {
        TLProperty tlp = new TLProperty();
        owner.addElement( tlp );
        return tlp;
    }


}
