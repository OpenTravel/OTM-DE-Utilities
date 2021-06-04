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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.common.ValidationUtils;
import org.opentravel.dex.action.manager.DexReadOnlyActionManager;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.validate.ValidationFinding;

import java.net.URL;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;

/**
 * Verifies the functions of the <code>UserSettings</code> class.
 */
public class TestCore extends TestOtmLibraryMemberBase<OtmCore> {
    private static final String CORE_NAME = "TestCore";

    private static Log log = LogFactory.getLog( TestCore.class );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null, null );
        subject = buildOtm( staticModelManager );
        baseObject = buildOtm( staticModelManager );
        baseObject.setName( "BaseCO" );
    }

    @Before
    public void beforeMethods() {
        staticModelManager.clear();

        staticLib = TestLibrary.buildOtm();
        subject = buildOtm( staticLib, "SubjectCF" );
        baseObject = buildOtm( staticLib, "BaseCF" );
    }


    @Test
    public void testFacets() {
        OtmCore core = buildOtm( staticModelManager );

        assertNotNull( core.getSummary() );
        assertNotNull( core.getDetail() );
        assertNotNull( core.getRoles() );
        assertNotNull( core.getSimpleList() );
        assertNotNull( core.getSummaryList() );
        assertNotNull( core.getDetailList() );
    }

    @Override
    public void testRefresh() {
        log.debug( "Testing core refresh." );
        if (subject.getActionManager() instanceof DexReadOnlyActionManager)
            assertTrue( "Must have read only name property.", subject.nameProperty() instanceof ReadOnlyStringWrapper );
    }

    /** ****************************************************** **/

    /**
     * Create a core object with an ID attribute and element in the summary facet.
     * 
     * @param mgr
     * @return
     */
    public static OtmCore buildOtm(OtmModelManager mgr) {
        OtmCore core = new OtmCore( buildTL( mgr.getStringType().getTL() ), mgr );
        mgr.add( core );
        assertNotNull( core );

        // OtmXsdSimple sType = mgr.getStringType();
        // core.setAssignedType( sType );
        // int i = 1;
        //
        // TLProperty tlp = new TLProperty();
        // tlp.setName( "P"+i++ );
        // tlp.setType( sType.getTL() );
        // core.getTL().getSummaryFacet().addElement( tlp );
        //
        // TLAttribute tlA = new TLAttribute();
        // tlA.setName( "P"+i++ );
        // core.getTL().getSummaryFacet().addAttribute( tlA );
        // OtmIdAttribute<TLAttribute> id = new OtmIdAttribute<TLAttribute>( tlId, core.getSummary() );
        // core.getSummary().add( id );

        assertTrue( core.getChildren().size() > 3 ); // Is 6
        assertTrue( core.getSummary().getChildren().size() == 2 );
        assertTrue( "Builder - must be managed.", mgr.getMembers().contains( core ) );
        return core;
    }

    public static TLCoreObject buildTL() {
        TLCoreObject tlCore = new TLCoreObject();
        tlCore.setName( CORE_NAME );
        return tlCore;
    }

    public static TLCoreObject buildTL(XSDSimpleType sType) {
        TLCoreObject tlCore = buildTL();
        int i = 1;

        tlCore.getSimpleFacet().setSimpleType( sType );

        TLProperty tlp = new TLProperty();
        tlp.setName( "P" + i++ );
        tlp.setType( sType );
        tlCore.getSummaryFacet().addElement( tlp );

        TLAttribute tlA = new TLAttribute();
        tlA.setName( "P" + i++ );
        tlA.setType( sType );
        tlCore.getSummaryFacet().addAttribute( tlA );

        return tlCore;
    }

    /**
     * @param mgr
     * @param name
     * @return
     */
    public static OtmCore buildOtm(OtmModelManager mgr, String name) {
        OtmCore core = buildOtm( mgr );
        core.setName( name );
        return core;
    }

    /**
     * Create a core object with the passed name in the library and model manager with an ID attribute and element in
     * the summary facet.
     * 
     * @param library
     * @param name
     * @return
     */
    public static OtmCore buildOtm(OtmLibrary lib, String name) {
        OtmModelManager mgr = lib.getModelManager();
        URL url = lib.getTL().getLibraryUrl();
        if (url == null)
            log.debug( "Missing URL on library." );
        assertTrue( "Library must have an URL.", url != null );

        OtmCore core = new OtmCore( buildTL( mgr.getStringType().getTL() ), mgr );
        lib.add( core );
        mgr.add( core );
        assertNotNull( core );
        core.setName( OtmLibraryMemberFactory.getUniqueName( lib, name ) );
        assertTrue( "Build: ", lib.getMembers().contains( core ) );

        if (!core.isValid()) {
            log.debug( ValidationUtils.getMessagesAsString( core.getFindings() ) );
            List<ValidationFinding> findings = core.getFindings().getAllFindingsAsList();
        }
        assertTrue( core.isValid() );

        return core;
    }

    /**
     * @param lib
     * @return
     */
    public static OtmCore buildOtm(OtmLibrary lib) {
        OtmCore core = buildOtm( lib.getModelManager() );
        lib.add( core );
        return core;
    }
}
