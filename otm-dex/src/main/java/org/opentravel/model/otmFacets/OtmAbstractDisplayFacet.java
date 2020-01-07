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

package org.opentravel.model.otmFacets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.OtmPropertyFactory;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLIndicatorOwner;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLPropertyOwner;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Abstract OTM facade for Facets that are only used in displays and are not part of the model.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class OtmAbstractDisplayFacet implements OtmPropertyOwner {
    private static Log log = LogFactory.getLog( OtmAbstractDisplayFacet.class );

    // private OtmChildrenOwner parent;
    private OtmPropertyOwner parent;
    // private List<OtmProperty> children;

    public OtmAbstractDisplayFacet(OtmPropertyOwner parent) {
        this.parent = parent;
    }

    @Override
    public OtmObject add(OtmObject child) {
        return parent.add( child );
    }

    @Override
    public OtmProperty add(TLModelElement tl) {
        return parent.add( tl );
    }

    // private boolean contains(List<OtmObject> list, OtmObject child) {
    // if (list.contains( child ))
    // return true;
    // for (OtmObject c : list)
    // if (c.getTL() == child.getTL())
    // return true;
    //
    // return false;
    // }

    @Override
    public void delete(OtmObject property) {
        parent.delete( property );
    }

    @Override
    public DexActionManager getActionManager() {
        return parent.getActionManager();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OtmObject> getChildren() {
        return parent.getChildren();
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        Collection<OtmObject> hierarchy = new ArrayList<>();
        parent.getInheritedChildren().forEach( hierarchy::add );
        parent.getChildren().forEach( hierarchy::add );
        return hierarchy;
    }

    @Override
    public Collection<OtmTypeProvider> getChildrenTypeProviders() {
        return Collections.emptyList();
    }

    @Override
    public List<OtmObject> getDescendants() {
        return Collections.emptyList();
    }

    @Override
    public Collection<OtmChildrenOwner> getDescendantsChildrenOwners() {
        return Collections.emptyList();
    }

    @Override
    public Collection<OtmPropertyOwner> getDescendantsPropertyOwners() {
        return Collections.emptyList();
    }

    @Override
    public Collection<OtmTypeProvider> getDescendantsTypeProviders() {
        return Collections.emptyList();
    }

    @Override
    public Collection<OtmTypeUser> getDescendantsTypeUsers() {
        Collection<OtmTypeUser> users = new ArrayList<>();
        if (getChildren() != null)
            getChildren().forEach( c -> {
                if (c instanceof OtmTypeUser)
                    users.add( (OtmTypeUser) c );
            } );
        return users;
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.FACET;
    }

    @Override
    public List<OtmObject> getInheritedChildren() {
        return parent.getInheritedChildren();
    }

    @Override
    public String getName() {
        return "FIXME";
    }

    /**
     * @see org.opentravel.model.OtmObject#nameProperty()
     */
    @Override
    public StringProperty nameProperty() {
        return new ReadOnlyStringWrapper( getName() );
    }

    @Override
    public StringProperty nameEditingProperty() {
        return new ReadOnlyStringWrapper( getName() );
    }

    @Override
    public StringProperty nameEditingProperty(String name) {
        return new ReadOnlyStringWrapper( name );
    }

    @Override
    public String getNamespace() {
        return parent.getNamespace();
    }

    public OtmPropertyOwner getParent() {
        return parent;
    }


    public String getRole() {
        return "FIXME";
    }

    /**
     * Facet edit-ability is the ability to add/remove properties.
     * 
     * @see org.opentravel.model.OtmModelElement#isEditable()
     */
    @Override
    public boolean isEditable() {
        return getParent() != null ? getParent().isEditable() : false;
    }

    @Override
    public boolean isExpanded() {
        return true;
    }

    @Override
    public StringProperty descriptionProperty() {
        return getParent().descriptionProperty();
    }

    @Override
    public String fixName(String candidateName) {
        return getParent().fixName( candidateName );
    }

    @Override
    public OtmModelManager getModelManager() {
        return getParent() != null ? getParent().getModelManager() : null;
    }

    @Override
    public String getDeprecation() {
        return getParent().getDeprecation();
    }

    @Override
    public Tooltip getTooltip() {
        return getParent().getTooltip();
    }

    @Override
    public String getValidationFindingsAsString() {
        return getParent() != null ? getParent().getValidationFindingsAsString() : "";
    }

    @Override
    public void setExpanded(boolean flag) {
        if (getParent() != null)
            getParent().setExpanded( flag );
    }

    @Override
    public boolean isValid() {
        return getParent().isValid();
    }

    @Override
    public boolean isValid(boolean refresh) {
        return getParent() != null ? getParent().isValid( refresh ) : false;
    }

    @Override
    public void setDescription(String description) {
        getParent().setDescription( description );
    }

    @Override
    public String setName(String name) {
        return getParent().setName( name );
    }

    @Override
    public ImageView validationImage() {
        return getParent() != null ? getParent().validationImage() : null;
    }

    @Override
    public ObjectProperty<ImageView> validationImageProperty() {
        return getParent() != null ? getParent().validationImageProperty() : null;
    }

    @Override
    public StringProperty validationProperty() {
        return getParent() != null ? getParent().validationProperty() : new ReadOnlyStringWrapper( "" );
    }

    @Override
    public String setDeprecation(String deprecation) {
        return getParent().setDeprecation( deprecation );
    }

    @Override
    public String setExample(String value) {
        return getParent().setExample( value );
    }

    @Override
    public void clearNameProperty() {
        getParent().clearNameProperty();
    }

    @Override
    public OtmLibraryMember getOwningMember() {
        return getParent() != null ? getParent().getOwningMember() : null;
    }

    @Override
    public boolean isInherited() {
        // log.debug("Is " + this + " inherited? " + !getParent().contains(this));
        // if (getParent() instanceof OtmLibraryMember)
        // return !((OtmLibraryMember) getParent()).contains( this );
        return false;
    }

    // @Override
    // public boolean isNameControlled() {
    // return true;
    // }

    /**
     * {@inheritDoc}
     * <p>
     * Creates properties to represent facet children.
     */
    @Override
    public void modelChildren() {
        if (getTL() instanceof TLIndicatorOwner)
            ((TLIndicatorOwner) getTL()).getIndicators().forEach( p -> OtmPropertyFactory.create( p, this ) );
        if (getTL() instanceof TLAttributeOwner)
            ((TLAttributeOwner) getTL()).getAttributes().forEach( p -> OtmPropertyFactory.create( p, this ) );
        if (getTL() instanceof TLPropertyOwner)
            ((TLPropertyOwner) getTL()).getElements().forEach( p -> OtmPropertyFactory.create( p, this ) );
    }

    @Override
    public void modelInheritedChildren() {
        // // Only model once
        // if (inheritedChildren == null)
        // inheritedChildren = new ArrayList<>();
        // else
        // inheritedChildren.clear(); // RE-model
        // // return;
        //
        // // All properties, local and inherited
        // // List<TLProperty> inheritedElements = PropertyCodegenUtils.getInheritedProperties(getTL());
        //
        // // Get only the directly inherited properties
        // if (getOwningMember().getBaseType() != null && getTL() instanceof TLFacet) {
        // PropertyCodegenUtils.getInheritedFacetProperties( (TLFacet) getTL() )
        // .forEach( ie -> OtmPropertyFactory.create( ie, this ) );
        // PropertyCodegenUtils.getInheritedFacetAttributes( (TLFacet) getTL() )
        // .forEach( ie -> OtmPropertyFactory.create( ie, this ) );
        // PropertyCodegenUtils.getInheritedFacetIndicators( (TLFacet) getTL() )
        // .forEach( ie -> OtmPropertyFactory.create( ie, this ) );
        // }
    }

    @Override
    public boolean isRenameable() {
        return false;
    }

    @Override
    public void remove(OtmObject property) {
        parent.remove( property );
        // if (children.contains( property ))
        // children.remove( property );
        // if (inheritedChildren.contains( property ))
        // inheritedChildren.remove( property );
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getExample() {
        return "";
    }

    @Override
    public ValidationFindings getFindings() {
        return getParent() != null ? getParent().getFindings() : null;
    }

    @Override
    public Image getIcon() {
        return getParent() != null ? getParent().getIcon() : null;
    }

    @Override
    public OtmLibrary getLibrary() {
        return getParent() != null ? getParent().getLibrary() : null;
    }

    @Override
    public String getNameWithPrefix() {
        return "";
    }

    @Override
    public String getObjectTypeName() {
        return "";
    }

    @Override
    public String getPrefix() {
        return "";
    }

    @Override
    public TLModelElement getTL() {
        return getParent() != null ? getParent().getTL() : null;
    }

}
