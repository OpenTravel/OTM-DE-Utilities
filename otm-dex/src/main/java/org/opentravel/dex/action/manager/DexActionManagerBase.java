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
import org.opentravel.common.ValidationUtils;
import org.opentravel.dex.actions.DexAction;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.dex.actions.DexBooleanAction;
import org.opentravel.dex.actions.DexRunAction;
import org.opentravel.dex.actions.DexStringAction;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.events.DexChangeEvent;
import org.opentravel.model.OtmObject;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.util.ArrayDeque;
import java.util.Deque;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Action manager base class. Implements add, change handlers, run, setListener and queue management.
 * 
 * @author dmh
 *
 */
public abstract class DexActionManagerBase implements DexActionManager {
    private static Log log = LogFactory.getLog( DexActionManagerBase.class );

    // Development Notes:
    // "The ObservableValue stores a strong reference to the listener which will prevent the listener from being garbage
    // collected and may result in a memory leak. (If the observable outlives the listener.) It is recommended to either
    // unregister a listener by calling removeListener after use or to use an instance of WeakChangeListener avoid this
    // situation."
    //
    // To protect against this, we add the listener and observable to the action and remove the listener when the action
    // is successfully pushed onto the queue. This also assures actions are not reused and rejected by push() as
    // duplicates.

    // Controller for accessing GUI controls
    protected DexMainController mainController = null;

    // Queue for holding actions that were successful
    private final Deque<DexAction<?>> queue = new ArrayDeque<>();

    protected boolean ignore;

    /**
     * Action manager that can not update status or display queue size and contents.
     */
    public DexActionManagerBase() {}

    /**
     * Action manager that can update status and display queue size and contents.
     * 
     * @param mainController for access to status and queue, can be null.
     */
    public DexActionManagerBase(DexMainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public BooleanProperty add(DexActions action, boolean currentValue, OtmObject subject) {
        BooleanProperty property = null;
        if (isEnabled( action, subject )) {
            property = new SimpleBooleanProperty( currentValue );
            setListener( property, action, subject );
        } else
            property = new ReadOnlyBooleanWrapper( currentValue );
        return property;
    }


    @Override
    public StringProperty add(DexActions action, String currentValue, OtmObject subject) {
        StringProperty property = null;
        if (isEnabled( action, subject )) {
            property = new SimpleStringProperty( currentValue );
            setListener( property, action, subject );
        } else
            property = new ReadOnlyStringWrapper( currentValue );
        return property;
    }

    @Override
    public void clearQueue() {
        queue.clear();
    }

    /**
     * Change Listener for actions on booleans.
     */
    protected void doBoolean(DexBooleanAction action, ObservableValue<? extends Boolean> o) {
        if (!ignore) {
            ignore = true;
            action.doIt( o );
            push( action );
            ignore = false;
        }
    }

    /**
     * Change Listener for actions on strings.
     * 
     * @param action DexStringAction to execute
     * @param o observable
     * @param oldString
     * @param newString
     */
    protected void doString(DexStringAction action, ObservableValue<? extends String> o, String oldString,
        String newString) {
        if (!ignore) {
            // ignore = true;
            action.doIt( o, oldString, newString );
            // ignore = false;
            push( action );
        }
    }

    @Override
    public DexAction getLastAction() {
        return queue.peek() != null ? queue.peek() : null;
    }

    @Override
    public String getLastActionName() {
        return queue.peek() != null ? queue.peek().getClass().getSimpleName() : "";
    }

    public DexMainController getMainController() {
        return mainController;
    }

    @Override
    public int getQueueSize() {
        return queue.size();
    }

    /**
     * Override if two parameter isEnabled() actions are supported.
     * 
     * @see org.opentravel.dex.action.manager.DexActionManager#isEnabled(org.opentravel.dex.actions.DexActions,
     *      org.opentravel.model.OtmObject, org.opentravel.model.OtmObject)
     */
    @Override
    public boolean isEnabled(DexActions action, OtmObject subject, OtmObject target) {
        if (target == null)
            return isEnabled( action, subject );
        return false;
    }

    @Override
    public void postStatus(String status) {
        if (mainController != null)
            mainController.postStatus( status );
    }

    @Override
    public void postWarning(String warning) {
        if (mainController != null)
            mainController.postError( null, warning );
    }

    @Override
    public void postError(Exception e, String warning) {
        if (mainController != null)
            mainController.postError( e, warning );
    }

    @Override
    public void push(DexAction<?> action) {
        if (action == null) {
            // log.debug( "Pushed a null action!" );
            return;
        }
        // log.debug( "Pushing action onto queue: " + ignore + " " + action.toString() );
        action.isValid();
        // Refresh is much more light weight than full validation/resolver
        if (action.getSubject() != null)
            action.getSubject().refresh();

        if (queue.contains( action )) {
            // Make sure not a duplicate
            log.warn( "Duplicate Action found!" );
            return;
        }
        ValidationFindings findings = action.getVetoFindings();
        if (findings != null && !findings.isEmpty()) {
            // Warn the user of the errors and back out the changes
            String msg = "Can not make change.\n" + ValidationUtils.getMessagesAsString( findings );
            if (mainController != null)
                mainController.postError( null, msg );
            ignore = true;
            action.undoIt();
            ignore = false;
            log.warn( "Action vetoed!" );
            // Possible enhancement - if warnings, post them and allow undo option in dialog.
            return;
        }

        queue.push( action );

        // Now that the action is on the queue, release its listener and create a new action for the observable
        action.removeChangeListener();
        setListener( action.getObservable(), action.getType(), action.getSubject() );

        // Throw an event if defined
        DexChangeEvent event = action.getEvent();
        if (event != null && mainController != null) {
            event.set( action.getSubject() );
            action.isValid();
            mainController.publishEvent( event );
            // log.debug( "Action manager threw event: " + event.toString() );
        }
        action.isValid();

        // Let the user know what happened
        if (mainController != null) {
            mainController.updateActionManagerDisplay( this );
            mainController.postStatus( "Performed action: " + action.toString() );
        }
        // Let the action update the action appropriate validation status
        action.isValid();

        log.debug( "Put action on queue: " + action.getClass().getSimpleName() );
    }

    @Override
    public Object run(DexActions action, OtmObject subject) {
        return run( action, subject, null );
    }

    @Override
    public Object run(DexActions action, OtmObject subject, Object value) {
        DexAction<?> actionHandler = null;
        Object result = null;
        if (subject == null)
            return null;
        try {
            if (value instanceof OtmObject)
                actionHandler = DexActions.getAction( action, subject, (OtmObject) value, this );
            else
                actionHandler = DexActions.getAction( action, subject, this );
        } catch (ExceptionInInitializerError | InstantiationException | IllegalAccessException | SecurityException
            | IllegalArgumentException e) {
            log.warn( "Could not create action. " + e.getLocalizedMessage() );
        }

        // If run action was successfully created, run it.
        if (actionHandler instanceof DexRunAction) {
            result = run( (DexRunAction) actionHandler, value );
        } else {
            String warning = "Error running action ";
            if (action != null)
                warning += action.toString();
            if (actionHandler == null)
                warning += "\nAction was not enabled by " + this.getClass().getSimpleName() + ".";
            postWarning( warning );
            log.warn( "Action is null or not a run action. " );
        }
        return result;
    }

    @Override
    public Object run(DexRunAction actionHandler, Object value) {
        Object result = null;

        if (actionHandler instanceof DexRunAction)
            result = actionHandler.doIt( value );
        // push results onto queue
        if (result != null)
            push( actionHandler );

        return result;
    }

    protected DexAction<?> setListener(BooleanProperty op, DexActions action, OtmObject subject) {
        try {
            DexAction<?> actionHandler = DexActions.getAction( action, subject, this );
            if (actionHandler instanceof DexBooleanAction) {
                ChangeListener<Boolean> changeListener = new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
                        Boolean newValue) {
                        doBoolean( (DexBooleanAction) actionHandler, op );
                    }
                };

                // Set and save listener
                ((DexBooleanAction) actionHandler).setChangeListener( changeListener, op );
                return actionHandler;
            }
        } catch (ExceptionInInitializerError | InstantiationException | IllegalAccessException | SecurityException
            | IllegalArgumentException e) {
            log.warn( "Failed to set listener on " + action + " because: " + e.getLocalizedMessage() );
        }
        return null;
    }

    public DexAction<?> setListener(ObservableValue<?> o, DexActions action, OtmObject subject) {
        if (o instanceof StringProperty)
            return setListener( ((StringProperty) o), action, subject );
        if (o instanceof BooleanProperty)
            return setListener( ((BooleanProperty) o), action, subject );
        return null;
    }

    protected DexAction<?> setListener(StringProperty op, DexActions action, OtmObject subject) {
        try {
            DexAction<?> actionHandler = DexActions.getAction( action, subject, this );
            if (actionHandler instanceof DexStringAction) {
                ChangeListener<String> changeListener = new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue,
                        String newValue) {
                        doString( (DexStringAction) actionHandler, op, oldValue, newValue );
                    }
                };
                // Set and Save listener so it can be removed later.
                ((DexStringAction) actionHandler).setChangeListener( changeListener, op );
                return actionHandler;
            }
        } catch (ExceptionInInitializerError | InstantiationException | IllegalAccessException | SecurityException
            | IllegalArgumentException e) {
            log.warn( "Failed to set listener on " + action + " because: " + e.getLocalizedMessage() );
        }
        return null;
    }

    /**
     * Set the main controller used for pop-up warning dialogs and status
     * 
     * @param controller
     */
    public void setMainController(DexMainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void undo() {
        ignore = true;
        if (!queue.isEmpty()) {
            DexAction<?> action = queue.pop();
            // log.debug( "Undo action: " + action.getClass().getSimpleName() );
            action.undoIt();

            if (action.getSubject() != null)
                action.getSubject().getOwningMember().isValid( true ); // Force the owner to refresh its findings.

            // Throw an event if defined
            DexChangeEvent event = action.getEvent();
            if (event != null && mainController != null) {
                event.set( action.getSubject() );
                mainController.publishEvent( event );
            }

            if (mainController != null) {
                mainController.updateActionManagerDisplay( this );
                mainController.postStatus( "Undid action: " + action.toString() );
                // if (action.getSubject() != null)
                // action.getSubject().getModelManager().startValidatingAndResolvingTasks();
                // Refresh is much more light weight than full validation/resolver
                if (action.getSubject() != null)
                    action.getSubject().refresh();
            }
        }
        ignore = false;
    }

}
