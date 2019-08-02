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
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.resource.OtmAction;
import org.opentravel.model.resource.OtmActionRequest;
import org.opentravel.model.resource.OtmActionResponse;
import org.opentravel.model.resource.OtmParameter;

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
public class ActionsDAO implements DexDAO<OtmObject> {
    private static Log log = LogFactory.getLog( ActionsDAO.class );

    protected OtmObject otmObject;

    public ActionsDAO(OtmObject member) {
        this.otmObject = member;
    }

    // public ActionsDAO(OtmTypeProvider provider) {
    // this.otmObject = provider;
    // }

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

    // public StringProperty usedTypesProperty() {
    // String usedTypeCount = "";
    // if (otmObject instanceof OtmLibraryMember) {
    // List<OtmTypeProvider> u = ((OtmLibraryMember) otmObject).getUsedTypes();
    // if (u != null)
    // usedTypeCount = Integer.toString( u.size() );
    // }
    // return new ReadOnlyStringWrapper( usedTypeCount );
    // }

    public StringProperty errorProperty() {
        return otmObject.validationProperty();
    }

    public ObjectProperty<ImageView> errorImageProperty() {
        return otmObject.validationImageProperty();
    }

    // public StringProperty libraryProperty() {
    // if (otmObject instanceof OtmLibraryMember)
    // return ((OtmLibraryMember) otmObject).libraryProperty();
    // return new ReadOnlyStringWrapper( otmObject.getLibrary().getName() );
    // }

    // public StringProperty prefixProperty() {
    // if (otmObject instanceof OtmLibraryMember)
    // return ((OtmLibraryMember) otmObject).prefixProperty();
    // return new ReadOnlyStringWrapper( otmObject.getPrefix() );
    // }

    public StringProperty nameProperty() {
        StringProperty wrapper = otmObject.nameProperty();
        if (otmObject instanceof OtmActionRequest)
            wrapper = ((OtmActionRequest) otmObject).methodProperty();
        if (otmObject instanceof OtmActionResponse)
            wrapper = ((OtmActionResponse) otmObject).statusCodeProperty();
        // else if (otmObject instanceof OtmParameter)
        // wrapper = ((OtmParameter)otmObject).g);
        return wrapper;
    }

    // TODO - add repeat count ??
    public StringProperty actionFacetProperty() {
        StringProperty wrapper = new ReadOnlyStringWrapper( "" );
        if (otmObject instanceof OtmAction)
            wrapper.set( "" );
        else if (otmObject instanceof OtmActionRequest)
            wrapper = ((OtmActionRequest) otmObject).urlProperty();
        else if (otmObject instanceof OtmActionResponse)
            wrapper = ((OtmActionResponse) otmObject).examplePayloadProperty();
        else if (otmObject instanceof OtmTypeUser)
            wrapper.set( ((OtmTypeUser) otmObject).getTlAssignedTypeName() );
        else if (otmObject instanceof OtmParameter)
            wrapper = ((OtmParameter) otmObject).locationProperty();
        return wrapper;
    }

    public StringProperty contentProperty() {
        StringProperty wrapper = new ReadOnlyStringWrapper( "" );
        if (otmObject instanceof OtmActionRequest)
            wrapper = ((OtmActionRequest) otmObject).examplePayloadProperty();
        else if (otmObject instanceof OtmParameter)
            wrapper = ((OtmParameter) otmObject).typeProperty();
        return wrapper;
    }

    // public void setName(String name) {
    // otmObject.setName( name );
    // }

    @Override
    public String toString() {
        return otmObject != null ? otmObject.getPrefix() + ":" + otmObject.toString() : "";
    }

    // public StringProperty versionProperty() {
    // if (otmObject instanceof OtmLibraryMember)
    // return ((OtmLibraryMember) otmObject).versionProperty();
    // return new ReadOnlyStringWrapper( "" );
    // }

    /**
     * Create and add to tree with no conditional logic.
     * 
     * @return new tree item added to tree at the parent
     */
    public TreeItem<ActionsDAO> createTreeItem(TreeItem<ActionsDAO> parent) {
        TreeItem<ActionsDAO> item = new TreeItem<>( this );
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
