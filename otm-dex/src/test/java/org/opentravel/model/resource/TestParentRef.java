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

package org.opentravel.model.resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestResource;
import org.opentravel.schemacompiler.codegen.impl.QualifiedAction;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.TLResourceParentRef;

import java.util.List;

/**
 * Test class for Parent Reference resource descendants.
 * <p>
 */
public class TestParentRef<L extends TestOtmResourceBase<OtmParentRef>> extends TestOtmResourceBase<OtmParentRef> {
    private static Log log = LogFactory.getLog( TestParentRef.class );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null );
        baseObject = TestBusiness.buildOtm( staticModelManager );
        testResource = TestResource.buildOtm( staticModelManager );

        subject = buildOtm( testResource );
        log.debug( "Before class ran." );
    }

    @Test
    public void testAddParentRef() {
        // Given two resources
        OtmResource p = TestResource.buildFullOtm( "/foo", "Foo", staticModelManager );
        OtmResource r = TestResource.buildFullOtm( "bar", "Bar", staticModelManager );

        // Given one is the parent and one is owner of parent ref
        OtmParentRef pr = r.addParentRef( p );
        check( pr, r, p );
    }

    @Test
    public void testParameterGroup() {
        // TODO
    }

    /**
     * Make assertions against the parent reference.
     * 
     * @param parentRef otm object to check
     * @param owner resource owner of the reference, if null, the reference's owningMember is used.
     * @param parent the resource that is the parent, if null, the reference's parentResource is used.
     */
    public static void check(OtmParentRef parentRef, OtmResource owner, OtmResource parent) {
        // Then - parent ref has correct relationships
        if (owner != null)
            assertTrue( parentRef.getOwningMember() == owner );
        else
            owner = parentRef.getOwningMember();
        if (parent != null)
            assertTrue( parentRef.getParentResource() == parent );
        else
            parent = parentRef.getParentResource();

        // Then - tl model has ResourceParentRef for parent resource
        assertFalse( owner.getTL().getParentRefs().isEmpty() );
        assertTrue( owner.getTL().getParentRefs().contains( parentRef.getTL() ) );
        // Then - tl relationships exist
        assertTrue( parentRef.getTL().getOwner() == owner.getTL() );
        assertTrue( parentRef.getTL().getParentResource() == parent.getTL() );

        // Then - the otm resource has parent ref
        assertTrue( owner.getParentRefs().size() >= 1 );
        assertTrue( owner.getChildren().contains( parentRef ) );

        // Then - codegen utils will find some Qualified actions
        // Qualified = Represents the pairing of zero or more TLResourceParentRefs and a TLAction for the purposes of
        // generating an API specification
        if (parent.isFirstClass())
            for (OtmAction action : owner.getActions()) {
                List<QualifiedAction> qa = ResourceCodegenUtils.getQualifiedActions( action.getTL() );
                assertFalse( qa.isEmpty() );
            }

        // log.debug( "done" );
    }


    public static OtmParentRef buildOtm(OtmResource owner) {
        OtmParentRef pr = new OtmParentRef( buildTL(), owner );
        return pr;
    }

    public static OtmParentRef buildOtm(OtmResource owner, OtmResource parent) {
        // OtmParentRef pr = new OtmParentRef( buildTL(), owner );
        return owner.addParentRef( parent );
        // return pr;
    }

    public static TLResourceParentRef buildTL() {
        TLResourceParentRef tlrpr = new TLResourceParentRef();
        return tlrpr;
    }
}
