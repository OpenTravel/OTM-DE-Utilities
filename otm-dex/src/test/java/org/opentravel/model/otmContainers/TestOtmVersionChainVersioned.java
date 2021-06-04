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
import org.opentravel.AbstractDexTest;

/**
 * Verifies the functions of the <code>Otm Version Chain - Versioned</code>.
 */
public class TestOtmVersionChainVersioned extends AbstractDexTest {
    private static Log log = LogFactory.getLog( TestOtmVersionChainVersioned.class );

    // // Populated by buildOtm()
    // OtmMajorLibrary majorLib = null;
    // OtmMinorLibrary minorLib1 = null;
    // OtmMinorLibrary minorLib2 = null;
    // OtmMinorLibrary minorLib3 = null;
    //
    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( null );
    }

    @Before
    public void beforeTest() {
        getModelManager().clear();
    }

    /**
     * After a minor version is created, getNextVersion() in isLastest() throws this error: <br>
     * <code> java.lang.IllegalArgumentException: The given
     * item is not assigned to this version chain</code>
     */
    @Test
    public void testGetTlVersionChain() {
        OtmLibrary major = buildMajor( "TestMajor" );
        OtmVersionChain majorChain = major.getVersionChain();
        assertTrue( majorChain.isLatest( major ) );
        // FIXME - todo
    }

    @Test
    public void testIsLatest() {
        // TODO
    }

    //
    // /**
    // * Check chain. Assure all libraries:
    // * <li>are in same base namespace
    // * <li>have the same major version number
    // * <li>have a valid version scheme
    // *
    // * @param chain
    // */
    // public static void check(OtmVersionChain chain) {
    // OtmLibrary major = chain.getMajor();
    // assertTrue( "Given: must have major version.", major instanceof OtmLibrary );
    //
    // int vn = -1;
    // vn = major.getMajorVersion();
    // assertTrue( "Given: must have valid version number.", vn >= 0 );
    //
    // VersionChain<TLLibrary> tlChain = null;
    // if (chain instanceof OtmVersionChainVersioned) {
    // assertTrue( "Given: must have major version.", major instanceof OtmMajorLibrary );
    // tlChain = ((OtmVersionChainVersioned) chain).getTlVersionChain( major.getTL() );
    // assertTrue( "Check: ", tlChain != null );
    // }
    // for (OtmLibrary lib : chain.getLibraries()) {
    // assertTrue( "Check: namespace error", lib.getNameWithBasenamespace().equals( chain.getBaseNamespace() ) );
    // assertTrue( "Check: must have same major version number.", lib.getMajorVersion() == vn );
    // if (tlChain != null)
    // assertTrue( tlChain.getVersions().contains( lib.getTL() ) );
    // // TODO - test getChainLibraries
    // }
    // }
    //
    // @Test
    // public void testIsChainEditable() {
    // // TODO
    // }
    //
    // @Test
    // public void testIsNewToChain() {
    // // Members of local libraries are always new.
    // OtmLibrary lib = TestLibrary.buildOtm( getModelManager() );
    // TestLibrary.addOneOfEach( lib );
    // OtmVersionChain chain = lib.getVersionChain();
    // for (OtmLibraryMember member : lib.getMembers()) {
    // assertTrue( "Then: local library member must be new.", chain.isNewToChain( member ) );
    // if (member instanceof OtmContextualFacet)
    // log.debug( "Checked contextual facet in local library." );
    // }
    //
    // OtmLibrary major = buildMajor( "VCTest1" );
    // rtuLock( (OtmManagedLibrary) major );
    // TestLibrary.addOneOfEachValid( major );
    // chain = major.getVersionChain();
    // for (OtmLibraryMember member : major.getMembers()) {
    // assertTrue( "Then: New library member must be new.", chain.isNewToChain( member ) );
    // if (member instanceof OtmContextualFacet)
    // log.debug( "Checked contextual facet in new Major library." );
    // }
    // //
    // // FIXME - finish
    // //
    // // OtmLibrary minor = buildMinor( major );
    // // TestLibrary.addOneOfEach( minor );
    // // Contextual facet members are always new.
    // // Members of managed are only new if no other chain member by name.
    // }
    //
    // /** ****************************** Old Tests ************************** **/
    //
    //
    // // Null library implies it is a empty chain
    // // /**
    // // * Test with null library
    // // */
    // // @Test(expected = IllegalArgumentException.class)
    // // public void testConstructor_null() throws Exception {
    // // new OtmVersionChain( null );
    // // assertTrue( "Must not reach this assertion.", false );
    // // }
    //
    // @Test
    // public void testConstructor_oneLib() {
    // // Test with one new library
    // // FIXME - must be a managed library
    // // OtmLibrary refLib = TestLibrary.buildOtm();
    // // OtmVersionChain chain = new OtmVersionChain( refLib );
    // // assertTrue( "Must build a chain.", chain != null );
    // // assertTrue( "Must contain the library.", chain.contains( refLib ) );
    // }
    //
    //
    // @Test
    // public void testConstructor() throws DexProjectException {
    // // Given a valid library in a project
    // OtmLocalLibrary mLib = buildTempLibrary( null, "TestVersionChain", "TVC" );
    // OtmProject proj = TestProject.build( getModelManager() );
    // proj.add( mLib );
    // OtmMajorLibrary majorLib = getModelManager().getOtmProjectManager().publish( mLib, getRepository() );
    //
    // // Build and test
    // buildOtm( majorLib );
    // }
    //
    // /** ****************************** Old Tests ************************** **/
    // /**
    // * {@link OtmVersionChain#isLaterVersion(OtmLibrary, OtmLibrary)} method is static with two libraries as
    // parameters
    // */
    // // FIXME - fails to build candidate 3
    // @Ignore
    // @Test
    // public void testIsLaterVersionLibrary_unmanagedLibs() {
    // // Test with un-managed libraries
    // OtmLibrary refLib = TestLibrary.buildOtm();
    // OtmLibrary candidate1 = TestLibrary.buildOtm( refLib.getModelManager(), "fooBar", "fb", "NotInChain" );
    // OtmLibrary candidate2 = TestLibrary.buildOtm( refLib.getModelManager(), refLib.getBaseNS(), refLib.getPrefix(),
    // refLib.getName() + "/v1.1" );
    // OtmLibrary candidate3 =
    // TestLibrary.buildOtm( refLib.getModelManager(), refLib.getBaseNS(), refLib.getPrefix(), refLib.getName() );
    // candidate3.getTL().setVersion( "0.1.0" );
    //
    // boolean result = false;
    // result = OtmVersionChainBase.isLaterVersion( refLib, candidate1 );
    // assertTrue( "Must be false because of different base namespace.", result == false );
    // result = OtmVersionChainBase.isLaterVersion( refLib, candidate2 );
    // assertTrue( "Must be false because different major version.", result == false );
    // result = OtmVersionChainBase.isLaterVersion( refLib, candidate3 );
    // assertTrue( "Must be true.", result == true );
    // }
    //
    // @Test
    // public void testIsLatest() {
    // OtmModelManager mgr = getModelManager();
    //
    // OtmLibrary localLib = TestLibrary.buildOtm( mgr );
    // OtmLibrary majorLib = buildMajor( "TMM1" );
    // OtmLibrary minor1 = buildMinor( majorLib );
    // OtmLibrary minor2 = buildMinor( minor1 );
    // OtmLibrary majorLib2 = buildMajor( "TMM2" );
    //
    // OtmVersionChain chain1 = localLib.getVersionChain();
    // check( chain1 );
    // OtmVersionChain chain2 = majorLib.getVersionChain();
    // check( chain2 );
    // OtmVersionChain chain3 = minor1.getVersionChain();
    // check( chain3 );
    // OtmVersionChain chain4 = minor2.getVersionChain();
    // check( chain4 );
    // OtmVersionChain chain5 = majorLib2.getVersionChain();
    // check( chain5 );
    //
    // //// The direct version chain checks were used while debugging
    // //// The TL Version chains are wrong! I don't know why.
    // //// The chain changes when new content is added/removed
    // // VersionChainFactory vcf = OtmVersionChainVersioned.getVersionChainFactory( getModelManager().getTlModel() );
    // // VersionChain<TLLibrary> mvc = vcf.getVersionChain( (TLLibrary) majorLib.getTL() );
    // // mvc.getVersions().forEach( v -> log.debug( v ) );
    // // VersionChain<TLLibrary> mvc2 = vcf.getVersionChain( (TLLibrary) minor1.getTL() );
    // // mvc2.getVersions().forEach( v -> log.debug( v ) );
    // // assertTrue( mvc == mvc2 );
    // // assertTrue( "TL check: ", mvc.getVersions().contains( majorLib.getTL() ) );
    // // assertTrue( "TL check: ", mvc.getVersions().contains( minor1.getTL() ) );
    // //
    // // VersionChain<TLLibrary> chain2vc = ((OtmVersionChainVersioned) chain2).getTlVersionChain( majorLib.getTL() );
    // // chain2vc.getVersions().forEach( v -> log.debug( v ) );
    // // assertTrue( "TL check: ", chain2vc.getVersions().contains( majorLib.getTL() ) );
    // // assertTrue( "TL check: ", chain2vc.getVersions().contains( minor1.getTL() ) );
    //
    // assertTrue( "Then: ", chain1.isLatest( localLib ) == true );
    // assertTrue( "Then: ", chain2.isLatest( majorLib ) == false );
    // assertTrue( "Then: ", chain3.isLatest( minor1 ) == false );
    // assertTrue( "Then: ", chain4.isLatest( minor2 ) == true );
    // assertTrue( "Then: ", chain5.isLatest( majorLib2 ) == true );
    // }
    //
    // @Test
    // public void testAdd() {
    // // assure the tl vc is updated
    // }
    //
    // /**
    // * {@link OtmVersionChain#isLaterVersion(OtmLibrary, OtmLibrary)} method is static with two libraries as
    // parameters
    // *
    // * @throws RepositoryException
    // * @throws DexProjectException
    // */
    // @Test
    // public void testIsLaterVersionLibrary() throws DexProjectException {
    // // Given the chain and its libraries
    // OtmLocalLibrary lib = buildTempLibrary( null, "TestVersionChain", "TVC" );
    // OtmProject proj = TestProject.build( getModelManager() );
    // proj.add( lib );
    // OtmMajorLibrary mLib = getModelManager().getOtmProjectManager().publish( lib, getRepository() );
    // buildOtm( mLib );
    //
    // int vn = majorLib.getMinorVersion();
    // int vn1 = minorLib1.getMinorVersion();
    // int vn2 = minorLib2.getMinorVersion();
    // int vn3 = minorLib3.getMinorVersion();
    // assertTrue( "Givens: ", vn < vn1 && vn1 < vn2 && vn2 < vn3 );
    //
    // boolean result = OtmVersionChainBase.isLaterVersion( majorLib, majorLib );
    // assertTrue( "Must be false because is same library.", result == false );
    //
    // assertTrue( "Must be true.", OtmVersionChainBase.isLaterVersion( majorLib, minorLib1 ) );
    // assertTrue( "Must be true.", OtmVersionChainBase.isLaterVersion( majorLib, minorLib2 ) );
    // assertTrue( "Must be true.", OtmVersionChainBase.isLaterVersion( majorLib, minorLib3 ) );
    // assertTrue( "Must be true.", OtmVersionChainBase.isLaterVersion( minorLib1, minorLib2 ) );
    // assertTrue( "Must be true.", OtmVersionChainBase.isLaterVersion( minorLib2, minorLib3 ) );
    //
    // assertTrue( "Must be false.", !OtmVersionChainBase.isLaterVersion( minorLib1, majorLib ) );
    // assertTrue( "Must be false.", !OtmVersionChainBase.isLaterVersion( minorLib2, minorLib1 ) );
    // assertTrue( "Must be false.", !OtmVersionChainBase.isLaterVersion( minorLib3, minorLib2 ) );
    //
    // OtmLibrary otherNS = TestLibrary.buildOtm( getModelManager() );
    // assertTrue( "Must be false.", !OtmVersionChainBase.isLaterVersion( otherNS, majorLib ) );
    // assertTrue( "Must be false.", !OtmVersionChainBase.isLaterVersion( minorLib2, otherNS ) );
    // assertTrue( "Must be false.", !OtmVersionChainBase.isLaterVersion( otherNS, minorLib2 ) );
    // }
    //
    //
    //
    // /**
    // * Build a chain with 3 minor versions from the passed major library.
    // *
    // * @param majorLib
    // * @return
    // */
    // public OtmVersionChain buildOtm(OtmMajorLibrary lib) {
    // assertTrue( lib != null );
    // // Not published, so this will fail - assertTrue( majorLib.isMajorVersion() );
    //
    // // When a minor is created from a valid library
    // majorLib = lib;
    // minorLib1 = buildMinor( majorLib );
    // minorLib2 = buildMinor( minorLib1 );
    // minorLib3 = buildMinor( minorLib2 );
    //
    // // Then
    // OtmVersionChain chain = majorLib.getVersionChain();
    // assertTrue( "Then - major library must have a chain.", chain != null );
    // assertTrue( "Must have all libraries.", chain.getLibraries().size() >= 2 );
    // assertTrue( "Then - chain must contain major library.", chain.getLibraries().contains( majorLib ) );
    // assertTrue( "Then - chain must contain minor1 library.", chain.getLibraries().contains( minorLib1 ) );
    // assertTrue( "Then - chain must contain minor2 library.", chain.getLibraries().contains( minorLib2 ) );
    // assertTrue( "Then - chain must contain minor3 library.", chain.getLibraries().contains( minorLib3 ) );
    //
    // String chainName = majorLib.getVersionChainName();
    // assertTrue( "Then chain must have a name.", !chainName.isEmpty() );
    //
    // // Constructor uses model manager to find chain members. Only finds managed libraries.
    // List<OtmLibrary> mchain = getModelManager().getChainLibraries( majorLib );
    // for (OtmLibrary m : mchain)
    // assertTrue( "Then modelManager must be able to find the same chain libraries.",
    // chain.getLibraries().contains( m ) );
    //
    // check( chain );
    // return chain;
    // }
    //
    // @Test
    // public void testContains() {
    // // // Test with un-managed library
    //
    // // // Test with test set
    // // Given a valid library in a project
    // OtmMajorLibrary major = buildMajor( "TestContains1" );
    // // Build and test
    // buildOtm( major );
    // OtmLibrary noInChain = TestLibrary.buildOtm( getModelManager() );
    //
    // // Then
    // OtmVersionChain chain = major.getVersionChain();
    // assertTrue( chain.contains( majorLib ) );
    // assertTrue( chain.contains( minorLib1 ) );
    // assertTrue( chain.contains( minorLib2 ) );
    // assertTrue( chain.contains( minorLib3 ) );
    //
    // assertTrue( !chain.contains( noInChain ) );
    // }
    //
    // /**
    // * Logic behind for {@link TestOtmModelChainsManager#testGetChainLibraries()} and
    // * {@link TestOtmModelManager_Gets#testGetChainLibraries()}
    // */
    // @Test
    // public void testGetChainLibraries_TestSuite() {
    // OtmModelManager mgr = getModelManager();
    // Map<OtmVersionChain,List<OtmLibrary>> setMap = buildLibraryTestSet();
    // List<OtmLibrary> libraries = mgr.getUserLibraries();
    //
    // // Check against the test set's map of chain:libraries
    // for (OtmVersionChain chain : setMap.keySet()) {
    // assertTrue( "Given: test set map must contain the chain.", setMap.containsKey( chain ) );
    // String chainName = chain.getBaseNamespace();
    //
    // assertTrue( "Then: must have name.", chainName != null && !chainName.isEmpty() );
    // assertTrue( "Then: Chain must have at least one library.", !chain.getLibraries().isEmpty() );
    //
    // for (OtmLibrary lib : chain.getLibraries())
    // assertTrue( "Then: chain libraries must be in model manager.", libraries.contains( lib ) );
    //
    // for (OtmLibrary lib : setMap.get( chain )) {
    // assertTrue( " Then: Library must have same chain name.",
    // lib.getNameWithBasenamespace().endsWith( chainName ) );
    // assertTrue( "Then: chain must contain each set member", chain.getLibraries().contains( lib ) );
    // assertTrue( "Then: model manager facade must return each set member",
    // mgr.getChainLibraries( lib ).contains( lib ) );
    // }
    // }
    // }
    //
    // /**
    // * <li>The subject must be the latest version of the subject.
    // * <li>The assigned type must not be the latest version.
    // * <p>
    // * {@link OtmVersionChain#canAssignLaterVersion(OtmTypeUser)}
    // */
    // @Test
    // public void testCanAssignLaterVersion() {
    // // Test with un-managed library
    //
    // // Given a library with namespace from file
    // OtmLocalLibrary mLib = buildTempLibrary( "http://example.com/test/CanAssign", null, "TestCanAssign1" );
    // OtmProject proj = TestProject.build( getModelManager() );
    // try {
    // proj.add( mLib );
    // } catch (DexProjectException e) {
    // assertTrue( "Exception building major library: " + e.getLocalizedMessage(), false );
    // }
    // assertTrue( mLib.isEditable() );
    // // assertTrue( "Given: un-published library must not be reported in version chain.",
    // // getModelManager().getChainLibraries( mLib ).isEmpty() );
    //
    // // Add provider and users to the library
    // OtmBusinessObject provider = TestBusiness.buildOtm( mLib, "ProviderBO" );
    // OtmCore coreUser = TestCore.buildOtm( mLib, "UserCore" );
    // OtmElement<?> user = TestElement.buildOtm( coreUser.getSummary(), provider );
    //
    // // Publish and finalize major library
    // majorLib = publishLibrary( mLib ); // Finalize the library
    // TestManageLibraryTask.check( majorLib, getModelManager() );
    //
    // // Get test subject chain
    // OtmVersionChain chain = majorLib.getVersionChain();
    // assertTrue( "Given", chain.contains( majorLib ) );
    //
    // // Create minor version
    // minorLib1 = buildMinor( majorLib );
    // OtmVersionChain mChain = majorLib.getVersionChain();
    // // FIXME - can the chain be reused? If not document it.
    // // FIXME
    //
    // // - make sure constructor only adds if not there ... no dups
    // assertTrue( "Given", mChain.contains( minorLib1 ) );
    // assertTrue( "Given", chain.contains( minorLib1 ) );
    //
    // // Then - neither condition is met
    // assertTrue( "Can NOT assign later version.", !chain.canAssignLaterVersion( user ) );
    //
    // // When - minor version of user created
    // OtmCore userV1 = (OtmCore) coreUser.createMinorVersion( minorLib1 );
    // // Then - there subject has not been versioned
    // assertTrue( "Can NOT assign later version.", !chain.canAssignLaterVersion( userV1 ) );
    //
    // // When - subject versioned
    // OtmBusinessObject providerV1 = (OtmBusinessObject) provider.createMinorVersion( minorLib1 );
    // // Then - both conditions met
    //
    // // //
    // // // FIXME
    // // //
    // // assertTrue( "Can assign later version.", chain.canAssignLaterVersion( userV1 ) );
    //
    // // minorLib2 = buildMinor( minorLib1 );
    // // minorLib3 = buildMinor( minorLib2 );
    // //
    // // // Build and test
    // // OtmLibrary notInChain = TestLibrary.buildOtm( getModelManager() );
    // }
    //
    // //
    // // @Test
    // // public void testGetEditable() {
    // // // Test with un-managed library
    // // // Test with test set
    // // }
    // //
    // // @Test
    // // public void testGetMajor() {
    // // // Test with un-managed library
    // // // Test with test set
    // // }
    // //
    // // @Test
    // // public void testGetPrefix() {
    // // // Test with un-managed library
    // // // Test with test set
    // // }
    // //
    // // @Test
    // // public void testGetLatestVersion() {
    // // // Test with un-managed library
    // // // Test with test set
    // // }
    // //
    // // @Test
    // // public void testGetLatestVersionWithMember() {
    // // // Test with un-managed library
    // // // Test with test set
    // // }
    // //
    // // @Test
    // // public void testGetLibraries() {
    // // // Test with un-managed library
    // // // Test with test set
    // // }
    // //
    // // @Test
    // // public void testGetNewMinorLibraryMember() {
    // // // Test with un-managed library
    // // // Test with test set
    // // }
    // //
    // // @Test
    // // public void testGetNewMinorPropertyOwner() {
    // // // Test with un-managed library
    // // // Test with test set
    // // }
    // //
    // // @Test
    // // public void testGetNewMinorTypeUser() {
    // // // Test with un-managed library
    // // // Test with test set
    // // }
    // //
    // // @Test
    // // public void testIsChainEditable() {
    // // // Test with un-managed library
    // // // Test with test set
    // // }
    // //
    // // @Test
    // // public void testIsLaterVersionMember() {
    // // // Test with un-managed library
    // // // Test with test set
    // // }
    // //
    // // @Test
    // // public void testIsLatestVersion() {
    // // // Test with un-managed library
    // // // Test with test set
    // // }
    // //
    // // @Test
    // // public void testIsLatestChain() {
    // // // Test with un-managed library
    // // // Test with test set
    // // }
    // //
    // // @Test
    // // public void testIsNewToChain() {
    // // // Test with un-managed library
    // // // Test with test set
    // // }
    // //
    // // @Test
    // // public void testRefresh() {
    // // // Test with un-managed library
    // // // Test with test set
    // // }
    //
    //
    // /** ********************* Utilities that need a new home ***************************** */
    // // Interesting test - where does it belong? TestDexFileHandler ?
    // @Test
    // public void testAddingVersionedProject() throws Exception {
    //
    // // Given a project that uses the OpenTravel repository
    // OtmModelManager mgr = TestOtmModelManager.buildModelManager( new DexFullActionManager( null ) );
    // assertNotNull( mgr.getActionManager( true ) );
    //
    // // Load project and get latest library
    // if (!TestDexFileHandler.loadVersionProject( mgr ))
    // return; // No editable libraries
    // OtmLibrary latestLib = getMinorInChain( mgr );
    //
    // // Test adding properties to object in latest major
    // createMinorMembers( latestLib );
    //
    // // Test deleting the new members
    // List<OtmLibraryMember> minors = mgr.getMembers( latestLib );
    // for (OtmLibraryMember m : minors) {
    // log.debug( "Deleting " + m );
    // latestLib.delete( m );
    // }
    //
    // // Assure other members still have their properties ownership set correctly.
    // for (OtmLibraryMember m : mgr.getMembers())
    // TestLibraryMemberBase.checkOwnership( m );
    // }
    //
    // /**
    // * Get the latest library in the version chain with the highest major version number.
    // *
    // * @param mgr
    // * @return a minor library or null
    // * @throws VersionSchemeException
    // */
    // // Interesting utility - where does it belong? 6 users
    // public static OtmLibrary getMinorInChain(OtmModelManager mgr) throws VersionSchemeException {
    // OtmLibrary latestLib = null;
    // int highestMajor = 0;
    // for (OtmLibrary lib : mgr.getLibraries()) {
    // if (lib.isBuiltIn())
    // continue;
    // // log.debug( "Library " + lib + " opened." );
    // // log.debug( "Is latest? " + lib.isLatestVersion() );
    // // log.debug( "Is minor? " + lib.isMinorVersion() );
    // // log.debug( "Version number " + lib.getMajorVersion() + " " + lib.getMinorVersion() );
    // // log.debug( "Is editable? " + lib.isEditable() );
    // // log.debug( "What action manager? " + lib.getActionManager().getClass().getSimpleName() );
    // // log.debug( "Version chain contains " + mgr.getVersionChain( lib ).size() + " libraries" );
    // // log.debug( "" );
    //
    // if (lib.getMajorVersion() > highestMajor)
    // highestMajor = lib.getMajorVersion();
    // if (lib.isLatestVersion())
    // latestLib = lib;
    // }
    // return latestLib.isMinorVersion() ? latestLib : null;
    // }
    //
    // /**
    // * Deep inspection to assure the new user and user are different.
    // *
    // * @param newUser
    // * @param user
    // */
    // // Interesting utility - where does it belong? NO users
    // public static void checkNewUser(OtmTypeUser newUser, OtmTypeUser user) throws IllegalStateException {
    // // TODO - move this into TestVersionChain
    // if (newUser != null) {
    // // Check TL facet and facet owner
    // // Check listeners
    // OtmObject l = OtmModelElement.get( newUser.getTL() ); // l == newUser
    // if (l != newUser)
    // throw new IllegalStateException( "Invalid listener on new user." );
    //
    // TLFacet tlp = (TLFacet) ((TLProperty) newUser.getTL()).getOwner();
    // if (tlp != ((OtmProperty) newUser).getParent().getTL())
    // throw new IllegalStateException( "Parent facets don't match." );
    //
    // TLFacetOwner tlOE = tlp.getOwningEntity();
    // if (tlOE != newUser.getOwningMember().getTL())
    // throw new IllegalStateException( "Owners don't match." );
    //
    // OtmObject ol = OtmModelElement.get( user.getTL() ); // l == newUser
    // if (ol != user)
    // throw new IllegalStateException( "Invalid listener on user." );
    //
    // TLFacet otlp = (TLFacet) ((TLProperty) user.getTL()).getOwner();
    // if (otlp == tlp)
    // throw new IllegalStateException( "Shared facet." );
    //
    // TLFacetOwner otlOE = otlp.getOwningEntity();
    // if (tlOE == otlOE)
    // throw new IllegalStateException( "Shared owner" );
    //
    // OtmLibrary newLib = newUser.getLibrary();
    // OtmLibraryMember newOwner = newUser.getOwningMember();
    // OtmLibrary userLib = user.getLibrary();
    // OtmLibraryMember userOwner = user.getOwningMember();
    // log.debug( newLib + " " + newOwner + " =? " + userLib + " " + userOwner );
    // if (newOwner == userOwner)
    // throw new IllegalStateException( "Owners of old and new are the same." );
    // }
    // }
    //
    //
    // /**
    // * Create a minor version of all members of the library chain.
    // * <p>
    // * Must be a chain and have a minor version library in the chain.
    // *
    // * @param library
    // * @return
    // */
    // // Interesting utility - where does it belong? 3 users
    // public static OtmLibrary createMinorMembers(OtmLibrary library) {
    // assertTrue( library != null );
    // assertTrue( library.getVersionChain() != null );
    // OtmLibrary major = library.getVersionChain().getMajor();
    // OtmLibrary latestLib = library.getVersionChain().getLatestVersion();
    // assertTrue( major != null );
    // assertTrue( latestLib != null );
    // assertTrue( major != latestLib );
    // OtmModelManager mgr = library.getModelManager();
    // assertTrue( mgr != null );
    // // Get the latest library and make sure we can add properties to the objects
    // assertTrue( "Given: Library in repository must be editable.", latestLib.isEditable() );
    //
    // OtmLibraryMember vlm = null;
    // for (OtmLibraryMember member : mgr.getMembers( latestLib.getVersionChain().getMajor() )) {
    // assertTrue( "This must be chain editable: ", member.getLibrary().isChainEditable() );
    //
    // // Create a new minor version with a new type users or just a minor version if no type users.
    // vlm = member.createMinorVersion( latestLib );
    // log.debug( "Created minor version of " + member + " = " + vlm );
    //
    // // Services are not versioned
    // if (vlm != null) {
    // // Post Checks
    // assertTrue( vlm != null );
    // // FIXME - VWA will have same parent type as member's parent type
    // if (!(vlm instanceof OtmValueWithAttributes) && !(vlm instanceof OtmSimpleObject))
    // assertTrue( vlm.getBaseType() == member );
    // assertTrue( vlm.getName().equals( member.getName() ) );
    // assertTrue( ((LibraryMember) vlm.getTL()).getOwningLibrary() == latestLib.getTL() );
    // assertTrue( vlm.getLibrary() == latestLib );
    //
    // TestLibraryMemberBase.checkOwnership( vlm );
    // TestLibraryMemberBase.checkOwnership( member );
    // }
    // // else
    // // assertTrue( !(member.getTL() instanceof Versioned) );
    // }
    // return latestLib;
    // }
}

