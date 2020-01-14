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
import org.opentravel.dex.events.DexChangeEvent;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmXsdElement;
import org.opentravel.model.otmLibraryMembers.OtmXsdSimple;

import java.util.List;

/**
 * Update the type user to a later version of the type provider.
 */
public class UpdateToLaterVersionAction extends DexRunAction {
    private static Log log = LogFactory.getLog( UpdateToLaterVersionAction.class );

    // private static final String VETO1 = "org.opentravel.schemacompiler.TLProperty.name.ELEMENT_REF_NAME_MISMATCH";
    // private static final String VETO2 = ".OBSOLETE_TYPE_REFERENCE";
    // private static final String VETO3 = ".ILLEGAL_REFERENCE";
    // private static final String[] VETOKEYS = {VETO1, VETO2, VETO3};

    public static boolean isEnabled(OtmObject subject) {
        if (!subject.isEditable())
            return false;
        return true;
    }

    /**
     * Return true if the subject is editable and there is a later version of type provider
     * 
     * @param subject
     * @return
     */
    public static boolean isEnabled(OtmObject subject, OtmObject provider) {
        if (subject == null || !subject.isEditable())
            return false;
        if (!(provider instanceof OtmTypeProvider))
            return false;
        if (provider instanceof OtmXsdElement || provider instanceof OtmXsdSimple)
            return false;

        if (provider.getOwningMember() == null)
            return false;
        if (provider.getOwningMember().isLatestVersion())
            return false;
        // if (provider.getLibrary() == null)
        // return false;
        // OtmVersionChain chain = provider.getLibrary().getVersionChain();
        // if (chain == null)
        // return false;
        // //
        // if (chain.isLatestChain() && chain.isLatestVersion( provider.getOwningMember() ))
        // return false;

        return true;
    }

    // private OtmTypeUser user = null;
    // private OtmTypeUser newUser = null;
    private OtmLibraryMember member = null;
    private OtmTypeProvider provider = null;

    // The type provider that replaces the provider
    private OtmTypeProvider replacement = null;
    // All users assigned the replacement
    private List<OtmTypeUser> users = null;

    private DexChangeEvent event = null;

    public UpdateToLaterVersionAction() {
        // Constructor for reflection
    }

    /**
     * {@inheritDoc} Override with the Delete member event constructed by undo when a minor version change that creates
     * new library member
     * 
     */
    @Override
    public DexChangeEvent getEvent() {
        return event == null ? super.getEvent() : event;
    }

    /**
     * This action will get the data from the user via modal dialog
     */
    public OtmTypeProvider doIt() {
        log.debug( "Ready to update assigned type to later version." + otm + " " + ignore );
        // if (ignore)
        return null;
        // if (user == null || !(otm instanceof OtmTypeUser))
        // return null;
        // if (actionManager == null)
        // return null;
        // if (otm.getOwningMember() == null)
        // return null;
        // if (otm.getLibrary() == null)
        // return null;
        // OtmModelManager modelMgr = otm.getOwningMember().getModelManager();
        // if (modelMgr == null)
        // return null;

        // // Setup controller to get the users selection
        // TypeSelectionContoller controller = TypeSelectionContoller.init();
        // controller.setManager( modelMgr );
        //
        // // Find out if this typeUser is in a minor and not new to chain
        // // user = (OtmTypeUser) otm;
        // if (!user.isEditable() && user.getLibrary().isChainEditable()) {
        // // Create a new property in a new minor version of the owning member
        // newUser = user.getLibrary().getVersionChain().getNewMinorTypeUser( user );
        // if (newUser == null) {
        // otm.getActionManager().postWarning( "Error creating minor version of " + otm.getOwningMember() );
        // return null;
        // }
        // // set filter and event
        // controller.getMemberFilterController().setMinorVersionFilter( user.getAssignedType() );
        // event = new OtmObjectReplacedEvent( newUser, user );
        // log.debug( "Created new user in new minor version of " + newUser.getOwningMember() );
        // }

        // // Set applicable filters
        // if (otm instanceof OtmResource)
        // controller.getMemberFilterController().setTypeFilterValue( OtmLibraryMemberType.BUSINESS );
        // if (otm instanceof OtmActionFacet)
        // controller.getMemberFilterController().setTypeFilterValue( OtmLibraryMemberType.CORE );
        //
        // // Get the user's selected new provider
        // controller.showAndWait( "MSG" );
        // OtmTypeProvider selected = controller.getSelectedProvider();
        // if (selected != null && controller.getResult() == Results.OK) {
        // doIt( selected );
        // } else {
        // // handle cancel and bad selection when newUser created
        // log.error( "Canceled or missing selection from Type Selection Controller" );
        // if (newUser != null && newUser.getLibrary() != null)
        // newUser.getLibrary().delete( newUser.getOwningMember() );
        // return null;
        // }
        //
        // return get();
    }

    /**
     * 
     * @return
     */
    @Override
    public Object doIt(Object type) {
        // log.debug( "Ready to update assigned type to later version. " + otm + " provider = " + type );
        if (getSubject() != null && type instanceof OtmTypeProvider) {
            provider = (OtmTypeProvider) type;

            // Get the latest member and handle case where provider is not an library member
            OtmLibraryMember latest = otm.getModelManager().getLatestMember( provider.getOwningMember() );
            replacement = latest.getMatchingProvider( provider );
            log.debug( "Replacement is " + replacement );

            if (replacement != null) {
                users = getSubject().getTypeUsers( provider ); // Find otm's properties that use provider
                users.forEach( u -> u.setAssignedType( replacement ) ); // set assigned type
            }
        }
        return replacement;
    }
    // TODO - Future - see if other types from the provider library are used and if so prompt user to see if they want
    // to upgrade those too.


    @Override
    public OtmTypeProvider get() {
        return replacement;
    }

    // @Override
    // public ValidationFindings getVetoFindings() {
    // // checkNewUser();
    // // return ValidationUtils.getRelevantFindings( VETOKEYS, otm.getFindings() );
    // return null;
    // }

    // @Override
    // public boolean isValid() {
    // // checkNewUser();
    // if (otm.getOwningMember() != null)
    // otm.getOwningMember().isValid( true ); // Update validation status
    // return otm.isValid( true ) ? true
    // : ValidationUtils.getRelevantFindings( VETOKEYS, otm.getFindings() ).isEmpty();
    // }

    @Override
    public boolean setSubject(OtmObject subject) {
        if (!(subject instanceof OtmLibraryMember))
            return false;
        otm = subject;
        return true;
    }

    @Override
    public OtmLibraryMember getSubject() {
        return otm instanceof OtmLibraryMember ? (OtmLibraryMember) otm : null;
    }

    @Override
    public String toString() {
        return "Updated assigned type: " + replacement;
    }


    @Override
    public OtmTypeProvider undoIt() {
        users.forEach( u -> u.setAssignedType( provider ) );
        return provider;
    }
}
