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

package org.opentravel.dex.action.manager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.dex.actions.DexAction;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.dex.actions.string.NameChangeAction;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmContainers.OtmVersionChain;
import org.opentravel.model.otmFacets.OtmAlias;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.OtmProperty;

/**
 * The <i>minor version</i> action manager first checks its lists to rule out actions not allowed in minor versions. If
 * allowed, it then checks the action's isEnabled() to return the value from the actions. This class extends
 * {@link DexActionManagerBase} which controls and manages actions; maintains queue of actions and notifies user of
 * performed action status.
 * <p>
 * To disable editing, use {@link DexReadOnlyActionManager}
 * 
 * @author dmh
 *
 */
public class DexMinorVersionActionManager extends DexActionManagerBase {
    private static Logger log = LogManager.getLogger( DexMinorVersionActionManager.class );
    private DexActionManager actionManager = null;

    /**
     * Creates an action manager for minor versions. Anything allowed in minor versions will be pushed
     * {@link DexActionManager#push(DexAction)} to the full action manager allowing sharing the queue.
     * 
     * @param fullActionManager
     */
    public DexMinorVersionActionManager(DexActionManager fullActionManager) {
        super();
        this.actionManager = fullActionManager;
        this.mainController = fullActionManager.getMainController();
    }

    /**
     * {@inheritDoc} Use the queue from the passed action manager.
     */
    @Override
    public int getQueueSize() {
        return actionManager.getQueueSize();
    }

    /**
     * {@inheritDoc} Use the queue from the passed action manager.
     */
    @Override
    public void push(DexAction<?> action) {
        actionManager.push( action );
    }

    /**
     * Use reflection on the action to get the action handler's isEnabled method and return its result.
     * <p>
     * Note: this could be static but do NOT move to DexActions because there are multiple action managers.
     */
    @Override
    public boolean isEnabled(DexActions action, OtmObject subject) {
        if (!isAllowedInMinor( action, subject ))
            return false;
        if (actionManager != null)
            return actionManager.isEnabled( action, subject );
        return false;
    }
    // TODO - implement two param isEnabled for drag-n-drop on minor

    // From language specification document
    // 1. Any new term can be defined
    // 2. Existing versioned terms (see section 11.3) can only be modified by adding indicators, optional attributes, or
    // optional element declarations
    // 3. New enumerated values can be added to both open and closed enumerations
    // 4. Extension point facets cannot be modified in a minor version library

    // Done - change description, deprecation, examples
    // Done - new property
    // Done - New properties to this version have full permission
    //
    // add role to core
    // add enum value, , service operation
    // assign type to later version of current type
    private boolean isAllowedInMinor(DexActions action, OtmObject subject) {
        if (action == null)
            return false;
        switch (action) {
            case ADDALIAS:
                return true;
            case DELETELIBRARYMEMBER:
            case NAMECHANGE:
                // Allow name change to objects new to the chain
                return isNewToChain( subject ) && NameChangeAction.isEnabled( subject );
            // if (subject instanceof OtmLibraryMember && subject.getLibrary() != null
            // && subject.getLibrary().getVersionChain() != null)
            // return subject.getLibrary().getVersionChain().isNewToChain( (OtmLibraryMember) subject );
            // return isNewProperty( subject );
            //
            case TYPECHANGE:
            case ASSIGNSUBJECT:
                if (isNewToChain( subject ))
                    return true;
                if (subject instanceof OtmTypeUser)
                    return canAssignLaterVersion( (OtmTypeUser) subject );

                // if (subject instanceof OtmTypeUser) {
                // if (subject.getLibrary() != null && subject.getLibrary().isChainEditable())
                // return subject.getLibrary().getVersionChain().canAssignLaterVersion( (OtmTypeUser) subject );
                // }
                return false;
            case DESCRIPTIONCHANGE:
            case DEPRECATIONCHANGE:
            case EXAMPLECHANGE:
            case COPYLIBRARYMEMBER:
            case NEWLIBRARYMEMBER:
            case ADDPROPERTY:
                return true;
            case ADDRESOURCECHILD:
                // You can add a resource child in a minor version
                return subject.getLibrary().isChainEditable();
            default:
                return isNewProperty( subject );
        }

        // case ADDALIAS:
        // Need to be able to rename and delete before enabling add
    }

    /**
     * Is the subject inherited? If so, it is not new to this owner.
     * 
     * @param subject
     * @return
     */
    public static boolean isNewProperty(OtmObject subject) {
        if (subject instanceof OtmProperty)
            return !subject.isInherited(); // if not in latest minor, the lib will not be editable
        if (subject instanceof OtmResourceChild)
            return !subject.isInherited();
        return false;
    }

    /**
     * See {@link OtmVersionChain#canAssignLaterVersion(OtmTypeUser) }
     * 
     * @param subject type user
     * @return true only if there is a later version of the type assigned to the subject type user
     */
    public static boolean canAssignLaterVersion(OtmTypeUser subject) {
        if (subject == null || subject.getLibrary() == null)
            return false;

        if (subject.getLibrary().getVersionChain() == null)
            return false;

        if (!subject.getLibrary().isChainEditable())
            return false;

        return subject.getLibrary().getVersionChain().canAssignLaterVersion( subject );
    }

    /**
     * Is the subject new to a chain?
     * <p>
     * <li>If a library member, is it in the latest version of the chain?
     * <li>If a property, is it inherited?
     * <li>If a resource child, is it inherited?
     * <li>Is the owning member new to chain?
     * 
     * @param subject
     * @return
     */
    public static boolean isNewToChain(OtmObject subject) {
        if (subject == null)
            return false;
        if (subject instanceof OtmLibraryMember)
            return subject.getLibrary().isLatestVersion();
        if (subject instanceof OtmAlias)
            return !subject.isInherited();
        if (subject instanceof OtmProperty)
            return !subject.isInherited(); // if not in latest minor, the lib will not be editable
        if (subject instanceof OtmResourceChild)
            return !subject.isInherited();
        if (subject.getLibrary() != null && subject.getLibrary().getVersionChain() != null)
            return (subject.getLibrary().getVersionChain().isNewToChain( subject.getOwningMember() ));

        return false;
    }
}
