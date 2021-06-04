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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.AbstractDexTest;
import org.opentravel.TestDexFileHandler;
import org.opentravel.common.DexProjectException;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.dex.action.manager.DexMinorVersionActionManager;
import org.opentravel.dex.action.manager.DexReadOnlyActionManager;
import org.opentravel.model.otmContainers.OtmDomain;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.model.otmContainers.OtmVersionChain;
import org.opentravel.model.otmContainers.OtmVersionChainEmpty;
import org.opentravel.model.otmContainers.OtmVersionChainVersioned;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmXsdSimple;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.ProjectManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Verifies the functions of the <code>Otm Model Manager</code>.
 */
public class TestOtmModelManager_Gets extends AbstractDexTest {
    private static Log log = LogFactory.getLog( TestOtmModelManager_Gets.class );

    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( TestOtmModelManager_Gets.class );
    }

    @Test
    public void testGetPredefinedTypes() {
        // Given
        OtmModelManager mgr = new OtmModelManager( null, null, null );
        mgr.addLibraries_BuiltIn( mgr.getTlModel() );

        // Then - id and empty will be non-null
        OtmXsdSimple id = mgr.getIdType();
        assertNotNull( id );
        OtmXsdSimple empty = mgr.getEmptyType();
        assertNotNull( empty );
    }

    @Test
    public void getDomains() {
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        String base1 = "http://example.com/bns1";
        String base2 = "http://example.com/bns2";
        String subBase1 = "http://example.com/bns1/sub1";
        String subBase2 = "http://example.com/bns2/sub2";

        // When - no unmanaged user libraries
        List<OtmDomain> domains = mgr.getDomains();
        assertTrue( domains.isEmpty() );
        assertTrue( "Must not be found.", mgr.getDomain( base1 ) == null );
        //
        // When - unmanaged library added
        OtmLibrary lib1 = TestLibrary.buildOtm( mgr, base1, "b1", "Base1a" );
        // Then
        assertTrue( !domains.isEmpty() );
        assertTrue( "Must be found.", mgr.getDomain( base1 ) != null );
        assertTrue( "Must be found.", mgr.getDomain( base1 ).getBaseNamespace().equals( base1 ) );

        // When - second domain created
        OtmLibrary lib2 = TestLibrary.buildOtm( mgr, base2, "b2", "Base2" );
        assertTrue( "Must be found.", mgr.getDomain( base1 ) != null );
        assertTrue( "Must be found.", mgr.getDomain( base1 ).getBaseNamespace().equals( base1 ) );
        assertTrue( "Must be found.", mgr.getDomain( base2 ) != null );
        assertTrue( "Must be found.", mgr.getDomain( base2 ).getBaseNamespace().equals( base2 ) );

        // When - sub-domain added to domain 1
        OtmLibrary lib1s1 = TestLibrary.buildOtm( mgr, subBase1, "sb1", "SubBase1" );
        assertTrue( "Must be found.", mgr.getDomain( base1 ) != null );
        assertTrue( "Must be found.", mgr.getDomain( base1 ).getBaseNamespace().equals( base1 ) );
        assertTrue( "Must be found.", mgr.getDomain( subBase1 ) != null );
        assertTrue( "Must be found.", mgr.getDomain( subBase1 ).getBaseNamespace().equals( subBase1 ) );
    }

    /**
     * get(abstractLibrary) get(otmLibrary) get(String) get(TLLibrary)
     */
    @Test
    public void testGets() {
        // Given a project that uses local library files
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
        TestDexFileHandler.loadAndAddManagedProject( mgr );
        TLModel tlModel = mgr.getTlModel();
        assertNotNull( tlModel );

        // Then
        for (AbstractLibrary absLibrary : tlModel.getAllLibraries()) {
            assertTrue( mgr.get( absLibrary ) instanceof OtmLibrary );

            if (absLibrary instanceof TLLibrary)
                assertTrue( mgr.get( (TLLibrary) absLibrary ) instanceof OtmLibrary );

            String ns = absLibrary.getNamespace();
            String fullName = ns + "/" + absLibrary.getName();
            assertTrue( mgr.get( fullName ) instanceof OtmLibrary );
        }

        for (OtmLibrary library : mgr.getLibraries())
            assertTrue( mgr.get( library ) instanceof AbstractLibrary );

        log.debug( "Tested get() on " + mgr.getLibraries().size() + " libraries." );
    }


    /**
     * Get Tests
     * 
     * getActionManager(true/false) getMinorActionManager(true/false)
     */
    @Test
    public void testGetActionManagers() {
        // Given a project that uses local library files
        OtmModelManager mgr = new OtmModelManager( new DexFullActionManager( null ), repoManager, null );
        DexActionManager actionMgr = null;

        // This is independent of libraries.
        actionMgr = mgr.getActionManager( true );
        assertTrue( actionMgr instanceof DexFullActionManager );
        actionMgr = mgr.getActionManager( false );
        assertTrue( actionMgr instanceof DexReadOnlyActionManager );

        actionMgr = mgr.getMinorActionManager( true );
        assertTrue( actionMgr instanceof DexMinorVersionActionManager );
        actionMgr = mgr.getMinorActionManager( false );
        assertTrue( actionMgr instanceof DexReadOnlyActionManager );
    }

    /**
     * getEditableLibraries() getLibraries() getUserLibraries()
     */
    @Test
    public void testGetLibraries() {
        // Given library files
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
        TestDexFileHandler.loadAndAddManagedProject( mgr );

        // Then
        assertFalse( mgr.getLibraries().isEmpty() );
        assertFalse( mgr.getEditableLibraries().isEmpty() );
        assertFalse( mgr.getUserLibraries().isEmpty() );

        for (OtmLibrary library : mgr.getLibraries())
            assertTrue( mgr.get( library ) instanceof AbstractLibrary );

        for (OtmLibrary library : mgr.getEditableLibraries())
            assertTrue( mgr.get( library ) instanceof TLLibrary );

        for (OtmLibrary library : mgr.getUserLibraries())
            assertTrue( mgr.get( library ) instanceof TLLibrary );
    }


    /**
     * getProjectManager() getProject(String) getProjects() getProjects(AbstractLibrary) getUserProjects()
     */
    @Test
    public void testGetProjects() {
        // Given a project that uses local library files
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
        TestDexFileHandler.loadAndAddManagedProject( mgr );
        TLModel tlModel = mgr.getTlModel();
        assertNotNull( tlModel );

        assertTrue( mgr.getProjectManager() instanceof ProjectManager );

        Collection<OtmProject> projects = mgr.getProjects();
        assertFalse( projects.isEmpty() );

        for (OtmProject p : projects)
            assertTrue( mgr.getOtmProjectManager().getProject( p.getName() ) == p );

        for (AbstractLibrary aLib : mgr.getTlModel().getAllLibraries())
            assertFalse( mgr.getOtmProjectManager().getProjects( aLib ).isEmpty() );
        for (OtmLibrary lib : mgr.getLibraries())
            assertFalse( mgr.getOtmProjectManager().getProjects( lib.getTL() ).isEmpty() );

        assertFalse( mgr.getProjects().isEmpty() );
    }

    /**
     * getBaseNamespaces() constructed
     * 
     * @throws DexProjectException
     */
    @Test
    public void testGetBaseNamespaces() throws DexProjectException {
        String ns = "http://example.com/namespaces/";
        String prefix = "pf";
        String name = "TestModelGetBaseNS";
        // Given - 2 local libraries in different namespaces
        String libName = "lib1";
        OtmLibrary lib = TestLibrary.buildOtm( getModelManager(), ns + libName, prefix + libName, libName );
        libName = "lib2";
        lib = TestLibrary.buildOtm( getModelManager(), ns + libName, prefix + libName, libName );

        // Given - 2 published major lib in different namespaces
        OtmProject proj = TestOtmProjectManager.buildProject( getModelManager() );
        libName = "lib3";
        lib = buildMajor( libName, proj, ns + libName );
        libName = "lib4";
        lib = buildMajor( libName, proj, ns + libName );

        List<OtmLibrary> libs = getModelManager().getUserLibraries();
        assertTrue( "Given: must have 4 libraries.", libs.size() == 4 );
        List<String> namespaces = new ArrayList<>();
        libs.forEach( l -> namespaces.add( l.getBaseNS() ) );
        assertTrue( "Given: must have 4 namespaces.", namespaces.size() == 4 );

        // When
        List<String> nsList = getModelManager().getBaseNamespaces();

        // Then
        assertTrue( "Then: Must have base namespaces.", !nsList.isEmpty() );
        assertTrue( "Then: Must have 4 base namespaces.", nsList.size() == 4 );

        // Then compare the lists
        nsList.forEach( b -> assertTrue( "Then: retrieved ns must be in namespaces.", namespaces.contains( b ) ) );
        for (String n : namespaces)
            assertTrue( "Then: namespace must be in retrieved list.", nsList.contains( n ) );
    }

    /**
     * getBaseNamespaces() from loaded projects
     */
    @Test
    public void testGetBaseNamespaces_LoadedProjects() {
        // Given a project that uses local library files
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
        TestDexFileHandler.loadAndAddManagedProject( mgr );
        TLModel tlModel = mgr.getTlModel();
        assertTrue( "Given: ", tlModel != null );

        // When
        List<String> nsList = mgr.getBaseNamespaces();

        // Then - 5/25/2021 - loads 6 namespaces
        assertTrue( "Then: Must have base namespaces.", !nsList.isEmpty() );
        assertTrue( "Then: Must have 6 base namespaces.", nsList.size() == 6 );
    }

    /**
     * getUserSettings()
     * 
     * getLibraryChain(string) getVersionChain(OtmLibrary) getVersionChainFactory()
     * 
     */
    @Test
    public void testGetUserSettings() {
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );

        // Running headless
        assertTrue( mgr.getUserSettings() == null );
    }

    /**
     * Facade for {@link OtmModelChainsManager#getChainName(OtmLibrary)}
     */
    @Test
    public void testGetChainLibraries() {
        // Given a project that uses local library files
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
        TestDexFileHandler.loadAndAddManagedProject( mgr );
        TLModel tlModel = mgr.getTlModel();
        assertTrue( "Given: ", tlModel != null );

        // Then
        for (OtmLibrary lib : mgr.getUserLibraries())
            assertTrue( mgr.getChainLibraries( lib ).contains( lib ) );
    }

    @Test
    public void TestGetVersionChain() {
        OtmModelManager mgr = getModelManager();
        OtmProject proj = TestOtmProjectManager.buildProject( mgr );
        String ns1 = "http://example.com/ns1/chaintest";
        String ns2 = "http://example.com/ns2/chaintest";

        // Library with empty chain
        OtmLibrary localLib = TestLibrary.buildOtm( mgr );
        OtmVersionChain lChain = mgr.getVersionChain( localLib );
        assertTrue( lChain instanceof OtmVersionChainEmpty );
        assertTrue( lChain.getLibraries().contains( localLib ) );

        // A major library
        OtmLibrary majorLib = buildMajor( "TMM1", proj, ns1 );
        OtmVersionChain mChain = mgr.getVersionChain( majorLib );
        assertTrue( mChain instanceof OtmVersionChainVersioned );
        assertTrue( mChain.getLibraries().contains( majorLib ) );

        // A major library
        OtmLibrary majorLibA = buildMajor( "TMM1a", proj, ns1 );
        OtmVersionChain mChainA = mgr.getVersionChain( majorLibA );
        assertTrue( mChainA instanceof OtmVersionChainVersioned );
        assertTrue( mChainA.getLibraries().contains( majorLibA ) );

        // A minor library
        OtmLibrary minor1 = buildMinor( majorLib );
        OtmVersionChain mChain1 = mgr.getVersionChain( minor1 );
        assertTrue( mChain1 instanceof OtmVersionChainVersioned );
        assertTrue( mChain1.getLibraries().contains( minor1 ) );

        OtmLibrary minor2 = buildMinor( minor1 );
        OtmVersionChain mChain2 = mgr.getVersionChain( minor2 );
        assertTrue( mChain2 instanceof OtmVersionChainVersioned );
        assertTrue( mChain2.getLibraries().contains( minor2 ) );

        OtmLibrary majorLib2 = buildMajor( "TMM2", proj, ns2 );
        OtmVersionChain mChainA2 = mgr.getVersionChain( majorLib2 );
        assertTrue( mChainA2 instanceof OtmVersionChainVersioned );
        assertTrue( mChainA2.getLibraries().contains( majorLib2 ) );

        List<OtmVersionChain> chains = new ArrayList<>();
        mgr.getUserLibraries().forEach( l -> chains.add( mgr.getVersionChain( l ) ) );
    }
}

