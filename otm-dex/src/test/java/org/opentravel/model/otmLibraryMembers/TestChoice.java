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
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public class TestChoice extends TestOtmLibraryMemberBase<OtmChoiceObject> {
    private static final String CH_NAME = "TestChoice";

    private static Log log = LogFactory.getLog( TestChoice.class );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null, null );
        subject = buildOtm( staticModelManager );
        baseObject = buildOtm( staticModelManager );
        baseObject.setName( "BaseCH" );
    }

    @Override
    public void testRefresh() {
        // Given: choice subject and facet names before refresh to test
        // Refresh will remove children including Contributed facets. Those will get recreated via lazy evaluation.
        Collection<String> facetNames = new ArrayList<>();
        for (OtmContributedFacet cf : subject.getChildrenContributedFacets()) {
            TestContextualFacet.testContributedFacet( cf, cf.getContributor(), subject );
            facetNames.add( cf.getName() );
        }

        // When
        super.testRefresh( subject );

        // Then
        for (OtmContributedFacet contrib : subject.getChildrenContributedFacets()) {
            log.debug( "Testing choice facet: " + contrib );
            OtmLibraryMember lm = contrib.getOwningMember();
            assertTrue( "Must still have subject as owner.", lm == subject );

            OtmContextualFacet contributor = contrib.getContributor();
            assertTrue( "Must have contributor.", contributor != null );

            // List<OtmObject> children = lm.getChildren();
            // There is a contributed facet, but it is a new one
            assertTrue( "Library member has contributor child with same name.",
                facetNames.contains( contrib.getName() ) );

            OtmContextualFacet cf = contrib.getContributor();
            assertTrue( "Contextual facet knows where it is contributed.", cf.getWhereContributed() == contrib );

            TestContextualFacet.testContributedFacet( contrib, contributor, lm );
        }
    }

    @Test
    public void testFacets() {
        OtmChoiceObject ch = buildOtm( staticModelManager );

        assertNotNull( ch.getShared() );
    }

    @Override
    public void testCopy(OtmLibraryMember member) {
        // testTLCopy( member );
        // testCopySteps( member ); // Test each step in the copy process

        OtmChoiceObject co = (OtmChoiceObject) member;
        assertTrue( "Given: must have contributed facet.", !co.getChildrenContributedFacets().isEmpty() );
        List<OtmObject> kids_member = co.getChildren();

        // When - copied
        OtmChoiceObject copy = co.copy();

        // Then
        List<OtmObject> copy_member = copy.getChildren();
        assertTrue( "Copy must not have contributed children.", copy.getChildrenContributedFacets().isEmpty() );
        assertTrue( "Copy must not have choice facets.", copy.getTL().getChoiceFacets().isEmpty() );
    }


    /** ****************************************************** **/

    /**
     * Create a choice object. Add element and attribute to shared facet.
     * 
     * @param library library to add the new choice object to. Must have model manager set.
     * @param name if null, uses "TestChoice"
     * @return
     */
    public static OtmChoiceObject buildOtm(OtmLibrary library, String name) {
        assertTrue( "Library must have model manager.", library.getModelManager() != null );
        OtmChoiceObject ch = new OtmChoiceObject( buildTL(), library.getModelManager() );
        if (name != null)
            ch.setName( name );
        library.add( ch );
        assertTrue( ch.getLibrary() == library );
        assertTrue( library.getTL().getNamedMembers().contains( ch.getTL() ) );
        return ch;
    }

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

        TestChoiceFacet.buildOtm( mgr, ch );
        return ch;
    }

    public static TLChoiceObject buildTL() {
        TLChoiceObject tlch = new TLChoiceObject();
        tlch.setName( CH_NAME );
        return tlch;
    }
}
