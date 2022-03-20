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
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLProperty;

/**
 * Verifies the functions of the <code>OtmContextualFacet</code> class.
 */
public class TestExtensionPointFacet extends TestOtmLibraryMemberBase<OtmExtensionPointFacet> {
    private static Logger log = LogManager.getLogger( TestExtensionPointFacet.class );
    private static final String EPF_NAME = "TestEPF";

    protected static OtmLibraryMember member = null;
    protected static OtmExtensionPointFacet epf = null;

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null, null );
        // Needed for library member tests
        baseObject = TestCore.buildOtm( staticModelManager );
        subject = buildOtm( staticModelManager, ((OtmCore) baseObject).getSummary() );
    }

    @Before
    public void beforeMethods() {
        staticModelManager.clear();
        staticLib = TestLibrary.buildOtm( staticModelManager );

        OtmLibrary baseLib = TestLibrary.buildOtm( staticModelManager, "http://ns2", "bpf", "BaseLib" );
        baseObject = TestCore.buildOtm( baseLib, "BaseCore" );
        subject = buildOtm( staticLib, ((OtmCore) baseObject).getSummary() );
    }

    @Override
    public void testConstructors(OtmExtensionPointFacet otm) {
        assertNotNull( otm );
        assertTrue( "Must be its own owner", otm.getOwningMember() == otm );
        assertTrue( "Must have identity listner.", OtmModelElement.get( otm.getTL() ) == otm );
        // if (otm.getModelManager() != null)
        // assertTrue( "Must be managed in model manager.", otm.getModelManager().getMembers().contains( otm ) );
        log.debug( "Simple Constuctor OK." );
    }

    public static void testExtensionPointFacet(OtmExtensionPointFacet epf, OtmObject baseFacet) {
        log.debug( "Testing extension point facet: " + epf );
        assertTrue( epf != null );
        assertTrue( baseFacet != null );
        //
        assertTrue( "Extension point facet must have base facet.", epf.getBaseType() == baseFacet );
        assertTrue( "Extension point facet must have library.", epf.getLibrary() != null );
        // assertTrue( "Extension point facet library must be patch.", epf.getLibrary().isPatchVersion());
        assertTrue( "Model manager must find TL EPF.", staticModelManager.getMember( epf.getTL() ) != null );
        assertTrue( "Extension point facet must have action manager.", epf.getActionManager() != null );
    }

    /**
     * ******************************************
     * 
     */
    private static OtmExtensionPointFacet buildOtm(OtmLibrary lib, OtmObject baseFacet) {
        assertTrue( "Given: must build into a different library than base object.", lib != baseFacet.getLibrary() );
        epf = buildOtm( lib.getModelManager(), baseFacet );
        lib.add( epf );
        return epf;
    }

    /**
     * Create custom facet and contribute it to the passed business object.
     * 
     * @param modelManager
     * @param bo
     * @return
     */
    private static OtmExtensionPointFacet buildOtm(OtmModelManager modelManager, OtmObject baseFacet) {
        OtmExtensionPointFacet epf = buildOtm( modelManager );
        epf.setBaseType( baseFacet );
        return epf;
    }

    private static OtmExtensionPointFacet buildOtm(OtmModelManager modelManager) {
        TLExtensionPointFacet tlEPF = buildTL();
        OtmExtensionPointFacet epf = new OtmExtensionPointFacet( tlEPF, modelManager );
        return epf;
    }

    public static TLExtensionPointFacet buildTL() {
        TLExtensionPointFacet tlEPF = new TLExtensionPointFacet();
        tlEPF.addAttribute( new TLAttribute() );
        tlEPF.addElement( new TLProperty() );
        return tlEPF;
    }

}
