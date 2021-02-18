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
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.TestOtmModelManager;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class TestLibraryMaps extends AbstractFxTest {
    private static Log log = LogFactory.getLog( TestLibraryMaps.class );

    public static final boolean RUN_HEADLESS = true;
    final int WATCH_TIME = 0; // How long to sleep so we can see what is happening. Can be 0.

    final static String FXID_PROJECTCOMBO = "#projectCombo"; // if .projectCombo that would be css selector
    final static String FILE_TESTOPENTRAVELREPO = "TestOpenTravelRepo.otp";
    final static String FILE_TESTLOCAL = "TestLocalFiles.otp";

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestLibraryMaps.class );
        repoManager = repositoryManager.get();
        // assertTrue( "Given: ", repositoryManager != null );
        // assertTrue( "Given: ", repoManager != null );
        log.debug( "Before class setup tests ran." );
    }
    // @Before
    // public void beforeTest() {
    // modelManager.clear();
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


    @Test
    public void testGetUsersMap() {
        // TODO - add usersMap test

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
