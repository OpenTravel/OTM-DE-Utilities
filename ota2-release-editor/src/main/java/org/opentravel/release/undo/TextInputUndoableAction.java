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

import javafx.event.EventHandler;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

/**
 * Undoable action that occurs when the user updates a text field or text area.
 */
public class TextInputUndoableAction extends UndoableAction {
	
	/** The number of milliseconds after which new actions for the same control will not be merged. */
	public static long pauseTimeLimit = 3000L;
	
	private static final String INSTRUMENTATION_KEY = "undo-redo-instrumented-keypress";
	
	private TextInputControl control;
	private String oldValue;
	private String newValue;
	private int caretPosition;
	private boolean isUndone = false;
	
	/**
	 * Constructor that specifies the text input control that was modified.
	 * 
	 * @param control  the text input control that was modified
	 * @param oldValue  the value of the text input control before it was modified
	 * @param manager  the manager that will execute this action
	 */
	public TextInputUndoableAction(TextInputControl control, String oldValue, UndoManager manager) {
		super( manager );
		this.control = control;
		this.newValue = control.textProperty().getValue();
		this.oldValue = oldValue;
		this.caretPosition = control.caretPositionProperty().get();
		instrument( control );
	}
	
	/**
	 * Constructor that supplies the action execution delegate for all types of
	 * actions.  Only use this constructor if the same delegate should be used
	 * for all three actions (initial execution, undo, and redo).
	 * 
	 * @param control  the text input control that was modified
	 * @param oldValue  the value of the text input control before it was modified
	 * @param manager  the manager that will execute this action
	 * @param actionDelegate  the delegate to be used for all actions of this action
	 */
	public TextInputUndoableAction(TextInputControl control, String oldValue, UndoManager manager,
			ActionExecutionDelegate actionDelegate) {
		this( control, oldValue, manager );
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
		return !control.textProperty().getValue().equals( oldValue );
	}
	
	/**
	 * @see org.opentravel.release.undo.UndoableAction#doExecuteUndo()
	 */
	@Override
	public boolean doExecuteUndo() {
		isUndone = true;
		control.textProperty().setValue( oldValue );
		control.positionCaret( caretPosition );
		return true;
	}
	
	/**
	 * @see org.opentravel.release.undo.UndoableAction#doExecuteRedo()
	 */
	@Override
	public boolean doExecuteRedo() {
		control.textProperty().setValue( newValue );
		control.positionCaret( caretPosition );
		return true;
	}
	
	/**
	 * @see org.opentravel.release.undo.UndoableAction#canMerge(org.opentravel.release.undo.UndoableAction)
	 */
	@Override
	public boolean canMerge(UndoableAction nextAction) {
		boolean result = false;
		
		if (!isUndone && (nextAction instanceof TextInputUndoableAction)) {
			TextInputUndoableAction action = (TextInputUndoableAction) nextAction;
			
			result = (action.control == this.control) &&
					((this.lastExecutionTimestamp() + pauseTimeLimit) > System.currentTimeMillis());
		}
		return result;
	}
	
	/**
	 * @see org.opentravel.release.undo.UndoableAction#merge(org.opentravel.release.undo.UndoableAction)
	 */
	@Override
	public void merge(UndoableAction nextAction) {
		TextInputUndoableAction textAction = (TextInputUndoableAction) nextAction;
		
		this.newValue = textAction.newValue;
		this.caretPosition = textAction.control.getCaretPosition() + 1;
		touchLastExecutionTimestamp();
	}
	
	private void instrument(TextInputControl control) {
		if ((control != null) && (control.getProperties().get( INSTRUMENTATION_KEY ) == null)) {
			control.setOnKeyPressed( new UndoRedoKeyHandler() );
			control.getProperties().put( INSTRUMENTATION_KEY, Boolean.TRUE );
		}
	}
	
	/**
	 * Keyboard event handler that suppresses the default undo/redo behavior for
	 * text controls.
	 */
	private class UndoRedoKeyHandler implements EventHandler<KeyEvent> {
		
		final KeyCombination keyCombCtrZ = new KeyCodeCombination( KeyCode.Z, KeyCombination.SHORTCUT_DOWN );
		final KeyCombination keyCombCtrY = new KeyCodeCombination( KeyCode.Y, KeyCombination.SHORTCUT_DOWN );
		
		/**
		 * @see javafx.event.EventHandler#handle(javafx.event.Event)
		 */
		@Override
		public void handle(KeyEvent event) {
			if (keyCombCtrZ.match( event )) {
				event.consume();
				getManager().executeUndo();
				
			} else if (keyCombCtrY.match( event )) {
				event.consume();
				getManager().executeRedo();
			}
		}
		
	}
	
}
