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
import org.opentravel.dex.action.manager.DexActionManagerBase;
import org.opentravel.dex.controllers.member.MemberFilterController;
import org.opentravel.dex.controllers.popup.DexPopupControllerBase.Results;
import org.opentravel.dex.controllers.popup.TypeSelectionContoller;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmProperties.OtmIdAttribute;
import org.opentravel.model.resource.OtmActionFacet;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.validate.ValidationFindings;

public class AssignedTypeChangeAction extends DexRunAction {
    private static Log log = LogFactory.getLog( AssignedTypeChangeAction.class );

    private static final String VETO1 = "org.opentravel.schemacompiler.TLProperty.name.ELEMENT_REF_NAME_MISMATCH";
    private static final String VETO2 = ".OBSOLETE_TYPE_REFERENCE";
    private static final String VETO3 = ".ILLEGAL_REFERENCE";
    private static final String[] VETOKEYS = {VETO1, VETO2, VETO3};

    public static boolean isEnabled(OtmObject subject) {
        // Id is a type user but can't be changed.
        if (subject instanceof OtmIdAttribute)
            return false;
        if (!(subject instanceof OtmTypeUser))
            return false;
        if (subject.isEditable())
            return true;
        if (subject.getLibrary() != null && subject.getLibrary().isChainEditable())
            return subject.getLibrary().getVersionChain().canAssignLaterVersion( (OtmTypeUser) subject );
        return false;
    }

    private OtmTypeUser user = null;
    private OtmTypeUser newUser = null;

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
     * This action will get the data from the user via modal dialog
     */
    public OtmTypeProvider doIt() {
        log.debug( "Ready to set assigned type to " + otm + " " + ignore );
        if (ignore)
            return null;
        if (user == null || !(otm instanceof OtmTypeUser))
            return null;
        if (actionManager == null)
            return null;
        if (otm.getOwningMember() == null)
            return null;
        if (otm.getLibrary() == null)
            return null;
        OtmModelManager modelMgr = otm.getOwningMember().getModelManager();
        if (modelMgr == null)
            return null;

        // Setup controller to get the users selection
        TypeSelectionContoller controller = TypeSelectionContoller.init();
        controller.setManager( modelMgr );

        // Find out if this typeUser is in a minor and not new to chain
        user = (OtmTypeUser) otm;
        if (!user.isEditable() && user.getLibrary().isChainEditable()) {
            // Create a new property in a new minor version of the owning member
            newUser = user.getLibrary().getVersionChain().getNewMinorTypeUser( user );
            // set filter
            controller.getMemberFilterController().setMinorVersionFilter( user.getAssignedType() );

            if (newUser == null) {
                otm.getActionManager().postWarning( "Error creating minor version of " + otm.getOwningMember() );
                return null;
            }
        }

        // Set applicable filters
        if (otm instanceof OtmResource)
            controller.getMemberFilterController().setTypeFilter( MemberFilterController.BUSINESS );
        if (otm instanceof OtmActionFacet)
            controller.getMemberFilterController().setTypeFilter( MemberFilterController.CORE );

        // Get the user's selected new provider
        controller.showAndWait( "MSG" );
        OtmTypeProvider selected = controller.getSelectedProvider();
        if (selected != null && controller.getResult() == Results.OK) {
            doIt( selected );
        } else {
            // handle cancel and bad selection when newUser created
            log.error( "Canceled or missing selection from Type Selection Controller" );
            if (newUser != null && newUser.getLibrary() != null)
                newUser.getLibrary().delete( newUser.getOwningMember() );
            return null;
        }

        return get();
    }

    /**
     * This action will get the data from the user via modal dialog
     * 
     * @return
     */
    @Override
    public Object doIt(Object data) {
        if (data == null)
            doIt();

        if (actionManager == null)
            return null;

        if (data instanceof OtmTypeProvider) {

            // Hold onto old values
            user = (OtmTypeUser) otm;
            oldProvider = user.getAssignedType();
            oldTLType = user.getAssignedTLType();
            oldName = otm.getName();
            if (oldTLType != null)
                oldTLTypeName = oldTLType.getLocalName();

            newProvider = (OtmTypeProvider) data;
            // Set value into model
            if (newUser != null)
                newUser.setAssignedType( newProvider );
            else
                user.setAssignedType( newProvider );

        }
        return get();
    }

    @Override
    public OtmTypeProvider get() {
        return user.getAssignedType();
    }

    @Override
    public ValidationFindings getVetoFindings() {
        return ValidationUtils.getRelevantFindings( VETOKEYS, otm.getFindings() );
    }

    @Override
    public boolean isValid() {
        if (otm.getOwningMember() != null)
            otm.getOwningMember().isValid( true ); // Update validation status
        return otm.isValid( true ) ? true
            : ValidationUtils.getRelevantFindings( VETOKEYS, otm.getFindings() ).isEmpty();
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
    public OtmTypeUser getSubject() {
        return newUser == null ? (OtmTypeUser) otm : newUser;
    }

    @Override
    public String toString() {
        return "Assigned Type: " + newProvider;
    }

    @Override
    public OtmTypeProvider undoIt() {
        if (newUser != null) {
            // simply delete the newly created minor version
            newUser.getLibrary().delete( newUser.getOwningMember() );
            newUser = null;
        } else {
            if (oldProvider != null) {
                if (oldProvider != user.setAssignedType( oldProvider ))
                    actionManager.postWarning( "Error undoing change." );
            } else if (oldTLType != null) {
                // If provider was not in model manager
                if (oldTLType != user.setAssignedTLType( oldTLType ))
                    actionManager.postWarning( "Error undoing change." );
            } else if (oldTLTypeName != null && !oldTLTypeName.isEmpty()) {
                // Sometimes, only the name is known because the tl model does not have the type loaded.
                user.setTLTypeName( oldTLTypeName );
                otm.setName( oldName );
            } else
                // No clues about what to set it to, so clear it.
                user.setAssignedType( null );
            otm.setName( oldName ); // May have been changed by assignment
        }
        log.debug( "Undo type assignment. Set to " + get() );
        return oldProvider;
    }
}
