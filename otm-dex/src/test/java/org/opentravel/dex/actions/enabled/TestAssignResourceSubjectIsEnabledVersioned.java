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

package org.opentravel.dex.actions.enabled;

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.dex.action.manager.DexMinorVersionActionManager;
import org.opentravel.dex.action.manager.DexReadOnlyActionManager;
import org.opentravel.dex.actions.ActionsTestSet;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.dex.actions.resource.AssignResourceSubjectAction;
import org.opentravel.dex.tasks.DexTaskException;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmMajorLibrary;
import org.opentravel.model.otmContainers.OtmVersionChain;
import org.opentravel.model.otmContainers.OtmVersionChainVersioned;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestResource;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * AssignResourceSubjectAction
 */
public class TestAssignResourceSubjectIsEnabledVersioned extends AbstractFxTest {
    private static Log log = LogFactory.getLog( TestAssignResourceSubjectIsEnabledVersioned.class );

    public static final boolean RUN_HEADLESS = true;
    final int WATCH_TIME = 0; // How long to sleep so we can see what is happening. Can be 0.

    // private static OtmModelManager staticModelManager = null;
    static OtmLibrary lib = null;
    static OtmBusinessObject globalBO = null;
    static OtmResource resource;
    static DexActionManager actionMgr;

    @BeforeClass
    public static void beforeClass() throws IOException {
        setupWorkInProcessArea( TestAssignResourceSubjectIsEnabledVersioned.class );
        // repoManager = repositoryManager.get();
        // Prevent java.nio.BufferOverflowException
        System.setProperty( "headless.geometry", "2600x2200-32" );
    }

    /**
     * Newly created unmanaged library.
     */
    @Test
    public void testIsEnabled() {
        // throws VersionSchemeException, RepositoryException, InterruptedException, DexTaskException {
        lib = TestLibrary.buildOtm();
        actionMgr = lib.getActionManager();
        globalBO = TestBusiness.buildOtm( lib, "TestSubject1" );
        resource = TestResource.buildOtm( lib, "Test1Resource" );
        assertTrue( "Given: ", resource.getSubject() == null );

        // True when no subject
        assertTrue( "Then assignment is enabled.", actionMgr.isEnabled( DexActions.ASSIGNSUBJECT, resource ) );

        // True when it has subject
        resource.setSubject( globalBO );
        assertTrue( "Then: assignment is enabled.", actionMgr.isEnabled( DexActions.ASSIGNSUBJECT, resource ) );
    }


    @Test
    public void testIsEnabled_NewInMinor()
        throws VersionSchemeException, RepositoryException, InterruptedException, DexTaskException {

        ActionsTestSet ts = new ActionsTestSet( application );
        ts.loadWithResource();

        List<OtmLibraryMember> members = new ArrayList<OtmLibraryMember>( ts.mgr.getMembers() );
        OtmResource resource = null;
        for (OtmLibraryMember m : members)
            if (m instanceof OtmResource)
                resource = (OtmResource) m;

        // Minor Resource
        OtmLibraryMember minorR = TestResource.buildOtm( ts.minor, "NewMinorResource" );
        assertTrue( "Given", minorR != null );
        assertTrue( "Given", minorR.isEditable() );
        actionMgr = minorR.getActionManager();
        assertTrue( "Given", actionMgr instanceof DexFullActionManager );
        // 6/1/2021 - assertTrue( "Given", actionMgr instanceof DexMinorVersionActionManager );
        boolean enabled = actionMgr.isEnabled( DexActions.ASSIGNSUBJECT, minorR );
        assertTrue( "Minor resource must be enabled.", enabled );
    }

    @Test
    public void testIsEnabled_Minor()
        throws VersionSchemeException, RepositoryException, InterruptedException, DexTaskException {

        ActionsTestSet ts = new ActionsTestSet( application );
        ts.loadWithResource();

        List<OtmLibraryMember> members = new ArrayList<OtmLibraryMember>( ts.mgr.getMembers() );
        OtmResource resource = null;
        for (OtmLibraryMember m : members)
            if (m instanceof OtmResource)
                resource = (OtmResource) m;

        // Minor Resource
        OtmLibraryMember minorR = resource.createMinorVersion( ts.minor );
        assertTrue( "Given", minorR != null );
        assertTrue( "Given", minorR.isEditable() );
        assertTrue( "Given", minorR.getActionManager() instanceof DexMinorVersionActionManager );
        assertTrue( "Given:", !resource.getLibrary().getVersionChain().isNewToChain( resource ) );

        // Given - the subject is latest in chain
        OtmBusinessObject subject = resource.getSubject();
        assertTrue( "Given: subject must be latest in the chain.", subject.isLatestVersion() );

        // When
        boolean enabled = minorR.getActionManager().isEnabled( DexActions.ASSIGNSUBJECT, minorR );
        boolean enabled2 = AssignResourceSubjectAction.isEnabled( minorR );
        // Then
        assertTrue( "Minor resource must NOT be enabled.", !enabled );
        assertTrue( "Minor resource must NOT be enabled.", !enabled2 );
    }

    /**
     * For AssignResourceSubjectAction to be enabled:
     * <li>1. there must be a minor of the subject
     * <li>2. the minor library must be editable
     * <li>3. The resource must be latest minor version
     */
    @Test
    public void testIsEnabled_Major()
        throws VersionSchemeException, RepositoryException, InterruptedException, DexTaskException {

        ActionsTestSet ts = new ActionsTestSet( application );
        ts.loadWithResource();
        List<OtmLibrary> libraries = ts.mgr.getUserLibraries();
        assertTrue( "Given: ", libraries.size() > 1 );
        List<OtmResource> resources = ts.mgr.getResources( false );
        assertTrue( "Given: test set must contain only one resource.", resources.size() == 1 );

        // Given : a resource in major library
        // List<OtmLibraryMember> members = new ArrayList<OtmLibraryMember>( ts.mgr.getMembers() );
        OtmResource resource = resources.get( 0 );
        // for (OtmLibraryMember m : members)
        // if (m instanceof OtmResource)
        // resource = (OtmResource) m;
        assertTrue( "Given", resource != null );
        assertTrue( "Given:", resource.getLibrary().getVersionChain().isNewToChain( resource ) );
        // 6/1/2021 - assertTrue( "Given:", !resource.getLibrary().getVersionChain().isNewToChain( resource ) );

        // Givens
        OtmLibrary rLib = resource.getLibrary();
        assertTrue( "Given", rLib instanceof OtmMajorLibrary );
        OtmVersionChain rChain = resource.getLibrary().getVersionChain();
        assertTrue( "Given", rChain instanceof OtmVersionChainVersioned );
        assertTrue( "Given: chain must contain resource library.", rChain.contains( rLib ) );
        assertTrue( "Given: chain must contain minor library.", rChain.contains( ts.minor ) );
        OtmBusinessObject subject = resource.getSubject();
        assertTrue( "Given: subject must be in major library.", subject.getLibrary() instanceof OtmMajorLibrary );

        assertTrue( "Given: resource library is not editable.", !rLib.isEditable() );
        DexActionManager rAM = resource.getActionManager();
        assertTrue( "Given", rAM instanceof DexReadOnlyActionManager );
        // 6/1/2021 - assertTrue( "Given", rAM instanceof DexMinorVersionActionManager );

        boolean enabled = rAM.isEnabled( DexActions.ASSIGNSUBJECT, resource );
        boolean enabled2 = AssignResourceSubjectAction.isEnabled( resource );
        assertTrue( "Major resource must NOT be enabled.", !rAM.isEnabled( DexActions.ASSIGNSUBJECT, resource ) );
        assertTrue( "Direct call must have same result as action manger call.",
            AssignResourceSubjectAction.isEnabled( resource ) == enabled );

        // TODO - check business logic. What if there is a minor subject?
        // TODO - assure subject has a minor version
        // TODO - test when minor subject version is removed

        // // Minor Resource
        // OtmLibraryMember minorR = resource.createMinorVersion( ts.minor );
        // assertTrue( "Given", minorR != null );
        // assertTrue( "Given", minorR.isEditable() );
        // assertTrue( "Given", minorR.getActionManager() instanceof DexMinorVersionActionManager );
        // enabled = minorR.getActionManager().isEnabled( DexActions.ASSIGNSUBJECT, minorR );
        // assertTrue( "Minor resource must be enabled.", enabled );

        // log.debug( "TODO" );
    }

    /* ********************************************************************** */
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
