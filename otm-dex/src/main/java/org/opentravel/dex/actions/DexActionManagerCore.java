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

import org.opentravel.model.OtmObject;

import javafx.beans.value.ObservableValue;

/**
 * DEx action manager <b>protected</b> interface. Intended <b>only</b> for use by actions and action managers.
 * <p>
 * Other users should only rely upon methods in {@link DexActionManager}
 * 
 * @author dmh
 *
 */
public interface DexActionManagerCore extends DexActionManager {

    /**
     * Get the action specific to the action name for the subject OtmObject.
     * 
     * @deprecated - create add(action, object) signature for these Only used for "run" actions <b>For use by
     *             DexActionManagers only.</b>
     * 
     * @param actionName
     * @param subject
     * @return
     */
    @Deprecated
    public DexAction<?> actionFactory(DexActions actionName, OtmObject subject);


    public void postWarning(String warning);

    /**
     * <b>For use by DexActions only.</b>
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
     * @param action
     * @param property
     * @param subject
     * @return
     */
    boolean addAction(DexActions action, ObservableValue<? extends Boolean> o, OtmObject subject);

    // /**
    // * @return
    // */
    // boolean addAction();


    // /**
    // * Create an action and do it. If successful will be added to the queue.
    // * <p>
    // * Used for actions that are not associated with an observable property such as set assigned type that uses a
    // dialog
    // * to get the type to assign.
    // *
    // * @param actionType what action to perform
    // * @param subject OTM object to act upon
    // * @param data used to modify the subject, may be null if action will run dialog to get data
    // */
    // void run(DexActions actionType, OtmObject subject, Object data);
    //
    // /**
    // * Pop an action from the queue and then undo it.
    // */
    // public void undo();

    // /**
    // * Create a string property and add an action if editable and enabled.
    // *
    // * @param action action to perform
    // * @param currentValue of string
    // * @param subject otmObject to change when string property changes
    // * @return
    // */
    // public StringProperty add(DexActions action, String currentValue, OtmObject subject);

}
