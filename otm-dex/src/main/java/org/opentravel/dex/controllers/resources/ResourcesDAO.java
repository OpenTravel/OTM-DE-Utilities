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

import org.opentravel.common.ImageManager;
import org.opentravel.dex.controllers.DexDAO;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Tooltip;
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
public class ResourcesDAO implements DexDAO<OtmObject> {
    // private static Log log = LogFactory.getLog( ResourcesDAO.class );

    protected OtmObject otmObject;

    public ResourcesDAO(OtmObject member) {
        this.otmObject = member;
    }

    public ResourcesDAO(OtmTypeProvider provider) {
        this.otmObject = provider;
    }

    @Override
    public ImageView getIcon(ImageManager imageMgr) {
        return ImageManager.get( otmObject );
    }

    @Override
    public OtmObject getValue() {
        return otmObject;
    }

    public boolean isEditable() {
        return otmObject.isEditable();
    }

    public StringProperty usedTypesProperty() {
        String usedTypeCount = "";
        if (otmObject instanceof OtmLibraryMember) {
            List<OtmTypeProvider> u = ((OtmLibraryMember) otmObject).getUsedTypes();
            if (u != null)
                usedTypeCount = Integer.toString( u.size() );
        }
        return new ReadOnlyStringWrapper( usedTypeCount );
    }

    public StringProperty errorProperty() {
        return otmObject.validationProperty();
    }

    public ObjectProperty<ImageView> errorImageProperty() {
        return otmObject.validationImageProperty();
    }

    public StringProperty libraryProperty() {
        if (otmObject instanceof OtmLibraryMember)
            return ((OtmLibraryMember) otmObject).libraryProperty();
        return new ReadOnlyStringWrapper( otmObject.getLibrary().getName() );
    }

    public StringProperty prefixProperty() {
        if (otmObject instanceof OtmLibraryMember)
            return ((OtmLibraryMember) otmObject).prefixProperty();
        return new ReadOnlyStringWrapper( otmObject.getPrefix() );
    }

    public StringProperty nameProperty() {
        return (otmObject.nameProperty());
    }

    public void setName(String name) {
        otmObject.setName( name );
    }

    @Override
    public String toString() {
        return otmObject != null ? otmObject.getPrefix() + ":" + otmObject.toString() : "";
    }

    public StringProperty versionProperty() {
        if (otmObject instanceof OtmLibraryMember)
            return ((OtmLibraryMember) otmObject).versionProperty();
        return new ReadOnlyStringWrapper( "" );
    }

    /**
     * Create and add to tree with no conditional logic.
     * 
     * @return new tree item added to tree at the parent
     */
    public TreeItem<ResourcesDAO> createTreeItem(TreeItem<ResourcesDAO> parent) {
        TreeItem<ResourcesDAO> item = new TreeItem<>( this );
        item.setExpanded( false );
        if (parent != null)
            parent.getChildren().add( item );

        // Decorate if possible
        ImageView graphic = ImageManager.get( otmObject );
        item.setGraphic( graphic );
        Tooltip.install( graphic, new Tooltip( otmObject.getObjectTypeName() ) );
        return item;
    }

}
