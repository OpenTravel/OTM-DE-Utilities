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

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.TestDexFileHandler;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmSimpleObject;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestChoice;
import org.opentravel.model.otmLibraryMembers.TestCore;
import org.opentravel.model.otmLibraryMembers.TestEnumerationClosed;
import org.opentravel.model.otmLibraryMembers.TestEnumerationOpen;
import org.opentravel.model.otmLibraryMembers.TestOtmSimple;
import org.opentravel.model.otmLibraryMembers.TestResource;
import org.opentravel.model.otmLibraryMembers.TestValueWithAttributes;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.OtmPropertyType;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Verifies the functions of the <code>Otm Model Manager</code>.
 */
// @Ignore
public class TestOtmModelManager_Finds extends AbstractFxTest {
    // public class TestOtmModelManager_Gets extends AbstractFxTest {
    private static Log log = LogFactory.getLog( TestOtmModelManager_Finds.class );

    public static final boolean RUN_HEADLESS = true;
    final int WATCH_TIME = 0; // How long to sleep so we can see what is happening. Can be 0.

    final static String FXID_PROJECTCOMBO = "#projectCombo"; // if .projectCombo that would be css selector
    final static String FILE_TESTOPENTRAVELREPO = "TestOpenTravelRepo.otp";
    final static String FILE_TESTLOCAL = "TestLocalFiles.otp";

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestOtmModelManager_Finds.class );
        repoManager = repositoryManager.get();
    }

    @Test
    public void testFindSubtypesOf_LoadedLibraries() {
        // Given
        OtmModelManager mgr = new OtmModelManager( null, null, null );
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
        TestDexFileHandler.loadAndAddManagedProject( mgr );

        // Then - no NPEs
        for (OtmLibraryMember member : mgr.getMembers()) {
            mgr.findSubtypesOf( member );
        }
    }

    @Test
    public void testFindSubtypesOf() {
        // Given
        OtmModelManager mgr = new OtmModelManager( null, null, null );
        OtmLibrary lib = TestLibrary.buildOtm( mgr );

        // When - one of each type with a base type assigned
        List<OtmLibraryMember> bases = buildSubTypes( mgr, lib );

        // Then - model manager will find sub-types
        for (OtmLibraryMember member : lib.getMembers()) {
            if (member instanceof OtmContextualFacet)
                continue;
            if (member.getBaseType() != null) {
                OtmLibraryMember base = (OtmLibraryMember) member.getBaseType();
                assertTrue( "Must find sub-type.", mgr.findSubtypesOf( base ).contains( member ) );
            }
        }

        // TODO - Extension point and contextual facets
    }

    public static List<OtmLibraryMember> buildSubTypes(OtmModelManager mgr, OtmLibrary lib) {
        List<OtmLibraryMember> bases = new ArrayList<>();
        OtmLibraryMember base = null;
        OtmLibraryMember extension = null;
        OtmObject ret = null;

        if (lib == null)
            return bases;
        if (!lib.isEditable())
            return bases;

        // Simple - does not have base type. Parent type is assignedType.

        // VWA
        base = TestValueWithAttributes.buildOtm( lib, "VWA_Base" );
        extension = TestValueWithAttributes.buildOtm( lib, "VWA_Ex" );
        bases.add( base );
        ret = extension.setBaseType( base );
        assertTrue( ret == base );

        // Open Enum
        base = TestEnumerationOpen.buildOtm( lib, "OE_Base" );
        extension = TestEnumerationOpen.buildOtm( lib, "OE_Ex" );
        bases.add( base );
        ret = extension.setBaseType( base );
        assertTrue( ret == base );

        // Closed Enum
        base = TestEnumerationClosed.buildOtm( lib, "CE_Base" );
        extension = TestEnumerationClosed.buildOtm( lib, "CE_Ex" );
        bases.add( base );
        ret = extension.setBaseType( base );
        assertTrue( ret == base );

        // Core
        base = TestCore.buildOtm( lib, "Cr_Base" );
        extension = TestCore.buildOtm( lib, "Cr_Ex" );
        bases.add( base );
        ret = extension.setBaseType( base );
        assertTrue( ret == base );

        // Choice
        base = TestChoice.buildOtm( lib, "Ch_Base" );
        extension = TestChoice.buildOtm( lib, "Ch_Ex" );
        bases.add( base );
        ret = extension.setBaseType( base );
        assertTrue( ret == base );

        // Business Object
        base = TestBusiness.buildOtm( lib, "Bo_Base" );
        extension = TestBusiness.buildOtm( lib, "Bo_Ex" );
        bases.add( base );
        ret = extension.setBaseType( base );
        assertTrue( ret == base );

        // Resource
        base = TestResource.buildOtm( lib, "R1_Base" );
        extension = TestResource.buildOtm( lib, "R1_Ex" );
        bases.add( base );
        ret = extension.setBaseType( base );
        assertTrue( ret == base );

        // Given - Check
        assertTrue( !bases.isEmpty() );
        for (OtmLibraryMember member : lib.getMembers()) {
            if (member instanceof OtmContextualFacet)
                continue;
            if (member.getBaseType() == null)
                assertTrue( bases.contains( member ) );
            else {
                base = (OtmLibraryMember) member.getBaseType();
                assertTrue( bases.contains( base ) );
            }
        }

        return bases;
    }


    @Test
    public void testFindUsersOf_LoadedLibraries() {
        // Given a project that uses local library files
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
        TestDexFileHandler.loadAndAddManagedProject( mgr );

        // Then
        for (OtmLibraryMember member : mgr.getMembers())
            if (member instanceof OtmTypeProvider)
                mgr.findUsersOf( (OtmTypeProvider) member );

        log.debug( "Tested findUsersOf() on " + mgr.getLibraries().size() + " libraries." );
    }

    @Test
    public void testFindUsersOf() {
        // Given a project that uses local library files
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        OtmLibrary lib = TestLibrary.buildOtm( mgr );

        OtmLibraryMember owner = buildTypeUsers( lib );

        // Then
        for (OtmLibraryMember member : lib.getMembers())
            if (member instanceof OtmTypeProvider) {
                List<OtmLibraryMember> users = mgr.findUsersOf( (OtmTypeProvider) member );
                if (!users.isEmpty())
                    assertTrue( mgr.findUsersOf( (OtmTypeProvider) member ).contains( owner ) );
            }

        log.debug( "Tested findUsersOf() on " + mgr.getLibraries().size() + " libraries." );
    }

    public static OtmLibraryMember buildTypeUsers(OtmLibrary lib) {
        OtmCore core = TestCore.buildOtm( lib );
        OtmFacet<?> facet = core.getSummary();

        // Add type providers
        OtmSimpleObject simple1 = TestOtmSimple.buildOtm( lib, "TheProvider" );

        // Add properties using the type provider
        for (OtmPropertyType type : OtmPropertyType.values()) {
            OtmProperty p = OtmPropertyType.build( type, facet );
            if (p instanceof OtmTypeUser) {
                p.setName( facet.getName() + type.toString() );
                ((OtmTypeUser) p).setAssignedType( simple1 );
                // log.debug( "Created property " + p + " of type " + type.toString() );
            }
        }
        return core;
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

