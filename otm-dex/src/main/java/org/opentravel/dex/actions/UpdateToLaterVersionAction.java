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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static Logger log = LogManager.getLogger( UpdateToLaterVersionAction.class );

    public static boolean isEnabled(OtmObject subject) {
        return subject != null && subject.isEditable();
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

        return true;
    }

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
        log.warn( "Error - must have a type provider to update." );
        return null;
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
