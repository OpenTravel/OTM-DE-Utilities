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
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.TestLibraryMemberBase;

import java.util.List;

/**
 * Verifies the functions of the <code>OtmDomain</code> class.
 */
public class TestOtmDomain {
    private static Log log = LogFactory.getLog( TestOtmDomain.class );

    private static final String defaultDomain = "http://www.example.com/domains/domain1";
    private static OtmModelManager modelManager;

    private static final String BASENAMESPACE = "http://www.example.com/OtmDomain";
    private static final String BASENAME = "Domain1";
    private static final String fullName = BASENAMESPACE + "/" + BASENAME;

    @BeforeClass
    public static void beforeClass() {
        modelManager = new OtmModelManager( null, null, null );
    }


    public static OtmLibraryMember assignTypes(OtmDomain domain, List<OtmTypeProvider> types) {
        int i = 0;
        OtmLibraryMember lastMember = null;
        for (OtmLibrary lib : domain.getLibraries())
            for (OtmLibraryMember member : lib.getMembers()) {
                for (OtmTypeUser user : member.getDescendantsTypeUsers()) {
                    user.setAssignedType( (OtmTypeProvider) types.get( i++ ) );
                    if (i >= types.size())
                        i = 0;
                }
                lastMember = member;
            }
        return lastMember;
    }

    /**
     * Create domain with all object types in its libraries
     * 
     * @return
     */
    public static OtmDomain buildDomainWithMembers(String name, OtmModelManager mgr) {
        OtmDomain domain = buildOtm( name, mgr );
        domain.getLibraries().forEach( l -> TestLibraryMemberBase.buildOneOfEachWithProperties( l ) );
        return domain;
    }

    /**
     * Create a domain with 3 sub domains, one of which has its own sub domain. 5 libraries are created.
     */
    public static OtmDomain buildOtmDomainWithSubDomains(String fullName, OtmModelManager mgr) {
        buildOtm( fullName + "/sub1", modelManager );
        buildOtm( fullName + "/sub2", modelManager );
        buildOtm( fullName + "/sub2/ssub2", modelManager );
        buildOtm( fullName + "/sub3", modelManager );
        return buildOtm( fullName, modelManager );
    }

    /**
     * Create a new domain and add to model manager.
     * 
     * @param mgr
     * @return
     */
    public static OtmDomain buildOtm(OtmModelManager mgr) {
        return buildOtm( defaultDomain, modelManager );
    }

    /**
     * @param mgr
     * @param fullName baseNamespace or other full name of the domain
     * @return
     */
    public static OtmDomain buildOtm(String fullName, OtmModelManager mgr) {
        String libName = "DomainLib_" + OtmDomain.getDomainName( fullName );
        OtmLibrary lib = TestLibrary.buildOtm( mgr, fullName, "Pre", libName );
        mgr.add( lib.getTL() );
        OtmDomain domain = mgr.getDomain( fullName );
        assertTrue( domain != null );
        assertTrue( domain.getDomain().equals( fullName ) );
        check( domain );
        return domain;
    }

    public static boolean check(OtmDomain domain) {
        assertTrue( domain.getBaseNamespace() != null );
        assertTrue( domain.getDomain() != null );
        // assertTrue( domain.getIcon() != null ); // Only true if image manager has been initialized
        assertTrue( domain.getName() != null );

        assertTrue( domain.getSubDomainNames() != null );

        assertTrue( domain.getName() != null );
        return true;
    }

    /** ****************************************************** **/

    @Test
    public void testGetLibraries() {
        // Given - domain structure
        modelManager.clear();
        OtmDomain domain = buildOtmDomainWithSubDomains( fullName, modelManager );
        check( domain );

        // When - model manager provides libraries
        List<OtmLibrary> allLibs = modelManager.getUserLibraries();

        // Then - all 5 built by the builder are returned
        assertTrue( allLibs.size() == 5 );

        // Then - the domain has just the one library from the builder
        List<OtmLibrary> libs = domain.getLibraries();
        assertTrue( libs.size() == 1 );

        // Then - all domains have the 1 library from the builder
        for (OtmDomain d : modelManager.getDomains())
            assertTrue( d.getLibraries().size() == 1 );
    }

    @Test
    public void testGetSubDomains() {
        // Given - domain structure
        modelManager.clear();
        OtmDomain domain = buildOtmDomainWithSubDomains( fullName, modelManager );
        check( domain );

        List<OtmDomain> subs = domain.getSubDomains();
        assertTrue( subs.size() == 3 );
    }


    // // This is a private method - manually make it protected to run this test.
    // @Test
    // public void testFilterList() {
    // OtmDomain rootDomain = buildOtmDomainWithSubDomains( fullName, modelManager );
    // List<OtmLibrary> list = new ArrayList<>();
    // modelManager.getDomains().forEach( d -> list.addAll( d.getLibraries() ) );
    //
    // for (OtmDomain domain : modelManager.getDomains()) {
    // List<OtmLibrary> newList = domain.filterList( list );
    // if (domain == rootDomain) {
    // assertTrue( "Must filter out all sub-domains.", newList.isEmpty() );
    // } else {
    // for (OtmLibrary lib : domain.getLibraries()) {
    // assertTrue( list.contains( lib ) );
    // assertTrue( "Filter must have removed domain library.", !newList.contains( lib ) );
    // }
    // }
    // }
    // }

    @Test
    public void testSubDomainNames() {
        // Given - domain structure
        OtmDomain domain = buildOtmDomainWithSubDomains( fullName, modelManager );

        // When
        List<String> subs = domain.getSubDomainNames();
        assertTrue( !subs.isEmpty() );
        // Then
        assertTrue( "Must be same size as sub-domain list.", subs.size() == domain.getSubDomains().size() );
        subs.forEach( s -> assertTrue( "Must start with domain.", s.startsWith( domain.getBaseNamespace() ) ) );
    }

    @Test
    public void testGetDomainNameAndPath() {
        final String path1 = "http://www.example.com/expected/path/to/domain";
        final String path2 = "http://www.example.com/expected/path/to/domain/";
        final String path3 = "/expected/path/to/domain";
        final String path4 = "somesillypath";
        final String path = "http://www.example.com/expected/path/to";
        final String dn = "http://www.example.com/expected";

        assertTrue( OtmDomain.getDomainName( path1 ).equals( "domain" ) );
        assertTrue( OtmDomain.getDomainPath( path1 ).equals( path ) );
        assertTrue( OtmDomain.getDomainSubPath( path1, dn ).equals( "path/to/domain" ) );

        assertTrue( OtmDomain.getDomainName( path2 ).equals( "" ) );
        assertTrue( OtmDomain.getDomainPath( path2 ).equals( path + "/domain" ) );
        assertTrue( OtmDomain.getDomainSubPath( path2, dn ).equals( "path/to/domain/" ) );

        assertTrue( OtmDomain.getDomainName( path3 ).equals( "domain" ) );
        assertTrue( OtmDomain.getDomainPath( path3 ).equals( "/expected/path/to" ) );
        assertTrue( OtmDomain.getDomainSubPath( path3, dn ).equals( "" ) );

        assertTrue( OtmDomain.getDomainName( path4 ).equals( "somesillypath" ) );
        assertTrue( OtmDomain.getDomainPath( path4 ).equals( "" ) );
        assertTrue( OtmDomain.getDomainSubPath( path4, dn ).equals( "" ) );
    }

    @Test
    public void testNames() {
        OtmDomain domain = null;

        // When - expected name
        domain = buildOtm( fullName, modelManager );
        check( domain );
        // Then
        assertTrue( domain.getDomain().equals( fullName ) );
        assertTrue( domain.getBaseNamespace().equals( fullName ) );
        assertTrue( domain.getName().equals( BASENAME ) );

        // When - name is not a URI/baseNamespace
        domain = buildOtm( "fooBar", modelManager );
        check( domain );
    }

    @Test
    public void testConstructor() {
        OtmDomain domain = null;

        // When - simple constructor
        domain = new OtmDomain( BASENAMESPACE, modelManager );
        assertTrue( domain != null );
        check( domain );

        // When - force error
        Exception ex = null;
        try {
            domain = new OtmDomain( null, modelManager );
        } catch (IllegalArgumentException e) {
            assertTrue( e != null );
            ex = e;
        }
        assertTrue( "Must have thrown exception.", ex != null );

        // When - force error
        ex = null;
        try {
            domain = new OtmDomain( "", modelManager );
        } catch (IllegalArgumentException e) {
            assertTrue( e != null );
            ex = e;
        }
        assertTrue( "Must have thrown exception.", ex != null );

        // When - force error
        ex = null;
        try {
            domain = new OtmDomain( "http://www.example.com/OtmDomain", null );
        } catch (IllegalArgumentException e) {
            assertTrue( e != null );
            ex = e;
        }
        assertTrue( "Must have thrown exception.", ex != null );
    }


}
