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
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.TestLibraryMemberBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class for Property Type Elements
 * <p>
 */
public class TestOtmTypeProviderInterface {
    private static Log log = LogFactory.getLog( TestOtmTypeProviderInterface.class );

    @BeforeClass
    public static void beforeClass() {
        // staticModelManager = new OtmModelManager( null, null, null );
        // baseObject = TestBusiness.buildOtm( staticModelManager );
        // // testResource = TestResource.buildOtm( staticModelManager );
        //
        log.debug( "Before class ran." );
    }

    @Test
    public void test_assignedTypeProperty() {}


    public static void checkTypeAssignment(List<OtmTypeProvider> users, OtmTypeProvider provider) {}

    /**
     * Build one of each type user in the passed library. Properties will be owned by the created core object.
     * 
     * @param lib
     * @return
     */
    public static List<OtmTypeProvider> buildOneOfEach(OtmLibrary lib) {
        List<OtmTypeProvider> typeProviders = new ArrayList<>();

        TestLibraryMemberBase.buildOneOfEachWithProperties( lib );
        // TODO
        // Alias and AliasFacet
        // XSD element and simple

        // Done
        // Facets
        // Complex Objects
        // Role Enumeration
        // Simple Objects
        // VWA
        // Resource

        for (OtmLibraryMember m : lib.getMembers()) {
            if (m instanceof OtmTypeProvider)
                typeProviders.add( (OtmTypeProvider) m );
            typeProviders.addAll( m.getDescendantsTypeProviders() );
        }

        return typeProviders;

    }


}
