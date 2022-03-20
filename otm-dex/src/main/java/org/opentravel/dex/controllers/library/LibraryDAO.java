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

package org.opentravel.dex.controllers.library;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.common.ImageManager;
import org.opentravel.dex.controllers.DexDAO;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmVersionChain;

import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

/**
 * The type of the TreeItem instances used in this TreeTableView. Simple Data Access Object that contains and provides
 * gui access to OTM model library members.
 * <p>
 * Properties are lazy evaluated and retained. Once created they can not be changed. Simply create a new DAO to change
 * property values.
 * 
 * @author dmh
 *
 * 
 * @author dmh
 *
 */
public class LibraryDAO implements DexDAO<OtmLibrary> {
    private static Logger log = LogManager.getLogger( LibraryDAO.class );

    protected OtmLibrary library;
    String editable = "False";
    int size = 0; // can be static because model change events creates new DAOs.
    // Properties
    private StringProperty editProperty = null;
    private StringProperty fileNameProperty = null;
    private StringProperty prefixProperty = null;
    private StringProperty nameProperty = null;
    private StringProperty nameSpaceProperty = null;
    private StringProperty projectsProperty = null;
    private StringProperty versionProperty = null;

    public LibraryDAO(OtmLibrary library) {
        this.library = library;
        if (library == null)
            throw new IllegalArgumentException( "No library provided to Project-Library DAO" );
        size = library.getMembers().size();
    }

    public StringProperty editProperty() {
        if (editProperty == null) {
            editable = "False";
            if (library.isEditable())
                editable = "True";
            editProperty = new ReadOnlyStringWrapper( editable );
        }
        return editProperty;
    }

    public StringProperty fileNameProperty() {
        if (fileNameProperty == null) {
            String path = "";
            if (library != null && library.getTL() != null && library.getTL().getLibraryUrl() != null)
                path = library.getTL().getLibraryUrl().getPath();
            fileNameProperty = new ReadOnlyStringWrapper( path );
        }
        return fileNameProperty;
    }

    @Override
    public ImageView getIcon(ImageManager imageMgr) {
        return ImageManager.get( library.getIconType() );
    }

    @Override
    public OtmLibrary getValue() {
        return library;
    }

    public StringProperty nameProperty() {
        if (nameProperty == null) {
            nameProperty = new SimpleStringProperty( library.getName() );
            nameProperty.addListener( (o, v, n) -> library.getTL().setName( n ) );
        }
        return nameProperty;
    }

    // Question - Why use ReadOnly when it doesn't seem to matter if the property is readonly or not.
    // Must be tested in controller.
    public StringProperty namespaceProperty() {
        if (nameSpaceProperty == null) {
            if (library.isEditable() && library.isUnmanaged())
                nameSpaceProperty = new SimpleStringProperty( library.getTL().getNamespace() );
            else
                nameSpaceProperty = new ReadOnlyStringWrapper( library.getTL().getNamespace() );
            nameSpaceProperty.addListener( (o, v, n) -> library.getTL().setNamespace( n ) );
        }
        return nameSpaceProperty;
    }

    public StringProperty prefixProperty() {
        if (prefixProperty == null) {
            if (library.isEditable() && library.isUnmanaged()) {
                prefixProperty = new SimpleStringProperty( library.getPrefix() );
                prefixProperty.addListener( (o, v, n) -> library.getTL().setPrefix( n ) );
            } else
                prefixProperty = new ReadOnlyStringWrapper( library.getPrefix() );
        }
        return prefixProperty;
    }

    public StringProperty stateProperty() {
        return new ReadOnlyStringWrapper( library.getState().toString() );
    }

    public IntegerProperty referenceProperty() {
        return new SimpleIntegerProperty( library.getTL().getReferenceCount() );
    }

    public IntegerProperty sizeProperty() {
        return new SimpleIntegerProperty( size );
    }

    public StringProperty statusProperty() {
        return new ReadOnlyStringWrapper( library.getStatus().toString() );
    }

    public StringProperty lockedProperty() {
        return new ReadOnlyStringWrapper( library.getLockedBy() );
    }

    public StringProperty projectsProperty() {
        if (projectsProperty == null) {
            String projects = "";
            for (String name : library.getProjectNames())
                if (projects.isEmpty())
                    projects = name;
                else
                    projects = projects + ", " + name;
            projectsProperty = new ReadOnlyStringWrapper( projects );
        }
        return projectsProperty;
    }

    public BooleanProperty readonlyProperty() {
        return new SimpleBooleanProperty( library.getTL().isReadOnly() );
    }

    @Override
    public String toString() {
        return library.getName() + " - " + library.getVersion();
        // return library.toString();
    }

    public StringProperty versionProperty() {
        if (versionProperty == null) {
            versionProperty = new SimpleStringProperty( library.getTL().getVersion() );
            versionProperty.addListener( (o, v, n) -> library.getTL().setVersion( n ) );
        }
        return versionProperty;
    }

    /**
     * Get the set of libraries in the base namespace from the model manager, create tree items for each and add to
     * parent's children.
     * 
     * @param chainName
     * @param modelMgr
     * @param parent
     * @param editableOnly filter setting
     */
    // public static void createNSItems(String chainName, OtmModelManager modelMgr, TreeItem<LibraryDAO> parent,
    public static void createNSItems(OtmVersionChain chain, TreeItem<LibraryDAO> parent, boolean editableOnly) {
        // log.debug( "Creating items for chain name: " + chain );
        TreeItem<LibraryDAO> latestItem = null;
        OtmLibrary latest = null;

        // Skip this chain if filter set and chain is not editable
        if (editableOnly && !chain.isChainEditable())
            return;

        // Get the latest library in the chain
        latest = chain.getLatestVersion();
        if (latest != null) {
            latestItem = new LibraryDAO( latest ).createTreeItem( parent );
            List<OtmLibrary> libs = chain.getLibraries();

            // Put all other libraries under it
            for (OtmLibrary lib : libs)
                if (lib != latest)
                    new LibraryDAO( lib ).createTreeItem( latestItem );
        }

        // Simplified after refactoring version chain. 5/30/2021
        // // List<OtmLibrary> libs = modelMgr.getBaseNSLibraries( chainName );
        // List<OtmLibrary> libs = modelMgr.getChainLibraries( chainName );
        //
        // for (OtmLibrary lib : libs)
        // if (lib != null && lib.isLatestVersion()) {
        // if (!editableOnly || lib.isEditable())
        // latestItem = new LibraryDAO( lib ).createTreeItem( parent );
        // latest = lib;
        // }
        // // Put 1st item at root, all rest under it.
        // if (latest != null)
        // for (OtmLibrary lib : libs)
        // if (lib != latest)
        // if (!editableOnly || lib.isEditable())
        // new LibraryDAO( lib ).createTreeItem( latestItem );

    }

    /**
     * TreeItem class does not extend the Node class.
     * 
     * Therefore, you cannot apply any visual effects or add menus to the tree items. Use the cell factory mechanism to
     * overcome this obstacle and define as much custom behavior for the tree items as your application requires.
     * 
     * @param item
     * @return
     */
    public TreeItem<LibraryDAO> createTreeItem(TreeItem<LibraryDAO> parent) {
        // log.debug( "Create tree item for: " + library );
        if (parent != null) {
            TreeItem<LibraryDAO> item = new TreeItem<>( this );
            item.setExpanded( false );
            parent.getChildren().add( item );

            // Decorate if possible
            ImageView graphic = ImageManager.get( library.getIconType() );
            item.setGraphic( graphic );
            // Tooltip toolTip = new Tooltip();
            // // toolTip.setText( "FIXME" );
            // Tooltip.install( graphic, toolTip );
            return item;
        }
        return null;
    }

}
