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
import org.opentravel.common.ValidationUtils;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.util.ArrayDeque;
import java.util.Deque;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

/**
 * Action manager base class. Implements factory and queue management.
 * 
 * @author dmh
 *
 */
public abstract class DexActionManagerBase implements DexActionManagerCore {
    private static Log log = LogFactory.getLog( DexActionManagerBase.class );

    // public enum DexActions {
    // NAMECHANGE, DESCRIPTIONCHANGE, TYPECHANGE
    // }

    // Controller for accessing GUI controls
    private DexMainController mainController = null;
    private OtmModelManager modelManager = null; // Made available to callers, not used internally

    protected Deque<DexAction<?>> queue;
    protected boolean ignore;

    /**
     * Action manager that can not update status or display queue size and contents.
     */
    public DexActionManagerBase() {
        queue = new ArrayDeque<>();
    }

    /**
     * Action manager that can not update status or display queue size and contents.
     */
    public DexActionManagerBase(OtmModelManager modelManager) {
        this.modelManager = modelManager;
        queue = new ArrayDeque<>();
    }

    public DexActionManagerBase(DexMainController mainController) {
        this.mainController = mainController;
        queue = new ArrayDeque<>();
    }

    public DexActionManagerBase(DexMainController mainController, OtmModelManager modelManager) {
        this.mainController = mainController;
        this.modelManager = modelManager;
        queue = new ArrayDeque<>();
    }

    // /**
    // * Triggering of actions on observable properties is delegated to the observable via its listener.
    // *
    // * @param action
    // * @param op
    // * @param subject
    // * @return
    // */
    // public boolean addAction(DexActions action, ObservableValue<? extends String> op, OtmModelElement<?> subject) {
    // // Make sure the action can register itself and access main controller
    // if (subject.getActionManager() == null)
    // throw new IllegalStateException("Subject of an action must provide access to action manger.");
    //
    // switch (action) {
    // case NAMECHANGE:
    // op.addListener((ObservableValue<? extends String> o, String old,
    // String newVal) -> doString(new NameChangeAction(subject), o, old, newVal));
    // break;
    // case DESCRIPTIONCHANGE:
    // op.addListener((ObservableValue<? extends String> o, String old,
    // String newVal) -> doString(new DescriptionChangeAction(subject), o, old, newVal));
    // break;
    // default:
    // return false;
    // }
    // return true;
    // }
    //
    // /**
    // * Actions available for OTM Properties wrapped by PropertiesDAO.
    // *
    // * @param action
    // * @param subject
    // */
    // public void addAction(DexActions action, OtmObject subject) {
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
    // }
    //
    // public boolean isEnabled(DexActions action, OtmObject subject) {
    // switch (action) {
    // case TYPECHANGE:
    // return AssignedTypeChangeAction.isEnabled(subject);
    // default:
    // }
    // return false;
    // }

    // public void doString(DexStringAction action, ObservableValue<? extends String> o, String oldName, String name) {
    // if (!ignore) {
    // ignore = true;
    // action.doIt(o, oldName, name);
    // ignore = false;
    // }
    // }

    public BooleanProperty add(DexActions action, boolean currentValue, OtmObject subject) {
        BooleanProperty property = null;
        if (subject.isEditable() && isEnabled( action, subject )) {
            property = new SimpleBooleanProperty( currentValue );
            addAction( action, property, subject );
        } else
            property = new ReadOnlyBooleanWrapper( currentValue );
        return property;
    }

    @Override
    public StringProperty add(DexActions action, String value, OtmObject subject) {
        StringProperty property = null;
        if (subject.isEditable() && isEnabled( action, subject )) {
            property = new SimpleStringProperty( value );
            addAction( action, property, (OtmModelElement<?>) subject );
        } else
            property = new ReadOnlyStringWrapper( value );
        return property;
    }


    @Override
    public DexAction<?> actionFactory(DexActions actionType, OtmObject subject) {
        DexAction<?> action = null;
        switch (actionType) {
            case NAMECHANGE:
                action = new NameChangeAction( subject );
                break;
            case DESCRIPTIONCHANGE:
                action = new DescriptionChangeAction( subject );
                break;
            case TYPECHANGE:
                if (subject instanceof OtmTypeUser)
                    action = new AssignedTypeChangeAction( (OtmTypeUser) subject );
                break;
            default:
                log.debug( "Unknown action: " + actionType.toString() );
        }
        return action;
    }

    /**
     * Change Listener for actions on strings. Assigned to observable strings by
     * {@link DexFullActionManager#addAction(DexActionManager.DexActions, ObservableValue, OtmModelElement)}
     * 
     * @param action DexStringAction to execute
     * @param o observable
     * @param oldString
     * @param newString
     */
    protected void doString(DexStringAction action, ObservableValue<? extends String> o, String oldString,
        String newString) {
        if (!ignore) {
            ignore = true;
            action.doIt( o, oldString, newString );
            ignore = false;
        }
    }

    /**
     * Change Listener for actions on booleans.
     */
    protected void doBoolean(DexBooleanAction action, ObservableValue<? extends Boolean> o) {
        if (!ignore) {
            ignore = true;
            action.doIt( o );
            ignore = false;
        }
    }

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

    @Override
    public String getLastActionName() {
        return queue.peek() != null ? queue.peek().getClass().getSimpleName() : "";
    }

    // // Used to get imageMrg to OtmModelElement
    // @Override
    // public DexMainController getMainController() {
    // return mainController;
    // }

    // @Override
    // public OtmModelManager getModelManager() {
    // return modelManager;
    // }

    @Override
    public int getQueueSize() {
        return queue.size();
    }

    @Override
    public void postWarning(String warning) {
        if (mainController != null)
            mainController.postError( null, warning );

    }

    /**
     * Record action to allow undo. Will validate results and warn user on errors. Veto'ed actions will not be pushed
     * onto the queue.
     * 
     * @param action
     */
    @Override
    public void push(DexAction<?> action) {
        if (queue.contains( action )) {
            // TEST - make sure not a duplicate
            log.debug( "Duplicate Action found!" );
            return;
        }
        if (action.getVetoFindings() != null && !action.getVetoFindings().isEmpty()) {
            // Warn the user of the errors and back out the changes
            ValidationFindings findings = action.getVetoFindings();
            String msg = "Can not make change.\n" + ValidationUtils.getMessagesAsString( findings );
            if (mainController != null)
                mainController.postError( null, msg );
            ignore = true;
            action.undo();
            ignore = false;
            // TODO - if warnings, post them and allow undo option in dialog.
        } else {
            queue.push( action );
            if (mainController != null)
                mainController.updateActionQueueSize( getQueueSize() );
            if (mainController != null)
                mainController.postStatus( "Performed action: " + action.toString() );
            log.debug( "Put action on queue: " + action.getClass().getSimpleName() );
        }
        action.getSubject().getOwningMember().isValid( true ); // Force the owner to refresh its findings.

    }

    // /**
    // * @param otmModelManager
    // */
    // public void setModelManager(OtmModelManager otmModelManager) {
    // this.modelManager = otmModelManager;
    // }

    /**
     * Pop an action from the queue and then undo it.
     */
    @Override
    public void undo() {
        ignore = true;
        if (!queue.isEmpty()) {
            DexAction<?> action = queue.pop();
            log.debug( "Undo action: " + action.getClass().getSimpleName() );
            action.undo();
            action.getSubject().getOwningMember().isValid( true ); // Force the owner to refresh its findings.
            if (mainController != null)
                mainController.updateActionQueueSize( getQueueSize() );
            if (mainController != null)
                mainController.postStatus( "Undid action: " + action.toString() );
        }
        ignore = false;
    }

}
