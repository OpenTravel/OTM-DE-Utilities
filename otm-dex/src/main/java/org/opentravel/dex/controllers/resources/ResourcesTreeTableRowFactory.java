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
import org.opentravel.dex.actions.DexActions;
import org.opentravel.dex.controllers.DexIncludedController;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMemberType;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.resource.OtmAction;
import org.opentravel.model.resource.OtmActionFacet;
import org.opentravel.model.resource.OtmParameterGroup;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLResourceParentRef;

import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;

/**
 * @author dmh
 *
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public final class ResourcesTreeTableRowFactory extends TreeTableRow<ResourcesDAO> {
    private static Log log = LogFactory.getLog( ResourcesTreeTableRowFactory.class );

    private static final PseudoClass EDITABLE = PseudoClass.getPseudoClass( "editable" );
    private static final PseudoClass INHERITED = PseudoClass.getPseudoClass( "inherited" );
    private static final PseudoClass DIVIDER = PseudoClass.getPseudoClass( "divider" );

    private final ContextMenu resourceMenu = new ContextMenu();
    MenuItem arItem = null;
    MenuItem paramItem = null;
    Menu addMenu = null;
    Menu addAFMenu = null;
    private MenuItem deleteItem = null;
    private MenuItem addResource = null;
    private MenuItem validateResource = null;
    private MenuItem refreshItem = null;

    private DexIncludedController<?> controller;


    public ResourcesTreeTableRowFactory(DexIncludedController<?> controller) {
        this.controller = controller;

        // Create Context menu

        // Add is a sub-menu
        addMenu = new Menu( "Add" ); // Menu is sub-type of menuItem
        addItem( addMenu, " Action", e -> addChild( new TLAction() ) );
        addItem( addMenu, " ParentRef", e -> addChild( new TLResourceParentRef() ) );
        addItem( addMenu, " Parameter Group", e -> addChild( new TLParamGroup() ) );


        addMenu.getItems().add( new SeparatorMenuItem() );
        addItem( addMenu, " Action Facet", e -> addChild( new TLActionFacet() ) );
        addAFMenu = new Menu( "Action Facets" ); // Menu is sub-type of menuItem
        addItem( addAFMenu, "Request Action Facet",
            e -> addChild( new TLActionFacet(), OtmActionFacet.BuildTemplate.REQUEST ) );
        addItem( addAFMenu, "Response Action Facet",
            e -> addChild( new TLActionFacet(), OtmActionFacet.BuildTemplate.RESPONSE ) );
        addItem( addAFMenu, "List Response Action Facet",
            e -> addChild( new TLActionFacet(), OtmActionFacet.BuildTemplate.LIST ) );
        addMenu.getItems().add( addAFMenu );

        addMenu.getItems().add( new SeparatorMenuItem() );
        arItem = addItem( addMenu, " Action Response", e -> addResponse() );
        paramItem = addItem( addMenu, " Parameter", e -> addParameter() );
        resourceMenu.getItems().add( addMenu );
        //
        resourceMenu.getItems().add( new SeparatorMenuItem() );
        deleteItem = addItem( "Delete", this::deleteChild );
        //
        resourceMenu.getItems().add( new SeparatorMenuItem() );
        addResource = addItem( "New Resource", e -> newResource() );

        // validate
        resourceMenu.getItems().add( new SeparatorMenuItem() );
        validateResource = addItem( "Validate", e -> validateResource() );
        //
        resourceMenu.getItems().add( new SeparatorMenuItem() );
        refreshItem = addItem( "Refresh", e -> refresh( null ) );

        // Add the menu to the factory
        setContextMenu( resourceMenu );

        // Set style listener (css class)
        treeItemProperty().addListener( (obs, oldTreeItem, newTreeItem) -> setCSSClass( this, newTreeItem ) );
    }

    private MenuItem addItem(String label, EventHandler<ActionEvent> handler) {
        MenuItem item = new MenuItem( label );
        resourceMenu.getItems().add( item );
        item.setOnAction( handler );
        return item;
    }

    private MenuItem addItem(Menu menu, String label, EventHandler<ActionEvent> handler) {
        MenuItem item = new MenuItem( label );
        menu.getItems().add( item );
        item.setOnAction( handler );
        return item;
    }

    private void validateResource() {
        OtmObject obj = getValue();
        if (obj == null && controller.getSelection() != null
            && controller.getSelection().getValue() instanceof OtmObject)
            obj = (OtmObject) controller.getSelection().getValue();

        if (obj != null)
            obj.isValid( true );

        controller.refresh();
    }

    private void newResource() {
        // Get something to use as the subject. Where the menu item was, what was selected or just any resource.
        OtmObject obj = getValue();
        if (obj == null && controller.getSelection() != null
            && controller.getSelection().getValue() instanceof OtmObject)
            obj = (OtmObject) controller.getSelection().getValue();
        // If there is nothing selected, use any object
        if (obj == null)
            obj = controller.getMainController().getModelManager().getResources( false ).get( 0 );

        Object result = null;
        // Run action
        if (obj != null)
            result = obj.getActionManager().run( DexActions.NEWLIBRARYMEMBER, obj, OtmLibraryMemberType.RESOURCE );
        // // Update display
        // if (result instanceof OtmObject) {
        // TreeItem<ResourcesDAO> item =
        // new ResourcesDAO( (OtmObject) result ).createTreeItem( getTreeItem().getParent() );
        // super.updateTreeItem( item );
        // }
    }

    private void deleteChild(ActionEvent e) {
        OtmObject obj = getValue();
        Object parent = null;
        // Run delete action
        if (obj instanceof OtmResource)
            obj.getActionManager().run( DexActions.DELETELIBRARYMEMBER, (OtmResource) obj );
        else if (obj instanceof OtmResourceChild)
            obj.getActionManager().run( DexActions.DELETERESOURCECHILD, obj );
    }

    private Object addChild(TLModelElement tlChild) {
        OtmObject obj = getValue();
        Object result = null;
        if (obj != null && obj.getOwningMember() instanceof OtmResource) {
            OtmResource resource = (OtmResource) obj.getOwningMember();
            result = resource.getActionManager().run( DexActions.ADDRESOURCECHILD, resource, tlChild );
            refresh( result );
        }
        return result;
    }

    private void addChild(TLModelElement tlChild, OtmActionFacet.BuildTemplate template) {
        Object result = addChild( tlChild );
        if (result instanceof OtmActionFacet)
            ((OtmActionFacet) result).build( template );
        refresh( result );
    }

    private void addResponse() {
        OtmObject obj = getValue();
        if (obj instanceof OtmAction)
            refresh( obj.getActionManager().run( DexActions.ADDRESOURCERESPONSE, obj ) );
    }

    private void addParameter() {
        OtmObject obj = getValue();
        if (obj instanceof OtmParameterGroup)
            refresh( obj.getActionManager().run( DexActions.ADDRESOURCEPARAMETER, obj ) );
    }

    // Update GUI
    private void refresh(Object newObject) {
        if (newObject instanceof OtmResourceChild) {
            TreeItem<ResourcesDAO> item =
                new ResourcesDAO( (OtmObject) newObject ).createTreeItem( getTreeItem().getParent() );
            super.updateTreeItem( item ); // needed to apply stylesheet to new item
        } else
            controller.refresh();
    }

    /**
     * @return the value OtmObject or null
     */
    private OtmObject getValue() {
        if (getTreeItem() != null && getTreeItem().getValue() != null
            && getTreeItem().getValue().getValue() instanceof OtmObject)
            return getTreeItem().getValue().getValue();
        return null;
    }

    /**
     * @param row
     * @param item
     */
    private void setCSSClass(TreeTableRow<ResourcesDAO> tc, TreeItem<ResourcesDAO> item) {
        if (item != null && item.getValue() != null && item.getValue().getValue() != null) {
            tc.pseudoClassStateChanged( EDITABLE, item.getValue().isEditable() );

            tc.pseudoClassStateChanged( INHERITED, item.getValue().isInherited() );

            // Turn on/off context sensitive items
            addResource.setDisable( !item.getValue().isEditable() );
            addMenu.setDisable( !item.getValue().isEditable() );
            deleteItem.setDisable( !item.getValue().isEditable() );
            arItem.setDisable( !(getValue() instanceof OtmAction) );
            paramItem.setDisable( !(getValue() instanceof OtmParameterGroup) );
        } else
            addMenu.setDisable( true );
    }
}

