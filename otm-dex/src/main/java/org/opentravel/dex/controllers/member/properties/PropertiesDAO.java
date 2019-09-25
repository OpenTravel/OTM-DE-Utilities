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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.ImageManager;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.dex.actions.ManditoryChangeAction;
import org.opentravel.dex.actions.PropertyRoleChangeAction;
import org.opentravel.dex.actions.SetRepeatCountAction;
import org.opentravel.dex.controllers.DexDAO;
import org.opentravel.dex.controllers.DexFilter;
import org.opentravel.dex.controllers.DexIncludedController;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmProperties.OtmElement;
import org.opentravel.model.otmProperties.OtmPropertyType;

import java.util.ArrayList;
import java.util.Collection;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

/**
 * Manage a facets and properties in a tree table.
 * 
 * @author dmh
 *
 */
public class PropertiesDAO implements DexDAO<OtmObject> {
    private static Log log = LogFactory.getLog( PropertiesDAO.class );

    protected OtmObject element;
    protected DexIncludedController<?> controller;
    // contextual facets will not know if they are inherited, only the contributed facet will know and it is not saved
    // in the DAO.
    protected boolean inherited;

    /**
     * 
     * @return an observable list of property roles
     */
    public static ObservableList<String> getRoleList() {
        ObservableList<String> list = FXCollections.observableArrayList();
        for (OtmPropertyType value : OtmPropertyType.values()) {
            list.add( value.label() );
        }
        return list;
    }

    /**
     * 
     * @return an observable list of values for minimum repeat field
     */
    public static ObservableList<String> minList() {
        ObservableList<String> list = FXCollections.observableArrayList();
        list.add( ManditoryChangeAction.OPTIONAL );
        list.add( ManditoryChangeAction.REQUIRED );
        return list;
    }


    public PropertiesDAO(OtmFacet<?> property) {
        this.element = property;
        // log.debug( "Created DAO for " + element + " Inherited? " + inherited );
    }

    public PropertiesDAO(OtmObject element, DexIncludedController<?> controller) {
        this.inherited = element.isInherited();
        this.element = element;
        // Save the contributor since the Contributed's children does not contain properties and contextual facets
        if (element instanceof OtmContributedFacet)
            this.element = ((OtmContributedFacet) element).getContributor();
        this.controller = controller;
        // log.debug( "Created1 DAO for " + element + " Inherited? " + inherited );
    }

    /**
     * 
     * @param element
     * @param controller
     * @param inherited Only the parent DAO of a child will know if the parent contextual facet is inherited.
     */
    public PropertiesDAO(OtmObject element, DexIncludedController<?> controller, TreeItem<PropertiesDAO> parent) {
        this( element, controller );
        if (!inherited && parent != null && parent.getValue() != null)
            this.inherited = parent.getValue().inherited;
        // log.debug( "Created2 DAO for " + element + " Inherited? " + inherited );
    }

    /**
     * If the property is a type user, create a simple string property with listener. Otherwise, create a read-only
     * property.
     * 
     * @return
     */
    public StringProperty assignedTypeProperty() {
        StringProperty ssp;
        if (element instanceof OtmTypeUser)
            ssp = ((OtmTypeUser) element).assignedTypeProperty();
        else
            ssp = new ReadOnlyStringWrapper( "" );
        return ssp;
    }

    /**
     * Add tree items to parent for each descendant of the child owner.
     * 
     * @param filter a filter that will exclude some members, returning a null item. Can be null for no filter.
     * @param member a child owning library member. Non-child owning properties are ignored.
     */
    public void createChildrenItems(TreeItem<PropertiesDAO> parent, DexFilter<OtmObject> filter) {
        OtmChildrenOwner member = null;

        if (element instanceof OtmChildrenOwner) {
            // Skip over the parent

            // create cells for member's facets and properties
            member = (OtmChildrenOwner) element;

            // Create a local copy to prevent concurrent modification
            Collection<OtmObject> kids = new ArrayList<>( member.getChildrenHierarchy() );
            for (OtmObject child : kids) {
                // Create item and add to tree at parent
                TreeItem<PropertiesDAO> item =
                    new PropertiesDAO( child, getController(), parent ).createTreeItem( parent, filter );

                // Recurse to Create tree items for children if any
                if (child instanceof OtmChildrenOwner) {
                    // If the item was filtered out, continue using the parent for the tree item
                    if (item == null)
                        item = parent;
                    // TO DO - sort order
                    new PropertiesDAO( child, getController(), item ).createChildrenItems( item, filter );
                }
            }
        }

    }

    /**
     * Create a tree item for this DAO's element and add to parent. No business logic.
     * 
     * @param parent to add item as child
     * @return
     */
    public TreeItem<PropertiesDAO> createTreeItem(TreeItem<PropertiesDAO> parent, DexFilter<OtmObject> filter) {
        // Apply Filter (if any)
        if (filter != null && !filter.isSelected( element ))
            return null;

        TreeItem<PropertiesDAO> item = new TreeItem<>( this );
        if (element instanceof OtmChildrenOwner)
            item.setExpanded( ((OtmChildrenOwner) element).isExpanded() );
        if (parent != null)
            parent.getChildren().add( item );

        // Decorate if possible
        if (controller != null && controller.getMainController() != null) {
            ImageView graphic = ImageManager.get( element );
            item.setGraphic( graphic );
            Tooltip.install( graphic, getTooltip() );
        }
        return item;
    }

    public StringProperty deprecationProperty() {
        return element.getActionManager().add( DexActions.DEPRECATIONCHANGE, element.getDeprecation(), element );
    }

    public StringProperty descriptionProperty() {
        return element.descriptionProperty();
    }

    public StringProperty exampleProperty() {
        return element.getActionManager().add( DexActions.EXAMPLECHANGE, element.getExample(), element );
    }

    public String getBaseTypeName() {
        if (isInherited() && element.getOwningMember() != null && element.getOwningMember().getBaseType() != null)
            return element.getOwningMember().getBaseType().getName();
        return "";
    }

    public DexIncludedController<?> getController() {
        return controller;
    }

    @Override
    public ImageView getIcon(ImageManager imageMgr) {
        return ImageManager.get( element );
    }

    protected Tooltip getTooltip() {
        Tooltip tip = null;
        if (isInherited()) {
            if (getBaseTypeName().isEmpty())
                tip = new Tooltip( element.getObjectTypeName() + " inherited" );
            else
                tip = new Tooltip( element.getObjectTypeName() + " inherited from " + getBaseTypeName() );
        } else {
            tip = new Tooltip( element.getObjectTypeName() );
        }
        return tip;
    }

    public String getValidationFindingsAsString() {
        if (inherited)
            return "Not validated here because it is inherited.";
        else
            return element.getValidationFindingsAsString();
    }

    @Override
    public OtmObject getValue() {
        return element;
    }

    public boolean isEditable() {
        return element.isEditable();
    }

    /**
     * @return true if the OtmProperty is inherited
     */
    public boolean isInherited() {
        return inherited;
    }

    public IntegerProperty maxProperty() {
        // If there are other integer properties, move this into action manager as another action sub-type like boolean
        // and string
        IntegerProperty property = null;
        Integer value = -1;
        if (element instanceof OtmElement)
            value = ((OtmElement<?>) element).getRepeatCount();
        if (SetRepeatCountAction.isEnabled( element )) {
            property = new SimpleIntegerProperty( value );
            property.addListener( (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                element.getActionManager().run( DexActions.SETREPEATCOUNT, element, newValue );
            } );
        } else {
            property = new ReadOnlyIntegerWrapper( value );
        }
        return property;
    }

    public StringProperty minProperty() {
        return element.getActionManager().add( DexActions.MANDITORYCHANGE, ManditoryChangeAction.getCurrent( element ),
            element );
    }

    /**
     * Name property from otmObject. If editable, it will have listener.
     * 
     * @return
     */
    public StringProperty nameProperty() {
        return element.nameProperty();
    }

    public StringProperty roleProperty() {
        String current = PropertyRoleChangeAction.getCurrent( element );
        return element.getActionManager().add( DexActions.PROPERTYROLECHANGE, current, element );
    }

    public void setMax(String newValue) {
        log.debug( "TODO: Set max to: " + newValue );
    }

    @Override
    public String toString() {
        if (element instanceof OtmTypeUser && ((OtmTypeUser) element).getAssignedType() != null)
            return element.toString() + " -> " + ((OtmTypeUser) element).getAssignedType().getNameWithPrefix();
        return element.toString();
    }

    public ObjectProperty<ImageView> validationImageProperty() {
        if (inherited)
            return null;
        element.isValid(); // create findings if none existed
        return element.validationImageProperty();
    }

    // ((TLProperty)tl).getDocumentation().addImplementer(implementer);(null);
    // ((TLProperty)tl).getDocumentation().addMoreInfo(moreInfo);(null);
    // ((TLProperty)tl).getDocumentation().addOtherDoc(otherDoc);(null);

}
