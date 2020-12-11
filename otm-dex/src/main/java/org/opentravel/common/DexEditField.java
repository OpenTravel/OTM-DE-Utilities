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

package org.opentravel.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.actions.DexAction;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.dex.actions.DexRunAction;
import org.opentravel.dex.controllers.popup.TextAreaEditorContoller;
import org.opentravel.model.OtmObject;

import java.util.Map;
import java.util.Map.Entry;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/**
 * Edit fields are presented by the GUI for the user to see and edit properties.
 * <P>
 * The fxNode should be fully configured with appropriate handlers.
 * 
 * @author dmh
 *
 */
public class DexEditField {
    private static Log log = LogFactory.getLog( DexEditField.class );

    public Node fxNode;
    public String label;
    public Tooltip tooltip;
    public int row;
    public int column;
    private Label fxLabel;
    private OtmObject otm;
    private DexActions actionType;
    boolean enabled = true;

    /**
     * 
     * @param row integer 0 to n. 0 is placed right after description row
     * @param column integer 0 to n. Fields with label will require 2 columns.
     * @param label string for first column or if null no label column is used
     * @param tooltip added to label and fxNode
     * @param fxNode actual Java FX control node or if null no node column is used
     */
    public DexEditField(int row, int column, String label, String tooltip, Node fxNode) {
        set( row, column, label, tooltip, fxNode );
    }

    public DexEditField() {
        this.tooltip = new Tooltip( "" );
    }

    public DexEditField(OtmObject otm, DexActions actionType, String label, String tooltip) {
        set( otm, actionType, label, tooltip );
        this.enabled = isEnabled();
    }

    public void set(int row, int column, String label, String tooltip, Node fxNode) {
        this.label = label;
        this.fxNode = fxNode;
        this.row = row;
        this.column = column;
        this.tooltip = new Tooltip( tooltip );
    }

    public void set(OtmObject otm, DexActions actionType, String label, String tooltip) {
        this.otm = otm;
        this.actionType = actionType;
        this.label = label;
        this.tooltip = new Tooltip( tooltip );
    }

    public void set(OtmObject otm) {
        this.otm = otm;
        enabled = otm.isEditable();
    }

    @Deprecated
    private static CheckBox makeCheckBox(boolean value, String label, OtmObject object) {
        CheckBox box = new CheckBox( label );
        box.setSelected( value );
        box.setDisable( !object.getOwningMember().isEditable() );
        // box.setOnAction( a -> log.debug( label + " check box selected." ) );
        return box;
    }

    public static CheckBox makeCheckBox(BooleanProperty value, String label) {
        CheckBox box = new CheckBox( label );
        box.setSelected( value.get() );
        if (!(value instanceof ReadOnlyBooleanWrapper)) {
            box.setDisable( false );
            box.setOnAction( a -> {
                value.set( box.isSelected() );
                // log.debug( label + " check box set to " + value.get() );
            } );
        } else
            box.setDisable( true );
        return box;
    }

    public static HBox makeCheckBoxRow(Map<String,Boolean> values, OtmObject object) {
        HBox hb = new HBox();
        hb.setSpacing( 10 );
        CheckBox cb;
        for (Entry<String,Boolean> t : values.entrySet()) {
            cb = DexEditField.makeCheckBox( t.getValue(), t.getKey(), object );
            hb.getChildren().add( cb );
            // cb.setOnAction( a -> log.debug( "Check box selected." ) );
        }
        return hb;
    }


    public static ComboBox<String> makeComboBox(ObservableList<String> candidates, StringProperty selection) {
        ComboBox<String> box = new ComboBox<>( candidates );
        box.getSelectionModel().select( selection.get() );
        box.setDisable( selection instanceof ReadOnlyStringWrapper );
        box.setOnAction( a -> {
            // ec.fireEvent( new DexResourceChangeEvent() );
            selection.set( box.getValue() );
            // log.debug( "Combo box selected" );
        } );
        return box;
    }

    /**
     * @param string property
     * @return
     */
    public static TextField makeTextField(StringProperty stringProperty) {
        TextField txtField = new TextField( stringProperty.get() );
        if (stringProperty instanceof ReadOnlyStringWrapper) {
            txtField.setEditable( false );
            txtField.setDisable( true );
            // txtField.setVisible( false );
        } else {
            txtField.setEditable( true );
            txtField.setDisable( false );
            txtField.setOnAction( a -> stringProperty.set( ((TextField) a.getSource()).getText() ) );
            txtField.focusedProperty().addListener( (ov, oldV, newV) -> {
                if (!newV) // focus lost
                    stringProperty.set( txtField.getText() );
            } );
        }
        return txtField;
    }

    /**
     * @param string property
     * @return
     */
    public TextField makeTextField(String value) {
        if (otm == null || actionType == null)
            return null;
        // Changes to string property will be handled by action
        StringProperty sp = otm.getActionManager().add( actionType, value, otm );
        // Changes to text field will cause string property to change
        TextField tf = makeTextField( sp );
        this.fxNode = tf;
        return tf;
    }

    /**
     * Add label (if any) and fxNode to grid to post them to the UI.
     * 
     * @param grid
     * @param obj
     * @param row
     * @param column
     * @return next available column
     */
    public int postField(GridPane grid, OtmObject obj, int row, int column) {
        this.row = row;
        this.column = column;
        this.otm = obj;
        return postField( grid );
    }

    /**
     * Add label (if any) and fxNode to grid to post them to the UI.
     * 
     * @param grid
     * @param obj
     * @return next available column
     */
    public int postField(final GridPane grid) {
        if (grid == null)
            return 0;
        if (label != null) {
            fxLabel = new Label( label );
            fxLabel.setTooltip( tooltip );
            fxLabel.setDisable( !enabled );
            grid.add( fxLabel, column, row );
            column += 1;
        }
        if (fxNode != null) {
            fxNode.setDisable( !enabled );
            if (fxNode instanceof Control) {
                ((Control) fxNode).setTooltip( tooltip );
            }
            grid.add( fxNode, column, row );
            column += 1;
        }
        return column;
    }

    /**
     * @param otm
     * @param actionType
     * @return true if editable and action is enabled for the object
     */
    public boolean isEnabled() {
        if (otm == null || actionType == null)
            return false;
        enabled = otm.isEditable();
        if (enabled)
            enabled = otm.getActionManager().isEnabled( actionType, otm );
        return enabled;
    }

    public void post(GridPane grid, StringProperty sp, boolean addButton) {
        enabled = !(sp instanceof ReadOnlyStringWrapper);
        this.fxNode = makeTextField( sp );
        postField( grid );
        if (addButton) {
            Button button = DexEditField.makeButton( sp );
            grid.add( button, column, row );
        }
    }

    /**
     * Make an "edit" button for the string property.
     * 
     * @param sp
     * @return
     */
    public static Button makeButton(StringProperty sp) {
        Button button = new Button( "Edit" );
        if (!(sp instanceof ReadOnlyStringWrapper)) {
            button.setDisable( false );
            button.setOnAction( a -> TextAreaEditorContoller.init().showAndWait( sp ) );
        } else {
            button.setDisable( true );
        }
        return button;
    }

    public Spinner<Integer> makeSpinner(int value) {
        Spinner<Integer> spinner = new Spinner<>( 0, 10000, value ); // min, max, init
        this.fxNode = spinner;
        if (!enabled) {
            spinner.setDisable( true );
            spinner.setEditable( false );
        } else {
            spinner.setDisable( false );
            spinner.setEditable( true );
        }
        if (otm != null && actionType != null) {
            spinner.getEditor().setOnAction( a -> spinnerListener( spinner, otm, actionType ) );
            spinner.focusedProperty().addListener( (o, old, newV) -> spinnerListener( spinner, otm, actionType ) );
        }
        spinner.setTooltip( tooltip );
        return spinner;
    }

    // Create the action and use it to get and set the value from the spinner
    private void spinnerListener(Spinner<Integer> spinner, OtmObject simple, DexActions actionType) {
        // log.debug( "Listener start." );
        DexRunAction action = null;
        try {
            DexAction<?> a = DexActions.getAction( actionType, simple, null, simple.getActionManager() );
            if (a instanceof DexRunAction)
                action = (DexRunAction) a;
        } catch (ExceptionInInitializerError | InstantiationException | IllegalAccessException e) {
            // log.debug( "Error getting action." );
            return;
        }

        if (action != null && spinner != null) {
            Object currentValue = action.get();
            try {
                // If they type, the editor will access the value
                int value = Integer.parseInt( spinner.getEditor().getText() );
                if (spinner.getValue() != value)
                    spinner.getValueFactory().setValue( value );

                // If the value changed, run the action
                if (spinner.getValue() != currentValue) {
                    simple.getActionManager().run( action, spinner.getValue() );
                    // log.debug( "Action ran." );
                }
            } catch (NumberFormatException e) {
                // log.debug( "Not a valid number format: " + spinner.getEditor().getText() );
                simple.getActionManager().run( action, null );
            }
        }
    }
}
