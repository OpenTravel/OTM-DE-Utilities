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
import org.opentravel.common.ValidationUtils;
import org.opentravel.dex.controllers.member.MemberAndProvidersDAO;
import org.opentravel.dex.controllers.popup.DexPopupControllerBase.Results;
import org.opentravel.dex.controllers.popup.TypeSelectionContoller;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.validate.ValidationFindings;

public class AssignedTypeChangeAction extends DexRunAction {
    private static Log log = LogFactory.getLog( AssignedTypeChangeAction.class );

    private static final String VETO1 = "org.opentravel.schemacompiler.TLProperty.name.ELEMENT_REF_NAME_MISMATCH";
    private static final String VETO2 = ".OBSOLETE_TYPE_REFERENCE";
    private static final String VETO3 = ".ILLEGAL_REFERENCE";
    private static final String[] VETOKEYS = {VETO1, VETO2, VETO3};

    public static boolean isEnabled(OtmObject subject) {
        return (subject.isEditable() && subject instanceof OtmTypeUser);
    }

    private OtmTypeUser user = null;

    private OtmTypeProvider oldProvider;
    private NamedEntity oldTLType;
    private String oldName;
    private String oldTLTypeName;
    private OtmTypeProvider newProvider;

    private DexActionManagerBase actionManager = null;


    public AssignedTypeChangeAction() {
        // Constructor for reflection
    }

    /**
     * {@inheritDoc} This action will get the data from the user via modal dialog
     */
    public OtmTypeProvider doIt() {
        log.debug( "Ready to set assigned type to " + otm + " " + ignore );
        if (ignore)
            return null;
        // Actions only created if action is enabled
        // if (!isEnabled())
        // return null;
        if (user == null || otm == null)
            return null;

        // Is action manager needed any more? For undo?
        if (actionManager == null)
            return null;

        if (otm.getOwningMember() == null)
            return null;
        OtmModelManager modelMgr = otm.getOwningMember().getModelManager();
        if (modelMgr == null)
            return null;

        // Get the user's selected new provider
        MemberAndProvidersDAO selected = null;
        TypeSelectionContoller controller = TypeSelectionContoller.init();
        controller.setManager( modelMgr );
        if (controller.showAndWait( "MSG" ) == Results.OK) {
            selected = controller.getSelected();
            if (selected == null || !(selected.getValue() instanceof OtmTypeProvider))
                log.error( "Missing selection from Type Selection Controller" ); // cancel?
            else
                doIt( selected.getValue() );
            // log.debug( "action - Set Assigned Type on: " + selected.getValue().getName() );
        }

        // Make the change and test the results
        // if (selected != null && selected.getValue() instanceof OtmTypeProvider)
        // doIt( selected.getValue() );

        return newProvider;
    }

    /**
     * This action will get the data from the user via modal dialog
     */
    @Override
    public void doIt(Object data) {
        if (actionManager == null)
            return;
        if (data == null)
            doIt();
        else {
            if (!(data instanceof OtmTypeProvider))
                return;

            // Hold onto old values
            user = (OtmTypeUser) otm;
            oldProvider = user.getAssignedType();
            oldTLType = user.getAssignedTLType();
            oldName = otm.getName();
            oldTLTypeName = oldTLType.getLocalName();

            newProvider = (OtmTypeProvider) data;
            // Set value into model
            OtmTypeProvider p = user.setAssignedType( newProvider );

            // if (p != newProvider)
            // outcome = false; // there was an error
            // // TODO - how to process the error? Veto does not look at this.
            //
            // // Validate results. Note: TL will not veto (prevent) change.
            // if (isValid())
            // outcome = true;

            // Record action to allow undo. Will validate results and warn user.
            actionManager.push( this );
            log.debug( "Set type to " + newProvider );
        }
    }

    @Override
    public OtmTypeProvider get() {
        return user.getAssignedType();
    }

    @Override
    public ValidationFindings getVetoFindings() {
        // TODO create a finding if the outcome is false
        return ValidationUtils.getRelevantFindings( VETOKEYS, otm.getFindings() );
    }

    // @Override
    // public boolean isAllowed(OtmObject value) {
    // return user instanceof OtmTypeUser && value instanceof OtmTypeProvider;
    // }

    @Override
    public boolean isValid() {
        return otm.isValid( true ) ? true
            : ValidationUtils.getRelevantFindings( VETOKEYS, otm.getFindings() ).isEmpty();
    }

    @Override
    public boolean set(OtmObject subject) {
        if (subject instanceof OtmTypeProvider) {
            user.setAssignedType( (OtmTypeProvider) subject );
            return true;
        }
        return false;
    }

    @Override
    public boolean setSubject(OtmObject subject) {
        if (!(subject instanceof OtmTypeUser))
            return false;
        otm = subject;
        user = (OtmTypeUser) subject;
        if (otm.getActionManager() instanceof DexActionManagerBase)
            actionManager = (DexActionManagerBase) otm.getActionManager();
        return true;
    }

    @Override
    public String toString() {
        return "Assigned Type: " + newProvider;
    }

    @Override
    public OtmTypeProvider undoIt() {
        log.debug( " TODO -Undo-ing change" );
        if (oldProvider != null) {
            if (oldProvider != user.setAssignedType( oldProvider ))
                actionManager.postWarning( "Error undoing change." );
        } else if (oldTLType != null) {
            // If provider was not in model manager
            if (oldTLType != user.setAssignedTLType( oldTLType ))
                actionManager.postWarning( "Error undoing change." );
        } else {
            // Sometimes, only the name is known because the tl model does not have the type loaded.
            user.setTLTypeName( oldTLTypeName );
            otm.setName( oldName );
        }
        otm.setName( oldName ); // May have been changed by assignment
        return oldProvider;
    }
}
