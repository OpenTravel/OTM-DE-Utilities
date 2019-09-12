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
import org.opentravel.dex.controllers.member.MemberAndProvidersDAO;
import org.opentravel.dex.controllers.popup.DexPopupControllerBase.Results;
import org.opentravel.dex.controllers.popup.MemberDetailsPopupController;
import org.opentravel.dex.controllers.popup.TypeSelectionContoller;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.lang.reflect.InvocationTargetException;

/**
 * This action uses the Member Detail Controller to create a library member and give the user the ability to set its
 * initial details.
 */
public class NewLibraryMemberAction extends DexRunAction {
    private static Log log = LogFactory.getLog( NewLibraryMemberAction.class );

    // private static final String VETO1 = "org.opentravel.schemacompiler.TLProperty.name.ELEMENT_REF_NAME_MISMATCH";
    // private static final String VETO2 = ".OBSOLETE_TYPE_REFERENCE";
    // private static final String VETO3 = ".ILLEGAL_REFERENCE";
    // private static final String[] VETOKEYS = {VETO1, VETO2, VETO3};


    /**
     * Any OTM object in the target library.
     * 
     * @param subject
     * @return
     */
    public static boolean isEnabled(OtmObject subject) {
        return (subject.getLibrary().isEditable());
        // if (subject.getModelManager() != null)
        // return subject.getModelManager().hasSaveableLibraries();
        // return false;
    }

    private OtmLibraryMember newMember = null;

    // private OtmTypeUser user = null;
    //
    // private OtmTypeProvider oldProvider;
    // private NamedEntity oldTLType;
    // private String oldName;
    // private String oldTLTypeName;
    // private OtmTypeProvider newProvider;
    //
    // private DexActionManagerBase actionManager = null;


    public NewLibraryMemberAction() {
        // Constructor for reflection
    }

    /**
     * {@inheritDoc} This action will get the data from the user via modal dialog
     */
    public OtmLibraryMember doIt() {
        if (ignore)
            return null;
        log.debug( "Ready to create new library member." );

        // Get the user's selected new provider
        TypeSelectionContoller controller = TypeSelectionContoller.init();
        controller.setManager( otm.getOwningMember().getModelManager() );

        if (controller.showAndWait( "New Library Member" ) == Results.OK) {
            MemberAndProvidersDAO selected = controller.getSelected();
            if (selected == null || !(selected.getValue() instanceof OtmLibraryMember))
                log.error( "Missing selection from Controller" ); // cancel?
            else
                doIt( selected.getValue() );
        }

        return newMember;
    }

    /**
     * {@inheritDoc} The new library action adds library members to the model manager.
     * 
     * @return
     */
    @Override
    public Object doIt(Object data) {
        if (otm != null && otm.getModelManager() != null && data instanceof LibraryMemberType) {
            try {
                // Build and hold onto for undo
                newMember = LibraryMemberType.buildMember( (LibraryMemberType) data, "New", otm.getModelManager() );

                // Add member to model manager model and library
                otm.getModelManager().add( newMember );
                otm.getLibrary().add( newMember );

                // Let user set library and other details
                MemberDetailsPopupController controller = MemberDetailsPopupController.init();
                controller.setMember( newMember );
                if (controller.showAndWait( "MSG" ) == Results.OK) {
                    // Record action to allow undo. Will validate results and warn user.
                    otm.getActionManager().push( this );
                    log.debug( "Added new member " + get() );
                } else {
                    // Cancel
                    otm.getLibrary().remove( newMember );
                    otm.getModelManager().remove( newMember );
                    newMember = null;
                }
            } catch (ExceptionInInitializerError | InstantiationException | IllegalAccessException
                | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {

                log.warn( "Error building library member. " + e.getLocalizedMessage() );
                otm.getActionManager().postWarning( "Error creating library member." );
            }
        }
        return get();
    }

    private void runWizard(OtmLibraryMember member) {}

    @Override
    public OtmLibraryMember get() {
        return newMember;
    }

    @Override
    public ValidationFindings getVetoFindings() {
        // // TODO create a finding if the outcome is false
        // return ValidationUtils.getRelevantFindings( VETOKEYS, otm.getFindings() );
        return null;
    }

    @Override
    public boolean isValid() {
        // return otm.isValid( true ) ? true
        // : ValidationUtils.getRelevantFindings( VETOKEYS, otm.getFindings() ).isEmpty();
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
        if (otm != null) {
            otm.getLibrary().remove( newMember );
            otm.getModelManager().remove( newMember );
            newMember = null;
        }
        log.debug( "Undo new member." );
        return newMember;
    }
}
