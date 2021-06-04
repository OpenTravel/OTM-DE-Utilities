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
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLLibrary;

import java.util.List;

/**
 * Set the library of the library member.
 * <p>
 * Junit: {@linkplain TestSetLibraryAction}
 */
public class SetLibraryAction extends DexRunAction {
    private static Log log = LogFactory.getLog( SetLibraryAction.class );


    /**
     * Any OTM object whose model manager has editable libraries besides the one the subect is in.
     * 
     * @param subject
     * @return
     */
    public static boolean isEnabled(OtmObject subject) {
        if (subject == null || subject.getModelManager() == null)
            return false;
        return subject.getModelManager().hasEditableLibraries( subject.getLibrary() );
    }

    public static boolean isEnabled(OtmObject subject, OtmObject value) {
        return isEnabled( subject );
    }

    private OtmLibrary oldLibrary = null;

    public SetLibraryAction() {
        // Constructor for reflection
    }

    /**
     * This method will get the data from the user via modal dialog
     */
    public OtmLibrary doIt() {
        if (ignore || !isEnabled( otm ))
            return null;
        List<OtmLibrary> candidates = otm.getModelManager().getEditableLibraries();
        candidates.remove( otm.getLibrary() );

        if (candidates.isEmpty())
            return null; // No libraries
        if (candidates.size() == 1)
            return doIt( candidates.get( 0 ) );

        // log.debug( "Dialog to select library to set." );
        SelectLibraryDialogController controller = SelectLibraryDialogController.init();
        controller.setModelManager( otm.getModelManager() );
        if (controller.showAndWait( "New Library Member" ) == Results.OK)
            doIt( controller.getSelected() );
        else {
            // log.debug( "Invalid selection or cancel." );
            return null;
        }
        return get();
    }

    /**
     * {@inheritDoc} Set the library in the library member.
     * 
     * @return the member's library or null.
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
     * <P>
     * Note: The OtmLibrary is retrieved from the TL Library Member' library's listener
     * 
     * @param library
     * @return the member's library or null.
     */
    public OtmLibrary doIt(OtmLibrary lib) {
        if (lib != null && otm instanceof OtmLibraryMember && lib != otm.getLibrary()) {
            if (lib.getTL() == null)
                return null;
            OtmLibraryMember member = (OtmLibraryMember) otm;
            if (!(member.getTlLM() instanceof LibraryMember))
                return null;

            // Save the old library for Undo
            oldLibrary = member.getLibrary();

            // 6/2/2021 dmh - changed to use TLModel's method. See TestSetLibraryAction.
            try {
                member.getModelManager().getTlModel().moveToLibrary( member.getTlLM(), (TLLibrary) lib.getTL() );
            } catch (Exception e) {
                log.warn( "Exception moving member to new library: " + e.getLocalizedMessage() );
                return null;
            }
            // // Debugging
            // if (member.getLibrary() != lib)
            // log.error( "Missing library." );
            // if (member.getTlLM().getOwningLibrary() != lib.getTL())
            // log.error( "TL Member is missing library." );
            // if (oldLibrary != null && oldLibrary.getTL().getNamedMember( member.getName() ) != null)
            // log.error( "Old library still has member" );

            // log.debug( "Set library to " + get() );
        }
        return get();
    }

    /**
     * Return the member's library or null.
     * 
     * @see org.opentravel.dex.actions.DexRunAction#get()
     */
    @Override
    public OtmLibrary get() {
        return otm.getLibrary();
    }

    // @Override
    // public ValidationFindings getVetoFindings() {
    // return null;
    // }

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
