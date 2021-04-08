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

package org.opentravel.dex.actions;

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexMinorVersionActionManager;
import org.opentravel.dex.tasks.DexTaskException;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Verifies the functions of the <code>UserSettings</code> class.
 */
public class TestAssignResourceSubjectActionVersioned extends AbstractFxTest {
    private static Log log = LogFactory.getLog( TestAssignResourceSubjectAction.class );

    public static final boolean RUN_HEADLESS = true;
    final int WATCH_TIME = 0; // How long to sleep so we can see what is happening. Can be 0.

    private static OtmModelManager staticModelManager = null;
    static OtmLibrary lib = null;
    static OtmBusinessObject globalBO = null;
    static OtmResource resource;
    static DexActionManager actionMgr;

    @BeforeClass
    public static void beforeClass() throws IOException {
        setupWorkInProcessArea( TestAssignResourceSubjectActionVersioned.class );
        repoManager = repositoryManager.get();
        // Prevent java.nio.BufferOverflowException
        System.setProperty( "headless.geometry", "2600x2200-32" );

        // lib = TestLibrary.buildOtm();
        // staticModelManager = lib.getModelManager();
        // actionMgr = lib.getActionManager();
        // assertTrue( lib.isEditable() );
        // assertTrue( actionMgr instanceof DexFullActionManager );
        // assertNotNull( staticModelManager );

        // resource = TestResource.buildFullOtm( "TheObject", "theSubject", staticModelManager );
        // lib.add( resource );
        // assertNotNull( resource );
        // assertTrue( resource.isEditable() );
        //
        // globalBO = TestBusiness.buildOtm( lib, "GlobalBO" ); // Tested in buildOtm()

    }

    /**
     * For AssignResourceSubjectAction to be enabled:
     * <li>1. there must be a minor of the subject
     * <li>2. the minor library must be editable
     * <li>3. The resource must be latest minor version
     */
    @Test
    public void testIsEnabled()
        throws VersionSchemeException, RepositoryException, InterruptedException, DexTaskException {

        ActionsTestSet ts = new ActionsTestSet( application );
        ts.loadWithResource();

        List<OtmLibraryMember> members = new ArrayList<OtmLibraryMember>( ts.mgr.getMembers() );
        OtmResource resource = null;
        for (OtmLibraryMember m : members)
            if (m instanceof OtmResource)
                resource = (OtmResource) m;

        // Major resource
        assertTrue( "Given", resource != null );
        assertTrue( "Given", resource.getLibrary().isMajorVersion() );
        log.debug( "Resource found. Subject = " + resource.getSubject() );
        DexActionManager rAM = resource.getActionManager();
        assertTrue( "Given", rAM instanceof DexMinorVersionActionManager );
        boolean enabled = rAM.isEnabled( DexActions.ASSIGNSUBJECT, resource );
        assertTrue( "Major resource must NOT be enabled.", !rAM.isEnabled( DexActions.ASSIGNSUBJECT, resource ) );

        // TODO - assure subject has a minor version
        // TODO - test when minor subject version is removed

        // Minor Resource
        OtmLibraryMember minorR = resource.createMinorVersion( ts.minor );
        assertTrue( "Given", minorR != null );
        assertTrue( "Given", minorR.isEditable() );
        assertTrue( "Given", minorR.getActionManager() instanceof DexMinorVersionActionManager );
        enabled = minorR.getActionManager().isEnabled( DexActions.ASSIGNSUBJECT, minorR );
        assertTrue( "Minor resource must be enabled.", enabled );

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
