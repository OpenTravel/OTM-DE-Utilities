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

import java.util.List;

import javafx.application.Platform;

/**
 * Make a copy of a library member.
 */
public class CopyLibraryMemberAction extends DexRunAction {
    private static Log log = LogFactory.getLog( CopyLibraryMemberAction.class );


    /**
     * Any OTM object that uses the intended model manager and an editable library.
     * 
     * @param subject
     * @return
     */
    public static boolean isEnabled(OtmObject subject) {
        if (subject instanceof OtmLibraryMember)
            return subject.getModelManager().hasEditableLibraries();
        return false;
    }

    public static boolean isEnabled(OtmObject subject, OtmObject value) {
        return isEnabled( subject );
    }

    private OtmLibraryMember newMember = null;

    // private OtmLibrary oldLibrary = null;

    public CopyLibraryMemberAction() {
        // Constructor for reflection
    }

    /**
     * {@inheritDoc} This action will get the data from the user via modal dialog
     */
    public OtmLibraryMember doIt() {
        if (ignore)
            return null;
        List<OtmLibrary> candidates = otm.getModelManager().getEditableLibraries();
        // No libraries
        if (candidates.isEmpty())
            return null;
        if (candidates.size() == 1)
            return doIt( candidates.get( 0 ) );

        if (Platform.isFxApplicationThread()) {
            // log.debug( "select library for the library member copy." );
            SelectLibraryDialogController controller = SelectLibraryDialogController.init();
            controller.setModelManager( otm.getModelManager() );
            if (controller.showAndWait( "New copy of Library Member" ) == Results.OK)
                doIt( controller.getSelected() );
            else
                log.error( "Invalid selection or cancel." );
        }
        return get();
    }

    /**
     * {@inheritDoc} Copy the member and set its library.
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
     * Copy the member and add to passed library.
     * 
     * @param library
     * @return
     */
    public OtmLibraryMember doIt(OtmLibrary lib) {
        if (lib != null && otm instanceof OtmLibraryMember) {
            newMember = getSubject().copy();
            if (newMember != null) {
                newMember.setName( newMember.getName() + "_Copy" );
                newMember = lib.add( newMember );
                // lib.getTL().addNamedMember( newMember.getTlLM() );
                // newMember.getTlLM().setOwningLibrary( lib.getTL() );
            }
        }
        if (newMember != null)
            newMember.refresh();
        return get();
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
    public boolean isValid() {
        return get() != null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.opentravel.dex.actions.DexAction#setSubject(org.opentravel.model.OtmObject)
     */
    @Override
    public boolean setSubject(OtmObject subject) {
        if (subject instanceof OtmLibraryMember)
            otm = subject;
        return otm instanceof OtmLibraryMember;
    }

    @Override
    public OtmLibraryMember getSubject() {
        return (OtmLibraryMember) otm;
    }

    @Override
    public String toString() {
        return "Set member library to: " + get();
    }

    @Override
    public OtmLibraryMember undoIt() {
        if (get() != null && get().getLibrary() != null) {
            get().getLibrary().delete( get() );
            newMember = null;
        }
        // log.debug( "Undo copy." );
        return get();
    }
}
