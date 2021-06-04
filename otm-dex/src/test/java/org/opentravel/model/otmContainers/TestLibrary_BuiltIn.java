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
import org.junit.Test;
import org.opentravel.dex.action.manager.DexReadOnlyActionManager;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.TestOtmModelManager;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;

import java.util.List;

/**
 *
 */
public class TestLibrary_BuiltIn {
    private static Log log = LogFactory.getLog( TestLibrary_BuiltIn.class );

    // @BeforeClass
    // public static void setupTests() throws Exception {
    // beforeClassSetup( TestLibrary_BuiltIn.class );
    // }
    private OtmModelManager getModelManager() {
        return TestOtmModelManager.build();
    }

    @Test
    public void testConstructor() {
        List<BuiltInLibrary> tlBuiltIns = getModelManager().getTlModel().getBuiltInLibraries();
        for (AbstractLibrary aLib : tlBuiltIns) {
            OtmLibrary lib = new OtmBuiltInLibrary( (BuiltInLibrary) aLib, getModelManager() );
            assertTrue( "Then: Must have a library.", lib != null );
        }
    }

    @Test
    public void testGetActionManager() {
        for (OtmLibrary lib : getModelManager().getLibraries()) {
            if (lib instanceof OtmBuiltInLibrary)
                assertTrue( "Then: must be read-only manager.",
                    lib.getActionManager() instanceof DexReadOnlyActionManager );
        }
    }

    @Test
    public void testGetActionManager_Member() {
        for (OtmLibrary lib : getModelManager().getLibraries()) {
            if (lib instanceof OtmBuiltInLibrary)
                assertTrue( "Then: must be read-only manager.",
                    lib.getActionManager() instanceof DexReadOnlyActionManager );
        }
    }

    @Test
    public void testGetState() {
        for (OtmLibrary lib : getModelManager().getLibraries()) {
            // RepositoryItemState state;
            if (lib instanceof OtmBuiltInLibrary)
                log.debug( "Library state = " + lib.getState() );
        }
    }
    // @Test
    // public void testGetX() {
    // //
    // }
    // @Test
    // public void testGetX() {
    // //
    // }
    // @Test
    // public void testGetX() {
    // //
    // }
}
