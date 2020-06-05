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

package org.opentravel.dex.controllers.member;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.dex.controllers.DexIncludedController;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmFacets.OtmAlias;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMemberType;

import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

/**
 * Row factory for the members tree table. Controls context menu and CSS style.
 * 
 * @author dmh
 *
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public final class MemberRowFactory extends TreeTableRow<MemberAndProvidersDAO> {
    private static Log log = LogFactory.getLog( MemberRowFactory.class );

    private final ContextMenu memberMenu = new ContextMenu();
    private static final PseudoClass EDITABLE = PseudoClass.getPseudoClass( "editable" );
    private static final PseudoClass DEPRECATED = PseudoClass.getPseudoClass( "deprecate" );
    private static final PseudoClass EDITANDDEPRECATED = PseudoClass.getPseudoClass( "editableanddeprecated" );
    private DexIncludedController<?> controller;

    Menu newMenu = null;
    MenuItem deleteItem = null;
    MenuItem validateItem = null;
    MenuItem copyItem = null;
    MenuItem addAliasItem = null;
    MenuItem deprecateItem = null;

    public MemberRowFactory(DexIncludedController<?> controller) {
        this.controller = controller;

        // Create Context menu
        addAliasItem = addItem( memberMenu, "Add Alias", e -> addAlias() );
        copyItem = addItem( memberMenu, "Copy", e -> copyMember() );
        deleteItem = addItem( memberMenu, "Delete", e -> deleteMember() );
        deprecateItem = addItem( memberMenu, "Deprecate", e -> deprecateMember() );

        newMenu = new Menu( "New" );
        // Create sub-menu for new objects
        for (OtmLibraryMemberType type : OtmLibraryMemberType.values())
            addItem( newMenu, type.label(), e -> newMember( type ) );
        memberMenu.getItems().add( newMenu );
        validateItem = addItem( memberMenu, "Validate", e -> validateMember() );
        setContextMenu( memberMenu );

        // Set style listener (css class)
        treeItemProperty().addListener( (obs, oldTreeItem, newTreeItem) -> setCSSClass( this, newTreeItem ) );

        /*
         * Set up Drag-n-drop
         */
        // drag was detected, start drag-and-drop gesture with object name on drag board
        setOnDragDetected( event -> {
            if (event == null)
                return;
            OtmObject obj = getSelectedObject( getTreeItem() );
            Dragboard db = null;
            if (obj instanceof OtmTypeProvider) {
                db = startDragAndDrop( TransferMode.LINK );
                String objId = obj.getOwningMember().getNameWithPrefix();
                ClipboardContent content = new ClipboardContent();
                content.putString( objId );
                db.setContent( content );
                log.debug( "onDragDetected: dragging " + objId );
            }
            event.consume();
        } );
    }

    // TODO - create utils class with statics for row factories
    private MenuItem addItem(Menu menu, String label, EventHandler<ActionEvent> handler) {
        MenuItem item = new MenuItem( label );
        menu.getItems().add( item );
        item.setOnAction( handler );
        return item;
    }

    private MenuItem addItem(ContextMenu menu, String label, EventHandler<ActionEvent> handler) {
        MenuItem item = new MenuItem( label );
        menu.getItems().add( item );
        item.setOnAction( handler );
        return item;
    }

    private void deleteMember() {
        OtmObject obj = getValue();
        if (obj instanceof OtmContributedFacet)
            obj = ((OtmContributedFacet) obj).getContributor();
        if (obj instanceof OtmLibraryMember)
            obj.getActionManager().run( DexActions.DELETELIBRARYMEMBER, (OtmLibraryMember) obj );
        else if (obj instanceof OtmAlias)
            obj.getActionManager().run( DexActions.DELETEALIAS, obj );
        super.updateTreeItem( getTreeItem().getParent() );
    }

    private void deprecateMember() {
        OtmObject obj = getValue();
        if (obj != null) {
            // Invoke action by setting a new value into the FX property
            obj.deprecationProperty().set( "Deprecated" );
        }
    }

    private void addAlias() {
        OtmObject obj = getValue();
        if (obj == null || obj instanceof OtmContributedFacet)
            return;
        obj.getActionManager().run( DexActions.ADDALIAS, (OtmLibraryMember) obj );
        super.updateTreeItem( getTreeItem().getParent() );
    }

    private void copyMember() {
        OtmObject obj = getValue();
        if (obj instanceof OtmContributedFacet)
            obj = ((OtmContributedFacet) obj).getContributor();
        if (obj instanceof OtmLibraryMember)
            obj.getActionManager().run( DexActions.COPYLIBRARYMEMBER, (OtmLibraryMember) obj );
        // else if (obj instanceof OtmAlias)
        // obj.getActionManager().run( DexActions.DELETEALIAS, obj );
        super.updateTreeItem( getTreeItem().getParent() );
    }

    private void validateMember() {
        OtmObject obj = getValue();
        if (obj instanceof OtmObject) {
            obj.isValid( true );
            // if (obj.getFindings() != null)
            // log.debug( "Validate " + obj + " finding count: " + obj.getFindings().count() );
        }
        controller.refresh();
    }

    private void newMember(OtmLibraryMemberType type) {
        OtmObject obj = getValue();
        // If they didn't select anything
        if (obj == null && controller.getMainController() != null)
            // Use any member the model manager delivers
            for (OtmLibraryMember member : controller.getMainController().getModelManager().getMembers()) {
            obj = member;
            break;
            }
        // Run action
        if (obj != null)
            obj.getModelManager().getActionManager( true ).run( DexActions.NEWLIBRARYMEMBER, obj, type );
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

    private OtmObject getSelectedObject(TreeItem<MemberAndProvidersDAO> item) {
        if (item != null && item.getValue() != null && item.getValue().getValue() instanceof OtmObject)
            return (item.getValue().getValue());
        return null;
    }

    /**
     * @param tc
     * @param newTreeItem
     * @return
     * @return
     */
    private void setCSSClass(TreeTableRow<MemberAndProvidersDAO> tc, TreeItem<MemberAndProvidersDAO> newTreeItem) {
        OtmObject obj = getSelectedObject( newTreeItem );
        if (obj != null) {
            setStateChanged( tc, obj.isDeprecated(), obj.isEditable() );
            // log.debug( obj.getNameWithPrefix() + " deprecated ? " + obj.isDeprecated() );

            addAliasItem.setDisable( !obj.getActionManager().isEnabled( DexActions.ADDALIAS, obj ) );
            newMenu.setDisable( !obj.getModelManager().hasEditableLibraries() );
            deprecateItem.setDisable( !obj.getActionManager().isEnabled( DexActions.DEPRECATIONCHANGE, obj ) );
            deleteItem.setDisable( true );
            // TODO - confirm that there will never be contributed facet then remove from code
            if ((obj instanceof OtmLibraryMember || obj instanceof OtmContributedFacet)
                && obj.getActionManager() != null)
                deleteItem.setDisable( !obj.getActionManager().isEnabled( DexActions.DELETELIBRARYMEMBER, obj ) );
            if (obj instanceof OtmAlias && obj.getActionManager() != null)
                deleteItem.setDisable( !obj.getActionManager().isEnabled( DexActions.DELETEALIAS, obj ) );
        }

    }

    private void setStateChanged(TreeTableRow<MemberAndProvidersDAO> tc, boolean deprecated, boolean editable) {
        if (deprecated && editable) {
            tc.pseudoClassStateChanged( EDITANDDEPRECATED, true );
            tc.pseudoClassStateChanged( DEPRECATED, false );
            tc.pseudoClassStateChanged( EDITABLE, false );
        } else {
            tc.pseudoClassStateChanged( EDITANDDEPRECATED, false );
            tc.pseudoClassStateChanged( DEPRECATED, deprecated );
            tc.pseudoClassStateChanged( EDITABLE, editable );
        }
    }
}
