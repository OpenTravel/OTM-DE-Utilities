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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.TestDexFileHandler;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMemberType;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.OtmSimpleObject;
import org.opentravel.model.otmLibraryMembers.OtmXsdSimple;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestLibraryMemberBase;
import org.opentravel.model.otmLibraryMembers.TestOtmSimple;
import org.opentravel.model.otmLibraryMembers.TestResource;
import org.opentravel.model.otmProperties.TestElement;
import org.opentravel.model.otmProperties.TestOtmTypeProviderInterface;
import org.opentravel.model.otmProperties.TestOtmTypeUserInterface;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.version.VersionSchemeException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.beans.property.SimpleStringProperty;

/**
 * Testing libraries.
 */
// @Ignore
public class TestLibrary extends TestLibraryUtils {
    private static Logger log = LogManager.getLogger( TestLibrary.class );

    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( TestLibrary.class );
    }

    //
    // FIXME - add changing namespace!
    //

    @Test
    public void testUtils() {
        testLibraryUtils();
    }

    @Test
    public void testAddMember() {
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, null, null );

        OtmLibrary lib1 = TestLibrary.buildOtm( mgr, "Namespace1", "p1", "Library1" );
        // log.debug( "Lib 1 name is: " + lib1.getFullName() );

        // Create business object
        OtmBusinessObject member = TestBusiness.buildOtm( mgr );
        member.setName( "TestBusinessObject" );
        // When
        lib1.add( member );
        // Then
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

    // // TODO - test facet set/un-set as assigned type
    // @Test
    // public void testDelete_whereUsed() {
    // // Given - a library
    // OtmLibrary lib = TestLibrary.buildOtm();
    // OtmModelManager mgr = lib.getModelManager();
    // addOneOfEach( lib );
    //
    // // Given an element to set and un-set assigned type
    // OtmCore core = TestCore.buildOtm( lib, "TestCore" );
    // OtmElement<?> element = TestElement.buildOtm( core.getSummary(), mgr.getStringType() );
    //
    // ArrayList<OtmLibraryMember> members = new ArrayList<>( mgr.getMembers() );
    // OtmTypeProvider a;
    // for (OtmLibraryMember member : members) {
    // // Skip over the xsd simple object that are used in Core#buildOTM
    // if (member instanceof OtmXsdSimple)
    // continue;
    // assertTrue( "Given: ", member.getWhereUsed().isEmpty() );
    // if (member instanceof OtmTypeProvider) {
    // log.debug( "Testing assignment of " + member.getObjectTypeName() + " " + member );
    // // When assigned as type
    // a = element.setAssignedType( (OtmTypeProvider) member );
    // // Not all members can be assigned
    // if (a == member)
    // assertTrue( "must contain core", member.getWhereUsed().contains( core ) );
    //
    // }
    // lib.delete( member );
    // // This is OK. Needed for un-delete
    // // if (!member.getWhereUsed().isEmpty())
    // // log.debug( "Where used lists deleted member " + member );
    // }
    // }

    /**
     * Check to assure member is deleted from library.
     * <ul>
     * <li>Member does not have a library.
     * <li>Library does not contain library.
     * <li>Model manager does not contain member or TLMember.
     * <li>TLLibrary does not contain named member.
     * </ul>
     * 
     * @param member
     * @param lib
     */
    public static void checkDeleted(OtmLibraryMember member, OtmLibrary lib) {
        OtmModelManager mgr = lib.getModelManager();
        assertTrue( "Check: Member's library must be null.", member.getLibrary() == null );
        assertTrue( "Check: Library does NOT contain member.", !lib.contains( member ) );
        assertTrue( "Check: Model manager must NOT contain memeber.", !mgr.contains( member ) );
        assertTrue( "Check: Model manager must NOT contain TL member.", !mgr.contains( member.getTlLM() ) );
        assertTrue( "Check: Model manager's member list must NOT contain member",
            !mgr.getMembers().contains( member ) );
        assertTrue( "Check: TLLibrary must NOT contain member.",
            !lib.getTL().getNamedMembers().contains( member.getTL() ) );
    }

    /**
     * Test deleting type providers. Check owner's whereUsed and typeUser lists.
     */
    @Test
    public void testDelete_AssignmentsProvider() {
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmModelManager mgr = lib.getModelManager();

        // Given - a set of providers
        // OtmXsdSimple simple = mgr.getStringType();
        List<OtmTypeProvider> providers = new ArrayList<>();
        TestOtmTypeProviderInterface.buildOneOfEach( lib ).forEach( p -> {
            if (p instanceof OtmLibraryMember)
                providers.add( p );
        } );

        // Given - a Business Object facet with elements for each provider
        OtmBusinessObject bo = TestBusiness.buildOtm( lib, "UserBo" );
        for (OtmTypeProvider p : providers)
            TestElement.buildOtm( bo.getSummary(), p );

        // When - each provider is deleted
        for (OtmTypeProvider m : providers) {
            OtmLibraryMember member = (OtmLibraryMember) m;
            // Where used before delete
            List<OtmLibraryMember> whereUsed = member.getWhereUsed();
            assertTrue( "Given: ", whereUsed.contains( bo ) );
            assertTrue( "Given: ", whereUsed.size() == 1 );
            // Type users before delete
            List<OtmTypeUser> typeUsers = bo.getTypeUsers( m );
            assertTrue( "Given:", typeUsers != null );
            assertTrue( "Given: ", typeUsers.size() == 1 );
            OtmTypeUser user = typeUsers.get( 0 );

            // When
            lib.delete( member );
            checkDeleted( member, lib );
            log.debug( "Deleted " + member );

            // User
            assertTrue( "Then: user must have null assigned type.", user.getAssignedType() == null );
            assertTrue( "Then: user must NOT be in Owner's type user list.", bo.getTypeUsers( m ).isEmpty() );

            // member Provider - Where used should be unchanged
            assertTrue( "Then: deleted member-provider where used must be unchanged.",
                member.getWhereUsed().contains( bo ) );
            assertTrue( "Then: deleted member-provider where used must be unchanged.",
                member.getWhereUsed().size() == 1 );
            // TODO - should the member:changedWhereUsed ignore if lib == null?
        }
    }

    /**
     * Test deleting type users.
     */
    @Test
    public void testDelete_AssignmentsUser() {
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmModelManager mgr = lib.getModelManager();

        // Given - providers and users
        OtmXsdSimple simple = mgr.getStringType();
        List<OtmTypeProvider> providers = TestOtmTypeProviderInterface.buildOneOfEach( lib );
        List<OtmTypeUser> users = TestOtmTypeUserInterface.buildOneOfEach( lib, simple, true );

        // When - Library member users are deleted
        for (OtmTypeUser user : users) {
            if (user instanceof OtmLibraryMember) {
                assertTrue( "Given: ", simple.getWhereUsed().contains( user ) );
                TestOtmTypeUserInterface.check( user, simple );
                // When deleted
                lib.delete( (OtmLibraryMember) user );

                checkDeleted( (OtmLibraryMember) user, lib );
                OtmTypeProvider at = user.getAssignedType();
                NamedEntity tlAt = user.getAssignedTLType();
                assertTrue( "Then: assigned type must be unchanged.", user.getAssignedType() == simple );
                assertTrue( "Then: assigned TL type must be unchanged.", user.getAssignedTLType() == simple.getTL() );
                assertTrue( "Then: must be in usedTypes list.",
                    ((OtmLibraryMember) user).getUsedTypes().contains( simple ) );
                assertTrue( "Then: must be in where used list.", simple.getWhereUsed().contains( user ) );
                // assertTrue( "Then: must not be in where used list.", !simple.getWhereUsed().contains( user ) );
            }
        }
    }


    @Test
    public void testAddAndDelete() throws ExceptionInInitializerError, InstantiationException, IllegalAccessException,
        NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        OtmModelManager mgr = new OtmModelManager( null, null, null );
        OtmLibrary lib = mgr.addLibrary( new TLLibrary() );
        // OtmLibrary lib = mgr.addOLD( new TLLibrary() );

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
            checkDeleted( member, lib );
            // assertFalse( "Then: ", mgr.contains( member.getTlLM() ) );
            // assertFalse( "Then: ", mgr.getMembers().contains( member ) );
            // assertFalse( "Then: ", lib.getTL().getNamedMembers().contains( member.getTL() ) );

            // TODO - what about contextual facets?

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

    @Test
    public void testGetUsers() {
        OtmLibrary lib = TestLibrary.buildOtm( getModelManager() );
        TestLibrary.addOneOfEach( lib );

        assertTrue( "Then: must find some users.", !lib.getUsers().isEmpty() );
        // TODO - add specific tests
    }

    @Test
    public void testGetProviders() {
        // Given - action and model managers
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, null, null );

        // Given - a subject library that uses types, base types, contextual facets
        OtmLibrary subjectLib = TestLibrary.buildOtm( mgr, "NamespaceS1", "s1", "LibraryS1" );
        addOneOfEach( subjectLib );
        TestLibraryMemberBase.buildOneOfEachWithProperties( subjectLib );

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

        // Local libraries are always editable
        // // When - library is forced to be not editable then refreshed
        // ((TLLibrary) lib.getTL()).setStatus( TLLibraryStatus.FINAL );
        // lib.refresh();
        // assertTrue( "Must be read only action manager.", lib.getActionManager() instanceof DexReadOnlyActionManager
        // );
        // // Then
        // for (OtmLibraryMember m : mgr.getMembers( lib )) {
        // // log.debug( "Read-only tests for Member: " + m.getName() + " type = " + m.getObjectTypeName() );
        // assertTrue( "Given", !m.isEditable() );
        // assertTrue( "Given", m.getActionManager() instanceof DexReadOnlyActionManager );
        // assertTrue( "Refresh must change property class.", m.nameProperty() instanceof ReadOnlyStringWrapper );
        // }
        //
        // // When - library is forced to be editable
        // ((TLLibrary) lib.getTL()).setStatus( TLLibraryStatus.DRAFT );
        // assertTrue( "Must be full action manager.", lib.getActionManager() instanceof DexFullActionManager );
        // lib.refresh();
        // // Then
        // for (OtmLibraryMember m : mgr.getMembers( lib )) {
        // assertTrue( "Given", m.isEditable() );
        // assertFalse( "Refresh must change property class.", m.nameProperty() instanceof ReadOnlyStringWrapper );
        // // log.debug( "Editable Member: " + m.getName() + " type = " + m.getObjectTypeName() );
        // }
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

    // /** *************************** Static Builders *************************** **/
    //
    // /**
    // * Create new ModelManager and Full Action Manager. Then {@link #buildOtm(OtmModelManager)}
    // *
    // * @param mgr
    // * @return
    // */
    // public static OtmLocalLibrary buildOtm() {
    // return buildOtm( new OtmModelManager( new DexFullActionManager( null ), null, null ) );
    // }
    //
    // /**
    // * Run {@link #buildOtm(OtmModelManager, String, String, String)} named "LibraryName" in example.com namespace
    // with
    // * prefix "pre".
    // *
    // * @param mgr
    // * @return
    // */
    // public static OtmLocalLibrary buildOtm(OtmModelManager mgr) {
    // return buildOtm( mgr, "http://example.com/ns/v0", "pre", "LibraryName" );
    // }
    //
    // public static OtmLocalLibrary buildOtm(OtmModelManager mgr, String namespace, String prefix, String name) {
    // TLLibrary tlLib = buildTL();
    // tlLib.setOwningModel( mgr.getTlModel() );
    // tlLib.setName( name );
    // tlLib.setPrefix( prefix );
    // tlLib.setNamespace( namespace );
    // tlLib.setLibraryUrl( URLUtils.toURL( "file://exampleLib.otm" ) );
    //
    // OtmLibrary lib = mgr.add( tlLib );
    //
    // assertTrue( "Builder", lib instanceof OtmLocalLibrary );
    // assertTrue( "Builder", lib.isEditable() );
    // assertTrue( "Builder - model manager must be able to find the library.", mgr.get( lib.getTL() ) == lib );
    // assertTrue( "Builder", lib.getStatus() == TLLibraryStatus.DRAFT );
    // assertTrue( "Builder: library must be owned by model.", tlLib.getOwningModel() != null );
    // assertTrue( "Builder",
    // lib.getState() == RepositoryItemState.MANAGED_WIP || lib.getState() == RepositoryItemState.UNMANAGED );
    //
    // checkLibrary( lib );
    // return (OtmLocalLibrary) lib;
    // }
    //
    // public static TLLibrary buildTL(String namespace, String prefix, String name) {
    // TLLibrary tlLib = new TLLibrary();
    // tlLib.setName( name );
    // tlLib.setPrefix( prefix );
    // tlLib.setNamespace( namespace );
    // tlLib.setLibraryUrl( URLUtils.toURL( "File://example.otm" ) );
    // return tlLib;
    // }
    //
    // public static TLLibrary buildTL() {
    // return new TLLibrary();
    // }
    //
    // /**
    // * Check model manager, prefix and namespace, tlLibrary.
    // *
    // * @param library
    // */
    // public static void checkLibrary(OtmLibrary library) {
    // assertTrue( "Check library: ", library != null );
    // assertTrue( "Check library: ", library.getModelManager() != null );
    // assertTrue( "Check library: ", library.getModelManager().getTlModel() != null );
    // //
    // assertTrue( "Check library: ", !library.getPrefix().isEmpty() );
    // assertTrue( "Check library: ", !library.getName().isEmpty() );
    // assertTrue( "Check library: ", !library.getBaseNamespace().isEmpty() );
    // //
    // assertTrue( "Check library: ", library.getTL() instanceof TLLibrary );
    // assertTrue( "Check library: ", library.contains( library.getTL() ) );
    // }
    //
    // /**
    // * Assure library contents are editable.
    // */
    // public static void checkContentsAreEditable(OtmLibrary lib) {
    // for (OtmLibraryMember lm : lib.getMembers()) {
    // assertTrue( lm.isEditable() );
    // for (OtmObject d : lm.getDescendants())
    // assertTrue( d.isEditable() );
    // }
    // }

    // private static final String AOEV = "AddOneOfEachValid_";
    //
    // /**
    // * Build and add one of each of the 6 primary object. No service or resource built.
    // *
    // * @param lib
    // */
    // public static void addOneOfEachValid(OtmLibrary lib) {
    // assertTrue( "Add Util pre-condition: must be valid.", lib.isValid() );
    // TestBusiness.buildOtm( lib, AOEV + "bo" );
    // TestCore.buildOtm( lib, AOEV + "core" );
    // TestChoice.buildOtm( lib, AOEV + "choice" );
    // TestValueWithAttributes.buildOtm( lib, AOEV + "vwa" );
    // TestEnumerationClosed.buildOtm( lib, AOEV + "ec" );
    // TestEnumerationOpen.buildOtm( lib, AOEV + "eo" );
    // if (!lib.isValid()) {
    // for (OtmLibraryMember m : lib.getMembers())
    // if (!m.isValid()) {
    // List<ValidationFinding> findings = lib.getFindings().getAllFindingsAsList();
    // String fString = ValidationUtils.getMessagesAsString( lib.getFindings() );
    // }
    // log.debug( ValidationUtils.getMessagesAsString( lib.getFindings() ) );
    // }
    // assertTrue( "Add Util post-condition: must be valid.", lib.isValid() );
    // }

    // /**
    // * Build one of each library member type.
    // * <p>
    // * <b>Note: </b> the library will not be valid.
    // *
    // * @see OtmLibraryMemberType for enumeration of member types
    // * @param lib
    // */
    // public static int addOneOfEach(OtmLibrary lib) {
    // int i = 1;
    // assertTrue( "Must be an editable library.", lib.isEditable() );
    //
    // for (OtmLibraryMemberType value : OtmLibraryMemberType.values()) {
    // try {
    // OtmLibraryMember member =
    // OtmLibraryMemberType.buildMember( value, "TestObj" + i++, lib.getModelManager() );
    // OtmLibraryMember result = lib.add( member );
    // // Checks
    // if (result != null) {
    // assertTrue( member.isEditable() );
    // assertTrue( member.getTlLM().getOwningLibrary() == lib.getTL() );
    // // if (member instanceof OtmContextualFacet)
    // // log.debug( "Here" );
    // } else {
    // i--;
    // log.debug( "Could not add an " + value );
    // }
    // } catch (ExceptionInInitializerError e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (InstantiationException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (IllegalAccessException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (NoSuchMethodException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (InvocationTargetException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }
    // // Attach the contextual facets
    // for (OtmLibraryMember m : lib.getMembers()) {
    // }
    // log.debug( "Added " + i + " to " + lib );
    // return i;
    // }


}
