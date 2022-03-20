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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.schemacompiler.model.TLAliasOwner;

/**
 * AddAliasAction
 */
public class TestAddAliasIsEnabled extends TestActionsIsEnabledBase {
    private static Logger log = LogManager.getLogger( TestAddAliasIsEnabled.class );

    /**
     */
    public TestAddAliasIsEnabled() {
        super( DexActions.ADDALIAS );
    }

    @Test
    public void testMembers() {
        super.testMembers();
    }

    @Override
    public void testMember(OtmLibraryMember member) {
        super.testMember( member, member.getTL() instanceof TLAliasOwner && !(member instanceof OtmContextualFacet) );
    }

    @Test
    public void testProperties() {
        super.testProperties();
    }

    @Override
    public void testProperty(OtmProperty property) {
        super.testProperty( property, false ); // All should return false, not enabled.
    }

}
