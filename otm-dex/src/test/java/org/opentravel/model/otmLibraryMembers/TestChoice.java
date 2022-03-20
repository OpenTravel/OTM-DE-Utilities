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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
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

    private static Logger log = LogManager.getLogger( TestChoice.class );

    @BeforeClass
    public static void beforeClass() {
        // staticLib = TestLibrary.buildOtm();
        // staticModelManager = staticLib.getModelManager();
        // subject = buildOtm( staticLib, "SubjectCH" );
        // baseObject = buildOtm( staticLib, "BaseCH" );
        staticModelManager = new OtmModelManager( null, null, null );
        subject = buildOtm( staticModelManager );
        subject.setName( "SubjectCH" );
        baseObject = buildOtm( staticModelManager );
        baseObject.setName( "BaseCH" );
    }

    @Before
    public void beforeMethods() {
        staticModelManager.clear();

        OtmLibrary lib = TestLibrary.buildOtm( staticModelManager );
        subject = buildOtm( lib, "SubjectCH" );
        // subject.setName( "SubjectCH" );
        baseObject = buildOtm( lib, "BaseCH" );
        // baseObject.setName( "BaseCH" );
    }

    @Override
    public void testConstructors(OtmChoiceObject subject) {
        super.testConstructors( subject );

        assertTrue( "Choice constructor must create shared facet.", subject.getShared() != null );
        assertTrue( "Choice constructor must create children.", !subject.getChildren().isEmpty() );
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

        // Assure member is in a library and has a choice facet
        if (co.getLibrary() == null) {
            OtmLibrary lib = TestLibrary.buildOtm( co.getModelManager() );
            lib.add( co );
        }
        TestChoiceFacet.buildOtm( co, "OriginalCF" );
        assertTrue( "Given: must have contributed facet.", !co.getChildrenContributedFacets().isEmpty() );
        // List<OtmObject> kids_member = co.getChildren();

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
        assertTrue( "Builder Parameter Error: Library must have model manager.", library.getModelManager() != null );

        // CoName = name; // set global static
        OtmChoiceObject ch = buildOtm( library.getModelManager() );
        ch.setName( name );
        library.add( ch );

        for (OtmContributedFacet cf : ch.getChildrenContributedFacets()) {
            assertTrue( "Contributed facet must have contributor.", cf.getContributor() != null );
            library.add( cf.getContributor() );
            assertTrue( "Contributor must be in library.", library.contains( cf.getContributor() ) );
        }

        // OtmChoiceObject ch = new OtmChoiceObject( buildTL_WithProperties(), library.getModelManager() );
        // if (name != null)
        // ch.setName( name );
        // library.add( ch );
        //
        // TestChoiceFacet.buildOtm( ch, "CHF1" );

        assertTrue( "Builder error: wrong library", ch.getLibrary() == library );
        assertTrue( library.getTL().getNamedMembers().contains( ch.getTL() ) );
        assertTrue( !ch.getChildren().isEmpty() );
        assertTrue( ch.getShared() != null );
        assertTrue( ch.getShared().getChildren() != null );
        assertTrue( ch.getShared().getChildren().size() == 2 );
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

        for (OtmTypeUser tu : ch.getDescendantsTypeUsers())
            tu.setAssignedType( mgr.getStringType() );

        assertTrue( !ch.getChildren().isEmpty() );
        assertTrue( ch.getShared().getChildren().size() == 2 );

        // TestChoiceFacet.buildOtm( ch, CH_NAME );
        return ch;
    }

    /** ******************************** Static TL Choice Builders ********************/

    public static TLChoiceObject buildTL() {
        TLChoiceObject tlch = new TLChoiceObject();
        tlch.setName( CH_NAME );
        return tlch;
    }

    public static TLChoiceObject buildTL_WithProperties() {
        TLChoiceObject tlch = new TLChoiceObject();
        tlch.setName( CH_NAME );
        tlch.getSharedFacet().addAttribute( new TLAttribute() );
        tlch.getSharedFacet().addElement( new TLProperty() );
        return tlch;
    }

}
