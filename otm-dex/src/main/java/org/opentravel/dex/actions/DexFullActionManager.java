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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;

import javafx.beans.value.ObservableValue;

/**
 * Default action manager used by OTM elements to determine what actions are available and how to execute them.
 * <p>
 * Controls and manages actions. Maintains queue of past actions and creates new actions. Notifies user of performed
 * action status.
 * 
 * @author dmh
 *
 */
public class DexFullActionManager extends DexActionManagerBase {
    // public enum DexActions {
    // NAMECHANGE, DESCRIPTIONCHANGE, TYPECHANGE
    // }

    private static Log log = LogFactory.getLog( DexFullActionManager.class );
    // Controller for accessing GUI controls
    // DexMainController mainController = null;
    // Deque<DexAction<?>> queue;
    //
    // private OtmModelManager modelManager = null;
    // private boolean ignore;

    public DexFullActionManager(DexMainController mainController) {
        super( mainController );
        // this.mainController = mainController;
        // queue = new ArrayDeque<>();
    }

    /**
     * Triggering of actions on observable properties is delegated to the observable via its listener.
     * 
     * @param action
     * @param op
     * @param subject
     * @return
     */
    @Override
    public boolean addAction(DexActions action, ObservableValue<? extends String> op, OtmModelElement<?> subject) {
        // Make sure the action can register itself and access main controller
        if (subject.getActionManager() == null)
            throw new IllegalStateException( "Subject of an action must provide access to action manger." );

        switch (action) {
            case NAMECHANGE:
                op.addListener( (ObservableValue<? extends String> o, String old,
                    String newVal) -> doString( new NameChangeAction( subject ), o, old, newVal ) );
                break;
            case DESCRIPTIONCHANGE:
                op.addListener( (ObservableValue<? extends String> o, String old,
                    String newVal) -> doString( new DescriptionChangeAction( subject ), o, old, newVal ) );
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * Actions available for OTM Properties wrapped by PropertiesDAO.
     * 
     * @param action
     * @param subject
     */
    @Override
    public void run(DexActions actionType, OtmObject subject, Object data) {
        actionFactory( actionType, subject ).doIt( data );
        // switch (action) {
        // case TYPECHANGE:
        // if (AssignedTypeChangeAction.isEnabled(subject)) {
        // ignore = true; // may fire a name change
        // new AssignedTypeChangeAction((OtmTypeUser) subject).doIt();
        // ignore = false;
        // }
        // break;
        // default:
        // }
    }

    @Override
    public boolean isEnabled(DexActions action, OtmObject subject) {
        switch (action) {
            case TYPECHANGE:
                return AssignedTypeChangeAction.isEnabled( subject );
            default:
        }
        return false;
    }

    public void doString(DexStringAction action, ObservableValue<? extends String> o, String oldName, String name) {
        if (!ignore) {
            ignore = true;
            action.doIt( o, oldName, name );
            ignore = false;
        }
    }

    // @Override
    // public String getLastActionName() {
    // return queue.peek() != null ? queue.peek().getClass().getSimpleName() : "";
    // }
    //
    // @Override
    // public DexMainController getMainController() {
    // return mainController;
    // }
    //
    // @Override
    // public OtmModelManager getModelManager() {
    // return modelManager;
    // }
    //
    // @Override
    // public int getQueueSize() {
    // return queue.size();
    // }

    // @Override
    // public void postWarning(String warning) {
    // mainController.postError(null, warning);
    //
    // }

    // /**
    // * Record action to allow undo. Will validate results and warn user on errors. Veto'ed actions will not be pushed
    // * onto the queue.
    // *
    // * @param action
    // */
    // @Override
    // public void push(DexAction<?> action) {
    // if (queue.contains(action)) {
    // // TEST - make sure not a duplicate
    // log.debug("Duplicate Action found!");
    // return;
    // }
    // if (action.getVetoFindings() != null && !action.getVetoFindings().isEmpty()) {
    // // Warn the user of the errors and back out the changes
    // ValidationFindings findings = action.getVetoFindings();
    // String msg = "Can not make change.\n" + ValidationUtils.getMessagesAsString(findings);
    // mainController.postError(null, msg);
    // ignore = true;
    // action.undo();
    // ignore = false;
    // // TODO - if warnings, post them and allow undo option in dialog.
    // } else {
    // queue.push(action);
    // mainController.updateActionQueueSize(getQueueSize());
    // mainController.postStatus("Performed action: " + action.toString());
    // log.debug("Put action on queue: " + action.getClass().getSimpleName());
    // }
    // action.getSubject().getOwningMember().isValid(true); // Force the owner to refresh its findings.
    //
    // }

    // /**
    // * @param otmModelManager
    // */
    // public void setModelManager(OtmModelManager otmModelManager) {
    // this.modelManager = otmModelManager;
    // }

    public DexStringAction stringActionFactory(DexActions action, OtmModelElement<?> subject) {
        // Make sure the action can register itself and access main controller
        if (subject.getActionManager() == null)
            throw new IllegalStateException( "Subject of an action must provide access to action manger." );

        DexStringAction a = null;
        switch (action) {
            case NAMECHANGE:
                a = new NameChangeAction( subject );
                break;
            case DESCRIPTIONCHANGE:
                a = new DescriptionChangeAction( subject );
                break;
            default:
                log.debug( "Unknown action: " + action.toString() );
        }
        return a;
    }

    // /**
    // * Pop an action from the queue and then undo it.
    // */
    // @Override
    // public void undo() {
    // ignore = true;
    // if (!queue.isEmpty()) {
    // DexAction<?> action = queue.pop();
    // log.debug("Undo action: " + action.getClass().getSimpleName());
    // action.undo();
    // action.getSubject().getOwningMember().isValid(true); // Force the owner to refresh its findings.
    // mainController.updateActionQueueSize(getQueueSize());
    // mainController.postStatus("Undid action: " + action.toString());
    // }
    // ignore = false;
    // }

}
