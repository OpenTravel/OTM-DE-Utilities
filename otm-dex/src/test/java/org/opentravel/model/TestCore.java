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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLProperty;

import java.util.List;

/**
 * Verifies the functions of the <code>UserSettings</code> class.
 */
public class TestCore {
    private static final String CORE_NAME = "TestCore";

    private static Log log = LogFactory.getLog( TestCore.class );

    private static OtmModelManager staticModelManager = null;

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null );
    }

    @Test
    public void testConstructors() {
        OtmCore core = buildOtmCore( staticModelManager );
        assertNotNull( core );
        assertTrue( core.getName().equals( CORE_NAME ) );
        assertTrue( core.getTL() instanceof TLCoreObject );
        assertTrue( core.getOwningMember() == core );

        log.debug( "Done." );
    }


    @Test
    public void testChildrenOwner() {
        OtmChildrenOwner core = buildOtmCore( staticModelManager );
        List<OtmObject> kids = core.getChildren();
        assertTrue( !kids.isEmpty() );

        assertTrue( !core.getChildrenHierarchy().isEmpty() );
        assertTrue( !core.getChildrenTypeProviders().isEmpty() );
        assertTrue( !core.getDescendantsTypeUsers().isEmpty() );
        assertTrue( !core.getDescendantsChildrenOwners().isEmpty() );
        assertTrue( !core.getDescendantsTypeUsers().isEmpty() );
    }

    @Test
    public void testFacets() {
        OtmCore core = buildOtmCore( staticModelManager );

        assertNotNull( core.getSummary() );
        assertNotNull( core.getDetail() );
        assertNotNull( core.getRoles() );
        assertNotNull( core.getSimpleList() );
        assertNotNull( core.getSummaryList() );
        assertNotNull( core.getDetailList() );
    }


    @Test
    public void testInheritance() {}

    public static OtmCore buildOtmCore(OtmModelManager mgr) {
        OtmCore core = new OtmCore( buildTLCoreObject(), mgr );
        assertNotNull( core );
        core.getTL().getSummaryFacet().addAttribute( new TLAttribute() );
        core.getTL().getSummaryFacet().addElement( new TLProperty() );
        // TODO - add value
        assertTrue( core.getChildren().size() > 3 );
        assertTrue( core.getSummary().getChildren().size() == 2 );
        return core;
    }

    public static TLCoreObject buildTLCoreObject() {
        TLCoreObject tlCore = new TLCoreObject();
        tlCore.setName( CORE_NAME );
        return tlCore;
    }
}
