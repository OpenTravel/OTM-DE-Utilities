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
import org.opentravel.AbstractDexTest;
import org.opentravel.TestDexFileHandler;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestCore;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;

import java.util.List;

/**
 * Verifies the functions of the <code>DEX Namespace Handler</code> class.
 */
public class TestOtmModelMembersManager extends AbstractDexTest {
    private static Log log = LogFactory.getLog( TestOtmModelMembersManager.class );

    @BeforeClass
    public static void beforeClass() throws Exception {
        beforeClassSetup( TestOtmModelMembersManager.class );
    }

    @Test
    public void testAdd() {
        // TODO
    }

    @Test
    public void testRemove() {
        // TODO
    }

    private static final int ITERATIONS = 10000;

    @Test
    public void testContains_TL() {
        OtmModelManager mgr = getModelManager();
        OtmLibrary lib = TestLibrary.buildOtm( mgr );
        TestLibrary.addOneOfEach( lib );
        OtmModelMembersManager mbrMgr = mgr.getOtmMembersManager();
        OtmCore subject = TestCore.buildOtm( lib );
        TLCoreObject subjectTL = subject.getTL();
        OtmBusinessObject bo = TestBusiness.buildOtm( lib, "JUNK" );

        assertTrue( mbrMgr.contains( subject.getTL() ) );
        // Test over x iterations
        int i = ITERATIONS;
        do {
            mbrMgr.add( bo );
            mbrMgr.contains( subjectTL );
            mbrMgr.remove( bo );
        } while (--i > 0);
    }

    @Test
    public void testContains_OTM() {
        OtmModelManager mgr = getModelManager();
        OtmLibrary lib = TestLibrary.buildOtm( mgr );
        TestLibrary.addOneOfEach( lib );
        OtmModelMembersManager mbrMgr = mgr.getOtmMembersManager();
        OtmCore subject = TestCore.buildOtm( lib );
        TLCoreObject subjectTL = subject.getTL();
        OtmBusinessObject bo = TestBusiness.buildOtm( lib, "JUNK" );

        assertTrue( mbrMgr.contains( subject ) );
        // Test over x iterations
        int i = ITERATIONS;
        do {
            mbrMgr.add( bo );
            mbrMgr.contains( subject );
            mbrMgr.remove( bo );
        } while (--i > 0);
    }

    @Test
    public void testGetMember_TL() {
        OtmModelManager mgr = getModelManager();
        OtmLibrary lib = TestLibrary.buildOtm( mgr );
        TestLibrary.addOneOfEach( lib );
        OtmModelMembersManager mbrMgr = mgr.getOtmMembersManager();
        OtmCore subject = TestCore.buildOtm( lib );
        TLCoreObject subjectTL = subject.getTL();
        OtmBusinessObject bo = TestBusiness.buildOtm( lib, "JUNK" );

        assertTrue( mbrMgr.contains( subject ) );
        // Test over x iterations
        int i = ITERATIONS;
        do {
            mbrMgr.add( bo );
            OtmObject s = mbrMgr.getMember( subjectTL );
            mbrMgr.remove( bo );
        } while (--i > 0);
    }

    @Test
    public void testGetMember_ModelElement() {
        OtmModelManager mgr = getModelManager();
        OtmLibrary lib = TestLibrary.buildOtm( mgr );
        TestLibrary.addOneOfEach( lib );
        OtmModelMembersManager mbrMgr = mgr.getOtmMembersManager();
        OtmCore subject = TestCore.buildOtm( lib );
        TLCoreObject subjectTL = subject.getTL();
        OtmBusinessObject bo = TestBusiness.buildOtm( lib, "JUNK" );

        assertTrue( mbrMgr.contains( subject ) );
        // Test over x iterations
        int i = ITERATIONS;
        do {
            mbrMgr.add( bo );
            OtmObject s = OtmModelElement.get( subjectTL );
            mbrMgr.remove( bo );
        } while (--i > 0);
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

        assertTrue( !mgr.getMembers().isEmpty() );

        for (OtmLibrary lib : mgr.getLibraries())
            assertTrue( !mgr.getMembers( lib ).isEmpty() );

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
     * getMember(String) getMember(TLModelElement)
     */
    @Test
    public void testGetMember() {
        // Given a project that uses local library files
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
        TestDexFileHandler.loadAndAddManagedProject( mgr );
        TLModel tlModel = mgr.getTlModel();
        assertTrue( tlModel != null );

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
}
