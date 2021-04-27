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

package org.opentravel.dex.actions.enabled;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.OtmProperty;

/**
 * AddPropertyAction
 */
public class TestCopyPropertyIsEnabled extends TestActionsIsEnabledBase {
    private static Log log = LogFactory.getLog( TestCopyPropertyIsEnabled.class );

    /**
     */
    public TestCopyPropertyIsEnabled() {
        super( DexActions.COPYPROPERTY );
    }

    @Test
    public void testMembers() {
        super.testMembers();
    }

    @Override
    public void testMember(OtmLibraryMember member) {
        super.testMember( member, false );
    }

    @Test
    public void testProperties() {
        TestLibrary.addOneOfEach( lib );
        super.testProperties();
    }

    @Override
    public void testProperty(OtmProperty property) {
        super.testProperty( property, property instanceof OtmProperty );

        // Two parameter test
        for (OtmLibraryMember m : lib.getMembers())
            for (OtmPropertyOwner p : m.getDescendantsPropertyOwners())
                if (property instanceof OtmProperty) {
                    super.testProperty( property, p, p.canAdd( property ) );
                } else
                    super.testProperty( property, p, false );
    }

}
