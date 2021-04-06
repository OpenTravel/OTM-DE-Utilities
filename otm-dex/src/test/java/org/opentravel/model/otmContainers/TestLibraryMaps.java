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
import org.opentravel.TestDexFileHandler;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.TestOtmModelManager;
import org.opentravel.model.TestOtmModelMapsManager;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.TestOtmTypeProviderInterface;
import org.opentravel.model.otmProperties.TestOtmTypeUserInterface;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Library provides convenience functions to access model manager maps. {@link TestOtmModelMapsManager} These tests
 * concentrate on the maps in version chains.
 */
public class TestLibraryMaps extends AbstractFxTest {

    private static Log log = LogFactory.getLog( TestLibraryMaps.class );

    public static final boolean RUN_HEADLESS = true;
    final int WATCH_TIME = 0; // How long to sleep so we can see what is happening. Can be 0.
    //
    // final static String FXID_PROJECTCOMBO = "#projectCombo"; // if .projectCombo that would be css selector
    // final static String FILE_TESTOPENTRAVELREPO = "TestOpenTravelRepo.otp";
    // final static String FILE_TESTLOCAL = "TestLocalFiles.otp";

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestLibraryMaps.class );
        // Prevent java.nio.BufferOverflowException
        System.setProperty( "headless.geometry", "2600x2200-32" );
        // repoManager = repositoryManager.get();
        // assertTrue( "Given: ", repositoryManager != null );
        // assertTrue( "Given: ", repoManager != null );
        log.debug( "Before class setup tests ran." );
    }
    // @Before
    // public void beforeTest() {
    // modelManager.clear();
    // }

    // @AfterClass
    // public static void tearDownTests() throws Exception {
    // shutdownTestServer();
    // }

    // /**
    // * Set assigned type and if successful and list is non-null, add the user's owning member to the list.
    // */
    // private void setProvider(OtmTypeUser u, OtmTypeProvider p, List<OtmLibraryMember> list) {
    // OtmTypeProvider r = u.setAssignedType( p );
    // if (list != null && r != null && u.getLibrary() != p.getLibrary()) {
    // if (!list.contains( u.getOwningMember() ))
    // list.add( u.getOwningMember() );
    // }
    // }

    // Used in OtmDomain to create domain provider's map
    @Test
    public void testGetProvidersMap() {
        // Given - action and model managers
        OtmModelManager mgr = TestOtmModelManager.build();

        // Given - full set of cross-dependencies
        TestOtmDomain_Providers.buildCrossDependendDomains( mgr );
        assertTrue( TestOtmDomain_Providers.domain1 != null );
        List<OtmTypeProvider> providers = TestOtmDomain_Providers.providers;
        assertTrue( "Given: ", !providers.isEmpty() );

        // Given - the sub-domain library and its type providing member
        OtmLibrary lib22 = TestOtmDomain_Providers.domain22.getLibraries().get( 0 );

        assertTrue( "Given: ", lib22 != null );
        OtmLibraryMember member22 = null;
        for (OtmLibraryMember m : lib22.getMembers())
            if (m.getWhereUsed() != null)
                member22 = m;
        assertTrue( "Given: ", member22 != null );

        // Each domain has at least one library

        // When - domain 1's map created
        OtmLibrary lib = TestOtmDomain_Providers.domain1.getLibraries().get( 0 );
        Map<OtmLibrary,List<OtmLibraryMember>> map = lib.getProvidersMap();
        List<OtmLibrary> pLibs = new ArrayList<>( map.keySet() );
        // Then - it provides no types to the other libraries
        assertTrue( "Domain 1 must provider no types.", map.isEmpty() );

        // When - domain 3's map created
        lib = TestOtmDomain_Providers.domain3.getLibraries().get( 0 );
        map = lib.getProvidersMap();
        pLibs = new ArrayList<>( map.keySet() );
        // Then - all other libraries are in the map
        for (OtmLibrary l : mgr.getUserLibraries()) {
            // FIXME - This fails when run with other tests ...
            // A different instance of l with namespace as expected
            // if (!l.getBaseNamespace().equals( TestOtmDomain_Providers.domain3.getBaseNamespace() ))
            // assertTrue( pLibs.contains( l ) );
        }
        // Assure all users are from providers
        List<OtmTypeProvider> foundProviders = new ArrayList<>();
        for (OtmLibraryMember m : lib.getMembers())
            for (OtmTypeUser u : m.getDescendantsTypeUsers()) {
                OtmTypeProvider at = u.getAssignedType();
                if (at != null) {
                    if (at.getLibrary() == null)
                        log.debug( "Error: provider did not have library " + u + " assigned type " + at );
                    else {
                        if (!at.getLibrary().isBuiltIn() && !providers.contains( at )) {
                            log.debug( "Error: providers did not contain " + u + " assigned type " + at );
                            assertTrue( providers.contains( at ) );
                            if (!foundProviders.contains( at ))
                                foundProviders.add( at );
                        }
                    }
                }
            }
        // Assure all providers are used
        // FIXME - fails
        // assertTrue( foundProviders.size() == providers.size() );
        // foundProviders.forEach( p -> assertTrue( providers.contains( p ) ) );
        // providers.forEach( p -> assertTrue( foundProviders.contains( p ) ) );
    }


    /**
     * Test the basic function: one library used by two others.
     */
    @Test
    public void testGetUsersMap() {
        // Given - libraries
        OtmModelManager mgr = TestOtmModelManager.build();
        OtmLibrary providerLib = TestLibrary.buildOtm( mgr );
        OtmLibrary userLib1 =
            TestLibrary.buildOtm( mgr, providerLib.getBaseNamespace() + "/Users", "Users1", "Users1" );
        OtmLibrary userLib2 =
            TestLibrary.buildOtm( mgr, providerLib.getBaseNamespace() + "/Users", "Users2", "Users2" );
        // Given - users and providers
        TestLibrary.addOneOfEach( providerLib );
        List<OtmTypeProvider> providers = TestOtmTypeProviderInterface.buildOneOfEach( providerLib );
        List<OtmTypeUser> users = TestOtmTypeUserInterface.buildOneOfEach( userLib1, providers.get( 0 ), true );
        users.addAll( TestOtmTypeUserInterface.buildOneOfEach( userLib2, providers.get( 1 ), true ) );
        // Given - more than one provider used
        int index = 0;
        while (index < users.size()) {
            users.get( index ).setAssignedType( providers.get( index % providers.size() ) );
            index++;
        }

        // When the map is retrieved
        Map<OtmLibrary,List<OtmLibraryMember>> map = providerLib.getUsersMap();

        // Then the key set contains only the two user libs.
        Set<OtmLibrary> keys = map.keySet();
        assertTrue( keys.size() == 2 );
        assertTrue( map.keySet().contains( userLib1 ) );
        assertTrue( map.keySet().contains( userLib2 ) );

        // // Given - full set of cross-dependencies
        // TestOtmDomain_Providers.buildCrossDependendDomains( mgr );
        // List<OtmTypeProvider> providers = TestOtmDomain_Providers.providers;
        //
        // // Given - the sub-domain library and its type providing member
        // OtmLibrary lib22 = TestOtmDomain_Providers.domain22.getLibraries().get( 0 );
        //
        // assertTrue( "Given: ", !providers.isEmpty() );
        // assertTrue( "Given: ", lib22 != null );

    }

    /**
     * Test the basic function: one library used by two others.
     * 
     * @throws VersionSchemeException
     * @throws InterruptedException - thread sleeping until type resolver completes
     */
    @Test
    public void testGetProvidersMap_OTA_Repo() throws VersionSchemeException, InterruptedException {

        OtmModelManager mgr = TestOtmModelManager.build();
        TestDexFileHandler.loadVersionProject( mgr );
        List<OtmLibrary> chainLibs = mgr.getUserLibraries();
        OtmLibrary minorLibrary = TestVersionChain.getMinorInChain( mgr );
        OtmLibrary majorLibrary = minorLibrary.getVersionChain().getMajor();
        List<OtmLibraryMember> chainMembers = new ArrayList<>();
        for (OtmLibrary cl : chainLibs)
            chainMembers.addAll( cl.getMembers() );

        assertTrue( "Given", !chainLibs.isEmpty() );
        assertTrue( "Given", !chainMembers.isEmpty() );
        assertTrue( "Given", majorLibrary != null );
        assertTrue( "Given", minorLibrary != null );
        assertTrue( "Given", minorLibrary.isEditable() );
        // assertTrue( "Given - minor is empty.", mgr.getMembers( minorLibrary ).isEmpty() );

        // Initial load - version 0 and version 1, each with a minor version
        // 0.0, 0.1, 1.0, 1.1 and 1.2 have members.
        // 0.0 and 1.0 must have no entries.
        // 0.1 must have one entry for 0.0
        // 1.1 must have one entry for 1.0
        for (OtmLibrary lib : chainLibs) {
            switch (lib.getVersion()) {
                case "0.0.0":
                case "1.0.0":
                    assertTrue( "Major version must have no dependacies.", lib.getProvidersMap().isEmpty() );
                    break;
                case "0.1.0":
                case "1.1.0":
                    assertTrue( "1st minor version must have one dependacy.", lib.getProvidersMap().size() == 1 );
                    break;
                case "1.2.0":
                    Map<OtmLibrary,List<OtmLibraryMember>> providerMap = new TreeMap<>();
                    List<OtmTypeUser> usersInTargetLibrary = new ArrayList<>();
                    for (OtmLibraryMember m : lib.getMembers()) {
                        if (m instanceof OtmTypeUser)
                            usersInTargetLibrary.add( (OtmTypeUser) m );
                        usersInTargetLibrary.addAll( m.getDescendantsTypeUsers() );
                    }
                    // This 10. (Was 1 until fixed to get inherited children users.
                    log.debug( "Users in target library: " + usersInTargetLibrary.size() );

                    // For each user as used in addToMap
                    for (OtmTypeUser user : usersInTargetLibrary) {
                        OtmLibrary key = user.getAssignedType().getLibrary();
                        OtmLibraryMember member = user.getOwningMember();
                        if (!key.isBuiltIn() && key != lib)
                            log.debug( "Key = " + key + " new value = " + user.getOwningMember() );

                        if (providerMap.containsKey( key ))
                            log.debug( "Key already in map. " + key );
                        List<OtmLibraryMember> values = providerMap.get( key );
                        if (values == null)
                            values = new ArrayList<>();
                        if (!values.contains( member )) {
                            values.add( member );
                        }
                        log.debug(
                            "Adding " + key.getName() + "  " + key.getVersionChainName() + " " + key.getVersion() );
                        providerMap.put( key, values ); // replaces old values
                        log.debug( "Map size is now: " + providerMap.size() );
                        TestOtmModelMapsManager.print( providerMap );
                    }

                    assertTrue( "2nd minor version must have three dependencies.", lib.getProvidersMap().size() == 3 );
                    TestOtmModelMapsManager.print( lib );
                    break;
                default:
                    log.debug( "Unknown verison: " + lib.getVersion() );
                    TestOtmModelMapsManager.print( lib );
            }
        }
        return;

        // // Create a core object that has an element for every member of the chain.
        // OtmLibrary lib = TestLibrary.buildOtm();
        // OtmCore core = TestCore.buildOtm( lib );
        // OtmSummaryFacet facet = core.getSummary();
        // for (OtmLibraryMember cm : chainMembers) {
        // if (cm.getTL() instanceof TLPropertyType)
        // TestElement.buildOtm( facet, (OtmTypeProvider) cm );
        // }
        //
        // // Verify some member of each chain library is used in facet
        // List<String> assignments = new ArrayList<>();
        // List<OtmLibrary> pLibs = new ArrayList<>();
        // for (OtmLibrary cl : chainLibs) {
        // for (OtmLibraryMember cm : cl.getMembers()) {
        // assertTrue( "", chainMembers.contains( cm ) );
        // if (cm.getTL() instanceof TLPropertyType) {
        // assertTrue( cm.getWhereUsed().contains( core ) );
        // assignments.add( cm.getNameWithPrefix() + " is assigned to " + cm.getWhereUsed() );
        // if (!pLibs.contains( cm.getLibrary() ))
        // pLibs.add( cm.getLibrary() );
        // }
        // }
        // }
        //
        // // When the map is retrieved
        // Map<OtmLibrary,List<OtmLibraryMember>> map = lib.getProvidersMap();
        //
        // // Then the key set contains version 0 and version 1 of VersionTest.
        // Set<OtmLibrary> keys = map.keySet();
        // List<OtmLibrary> kList = new ArrayList<>( map.keySet() );
        // assertTrue( !kList.isEmpty() );
        // keys.forEach( k -> log.debug( "Map key: " + k.getNameWithPrefix() ) );
        // assertTrue( !keys.isEmpty() );
        //
        // assertTrue( kList.size() == pLibs.size() );
        //
        // // Providers Tree Controller - parent is a TreeItem
        // // map.keySet().forEach( k -> {
        // // TreeItem<LibraryAndMembersDAO> libItem = new LibraryAndMembersDAO( k ).createTreeItem( parent );
        // // map.get( k ).forEach( m -> new LibraryAndMembersDAO( m, k ).createTreeItem( libItem ) );
        // // } );
        // for (OtmLibrary k : map.keySet()) {
        // // map.keySet().forEach( k -> {
        // LibraryAndMembersDAO dao = new LibraryAndMembersDAO( k );
        // String prefix = dao.prefixProperty().get();
        // String name = dao.nameProperty().get();
        // String version = dao.versionProperty().get();
        // String toString = dao.toString();
        // log.debug( dao.prefixProperty().get() );
        // log.debug( dao.nameProperty().get() );
        // log.debug( dao.versionProperty().get() );
        // // TreeItem<LibraryAndMembersDAO> libItem = new LibraryAndMembersDAO( k ).createTreeItem( parent );
        // // map.get( k ).forEach( m -> new LibraryAndMembersDAO( m, k ).createTreeItem( libItem ) );
        // }

    }


    /** **********************************************************************/
    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getApplicationClass()
     */
    @Override
    protected Class<? extends AbstractOTMApplication> getApplicationClass() {
        return ObjectEditorApp.class;
    }

    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getBackgroundTaskNodeQuery()
     */
    @Override
    protected String getBackgroundTaskNodeQuery() {
        return "#libraryText";
    }

    /**
     * Configure headless/normal mode for TestFX execution.
     */
    static {
        TestFxMode.setHeadless( RUN_HEADLESS );
    }

}
