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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The <i>full</i> action manager implements isEnabled() to return the value from the actions. This class extends
 * {@link DexActionManagerBase} which controls and manages actions; maintains queue of actions and notifies user of
 * performed action status.
 * <p>
 * To disable editing, use {@link DexReadOnlyActionManager}
 * 
 * @author dmh
 *
 */
public class DexFullActionManager extends DexActionManagerBase {
    private static Log log = LogFactory.getLog( DexFullActionManager.class );

    public DexFullActionManager(DexMainController mainController) {
        super( mainController );
    }

    /**
     * Is the action enabled for the subject? Subject is assumed to be editable because this action manager must only
     * available to editable objects.
     * <p>
     * Uses reflection on the action to get the action handler's isEnabled method and returns its result. Failure to
     * invoke returns a result of false.
     * <p>
     * Note: this could be static but is not to assure the action managed is retrieved from the appropriate object. Do
     * NOT move to DexActions because there are multiple action managers.
     */
    @Override
    public boolean isEnabled(DexActions action, OtmObject subject) {
        boolean result = false;
        if (subject != null)
            try {
                Method m = action.actionClass().getMethod( "isEnabled", OtmObject.class );
                result = (boolean) m.invoke( null, subject );
                // log.debug( "Method " + action.toString() + " isEnabled invoke result: " + result );
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
                log.error( "Could not invoke " + action.actionClass() + ".isEnabled( ): " + e.getMessage() );
            }
        return result;
    }

}
