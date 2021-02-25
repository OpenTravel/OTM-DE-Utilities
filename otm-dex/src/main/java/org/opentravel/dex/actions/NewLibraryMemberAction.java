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
import org.opentravel.dex.actions.resource.AssignResourceSubjectAction;
import org.opentravel.dex.controllers.popup.DexPopupControllerBase.Results;
import org.opentravel.dex.controllers.popup.MemberDetailsPopupController;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMemberType;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

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

    public static boolean isEnabled(OtmObject subject, OtmObject value) {
        return isEnabled( subject );
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
                if (((OtmLibraryMemberType) data).equals( OtmLibraryMemberType.EXTENSIONPOINTFACET ))
                    return null; // TODO - implement when patch library is fully implemented.

                // Build and hold onto for undo
                member = OtmLibraryMemberType.buildMember( (OtmLibraryMemberType) data, "New", otm.getModelManager() );
                // Check: we have a member
                if (member == null)
                    return null;

                // Provide a temporary wizardActionManager
                member.setNoLibraryActionManager( new DexWizardActionManager( null ) );

                // Try to add to an editable library. Note: user may change it later
                // Check: Member is NOT in a library yet
                if (member.getLibrary() != null)
                    log.warn( "Member has a library: " + member.getLibraryName() );

                // Get an editable library to assign
                //
                // If the subject is editable, use it to set initial library
                OtmLibrary lib = otm.getLibrary();
                if (lib == null || !lib.isEditable()) {
                    // Otherwise, get any one from the model manager
                    List<OtmLibrary> libs = otm.getModelManager().getEditableLibraries();
                    if (libs != null && !libs.isEmpty())
                        lib = libs.get( 0 );
                }
                if (lib == null) {
                    member = null; // cancel
                    return null;
                }

                // Add member to library. Increment name until add is successful
                OtmLibraryMember result = null;
                int i = 1;
                do {
                    result = lib.add( member );
                    if (result == null)
                        member.setName( "New" + Integer.toString( i++ ) );
                } while (result == null && i < 100);
                // Check: Member IS in a library
                if (member.getLibrary() == null)
                    log.warn( "Member does not have a library." );

                // If it is a contextual facet, try to set the base type
                if (member instanceof OtmContextualFacet)
                    ((OtmContextualFacet) member).setBaseType( otm );

                // If in gui thread, Let user set library and other details
                if (member != null && Platform.isFxApplicationThread()) {
                    MemberDetailsPopupController controller = MemberDetailsPopupController.init();
                    controller.setMember( member );
                    if (controller.showAndWait( "MSG" ) != Results.OK)
                        member = null; // Cancel
                }
                // Check - make sure member is one library and only in one library
                if (member != null) {
                    if (!(member.getTL() instanceof LibraryMember))
                        log.error( "Invalid library member." );;
                    if (member.getLibrary() == null)
                        log.error( "New member is not in a library." );
                    if (member.getModelManager().get( member.getLibrary().getTL() ) == null)
                        log.error( "Model manager can not find member's library." );
                    if (!member.getLibrary().getMembers().contains( member ))
                        log.error( "Member's library does not contain member." );
                    if (member.getLibrary() != lib) {
                        // Library was changed
                        if (lib.getMembers().contains( member ))
                            log.error( "Original library contain member." );
                        if (lib.getTL().getNamedMembers().contains( (LibraryMember) member.getTL() ))
                            log.error( "Original library contains TL Library Member." );
                        if (lib.getTL().getNamedMember( member.getName() ) != null)
                            log.error( "Member name found in original library." );
                    }
                }

                if (member instanceof OtmResource && ((OtmResource) member).getSubject() == null) {
                    ((OtmResource) member).setBasePath( null );
                    OtmBusinessObject subject =
                        AssignResourceSubjectAction.getUserTypeSelection( member.getModelManager(), null );
                    ((OtmResource) member).setSubject( subject );
                }

                // Try to build out the object
                if (member != null)
                    member.build();

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
            // Subject was only used to get a guess at library. Replace it so the event has useful subject.
            setSubject( newMember );

            // Add member to model manager model and library (if needed)
            otm.getModelManager().add( newMember );

            // Remove temporary wizardActionManager
            newMember.setNoLibraryActionManager( null );
        }
        return newMember;
    }


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
        // TODO - should no library be a veto?
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
        String name = "";
        if (newMember != null && newMember.getName() != null)
            name = newMember.getName();
        return "Created new library member: " + name;
    }

    @Override
    public OtmLibraryMember undoIt() {
        if (newMember != null && newMember.getLibrary() != null)
            newMember.getLibrary().delete( newMember );

        newMember = null;
        setSubject( null );
        log.debug( "Undo new member." );
        return newMember;
    }
}
