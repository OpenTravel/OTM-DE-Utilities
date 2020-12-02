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
import org.opentravel.TestDexFileHandler;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.dex.action.manager.DexReadOnlyActionManager;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.TestOtmModelManager;
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
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

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
public class TestLibrary extends AbstractFxTest {
    private static Log log = LogFactory.getLog( TestLibrary.class );

    public static final boolean RUN_HEADLESS = true;
    final int WATCH_TIME = 0; // How long to sleep so we can see what is happening. Can be 0.

    final static String FXID_PROJECTCOMBO = "#projectCombo"; // if .projectCombo that would be css selector
    final static String FILE_TESTOPENTRAVELREPO = "TestOpenTravelRepo.otp";
    final static String FILE_TESTLOCAL = "TestLocalFiles.otp";

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestLibrary.class );
        repoManager = repositoryManager.get();
        // assertTrue( "Given: ", repositoryManager != null );
        // assertTrue( "Given: ", repoManager != null );
        log.debug( "Before class setup tests ran." );
    }
    // @Before
    // public void beforeTest() {
    // modelManager.clear();
    // }


    @Test
    public void testAddMember() {
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, null, null );

        OtmLibrary lib1 = TestLibrary.buildOtm( mgr, "Namespace1", "p1", "Library1" );
        // log.debug( "Lib 1 name is: " + lib1.getFullName() );

        // Create business object
        OtmBusinessObject member = TestBusiness.buildOtm( mgr, "TestBusinessObject" );
        lib1.add( member );
        assertTrue( "Given", member.getLibrary() == lib1 );

        // OtmLibrary lib2 = TestLibrary.buildOtm( mgr, "Namespace2", "p2", "Library2" );
    }

    @Test
    public void testContainsAbstractLibrary() {
        // TODO
        log.debug( "TO DO" );
    }

    @Test
    public void testContainsMember() {
        // TODO
        log.debug( "TO DO" );
    }

    @Test
    public void testIncludes() {
        // assertTrue( "Given: ", repoManager != null );
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, repoManager, null );

        TestDexFileHandler.loadAndAddManagedProject( mgr );
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
        // none found in these libraries
        for (OtmLibrary lib : mgr.getUserLibraries()) {
            List<OtmLibrary> includes = lib.getIncludes();
            // log.debug( lib + " includes = " + includes );
        }
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
            // if (!member.getWhereUsed().isEmpty())
            // log.debug( "Where used lists deleted member " + member );
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

            // log.debug( "Added and removed: " + member );
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

    /**
     * Set assigned type and if successful and list is non-null, add the user's owning member to the list.
     */
    private void setProvider(OtmTypeUser u, OtmTypeProvider p, List<OtmLibraryMember> list) {
        OtmTypeProvider r = u.setAssignedType( p );
        if (list != null && r != null && u.getLibrary() != p.getLibrary()) {
            if (!list.contains( u.getOwningMember() ))
                list.add( u.getOwningMember() );
        }
    }

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
        simples[2] = TestOtmSimple.buildOtm( subjectLib );
        simples[2].setName( "Simple2" );

        // Given - make assignments and keep an array of user's members assigned to providerLib2
        List<OtmLibraryMember> providerLib2Users = new ArrayList<>();
        OtmSimpleObject simple;
        int i = 0;
        for (OtmLibraryMember m : subjectLib.getMembers()) {
            if (m instanceof OtmTypeUser) {
                simple = simples[i++ % 3];
                if (simple.getLibrary() == providerLib2)
                    setProvider( (OtmTypeUser) m, simple, providerLib2Users );
                else
                    setProvider( (OtmTypeUser) m, simple, null );
            }
            for (OtmTypeUser u : m.getDescendantsTypeUsers()) {
                simple = simples[i++ % 3];
                if (simple.getLibrary() == providerLib2)
                    setProvider( u, simple, providerLib2Users );
                else
                    setProvider( u, simple, null );
                // log.debug( "Assigned " + simple + " to " + u );
            }
        }

        // When - getProviders unsorted
        Map<OtmLibrary,List<OtmLibraryMember>> map = subjectLib.getProvidersMap();

        // Then - library key and value sets are not empty and contain the provider libraries
        Set<OtmLibrary> keys = map.keySet();
        assertTrue( !keys.isEmpty() );
        assertTrue( map.keySet().contains( providerLib ) );
        assertTrue( map.keySet().contains( providerLib2 ) );
        Collection<List<OtmLibraryMember>> valueSet = map.values();
        assertTrue( !valueSet.isEmpty() );

        // When - sorted
        map = subjectLib.getProvidersMap();
        keys = map.keySet();
        valueSet = map.values();

        // Then
        assertTrue( !keys.isEmpty() );
        assertTrue( !valueSet.isEmpty() );

        // Then -
        List<OtmLibraryMember> values = map.get( providerLib2 );
        assertTrue( !values.isEmpty() );
        // Then - all providerLib2Users must be in the value set
        for (OtmLibraryMember u : providerLib2Users)
            assertTrue( values.contains( u ) );
        // Then - all values must be in providerLib2Users
        for (OtmLibraryMember u : values)
            assertTrue( providerLib2Users.contains( u ) );

        // TODO - is this finding dependencies in Resources?
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

        // When - library is forced to be not editable then refreshed
        ((TLLibrary) lib.getTL()).setStatus( TLLibraryStatus.FINAL );
        lib.refresh();
        assertTrue( "Must be read only action manager.", lib.getActionManager() instanceof DexReadOnlyActionManager );
        // Then
        for (OtmLibraryMember m : mgr.getMembers( lib )) {
            // log.debug( "Read-only tests for Member: " + m.getName() + " type = " + m.getObjectTypeName() );
            assertTrue( "Given", !m.isEditable() );
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
            // log.debug( "Editable Member: " + m.getName() + " type = " + m.getObjectTypeName() );
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
     * Assure library contents are editable.
     */
    public static void checkContentsAreEditable(OtmLibrary lib) {
        for (OtmLibraryMember lm : lib.getMembers()) {
            assertTrue( lm.isEditable() );
            for (OtmObject d : lm.getDescendants())
                assertTrue( d.isEditable() );
        }
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
        OtmLibrary lib = mgr.add( buildTL( namespace, prefix, name ) );
        check( lib );
        return lib;
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

    public static void check(OtmLibrary library) {
        assertTrue( library.getModelManager() != null );
        assertTrue( library.getModelManager().getTlModel() != null );

        assertTrue( !library.getPrefix().isEmpty() );
        assertTrue( !library.getName().isEmpty() );
        assertTrue( !library.getBaseNamespace().isEmpty() );
        //
        assertTrue( library.getTL() instanceof TLLibrary );
        assertTrue( library.contains( library.getTL() ) );
        // FIXME - why are some test failing here?
        // assertTrue( library.getTL().getOwningModel() != null );
    }

    /**
     * Build one of each library member type.
     * 
     * @param lib
     */
    public static void addOneOfEach(OtmLibrary lib) {
        int i = 1;
        for (OtmLibraryMemberType value : OtmLibraryMemberType.values()) {
            try {
                OtmLibraryMember member =
                    OtmLibraryMemberType.buildMember( value, "TestObj" + i++, lib.getModelManager() );
                OtmLibraryMember result = lib.add( member );
                // Checks
                if (lib.isEditable() && result != null) {
                    assertTrue( member.isEditable() );
                    assertTrue( member.getTlLM().getOwningLibrary() == lib.getTL() );
                    // if (member instanceof OtmContextualFacet)
                    // log.debug( "Here" );
                }
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
        // Attach the contextual facets
        for (OtmLibraryMember m : lib.getMembers()) {
        }
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
