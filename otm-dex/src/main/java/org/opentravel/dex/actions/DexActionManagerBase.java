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
    private DexMainController mainController = null;

    // Queue for holding actions that were successful
    private final Deque<DexAction<?>> queue = new ArrayDeque<>();
    protected boolean ignore;

    /**
     * Action manager that can not update status or display queue size and contents.
     */
    public DexActionManagerBase() {}


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
            ignore = true;
            action.doIt( o, oldString, newString );
            push( action );
            ignore = false;
        }
    }

    @Override
    public String getLastActionName() {
        return queue.peek() != null ? queue.peek().getClass().getSimpleName() : "";
    }

    @Override
    public int getQueueSize() {
        return queue.size();
    }

    @Override
    public void postWarning(String warning) {
        if (mainController != null)
            mainController.postError( null, warning );
    }

    @Override
    public void push(DexAction<?> action) {
        if (queue.contains( action )) {
            // Make sure not a duplicate
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
            action.undoIt();
            ignore = false;
            log.debug( "Action vetoed!" );
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
            mainController.publishEvent( event );
        }

        // Let the user know what happened
        if (mainController != null)
            mainController.updateActionQueueSize( getQueueSize() );
        if (mainController != null)
            mainController.postStatus( "Performed action: " + action.toString() );

        action.getSubject().getOwningMember().isValid( true ); // Force the owner to refresh its findings.

        log.debug( "Put action on queue: " + action.getClass().getSimpleName() );
    }

    @Override
    public Object run(DexActions action, OtmObject subject) {
        return run( action, subject, null );
    }

    @Override
    public Object run(DexActions action, OtmObject subject, Object value) {
        DexAction<?> actionHandler;
        Object result = null;
        try {
            actionHandler = DexActions.getAction( action, subject );
            if (actionHandler instanceof DexRunAction) {
                if (value == null)
                    result = ((DexRunAction) actionHandler).doIt();
                else
                    result = ((DexRunAction) actionHandler).doIt( value );
            }
        } catch (ExceptionInInitializerError | InstantiationException | IllegalAccessException | SecurityException
            | IllegalArgumentException e) {
            log.warn( "Could not create action. " + e.getLocalizedMessage() );
        }
        return result;
    }

    protected DexAction<?> setListener(BooleanProperty op, DexActions action, OtmObject subject) {
        try {
            DexAction<?> actionHandler = DexActions.getAction( action, subject );
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

    protected DexAction<?> setListener(ObservableValue<?> o, DexActions action, OtmObject subject) {
        if (o instanceof StringProperty)
            return setListener( ((StringProperty) o), action, subject );
        if (o instanceof BooleanProperty)
            return setListener( ((BooleanProperty) o), action, subject );
        return null;
    }

    protected DexAction<?> setListener(StringProperty op, DexActions action, OtmObject subject) {
        try {
            DexAction<?> actionHandler = DexActions.getAction( action, subject );
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

    @Override
    public void undo() {
        ignore = true;
        if (!queue.isEmpty()) {
            DexAction<?> action = queue.pop();
            log.debug( "Undo action: " + action.getClass().getSimpleName() );
            action.undoIt();
            action.getSubject().getOwningMember().isValid( true ); // Force the owner to refresh its findings.

            // Throw an event if defined
            DexChangeEvent event = action.getEvent();
            if (event != null && mainController != null) {
                event.set( action.getSubject() );
                mainController.publishEvent( event );
            }
            if (mainController != null)
                mainController.updateActionQueueSize( getQueueSize() );
            if (mainController != null)
                mainController.postStatus( "Undid action: " + action.toString() );
        }
        ignore = false;
    }

}
