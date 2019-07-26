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
import org.opentravel.dex.controllers.DexDAO;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.resource.OtmActionRequest;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

/**
 * The TreeItem properties for Library Members and Type Providers.
 * <P>
 * Used in ModelMembersTreeController TreeTableView. Simple Data Access Object that contains and provides gui access.
 *
 * @author dmh
 * @param <T>
 *
 */
public class ResourcePathsDAO implements DexDAO<OtmActionRequest> {
    private static Log log = LogFactory.getLog( ResourcePathsDAO.class );

    protected OtmResource otmResource;
    protected OtmActionRequest otmRequest;

    // public ResourcePathsDAO(OtmResource resource) {
    // this.otmResource = resource;
    // }
    public ResourcePathsDAO(OtmActionRequest request) {
        this.otmRequest = request;
        otmResource = request.getOwningMember();
    }

    // public ResourcePathsDAO(OtmTypeProvider provider) {
    // this.otmResource = provider;
    // }

    @Override
    public ImageView getIcon(ImageManager imageMgr) {
        return ImageManager.get( otmResource );
    }

    @Override
    public OtmActionRequest getValue() {
        return otmRequest;
    }

    public boolean isEditable() {
        return otmResource.isEditable();
    }

    // public StringProperty usedTypesProperty() {
    // String usedTypeCount = "";
    // if (otmResource instanceof OtmLibraryMember) {
    // List<OtmTypeProvider> u = ((OtmLibraryMember) otmResource).getUsedTypes();
    // if (u != null)
    // usedTypeCount = Integer.toString( u.size() );
    // }
    // return new ReadOnlyStringWrapper( usedTypeCount );
    // }

    // public StringProperty errorProperty() {
    // return otmResource.validationProperty();
    // }

    public ObjectProperty<ImageView> errorImageProperty() {
        return otmResource.validationImageProperty();
    }

    // public StringProperty libraryProperty() {
    // if (otmResource instanceof OtmLibraryMember)
    // return ((OtmLibraryMember) otmResource).libraryProperty();
    // return new ReadOnlyStringWrapper( otmResource.getLibrary().getName() );
    // }

    public StringProperty methodProperty() {
        // otmRequest.getMethod();
        // if (otmResource instanceof OtmLibraryMember)
        // return ((OtmLibraryMember) otmResource).prefixProperty();
        return new ReadOnlyStringWrapper( "METHOD" );
    }

    public StringProperty nameProperty() {
        return (otmRequest.nameProperty());
    }

    public StringProperty urlProperty() {
        // if (otmResource instanceof OtmLibraryMember)
        // return ((OtmLibraryMember) otmResource).prefixProperty();
        return new ReadOnlyStringWrapper( "http://www.travelport.com/someCollection/{collectionID}" );
    }

    // public void setName(String name) {
    // otmResource.setName( name );
    // }

    @Override
    public String toString() {
        return otmResource != null ? otmResource.getPrefix() + ":" + otmResource.toString() : "";
    }

    // public StringProperty versionProperty() {
    // if (otmResource instanceof OtmLibraryMember)
    // return ((OtmLibraryMember) otmResource).versionProperty();
    // return new ReadOnlyStringWrapper( "" );
    // }

    /**
     * Create and add to tree with no conditional logic.
     * 
     * @return new tree item added to tree at the parent
     */
    public TreeItem<ResourcePathsDAO> createTreeItem(TreeItem<ResourcePathsDAO> parent) {
        TreeItem<ResourcePathsDAO> item = new TreeItem<>( this );
        item.setExpanded( false );
        if (parent != null)
            parent.getChildren().add( item );

        // Decorate if possible
        // ImageView graphic = ImageManager.get( otmObject );
        // item.setGraphic( graphic );
        // Tooltip.install( graphic, new Tooltip( otmObject.getObjectTypeName() ) );
        return item;
    }

}
