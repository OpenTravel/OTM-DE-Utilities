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
import org.opentravel.common.ImageManager;
import org.opentravel.dex.controllers.DexDAO;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmFacets.OtmAbstractDisplayFacet;
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
public class MemberAndProvidersDAO implements DexDAO<OtmObject> {
    private static Log log = LogFactory.getLog( MemberAndProvidersDAO.class );

    protected OtmObject otmObject;
    protected OtmLibraryMember usingMember = null;

    public MemberAndProvidersDAO(OtmObject member) {
        this.otmObject = member;
    }

    public MemberAndProvidersDAO(OtmTypeProvider provider) {
        this.otmObject = provider;
    }

    public MemberAndProvidersDAO(OtmTypeProvider provider, OtmLibraryMember usingMember) {
        this.otmObject = provider;
        this.usingMember = usingMember;
    }

    /**
     * Create and add to tree with no conditional logic.
     * 
     * @return new tree item added to tree at the parent
     */
    public TreeItem<MemberAndProvidersDAO> createTreeItem(TreeItem<MemberAndProvidersDAO> parent) {
        TreeItem<MemberAndProvidersDAO> item = new TreeItem<>( this );
        item.setExpanded( false );
        if (parent != null)
            parent.getChildren().add( item );

        // Decorate if possible
        ImageView graphic = ImageManager.get( otmObject );
        item.setGraphic( graphic );
        Tooltip.install( graphic, new Tooltip( otmObject.getObjectTypeName() ) );
        return item;
    }

    public ObjectProperty<ImageView> errorImageProperty() {
        return otmObject.validationImageProperty();
    }

    public StringProperty errorProperty() {
        return otmObject.validationProperty();
    }

    public OtmLibraryMember getUsingMember() {
        return usingMember;
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

    public StringProperty libraryProperty() {
        if (otmObject instanceof OtmLibraryMember)
            return ((OtmLibraryMember) otmObject).libraryProperty();
        String name = otmObject.getLibrary() != null ? otmObject.getLibrary().getName() : "";
        return new ReadOnlyStringWrapper( name );
    }

    public StringProperty nameProperty() {
        return (otmObject.nameProperty());
    }

    public StringProperty prefixProperty() {
        if (otmObject instanceof OtmLibraryMember)
            return ((OtmLibraryMember) otmObject).prefixProperty();
        return new ReadOnlyStringWrapper( otmObject.getPrefix() );
    }

    public void setName(String name) {
        otmObject.setName( name );
    }

    @Override
    public String toString() {
        if (otmObject instanceof OtmAbstractDisplayFacet)
            return otmObject.getName();
        // return otmObject != null ? otmObject.getPrefix() + ":" + otmObject.toString() : "";
        return otmObject != null ? otmObject.toString() : "";
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

    public StringProperty versionProperty() {
        if (otmObject instanceof OtmLibraryMember)
            return ((OtmLibraryMember) otmObject).versionProperty();
        return new ReadOnlyStringWrapper( "" );
    }

}
