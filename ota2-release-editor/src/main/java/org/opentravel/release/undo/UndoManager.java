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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the execution and orchestration of undo and redo actions for
 * a JavaFX application.
 */
public class UndoManager {
	
	private static final int DEFAULT_UNDO_COUNT = 20;
	
    private static final Logger log = LoggerFactory.getLogger( UndoManager.class );
    
    private Set<UndoStatusListener> listeners = new HashSet<>();
	private Deque<UndoableAction> undoStack = new ArrayDeque<>();
	private Deque<UndoableAction> redoStack = new ArrayDeque<>();
	private boolean executionDisabled = false;
	private int maxUndoCount;
	
	/**
	 * Default constructor.
	 */
	public UndoManager() {
		this( DEFAULT_UNDO_COUNT );
	}
	
	/**
	 * Constructor that specifies the maximum number of undo actions that
	 * will be retained by this manager.
	 * 
	 * @param maxUndoCount  the maximum number of undo actions retained by this manager
	 */
	public UndoManager(int maxUndoCount) {
		this.maxUndoCount = maxUndoCount;
	}
	
	/**
	 * Executes the given action and pushes it onto the undo stack.  If successful,
	 * the redo stack is purged and this method returns null.  If the action's execution
	 * is unsuccessful, it will be discarded and false will be returned.
	 * 
	 * <p>NOTE: Each time a action is successfully executed by this method, the redo
	 * stack is purged.  This is true in all cases except when the action can be merged
	 * with the prior undo action.
	 * 
	 * <p>If action execution is currently disabled, the action will be ignored (not
	 * executed) and no error will be thrown.
	 * 
	 * @param action  the undoable action to be executed
	 * @return boolean
	 */
	public synchronized boolean execute(UndoableAction action) {
		boolean oldUndo = canUndo();
		boolean oldRedo = canRedo();
		boolean successInd = false;
		
		// Execute the action and determine whether the outcome was successful
		if (!executionDisabled) {
			try {
				executionDisabled = true;
				successInd = action.execute();
				
			} catch (Exception e) {
				log.error("Unexpected error during action execution (discarding undo/redo history).", e);
				
			} finally {
				executionDisabled = false;
			}
			
			// If successful, either push the action onto the undo stack or merge it with the top item
			if (successInd) {
				pushOrMergeAction( action );
				
			} else {
				purge(); // Purge the undo/redo history if something went wrong
			}
			
			notifyStatusChange( oldUndo, oldRedo );
		}
		return successInd;
	}

    /**
     * Either push the action onto the undo stack or merge it with the top item.
     * 
     * @param action  the action to be pushed or merged onto the stack
     */
    private void pushOrMergeAction(UndoableAction action) {
        UndoableAction previousAction = undoStack.isEmpty() ? null : undoStack.peek();
        
        if ((previousAction != null) && previousAction.canMerge( action )) {
        	try {
        		previousAction.merge( action );
        		
        	} catch (Exception e) {
        		log.error("Unexpected error merging undo action (discarding undo/redo history).", e);
        		purge();
        	}
        	
        } else {
        	undoStack.push( action );
        	truncateUndoRedoStacks();
        }
        redoStack.clear();
    }
	
	/**
	 * Returns true if at least one action exists on the undo stack.
	 * 
	 * @return boolean
	 */
	public boolean canUndo() {
		return !undoStack.isEmpty();
	}
	
	/**
	 * Calls the 'executedUndo()' method for the action at the top of the undo
	 * stack.  If successful, the action moved to the top of the redo stack.
	 * 
	 * @return boolean
	 */
	public synchronized boolean executeUndo() {
		boolean oldUndo = canUndo();
		boolean oldRedo = canRedo();
		boolean successInd;
		
		if (canUndo()) {
			UndoableAction action = undoStack.pop();
			
			try {
				executionDisabled = true;
				successInd = action.executeUndo();
				
				if (successInd) {
					redoStack.push( action );
					truncateUndoRedoStacks();
				}
				
			} catch (Exception e) {
				log.error("Unexpected error during undo execution (discarding undo/redo history).", e);
				successInd = false;
				
			} finally {
				executionDisabled = false;
			}
			
			if (!successInd) {
				purge();
			}
			
		} else {
			successInd = false;
		}
		notifyStatusChange( oldUndo, oldRedo );
		return successInd;
	}
	
	/**
	 * Returns true if at least one action exists on the redo stack.
	 * 
	 * @return boolean
	 */
	public boolean canRedo() {
		return !redoStack.isEmpty();
	}
	
	/**
	 * Calls the 'executeRedo()' method for the action at the top of the redo
	 * stack.  If successful, the action moved to the top of the undo stack.
	 * 
	 * @return boolean
	 */
	public synchronized boolean executeRedo() {
		boolean oldUndo = canUndo();
		boolean oldRedo = canRedo();
		boolean successInd;
		
		if (canRedo()) {
			UndoableAction action = redoStack.pop();
			
			try {
				executionDisabled = true;
				successInd = action.executeRedo();
				
				if (successInd) {
					undoStack.push( action );
					truncateUndoRedoStacks();
				}
				
			} catch (Exception e) {
				log.error("Unexpected error during redo execution (discarding undo/redo history).", e);
				successInd = false;
				
			} finally {
				executionDisabled = false;
			}
			
			if (!successInd) {
				purge();
			}
			
		} else {
			successInd = false;
		}
		notifyStatusChange( oldUndo, oldRedo );
		return successInd;
	}
	
	/**
	 * Purges all actions from the undo and redo stacks.
	 */
	public synchronized void purge() {
		boolean oldUndo = canUndo();
		boolean oldRedo = canRedo();
		
		undoStack.clear();
		redoStack.clear();
		notifyStatusChange( oldUndo, oldRedo );
	}
	
	/**
	 * Truncates the oldest actions from the bottom of the undo and redo
	 * stacks if either one contains greater than the <code>maxUndoCount</code>
	 * number of actions.
	 */
	private void truncateUndoRedoStacks() {
		if (maxUndoCount > 0) {
			while (undoStack.size() > maxUndoCount) {
			    undoStack.removeFirst();
			}
			while (redoStack.size() > maxUndoCount) {
			    redoStack.removeFirst();
			}
		}
	}
	
	/**
	 * Returns the maximum number of undo actions retained by this manager.  A
	 * maximum undo count that is less than zero allows an unlimited number of
	 * undos.
	 *
	 * @return int
	 */
	public synchronized int getMaxUndoCount() {
		return maxUndoCount;
	}
	
	/**
	 * Assigns the maximum number of undo actions retained by this manager.  A
	 * maximum undo count that is less than zero allows an unlimited number of
	 * undos.
	 *
	 * @param maxUndoCount  the field value to assign
	 */
	public synchronized void setMaxUndoCount(int maxUndoCount) {
		boolean oldUndo = canUndo();
		boolean oldRedo = canRedo();
		
		this.maxUndoCount = maxUndoCount;
		truncateUndoRedoStacks();
		notifyStatusChange( oldUndo, oldRedo );
	}
	
	/**
	 * Adds the given listener to this manager.
	 * 
	 * @param listener  the listener instance to add
	 */
	public void addListener(UndoStatusListener listener) {
		if ((listener != null) && !listeners.contains( listener )) {
			listeners.add( listener );
		}
	}
	
	/**
	 * Removes the given listener from this manager.
	 * 
	 * @param listener  the listener instance to remove
	 */
	public void removeListener(UndoStatusListener listener) {
		listeners.remove( listener );
	}
	
	/**
	 * If the status of either the 'canUndo()' or 'canRedo()' values have changed, an
	 * event will be sent to each registered listener.
	 * 
	 * @param oldCanUndo  the old value of the 'canUndo()' status
	 * @param oldCanRedo  the old value of the 'canRedo()' status
	 */
	private void notifyStatusChange(boolean oldCanUndo, boolean oldCanRedo) {
		boolean statusChanged = (oldCanUndo != canUndo()) || (oldCanRedo != canRedo());
		
		if (statusChanged) {
			for (UndoStatusListener listener : listeners) {
				listener.undoStatusChanged( this );
			}
		}
	}
	
	/**
	 * Disables the propagation of change events for all controls that were configured by
	 * the 'installListener()' method.
	 */
	public void disableActionExecution() {
		executionDisabled = true;
	}
	
	/**
	 * Enables the propagation of change events for all controls that were configured by
	 * the 'installListener()' method.
	 */
	public void enableActionExecution() {
		executionDisabled = false;
	}
	
}
