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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;

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

    public static OtmParentRef buildOtm(OtmResource testResource) {
        OtmParentRef af = new OtmParentRef( buildTL(), testResource );
        return af;
    }

    public static TLResourceParentRef buildTL() {
        TLResourceParentRef tlaf = new TLResourceParentRef();
        return tlaf;
    }
}
