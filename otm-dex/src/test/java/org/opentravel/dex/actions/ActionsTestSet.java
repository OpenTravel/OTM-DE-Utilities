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
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.dex.action.manager.DexMinorVersionActionManager;
import org.opentravel.dex.action.manager.DexReadOnlyActionManager;
import org.opentravel.dex.tasks.DexTaskException;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.model.otmContainers.OtmVersionChain;
import org.opentravel.model.otmContainers.TestProject;
import org.opentravel.model.otmContainers.TestVersionChain;
import org.opentravel.model.otmFacets.OtmQueryFacet;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.version.VersionSchemeException;

/**
 * Create test set for testing actions.
 * <p>
 * caller must extend AbstractFxTest
 */
public class ActionsTestSet {
    private static Log log = LogFactory.getLog( ActionsTestSet.class );

    TestProject.ProjectTestSet pts;

    // public class VersionTestSet {
    public OtmModelManager mgr;
    public DexActionManager fullAM;
    public DexActionManager roAM;
    public DexActionManager minorAM;

    public OtmVersionChain chain;
    public OtmLibrary minor;
    public OtmLibrary major;

    public OtmBusinessObject majorBO;
    public OtmBusinessObject minorBO;
    public OtmQueryFacet majorQF;
    public OtmQueryFacet minorQF;

    public ActionsTestSet(AbstractOTMApplication application) throws DexTaskException, RepositoryException {

        // Create a project
        pts = new TestProject().new ProjectTestSet( application );
        // OtmProject px = pts.create();

        // Given - model manager and its action managers
        mgr = pts.modelMgr;
        fullAM = mgr.getActionManager( true );
        roAM = mgr.getActionManager( false );
        minorAM = new DexMinorVersionActionManager( fullAM );
        assertTrue( "Given", fullAM instanceof DexFullActionManager );
        assertTrue( "Given", minorAM instanceof DexMinorVersionActionManager );
        assertTrue( "Given", roAM instanceof DexReadOnlyActionManager );
    }

    // I don't know how to create new managed libraries without loading them.
    // // Create project and repository managed major library
    // OtmLibrary major = pts.createManagedLibrary();
    //
    // // Promote to final
    // // TODO
    //
    // VersionType type = VersionType.MINOR;
    // VersionLibraryTask task =
    // new VersionLibraryTask( type, major, new RepositoryResultHandler( null ), null, null, mgr );
    // task.doIT();
    // log.debug( task.getErrorMsg() );
    // assertTrue( task.getErrorMsg() == null );
    //
    // log.debug( "Is major editable? " + major.isEditable() );
    // ProjectManager tlPM = pts.p.getTL().getProjectManager();
    // ProjectItem pi = pts.p.getProjectItem( major.getTL() );
    // // Fails - "Unable to obtain lock - only draft repository items can be edited"
    // tlPM.lock( pi );
    // log.debug( "Is major editable? " + major.isEditable() );


    public void loadWithResource() throws InterruptedException, VersionSchemeException {
        // Load versioned project
        OtmProject vProj = pts.loadVersionedProjectWithResource();
        // OtmProject vProj = pts.loadVersionedProject();
        assertTrue( vProj != null );

        minor = TestVersionChain.getMinorInChain( mgr );
        chain = minor.getVersionChain();
        major = chain.getMajor();

        assertTrue( "Given", chain != null );
        assertTrue( "Given", chain.getLatestVersion() == minor );
        assertTrue( "Given", chain.getMajor() == major );
        assertTrue( "Given", major != null );
        assertTrue( "Given", minor != null );
        assertTrue( "Given", minor.isEditable() );

        for (OtmLibrary lib : chain.getLibraries())
            assertTrue( "Given", lib.isValid() );
    }

    public void load() throws InterruptedException, VersionSchemeException {
        // Load versioned project
        // OtmProject vProj = pts.loadVersionedProjectWithResource();
        OtmProject vProj = pts.loadVersionedProject();
        assertTrue( vProj != null );

        minor = TestVersionChain.getMinorInChain( mgr );
        chain = minor.getVersionChain();
        major = chain.getMajor();

        assertTrue( "Given", chain != null );
        assertTrue( "Given", chain.getLatestVersion() == minor );
        assertTrue( "Given", chain.getMajor() == major );
        assertTrue( "Given", major != null );
        assertTrue( "Given", minor != null );
        assertTrue( "Given", minor.isEditable() );

        for (OtmLibrary lib : chain.getLibraries())
            assertTrue( "Given", lib.isValid() );
    }

    // public OtmBusinessObject addBO() {
    // // Major is NOT editable.
    // majorBO = TestBusiness.buildOtm( major, "TestBO1" );
    // minorBO = (OtmBusinessObject) majorBO.createMinorVersion( minor );
    //
    // assertTrue( majorBO instanceof OtmBusinessObject );
    // assertTrue( minorBO instanceof OtmBusinessObject );
    // return minorBO;
    // }
    //
    // OtmQueryFacet addQuery() {
    // assertTrue( majorBO instanceof OtmBusinessObject );
    // assertTrue( minorBO instanceof OtmBusinessObject );
    // String queryName = "Qf1";
    // majorQF = TestQueryFacet.buildOtm( majorBO, queryName );
    // minorQF = TestQueryFacet.buildOtm( minorBO, queryName );
    //
    // assertTrue( majorQF.getContributedObject() == majorBO );
    // assertTrue( minorQF.getContributedObject() == minorBO );
    // return minorQF;
    // }
}

