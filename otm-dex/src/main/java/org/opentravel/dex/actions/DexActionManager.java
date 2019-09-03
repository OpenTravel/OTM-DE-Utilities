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
 * Dex action manager <b>public</b> interface. Action manager can work with FX observable properties using the
 * {@code add()} methods or with OtmObjects using {@code run()} actions. Various instances of {@code DexActionManager}
 * control what actions are enabled.
 * <p>
 * {@code add()} wraps the passed value in a FX observable property If {@code isEnabled()} the returned property is
 * writable and has an action assigned to its listener. If not, {@code add()} will return a read-only property.
 * <p>
 * {@code run()} is used in menu or button {@code onAction()} event handlers. If enabled, run executes the action on the
 * associated OtmObject. These actions must acquire their own data, typically from a modal dialog.
 * <p>
 * The action handler used will be retrieved from {@link DexActions}.
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
     * @param action to perform
     * @param boolean to wrap
     * @param subject otmObject to change when property changes
     * @return
     */
    public BooleanProperty add(DexActions action, boolean currentValue, OtmObject subject);

    /**
     * Create a string property and add an action if editable and enabled.
     * 
     * @param action to perform
     * @param string to wrap
     * @param subject otmObject to change when string property changes
     * @return simple string property if editable and action enabled, read only property otherwise
     */
    public StringProperty add(DexActions action, String currentValue, OtmObject subject);


    /**
     * Set a listener on the FX observable string property to invoke the action.
     * <p>
     * 
     * @deprecated - use {@link #add(DexActions, String, OtmObject)}
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

    public void postWarning(String warning);

    /**
     * Create an action and do it. If successful will be added to the queue.
     * <p>
     * Used for actions that are not associated with an observable property such as set assigned type that uses a dialog
     * to get the type to assign.
     * 
     * @param actionType what action to perform
     * @param subject OTM object to act upon
     */
    void run(DexActions actionType, OtmObject subject);

    // /**
    // * @deprecated
    // * @param actionType
    // * @param subject
    // * @param data used to modify the subject, may be null if action will run dialog to get data
    // */
    // void run(DexActions actionType, OtmObject subject, Object data);

    /**
     * Pop an action from the queue and then undo it.
     * 
     * @param publishEvent if true, publish the event associated with the action
     */
    public void undo();

}
