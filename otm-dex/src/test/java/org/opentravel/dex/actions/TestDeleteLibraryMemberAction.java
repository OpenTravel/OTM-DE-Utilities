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

package org.opentravel.dex.actions;

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmFacets.OtmSummaryFacet;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmExtensionPointFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.OtmServiceObject;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestCore;
import org.opentravel.model.otmProperties.OtmElement;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.TestElement;
import org.opentravel.model.otmProperties.TestOtmPropertiesBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Verifies the functions of the <code>delete property action</code> class.
 */
public class TestDeleteLibraryMemberAction {
    private static Logger log = LogManager.getLogger( TestDeleteLibraryMemberAction.class );

    private static OtmModelManager staticModelManager = null;
    static OtmLibrary lib = null;
    static OtmBusinessObject globalBO = null;

    @BeforeClass
    public static void beforeClass() throws IOException {
        lib = TestLibrary.buildOtm();
        staticModelManager = lib.getModelManager();
        assertTrue( lib.isEditable() );
        assertTrue( lib.getActionManager() instanceof DexFullActionManager );

        globalBO = TestBusiness.buildOtm( lib, "GlobalBO" ); // Tested in buildOtm()
    }

    @Test
    public void testDeleteMemberAction() {
        TestLibrary.addOneOfEach( lib );
        assertTrue( "Given: ", !staticModelManager.getMembers( lib ).isEmpty() );
        int memberCount = lib.getMembers().size();

        // Given - a property in a different library with each LM assigned
        OtmLibrary tlib = TestLibrary.buildOtm( staticModelManager );
        assertTrue( tlib.isEditable() );
        OtmCore core = TestCore.buildOtm( tlib );
        OtmSummaryFacet facet = core.getSummary();
        OtmElement<?> e = null;
        ArrayList<OtmElement<?>> elements = new ArrayList<>();

        //
        // Test Deleting
        for (OtmLibraryMember member : staticModelManager.getMembers( lib )) {
            // log.debug( "Checking " + member );
            assertTrue( "Given: ", member.getLibrary() == lib );
            assertTrue( "Given: member must be editable.", member.isEditable() );
            assertTrue( "Delete must be enabled.", DeleteLibraryMemberAction.isEnabled( member ) );
            // Create property with member as the type
            if (member instanceof OtmTypeProvider && !(member instanceof OtmResource)
                && !(member instanceof OtmServiceObject) && !(member instanceof OtmExtensionPointFacet)) {
                e = TestElement.buildOtm( facet );
                e.setAssignedType( (OtmTypeProvider) member );
                e.setName( member.getName() );
                elements.add( e );
                assertTrue( "Given:", e.getName() != null );
            }
            List<OtmLibraryMember> users = member.getWhereUsed();

            // Delete
            OtmLibraryMember result =
                (OtmLibraryMember) member.getActionManager().run( DexActions.DELETELIBRARYMEMBER, member );

            // Then
            assertTrue( "Must return deleted member.", result == member );
            assertTrue( "Library must not have member.", !lib.getMembers().contains( member ) );
            // // 4/28/2020 - dmh
            // // Leave the type assigned. The member object is gone so it will be an error but at least the user
            // // will get some indication of what was assigned.
            // for (OtmLibraryMember user : users) {
            // assertTrue( "Must not use member as assigned type.", !user.getUsedTypes().contains( member ) );
            // }
        }
        // Then - all members are deleted
        assertTrue( "Given: ", staticModelManager.getMembers( lib ).isEmpty() );
        assertTrue( "Given: ", lib.getMembers().isEmpty() );

        //
        // Test Undo
        //
        DexActionManager mgr = lib.getActionManager();
        while (mgr.getQueueSize() > 0) {
            mgr.undo();
        }
        assertTrue( "All members must have been restored.", lib.getMembers().size() == memberCount );
        List<OtmLibraryMember> members = lib.getMembers();
        for (OtmElement<?> t : elements)
            assertTrue( lib.getMembers().contains( t.getAssignedType() ) );
        log.debug( "Tested deleting and un-deleting " + memberCount + " members." );
    }

    @Test
    public void testAction() {
        // Given - a business object with one of each property type
        OtmBusinessObject bo = TestBusiness.buildOtm( lib, "TestOwner" );
        assertTrue( "Given", bo.isEditable() );
        TestOtmPropertiesBase.buildOneOfEach2( bo.getSummary() );
        int initialChildCount = bo.getSummary().getChildren().size();
        assertTrue( "Given", initialChildCount > 0 );

        ArrayList<OtmObject> children = new ArrayList<>( bo.getSummary().getChildren() );
        for (OtmObject obj : children) {
            if (!(obj instanceof OtmProperty))
                continue;
            OtmProperty property = (OtmProperty) obj;
            assertTrue( "Must be enabled.", DeletePropertyAction.isEnabled( property ) );

            DeletePropertyAction action = new DeletePropertyAction();
            action.setSubject( property );
            action.doIt( property );
            List<OtmObject> newKids = bo.getSummary().getChildren();
            assertTrue( "Children array must be smaller.", bo.getSummary().getChildren().size() < initialChildCount );

            action.undoIt();
            assertTrue( "Children array must be initial size.",
                bo.getSummary().getChildren().size() == initialChildCount );
        }
    }
}
