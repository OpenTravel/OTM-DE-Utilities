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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.TestDexFileHandler;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.dex.action.manager.DexMinorVersionActionManager;
import org.opentravel.dex.action.manager.DexReadOnlyActionManager;
import org.opentravel.model.otmContainers.OtmDomain;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmXsdSimple;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import java.util.Collection;
import java.util.List;

/**
 * Verifies the functions of the <code>Otm Model Manager</code>.
 */
// @Ignore
public class TestOtmModelManager_Gets extends AbstractFxTest {
    // public class TestOtmModelManager_Gets extends AbstractFxTest {
    private static Log log = LogFactory.getLog( TestOtmModelManager_Gets.class );

    public static final boolean RUN_HEADLESS = true;
    final int WATCH_TIME = 0; // How long to sleep so we can see what is happening. Can be 0.

    final static String FXID_PROJECTCOMBO = "#projectCombo"; // if .projectCombo that would be css selector
    final static String FILE_TESTOPENTRAVELREPO = "TestOpenTravelRepo.otp";
    final static String FILE_TESTLOCAL = "TestLocalFiles.otp";

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestOtmModelManager_Gets.class );
        repoManager = repositoryManager.get();
    }

    @Test
    public void testGetPredefinedTypes() {
        // Given
        OtmModelManager mgr = new OtmModelManager( null, null, null );
        mgr.addBuiltInLibraries( new TLModel() );

        // Then - id and empty will be non-null
        OtmXsdSimple id = mgr.getIdType();
        assertNotNull( id );
        OtmXsdSimple empty = mgr.getEmptyType();
        assertNotNull( empty );
    }

    @Test
    public void getDomains() {
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        String base1 = "http://example.com/bns1";
        String base2 = "http://example.com/bns2";
        String subBase1 = "http://example.com/bns1/sub1";
        String subBase2 = "http://example.com/bns2/sub2";

        // When - no unmanaged user libraries
        List<OtmDomain> domains = mgr.getDomains();
        assertTrue( domains.isEmpty() );
        assertTrue( "Must not be found.", mgr.getDomain( base1 ) == null );
        //
        // When - unmanaged library added
        OtmLibrary lib1 = TestLibrary.buildOtm( mgr, base1, "b1", "Base1a" );
        // Then
        assertTrue( !domains.isEmpty() );
        assertTrue( "Must be found.", mgr.getDomain( base1 ) != null );
        assertTrue( "Must be found.", mgr.getDomain( base1 ).getBaseNamespace().equals( base1 ) );

        // When - second domain created
        OtmLibrary lib2 = TestLibrary.buildOtm( mgr, base2, "b2", "Base2" );
        assertTrue( "Must be found.", mgr.getDomain( base1 ) != null );
        assertTrue( "Must be found.", mgr.getDomain( base1 ).getBaseNamespace().equals( base1 ) );
        assertTrue( "Must be found.", mgr.getDomain( base2 ) != null );
        assertTrue( "Must be found.", mgr.getDomain( base2 ).getBaseNamespace().equals( base2 ) );

        // When - sub-domain added to domain 1
        OtmLibrary lib1s1 = TestLibrary.buildOtm( mgr, subBase1, "sb1", "SubBase1" );
        assertTrue( "Must be found.", mgr.getDomain( base1 ) != null );
        assertTrue( "Must be found.", mgr.getDomain( base1 ).getBaseNamespace().equals( base1 ) );
        assertTrue( "Must be found.", mgr.getDomain( subBase1 ) != null );
        assertTrue( "Must be found.", mgr.getDomain( subBase1 ).getBaseNamespace().equals( subBase1 ) );
    }

    /**
     * get(abstractLibrary) get(otmLibrary) get(String) get(TLLibrary)
     */
    @Test
    public void testGets() {
        // Given a project that uses local library files
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
        TestDexFileHandler.loadAndAddManagedProject( mgr );
        TLModel tlModel = mgr.getTlModel();
        assertNotNull( tlModel );

        // Then
        for (AbstractLibrary absLibrary : tlModel.getAllLibraries()) {
            assertTrue( mgr.get( absLibrary ) instanceof OtmLibrary );

            if (absLibrary instanceof TLLibrary)
                assertTrue( mgr.get( (TLLibrary) absLibrary ) instanceof OtmLibrary );

            String ns = absLibrary.getNamespace();
            String fullName = ns + "/" + absLibrary.getName();
            assertTrue( mgr.get( fullName ) instanceof OtmLibrary );
        }

        for (OtmLibrary library : mgr.getLibraries())
            assertTrue( mgr.get( library ) instanceof AbstractLibrary );

        log.debug( "Tested get() on " + mgr.getLibraries().size() + " libraries." );
    }


    /**
     * Get Tests
     * 
     * getActionManager(true/false) getMinorActionManager(true/false)
     */
    @Test
    public void testGetActionManagers() {
        // Given a project that uses local library files
        OtmModelManager mgr = new OtmModelManager( new DexFullActionManager( null ), repoManager, null );
        DexActionManager actionMgr = null;

        // This is independent of libraries.
        actionMgr = mgr.getActionManager( true );
        assertTrue( actionMgr instanceof DexFullActionManager );
        actionMgr = mgr.getActionManager( false );
        assertTrue( actionMgr instanceof DexReadOnlyActionManager );

        actionMgr = mgr.getMinorActionManager( true );
        assertTrue( actionMgr instanceof DexMinorVersionActionManager );
        actionMgr = mgr.getMinorActionManager( false );
        assertTrue( actionMgr instanceof DexReadOnlyActionManager );
    }

    /**
     * getEditableLibraries() getLibraries() getUserLibraries()
     */
    @Test
    public void testGetLibraries() {
        // Given library files
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
        TestDexFileHandler.loadAndAddManagedProject( mgr );

        // Then
        assertFalse( mgr.getLibraries().isEmpty() );
        assertFalse( mgr.getEditableLibraries().isEmpty() );
        assertFalse( mgr.getUserLibraries().isEmpty() );

        for (OtmLibrary library : mgr.getLibraries())
            assertTrue( mgr.get( library ) instanceof AbstractLibrary );

        for (OtmLibrary library : mgr.getEditableLibraries())
            assertTrue( mgr.get( library ) instanceof TLLibrary );

        for (OtmLibrary library : mgr.getUserLibraries())
            assertTrue( mgr.get( library ) instanceof TLLibrary );
    }

    /**
     * getMember(String) getMember(TLModelElement)
     */
    @Test
    public void testGetMember() {
        // Given a project that uses local library files
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
        TestDexFileHandler.loadAndAddManagedProject( mgr );
        TLModel tlModel = mgr.getTlModel();
        assertNotNull( tlModel );

        for (AbstractLibrary absLibrary : tlModel.getUserDefinedLibraries()) {
            for (LibraryMember namedMember : absLibrary.getNamedMembers()) {
                // get member with string
                assertTrue( namedMember.getOwningLibrary() != null );
                assertTrue( namedMember.getOwningLibrary().getPrefix() != null );
                assertTrue( namedMember.getLocalName() != null );
                String nameWithPrefix = namedMember.getOwningLibrary().getPrefix() + ":" + namedMember.getLocalName();
                assertTrue( mgr.getMember( nameWithPrefix ) instanceof OtmLibraryMember );

                // get member with tl model element
                assertTrue( namedMember instanceof TLModelElement );
                assertTrue( mgr.getMember( (TLModelElement) namedMember ) instanceof OtmLibraryMember );
            }
        }
    }

    /**
     * getMembers() getMembers(OtmLibrary) getMembers(OtmLibraryMember)
     */
    @Test
    public void testGetMembers() {
        // Given a project that uses local library files
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
        TestDexFileHandler.loadAndAddManagedProject( mgr );

        assertFalse( mgr.getMembers().isEmpty() );

        for (OtmLibrary lib : mgr.getLibraries())
            assertFalse( mgr.getMembers( lib ).isEmpty() );

        // Test name matching get
        for (OtmLibrary lib : mgr.getLibraries())
            for (OtmLibraryMember otm : lib.getMembers()) {
                List<OtmLibraryMember> matches = mgr.getMembers( otm );
                assertTrue( matches != null );
                if (!matches.isEmpty())
                    log.debug( "Match found: " + matches );
            }
    }

    /**
     * getProjectManager() getProject(String) getProjects() getProjects(AbstractLibrary) getUserProjects()
     */
    @Test
    public void testGetProjects() {
        // Given a project that uses local library files
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
        TestDexFileHandler.loadAndAddManagedProject( mgr );
        TLModel tlModel = mgr.getTlModel();
        assertNotNull( tlModel );

        assertTrue( mgr.getProjectManager() instanceof ProjectManager );

        Collection<OtmProject> projects = mgr.getProjects();
        assertFalse( projects.isEmpty() );

        for (OtmProject p : projects)
            assertTrue( mgr.getOtmProjectManager().getProject( p.getName() ) == p );

        for (AbstractLibrary aLib : mgr.getTlModel().getAllLibraries())
            assertFalse( mgr.getOtmProjectManager().getProjects( aLib ).isEmpty() );
        for (OtmLibrary lib : mgr.getLibraries())
            assertFalse( mgr.getOtmProjectManager().getProjects( lib.getTL() ).isEmpty() );

        assertFalse( mgr.getProjects().isEmpty() );
    }

    /**
     * getBaseNamespaces()
     * 
     * getUserSettings()
     * 
     * getLibraryChain(string) getVersionChain(OtmLibrary) getVersionChainFactory()
     * 
     */
    @Test
    public void testGetOther() {
        // Given a project that uses local library files
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
        TestDexFileHandler.loadAndAddManagedProject( mgr );
        TLModel tlModel = mgr.getTlModel();
        assertNotNull( tlModel );

        assertFalse( mgr.getBaseNamespaces().isEmpty() );

        // Running headless
        assertTrue( mgr.getUserSettings() == null );

        for (OtmLibrary lib : mgr.getLibraries())
            assertFalse( mgr.getVersionChain( lib ) == null );
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

