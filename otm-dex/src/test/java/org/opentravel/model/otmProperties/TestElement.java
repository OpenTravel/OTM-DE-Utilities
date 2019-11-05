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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;

/**
 * Test class for Property Type Elements
 * <p>
 */
public class TestElement extends TestOtmPropertiesBase<OtmElement<?>> {
    private static Log log = LogFactory.getLog( TestElement.class );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null );
        baseObject = TestBusiness.buildOtm( staticModelManager );
        // testResource = TestResource.buildOtm( staticModelManager );

        log.debug( "Before class ran." );
    }

    @Test
    public void testChildren() {}



    /**
     * **********************************************************************************
     * 
     */
    /**
     * Build an element.
     * 
     * @param parent is a TLPropertyOwner
     * @return
     */
    public static OtmElement<?> buildOtm(OtmPropertyOwner parent) {
        assert (parent.getTL() instanceof TLPropertyOwner);
        return new OtmElement<TLProperty>( buildTL( (TLPropertyOwner) parent.getTL() ), parent );
    }

    public static TLProperty buildTL(TLPropertyOwner owner) {
        TLProperty tlp = new TLProperty();
        owner.addElement( tlp );
        return tlp;
    }


}
