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
import org.opentravel.model.OtmObject;

import java.util.Map;
import java.util.Map.Entry;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
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

    }

    public void set(int row, int column, String label, String tooltip, Node fxNode) {
        this.label = label;
        this.fxNode = fxNode;
        this.row = row;
        this.column = column;
        this.tooltip = new Tooltip( tooltip );
    }

    @Deprecated
    public static CheckBox makeCheckBox(boolean value, String label, OtmObject object) {
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
        TextField field = new TextField( stringProperty.get() );
        if (stringProperty instanceof ReadOnlyStringWrapper) {
            field.setEditable( false );
            field.setDisable( true );
            // field.setVisible( false );
        } else {
            field.setEditable( true );
            field.setDisable( false );
            field.setOnAction( a -> {
                stringProperty.set( ((TextField) a.getSource()).getText() );
            } );
        }
        return field;
    }


    public Spinner<Integer> makeSpinner(int value, OtmObject obj, DexActions actionType) {
        Spinner<Integer> spinner = new Spinner<>( 0, 10000, value ); // min, max, init
        spinner.setDisable( !obj.isEditable() );
        spinner.setEditable( obj.isEditable() );

        spinner.getEditor().setOnAction( a -> spinnerListener( spinner, obj, actionType ) );
        spinner.focusedProperty().addListener( (o, old, newV) -> spinnerListener( spinner, obj, actionType ) );

        // spinner.getEditor().setOnAction( a -> spinnerListener( spinner ) );
        // // spinnerListener( spinner ) );
        // spinner.focusedProperty().addListener( (o, old, newV) -> spinnerListener( spinner ) );
        return spinner;
    }

    // Create the action and use it to get and set the value from the spinner
    private void spinnerListener(Spinner<Integer> spinner, OtmObject simple, DexActions actionType) {
        DexRunAction action = null;
        try {
            DexAction<?> a = DexActions.getAction( actionType, simple, null, simple.getActionManager() );
            if (a instanceof DexRunAction)
                action = (DexRunAction) a;
        } catch (ExceptionInInitializerError | InstantiationException | IllegalAccessException e) {
            log.debug( "Error getting action." );
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
                }
            } catch (NumberFormatException e) {
                log.debug( "Not a valid number format: " + spinner.getEditor().getText() );
                simple.getActionManager().run( action, null );
            }
        }
    }
}
