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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmQueryFacet;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLProperty;

/**
 * Verifies the functions of the <code>OtmCustomFacet</code> class. Very minimal testing without contributed facets or
 * owning object.
 */
public class TestQueryFacet extends TestContextualFacet {
    // private static Log log = LogFactory.getLog( TestContextualFacet.class );
    private static final String CF_NAME = "TestCQF";


    @BeforeClass
    public static void beforeClass() {
        staticLib = TestLibrary.buildOtm();
        staticModelManager = staticLib.getModelManager();
        member = TestBusiness.buildOtm( staticLib, "MemberBO" );
        subject = buildOtm( (OtmBusinessObject) member, "Subject" );
        baseObject = buildOtm( (OtmBusinessObject) member, "Base" );
        // baseObject.setName( "BaseCF" );
    }

    @Before
    public void beforeTest() {
        // member = TestBusiness.buildOtm( staticLib, "MemberBO" );
        cf = buildOtm( (OtmBusinessObject) member, "QF1" );
        contrib = (OtmContributedFacet) member.add( cf );
        testContributedFacet( contrib, cf, member );
    }

    @Test
    public void testFacets() {}

    @Test
    public void testWhenContributed() {
        // Given - a business object and contextual facet
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmBusinessObject bo = TestBusiness.buildOtm( lib, "TestBO" );
        TestCustomFacet.buildOtm( bo, "CF1" );
        TestQueryFacet.buildOtm( bo, "QF1" );

        // When added
        OtmContributedFacet contrib = bo.add( cf );
        // Then (lazy evaluation)
        assertTrue( "Contributor must be owned by Business Object.", contrib.getOwningMember() == bo );
        assertTrue( "Business object has contributor child.", bo.getChildren().contains( contrib ) );
        assertTrue( "Contributor linked to contextual facet.", contrib.getContributor() == cf );
        assertTrue( "Contextual facet knows where it is contributed.", cf.getWhereContributed() == contrib );
    }

    /**
     * @see org.opentravel.model.otmLibraryMembers.TestOtmLibraryMemberBase#testChildrenOwner(org.opentravel.model.OtmChildrenOwner)
     */
    @Override
    public void testChildrenOwner(OtmChildrenOwner otm) {
        if (member == null)
            beforeTest(); // may be invoked from test library member
        super.testAdd();
    }

    @Test
    public void testDeletingChildren() {
        super.testDeletingChildren();
    }

    @Test
    public void testDeleting() {
        super.testDeleteFromMember();
        assertFalse( ((TLBusinessObject) member.getTL()).getCustomFacets().contains( cf.getTL() ) );
    }


    /** ************************ Query Facet Builders ***************************** **/

    /**
     * Create query facet, add it to the model and inject onto the passed business object.
     * 
     * @param bo
     * @param name
     */
    public static OtmQueryFacet buildOtm(OtmBusinessObject bo, String name) {
        assertTrue( "Builder: parameter must have model manager.", bo.getModelManager() != null );
        assertTrue( "Builder: parameter must have library.", bo.getLibrary() != null );
        OtmQueryFacet qf = buildOtm( bo.getModelManager() );
        bo.getLibrary().add( qf );
        qf.setName( name );
        bo.add( qf );

        testContributedFacet( qf.getWhereContributed(), qf, bo );
        return qf;
    }

    // /**
    // * Create query facet and contribute it to the passed business object.
    // *
    // * @param modelManager
    // * @param bo
    // * @return
    // */
    // @Deprecated
    // public static OtmQueryFacet buildOtm(OtmModelManager modelManager, OtmBusinessObject bo) {
    // OtmQueryFacet cf = buildOtm( modelManager );
    // bo.add( cf );
    // testContributedFacet( cf.getWhereContributed(), cf, bo );
    // return cf;
    // }


    /**
     * Build a custom facet. It will not have where contributed or children!
     * 
     * @param mgr
     * @return
     */
    private static OtmQueryFacet buildOtm(OtmModelManager mgr) {
        OtmQueryFacet query = new OtmQueryFacet( buildTL(), mgr );
        assertNotNull( query );
        mgr.add( query );
        return query;
    }

    public static TLContextualFacet buildTL() {
        TLContextualFacet tlcf = new TLContextualFacet();
        tlcf.setName( CF_NAME );
        tlcf.setFacetType( TLFacetType.QUERY );
        tlcf.addAttribute( new TLAttribute() );
        tlcf.addElement( new TLProperty() );

        TLBusinessObject tlbo = TestBusiness.buildTL();
        // does NOT tell BO that it has custom facet - tlcf.setOwningEntity( tlbo );
        tlbo.addQueryFacet( tlcf );
        return tlcf;
    }

}
