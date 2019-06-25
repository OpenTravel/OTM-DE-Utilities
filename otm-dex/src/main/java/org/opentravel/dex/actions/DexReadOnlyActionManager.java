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
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;

import javafx.beans.value.ObservableValue;

/**
 * Read Only action manager. All actions are disabled (isEnabled = false) and run commands ignored.
 * 
 * @author dmh
 *
 */
public class DexReadOnlyActionManager extends DexActionManagerBase {
    private static Log log = LogFactory.getLog( DexReadOnlyActionManager.class );
    // Controller for accessing GUI controls
    // DexMainController mainController = null;
    // Deque<DexAction<?>> queue;
    // private OtmModelManager modelManager = null;
    //
    // private boolean ignore;

    public DexReadOnlyActionManager() {
        super();
    }

    /**
     * {@inheritDoc} Does nothing and returns false.
     */
    @Override
    public boolean addAction(DexActions action, ObservableValue<? extends String> op, OtmModelElement<?> subject) {
        return false;
    }

    /**
     * {@inheritDoc} Does nothing and returns false.
     */
    @Override
    public void run(DexActions action, OtmObject subject, Object data) {
        // Do Nothing - READ ONLY!
    }

    @Override
    public boolean isEnabled(DexActions action, OtmObject subject) {
        return false;
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
    //
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
    // @Override
    // public void setModelManager(OtmModelManager otmModelManager) {
    // this.modelManager = otmModelManager;
    // }

    // public DexStringAction stringActionFactory(DexActions action, OtmModelElement<?> subject) {
    // // Make sure the action can register itself and access main controller
    // if (subject.getActionManager() == null)
    // throw new IllegalStateException("Subject of an action must provide access to action manger.");
    //
    // DexStringAction a = null;
    // switch (action) {
    // case NAMECHANGE:
    // a = new NameChangeAction(subject);
    // break;
    // case DESCRIPTIONCHANGE:
    // a = new DescriptionChangeAction(subject);
    // break;
    // default:
    // log.debug("Unknown action: " + action.toString());
    // }
    // return a;
    // }

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
