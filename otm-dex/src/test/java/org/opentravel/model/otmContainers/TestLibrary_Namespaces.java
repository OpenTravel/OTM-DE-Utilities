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

package org.opentravel.model.otmContainers;

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.AbstractDexTest;
import org.opentravel.common.DexLibraryException;
import org.opentravel.common.DexProjectException;
import org.opentravel.model.TestOtmProjectManager;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.version.VersionChain;
import org.opentravel.schemacompiler.version.VersionChainFactory;

/**
 * Verify handling of various namespaces
 * 
 */
public class TestLibrary_Namespaces extends AbstractDexTest {
    private static Log log = LogFactory.getLog( TestLibrary_Namespaces.class );

    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( TestLibrary.class );
    }

    String base = "http://example.com/ns";
    String version = "/v1";
    String base1 = base + "/ns1";
    String base2 = base + "/ns2";
    String base3 = base + "/ns3";
    String base4 = base + "/ns4";
    String[] bases = {base1, base2, base3, base4};

    // Sub-namespaces
    String sub1 = base + "/ns1";
    String sub2 = sub1 + "/ns2";
    String sub3 = sub2 + "/ns3";
    String sub4 = sub3 + "/ns4";
    String[] subs = {sub1, sub2, sub3, sub4};

    @Test
    public void getBaseNS() throws DexProjectException, DexLibraryException {
        // OtmLibrary lib = TestLibrary.buildOtm( getModelManager() );
        // TLLibrary tlLib = (TLLibrary) lib.getTL();
        OtmProject proj = TestOtmProjectManager.buildProject( getModelManager() );
        // proj.add( (OtmLocalLibrary) lib );
        // ProjectItem pi = getModelManager().getProjectManager().getProjectItem( tlLib );
        // assertTrue( "Given: tl library is in a project.", pi != null );

        // Given - testing with versioned libraries
        OtmLibrary lib = buildMajor( "NSTest", proj, base + version );
        lib = buildMinor( lib );
        TLLibrary tlLib = (TLLibrary) lib.getTL();

        // When null
        tlLib.setNamespace( null );
        assertTrue( "Then: must have null namespace.", lib.getBaseNS() == null );

        // When empty
        tlLib.setNamespace( "" );
        assertTrue( "Then: must have null namespace.", lib.getBaseNS().isEmpty() );

        // When no version
        tlLib.setNamespace( base1 );
        // String result = lib.getBaseNamespace();
        assertTrue( "Then: must have full namespace.", lib.getBaseNS().equals( base1 ) );

        // When set to one of the bases
        for (String bns : bases) {
            tlLib.setNamespace( bns + version );
            assertTrue( "Then: ", lib.getBaseNS().equals( bns ) );
        }

        // When set to one of the bases
        // Then the full sub-namespace is returned
        for (String sub : subs) {
            tlLib.setNamespace( sub + version );
            log.debug( sub + " resulted in " + lib.getBaseNS() );
            assertTrue( "Then: ", lib.getBaseNS().equals( sub ) );
        }
    }

    /**
     * Assure lib.getBaseNamespace and VersionChain return same baseNS
     * 
     * @throws DexLibraryException
     */
    @Test
    public void testVersionChainFactory() throws DexLibraryException {
        // Given - testing with versioned libraries
        OtmProject proj = TestOtmProjectManager.buildProject( getModelManager() );
        OtmLibrary lib = buildMajor( "NSTest", proj, base + version );
        int i = 1;
        for (String bns : bases) {
            lib = buildMajor( "NSTest" + i, proj, bns + version );
            lib = buildMinor( lib );
        }
        // Note: one of these is in same namespace as base1
        for (String bns : subs) {
            lib = buildMajor( "NSTestSub" + i, proj, bns + version );
            lib = buildMinor( lib );
        }
        // TLLibrary tlLib = (TLLibrary) lib.getTL();

        // Given - version chain factory
        VersionChainFactory vcFactory = getVersionChainFactory( getModelManager().getTlModel() );

        for (String bns : vcFactory.getBaseNamespaces())
            log.debug( bns );

        // Compare lib.getbasenamespace with vc
        for (OtmLibrary l : getModelManager().getUserLibraries()) {
            VersionChain<TLLibrary> vc = vcFactory.getVersionChain( (TLLibrary) l.getTL() );
            String vns = vc.getBaseNS();
            String lns = l.getBaseNS();
            assertTrue( "Then: ", vns.equals( lns ) );
        }
    }

    /**
     * Use the TL Model to attempt to get a version chain factory.
     * 
     * @return the factory or null if factory throws exception
     * @throws DexLibraryException
     */
    private static VersionChainFactory getVersionChainFactory(TLModel tlModel) throws DexLibraryException {
        VersionChainFactory versionChainFactory = null;
        try {
            versionChainFactory = new VersionChainFactory( tlModel );
        } catch (Exception e) {
            throw new DexLibraryException( "Factory can not access version chain factory." );
        }
        return versionChainFactory;
    }


}
