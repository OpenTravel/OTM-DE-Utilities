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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.actions.CopyPropertyAction;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.dex.actions.SetAssignedTypeAction;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmFacets.OtmAbstractDisplayFacet;
import org.opentravel.model.otmFacets.OtmAlias;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.OtmPropertyType;

import javafx.css.PseudoClass;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

/**
 * @author dmh
 *
 */
public final class MemberPropertiesRowFactory extends TreeTableRow<PropertiesDAO> {
    private static Logger log = LogManager.getLogger( MemberPropertiesRowFactory.class );

    private static final PseudoClass EDITABLE = PseudoClass.getPseudoClass( "editable" );
    private static final PseudoClass INHERITED = PseudoClass.getPseudoClass( "inherited" );
    private static final PseudoClass DIVIDER = PseudoClass.getPseudoClass( "divider" );
    private MemberPropertiesTreeTableController controller;

    private static final String PROPERTY_DIVIDER = "#";
    private final ContextMenu contextMenu = new ContextMenu();
    private Menu addMenu;
    private MenuItem deleteItem;
    private MenuItem changeType;
    MenuItem validateItem = null;

    // Constructor does not have access to content, just the empty row
    public MemberPropertiesRowFactory(MemberPropertiesTreeTableController controller) {
        this.controller = controller;

        // Create Context menu
        addMenu = new Menu( "Add" );
        setupAddMenu( addMenu );
        deleteItem = new MenuItem( "Delete" );
        changeType = new MenuItem( "Change Assigned Type" );
        validateItem = new MenuItem( "Validate" );

        contextMenu.getItems().addAll( addMenu, deleteItem, changeType, validateItem );
        setContextMenu( contextMenu );

        changeType.setOnAction( e -> changeAssignedType() );
        deleteItem.setOnAction( e -> delete() );
        validateItem.setOnAction( e -> validateMember() );

        // Set style listener (css class)
        treeItemProperty().addListener( (obs, oldTreeItem, newTreeItem) -> setCSSClass( this, newTreeItem ) );

        /*
         * Set up Drag-n-drop
         */
        // drag was detected, start drag-and-drop gesture with object name on drag board
        setOnDragDetected( event -> {
            if (event == null)
                return;
            OtmObject obj = getObject();
            Dragboard db = null;
            if (obj != null) {
                if (obj instanceof OtmTypeProvider)
                    db = startDragAndDrop( TransferMode.LINK );
                else
                    db = startDragAndDrop( TransferMode.COPY );
                String objId = obj.getOwningMember().getNameWithPrefix() + PROPERTY_DIVIDER + obj.getName();
                ClipboardContent content = new ClipboardContent();
                content.putString( objId );
                db.setContent( content );
                log.debug( "onDragDetected: copy object " + objId );
            }
            event.consume();
        } );

        // Control transfer model based on source data dragged over this target
        setOnDragOver( event -> {
            if (event == null)
                return;
            OtmObject source = getDraggedObject( event.getDragboard() );
            OtmObject target = getObject();
            if (target != null && source != null) {
                // log.debug( "dragOverDetected: copy object " + source + " to " + target );
                // LINK If source is type provider and target can be assigned the type
                if (source instanceof OtmTypeProvider && target instanceof OtmTypeUser
                    && SetAssignedTypeAction.isEnabled( target ))
                    event.acceptTransferModes( TransferMode.LINK );
                // COPY if target is a facet and copy enabled
                else if (target instanceof OtmFacet
                    && CopyPropertyAction.isEnabled( source, (OtmPropertyOwner) target ))
                    event.acceptTransferModes( TransferMode.COPY );
                else
                    event.acceptTransferModes( TransferMode.NONE );
            }
            event.consume();
        } );

        // Data dropped. Get source from drag board and use it on this target
        setOnDragDropped( event -> {
            if (event == null)
                return;
            OtmObject target = getObject();
            OtmObject source = getDraggedObject( event.getDragboard() );
            boolean success = false;
            if (source != null && target != null) {
                // log.debug( event.getAcceptedTransferMode() + " the property: " + source + " to " + target );
                if (event.getAcceptedTransferMode() == TransferMode.COPY)
                    target.getActionManager().run( DexActions.COPYPROPERTY, source, target );
                else if (event.getAcceptedTransferMode() == TransferMode.LINK)
                    target.getActionManager().run( DexActions.TYPECHANGE, target, source );

                controller.refresh();
                success = true;
            }
            // let the source know whether the string was successfully transferred and used
            event.setDropCompleted( success );
            event.consume();
        } );
    }

    private OtmObject getDraggedObject(Dragboard db) {
        if (db == null)
            return null;
        String objId = db.getString();
        OtmObject obj = null;
        OtmLibraryMember owner = null;
        if (controller.getMainController() != null && controller.getMainController().getModelManager() != null) {
            String ownerId = objId;
            String propertyId = "";
            if (objId.contains( PROPERTY_DIVIDER )) {
                ownerId = objId.substring( 0, objId.indexOf( PROPERTY_DIVIDER ) );
                propertyId = objId.substring( objId.indexOf( PROPERTY_DIVIDER ) + 1, objId.length() );
            }
            owner = controller.getMainController().getModelManager().getMember( ownerId );
            obj = owner;
            if (owner != null && !propertyId.isEmpty()) {
                for (OtmObject o : owner.getDescendants())
                    if (o.getName().equals( propertyId ))
                        obj = o;
            }
        }
        // log.debug( "Dragged object is: " + obj );
        return obj;
    }

    /**
     * Add a new member to the tree
     * 
     * @param t
     */
    private void addProperty(OtmPropertyType type) {
        // Get a valid owner as the subject
        OtmPropertyOwner owner = getPropertyOwner( getObject() );

        if (owner != null) {
            Object ret = owner.getActionManager().run( DexActions.ADDPROPERTY, owner, type );
            if (ret instanceof OtmObject) {
                // log.debug( "Add property action returned: " + ret );
                controller.post( ((OtmObject) ret).getOwningMember() );
            } else
                controller.refresh();
        }
    }

    private void changeAssignedType() {
        OtmObject obj = getObject();
        if (obj instanceof OtmTypeUser) {
            obj.getActionManager().run( DexActions.TYPECHANGE, (OtmTypeUser) obj );
            controller.getMainController().refresh();
        }
    }

    private void delete() {
        OtmObject object = getObject();
        if (object instanceof OtmProperty)
            delete( (OtmProperty) object );
        if (object instanceof OtmAlias)
            delete( (OtmAlias) object );
        controller.refresh();
    }

    private void delete(OtmProperty p) {
        p.getActionManager().run( DexActions.DELETEPROPERTY, p );
    }

    private void delete(OtmAlias a) {
        a.getActionManager().run( DexActions.DELETEALIAS, a );
    }

    /**
     * @return treeItem.getValue().getValue() or null
     */
    private OtmObject getObject() {
        PropertiesDAO dao = null;
        if (getTreeItem() != null)
            dao = getTreeItem().getValue();
        if (dao != null && dao.getValue() instanceof OtmObject)
            return dao.getValue();
        return null;
    }

    /**
     * @return treeItem.getValue().getValue() or null
     */
    private OtmProperty getProperty(OtmObject object) {
        if (object instanceof OtmProperty)
            return ((OtmProperty) object);
        return null;
    }

    /**
     * @return treeItem.getValue().getValue() or null
     */
    private OtmAlias getAlias(OtmObject object) {
        if (object instanceof OtmAlias)
            return ((OtmAlias) object);
        return null;
    }

    private OtmPropertyOwner getPropertyOwner(OtmObject object) {
        if (object instanceof OtmProperty)
            return ((OtmProperty) object).getParent();
        if (object instanceof OtmAbstractDisplayFacet)
            return ((OtmAbstractDisplayFacet) object).getParent();
        if (object instanceof OtmPropertyOwner)
            return (OtmPropertyOwner) object;
        // if (object instanceof OtmAlias)
        // return ((OtmAlias)object).getOwningMember();
        return null;

    }

    /**
     * @param tc
     * @param newTreeItem
     * @return
     */
    private void setCSSClass(TreeTableRow<PropertiesDAO> tc, TreeItem<PropertiesDAO> newTreeItem) {
        OtmObject object = getObject();
        if (object == null)
            return;
        DexActionManager am = object.getActionManager();

        addMenu.setDisable( true );
        changeType.setDisable( true );
        deleteItem.setDisable( true );
        // Turn on menu items
        if (am != null) {
            if (object instanceof OtmAlias) {
                // log.debug( "Alias = " + getAlias( object ) + am.getClass().getSimpleName() );
                deleteItem.setDisable( !am.isEnabled( DexActions.DELETEALIAS, getAlias( object ) ) );
            } else {
                OtmProperty property = getProperty( object );
                OtmPropertyOwner propertyOwner = getPropertyOwner( object );

                if (propertyOwner != null)
                    addMenu.setDisable(
                        !propertyOwner.getActionManager().isEnabled( DexActions.ADDPROPERTY, propertyOwner ) );
                changeType.setDisable( !am.isEnabled( DexActions.TYPECHANGE, property ) );
                deleteItem.setDisable( !am.isEnabled( DexActions.DELETEPROPERTY, property ) );
                // enable/disable each property type menu item
                OtmPropertyType.enableMenuItems( addMenu, propertyOwner );
            }
        }

        tc.setEditable( object.isEditable() );
        tc.pseudoClassStateChanged( INHERITED, newTreeItem.getValue().isInherited() );
        tc.pseudoClassStateChanged( EDITABLE, object.isEditable() );
        if (object instanceof OtmChildrenOwner) {
            // Make facets dividers, not aliases
            tc.pseudoClassStateChanged( DIVIDER, !(object instanceof OtmAlias) );
        } else {
            tc.pseudoClassStateChanged( DIVIDER, false );
        }
    }

    private void setupAddMenu(Menu menu) {
        for (MenuItem item : OtmPropertyType.menuItems()) {
            if (item.getUserData() instanceof OtmPropertyType)
                item.setOnAction( e -> addProperty( (OtmPropertyType) item.getUserData() ) );
            menu.getItems().add( item );
        }
    }

    private void validateMember() {
        OtmObject obj = getProperty( getObject() );
        if (obj != null) {
            obj.isValid( true );
            if (obj.getOwningMember() != null)
                obj.getOwningMember().isValid( true );
            controller.refresh();
            // log.debug( "Validate " + obj + " finding count: " + obj.getFindings().count() );
        }
    }

}
