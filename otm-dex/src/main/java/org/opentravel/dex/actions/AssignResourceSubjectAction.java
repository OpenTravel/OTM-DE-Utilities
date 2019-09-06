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
import org.opentravel.dex.controllers.member.MemberFilterController;
import org.opentravel.dex.controllers.popup.DexPopupControllerBase.Results;
import org.opentravel.dex.controllers.popup.TypeSelectionContoller;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.schemacompiler.validate.ValidationFindings;

public class AssignResourceSubjectAction extends DexRunAction {
    private static Log log = LogFactory.getLog( AssignResourceSubjectAction.class );

    // private static final String VETO1 = "org.opentravel.schemacompiler.TLProperty.name.ELEMENT_REF_NAME_MISMATCH";
    // private static final String VETO2 = ".OBSOLETE_TYPE_REFERENCE";
    // private static final String VETO3 = ".ILLEGAL_REFERENCE";
    // private static final String[] VETOKEYS = {VETO1, VETO2, VETO3};

    public static boolean isEnabled(OtmObject subject) {
        return (subject.isEditable() && subject instanceof OtmResource);
    }

    private OtmResource resource = null;
    private OtmBusinessObject oldSubject = null;

    // private DexActionManagerBase actionManager = null;


    public AssignResourceSubjectAction() {
        // Constructor for reflection
        actionType = DexActions.ASSIGNSUBJECT;
    }

    /**
     * {@inheritDoc} This action will get the data from the user via modal dialog
     */
    public OtmTypeProvider doIt() {
        log.debug( "Ready to set resource " + resource + " assigned subject. " + ignore );
        if (ignore)
            return null;
        if (resource == null)
            return null;
        if (resource.getActionManager() == null)
            return null;

        // Get the user's selected business object
        MemberAndProvidersDAO selected = null;
        TypeSelectionContoller controller = TypeSelectionContoller.init();
        MemberFilterController filter = controller.getMemberFilterController();
        filter.setTypeFilter( MemberFilterController.BUSINESS );

        controller.setManager( resource.getModelManager() );
        if (controller.showAndWait( "MSG" ) == Results.OK) {
            selected = controller.getSelected();

            if (selected != null && selected.getValue() instanceof OtmBusinessObject)
                doIt( selected.getValue() );
            else
                log.error( "Missing selection from Type Selection Controller" ); // cancel?
        }

        return get();
    }

    /**
     * This action will get the data from the user via modal dialog
     */
    @Override
    public void doIt(Object data) {
        if (resource.getActionManager() == null)
            return;
        if (!(data instanceof OtmBusinessObject))
            return;


        oldSubject = resource.getSubject();
        OtmBusinessObject newSubject = (OtmBusinessObject) data;

        // Set value into model
        OtmBusinessObject result = resource.setSubject( newSubject );

        if (result != newSubject)
            log.debug( "ERROR setting subject." );

        // Record action to allow undo. Will validate results and warn user.
        resource.getActionManager().push( this );

        log.debug( "Set resource subject to " + newSubject );

    }

    @Override
    public OtmBusinessObject get() {
        return resource.getSubject();
    }

    @Override
    public ValidationFindings getVetoFindings() {
        return null;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean setSubject(OtmObject subject) {
        if (!(subject instanceof OtmResource))
            return false;
        resource = (OtmResource) subject;
        return true;
    }

    @Override
    public OtmResource getSubject() {
        return resource;
    }

    @Override
    public String toString() {
        return "Assigned resource subject: " + get();
    }

    @Override
    public OtmBusinessObject undoIt() {
        log.debug( " TODO -Undo-ing change" );
        if (oldSubject != null) {
            if (oldSubject != resource.setAssignedType( oldSubject ))
                resource.getActionManager().postWarning( "Error undoing change." );
            // } else if (oldTLType != null) {
            // // If provider was not in model manager
            // if (oldTLType != user.setAssignedTLType( oldTLType ))
            // actionManager.postWarning( "Error undoing change." );
            // } else {
            // // Sometimes, only the name is known because the tl model does not have the type loaded.
            // user.setTLTypeName( oldTLTypeName );
            // otm.setName( oldName );
            // }
            // otm.setName( oldName ); // May have been changed by assignment
        }
        return get();
    }
}
