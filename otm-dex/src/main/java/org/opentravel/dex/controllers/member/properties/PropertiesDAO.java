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
import org.opentravel.dex.controllers.DexDAO;
import org.opentravel.dex.controllers.DexFilter;
import org.opentravel.dex.controllers.DexIncludedController;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmProperties.OtmElement;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.UserSelectablePropertyTypes;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
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

    static final String REQUIRED = "Required";
    static final String OPTIONAL = "Optional";

    protected OtmObject element;
    protected DexIncludedController<?> controller;
    protected boolean inherited; // contextual facets will not know if they are inherited, only the contributed facet
                                 // will know and it is not saved in the DAO.

    public PropertiesDAO(OtmFacet<?> property) {
        this.element = property;
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
        // log.debug("Created2 DAO for " + element + " Inherited? " + inherited);
    }

    public PropertiesDAO(OtmObject element, DexIncludedController<?> controller) {
        this.inherited = element.isInherited();
        this.element = element;
        // Save the contributor since the Contributed's children does not contain properties and contextual facets
        if (element instanceof OtmContributedFacet)
            this.element = ((OtmContributedFacet) element).getContributor();
        this.controller = controller;
        // log.debug("Created1 DAO for " + element + " Inherited? " + inherited);
    }

    /**
     * 
     * @return an observable list of property roles
     */
    public static ObservableList<String> getRoleList() {
        return UserSelectablePropertyTypes.getObservableList();
    }

    /**
     * 
     * @return an observable list of values for minimum repeat field
     */
    public static ObservableList<String> minList() {
        ObservableList<String> list = FXCollections.observableArrayList();
        list.add( OPTIONAL );
        list.add( REQUIRED );
        return list;
    }

    /**
     * If the property is a type user, create a simple string property with listener. Otherwise, create a read-only
     * property.
     * 
     * @return
     */
    public StringProperty assignedTypeProperty() {
        StringProperty ssp;
        if (element instanceof OtmTypeUser) {
            ssp = ((OtmTypeUser) element).assignedTypeProperty();
            // if (ssp instanceof SimpleStringProperty)
            // ssp.addListener((v, o, n) -> {
            // new AssignedTypesMenuHandler().handle(n, this);
            // controller.refresh();
            // });
        } else {
            ssp = new ReadOnlyStringWrapper( "" );
        }
        return ssp;
    }

    public StringProperty deprecationProperty() {
        String value = element.getDeprecation();

        if (element instanceof OtmFacet)
            return new ReadOnlyStringWrapper( "" );
        if (!element.isEditable())
            return new ReadOnlyStringWrapper( value );

        StringProperty desc = new SimpleStringProperty( value );
        // TODO - move to action handler
        desc.addListener( (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
            // element.setDesc(newValue);
            log.debug( "TODO: Set " + element + " deprecation to " + newValue );
        } );
        return desc;
    }

    public StringProperty descriptionProperty() {
        return element.descriptionProperty();
        // if (!(element.getTL() instanceof TLDocumentationOwner))
        // return new ReadOnlyStringWrapper("");
        //
        // String value = element.getDescription();
        // if (!element.isEditable())
        // return new ReadOnlyStringWrapper(value);
        //
        // StringProperty desc = new SimpleStringProperty(value);
        // // TODO - move to action handler
        // desc.addListener(
        // (ObservableValue<? extends String> ov, String oldValue, String newValue) -> setDescription(newValue));
        // return desc;
    }

    public void setDescription(String description) {
        element.setDescription( description );
        // log.debug("setDescription " + description + " on " + element);
    }

    public StringProperty exampleProperty() {
        String value = element.getExample();

        // Add empty for properties with complex types
        // if (element.isAssignedComplexType())
        if (element instanceof OtmFacet)
            return new ReadOnlyStringWrapper( "" );
        if (!element.isEditable())
            return new ReadOnlyStringWrapper( value );

        StringProperty desc = new SimpleStringProperty( value );
        // TODO - move to action handler
        desc.addListener( (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
            // element.setDesc(newValue);
            log.debug( "TODO: Set " + element + " example to " + newValue );
        } );
        return desc;
    }

    @Override
    public ImageView getIcon(ImageManager imageMgr) {
        return imageMgr.getView( element );
    }

    @Override
    public OtmObject getValue() {
        return element;
    }

    public DexIncludedController<?> getController() {
        return controller;
    }

    public boolean isEditable() {
        return element.isEditable();
    }

    public IntegerProperty maxProperty() {
        Integer value = -1;
        if (element instanceof OtmElement)
            value = ((OtmElement<?>) element).getTL().getRepeat();
        return new SimpleIntegerProperty( value );
        // TODO - add listener
    }

    public StringProperty minProperty() {
        if (!(element instanceof OtmProperty))
            return new ReadOnlyStringWrapper( "" );

        String value = OPTIONAL;
        if (((OtmProperty<?>) element).isManditory())
            value = REQUIRED;

        SimpleStringProperty ssp = new SimpleStringProperty( value );
        if (element.isEditable())
            // TODO - move to action handler
            ssp.addListener( (ObservableValue<? extends String> ov, String oldVal, String newVal) -> {
                ((OtmProperty<?>) element).setManditory( newVal.equals( REQUIRED ) );
                // log.debug("Set optional/manditory of " + element.getName() + " to " + newVal);
            } );

        return ssp;
    }

    public StringProperty nameProperty() {
        if (element.nameProperty() != null)
            return element.nameProperty();
        // if (element instanceof OtmProperty)
        // return ((OtmProperty<?>) element).nameProperty();
        else
            // TODO - have facet return property
            return new ReadOnlyStringWrapper( "" + element.getName() );
    }

    public StringProperty roleProperty() {
        StringProperty ssp;
        if (element instanceof OtmProperty) {
            ssp = new SimpleStringProperty( ((OtmProperty<?>) element).getRole() );
            // TODO - create action handler
            ssp.addListener( (ObservableValue<? extends String> ov, String oldVal, String newVal) -> {
                log.debug( "TODO - set role of " + element.getName() + " to " + newVal );
            } );
        } else {
            ssp = new ReadOnlyStringWrapper( "" );
        }
        return ssp;
    }

    public void setMax(String newValue) {
        log.debug( "TODO: Set max to: " + newValue );
    }

    public String getValidationFindingsAsString() {
        if (inherited)
            return "Not validated here because it is inherited.";
        else
            return element.getValidationFindingsAsString();
    }

    public ObjectProperty<ImageView> validationImageProperty() {
        if (inherited)
            return null;
        element.isValid(); // create findings if none existed
        return element.validationImageProperty();
    }

    @Override
    public String toString() {
        if (element instanceof OtmTypeUser && ((OtmTypeUser) element).getAssignedType() != null)
            return element.toString() + " -> " + ((OtmTypeUser) element).getAssignedType().getNameWithPrefix();
        return element.toString();
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
            ImageManager imageMgr = controller.getMainController().getImageManager();
            if (imageMgr != null) {
                ImageView graphic = imageMgr.getView( element );
                item.setGraphic( graphic );
                Tooltip.install( graphic, getTooltip() );
            }
        }
        return item;
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

            for (OtmObject child : member.getChildrenHierarchy()) {
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
     * @return true if the OtmProperty is inherited
     */
    public boolean isInherited() {
        return inherited;
    }

    public String getBaseTypeName() {
        if (isInherited() && element.getOwningMember() != null && element.getOwningMember().getBaseType() != null)
            return element.getOwningMember().getBaseType().getName();
        return "";
    }

    // ((TLProperty)tl).getDocumentation().addImplementer(implementer);(null);
    // ((TLProperty)tl).getDocumentation().addMoreInfo(moreInfo);(null);
    // ((TLProperty)tl).getDocumentation().addOtherDoc(otherDoc);(null);

}
