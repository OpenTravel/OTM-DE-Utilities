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

package org.opentravel.model;

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.AbstractDexTest;
import org.opentravel.TestDexFileHandler;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

import java.util.Set;

/**
 * Verifies the functions of the <code>DEX Namespace Handler</code> class.
 */
public class TestOtmModelNamespaceManager extends AbstractDexTest {
    private static Logger log = LogManager.getLogger( TestOtmModelNamespaceManager.class );

    @BeforeClass
    public static void beforeClass() throws Exception {
        beforeClassSetup( TestOtmModelNamespaceManager.class );
    }

    @Test
    public void testGetBaseNamespaces() {
        OtmModelManager mgr = getModelManager();
        mgr.clear();
        String ns1_1 = "http://example.com/nstest/ns1/v1_1";
        String ns1_2 = "http://example.com/nstest/ns1/v1_2";
        String ns2 = "http://example.com/nstest/ns2/v1";
        URLUtils.isValidURI( ns1_1 );

        OtmLibrary lib1_1 = TestLibrary.buildOtm( mgr, ns1_1, "p1_1", "TestNSMgr1" );
        OtmLibrary lib1_2 = TestLibrary.buildOtm( mgr, ns1_2, "p1_2", "TestNSMgr2" );
        OtmLibrary lib2 = TestLibrary.buildOtm( mgr, ns2, "p2", "TestNSMgr3" );

        // Then
        assertTrue( "Then: must only have 2 base namespaces.", mgr.getBaseNamespaces().size() == 2 );

        // Debugging - not valid versioned OTA2 namespaces
        //
        // String ns1_1 = "http://example.com/nstest/ns1/v1.1";
        // String ns1_2 = "http://example.com/nstest/ns1/v1.2";
        // AbstractLibrary absLib = lib1_1.getTL();
        // String versionScheme = absLib.getVersionScheme(); // OTA2
        // VersionScheme vScheme = null;
        // try {
        // vScheme = VersionSchemeFactory.getInstance().getVersionScheme( versionScheme );
        // } catch (VersionSchemeException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // String versionId = vScheme.getVersionIdentifier( absLib.getNamespace() );
        // Integer vn = Integer.valueOf( vScheme.getMinorVersion( versionId ) );
        // String baseNS = lib1_1.getBaseNS();
        // String nPlus = lib1_1.getNameWithBasenamespace();

    }

    // TODO - test, should this be starts with or equals?
    @Test
    public void testGetBaseNsLibraries() throws VersionSchemeException {
        OtmModelManager mgr = getModelManager();
        mgr.clear();
        String ns1_1 = "http://example.com/nstest/ns1/v1_1";
        String ns1_2 = "http://example.com/nstest/ns1/v_2";
        String ns2 = "http://example.com/nstest/ns2/v1";

        OtmLibrary lib1_1 = TestLibrary.buildOtm( mgr, ns1_1, "p1_1", "TestNSMgr1" );
        OtmLibrary lib1_2 = TestLibrary.buildOtm( mgr, ns1_2, "p1_2", "TestNSMgr2" );
        OtmLibrary lib2 = TestLibrary.buildOtm( mgr, ns2, "p2", "TestNSMgr3" );
        assertTrue( "Given: must only have 2 base namespaces.", mgr.getBaseNamespaces().size() == 2 );

        // Trim the full namespace down to just the base without version
        VersionScheme vScheme = VersionSchemeFactory.getInstance().getVersionScheme( "OTA2" );
        String bns1 = vScheme.getBaseNamespace( ns1_1 );
        String bns2 = vScheme.getBaseNamespace( ns2 );

        // Then
        assertTrue( "Then: must have 2 libraries.", mgr.getBaseNSLibraries( bns1 ).size() == 2 );
        assertTrue( "Then: must have 1 libraries.", mgr.getBaseNSLibraries( bns2 ).size() == 1 );
    }

    @Test
    public void testGetPrefix() {
        OtmModelManager modelMgr = TestOtmModelManager.build();
        OtmModelNamespaceManager nsHandler = new OtmModelNamespaceManager( modelMgr );
        String ns1 = "http://example.com/nstest/ns1";
        String pf1 = "pf1";
        String name1 = "testName1";
        TestLibrary.buildOtm( modelMgr, ns1, pf1, name1 );

        Set<String> prefixes = nsHandler.getPrefixes();
        assertTrue( "Given: Must have entry for " + pf1, prefixes.contains( pf1 ) );

        String newPf = nsHandler.getPrefix( ns1 );
        assertTrue( "Must be from namspace.", newPf.equals( pf1 ) );

        newPf = nsHandler.getPrefix( ns1 + "/part2" );
        assertTrue( "Must be different.", !newPf.equals( pf1 ) );
    }

    @Test
    public void testGetPrefixes() {
        OtmModelNamespaceManager nsHandler = new OtmModelNamespaceManager( getModelManager() );
        TestDexFileHandler.loadAndAddUnmanagedProject( getModelManager() );
        Set<String> prefixes = nsHandler.getPrefixes();

        assertTrue( "Given: must have entries in set.", !prefixes.isEmpty() );
        assertTrue( "Given: must have entries in set.", prefixes.contains( "ota2" ) );
        assertTrue( "Given: must have entries in set.", prefixes.contains( "xsd" ) );

        String ns1 = "http://example.com/nstest/ns1";
        String pf1 = "prefix1";
        String name1 = "testName1";
        TestLibrary.buildOtm( getModelManager(), ns1, pf1, name1 );

        prefixes = nsHandler.getPrefixes();
        assertTrue( "Must have entry for " + pf1, prefixes.contains( pf1 ) );
    }

    @Test
    public void testFixNamespaceVersion() {
        String ns = "http://example.com/ns1";
        String result = OtmModelNamespaceManager.fixNamespaceVersion( ns );
        assertTrue( result.endsWith( "/v1" ) );

        ns = "http://example.com/ns1/v3";
        result = OtmModelNamespaceManager.fixNamespaceVersion( ns );
        assertTrue( result.equals( ns ) );
    }
}
