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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.action.manager.DexWizardActionManager;
import org.opentravel.dex.controllers.popup.DexPopupControllerBase.Results;
import org.opentravel.dex.controllers.popup.MemberDetailsPopupController;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMemberType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.lang.reflect.InvocationTargetException;

import javafx.application.Platform;

/**
 * This action uses the Member Detail Controller to create a library member and give the user the ability to set its
 * initial details.
 */
public class NewLibraryMemberAction extends DexRunAction {
    private static Log log = LogFactory.getLog( NewLibraryMemberAction.class );

    /**
     * Any OTM object that uses the intended model manager.
     * 
     * @param subject
     * @return
     */
    public static boolean isEnabled(OtmObject subject) {
        return true;
    }

    private OtmLibraryMember newMember = null;

    public NewLibraryMemberAction() {
        // Constructor for reflection
    }

    /**
     * {@inheritDoc} The new library action adds library members to the model manager.
     * 
     * @return
     */
    @Override
    public Object doIt(Object data) {
        OtmLibraryMember member = null;
        if (otm != null && otm.getModelManager() != null && data instanceof OtmLibraryMemberType) {
            try {
                // Build and hold onto for undo
                member = OtmLibraryMemberType.buildMember( (OtmLibraryMemberType) data, "New", otm.getModelManager() );

                // Provide a temporary wizardActionManager
                member.setNoLibraryActionManager( new DexWizardActionManager( null ) );

                // If the subject is editable, use it to set initial library - user may change it
                if (otm.getLibrary().isEditable())
                    otm.getLibrary().add( member );

                // If in gui thread, Let user set library and other details
                if (Platform.isFxApplicationThread()) {
                    MemberDetailsPopupController controller = MemberDetailsPopupController.init();
                    controller.setMember( member );
                    if (controller.showAndWait( "MSG" ) != Results.OK)
                        // Cancel
                        member = null;
                }
            } catch (ExceptionInInitializerError | InstantiationException | IllegalAccessException
                | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {

                log.warn( "Error building library member. " + e.getLocalizedMessage() );
                otm.getActionManager().postWarning( "Error creating library member." );
                member = null;
            }
        }

        doIt( member );
        log.debug( "Added new member " + get() );
        return get();
    }

    /**
     * Add the member to the model and clear its no-library action
     * 
     * @param member
     * @return
     */
    public OtmLibraryMember doIt(OtmLibraryMember member) {
        if (member != null) {
            newMember = member;
            // Add member to model manager model and library
            otm.getModelManager().add( newMember );

            // Remove temporary wizardActionManager
            newMember.setNoLibraryActionManager( null );
        }
        return newMember;
    }

    // private void runWizard(OtmLibraryMember member) {}

    /**
     * Return the new member or null if none created.
     * 
     * @see org.opentravel.dex.actions.DexRunAction#get()
     */
    @Override
    public OtmLibraryMember get() {
        return newMember;
    }

    @Override
    public ValidationFindings getVetoFindings() {
        return null;
    }

    @Override
    public boolean isValid() {
        return newMember != null ? newMember.isValid() : false;
    }

    @Override
    public boolean setSubject(OtmObject subject) {
        otm = subject;
        return otm != null;
    }

    @Override
    public String toString() {
        return "Created new library member: " + newMember;
    }

    @Override
    public OtmLibraryMember undoIt() {
        if (newMember != null && newMember.getLibrary() != null)
            newMember.getLibrary().remove( newMember );
        if (otm != null)
            otm.getModelManager().remove( newMember );

        newMember = null;
        log.debug( "Undo new member." );
        return newMember;
    }
}
