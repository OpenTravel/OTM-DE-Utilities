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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmChoiceObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.TestChoice;
import org.opentravel.model.otmProperties.OtmProperty;

/**
 * NameChangeAction
 * 
 * 
 * if (subject == null || !subject.isRenameable()) return false; if (subject instanceof OtmActionFacet || subject
 * instanceof OtmResource) return subject.isEditable(); // Resource and AF are type users but not name controlled if
 * (subject instanceof OtmTypeUser && ((OtmTypeUser) subject).getAssignedType() != null && ((OtmTypeUser)
 * subject).getAssignedType().isNameControlled()) return false;
 * 
 */
public class TestNameChangeIsEnabled extends TestActionsIsEnabledBase {
    private static Logger log = LogManager.getLogger( TestNameChangeIsEnabled.class );

    private OtmChoiceObject choice;

    /**
     */
    public TestNameChangeIsEnabled() {
        super( DexActions.NAMECHANGE );
        choice = TestChoice.buildOtm( lib, "NameControlledChoice" );
        assertTrue( choice.isNameControlled() );
    }

    @Test
    public void testMembers() {
        super.testMembers();
    }

    // All Enabled
    @Override
    public void testMember(OtmLibraryMember member) {
        // log.debug( "Testing if " + actionEnum + " is enabled for " + member.getObjectTypeName() + " " + member );
        super.testMember( member, true );
    }

    @Test
    public void testProperties() {
        super.testProperties();
    }

    @Override
    public void testProperty(OtmProperty property) {
        log.debug( "Testing if " + actionEnum + " is enabled for " + property.getObjectTypeName() + " " + property );
        if (property.isRenameable()) {
            assertTrue( "Test: ", actionManager.isEnabled( actionEnum, property ) );
            testWithNameControllerTypeProvider( property );
        } else
            log.debug( "Skipping." );

    }

    private void testWithNameControllerTypeProvider(OtmProperty property) {
        OtmTypeUser user = null;
        if (property instanceof OtmTypeUser) {
            user = (OtmTypeUser) property;
            OtmTypeProvider result = user.setAssignedType( choice );
            if (result == choice)
                assertTrue( "Test: ", !actionManager.isEnabled( actionEnum, property ) );
            else
                log.debug( "Skipping - could not assign choice to " + property );
        }
    }
}
