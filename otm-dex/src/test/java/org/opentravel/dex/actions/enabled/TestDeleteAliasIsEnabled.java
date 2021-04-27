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

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.model.otmFacets.OtmAlias;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;

/**
 * DeleteAliasAction
 */
public class TestDeleteAliasIsEnabled extends TestActionsIsEnabledBase {
    private static Log log = LogFactory.getLog( TestDeleteAliasIsEnabled.class );

    /**
     */
    public TestDeleteAliasIsEnabled() {
        super( DexActions.DELETEALIAS );
    }

    @Test
    public void testMembers() {
        super.testMembers();
    }

    @Override
    public void testMember(OtmLibraryMember member) {
        super.testMember( member, false ); // never for the member

        // Add an alias to the member then test
        if (member.getTL() instanceof TLAliasOwner) {
            OtmAlias a = new OtmAlias( new TLAlias(), member );
            assertTrue( "Then - must be enabled.", actionManager.isEnabled( DexActions.DELETEALIAS, a ) );
        }
    }

    @Test
    public void testProperties() {
        super.testProperties();
    }

    @Override
    public void testProperty(OtmProperty property) {
        super.testProperty( property, false ); // always false for non-members
    }

}
