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
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexMinorVersionActionManager;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.schemacompiler.version.VersionSchemeException;

/**
 * DeleteLibraryMemberAction
 */
public class TestDeleteLibraryMemberIsEnabled extends TestActionsIsEnabledBase {
    private static Log log = LogFactory.getLog( TestDeleteLibraryMemberIsEnabled.class );

    /**
     */
    public TestDeleteLibraryMemberIsEnabled() {
        super( DexActions.DELETELIBRARYMEMBER );
    }

    @Test
    public void testMembers() {
        super.testMembers();
    }

    @Override
    public void testMember(OtmLibraryMember member) {
        super.testMember( member, true ); // always true for editable members
    }

    @Test
    public void testProperties() {
        super.testProperties();
    }

    @Override
    public void testProperty(OtmProperty property) {
        super.testProperty( property, false ); // always false for non-members
    }

    /**
     * Test cases:
     * <ol>
     * <li>New Member in major - all types
     * <li>New Member in minor - all types
     * </ol>
     * 
     * @throws VersionSchemeException
     * @throws InterruptedException
     */
    @Test
    public void testIsEnabled1() throws VersionSchemeException, InterruptedException {

        // Given - case 1 - all the types of members
        TestLibrary.addOneOfEach( lib );
        assertTrue( "Given: ", !modelManager.getMembers( lib ).isEmpty() );
        DexActionManager fullAM = modelManager.getActionManager( true );
        DexActionManager roAM = modelManager.getActionManager( false );
        DexActionManager minorAM = new DexMinorVersionActionManager( fullAM );

        // Read-only case
        for (OtmLibraryMember member : modelManager.getMembers( lib )) {
            log.debug( "Testing " + member + " in a major library." );
            assertTrue( fullAM.isEnabled( DexActions.DELETELIBRARYMEMBER, member ) );
            // assertTrue( "This would be an error.", !minorAM.isEnabled( DexActions.DELETELIBRARYMEMBER, member ) );
            assertTrue( !roAM.isEnabled( DexActions.DELETELIBRARYMEMBER, member ) );
        }
    }

}
