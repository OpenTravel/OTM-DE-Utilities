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
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmFacets.OtmChoiceFacet;
import org.opentravel.model.otmFacets.OtmCustomFacet;
import org.opentravel.model.otmFacets.OtmQueryFacet;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmChoiceObject;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.model.TLExtensionOwner;
import org.opentravel.schemacompiler.validate.ValidationFindings;

public class BaseTypeChangeAction extends DexRunAction {
    private static Log log = LogFactory.getLog( BaseTypeChangeAction.class );

    private static final String VETO1 =
        "org.opentravel.schemacompiler.TLBusinessObject.versionExtension.INVALID_VERSION_EXTENSION";
    private static final String[] VETOKEYS = {VETO1};
    // private static final String VETO2 = ".OBSOLETE_TYPE_REFERENCE";
    // private static final String VETO3 = ".ILLEGAL_REFERENCE";
    // private static final String[] VETOKEYS = {VETO1, VETO2, VETO3};
    // VETOFINDING: Can not make change.
    // The business object that is extended is assigned to a later version.
    // Circular extension references are not allowed.
    // org.opentravel.schemacompiler.TLBusinessObject.versionExtension.INVALID_VERSION_EXTENSION

    public static boolean isEnabled(OtmObject subject) {
        if (subject != null && subject.isEditable()) {
            return subject instanceof OtmContextualFacet || subject.getTL() instanceof TLExtensionOwner;
        }
        return false;
    }

    private OtmObject oldBaseType;

    public BaseTypeChangeAction() {
        // Constructor for reflection
    }

    /**
     * This action will get the data from the user via modal dialog
     */
    public OtmObject doIt() {
        // log.debug( "Ready to set base type to " + otm + " " + ignore );
        if (!isEnabled( getSubject() ) || ignore)
            return null;

        // Get type selection controller for user to select new base type
        MemberAndProvidersDAO selected = null;
        TypeSelectionContoller controller = TypeSelectionContoller.init();
        controller.setManager( getSubject().getModelManager() );
        if (getSubject() instanceof OtmChoiceFacet)
            controller.getMemberFilterController().setTypeFilterValue( OtmChoiceObject.class.getSimpleName() );
        else if (getSubject() instanceof OtmQueryFacet || getSubject() instanceof OtmCustomFacet)
            controller.getMemberFilterController().setTypeFilterValue( OtmBusinessObject.class.getSimpleName() );
        else
            controller.getMemberFilterController().setTypeFilter( (OtmLibraryMember) getSubject() );

        // Run dialog to get user selection
        if (controller.showAndWait( "MSG" ) == Results.OK) {
            selected = controller.getSelected();
            if (selected != null && selected.getValue() != null)
                doIt( selected.getValue() );
        } else
            return null; // User cancelled
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
            return doIt();

        if (data.getClass() == getSubject().getClass() || getSubject() instanceof OtmContextualFacet) {
            oldBaseType = getSubject().getBaseType();
            getSubject().setBaseType( (OtmObject) data );
            // log.debug( "Set base type of " + getSubject() + " to " + get() );
        } else
            return null;
        return get();
    }

    @Override
    public OtmObject get() {
        return getSubject().getBaseType();
    }

    @Override
    public OtmLibraryMember getSubject() {
        return (OtmLibraryMember) otm;
    }

    @Override
    public ValidationFindings getVetoFindings() {
        otm.isValid( true );
        return ValidationUtils.getRelevantFindings( VETOKEYS, otm.getFindings() );
    }

    @Override
    public boolean isValid() {
        if (otm.getOwningMember() != null)
            otm.getOwningMember().isValid( true ); // Update validation status
        return otm.isValid();
    }

    @Override
    public boolean setSubject(OtmObject subject) {
        if (!(subject instanceof OtmLibraryMember))
            return false;
        otm = subject;
        return true;
    }

    @Override
    public String toString() {
        return "Assigned Base Type: " + getSubject();
    }

    @Override
    public OtmObject undoIt() {
        getSubject().setBaseType( oldBaseType );
        oldBaseType = null;
        // log.debug( "Undo type assignment. Set to " + get() );
        return oldBaseType;
    }
}
