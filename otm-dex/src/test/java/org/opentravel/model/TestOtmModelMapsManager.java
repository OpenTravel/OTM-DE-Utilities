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
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmContainers.TestLibraryMaps;
import org.opentravel.model.otmFacets.OtmAlias;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmChoiceObject;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.OtmSimpleObject;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestChoice;
import org.opentravel.model.otmLibraryMembers.TestCore;
import org.opentravel.model.otmLibraryMembers.TestOtmSimple;
import org.opentravel.model.otmLibraryMembers.TestResource;
import org.opentravel.model.otmLibraryMembers.TestValueWithAttributes;
import org.opentravel.model.otmProperties.OtmElement;
import org.opentravel.model.otmProperties.TestElement;
import org.opentravel.model.otmProperties.TestOtmPropertiesBase;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Tests and utilities for testing maps of providers and users. {@link TestLibraryMaps} for tests in versioned
 * libraries.
 * <p>
 */
public class TestOtmModelMapsManager {
    private static Logger log = LogManager.getLogger( TestOtmModelMapsManager.class );

    // protected static OtmModelManager staticModelManager = null;

    @BeforeClass
    public static void beforeClass() {
        // staticModelManager = new OtmModelManager( null, null, null );
        // log.debug( "Before class." );
    }

    @Test
    public void testGetProvidersMap() {
        OtmModelManager mgr = new OtmModelManager( null, null, null );
        OtmModelMapsManager mapMgr = mgr.getMapManager();

        Map<OtmLibrary,List<OtmLibraryMember>> tMap = new HashMap<>();

        OtmLibrary userLib = buildTestProvidersMap( mgr, tMap );
        assertTrue( "Given: ", userLib.getMembers().size() > 0 );
        assertTrue( "Given: ", !tMap.isEmpty() );

        // When
        // Map<OtmLibrary,List<OtmLibraryMember>> pMap = mapMgr.getUsersMap( userLib, false );
        Map<OtmLibrary,List<OtmLibraryMember>> pMap = userLib.getProvidersMap();

        // Then - compare works
        compareMaps( mgr, tMap, pMap );

        // FIXME - aliases, contextual facets, resource.actionFacets
    }

    @Test
    public void testGetUsersMap() {
        OtmModelManager mgr = new OtmModelManager( null, null, null );
        OtmModelMapsManager mapMgr = mgr.getMapManager();

        Map<OtmLibrary,List<OtmLibraryMember>> tMap = new HashMap<>();

        OtmLibrary userLib = buildTestUserMap( mgr, tMap );
        assertTrue( "Given: ", userLib.getMembers().size() > 0 );
        assertTrue( "Given: ", !tMap.isEmpty() );

        // When - get the map from model manager's map manager
        Map<OtmLibrary,List<OtmLibraryMember>> uMap = mapMgr.getUsersMap( userLib, false );
        // Then
        compareMaps( mgr, tMap, uMap );

    }

    /**
     * Assure addToUsersMap finds members that have properties that use types from the library.
     */
    @Test
    public void testAddToUsersMap() {
        OtmModelManager mgr = new OtmModelManager( null, null, null );
        OtmModelMapsManager mapMgr = mgr.getMapManager();

        // Map is a new map
        Map<OtmLibrary,List<OtmLibraryMember>> usersMap = new HashMap<>();

        // Library to test containing the provider
        OtmLibrary lib = TestLibrary.buildOtm( mgr );
        OtmCore coreMember = TestCore.buildOtm( lib, "CoreMember" );

        // Library and its users
        OtmLibrary userLib = TestLibrary.buildOtm( mgr );
        OtmBusinessObject bo = TestBusiness.buildOtm( userLib, "UserBO" );
        TestElement.buildOtm( bo.getSummary(), coreMember );
        OtmChoiceObject ch = TestChoice.buildOtm( userLib, "UserChoice" );
        TestElement.buildOtm( ch.getShared(), coreMember );

        // Library2 and its users
        OtmLibrary userLib2 = TestLibrary.buildOtm( mgr );
        OtmValueWithAttributes vwa = TestValueWithAttributes.buildOtm( userLib2, "UserVwa" );
        TestOtmPropertiesBase.buildAttribute( vwa, coreMember );
        // Assign a descendant of coreMember
        OtmCore core = TestCore.buildOtm( userLib2, "UserCore" );
        TestElement.buildOtm( core.getSummary(), coreMember.getDetail() );

        // Where used list from a member of the library
        List<OtmLibraryMember> users = coreMember.getWhereUsed();
        assertTrue( "Given: check to assure where used contains users.",
            users.contains( core ) && users.contains( bo ) && users.contains( ch ) && users.contains( vwa ) );

        // When
        mapMgr.addToUsersMap( usersMap, lib, coreMember );

        assertTrue( "Then: map has two entries.", usersMap.size() == 2 );

        for (Entry<OtmLibrary,List<OtmLibraryMember>> entry : usersMap.entrySet()) {
            OtmLibrary key = entry.getKey();
            List<OtmLibraryMember> values = entry.getValue();
            log.debug( "Entry for " + key + " = " + values );
            if (key == userLib)
                assertTrue( "Then: ", values.contains( bo ) && values.contains( ch ) );
            else if (key == userLib2)
                assertTrue( "Then: ", values.contains( core ) && values.contains( vwa ) );
            else
                assertTrue( "Then: error.", false );
        }
        // TODO - finish test
    }

    public void compareMaps(OtmModelManager mgr, Map<OtmLibrary,List<OtmLibraryMember>> map1,
        Map<OtmLibrary,List<OtmLibraryMember>> map2) {
        print( map1 );
        print( map2 );
        assertTrue( map1.size() == map2.size() );

        for (OtmLibrary l : mgr.getUserLibraries()) {
            // Library may not be in the map, if it is, test it.
            if (!map1.keySet().contains( l )) {
                assertTrue( !map2.keySet().contains( l ) );
            } else {
                assertTrue( map1.keySet().contains( l ) );
                assertTrue( map2.keySet().contains( l ) );

                List<OtmLibraryMember> users1 = map1.get( l );
                List<OtmLibraryMember> users2 = map2.get( l );
                assertTrue( users1.size() == users2.size() );

                for (OtmLibraryMember m : users1)
                    assertTrue( users2.contains( m ) );
                for (OtmLibraryMember m : users2)
                    assertTrue( users1.contains( m ) );
            }
        }
    }

    /**
     * Populate the passed map with two entries. Each entry has a library and a list of custom built type users that use
     * types from a different library.
     * 
     * @param mgr
     * @param map
     * @return the library containing the type providers
     */
    public static OtmLibrary buildTestUserMap(OtmModelManager mgr, Map<OtmLibrary,List<OtmLibraryMember>> map) {
        String namespace = "http://example.com/ns";
        String prefix = "ns";
        String name = "Library";
        int i = 1;

        // Build 3 libraries (not valid namespaces!)
        OtmLibrary targetLib = TestLibrary.buildOtm( mgr, namespace + i, prefix + i, name + i++ );
        OtmLibrary lib2 = TestLibrary.buildOtm( mgr, namespace + i, prefix + i, name + i++ );
        OtmLibrary lib3 = TestLibrary.buildOtm( mgr, namespace + i, prefix + i, name + i++ );

        // Lists containing built users in that library
        List<OtmLibraryMember> users2 = new ArrayList<>();
        List<OtmLibraryMember> users3 = new ArrayList<>();

        // Put types in target library that are used in library 2 and 3
        OtmSimpleObject simple1 = TestOtmSimple.buildOtm( targetLib, "Simple1" );
        OtmCore core1T = TestCore.buildOtm( targetLib, "Core1InLib1" );
        OtmCore core2T = TestCore.buildOtm( targetLib, "Core2InLib1" );
        OtmBusinessObject bo1T = TestBusiness.buildOtm( targetLib, "BO1_In_Lib1" );

        // Use the simple type which is a library member
        OtmCore core2 = TestCore.buildOtm( lib2, "CoreInLib2" );
        core2.setAssignedType( simple1 );
        users2.add( core2 );
        OtmCore core3 = TestCore.buildOtm( lib3, "CoreInLib3" );
        core3.setAssignedType( simple1 );
        users3.add( core3 );

        // Use a child property as assigned type
        OtmCore core2a = TestCore.buildOtm( lib2, "CoreInLib2withAssignedType" );
        TestElement.buildOtm( core2a.getSummary(), core1T.getDetail() );
        users2.add( core2a );

        OtmCore core3a = TestCore.buildOtm( lib3, "CoreInLib3withAssignedType" );
        TestElement.buildOtm( core3a.getSummary(), core1T.getDetail() );
        users3.add( core3a );

        // Use a child as base type
        OtmCore core4 = TestCore.buildOtm( lib2, "CoreInLib3withBaseType" );
        core4.setBaseType( core2T );
        users2.add( core4 );

        // Use bo in resource
        OtmResource r = TestResource.buildFullValidOtm( "/path", "foo", lib2, mgr );
        r.setSubject( bo1T );
        users2.add( r );

        map.put( lib2, users2 );
        map.put( lib3, users3 );

        // Verify whereUsed
        assertTrue( "Builder check: ", simple1.getWhereUsed().contains( core2 ) );
        assertTrue( "Builder check: ", simple1.getWhereUsed().contains( core3 ) );
        assertTrue( "Builder check: ", core1T.getWhereUsed().contains( core2a ) );
        assertTrue( "Builder check: ", core1T.getWhereUsed().contains( core3a ) );
        assertTrue( "Builder check: ", core2T.getWhereUsed().contains( core4 ) );
        assertTrue( "Builder check: ", bo1T.getWhereUsed().contains( r ) );

        return targetLib;
    }

    /**
     * Build a provider map.
     * <p>
     * The keys are each provider library -- libraries containing types assigned to type-users in this library.
     * <p>
     * The values are an array of this library's members that use the provided the types. Each value is a member that
     * uses types from the provider library.
     * 
     * @return library that provides types to libraries in the map.
     */
    public static OtmLibrary buildTestProvidersMap(OtmModelManager mgr, Map<OtmLibrary,List<OtmLibraryMember>> map) {
        String namespace = "http://example.com/ns";
        String prefix = "ns";
        String name = "Library";
        int i = 1;


        // Create the 3 libraries
        OtmLibrary targetLib = TestLibrary.buildOtm( mgr, namespace + i, prefix + i, name + i++ );
        OtmLibrary lib2 = TestLibrary.buildOtm( mgr, namespace + i, prefix + i, name + i++ );
        List<OtmLibraryMember> providers2 = new ArrayList<>();
        OtmLibrary lib3 = TestLibrary.buildOtm( mgr, namespace + i, prefix + i, name + i++ );
        List<OtmLibraryMember> providers3 = new ArrayList<>();

        //
        // Put types in target library that are used in library 2 and 3
        OtmSimpleObject simple1T = TestOtmSimple.buildOtm( targetLib, "Simple1InTargetLib" );
        OtmCore core1T = TestCore.buildOtm( targetLib, "Core1InTargetLib" );
        OtmCore core2T = TestCore.buildOtm( targetLib, "Core2InTargetLib" );
        OtmCore core3T = TestCore.buildOtm( targetLib, "Core3InTargetLib" );
        OtmCore core4T = TestCore.buildOtm( targetLib, "Core4InTargetLib" );
        OtmCore core5T = TestCore.buildOtm( targetLib, "Core5InTargetLib" );
        OtmCore core6T = TestCore.buildOtm( targetLib, "Core6InTargetLib" );

        // /XMLSchema 1.0=[ns1:Core1InTargetLib, ns1:Core2InTargetLib]
        // 12/2/2020 - changed map to exclude built-in libraries
        // List<OtmLibraryMember> list = new ArrayList<OtmLibraryMember>();
        // list.add( core1T );
        // list.add( core2T );
        // list.add( core3T );
        // list.add( core4T );
        // list.add( core5T );
        // list.add( core6T );
        // for (OtmLibrary l : mgr.getLibraries())
        // if (l instanceof OtmBuiltInLibrary && l.getName().equals( "XMLSchema" ))
        // map.put( l, list );

        //
        // Add objects in lib2 and lib3 that provide types to objects in target lib
        //
        // Simple which is a library member
        OtmSimpleObject simple2 = TestOtmSimple.buildOtm( lib2, "Simple2" );
        simple1T.setAssignedType( simple2 );
        providers2.add( simple1T );

        // Use the Core type
        OtmCore core2 = TestCore.buildOtm( lib2, "CoreInLib2" );
        OtmCore core3 = TestCore.buildOtm( lib3, "CoreInLib3" );
        core1T.setAssignedType( core2 );
        providers2.add( core1T );
        core2T.setAssignedType( core3 );
        providers3.add( core2T );

        // Use a child property as assigned type
        OtmChoiceObject choice4 = TestChoice.buildOtm( lib2, "Choice4InLib2WithAssignedType" );
        OtmElement<TLProperty> e2 = TestOtmPropertiesBase.buildElement( core4T.getSummary() );
        OtmTypeProvider ret = e2.setAssignedType( choice4.getShared() );
        providers2.add( core4T );

        // Use a child as base type
        OtmCore core5 = TestCore.buildOtm( lib2, "Core5InLib2withBaseType" );
        core5T.setBaseType( core5 );
        providers2.add( core5T );

        // Use an alias as the type
        OtmChoiceObject choice6 = TestChoice.buildOtm( lib2, "Choice6InLib2WithAssignedType" );
        TLAlias tlAlias = new TLAlias();
        tlAlias.setName( "Alias6" );
        tlAlias.setOwningEntity( choice6.getTL() );
        OtmAlias alias6 = new OtmAlias( tlAlias, choice6 );
        OtmElement<TLProperty> e6 = TestOtmPropertiesBase.buildElement( core6T.getSummary() );
        e6.setAssignedType( alias6 );
        providers2.add( core6T );
        assertTrue( alias6.getOwningMember() == choice6 );
        assertTrue( e6.getAssignedType() == alias6 );


        // Use bo in resource
        OtmBusinessObject bo1 = TestBusiness.buildOtm( lib2, "BO1_In_Lib1" );
        OtmResource rT = TestResource.buildFullValidOtm( "/path", "foo", targetLib, mgr );
        rT.setSubject( bo1 );
        providers2.add( rT );

        //
        map.put( lib2, providers2 );
        map.put( lib3, providers3 );

        return targetLib;
    }

    public static void print(OtmLibrary lib) {
        log.debug( lib + " Provider Map contains" );
        print( lib.getProvidersMap() );
    }

    public static void print(Map<OtmLibrary,List<OtmLibraryMember>> map) {
        for (OtmLibrary l : map.keySet()) {
            log.debug( "\t" + l.getNameWithPrefix() + " provides types to " + map.get( l ) );
        }
    }
}
