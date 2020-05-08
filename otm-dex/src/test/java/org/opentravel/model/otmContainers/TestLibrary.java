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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.dex.action.manager.DexReadOnlyActionManager;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMemberType;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.OtmSimpleObject;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestCore;
import org.opentravel.model.otmLibraryMembers.TestLibraryMemberBase;
import org.opentravel.model.otmLibraryMembers.TestOtmSimple;
import org.opentravel.model.otmLibraryMembers.TestResource;
import org.opentravel.model.otmProperties.OtmElement;
import org.opentravel.model.otmProperties.TestElement;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.version.VersionSchemeException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;

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
        // log.debug( "Lib 1 name is: " + lib1.getFullName() );

        // Create business object
        OtmBusinessObject member = TestBusiness.buildOtm( mgr, "TestBusinessObject" );
        lib1.add( member );
        assertTrue( "Given", member.getLibrary() == lib1 );

        // OtmLibrary lib2 = TestLibrary.buildOtm( mgr, "Namespace2", "p2", "Library2" );
    }

    // TODO - test facet set/un-set as assigned type
    @Test
    public void testWhereUsedWhenDeleted() {
        // Given - a library
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, null, null );
        OtmLibrary lib = TestLibrary.buildOtm( mgr, "Namespace1", "p1", "Library1" );
        addOneOfEach( lib );

        // Given an element to set and un-set assigned type
        OtmCore core = TestCore.buildOtm( mgr, "TestCore" );
        OtmElement<?> element = TestElement.buildOtm( core.getSummary() );

        ArrayList<OtmLibraryMember> members = new ArrayList<>( mgr.getMembers() );
        OtmTypeProvider a;
        for (OtmLibraryMember member : members) {
            assertTrue( member.getWhereUsed().isEmpty() );
            if (member instanceof OtmTypeProvider) {

                // When assigned as type
                a = element.setAssignedType( (OtmTypeProvider) member );
                // Not all members can be assigned
                if (a == member)
                    assertTrue( member.getWhereUsed().contains( core ) );

            }
            lib.delete( member );
            // This is OK. Needed for un-delete
            if (!member.getWhereUsed().isEmpty())
                log.debug( "Where used lists deleted member " + member );
        }
    }

    @Test
    public void testAddAndDelete() throws ExceptionInInitializerError, InstantiationException, IllegalAccessException,
        NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        OtmModelManager mgr = new OtmModelManager( null, null, null );
        OtmLibrary lib = mgr.add( new TLLibrary() );

        for (OtmLibraryMemberType type : OtmLibraryMemberType.values()) {
            OtmLibraryMember member = OtmLibraryMemberType.buildMember( type, "Test" + type.toString(), mgr );

            // When added
            lib.add( member );
            // Then - add works
            if (type != OtmLibraryMemberType.EXTENSIONPOINTFACET) {
                // FIXME - why not extension points?
                assertTrue( lib.contains( member ) );
                assertTrue( member.getLibrary() == lib );
            }

            assertTrue( mgr.contains( member.getTlLM() ) );
            assertTrue( mgr.getMembers().contains( member ) );
            assertTrue( lib.getTL().getNamedMembers().contains( member.getTL() ) );

            // When deleted
            lib.delete( member );
            assertFalse( mgr.contains( member.getTlLM() ) );
            assertFalse( mgr.getMembers().contains( member ) );
            assertFalse( lib.getTL().getNamedMembers().contains( member.getTL() ) );

            log.debug( "Added and removed: " + member );
        }
    }

    @Test
    public void testResourceTestBuilders() {
        // Given - action and model managers
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, null, null );
        // Given - a subject library that uses types, base types, contextual facets
        OtmLibrary lib = TestLibrary.buildOtm( mgr, "NamespaceS1", "s1", "LibraryS1" );
        String pathString = "http://example.com/test";
        String subjectName = "TestResource";

        OtmResource r = null;
        // todo r = TestResource.buildBaseOtm( resource, mgr );

        r = TestResource.buildOtm( mgr );
        OtmLibraryMember result = lib.add( r );
        assertTrue( result == r );
        assertTrue( lib.contains( r ) );
        assertTrue( r.getLibrary() == lib );
        lib.delete( r );

        r = TestResource.buildFullOtm( pathString, subjectName + "b", mgr );
        result = lib.add( r );
        assertTrue( result == r );
        assertTrue( lib.contains( r ) );
        assertTrue( r.getLibrary() == lib );
        lib.delete( r );

        r = TestResource.buildFullOtm( pathString, subjectName + "c", lib, mgr );
        assertTrue( lib.contains( r ) );
        assertTrue( r.getLibrary() == lib );

    }

    @Test
    public void testGetProviders() {
        // Given - action and model managers
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, null, null );
        // Given - a subject library that uses types, base types, contextual facets
        OtmLibrary subjectLib = TestLibrary.buildOtm( mgr, "NamespaceS1", "s1", "LibraryS1" );
        addOneOfEach( subjectLib );
        TestLibraryMemberBase.buildOneOfEachWithProperties( mgr, subjectLib );

        // Given - a provider library that provides types, base types, contextual facets
        OtmLibrary providerLib = TestLibrary.buildOtm( mgr, "NamespaceP1", "p1", "LibraryP1" );
        OtmLibrary providerLib2 = TestLibrary.buildOtm( mgr, "NamespaceP2", "p2", "LibraryP2" );
        OtmSimpleObject[] simples = new OtmSimpleObject[3];
        simples[0] = TestOtmSimple.buildOtm( providerLib );
        simples[0].setName( "Simple0" );
        simples[1] = TestOtmSimple.buildOtm( providerLib2 );
        simples[1].setName( "Simple1" );
        simples[2] = TestOtmSimple.buildOtm( providerLib2 );
        simples[2].setName( "Simple2" );

        // Given - make assignments
        int i = 0;
        for (OtmLibraryMember m : subjectLib.getMembers())
            for (OtmTypeUser u : m.getDescendantsTypeUsers()) {
                OtmTypeProvider r = u.setAssignedType( simples[i++ % 3] );
                log.debug( "Assigned " + simples[(i - 1) % 3] + " to " + u + " resulting in " + r );
            }

        // When - getProviders
        Map<OtmLibrary,List<OtmLibraryMember>> map = subjectLib.getProviderMap( false );
        Set<OtmLibrary> keys = map.keySet();
        Collection<List<OtmLibraryMember>> values = map.values();
        // Then
        assertTrue( !keys.isEmpty() );
        assertTrue( !values.isEmpty() );
        assertTrue( map.keySet().contains( providerLib ) );
        assertTrue( map.keySet().contains( providerLib2 ) );
        // assertTrue( map.get( providerLib2 ).contains( simples[1] ) );
        // assertTrue( map.get( providerLib2 ).contains( simples[2] ) );

        // When - sorted
        map = subjectLib.getProviderMap( true );
        keys = map.keySet();
        values = map.values();
        // Then
        assertTrue( !keys.isEmpty() );
        assertTrue( !values.isEmpty() );
    }
    // TODO - add usersMap test

    @Test
    public void testGetActionManager() {

    }

    @Test
    public void testRefresh() {
        // assure resource gets refreshed and when done they name property is null
        DexActionManager actionMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( actionMgr, null, null );
        OtmLibrary lib = TestLibrary.buildOtm( mgr );
        addOneOfEach( lib );
        assertTrue( "Given", lib.isEditable() );

        // Action manager is not used...library status is for isEditable()
        for (OtmLibraryMember m : mgr.getMembers( lib )) {
            assertTrue( "Given", m.isEditable() );
            assertTrue( "Given", m.nameProperty() instanceof SimpleStringProperty );
            // log.debug( "Member: " + m.getName() + " type = " + m.getObjectTypeName() );
        }

        // When - library is forced to be not editable
        ((TLLibrary) lib.getTL()).setStatus( TLLibraryStatus.FINAL );
        assertTrue( "Must be read only action manager.", lib.getActionManager() instanceof DexReadOnlyActionManager );
        lib.refresh();
        // DexActionManager ma = lib.getActionManager(); // Will be full because unmanaged
        assertTrue( "Must be read only action manager.", lib.getActionManager() instanceof DexReadOnlyActionManager );
        // Then
        for (OtmLibraryMember m : mgr.getMembers( lib )) {
            log.debug( "Read-only Member: " + m.getName() + " type = " + m.getObjectTypeName() );
            assertTrue( "Given", !m.isEditable() );
            // ma = m.getActionManager(); // Will be full because unmanaged
            assertTrue( "Given", m.getActionManager() instanceof DexReadOnlyActionManager );
            assertTrue( "Refresh must change property class.", m.nameProperty() instanceof ReadOnlyStringWrapper );
        }

        // When - library is forced to be editable
        ((TLLibrary) lib.getTL()).setStatus( TLLibraryStatus.DRAFT );
        assertTrue( "Must be full action manager.", lib.getActionManager() instanceof DexFullActionManager );
        lib.refresh();
        // Then
        for (OtmLibraryMember m : mgr.getMembers( lib )) {
            assertTrue( "Given", m.isEditable() );
            assertFalse( "Refresh must change property class.", m.nameProperty() instanceof ReadOnlyStringWrapper );
            log.debug( "Editable Member: " + m.getName() + " type = " + m.getObjectTypeName() );
        }
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

        OtmModelManager mgr = new OtmModelManager( null, null, null );
        OtmLibrary lib = TestLibrary.buildOtm( mgr );

        for (String ns : namespaces) {
            lib.getTL().setNamespace( ns );
            int major = lib.getMajorVersion();
            assertTrue( major < 2 );
            int minor = lib.getMinorVersion();
            // Can't test isMinor() because of library state
            // log.debug( "Testing: " + ns + " = " + major + " " + minor );
        }
    }

    /** ****************************************************** **/

    /**
     * Create new ModelManager and Full Action Manager. Then create a TL_library and OtmLibrary named "LibraryName" in
     * example.com namespace.
     * <p>
     * Assure library is: editable, DRAFT, MANAGED_WIP or UNMANAGED, and manager can find it
     * 
     * @param mgr
     * @return
     */
    public static OtmLibrary buildOtm() {
        OtmLibrary lib = TestLibrary.buildOtm( new OtmModelManager( new DexFullActionManager( null ), null, null ) );
        return lib;
    }

    /**
     * Create a TL_library and OtmLibrary named "LibraryName" in example.com namespace
     * <p>
     * Assure library is: editable, DRAFT, MANAGED_WIP or UNMANAGED, and manager can find it
     * 
     * @param mgr
     * @return
     */
    public static OtmLibrary buildOtm(OtmModelManager mgr) {
        TLLibrary tlLib = buildTL();
        tlLib.setName( "LibraryName" );
        tlLib.setPrefix( "pre" );
        tlLib.setNamespace( "http://example.com/ns/v0" );
        OtmLibrary lib = mgr.add( tlLib );
        assertTrue( "Given", lib.isEditable() );
        assertTrue( "Given - model manager must be able to find the library.", mgr.get( lib.getTL() ) == lib );
        assertTrue( "Given", lib.getStatus() == TLLibraryStatus.DRAFT );
        assertTrue( "Given",
            lib.getState() == RepositoryItemState.MANAGED_WIP || lib.getState() == RepositoryItemState.UNMANAGED );
        return lib;
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
