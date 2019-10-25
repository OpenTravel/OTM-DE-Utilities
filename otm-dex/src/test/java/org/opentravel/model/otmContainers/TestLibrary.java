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
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMemberType;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.version.VersionSchemeException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TestLibrary {
    private static Log log = LogFactory.getLog( TestLibrary.class );

    @BeforeClass
    public static void beforeClass() {}

    @Test
    public void testAddMember() {
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, null );

        OtmLibrary lib1 = TestLibrary.buildOtm( mgr, "Namespace1", "p1", "Library1" );
        log.debug( "Lib 1 name is: " + lib1.getFullName() );

        // Create business object
        OtmBusinessObject member = TestBusiness.buildOtm( mgr, "TestBusinessObject" );
        lib1.add( member );
        assertTrue( "Given", member.getLibrary() == lib1 );

        // OtmLibrary lib2 = TestLibrary.buildOtm( mgr, "Namespace2", "p2", "Library2" );
    }

    @Test
    public void testAddandDeleteAllMembers() {
        // Given - a library
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, null );
        OtmLibrary lib = TestLibrary.buildOtm( mgr, "Namespace1", "p1", "Library1" );
        int startMemberCount = mgr.getMembers().size();

        addOneOfEach( lib );
        assertTrue( "Model must have more members.", mgr.getMembers().size() > startMemberCount );
        ArrayList<OtmLibraryMember> members = new ArrayList<>( mgr.getMembers() );
        for (OtmLibraryMember member : members) {
            // log.debug( "Deleting member " + member );
            lib.delete( member );
        }

        assertTrue( "Model must have original count of members.", mgr.getMembers().size() == startMemberCount );
    }


    @Test
    public void testVersionFromNS() throws VersionSchemeException {
        List<String> namespaces = new ArrayList<>();
        namespaces.add( "http://example.com/foo/v0" );
        namespaces.add( "http://example.com/foo/v1" );
        namespaces.add( "http://example.com/foo/v1_2" );
        namespaces.add( "http://example.com/foo/v1_2_3" );
        namespaces.add( "http://example.com/foo/v1_0_0" );
        namespaces.add( "http://example.com/foo/v1_0_2" );

        OtmModelManager mgr = new OtmModelManager( null, null );
        OtmLibrary lib = TestLibrary.buildOtm( mgr );

        for (String ns : namespaces) {
            lib.getTL().setNamespace( ns );
            int major = lib.getMajorVersion();
            assertTrue( major < 2 );
            int minor = lib.getMinorVersion();
            // Can't test isMinor() because of library state
            log.debug( "Testing: " + ns + " = " + major + " " + minor );
        }
    }

    /** ****************************************************** **/

    public static OtmLibrary buildOtm(OtmModelManager mgr) {
        return mgr.add( buildTL() );
    }

    public static OtmLibrary buildOtm(OtmModelManager mgr, String namespace, String prefix, String name) {
        return mgr.add( buildTL( namespace, prefix, name ) );
    }

    public static TLLibrary buildTL(String namespace, String prefix, String name) {
        TLLibrary tlLib = new TLLibrary();
        tlLib.setName( name );
        tlLib.setPrefix( prefix );
        tlLib.setNamespace( namespace );
        return tlLib;
    }

    public static TLLibrary buildTL() {
        return new TLLibrary();
    }

    public static void addOneOfEach(OtmLibrary lib) {
        int i = 1;
        for (OtmLibraryMemberType value : OtmLibraryMemberType.values()) {
            try {
                OtmLibraryMember member =
                    OtmLibraryMemberType.buildMember( value, "TestObj" + i++, lib.getModelManager() );
                lib.add( member );
            } catch (ExceptionInInitializerError e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
