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
import org.opentravel.dex.controllers.popup.DexPopupControllerBase.Results;
import org.opentravel.dex.controllers.popup.SelectLibraryDialogController;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.util.List;

/**
 * Set the library of the library member.
 */
public class SetLibraryAction extends DexRunAction {
    private static Log log = LogFactory.getLog( SetLibraryAction.class );


    /**
     * Any OTM object that uses the intended model manager.
     * 
     * @param subject
     * @return
     */
    public static boolean isEnabled(OtmObject subject) {
        return subject.getModelManager().hasEditableLibraries( subject.getLibrary() );
    }

    private OtmLibrary oldLibrary = null;

    public SetLibraryAction() {
        // Constructor for reflection
    }

    /**
     * {@inheritDoc} This action will get the data from the user via modal dialog
     */
    public OtmLibrary doIt() {
        if (ignore)
            return null;
        List<OtmLibrary> candidates = otm.getModelManager().getEditableLibraries();
        // No libraries
        if (candidates.isEmpty())
            return null;
        if (candidates.size() == 1)
            return doIt( candidates.get( 0 ) );

        // log.debug( "TEST - select library to set." );
        SelectLibraryDialogController controller = SelectLibraryDialogController.init();
        controller.setModelManager( otm.getModelManager() );
        if (controller.showAndWait( "New Library Member" ) == Results.OK)
            doIt( controller.getSelected() );
        else
            log.error( "Invalid selection or cancel." );
        return get();
    }

    /**
     * {@inheritDoc} Set the library in the library member.
     * 
     * @return
     */
    @Override
    public Object doIt(Object data) {
        if (data == null)
            return doIt();
        else if (otm instanceof OtmLibraryMember && data instanceof OtmLibrary)
            return doIt( ((OtmLibrary) data) );
        return null;
    }

    /**
     * Add the member to the model and clear its no-library action
     * 
     * @param library
     * @return
     */
    public OtmLibrary doIt(OtmLibrary lib) {
        if (lib != null && otm instanceof OtmLibraryMember) {
            OtmLibraryMember member = (OtmLibraryMember) otm;

            // Save the old library for Undo
            oldLibrary = member.getLibrary();

            lib.getTL().addNamedMember( member.getTlLM() );
            member.getTlLM().setOwningLibrary( lib.getTL() );
            member.refresh();
            // If you add and remove from library instead of setting member, TL will not update all the dependent type
            // assignments.
            // Add to new library
            // lib.add( member );
            // // Remove from old library
            // if (oldLibrary != null)
            // oldLibrary.getTL().removeNamedMember( member.getTlLM() );

            // Debugging
            // assert member.getLibrary() == lib;
            // assert member.getTlLM().getOwningLibrary() == lib.getTL();
            // log.debug( "Set library to " + get() );
        }
        return get();
    }

    /**
     * Return the new member or null if none created.
     * 
     * @see org.opentravel.dex.actions.DexRunAction#get()
     */
    @Override
    public OtmLibrary get() {
        return otm.getLibrary();
    }

    @Override
    public ValidationFindings getVetoFindings() {
        return null;
    }

    @Override
    public boolean isValid() {
        return get() != null;
    }

    @Override
    public boolean setSubject(OtmObject subject) {
        otm = subject;
        return otm != null;
    }

    @Override
    public String toString() {
        return "Set member library to: " + get();
    }

    @Override
    public OtmLibrary undoIt() {
        doIt( oldLibrary );
        // log.debug( "Undo set library." );
        return get();
    }
}
