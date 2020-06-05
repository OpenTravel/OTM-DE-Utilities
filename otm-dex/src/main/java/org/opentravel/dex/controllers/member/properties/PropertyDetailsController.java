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

package org.opentravel.dex.controllers.member.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.common.DexEditField;
import org.opentravel.common.ImageManager;
import org.opentravel.dex.actions.DeprecationChangeAction;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.dex.actions.MoveElementAction;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.popup.TextAreaEditorContoller;
import org.opentravel.dex.events.DexFacetSelectionEvent;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.dex.events.DexNavigationEvent;
import org.opentravel.dex.events.DexPropertySelectionEvent;
import org.opentravel.dex.events.OtmObjectChangeEvent;
import org.opentravel.dex.events.OtmObjectModifiedEvent;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmFacets.OtmVWAValueFacet;
import org.opentravel.model.otmLibraryMembers.OtmSimpleObject;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.schemacompiler.model.TLExampleOwner;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

/**
 * Controller for member property details.
 * 
 * @author dmh
 *
 */
public class PropertyDetailsController extends DexIncludedControllerBase<OtmObject> {
    private static Log log = LogFactory.getLog( PropertyDetailsController.class );

    // All event types fired by this controller.
    private static final EventType[] publishedEvents = {OtmObjectModifiedEvent.OBJECT_MODIFIED};
    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents = {DexPropertySelectionEvent.PROPERTY_SELECTED,
        DexFacetSelectionEvent.FACET_SELECTED, DexMemberSelectionEvent.MEMBER_SELECTED,
        DexMemberSelectionEvent.RESOURCE_SELECTED, OtmObjectModifiedEvent.OBJECT_MODIFIED,
        DexModelChangeEvent.MODEL_CHANGED, OtmObjectChangeEvent.OBJECT_CHANGED};
    /**
     * FXML Java FX Nodes this controller is dependent upon
     */
    @FXML
    private TitledPane propertyDetailsPane;
    @FXML
    private ButtonBar propertyButtons;
    @FXML
    private GridPane propertyGrid;
    @FXML
    private ImageView objectImageView;

    private int rowIndex = 0; // last row populated with data
    private boolean ignoreClear = false;
    private int currentRow;

    public PropertyDetailsController() {
        super( subscribedEvents, publishedEvents );
        // log.debug( "Member Details Controller constructor." );
    }

    @Override
    public void checkNodes() {
        if (!(propertyDetailsPane instanceof TitledPane))
            throw new IllegalStateException( "Member Details not injected by FXML." );
    }

    @Override
    public void clear() {
        // When posting updated filter results, do not clear the filters.
        if (!ignoreClear) {
            // log.debug( "Clearing property details." );
            postedData = null;
            propertyButtons.getButtons().clear();
            objectImageView.setImage( null );
            propertyDetailsPane.setText( "Property Details" );
            propertyGrid.getChildren().clear();
        }
    }

    @Override
    public void configure(DexMainController mainController, int viewGroupId) {
        super.configure( mainController, viewGroupId );
        eventPublisherNode = propertyDetailsPane;
    }


    @Override
    public void handleEvent(AbstractOtmEvent event) {
        // log.debug( "Event received: " + event.getClass().getSimpleName() );
        if (event instanceof DexPropertySelectionEvent)
            handleEvent( (DexPropertySelectionEvent) event );
        else if (event instanceof DexFacetSelectionEvent)
            post( ((DexFacetSelectionEvent) event).get() );
        else if (event instanceof DexMemberSelectionEvent
            && ((DexMemberSelectionEvent) event).getMember() instanceof OtmSimpleObject)
            post( ((DexNavigationEvent) event).getMember() );
        else if (event instanceof OtmObjectModifiedEvent)
            refresh();
        else if (event instanceof OtmObjectChangeEvent)
            refresh();
        else
            clear();
    }

    public void handleEvent(DexPropertySelectionEvent event) {
        post( event.get() );
    }

    @Override
    public void initialize() {
        checkNodes();
    }

    @Override
    public void post(OtmObject obj) {
        // log.debug( "Posting object " + obj );
        clear();
        if (obj == null)
            return;

        if (obj instanceof OtmProperty)
            post( (OtmProperty) obj );
        else if (obj instanceof OtmSimpleObject)
            post( (OtmSimpleObject) obj );
        else {
            postTitle( obj );
            postExample( obj );
        }
    }


    /**
     * <ul>
     * <li>Description(s)
     * <li>Example
     * <li>Buttons: up, down, change type, Deprecate, copy, delete
     * 
     * @param property
     */
    public void post(OtmProperty property) {
        // log.debug( "Posting property " + property );
        if (property == null) {
            clear();
            return;
        }

        propertyGrid.getChildren().clear();
        postButtons( property );

        postedData = property;
        postTitle( property );
        currentRow = 0;
        postDescription( property );
        postDeprecation( property );
        postExample( property );
    }


    public void post(OtmSimpleObject property) {
        // log.debug( "Posting simple object " + property );
        postedData = property;
        propertyGrid.getChildren().clear();
        postTitle( property );
        currentRow = 0;
        propertyButtons.setVisible( false );

        // List
        BooleanProperty listProperty =
            property.getActionManager().add( DexActions.SETLIST, property.isList(), property );
        Node checkBox = DexEditField.makeCheckBox( listProperty, "Set to make value repeat." );
        DexEditField field = new DexEditField( currentRow++, 0, "List", "Does this value repeat.", checkBox );
        postField( field, property );

        // Pattern
        // Min/Max Length
        // Fraction/Total digits
        // Min/Max inclusive
        // Min/Max exclusive
    }

    private Button postButton(ObservableList<Node> list, DexActions action, OtmProperty p, String label) {
        Button button = new Button( label );
        if (action != null && p != null && p.getActionManager() != null) {
            button.setDisable( !p.getActionManager().isEnabled( action, p ) );
            button.setOnAction( a -> p.getActionManager().run( action, p ) );
        } else
            button.setDisable( true );
        list.add( button );
        return button;
    }

    private Button postButton(ObservableList<Node> list, DexActions action, OtmProperty p, Object value, String label) {
        Button button = new Button( label );
        if (action != null && p != null && p.getActionManager() != null) {
            button.setDisable( !p.getActionManager().isEnabled( action, p ) );
            button.setOnAction( a -> p.getActionManager().run( action, p, value ) );
        } else
            button.setDisable( true );
        list.add( button );
        return button;
    }

    private void postButtons(OtmProperty p) {
        ObservableList<Node> list = propertyButtons.getButtons();
        if (list != null) {
            propertyButtons.setVisible( true );
            list.clear();
            postButton( list, DexActions.TYPECHANGE, p, "Change Type" );
            postButton( list, DexActions.COPYPROPERTY, p, "Copy" );
            postButton( list, DexActions.DELETEPROPERTY, p, "Delete" );

            Button button = postButton( list, DexActions.DEPRECATIONCHANGE, p, "Deprecate" );
            if (DeprecationChangeAction.isEnabled( p ))
                button.setOnAction( ae -> {
                    p.setDeprecation( "" );
                    p.deprecationProperty().set( "Deprecated" );
                } );
            postButton( list, DexActions.MOVEELEMENT, p, MoveElementAction.MoveDirection.UP, "Move Up" );
            postButton( list, DexActions.MOVEELEMENT, p, MoveElementAction.MoveDirection.DOWN, "Move Down" );
        }
    }

    private void postDeprecation(OtmObject obj) {
        if (obj.getDeprecation() != null && !obj.getDeprecation().isEmpty()) {
            TextField txt = new TextField( obj.deprecationProperty().get() );
            txt.setDisable( obj.deprecationProperty() instanceof ReadOnlyStringWrapper );
            txt.setOnAction( a -> setProperty( a, obj.deprecationProperty() ) );

            DexEditField field =
                new DexEditField( currentRow++, 0, "Deprecation", "Describe why this property is obsolete.", txt );
            postField( field, obj, obj.deprecationProperty() );
        }
    }

    private void postDescription(OtmObject obj) {
        // Editable field. String property will invoke action if needed
        TextField txt = new TextField( obj.descriptionProperty().get() );
        txt.setDisable( obj.descriptionProperty() instanceof ReadOnlyStringWrapper );
        txt.setOnAction( a -> setProperty( a, obj.descriptionProperty() ) );
        DexEditField field =
            new DexEditField( currentRow++, 0, "Description", "Describe what this property contains.", txt );
        postField( field, obj, obj.descriptionProperty() );
    }

    private void postExample(OtmObject obj) {
        if (obj instanceof OtmVWAValueFacet)
            postExample( obj.getOwningMember(), ((OtmVWAValueFacet) obj).exampleProperty() );
    }

    private void postExample(OtmProperty property) {
        postExample( property, property.exampleProperty() );
    }

    private void postExample(OtmObject obj, StringProperty stringProperty) {
        if (obj.getTL() instanceof TLExampleOwner) {
            // if (property.getExample() != null && !property.getExample().isEmpty()) {
            TextField txt = new TextField( stringProperty.get() );
            txt.setDisable( stringProperty instanceof ReadOnlyStringWrapper );
            txt.setOnAction( a -> setProperty( a, stringProperty ) );
            DexEditField field =
                new DexEditField( currentRow++, 0, "Example", "Provide an example of this data field.", txt );
            postField( field, obj, stringProperty );
        }
    }

    private int postField(DexEditField field, OtmObject obj) {
        int column = field.column;
        if (field.label != null) {
            Label label = new Label( field.label );
            label.setTooltip( field.tooltip );
            propertyGrid.add( label, field.column, field.row + rowIndex );
            column += 1;
        }
        if (field.fxNode != null) {
            field.fxNode.setDisable( !obj.isEditable() );
            if (field.fxNode instanceof Control) {
                ((Control) field.fxNode).setTooltip( field.tooltip );
            }
            propertyGrid.add( field.fxNode, column++, field.row + rowIndex );
        }
        return column;
    }

    private void postField(DexEditField field, OtmObject obj, StringProperty property) {
        int column = postField( field, obj );
        if (field.fxNode instanceof TextField && obj.isEditable()) {
            Button button = new Button( "Edit" );
            button.setOnAction( a -> TextAreaEditorContoller.init().showAndWait( property ) );
            propertyGrid.add( button, column, field.row + rowIndex );
        }
    }

    private void postTitle(OtmObject obj) {
        if (obj != null && obj.getOwningMember() != null) {
            objectImageView.setImage( ImageManager.getImage( obj.getIconType() ) );
            propertyDetailsPane.setText( "Details of " + obj.getObjectTypeName() + "  " + obj.getName() + "   in "
                + obj.getOwningMember().getNameWithPrefix() );
        } else
            propertyDetailsPane.setText( "Details" );
    }

    @Override
    public void refresh() {
        // log.debug( "Refreshing property details." );
        post( postedData );
    }

    private void setProperty(ActionEvent a, StringProperty p) {
        if (p != null && a.getSource() instanceof TextField) {
            p.set( ((TextField) a.getSource()).getText() );
        }
    }

}
