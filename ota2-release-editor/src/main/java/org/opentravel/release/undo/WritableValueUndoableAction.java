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

import javafx.beans.value.WritableValue;

/**
 * Undoable action that occurs when the user changes the state of an observable value.
 *
 * @param <T>  the type of the observable value
 */
public class WritableValueUndoableAction<T> extends UndoableAction {
	
	private WritableValue<T> property;
	private T oldValue;
	private T newValue;
	
	/**
	 * Constructor that specifies the checkbox that was modified.
	 * 
	 * @param value  the observable value that was modified
	 * @param oldValue  the original value before it was modified
	 * @param manager  the manager that will execute this action
	 */
	public WritableValueUndoableAction(WritableValue<T> property, T oldValue, UndoManager manager) {
		super( manager );
		this.property = property;
		this.newValue = property.getValue();
		this.oldValue = oldValue;
	}
	
	/**
	 * Constructor that supplies the action execution delegate for all types of
	 * actions.  Only use this constructor if the same delegate should be used
	 * for all three actions (initial execution, undo, and redo).
	 * 
	 * @param value  the observable value that was modified
	 * @param oldValue  the original value before it was modified
	 * @param manager  the manager that will execute this action
	 * @param actionDelegate  the delegate to be used for all actions of this action
	 */
	public WritableValueUndoableAction(WritableValue<T> property, T oldValue, UndoManager manager,
			ActionExecutionDelegate actionDelegate) {
		this( property, oldValue, manager );
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
		property.setValue( oldValue );
		return true;
	}
	
	/**
	 * @see org.opentravel.release.undo.UndoableAction#doExecuteRedo()
	 */
	@Override
	public boolean doExecuteRedo() {
		property.setValue( newValue );
		return true;
	}
	
}
