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

/**
 * Abstract base class that provides a partial implementation for the
 * <code>UndoableAction</code> interface.
 */
public abstract class UndoableAction {
	
	private ActionExecutionDelegate executeDelegate;
	private ActionExecutionDelegate undoDelegate;
	private ActionExecutionDelegate redoDelegate;
	private long lastExecutionTimestamp = Long.MIN_VALUE;
	private UndoManager manager;
	
	/**
	 * Constructor that specifies the <code>UndoManager</code> that will execute
	 * this action.
	 * 
	 * @param manager  the manager that will execute this action
	 */
	public UndoableAction(UndoManager manager) {
		this.manager = manager;
	}
	
	/**
	 * Constructor that supplies the action execution delegate for all types of
	 * actions.  Only use this constructor if the same delegate should be used
	 * for all three actions (initial execution, undo, and redo).
	 * 
	 * @param manager  the manager that will execute this action
	 * @param actionDelegate  the delegate to be used for all actions of this action
	 */
	public UndoableAction(UndoManager manager, ActionExecutionDelegate actionDelegate) {
		this.manager = manager;
		this.executeDelegate = actionDelegate;
		this.undoDelegate = actionDelegate;
		this.redoDelegate = actionDelegate;
	}
	
	/**
	 * Submits this action to the undo manager for execution and returns true
	 * if successful.
	 * 
	 * @return boolean
	 */
	public boolean submit() {
		return manager.execute( this );
	}
	
	/**
	 * Executes the action for the first time.  This is typically done synchronously
	 * in response to a user's edit or other action.  This method should return true
	 * if the action is executed successfully, false otherwise.
	 * 
	 * @return boolean
	 */
	public boolean execute() {
		boolean result = doExecute();
		
		if (result) {
			if (executeDelegate != null) {
				executeDelegate.execute();
			}
			touchLastExecutionTimestamp();
		}
		return result;
	}
	
	/**
	 * Performs any sub-class specific work of the 'execute()' method.
	 * 
	 * @return boolean
	 */
	public abstract boolean doExecute();
	
	/**
	 * Performs all actions required to undo the original action (or a prior redo).
	 * It is the reponsibility of the action implementor to ensure that the state of
	 * the application and any underlying data structures are returned to the exact
	 * state that existed prior to the action's execution or a previous redo action.
	 * 
	 * <p>This method should return true if the action is undone successfully, false
	 * otherwise.
	 * 
	 * @return boolean
	 */
	public boolean executeUndo() {
		boolean result = doExecuteUndo();
		
		if (result) {
			if (undoDelegate != null) {
				undoDelegate.execute();
			}
			touchLastExecutionTimestamp();
		}
		return result;
	}
	
	/**
	 * Performs any sub-class specific work of the 'execute()' method.
	 * 
	 * @return boolean
	 */
	public abstract boolean doExecuteUndo();
	
	/**
	 * Performs all actions required to redo a previous undo action performed by
	 * this action.  It is the reponsibility of the action implementor to ensure that
	 * the state of the application and any underlying data structures are returned
	 * to the exact state that existed after the original execution of this action.
	 * 
	 * <p>This method should return true if the action is redone successfully, false
	 * otherwise.
	 * 
	 * @return boolean
	 */
	public boolean executeRedo() {
		boolean result = doExecuteRedo();
		
		if (result) {
			if (redoDelegate != null) {
				redoDelegate.execute();
			}
			touchLastExecutionTimestamp();
		}
		return result;
	}
	
	/**
	 * Performs any sub-class specific work of the 'executeRedo()' method.
	 * 
	 * @return boolean
	 */
	public abstract boolean doExecuteRedo();
	
	/**
	 * Returns system timestamp (as returned by <code>System.currentTimeMillis()</code>)
	 * when this action performed its original execution or an undo/redo action.
	 * 
	 * @return long
	 */
	public long lastExecutionTimestamp() {
		return lastExecutionTimestamp;
	}
	
	/**
	 * Updates the last execution timestamp to the current system time.
	 */
	public void touchLastExecutionTimestamp() {
		lastExecutionTimestamp = System.currentTimeMillis();
	}
	
	/**
	 * Returns true if this action can be merged with the next action that was executed
	 * immediately following this one.  Typically, this is used for consolidating
	 * text-edit events into a single action but may be useful in other situations as
	 * well.
	 * 
	 * <p>This method returns false by default.  Sub-classes may override if merges
	 * are supported.
	 * 
	 * @param nextAction  the action to check for merging with this action
	 * @return boolean
	 */
	public boolean canMerge(UndoableAction nextAction) {
		return false;
	}
	
	/**
	 * Merges the given action with this one.  This method will only be called by the
	 * <code>UndoManager</code> if the 'canMerge()' method returns true.
	 * 
	 * <p>By default, this method always throws an <code>UnsupportedOperationException</code>.
	 * Sub-classes may override to implement action-specific merge behaviour.
	 * 
	 * @param nextAction  the action to be merged with this one
	 */
	public void merge(UndoableAction nextAction) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the <code>UndoManager</code> that will execute this action.
	 *
	 * @return UndoManager
	 */
	public UndoManager getManager() {
		return manager;
	}

	/**
	 * Assigns the delegate that will be executed during the initial execution
	 * of this action.
	 *
	 * @param executeDelegate  the field value to assign
	 */
	public void setExecuteDelegate(ActionExecutionDelegate executeDelegate) {
		this.executeDelegate = executeDelegate;
	}

	/**
	 * Assigns the delegate that will be executed during a undo operation.
	 *
	 * @param undoDelegate  the field value to assign
	 */
	public void setUndoDelegate(ActionExecutionDelegate undoDelegate) {
		this.undoDelegate = undoDelegate;
	}

	/**
	 * Assigns the delegate that will be executed during a redo operation.
	 *
	 * @param redoDelegate  the field value to assign
	 */
	public void setRedoDelegate(ActionExecutionDelegate redoDelegate) {
		this.redoDelegate = redoDelegate;
	}
	
}
