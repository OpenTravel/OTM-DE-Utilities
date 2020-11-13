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
import org.opentravel.schemacompiler.model.TLSimple;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
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

    private static final String PATTERN_LABEL = "Pattern";
    private static final String PATTERN_TIP = "Assign a regular expression pattern constraint.";
    private static final String MININ_LABEL = "Min Inclusive";
    private static final String MININ_TIP = "Assigns a minimum (inclusive) value.";
    private static final String MAXIN_LABEL = "Max Inclusive";
    private static final String MAXIN_TIP = "Assigns a maximum (inclusive) value.";
    private static final String MINEX_LABEL = "Min Exclusive";
    private static final String MINEX_TIP = "Assigns a minimum (exclusive) value.";
    private static final String MAXEX_LABEL = "Max Exclusive";
    private static final String MAXEX_TIP = "Assigns a maximum (exclusive) value.";
    private static final String TOTALDIGITS_LABEL = "Total Digits";
    private static final String TOTALDIGITS_TIP = "Assign the total number of digits of a numeric datatype.";
    private static final String FRACTIONDIGITS_LABEL = "Fraction Digits";
    private static final String FRACTIONDIGITS_TIP = "Assign the number of fractional digits of a numerical datatype.";
    private static final String MINLEN_LABEL = "Min Length";
    private static final String MINLEN_TIP =
        "Assign the minimum length expressed in a unit that depends on the datatype.";
    private static final String MAXLEN_LABEL = "Max Length";
    private static final String MAXLEN_TIP =
        "Assign the maximum length expressed in a unit that depends on the datatype.";

    private static final String DEPRECATION_LABEL = "Deprecation";
    private static final String DEPRECATION_TIP = "Describe why this property is obsolete.";
    private static final String DESCRIPTION_LABEL = "Description";
    private static final String DESCRIPTION_TIP = "Describe what this property contains.";
    private static final String EXAMPLE_LABEL = "Example";
    private static final String EXAMPLE_TIP = "Provide an example of this data field.";


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

    // private int rowIndex = 0; // last row populated with data
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
        propertyDetailsPane.setExpanded( false );
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

        postTitle( obj );
        postExample( obj );
        if (obj instanceof OtmProperty)
            post( (OtmProperty) obj );
        else if (obj instanceof OtmSimpleObject)
            post( (OtmSimpleObject) obj );
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
        propertyDetailsPane.setExpanded( true );

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



    public void post(OtmSimpleObject simple) {
        // log.debug( "Posting simple object " + property );
        propertyDetailsPane.setExpanded( true );

        postedData = simple;
        propertyGrid.getChildren().clear();
        postTitle( simple );
        currentRow = 0;
        propertyButtons.setVisible( false );

        // List
        BooleanProperty listProperty = simple.getActionManager().add( DexActions.SETLIST, simple.isList(), simple );
        Node checkBox = DexEditField.makeCheckBox( listProperty, "Repeats" );
        DexEditField field = new DexEditField( currentRow, 0, "List", "Set to allow the value to repeat.", checkBox );
        field.postField( propertyGrid, simple, currentRow, 0 );

        // post fields creating actions
        // DexActionManager am = simple.getActionManager();
        TLSimple tl = simple.getTL();

        // Pattern
        makeField( simple, DexActions.SETCONSTRAINT_PATTERN, currentRow++, 2, tl.getPattern(), PATTERN_LABEL,
            PATTERN_TIP );

        // Min/Max Length
        makeSpinner( simple, DexActions.SETCONSTRAINT_MINLENGTH, tl.getMinLength(), currentRow, 0, MINLEN_LABEL,
            MINLEN_TIP );
        makeSpinner( simple, DexActions.SETCONSTRAINT_MAXLENGTH, tl.getMaxLength(), currentRow, 2, MAXLEN_LABEL,
            MAXLEN_TIP );

        // Total and fraction digits
        makeSpinner( simple, DexActions.SETCONSTRAINT_TOTALDIGITS, tl.getTotalDigits(), currentRow, 4,
            TOTALDIGITS_LABEL, TOTALDIGITS_TIP );
        makeSpinner( simple, DexActions.SETCONSTRAINT_FRACTIONDIGITS, tl.getFractionDigits(), currentRow++, 6,
            FRACTIONDIGITS_LABEL, FRACTIONDIGITS_TIP );

        // Min/Max inclusive/exclusive
        makeField( simple, DexActions.SETCONSTRAINT_MININCLUSIVE, currentRow, 0, tl.getMinInclusive(), MININ_LABEL,
            MININ_TIP );
        makeField( simple, DexActions.SETCONSTRAINT_MAXINCLUSIVE, currentRow, 2, tl.getMaxInclusive(), MAXIN_LABEL,
            MAXIN_TIP );
        makeField( simple, DexActions.SETCONSTRAINT_MINEXCLUSIVE, currentRow, 4, tl.getMinExclusive(), MINEX_LABEL,
            MINEX_TIP );
        makeField( simple, DexActions.SETCONSTRAINT_MAXEXCLUSIVE, currentRow, 6, tl.getMaxExclusive(), MAXEX_LABEL,
            MAXEX_TIP );
    }

    private void makeSpinner(OtmSimpleObject simple, DexActions actionType, int value, int row, int col, String label,
        String tip) {
        DexEditField field = new DexEditField( simple, actionType, label, tip );
        field.makeSpinner( value );
        field.postField( propertyGrid, simple, row, col );
    }

    private DexEditField makeField(OtmSimpleObject simple, DexActions action, int row, int col, String value,
        String label, String tip) {
        DexEditField field = new DexEditField( simple, action, label, tip );
        field.makeTextField( value );
        field.postField( propertyGrid, simple, row, col );
        return field;
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
        // If deprecated, show the label, value and button
        if (obj.getDeprecation() != null && !obj.getDeprecation().isEmpty()) {
            DexEditField field = new DexEditField();
            field.set( currentRow++, 0, DEPRECATION_LABEL, DEPRECATION_TIP, null );
            field.post( propertyGrid, obj.deprecationProperty(), true );
        }
    }

    private void postDescription(OtmObject obj) {
        DexEditField field = new DexEditField();
        field.set( currentRow++, 0, DESCRIPTION_LABEL, DESCRIPTION_TIP, null );
        field.post( propertyGrid, obj.descriptionProperty(), true );
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
            DexEditField field = new DexEditField();
            field.set( currentRow++, 0, EXAMPLE_LABEL, EXAMPLE_TIP, null );
            field.post( propertyGrid, stringProperty, true );
        }
    }


    // private void postField(DexEditField field, OtmObject obj, StringProperty property) {
    // field.set( obj );
    // field.postField( propertyGrid );
    // // Add edit button with listener
    // Button button = DexEditField.makeButton( property );
    // propertyGrid.add( button, field.column, field.row );
    // }
    //
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

    // private void setProperty(ActionEvent a, StringProperty p) {
    // if (p != null && a.getSource() instanceof TextField) {
    // p.set( ((TextField) a.getSource()).getText() );
    // // log.debug( "Set text to: " + p.get() );
    // }
    // }
    //
}
