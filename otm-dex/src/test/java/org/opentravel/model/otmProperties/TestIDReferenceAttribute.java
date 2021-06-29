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

package org.opentravel.model.otmProperties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.otmLibraryMembers.OtmChoiceObject;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestChoice;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeOwner;

/**
 * Test class for ID Reference Attribute Property
 * <p>
 */
public class TestIDReferenceAttribute extends TestOtmPropertiesBase<OtmAttribute<?>> {
    private static Log log = LogFactory.getLog( TestIDReferenceAttribute.class );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null, null );
        baseObject = TestBusiness.buildOtm( staticModelManager );
        // testResource = TestResource.buildOtm( staticModelManager );

        log.debug( "Before class ran." );
    }

    @Test
    public void testNames() {
        OtmIdReferenceAttribute<?> ref = buildOtm();
        OtmChoiceObject refObj = TestChoice.buildOtm( staticModelManager );
        assertTrue( ref != null );
        String name = ref.getName();

        ref.setAssignedType( refObj );
        String name2 = ref.getName();

        assertTrue( "Name must changed when assigned type.", !name2.equals( name ) );
        // false because attributes are initial lower case while objects are upper case
        assertFalse( "Name must NOT start with reference object name.", name2.startsWith( refObj.getName() ) );
        assertTrue( "Must end in Ref", name2.endsWith( "Ref" ) );
    }



    /**
     * **********************************************************************************
     * 
     */
    /**
     * Build an id reference attribute.
     * 
     * @param resource
     * @return
     */
    public static OtmIdReferenceAttribute<?> buildOtm() {
        assert baseObject.getIdFacet() != null;
        return buildFullOtm( baseObject.getIdFacet() );
    }

    public static TLAttribute buildTL(TLAttributeOwner owner) {
        TLAttribute tla = new TLAttribute();
        tla.setOwner( owner );
        tla.setReference( true );
        return tla;
    }

    /**
     * Build an id reference attribute
     * 
     */
    public static OtmIdReferenceAttribute<?> buildFullOtm(OtmPropertyOwner owner) {
        assert owner != null;
        assert owner.getTL() instanceof TLAttributeOwner;
        return new OtmIdReferenceAttribute<TLAttribute>( buildTL( (TLAttributeOwner) owner.getTL() ), owner );
    }

}
