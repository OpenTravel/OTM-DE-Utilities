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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.AbstractDexTest;
import org.opentravel.common.DexLibraryException;
import org.opentravel.dex.tasks.DexTaskException;
import org.opentravel.dex.tasks.repository.VersionLibraryTask.VersionType;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmLocalLibrary;
import org.opentravel.model.otmContainers.OtmManagedLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.model.otmContainers.OtmVersionChain;
import org.opentravel.model.otmContainers.OtmVersionChainEmpty;
import org.opentravel.model.otmContainers.OtmVersionChainVersioned;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmContainers.TestOtmVersionChain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Verifies the functions of the <code>Otm Model Manager's Version Chain Manager</code>.
 */
public class TestOtmModelChainsManager extends AbstractDexTest {
    private static Log log = LogFactory.getLog( TestOtmModelChainsManager.class );

    /**
     * Check to assure chain manager:
     * <ul>
     * <li>has a version chain for the library
     * <li>has a version chain for the library's chain name
     * <li>library is included in chainNames set
     * <li>library is included in chainLibraries list
     * </ul>
     * 
     * @param lib
     * @param cm if null, use the library's modelManager's chains manager.
     */
    public static void check(OtmLibrary lib, OtmModelChainsManager cm) {
        assertTrue( "Check: Must have model manager.", lib.getModelManager() != null );
        if (cm == null)
            cm = lib.getModelManager().getChainsManager();
        assertTrue( "Check: ", cm.get( lib ) != null );
        assertTrue( "Check: ", cm.get( cm.getChainName( lib ) ) != null );
        assertTrue( "Check: ", cm.getChainNames().contains( lib.getChainName() ) );
        assertTrue( "Check: ", cm.getChainLibraries( cm.getChainName( lib ) ).contains( lib ) );
    }

    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( TestOtmModelChainsManager.class );
    }

    @Test
    public void testAdd() {
        OtmModelManager mgr = getModelManager();
        mgr.clear();
        OtmProject proj = TestOtmProjectManager.buildProject( mgr );
        String ns1 = "http://example.com/ns1/chaintest";
        String ns2 = "http://example.com/ns2/chaintest";
        OtmModelChainsManager cm = new OtmModelChainsManager();

        OtmLibrary localLib = TestLibrary.buildOtm( mgr );
        cm.add( localLib );
        assertTrue( "Then: must get added library.", cm.get( localLib ) instanceof OtmVersionChainEmpty );

        OtmLibrary majorLib = buildMajor( "TMM1", proj, ns1 );
        cm.add( majorLib );
        // OtmVersionChain chainMajor = cm.get( majorLib );
        assertTrue( "Then: must get added library.", cm.get( majorLib ) instanceof OtmVersionChainVersioned );

        OtmLibrary minor1 = buildMinor( majorLib );
        cm.add( minor1 );
        // OtmVersionChain chainMinor1 = cm.get( minor1 );
        assertTrue( "Given: must have same chain name.",
            cm.getChainName( minor1 ).equals( cm.getChainName( majorLib ) ) );
        assertTrue( "Then: must get added library.", cm.get( minor1 ) instanceof OtmVersionChainVersioned );
        assertTrue( "Then: Must be same chain.", cm.get( minor1 ) == cm.get( majorLib ) );

        OtmLibrary minor2 = buildMinor( minor1 );
        cm.add( minor2 );
        assertTrue( "Then: must get added library.", cm.get( minor2 ) instanceof OtmVersionChainVersioned );
        assertTrue( "Then: Must be same chain.", cm.get( minor2 ) == cm.get( majorLib ) );

        // Same namespace but different name
        OtmLibrary majorLibA = buildMajor( "TMM1a", proj, ns1 );
        cm.add( majorLibA );
        assertTrue( "Then: must get added library.", cm.get( majorLibA ) instanceof OtmVersionChainVersioned );
        assertTrue( "Then: Must NOT be same chain.", cm.get( majorLibA ) != cm.get( majorLib ) );

        // Same name, different namespace
        OtmLibrary majorLib2 = buildMajor( "TMM1", proj, ns2 );
        cm.add( majorLib2 );
        assertTrue( "Then: must get added library.", cm.get( majorLib2 ) instanceof OtmVersionChainVersioned );
        assertTrue( "Then: Must NOT be same chain.", cm.get( majorLib2 ) != cm.get( majorLib ) );
    }

    @Test
    public void testAdd_AlreadyInChain() throws DexLibraryException {
        OtmManagedLibrary mLib = buildMajor( "VCMgrTest" );
        OtmModelChainsManager cm = new OtmModelChainsManager();
        // When
        cm.add( mLib );

        // Then
        assertTrue( "Then: manager must return chain for library.", cm.get( mLib ) != null );
    }

    @Test
    public void testAdd_MultipleMajors() throws DexTaskException {
        OtmModelManager mgr = getModelManager();
        mgr.clear();
        OtmProject proj = TestOtmProjectManager.buildProject( mgr );
        String ns1 = "http://example.com/ns1/chaintest";
        String ns2 = "http://example.com/ns2/chaintest";
        OtmModelChainsManager cm = new OtmModelChainsManager();

        // Given a major library
        OtmManagedLibrary major1 = buildMajor( "TMM1", proj, ns1 );
        cm.add( major1 );
        OtmVersionChain major1chain = cm.get( major1 );
        assertTrue( "Then: must get added library.", major1chain instanceof OtmVersionChainVersioned );
        check( major1, cm );

        // When a new version is created
        OtmManagedLibrary major2 = rtuVersion( VersionType.MAJOR, major1 );
        cm.add( major2 );
        OtmVersionChain major2chain = cm.get( major2 );

        // Then
        assertTrue( "Then: must get added library.", major2chain instanceof OtmVersionChainVersioned );
        check( major1, cm );
        check( major2, cm );
        assertTrue( "Then: chains are different.", major1chain != major2chain );
    }

    @Test
    public void testAdd_NotInChain() {
        // This is hard to test since added by Version and Publish
    }

    /**
     * Test against the pre-built library suite
     */
    @Test
    public void testAdd_TestSuite() {
        OtmModelManager mgr = getModelManager();
        buildLibraryTestSet();
        OtmModelChainsManager cm = mgr.getChainsManager();

        // cm.chainMap.keySet().forEach( k -> log.debug( k ) );
        // ArrayList<OtmVersionChain> chains = new ArrayList<>( cm.chainMap.values() );

        // If added, get will work.
        for (OtmLibrary lib : mgr.getUserLibraries()) {
            OtmVersionChain chain = cm.get( lib );
            assertTrue( chain != null );
            assertTrue( "Given: ", chain.contains( lib ) );
        }
    }


    @Test
    public void testConstructor() {
        OtmModelChainsManager cm = new OtmModelChainsManager();
        assertTrue( "Then: ", cm != null );
    }

    @Test
    public void testGetChainLibraries() {
        getModelManager().clear();
        OtmManagedLibrary mLib = buildMajor( "VCMgrTest3" );
        OtmLocalLibrary lLib = TestLibrary.buildOtm( getModelManager() );
        OtmModelChainsManager cm = new OtmModelChainsManager();
        // When
        cm.add( mLib );
        cm.add( lLib );

        // Then
        assertTrue( "Then: ", cm.getChainLibraries( cm.getChainName( mLib ) ).contains( mLib ) );
        assertTrue( "Then: ", cm.getChainLibraries( cm.getChainName( lLib ) ).contains( lLib ) );
    }

    /**
     * Test Facade for {@link OtmVersionChain#getLibraries()}
     * {@link TestOtmVersionChain#testGetChainLibraries_TestSuite()}
     */
    @Test
    public void testGetChainLibraries_TestSuite() {
        OtmModelManager mgr = getModelManager();
        Map<OtmVersionChain,List<OtmLibrary>> setMap = buildLibraryTestSet();
        OtmModelChainsManager cm = mgr.getChainsManager();

        List<OtmVersionChain> chains = new ArrayList<>( cm.chainMap.values() );

        // Check against the test set's map of chain:libraries
        for (OtmVersionChain chain : chains) {
            assertTrue( "Given: test set map must contain the chain.", setMap.containsKey( chain ) );
            String chainName = chain.getBaseNamespace();
            List<OtmLibrary> cLibs = cm.getChainLibraries( chainName );
            assertTrue( "Then: must have name.", chainName != null && !chainName.isEmpty() );
            assertTrue( "Then: Chain must have at least one library.", !chain.getLibraries().isEmpty() );

            for (OtmLibrary lib : setMap.get( chain )) {
                assertTrue( "Then: chains manger facade must return each set member", cLibs.contains( lib ) );
            }
        }
    }

    @Test
    public void testGetChainName() {
        OtmManagedLibrary mLib = buildMajor( "VCMgrTest2" );
        OtmLocalLibrary lLib = TestLibrary.buildOtm( getModelManager() );
        OtmModelChainsManager cm = new OtmModelChainsManager();
        // When
        cm.add( mLib );
        cm.add( lLib );

        // Then
        assertTrue( "Then: ", cm.getChainName( mLib ) != null );
        assertTrue( "Then: ", cm.getChainName( mLib ).equals( mLib.getChainName() ) );
        assertTrue( "Then: ", cm.getChainName( lLib ) != null );
        assertTrue( "Then: ", cm.getChainName( lLib ).equals( lLib.getChainName() ) );
    }

    @Test
    public void testGets() {
        OtmManagedLibrary mLib = buildMajor( "VCMgrTest2" );
        OtmLocalLibrary lLib = TestLibrary.buildOtm( getModelManager() );
        OtmModelChainsManager cm = new OtmModelChainsManager();
        // When
        cm.add( mLib );
        cm.add( lLib );

        // Then
        check( mLib, cm );
        assertTrue( "Then: ", cm.get( mLib ) != null );
        assertTrue( "Then: ", cm.get( cm.getChainName( mLib ) ) != null );
        assertTrue( "Then: ", cm.getChainLibraries( cm.getChainName( mLib ) ).contains( mLib ) );

        check( lLib, cm );
        assertTrue( "Then: ", cm.get( lLib ) != null );
        assertTrue( "Then: ", cm.get( cm.getChainName( lLib ) ) != null );
        assertTrue( "Then: ", cm.getChainLibraries( cm.getChainName( lLib ) ).contains( lLib ) );
    }

    @Test
    public void testRemove() {
        buildLibraryTestSet();
        OtmModelManager mgr = getModelManager();
        OtmModelChainsManager cm = mgr.getChainsManager();

        // Tests
        for (OtmLibrary lib : mgr.getUserLibraries()) {
            cm.remove( lib );
            // Then - either the chain is removed or the library is removed from chain.
            if (cm.get( lib ) != null)
                assertTrue( "Given: ", !cm.get( lib ).contains( lib ) );
        }
    }
}

