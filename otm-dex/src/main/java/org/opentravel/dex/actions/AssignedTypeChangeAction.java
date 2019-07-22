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
import org.opentravel.common.ImageManager;
import org.opentravel.common.ValidationUtils;
import org.opentravel.dex.controllers.member.MemberAndProvidersDAO;
import org.opentravel.dex.controllers.member.properties.PropertiesDAO;
import org.opentravel.dex.controllers.popup.DexPopupControllerBase.Results;
import org.opentravel.dex.controllers.popup.TypeSelectionContoller;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.validate.ValidationFindings;

public class AssignedTypeChangeAction implements DexAction<OtmTypeProvider> {
    private static Log log = LogFactory.getLog( AssignedTypeChangeAction.class );

    private OtmObject otm;
    private OtmTypeUser user = null;
    private boolean outcome = false;

    // private PropertiesDAO propertiesDAO;
    private OtmTypeProvider oldProvider;
    private NamedEntity oldTLType;
    private String oldName;
    private OtmTypeProvider newProvider;
    private boolean ignore;

    private ImageManager imageMgr = null;

    private String oldTLTypeName;

    private static final String VETO1 = "org.opentravel.schemacompiler.TLProperty.name.ELEMENT_REF_NAME_MISMATCH";
    private static final String VETO2 = ".OBSOLETE_TYPE_REFERENCE";
    private static final String VETO3 = ".ILLEGAL_REFERENCE";
    private static final String[] VETOKEYS = {VETO1, VETO2, VETO3};

    public AssignedTypeChangeAction(OtmTypeUser user) {
        this.user = user;
        this.otm = user;
    }

    public AssignedTypeChangeAction(PropertiesDAO prop) {
        if (prop != null) {
            if (prop.getValue() instanceof OtmTypeUser)
                this.user = (OtmTypeUser) prop.getValue();
            this.otm = prop.getValue();
        }
    }

    @Override
    public OtmObject getSubject() {
        return otm;
    }

    /**
     * {@inheritDoc} This action will get the data from the user via modal dialog
     */
    @Override
    public void doIt(Object data) {
        if (data == null)
            doIt();
        else {
            if (!(data instanceof OtmTypeProvider))
                return;
            newProvider = (OtmTypeProvider) data;
            // Set value into model
            OtmTypeProvider p = user.setAssignedType( newProvider );

            if (p != newProvider)
                outcome = false; // there was an error
            // TODO - how to process the error? Veto does not look at this.

            // Validate results. Note: TL will not veto (prevent) change.
            if (isValid())
                outcome = true;

            // Record action to allow undo. Will validate results and warn user.
            otm.getActionManager().push( this );
        }
    }

    /**
     * {@inheritDoc} This action will get the data from the user via modal dialog
     */
    public OtmTypeProvider doIt() {
        log.debug( "Ready to set assigned type to " + otm + " " + ignore );
        if (ignore)
            return null;
        if (!isEnabled())
            return null;
        if (user == null || otm == null)
            return null;

        // Is action manager needed any more? For undo?
        if (otm.getActionManager() == null)
            return null;

        if (otm.getOwningMember() == null)
            return null;
        OtmModelManager modelMgr = otm.getOwningMember().getModelManager();
        if (modelMgr == null)
            return null;

        // Hold onto old value
        user = (OtmTypeUser) otm;
        oldProvider = user.getAssignedType();
        oldTLType = user.getAssignedTLType();
        oldName = otm.getName();
        oldTLTypeName = user.assignedTypeProperty().get();

        // Get the user's selected new provider
        MemberAndProvidersDAO selected = null;
        TypeSelectionContoller controller = TypeSelectionContoller.init();
        controller.setManager( modelMgr );
        if (controller.showAndWait( "MSG" ) == Results.OK) {
            selected = controller.getSelected();
            if (selected == null || selected.getValue() == null)
                log.error( "Missing selection from Type Selection Controller" ); // cancel?
            else
                log.debug( "action - Set Assigned Type on: " + selected.getValue().getName() );
        }

        // Make the change and test the results
        if (selected != null && selected.getValue() instanceof OtmTypeProvider) {
            doIt( selected.getValue() );
            // newProvider = (OtmTypeProvider) selected.getValue();
            // // Set value into model
            // OtmTypeProvider p = user.setAssignedType(newProvider);
            //
            // if (p != newProvider)
            // outcome = false; // there was an error
            // // TODO - how to process the error? Veto does not look at this.
            //
            // // Validate results. Note: TL will not veto (prevent) change.
            // if (isValid())
            // outcome = true;
            //
            // // Record action to allow undo. Will validate results and warn user.
            // otm.getActionManager().push(this);

            log.debug( "Set type to " + newProvider + "  success: " + outcome );
        }
        return newProvider;
    }

    @Override
    public OtmTypeProvider undo() {
        log.debug( " TODO -Undo-ing change" );
        if (oldProvider != null) {
            if (oldProvider != user.setAssignedType( oldProvider ))
                otm.getActionManager().postWarning( "Error undoing change." );
        } else if (oldTLType != null) {
            // If provider was not in model manager
            if (oldTLType != user.setAssignedTLType( oldTLType ))
                otm.getActionManager().postWarning( "Error undoing change." );
        } else {
            // Sometimes, only the name is known because the tl model does not have the type loaded.
            user.setTLTypeName( oldTLTypeName );
            otm.setName( oldName );
        }
        otm.setName( oldName ); // May have been changed by assignment
        return oldProvider;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Assure the object is a type user and editable.
     */
    @Override
    public boolean isEnabled() {
        if (!(otm instanceof OtmTypeUser))
            return false;
        if (!otm.isEditable())
            return false;
        return true;
    }

    public static boolean isEnabled(OtmObject subject) {
        if (!(subject instanceof OtmTypeUser))
            return false;
        if (!subject.isEditable())
            return false;
        return true;
    }

    @Override
    public boolean isAllowed(OtmTypeProvider value) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ValidationFindings getVetoFindings() {
        // TODO create a finding if the outcome is false
        return ValidationUtils.getRelevantFindings( VETOKEYS, otm.getFindings() );
    }

    @Override
    public boolean isValid() {
        return otm.isValid( true ) ? true
            : ValidationUtils.getRelevantFindings( VETOKEYS, otm.getFindings() ).isEmpty();
    }

    @Override
    public String toString() {
        return "Assigned Type: " + newProvider;
    }
}
