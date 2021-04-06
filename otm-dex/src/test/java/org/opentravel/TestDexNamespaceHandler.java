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

package org.opentravel;

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.common.DexNamespaceHandler;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import java.io.IOException;
import java.util.Set;

/**
 * Verifies the functions of the <code>DEX Namespace Handler</code> class.
 */
public class TestDexNamespaceHandler extends AbstractFxTest {
    private static Log log = LogFactory.getLog( TestDexNamespaceHandler.class );

    public static final boolean RUN_HEADLESS = true;
    final int WATCH_TIME = 5000; // How long to sleep so we can see what is happening. Can be 0.

    private static OtmModelManager staticModelManager = null;
    static OtmLibrary lib = null;

    @BeforeClass
    public static void beforeClass() throws IOException {
        setupWorkInProcessArea( TestDexNamespaceHandler.class );
        repoManager = repositoryManager.get();
        // Prevent java.nio.BufferOverflowException
        System.setProperty( "headless.geometry", "2600x2200-32" );

        staticModelManager = new OtmModelManager( new DexFullActionManager( null ), null, null );
        lib = staticModelManager.add( new TLLibrary() );
        lib.getTL().setOwningModel( staticModelManager.getTlModel() );
        lib.getTL().setNamespace( "http://example.com/testNs" );

        // Library must be in TL Model for validation to be accurate.
        // Library must have namespace for simple assignments to work correctly.
        assertTrue( lib.isEditable() );
        assertTrue( lib.getActionManager() instanceof DexFullActionManager );
        assertTrue( lib.getTL().getOwningModel() != null );
        assertTrue( lib.getBaseNamespace() != null );
    }

    @Test
    public void testGetBaseNamespaces() {
        // TODO
    }

    @Test
    public void testGetPrefix() {
        DexNamespaceHandler nsHandler = new DexNamespaceHandler( staticModelManager );
        String ns1 = "http://example.com/nstest/ns1";
        String pf1 = "pf1";
        String name1 = "testName1";
        TestLibrary.buildOtm( staticModelManager, ns1, pf1, name1 );

        Set<String> prefixes = nsHandler.getPrefixes();
        assertTrue( "Given: Must have entry for " + pf1, prefixes.contains( pf1 ) );

        String newPf = nsHandler.getPrefix( ns1 );
        assertTrue( "Must be from namspace.", newPf.equals( pf1 ) );

        newPf = nsHandler.getPrefix( ns1 + "/part2" );
        assertTrue( "Must be different.", !newPf.equals( pf1 ) );
    }

    @Test
    public void testGetPrefixes() {
        DexNamespaceHandler nsHandler = new DexNamespaceHandler( staticModelManager );
        TestDexFileHandler.loadAndAddUnmanagedProject( staticModelManager );
        Set<String> prefixes = nsHandler.getPrefixes();

        assertTrue( "Given: must have entries in set.", !prefixes.isEmpty() );
        assertTrue( "Given: must have entries in set.", prefixes.contains( "ota2" ) );
        assertTrue( "Given: must have entries in set.", prefixes.contains( "xsd" ) );

        String ns1 = "http://example.com/nstest/ns1";
        String pf1 = "prefix1";
        String name1 = "testName1";
        TestLibrary.buildOtm( staticModelManager, ns1, pf1, name1 );

        prefixes = nsHandler.getPrefixes();
        assertTrue( "Must have entry for " + pf1, prefixes.contains( pf1 ) );
    }

    @Test
    public void testFixNamespaceVersion() {
        String ns = "http://example.com/ns1";
        String result = DexNamespaceHandler.fixNamespaceVersion( ns );
        assertTrue( result.endsWith( "/v1" ) );

        ns = "http://example.com/ns1/v3";
        result = DexNamespaceHandler.fixNamespaceVersion( ns );
        assertTrue( result.equals( ns ) );
    }

    /** ********************************************************************** */
    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getApplicationClass()
     */
    @Override
    protected Class<? extends AbstractOTMApplication> getApplicationClass() {
        return ObjectEditorApp.class;
    }

    /**
     * Configure headless/normal mode for TestFX execution.
     */
    static {
        TestFxMode.setHeadless( RUN_HEADLESS );
    }

    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getBackgroundTaskNodeQuery()
     */
    @Override
    protected String getBackgroundTaskNodeQuery() {
        return "#libraryText";
    }

}
