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

package org.opentravel.dex.controllers.library.usage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.common.ImageManager;
import org.opentravel.dex.controllers.DexDAO;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmFacets.OtmAbstractDisplayFacet;
import org.opentravel.model.otmFacets.OtmLibraryDisplayFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import java.util.List;
import java.util.Map;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

/**
 * The TreeItem properties for Library Members and Type Users.
 * <P>
 * Simple Data Access Object that contains and provides gui access.
 *
 * @author dmh
 * @param <T>
 *
 */
public class LibraryAndMembersDAO implements DexDAO<OtmObject> {
    private static Logger log = LogManager.getLogger( LibraryAndMembersDAO.class );

    protected OtmObject member;
    protected OtmLibrary library;

    public LibraryAndMembersDAO(OtmLibrary library) {
        this.library = library;
        member = new OtmLibraryDisplayFacet( library );
        // log.debug( "DAO created for library " + library.getName() );
    }

    public LibraryAndMembersDAO(OtmLibraryMember member) {
        this.member = member;
        library = null;
    }

    public LibraryAndMembersDAO(OtmLibraryMember member, OtmLibrary lib) {
        this.member = member;
        library = lib;
    }

    @Override
    public ImageView getIcon(ImageManager imageMgr) {
        return imageMgr != null ? ImageManager.get( member ) : null;
    }

    @Override
    public OtmObject getValue() {
        return member;
    }

    public boolean isEditable() {
        return member.isEditable();
    }


    public StringProperty prefixProperty() {
        if (member instanceof OtmLibraryMember)
            return ((OtmLibraryMember) member).prefixProperty();
        return new ReadOnlyStringWrapper( member.getPrefix() );
    }

    public StringProperty nameProperty() {
        // if (member instanceof OtmObject)
        // return "Used in "+member.nameProperty();
        return member.nameProperty();
    }

    @Override
    public String toString() {
        if (member instanceof OtmAbstractDisplayFacet)
            return member.toString();
        if (member != null) {
            if (library != null)
                return member.getName() + " uses types from " + library.getVersionChainName();
            // return member.getName() + " uses types from " + library.getPrefix() + " : " + library.getName();
            return member.getName();
        }
        return "";
    }

    public StringProperty versionProperty() {
        if (member instanceof OtmLibraryMember)
            return ((OtmLibraryMember) member).versionProperty();
        return new ReadOnlyStringWrapper( "" );
    }

    /**
     * Create and add to tree with no conditional logic.
     * 
     * @return new tree item added to tree at the parent
     */
    public TreeItem<LibraryAndMembersDAO> createTreeItem(TreeItem<LibraryAndMembersDAO> parent) {
        return createTreeItem( parent, this );
    }
    // TreeItem<LibraryAndMembersDAO> item = new TreeItem<>( this );
    // item.setExpanded( false );
    // if (parent != null)
    // parent.getChildren().add( item );
    //
    // // Decorate if possible
    // ImageView graphic = ImageManager.get( member );
    // item.setGraphic( graphic );
    // Tooltip toolTip = new Tooltip();
    // // if (member instanceof OtmTypeUser && ((OtmTypeUser) member).getAssignedType() != null)
    // // toolTip.setText( "Uses " + ((OtmTypeUser) member).getAssignedType().getNameWithPrefix() );
    // // else
    // // toolTip.setText( member.getObjectTypeName() );
    // toolTip.setText( "FIXME" );
    // Tooltip.install( graphic, toolTip );
    // return item;
    // }

    public static TreeItem<LibraryAndMembersDAO> createTreeItem(TreeItem<LibraryAndMembersDAO> parent,
        LibraryAndMembersDAO dao) {
        TreeItem<LibraryAndMembersDAO> item = new TreeItem<>( dao );
        // item.setExpanded( dao.getValue() instanceof OtmAbstractDisplayFacet );
        item.setExpanded( false );
        if (parent != null)
            parent.getChildren().add( item );

        // Decorate if possible
        ImageView graphic = ImageManager.get( dao.getValue() );
        item.setGraphic( graphic );
        Tooltip toolTip = new Tooltip();
        toolTip.setText( "FIXME" );
        Tooltip.install( graphic, toolTip );
        return item;
    }

    /**
     * 
     * @param map - map of library keys with array of library members to display under library
     * @param parent - tree item of parent
     */
    public static void createChildrenItems(Map<OtmLibrary,List<OtmLibraryMember>> map,
        TreeItem<LibraryAndMembersDAO> parent) {
        map.keySet().forEach( k -> {
            TreeItem<LibraryAndMembersDAO> libItem = new LibraryAndMembersDAO( k ).createTreeItem( parent );
            map.get( k ).forEach( m -> new LibraryAndMembersDAO( m, k ).createTreeItem( libItem ) );
        } );
    }

    /**
     * Create children items without library added to member's DAO
     * 
     * @param map - map of library keys with array of library members to display under library
     * @param parent - tree item of parent
     */
    public static void createChildrenItemsNoLib(Map<OtmLibrary,List<OtmLibraryMember>> map,
        TreeItem<LibraryAndMembersDAO> parent) {
        map.keySet().forEach( k -> {
            TreeItem<LibraryAndMembersDAO> libItem = new LibraryAndMembersDAO( k ).createTreeItem( parent );
            map.get( k ).forEach( m -> new LibraryAndMembersDAO( m ).createTreeItem( libItem ) );
        } );
    }

    /**
     * @param item
     */
    public void createChildrenItems(TreeItem<LibraryAndMembersDAO> item) {
        if (item != null && item.getValue() != null) {
            OtmObject obj = item.getValue().getValue();
            if (obj instanceof OtmLibraryDisplayFacet) {
                OtmLibrary lib = ((OtmLibraryDisplayFacet) obj).getLibrary();
                if (lib == null)
                    return;
                OtmModelManager mgr = lib.getModelManager();
                if (mgr == null)
                    return;
                mgr.getMembers( lib ).forEach( m -> new LibraryAndMembersDAO( m ).createTreeItem( item ) );
            }
        }
    }

}
