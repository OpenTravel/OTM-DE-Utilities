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

package org.opentravel.model.otmLibraryMembers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;

import java.lang.reflect.InvocationTargetException;

/**
 * test class the otm library member base class.
 */
public class TestLibraryMemberBase {

    private static Log log = LogFactory.getLog( TestLibraryMemberBase.class );

    protected static OtmModelManager staticModelManager = null;
    protected static OtmLibraryMember subject;
    protected static OtmLibraryMember baseObject;

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null );
        log.debug( "Model manager created." );
    }


    @Test
    public void testAddAndRemove() throws ExceptionInInitializerError, InstantiationException, IllegalAccessException,
        NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        OtmModelManager mgr = new OtmModelManager( null, null );
        OtmLibrary lib = mgr.add( new TLLibrary() );

        for (OtmLibraryMemberType type : OtmLibraryMemberType.values()) {
            OtmLibraryMember member = OtmLibraryMemberType.buildMember( type, "Test", mgr );

            // When added
            lib.add( member );
            mgr.add( member );
            // Then - add works
            // assertTrue(lib.contains( member ));
            assertTrue( mgr.contains( member.getTlLM() ) );

            // When deleted
            lib.remove( member );
            mgr.remove( member );
            assertFalse( mgr.contains( member.getTlLM() ) );
            assertFalse( mgr.getMembers().contains( member ) );

            log.debug( "Added and removed: " + member );
        }
    }

    @Test
    public void testEnumFactory() throws ExceptionInInitializerError, InstantiationException, IllegalAccessException,
        NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        for (OtmLibraryMemberType lmType : OtmLibraryMemberType.values()) {
            OtmLibraryMember member = OtmLibraryMemberType.buildMember( lmType, "Hi", staticModelManager );
            assertNotNull( member );
        }
    }

    @Test
    public void testGetLabel() throws ExceptionInInitializerError, InstantiationException, IllegalAccessException,
        NoSuchMethodException, InvocationTargetException {
        for (OtmLibraryMemberType lmType : OtmLibraryMemberType.values()) {
            OtmLibraryMember member = OtmLibraryMemberType.buildMember( lmType, "Hi", staticModelManager );
            assertNotNull( member );

            String label = OtmLibraryMemberType.getLabel( member );
            assertTrue( !label.isEmpty() );
            log.debug( "Label for " + member.getClass().getSimpleName() + " is " + label );
        }

    }
}
