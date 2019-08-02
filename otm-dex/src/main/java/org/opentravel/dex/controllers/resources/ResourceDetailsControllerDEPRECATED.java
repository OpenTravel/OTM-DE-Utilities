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

package org.opentravel.dex.controllers.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.ImageManager;
import org.opentravel.dex.actions.DexActionManager.DexActions;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.popup.DialogBoxContoller;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmResource;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Controller for resource details controller.
 * 
 * @author dmh
 *
 */
@Deprecated
public class ResourceDetailsControllerDEPRECATED extends DexIncludedControllerBase<Void> {
    private static Log log = LogFactory.getLog( ResourceDetailsControllerDEPRECATED.class );

    /**
     * FXML Java FX Nodes this controller is dependent upon
     */
    @FXML
    private VBox resourceDetailsView;
    @FXML
    private TextField memberName;
    @FXML
    private TitledPane resourceTitle;
    @FXML
    private Label objectLabel;
    @FXML
    private ImageView objectImageView;
    @FXML
    private TextField libraryName;
    @FXML
    private Button changeLibraryButton;
    @FXML
    private Label typeLabel;
    @FXML
    private TextField baseTypeName;
    @FXML
    private Button changeBaseButton;
    @FXML
    private TextField assignedTypeName;
    @FXML
    private Button changeTypeButton;
    @FXML
    private TextField memberDescription;

    private OtmResource selectedResource;

    private boolean ignoreClear = false;

    // All event types fired by this controller.
    private static final EventType[] publishedEvents = {};

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents =
        {DexMemberSelectionEvent.MEMBER_SELECTED, DexModelChangeEvent.MODEL_CHANGED};

    public ResourceDetailsControllerDEPRECATED() {
        super( subscribedEvents, publishedEvents );
        log.debug( "Member Details Controller constructor." );
    }

    @Override
    public void checkNodes() {
        if (!(resourceDetailsView instanceof VBox))
            throw new IllegalStateException( "Member Details not injected by FXML." );
        if (objectLabel == null)
            throw new IllegalStateException( "Object label not injected by FXML." );
        if (!(libraryName instanceof TextField))
            throw new IllegalStateException( "Library  not injected by FXML." );
        if (!(memberName instanceof TextField))
            throw new IllegalStateException( "memberName not injected by FXML." );
        if (!(typeLabel instanceof Label))
            throw new IllegalStateException( "label not injected by FXML." );
        if (!(memberDescription instanceof TextField))
            throw new IllegalStateException( "member description not injected by FXML." );
        if (!(assignedTypeName instanceof TextField))
            throw new IllegalStateException( "member type name not injected by FXML." );
    }

    @Override
    public void initialize() {
        log.debug( "Member Filter Controller - Initialize" );
        checkNodes();

    }

    @Override
    public void configure(DexMainController mainController) {
        super.configure( mainController );
        eventPublisherNode = resourceDetailsView;

        changeBaseButton.setOnAction( e -> postNotImplemented() );
        changeTypeButton.setOnAction( e -> postNotImplemented() );
    }

    @Override
    public void handleEvent(Event event) {
        if (event instanceof DexMemberSelectionEvent)
            memberSelectionHandler( (DexMemberSelectionEvent) event );
        if (event instanceof DexModelChangeEvent)
            modelChangeEvent( (DexModelChangeEvent) event );
    }

    public void modelChangeEvent(DexModelChangeEvent event) {
        clear();
    }

    public void memberSelectionHandler(DexMemberSelectionEvent event) {
        if (event != null && event.getMember() != null) {
            if (event.getMember() instanceof OtmResource)
                post( (OtmResource) event.getMember() );
            else if (event.getMember() instanceof OtmResourceChild)
                post( ((OtmResourceChild) event.getMember()).getOwningMember() );
        }
    }

    private void postNotImplemented() {
        DialogBoxContoller.init().show( "Not Implemented", "Work in progress." );
    }

    public void post(OtmResource resource) {
        if (resource == null) {
            clear();
            return;
        }
        selectedResource = resource;
        // Collection<OtmTypeUser> u = member.getDescendantsTypeUsers();
        // Collection<OtmTypeProvider> p = member.getDescendantsTypeProviders();
        // Collection<OtmTypeProvider> c = member.getChildrenTypeProviders();

        objectLabel.setTooltip( new Tooltip( resource.getObjectTypeName() ) );
        // if (imageMgr != null)
        // objectImageView.setImage( imageMgr.get_OLD( member.getIconType() ) );
        objectImageView.setImage( ImageManager.getImage( resource.getIconType() ) );
        resourceTitle.setText( resource.getObjectTypeName() + " - " + resource.getName() );

        memberName.setEditable( resource.isEditable() );
        memberName.setText( resource.nameProperty().get() );
        memberName.setOnAction( e -> resource.nameProperty().set( memberName.getText() ) );

        // Set library
        libraryName.setEditable( false );
        libraryName.setText( resource.libraryProperty().get() );
        changeLibraryButton.setDisable( !resource.isEditable() );
        changeLibraryButton.setDisable( true ); // TEMP

        // Description
        memberDescription.setEditable( resource.isEditable() );
        memberDescription.setText( resource.descriptionProperty().get() );
        memberDescription.setOnAction( e -> resource.descriptionProperty().set( memberDescription.getText() ) );

        // Base type
        changeBaseButton.setDisable( true ); // TEMP
        baseTypeName.setText( resource.baseTypeProperty().get() );

        // Assigned type
        final String TYPELABELRESOURCE = "Exposed Object";
        typeLabel.setDisable( !(resource instanceof OtmTypeUser) );
        typeLabel.setText( TYPELABELRESOURCE );

        // icon?
        if (resource.getActionManager().isEnabled( DexActions.TYPECHANGE, resource )) {
            changeTypeButton.setDisable( false );
            changeTypeButton.setOnAction( e -> setAssignedType() );
        } else {
            changeTypeButton.setDisable( true );
            changeTypeButton.setOnAction( null );
        }
        assignedTypeName.setDisable( (!(resource instanceof OtmTypeUser)) );
        assignedTypeName.setEditable( false );
        if (resource instanceof OtmTypeUser && (resource).getAssignedType() != null) {
            assignedTypeName.setTooltip( new Tooltip( (resource).getAssignedType().getDescription() ) );
            assignedTypeName.setText( (resource).assignedTypeProperty().get() );
        } else {
            assignedTypeName.setTooltip( null );
            assignedTypeName.setText( "" );
        }
    }

    // Called when button is pressed
    private void setAssignedType() {
        log.debug( "Set assigned type event." );
        if (selectedResource instanceof OtmTypeUser)
            selectedResource.getActionManager().run( DexActions.TYPECHANGE, selectedResource, null );
        refresh();
    }

    // /**
    // * Make and fire a filter event. Set ignore clear in case event handler tries to clear() this controller.
    // */
    // private void fireFilterChangeEvent() {
    // if (eventPublisherNode != null) {
    // ignoreClear = true; // Set just in case event handler does a clear
    // log.debug("Ready to fire controller level Filter Change event.");
    // eventPublisherNode.fireEvent(new DexFilterChangeEvent(this, memberDetails));
    // ignoreClear = false;
    // } else if (popupController != null) {
    // popupController.refresh();
    // }
    // }

    @Override
    public void clear() {
        // When posting updated filter results, do not clear the filters.
        if (!ignoreClear) {
            // if (mainController != null)
            // modelMgr = mainController.getModelManager();
            selectedResource = null;
            assignedTypeName.setText( "" );
            memberName.setText( "" );
            libraryName.setText( "" );
            baseTypeName.setText( "" );
            memberDescription.setText( "" );
            objectImageView.setImage( null );
        }
    }

    @Override
    public void refresh() {
        if (selectedResource instanceof OtmResource)
            post( (OtmResource) selectedResource );
        else if (selectedResource instanceof OtmResourceChild)
            post( ((OtmResourceChild) selectedResource).getOwningMember() );
    }

}
