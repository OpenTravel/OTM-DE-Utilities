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

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.schemacompiler.model.TLSimple;

/**
 * Verifies the functions of the <code>UserSettings</code> class.
 */
public class TestOtmSimple extends TestOtmLibraryMemberBase<OtmXsdSimple> {
    private static Logger log = LogManager.getLogger( TestOtmSimple.class );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null, null );
        subject = buildOtm( staticModelManager );
        baseObject = null;
    }

    @Before
    public void beforeTests() {
        staticModelManager.clear();
        staticLib = TestLibrary.buildOtm( staticModelManager );
        subject = buildOtm( staticLib );
    }

    /**
     * {@inheritDoc}
     * <p>
     * Must override the base assumption that object will have children because this is the only library member that
     * does not have children.
     * 
     * @see org.opentravel.model.otmLibraryMembers.TestOtmLibraryMemberBase#testChildrenOwner(org.opentravel.model.OtmChildrenOwner)
     */
    @Override
    public void testChildrenOwner(OtmChildrenOwner otm) {
        // NO-OP
    }

    /** ****************************************************** **/

    /**
     * @param lib
     * @param name
     * @return
     */
    public static OtmSimpleObject buildOtm(OtmLibrary lib, String name) {
        assertTrue( lib.isEditable() );
        OtmSimpleObject s = buildOtm( lib.getModelManager() );
        s.setName( OtmLibraryMemberFactory.getUniqueName( lib, name ) );
        lib.add( s );
        assertTrue( lib.contains( s ) );
        return s;
    }

    // 11/11/2020 - causes NPE if tlLib is managed
    public static OtmSimpleObject buildOtm(OtmModelManager mgr) {
        OtmSimpleObject simple = new OtmSimpleObject( buildTL(), mgr );
        simple.setAssignedType( TestXsdSimple.buildOtm( mgr ) );
        mgr.add( simple );
        return simple;
    }

    /**
     * Create a simple object using library's model manager and add to the library.
     * 
     * @param lib
     * @return
     */
    public static OtmSimpleObject buildOtm(OtmLibrary lib) {
        OtmSimpleObject simple = buildOtm( lib.getModelManager() );
        lib.add( simple );
        assertTrue( "Given", lib.getMembers().contains( simple ) );
        return simple;
    }


    /**
     * Build an TL simple type named "Simple".
     * 
     * @return
     */
    public static TLSimple buildTL() {
        TLSimple simple = new TLSimple();
        simple.setName( "Simple" );
        return simple;
    }


}
