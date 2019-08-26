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
    public enum DexActions {
        NAMECHANGE,
        DESCRIPTIONCHANGE,
        TYPECHANGE,
        BASEPATHCHANGE,
        SETFIRSTCLASS,
        SETABSTRACT,
        SETIDGROUP,
        SETCOMMONACTION,
        SETRESOURCEEXTENSION,
        SETPARENTPARAMETERGROUP,
        SETPARENTPATHTEMPLATE,
        SETPARENTREFPARENT,
        SETAFREFERENCETYPE,
        SETAFREFERENCEFACET,
        SETREQUESTPAYLOAD,
        SETREQUESTPARAMETERGROUP,
        SETREQUESTMETHOD,
        SETREQUESTPATH,
        SETRESPONSEPAYLOAD,
        SETPARAMETERLOCATION,
        SETPARAMETERGROUPFACET
    }

    // /**
    // * <b>For use by DexActionManagers only.</b>
    // * <p>
    // * Get the action specific to the action name for the subject OtmObject.
    // *
    // * @param actionName
    // * @param subject
    // * @return
    // */
    // public DexAction<?> actionFactory(DexActions actionName, OtmObject subject);

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

    // // Why?
    // @Deprecated
    // public DexMainController getMainController();

    // // Why?
    // @Deprecated
    // public OtmModelManager getModelManager();

    public int getQueueSize();

    public boolean isEnabled(DexActions action, OtmObject subject);

    // public void postWarning(String warning);

    // /**
    // * <b>For use by DexActions only.</b>
    // * <p>
    // * Push performed action onto queue. This records the action to allow undo. Will validate results and warn user on
    // * errors.
    // * <p>
    // * Note: Veto'ed actions ({@link DexAction#getVetoFindings()} will {@link DexAction#undo()} and not be added to
    // the
    // * queue.
    // *
    // * @param action
    // */
    // public void push(DexAction<?> action);


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
     */
    public void undo();

    /**
     * Create a string property and add an action if editable and enabled.
     * 
     * @param action action to perform
     * @param currentValue of string
     * @param subject otmObject to change when string property changes
     * @return
     */
    public StringProperty add(DexActions action, String currentValue, OtmObject subject);

    /**
     * Create a boolean property and add an action if editable and enabled.
     * 
     * @param action action to perform
     * @param currentValue of boolean
     * @param subject otmObject to change when property changes
     * @return
     */
    public BooleanProperty add(DexActions action, boolean currentValue, OtmObject subject);

}
