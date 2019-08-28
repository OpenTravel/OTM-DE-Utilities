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

import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

/**
 * Dex action manager <b>public</b> interface.
 * <p>
 * Create actions associated with observable values or run actions associated with an OtmObject
 * <p>
 * Action managers control and manage actions. Maintains queue of past actions and creates new actions. Notifies user of
 * performed action status.
 * <p>
 * See {@link DexActionManagerCore} for <b>protected</b> methods.
 * 
 * @author dmh
 *
 */
public interface DexActionManager {

    /**
     * Create a boolean property and add an action if editable and enabled.
     * 
     * @param action action to perform
     * @param currentValue of boolean
     * @param subject otmObject to change when property changes
     * @return
     */
    public BooleanProperty add(DexActions action, boolean currentValue, OtmObject subject);

    /**
     * Create a string property and add an action if editable and enabled.
     * 
     * @param action action to perform
     * @param currentValue of string
     * @param subject otmObject to change when string property changes
     * @return simple string property if editable and action enabled, read only property otherwise
     */
    public StringProperty add(DexActions action, String currentValue, OtmObject subject);

    public boolean addAction(DexActions action, ObservableValue<? extends Boolean> property, OtmObject subject);

    /**
     * Set a listener on the FX observable string property to invoke the action.
     * <p>
     * To be deprecated, it is preferred to use {@link #add(DexActions, String, OtmObject)}
     * 
     * @param action
     * @param op
     * @param subject
     * @return
     */
    public boolean addAction(DexActions action, ObservableValue<? extends String> op, OtmModelElement<?> subject);

    public String getLastActionName();

    public int getQueueSize();

    public boolean isEnabled(DexActions action, OtmObject subject);

    /**
     * Create an action and do it. If successful will be added to the queue.
     * <p>
     * Used for actions that are not associated with an observable property such as set assigned type that uses a dialog
     * to get the type to assign.
     * 
     * @param actionType what action to perform
     * @param subject OTM object to act upon
     * @param data used to modify the subject, may be null if action will run dialog to get data
     */
    void run(DexActions actionType, OtmObject subject, Object data);

    /**
     * Pop an action from the queue and then undo it.
     * 
     * @param publishEvent if true, publish the event associated with the action
     */
    public void undo();

}
