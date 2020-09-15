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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.TestDexFileHandler;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.TestOtmModelManager;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmSimpleObject;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;
import org.opentravel.model.otmLibraryMembers.TestLibraryMemberBase;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import java.util.List;

/**
 * Verifies the functions of the <code>Otm Model Manager</code>.
 */
// @Ignore
public class TestVersionChain extends AbstractFxTest {
    private static Log log = LogFactory.getLog( TestVersionChain.class );

    public static final boolean RUN_HEADLESS = true;
    final int WATCH_TIME = 0; // How long to sleep so we can see what is happening. Can be 0.

    final static String FXID_PROJECTCOMBO = "#projectCombo"; // if .projectCombo that would be css selector
    final static String FILE_TESTOPENTRAVELREPO = "TestOpenTravelRepo.otp";
    final static String FILE_TESTLOCAL = "TestLocalFiles.otp";

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestVersionChain.class );
        repoManager = repositoryManager.get();
    }


    /** ******************************************************************* **/
    @Test
    public void testAddingVersionedProject() throws Exception {

        // Given a project that uses the OpenTravel repository
        OtmModelManager mgr = TestOtmModelManager.buildModelManager( new DexFullActionManager( null ) );
        assertNotNull( mgr.getActionManager( true ) );

        // Load project and get latest library
        if (!TestDexFileHandler.loadVersionProject( mgr ))
            return; // No editable libraries
        OtmLibrary latestLib = getMinorInChain( mgr );

        // Test adding properties to object in latest major
        createMinorMembers( latestLib );

        // Test deleting the new members
        List<OtmLibraryMember> minors = mgr.getMembers( latestLib );
        for (OtmLibraryMember m : minors) {
            log.debug( "Deleting " + m );
            latestLib.delete( m );
        }

        // Assure other members still have their properties ownership set correctly.
        for (OtmLibraryMember m : mgr.getMembers())
            TestLibraryMemberBase.checkOwnership( m );
    }

    /**
     * Get the latest library in the version chain with the highest major version number.
     * 
     * @param mgr
     * @return a minor library or null
     * @throws VersionSchemeException
     */
    public static OtmLibrary getMinorInChain(OtmModelManager mgr) throws VersionSchemeException {
        OtmLibrary latestLib = null;
        int highestMajor = 0;
        for (OtmLibrary lib : mgr.getLibraries()) {
            if (lib.isBuiltIn())
                continue;
            // log.debug( "Library " + lib + " opened." );
            // log.debug( "Is latest? " + lib.isLatestVersion() );
            // log.debug( "Is minor? " + lib.isMinorVersion() );
            // log.debug( "Version number " + lib.getMajorVersion() + " " + lib.getMinorVersion() );
            // log.debug( "Is editable? " + lib.isEditable() );
            // log.debug( "What action manager? " + lib.getActionManager().getClass().getSimpleName() );
            // log.debug( "Version chain contains " + mgr.getVersionChain( lib ).size() + " libraries" );
            // log.debug( "" );

            if (lib.getMajorVersion() > highestMajor)
                highestMajor = lib.getMajorVersion();
            if (lib.isLatestVersion())
                latestLib = lib;
        }
        return latestLib.isMinorVersion() ? latestLib : null;
    }

    /**
     * Deep inspection to assure the new user and user are different.
     * 
     * @param newUser
     * @param user
     */
    public static void checkNewUser(OtmTypeUser newUser, OtmTypeUser user) throws IllegalStateException {
        // TODO - move this into TestVersionChain
        if (newUser != null) {
            // Check TL facet and facet owner
            // Check listeners
            OtmObject l = OtmModelElement.get( newUser.getTL() ); // l == newUser
            if (l != newUser)
                throw new IllegalStateException( "Invalid listener on new user." );

            TLFacet tlp = (TLFacet) ((TLProperty) newUser.getTL()).getOwner();
            if (tlp != ((OtmProperty) newUser).getParent().getTL())
                throw new IllegalStateException( "Parent facets don't match." );

            TLFacetOwner tlOE = tlp.getOwningEntity();
            if (tlOE != newUser.getOwningMember().getTL())
                throw new IllegalStateException( "Owners don't match." );

            OtmObject ol = OtmModelElement.get( user.getTL() ); // l == newUser
            if (ol != user)
                throw new IllegalStateException( "Invalid listener on  user." );

            TLFacet otlp = (TLFacet) ((TLProperty) user.getTL()).getOwner();
            if (otlp == tlp)
                throw new IllegalStateException( "Shared facet." );

            TLFacetOwner otlOE = otlp.getOwningEntity();
            if (tlOE == otlOE)
                throw new IllegalStateException( "Shared owner" );

            OtmLibrary newLib = newUser.getLibrary();
            OtmLibraryMember newOwner = newUser.getOwningMember();
            OtmLibrary userLib = user.getLibrary();
            OtmLibraryMember userOwner = user.getOwningMember();
            log.debug( newLib + " " + newOwner + " =? " + userLib + " " + userOwner );
            if (newOwner == userOwner)
                throw new IllegalStateException( "Owners of old and new are the same." );
        }
    }


    /**
     * Create a minor version of all members of the library chain.
     * <p>
     * Must be a chain and have a minor version.
     * 
     * @param library
     * @return
     */
    public static OtmLibrary createMinorMembers(OtmLibrary library) {
        assertTrue( library != null );
        assertTrue( library.getVersionChain() != null );
        OtmLibrary major = library.getVersionChain().getMajor();
        OtmLibrary latestLib = library.getVersionChain().getLatestVersion();
        assertTrue( major != null );
        assertTrue( latestLib != null );
        assertTrue( major != latestLib );
        OtmModelManager mgr = library.getModelManager();
        assertTrue( mgr != null );
        // Get the latest library and make sure we can add properties to the objects
        assertTrue( "Given: Library in repository must be editable.", latestLib.isEditable() );

        OtmLibraryMember vlm = null;
        for (OtmLibraryMember member : mgr.getMembers( latestLib.getVersionChain().getMajor() )) {
            assertTrue( "This must be chain editable: ", member.getLibrary().isChainEditable() );

            // Create a new minor version with a new type users or just a minor version if no type users.
            vlm = member.createMinorVersion( latestLib );
            log.debug( "Created minor version of " + member + " = " + vlm );

            // Services are not versioned
            if (vlm != null) {
                // Post Checks
                assertTrue( vlm != null );
                // FIXME - VWA will have same parent type as member's parent type
                if (!(vlm instanceof OtmValueWithAttributes) && !(vlm instanceof OtmSimpleObject))
                    assertTrue( vlm.getBaseType() == member );
                assertTrue( vlm.getName().equals( member.getName() ) );
                assertTrue( ((LibraryMember) vlm.getTL()).getOwningLibrary() == latestLib.getTL() );
                assertTrue( vlm.getLibrary() == latestLib );

                TestLibraryMemberBase.checkOwnership( vlm );
                TestLibraryMemberBase.checkOwnership( member );
            }
            // else
            // assertTrue( !(member.getTL() instanceof Versioned) );
        }
        return latestLib;
    }



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

