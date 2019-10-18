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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.actions.DexAction;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.model.OtmObject;
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
    private static Log log = LogFactory.getLog( DexMinorVersionActionManager.class );
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

    // Done - change description, deprecation, examples
    // Done - new property
    // Done - New properties to this version have full permission
    //
    // add role to core
    // add enum value, , service operation
    // assign type to later version of current type
    private boolean isAllowedInMinor(DexActions action, OtmObject subject) {
        switch (action) {
            case DESCRIPTIONCHANGE:
            case DEPRECATIONCHANGE:
            case EXAMPLECHANGE:
            case ADDPROPERTY:
                return true;
            default:
                return isNewProperty( subject );
        }
    }

    private boolean isNewProperty(OtmObject subject) {
        if (subject instanceof OtmProperty) {
            return !subject.isInherited(); // if not in latest minor, the lib will not be editable
        }
        return false;
    }
}
