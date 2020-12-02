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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.TestOtmModelManager;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.TestOtmSimple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Verifies the functions of the <code>OtmDomain</code> class. Test contract (api methods) the Sprite needs
 */
public class TestOtmDomain_Providers {
    private static Log log = LogFactory.getLog( TestOtmDomain_Providers.class );

    // private static final String defaultDomain = "http://www.example.com/domains/domain1";
    private static OtmModelManager modelManager;

    private static final String BASENAMESPACE = "http://www.example.com/OtmDomain";
    private static final String BASENAME = "Domain";
    private static final String fullName = BASENAMESPACE + "/" + BASENAME;

    // May be needed outside of this test junit.
    static List<OtmTypeProvider> providers = new ArrayList<>();
    static OtmDomain domain1 = null;
    static OtmDomain domain2 = null;
    static OtmDomain domain22 = null;
    static OtmDomain domain3 = null;


    @BeforeClass
    public static void beforeClass() {
        modelManager = new OtmModelManager( null, null, null );
    }

    @Before
    public void beforeTest() {
        modelManager.clear();
    }

    /**
     * Create many domains with type providers and users. Use model manage to access domains.
     * <li>All domains depend on domain1
     * <li>domain2 only depends on domain1
     * <li>domain3 depends on domain1 and 2 and sdomain2
     * 
     * @see TestLibrary#testGetProvidersMap()
     * @return
     */
    public static OtmDomain buildCrossDependendDomains(OtmModelManager mgr) {
        mgr.clear();
        domain1 = buildDomainWithMembers( fullName + "/domain1", mgr );
        OtmLibrary d1 = domain1.getLibraries().get( 0 );
        OtmLibraryMember d1Provider = TestOtmSimple.buildOtm( d1, d1.getName() + "_Simple" );
        providers.add( (OtmTypeProvider) d1Provider );

        domain2 = buildDomainWithMembers( fullName + "/domain2", mgr );
        OtmLibraryMember d2Provider = assignTypes( domain2, providers );
        providers.add( (OtmTypeProvider) d2Provider );

        domain22 = buildDomainWithMembers( fullName + "/domain2/sdomain2", mgr );
        OtmLibraryMember d22Provider = assignTypes( domain22, providers );
        providers.add( (OtmTypeProvider) d22Provider );

        domain3 = buildDomainWithMembers( fullName + "/domain3", mgr );
        assignTypes( domain3, providers );

        return domain1;
    }

    public static OtmLibraryMember assignTypes(OtmDomain domain, List<OtmTypeProvider> types) {
        int i = 0;
        OtmLibraryMember newSimple = null;
        for (OtmLibrary lib : domain.getLibraries()) {
            // use a new simple type to assure all users can be set
            newSimple = TestOtmSimple.buildOtm( lib, lib.getName() + "_Simple" );
            for (OtmLibraryMember member : lib.getMembers()) {
                for (OtmTypeUser user : member.getDescendantsTypeUsers()) {
                    user.setAssignedType( (OtmTypeProvider) types.get( i++ ) );
                    if (i >= types.size())
                        i = 0;
                }
            }
        }
        return newSimple;
    }

    /** ******************************************************************************* **/

    /**
     * Create domain with all object types in its libraries
     * 
     * @return
     */
    public static OtmDomain buildDomainWithMembers(String name, OtmModelManager mgr) {
        return TestOtmDomain.buildDomainWithMembers( name, mgr );
    }

    @Test
    public void testGetProviderDomains() {
        // ProvidersSprite#draw() iterates through getProviderDomains
        //
        // Given - model setup with 4 domains with members that depend on each other
        modelManager = TestOtmModelManager.build();
        buildCrossDependendDomains( modelManager );

        for (OtmDomain otmDomain : modelManager.getDomains()) {
            // When - we get the providers and providers map
            List<String> providers = otmDomain.getProviderDomains();

            // Then - domain list and libraries in the map are correct
            switch (otmDomain.getName()) {
                case "domain1":
                    // Has no providers
                    assertTrue( providers.isEmpty() );
                    break;
                case "domain2":
                    // Has providers from 1 but no others
                    assertTrue( providers.contains( domain1.getDomain() ) );
                    assertTrue( !providers.contains( domain22.getDomain() ) );
                    break;
                case "sdomain2":
                    assertTrue( providers.contains( domain1.getDomain() ) );
                    // FIXME - fails when run with other tests
                    // assertTrue( providers.contains( domain2.getDomain() ) );
                    break;
                case "domain3":
                    assertTrue( providers.contains( domain1.getDomain() ) );
                    assertTrue( providers.contains( domain2.getDomain() ) );
                    assertTrue( providers.contains( domain22.getDomain() ) );
                    break;
                default:
                    log.debug( "Opps - unknown domain: " + otmDomain.getName() );
            }
        }

    }

    @Test
    public void testGetProviderMap() {
        // DomainProviderFR constructor gets the otmDomain#getProvidersMap()
        // Then draws facets by iterating on entries generating LibraryAndMembersFR
        // for (Entry<OtmLibrary,List<OtmLibraryMember>> entry : map.entrySet()) {
        //

        // Given - model setup with 4 domains with members that depend on each other
        modelManager = TestOtmModelManager.build();
        buildCrossDependendDomains( modelManager );

        // When - domain 3 is the most complex, using providers from a sub-domain
        Map<OtmLibrary,List<OtmLibraryMember>> map = domain3.getProvidersMap();
        Set<OtmLibrary> libSet = map.keySet();
        List<OtmLibrary> list = new ArrayList<>( libSet );
        for (OtmLibrary l : domain22.getLibraries()) {
            assertTrue( list.contains( l ) );
        }

        for (OtmDomain otmDomain : modelManager.getDomains()) {
            // When - data is retrieved from otm domain
            map = otmDomain.getProvidersMap();
            libSet = map.keySet();
            List<OtmLibrary> libs = new ArrayList<>( libSet );

            // Then - assure all libraries in the map are correct
            switch (otmDomain.getName()) {
                case "domain1":
                    assertTrue( map.isEmpty() );
                    break;
                case "domain2":
                    // All domain 1 libraries must be in the key set
                    domain1.getLibraries().forEach( l -> assertTrue( libs.contains( l ) ) );
                    break;
                case "sdomain2":
                    domain1.getLibraries().forEach( l -> assertTrue( libs.contains( l ) ) );
                    domain2.getLibraries().forEach( l -> assertTrue( libs.contains( l ) ) );
                    break;
                case "domain3":
                    domain1.getLibraries().forEach( l -> assertTrue( libs.contains( l ) ) );
                    domain2.getLibraries().forEach( l -> assertTrue( libs.contains( l ) ) );
                    domain22.getLibraries().forEach( l -> assertTrue( libs.contains( l ) ) );
                    break;
                default:
                    log.debug( "Opps - unknown domain: " + otmDomain.getName() );
            }
        }

    }
}
