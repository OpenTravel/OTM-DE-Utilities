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
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmCustomFacet;
import org.opentravel.model.otmProperties.OtmElement;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Verifies the functions of the <code>OtmBusinessObject</code> class.
 */
public class TestBusiness extends TestOtmLibraryMemberBase<OtmBusinessObject> {
    private static String BoName = "Testbo";

    private static Log log = LogFactory.getLog( TestBusiness.class );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null, null );
        subject = buildOtm( staticModelManager );
        baseObject = buildOtm( staticModelManager );
        baseObject.setName( "BaseBO" );
    }

    @Test
    public void testFacets() {
        OtmBusinessObject bo = buildOtm( staticModelManager );

        assertNotNull( bo.getSummary() );
        assertNotNull( bo.getDetail() );
    }

    @Override
    public void testCopy(OtmLibraryMember member) {
        testTLCopy( member );
        testCopySteps( member ); // Test each step in the copy process

        assertTrue( "Given: ", !member.getChildrenContributedFacets().isEmpty() );
        List<OtmObject> kids_member = member.getChildren();

        // When - copied
        OtmBusinessObject bo = (OtmBusinessObject) member;
        OtmBusinessObject copy = bo.copy();

        // Then
        List<OtmObject> copy_member = copy.getChildren();
        assertTrue( "Copy must not have contributed children.", copy.getChildrenContributedFacets().isEmpty() );
    }

    public void testCopySteps(OtmLibraryMember member) {
        assertTrue( "Given: ", !member.getChildrenContributedFacets().isEmpty() );
        OtmBusinessObject bo = (OtmBusinessObject) member;
        Collection<OtmContributedFacet> customs_member = bo.getChildrenContributedFacets();
        List<TLContextualFacet> tlCustoms_member = bo.getTL().getCustomFacets();

        // When - clone
        LibraryElement tlMember = null;
        try {
            tlMember = member.getTL().cloneElement();
        } catch (Exception e) {
            log.debug( "Error cloning." + getClass().getSimpleName() + " " + member.getName() );
        }
        assertTrue( tlMember instanceof TLBusinessObject );
        List<TLContextualFacet> tlCustoms_copy = ((TLBusinessObject) tlMember).getCustomFacets();

        // // When - factory used to create OTM facade
        OtmLibraryMember c = OtmLibraryMemberFactory.create( (LibraryMember) tlMember, member.getModelManager() );
        assertTrue( "Factory must return a business object.", c instanceof OtmBusinessObject );
        OtmBusinessObject copy = (OtmBusinessObject) c;

        // Then - check the contributed facets
        Collection<OtmContributedFacet> contributedList_copy = copy.getChildrenContributedFacets();
        assertTrue( "Must have a contributed facets.", !contributedList_copy.isEmpty() );

        for (OtmContributedFacet contrib : contributedList_copy) {
            assertTrue( "Contributed TL must be from copy.", tlCustoms_copy.contains( contrib.getTL() ) );
            assertTrue( "Contributed must not be same as in original member.", !customs_member.contains( contrib ) );
            assertTrue( "Contributed TL must not be from original member.",
                !tlCustoms_member.contains( contrib.getTL() ) );
            assertTrue( "Contributed must not have contributor.", contrib.getContributor() == null );
        }

        // When - delete contributed facets
        for (OtmContributedFacet contrib : contributedList_copy) {
            copy.delete( contrib );
        }
        // Then
        assertTrue( copy.getChildrenContributedFacets().isEmpty() );
        assertTrue( copy.getTL().getCustomFacets().isEmpty() );
    }

    public void testTLCopy(OtmLibraryMember member) {

        log.debug( "Testing copy of TL business object." );
        TLBusinessObject tlMember = ((OtmBusinessObject) member).getTL();
        List<TLContextualFacet> customs_member = tlMember.getCustomFacets();
        if (customs_member.isEmpty())
            return; // Nothing to check

        // When - TL is cloned
        TLBusinessObject tlCopy = (TLBusinessObject) member.getTL().cloneElement();

        // Then
        assertTrue( tlCopy != null );
        List<TLContextualFacet> customs = new ArrayList<>( tlCopy.getCustomFacets() );
        assertTrue( "Must have same number of customs.", customs.size() == customs_member.size() );
        for (TLContextualFacet custom : customs) {
            assertTrue( "Custom must not be same as in original member.", !customs_member.contains( custom ) );
            assertTrue( "Custom copy has not be modeled.", custom.getListeners().isEmpty() );
        }

        // When - TL customs are deleted
        for (TLContextualFacet c : customs)
            tlCopy.removeCustomFacet( c );

        // Then
        assertTrue( "Copy must not have custom facets.", tlCopy.getCustomFacets().isEmpty() );
        assertTrue( "Original must still have custom facets.", !tlMember.getCustomFacets().isEmpty() );
        assertTrue( "Original must have same number of custom facets.",
            tlMember.getCustomFacets().size() == customs_member.size() );
    }

    /** ****************************************************** **/
    /**
     * Build business object with attribute and element in ID and Summary facets.
     * 
     * @param mgr
     * @param name
     * @return
     */
    public static OtmBusinessObject buildOtm(OtmLibrary lib, String name) {
        BoName = name; // set global static
        OtmBusinessObject bo = buildOtm( lib.getModelManager() );
        lib.add( bo );
        return bo;
    }

    /**
     * Build business object with attribute and element in ID and Summary facets.
     * 
     * @param mgr
     * @param name
     * @return
     */
    public static OtmBusinessObject buildOtm(OtmModelManager mgr, String name) {
        BoName = name;
        return buildOtm( mgr );
    }

    /**
     * Get an element from the summary facet
     * 
     * @param member
     * @return
     */
    public static OtmElement<?> getElement(OtmBusinessObject member) {
        for (OtmObject child : member.getSummary().getChildren())
            if (child instanceof OtmElement)
                return (OtmElement<?>) child;
        return null;
    }

    public static OtmBusinessObject buildOtm(OtmModelManager mgr) {
        OtmBusinessObject bo = new OtmBusinessObject( buildTL(), mgr );
        assertNotNull( bo );
        mgr.add( bo );

        // TestCustomFacet.buildOtm( staticModelManager );
        assertTrue( bo.getChildren().size() > 2 );
        assertTrue( bo.getSummary().getChildren().size() == 2 );
        assertTrue( "Must have identity listener.", OtmModelElement.get( bo.getTL() ) == bo );

        OtmCustomFacet cf = TestCustomFacet.buildOtm( mgr );
        cf.setName( "SomeCustom" );
        bo.add( cf );

        return bo;
    }

    public static TLBusinessObject buildTL() {
        TLBusinessObject tlbo = new TLBusinessObject();
        tlbo.setName( BoName );
        TLAttribute tla = new TLAttribute();
        tla.setName( "idAttr_" + BoName );
        buildExample( tla );
        tlbo.getIdFacet().addAttribute( tla );

        TLProperty tlp = new TLProperty();
        tlp.setName( "idProp_" + BoName );
        buildExample( tlp );
        tlbo.getIdFacet().addElement( tlp );

        tla = new TLAttribute();
        tla.setName( "sumAttr_" + BoName );
        tlbo.getSummaryFacet().addAttribute( tla );
        tlp = new TLProperty();
        tlp.setName( "sumProp_" + BoName );
        tlbo.getSummaryFacet().addElement( tlp );
        return tlbo;
    }

    public static void buildExample(TLExampleOwner tleo) {
        TLExample tle = new TLExample();
        tle.setValue( "ExampleValue123" );
        tleo.addExample( tle );
        assertTrue( tleo.getExamples().size() >= 1 );
    }
}
