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
import org.opentravel.dex.actions.DexActions;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.model.OtmObject;

/**
 * The <i>wizard</i> action manager implements isEnabled() to return true to allow wizards full control.
 * <p>
 * The wizard is an action manager with its own queue, so actions fired using the wizard action manager will not be
 * un-doable using other action managers.
 * 
 * @author dmh
 *
 */
public class DexWizardActionManager extends DexActionManagerBase {
    private static Log log = LogFactory.getLog( DexWizardActionManager.class );

    public DexWizardActionManager(DexMainController mainController) {
        super( mainController );
    }

    /**
     * Use reflection on the action to get the action handler's isEnabled method and return its result.
     * <p>
     * Note: this could be static but do NOT move to DexActions because there are multiple action managers.
     */
    @Override
    public boolean isEnabled(DexActions action, OtmObject subject) {
        return true;
    }

}
