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

package org.opentravel.release.undo;

import javafx.scene.control.Spinner;

/**
 * Undoable action that occurs when the user changes the state of a spinner value.
 *
 * @param <T> the type of value managed by the spinner control
 */
public class SpinnerUndoableAction<T> extends UndoableAction {

    private Spinner<T> spinner;
    private T oldValue;
    private T newValue;

    /**
     * Constructor that specifies the spinner that will be managed by this action.
     * 
     * @param spinner the spinner whose value was modified
     * @param oldValue the original spinner value before it was modified
     * @param manager the manager that will execute this action
     */
    public SpinnerUndoableAction(Spinner<T> spinner, T oldValue, UndoManager manager) {
        super( manager );
        this.spinner = spinner;
        this.newValue = spinner.getValue();
        this.oldValue = oldValue;
    }

    /**
     * Constructor that supplies the action execution delegate for all types of actions. Only use this constructor if
     * the same delegate should be used for all three actions (initial execution, undo, and redo).
     * 
     * @param spinner the spinner whose value was modified
     * @param oldValue the original spinner value before it was modified
     * @param manager the manager that will execute this action
     * @param actionDelegate the delegate to be used for all actions of this action
     */
    public SpinnerUndoableAction(Spinner<T> spinner, T oldValue, UndoManager manager,
        ActionExecutionDelegate actionDelegate) {
        this( spinner, oldValue, manager );
        setExecuteDelegate( actionDelegate );
        setUndoDelegate( actionDelegate );
        setRedoDelegate( actionDelegate );
    }

    /**
     * @see org.opentravel.release.undo.UndoableAction#doExecute()
     */
    @Override
    public boolean doExecute() {
        // No action required since the update was already performed by the user
        return true;
    }

    /**
     * @see org.opentravel.release.undo.UndoableAction#doExecuteUndo()
     */
    @Override
    public boolean doExecuteUndo() {
        spinner.getValueFactory().setValue( oldValue );
        return true;
    }

    /**
     * @see org.opentravel.release.undo.UndoableAction#doExecuteRedo()
     */
    @Override
    public boolean doExecuteRedo() {
        spinner.getValueFactory().setValue( newValue );
        return true;
    }

}
