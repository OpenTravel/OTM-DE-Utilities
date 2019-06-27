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
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionOwner;

import java.util.List;

/**
 * Base test class for all otm objects.
 */
public abstract class TestOtmLibraryMemberBase<L extends OtmLibraryMember> {
    // private static final String CORE_NAME = "TestCore";

    private static Log log = LogFactory.getLog( TestOtmLibraryMemberBase.class );

    protected static OtmModelManager staticModelManager = null;

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null );
        log.debug( "Model manager created." );
    }

    /**
     * Assure:
     * <ol>
     * <li>Must be its own owner
     * <li>Must have identity listener
     * </ol>
     * 
     * @param otm
     */
    public void testConstructors(L otm) {
        assertNotNull( otm );
        assertTrue( "Must be its own owner", otm.getOwningMember() == otm );
        assertTrue( "Must have identity listner.", OtmModelElement.get( otm.getTL() ) == otm );
        log.debug( "Constuctor OK." );
    }


    public void testChildrenOwner(L otm) {
        if (otm instanceof OtmChildrenOwner) {
            OtmChildrenOwner co = (OtmChildrenOwner) otm;
            List<OtmObject> kids = co.getChildren();
            assertTrue( !kids.isEmpty() );

            assertTrue( !co.getChildrenHierarchy().isEmpty() );
            assertTrue( !co.getChildrenTypeProviders().isEmpty() );
            assertTrue( !co.getDescendantsTypeUsers().isEmpty() );
            assertTrue( !co.getDescendantsChildrenOwners().isEmpty() );
            assertTrue( !co.getDescendantsTypeUsers().isEmpty() );
            log.debug( "Children owner methods OK." );
        }
    }

    public void testTypeUser(OtmTypeUser tu) {

        assertTrue( tu.getAssignedType() != null );
        assertTrue( tu.getAssignedTLType() != null );
        assertTrue( tu.getAssignedTLType() == tu.getAssignedType().getTL() );

        // FIXME - this will fail because it needs to know the library
        // assertTrue( !core.assignedTypeProperty().get().isEmpty() );
    }


    public void testInheritance(L otm) {
        OtmObject base = otm.getBaseType();

        // If there is a base type, assure the children are inherited
        if (base instanceof OtmChildrenOwner && ((OtmChildrenOwner) base).getChildren().isEmpty()) {
            assertTrue( !((OtmChildrenOwner) otm).getInheritedChildren().isEmpty() );
            ((OtmChildrenOwner) otm).getInheritedChildren().forEach( i -> assertTrue( i.isInherited() ) );
        }
    }

    /**
     * Extend the base object with the extension object. Return the extension object if successful. Example:
     * extendObject(animals, pig);
     * 
     * @param base
     * @param extension object to become a sub-type of base
     * @return extension if successful or null
     */
    public L extendObject(L base, L extension) {
        if (base.getTL() instanceof NamedEntity && extension.getTL() instanceof TLExtensionOwner) {
            TLExtension tlex = new TLExtension();
            tlex.setExtendsEntity( (NamedEntity) base.getTL() );
            ((TLExtensionOwner) extension.getTL()).setExtension( tlex );

            assertTrue( extension.getBaseType() != null );
            assertTrue( extension.getBaseType() == base );
            return extension;
        }
        return null;
    }

    // public static O buildOtm(OtmModelManager mgr) {}
    // public static TLModelElement buildTL() {}
}
