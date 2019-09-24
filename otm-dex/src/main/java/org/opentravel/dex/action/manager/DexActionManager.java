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

import org.opentravel.dex.actions.DexAction;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.model.OtmObject;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;

/**
 * Performing actions that change OtmObjects uses three interfaces:
 * <ol>
 * <li>{@link DexAction} that defines the api that each individual action must implement
 * <li>{@link DexActions} that identifies all the actions and related events. It also acts as factory using reflection
 * to create action objects.
 * <li>{@link DexActionManager} that is described here and is shared via the OtmModelManager and OtmLibraries.
 * </ol>
 * Dex action manager interface. Action manager can work with FX observable properties using the {@code add()} methods
 * or with OtmObjects using {@code run()} actions. Various sub-types of {@code DexActionManager} control what actions
 * are enabled.
 * <p>
 * {@code add()} wraps the passed value in a FX observable property If {@code isEnabled()} the returned property is
 * writable and has an action assigned to its listener. If not, {@code add()} will return a read-only property.
 * <p>
 * {@code run()} is used in menu or button {@code onAction()} event handlers. If enabled, run executes the action on the
 * associated OtmObject. These actions must acquire their own data, typically from a modal dialog.
 * <p>
 * The action handler used will be retrieved from {@link DexActions}.
 * <p>
 * Action managers control and manage actions; maintain queue of past actions; creates new actions; and notifies user of
 * performed action status.
 * 
 * @author dmh
 *
 */
public interface DexActionManager {

    /**
     * Create a boolean property and add an action if editable and enabled.
     * <p>
     * Action will be triggered by changes to the observable property that is returned. When that action is successfully
     * triggered (pushed onto queue) the action is replaced with a new action.
     * 
     * @param action to perform
     * @param boolean to wrap
     * @param subject otmObject to change when property changes
     * @return
     */
    public BooleanProperty add(DexActions action, boolean currentValue, OtmObject subject);

    /**
     * Create a string property and add an action if editable and enabled.
     * <p>
     * Action will be triggered by changes to the observable property that is returned. When that action is successfully
     * triggered (pushed onto queue) the action is replaced with a new action.
     * 
     * @param action to perform
     * @param string to wrap
     * @param subject otmObject to change when string property changes
     * @return simple string property if editable and action enabled, read only property otherwise
     */
    public StringProperty add(DexActions action, String currentValue, OtmObject subject);

    /**
     * Get the class name of the last action put onto the queue.
     * 
     * @return
     */
    public String getLastActionName();

    /**
     * @return the main controller for GUI controls
     */
    public DexMainController getMainController();

    /**
     * 
     * @return number of entries in the queue
     */
    public int getQueueSize();

    /**
     * Is the action enabled for the subject?
     * <p>
     * If the type of action manager supports editing and the object's owning member is editable then it returns the
     * result of the action's static isEnabled() method.
     * 
     * @param action
     * @param subject
     * @return true if action can be performed
     */
    public boolean isEnabled(DexActions action, OtmObject subject);

    /**
     * If the controller has been set, use it to post the status message.
     * 
     * @param status
     */
    void postStatus(String status);

    /**
     * If the controller has been set, use it to post the warning message.
     * ({@link DexMainController#postError(Exception, String)} displays warning in a dialog box).
     * 
     * @param warning
     */
    public void postWarning(String warning);

    /**
     * <b>For use by DexActions only.</b> Push is automatically performed when actions are executed.
     * <p>
     * Push performed action onto queue. This records the action to allow undo. Will validate results and warn user on
     * errors.
     * <p>
     * Note: Veto'ed actions ({@link DexAction#getVetoFindings()} will {@link DexAction#undoIt()} and not be added to
     * the queue.
     * 
     * @param action
     */
    public void push(DexAction<?> action);

    /**
     * Create an action and do it. If successful the action will be added to the queue.
     * <p>
     * Used for actions that are not associated with an observable property such as set assigned type that uses a dialog
     * to get the type to assign. To pass in the value, see {@link #run(DexActions, OtmObject, Object)}
     * 
     * @param actionType what action to perform
     * @param subject OTM object to act upon
     */
    public Object run(DexActions actionType, OtmObject subject);

    /**
     * Create an action and do it. If successful the action will be added to the queue.
     * <p>
     * Used for actions that are not associated with an observable property such as set assigned type that uses a dialog
     * to get the type to assign. If the action gets its own value from controller or other sources, see
     * {@link #run(DexActions, OtmObject)}
     * 
     * @see #run(DexActions, OtmObject)
     * @param actionType what action to perform
     * @param subject OTM object to act upon
     * @param data passed to the actions's doIt() method used to modify the subject, may be null if action will run
     *        dialog to get data
     */
    public Object run(DexActions actionType, OtmObject subject, Object data);

    public void setMainController(DexMainController mainController);

    /**
     * Pop an action from the queue and then undo it. The event associated with the action, if any, will be published.
     * 
     */
    public void undo();

    /**
     * Peek at the last action put onto the queue. See {@link #undo()}
     * 
     * @return
     */
    public DexAction getLastAction();

}
