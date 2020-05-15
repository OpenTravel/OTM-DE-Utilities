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

package org.opentravel.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.ImageManager;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Abstract base for OTM Facade objects which wrap all OTM libraries, objects, facets and properties.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class OtmModelElement<T extends TLModelElement> implements OtmObject, Comparable<OtmObject> {
    private static Log log = LogFactory.getLog( OtmModelElement.class );

    private static final String NONAMESPACE = "no-namespace-for-for-this-object";
    private static final String NONAME = "no-name-for-for-this-object";

    /**
     * Utility to <i>get</i> the OTM facade object that wraps the TL Model object. Uses the listener added to all TL
     * objects in the facade's constructor.
     * 
     * @param tlObject the wrapped TLModelElement. Can be null.
     * @return otm facade wrapper or null if no listener found.
     */
    public static OtmObject get(TLModelElement tlObject) {
        if (tlObject != null)
            for (ModelElementListener l : tlObject.getListeners())
                if (l instanceof OtmModelElementListener) {
                    OtmObject otm = ((OtmModelElementListener) l).get();
                    // Contextual facets will have two listeners, return just the contextual facet
                    if (otm instanceof OtmContributedFacet)
                        continue;
                    return otm;
                }
        return null;
    }


    public static ValidationFindings isValid(TLModelElement tl) {
        return OtmValidationHandler.isValid( tl );
    }

    protected T tlObject;
    // leave empty if object can have children but does not or has not been modeled yet.
    // leave null if the element can not have children.
    protected List<OtmObject> children = new ArrayList<>();

    // Inherited children can not be inflated until after the model completes initial loading.
    // Use lazy inflation on the getter.
    protected List<OtmObject> inheritedChildren = null;
    // JavaFX Properties
    protected StringProperty nameProperty;
    protected StringProperty nameEditingProperty;
    private OtmDocHandler docHandler;
    private OtmValidationHandler validationHandler;
    private boolean expanded = false;

    /**
     * Construct model element. Set its TL object and add a listener.
     * 
     * @param tl
     */
    public OtmModelElement(T tl) {
        if (tl == null)
            throw new IllegalArgumentException( "Must have a tl element to create facade." );
        tlObject = tl;
        addListener();
        docHandler = new OtmDocHandler( this );
        validationHandler = new OtmValidationHandler( this );
    }

    /**
     * Add a OtmModelElement listener if it does not already have one.
     */
    private void addListener() {
        for (ModelElementListener l : tlObject.getListeners())
            if (l instanceof OtmModelElementListener)
                return;
        tlObject.addListener( new OtmModelElementListener( this ) );
    }

    @Override
    public void clearNameProperty() {
        nameProperty = null;
        nameEditingProperty = null;
    }

    @Override
    public int compareTo(OtmObject o) {
        if (o == null || o.getName() == null)
            return 1;
        if (getName() == null)
            return -1;
        if (getName().equals( o.getName() ))
            return getNamespace().compareTo( o.getNamespace() );
        return getName().compareTo( o.getName() );
    }

    @Override
    public StringProperty deprecationProperty() {
        return docHandler.deprecationProperty();
    }

    @Override
    public StringProperty descriptionProperty() {
        return docHandler.descriptionProperty();
    }

    public StringProperty exampleProperty() {
        return docHandler.exampleProperty();
    }

    /**
     * {@inheritDoc} Unless overridden, apply the initial capital rule.
     */
    @Override
    public String fixName(String name) {
        return name.substring( 0, 1 ).toUpperCase() + name.substring( 1 );

    }

    @Override
    public DexActionManager getActionManager() {
        // assert getOwningMember() != null;
        return getOwningMember() != null ? getOwningMember().getActionManager() : null;
    }

    @Override
    public String getDeprecation() {
        return docHandler.getDeprecation();
    }

    @Override
    public synchronized List<OtmObject> getDescendants() {
        List<OtmObject> objects = new ArrayList<>();
        List<OtmObject> kids = new ArrayList<>();
        if (this instanceof OtmChildrenOwner)
            kids.addAll( ((OtmChildrenOwner) this).getChildren() );

        for (OtmObject child : kids) {
            objects.add( child );
            if (child instanceof OtmChildrenOwner) {
                // Recurse
                objects.addAll( child.getDescendants() );
            }
        }
        return objects;
    }

    @Override
    public String getDescription() {
        return docHandler.getDescription();
    }

    @Override
    public String getExample() {
        return docHandler.getExample();
    }

    @Override
    public ValidationFindings getFindings() {
        return validationHandler.getFindings();
    }

    @Override
    public Image getIcon() {
        return ImageManager.getImage( this.getIconType() );
    }

    @Override
    public OtmLibrary getLibrary() {
        return getOwningMember() != null ? getOwningMember().getLibrary() : null;
    }

    @Override
    public OtmModelManager getModelManager() {
        if (getOwningMember() != null)
            return getOwningMember().getModelManager();
        return null;
    }

    @Override
    public String getName() {
        return tlObject instanceof NamedEntity ? ((NamedEntity) tlObject).getLocalName() : NONAME;
    }

    @Override
    public String getNamespace() {
        return tlObject instanceof NamedEntity ? ((NamedEntity) tlObject).getNamespace() : NONAMESPACE;
    }

    @Override
    public String getNameWithPrefix() {
        return getPrefix() + ":" + getName();
    }

    // Should be overridden
    @Override
    public String getObjectTypeName() {
        return getClass().getSimpleName();
    }

    @Override
    public String getPrefix() {
        return getOwningMember() != null && getOwningMember().getLibrary() != null
            ? getOwningMember().getLibrary().getPrefix() : "---";
    }

    @Override
    public T getTL() {
        return tlObject;
    }

    // All objects should override with their own TOOLTIP
    // When done, make this abstract or delete it
    @Override
    public Tooltip getTooltip() {
        return new Tooltip( "" );
    }

    @Override
    public String getValidationFindingsAsString() {
        return validationHandler.getValidationFindingsAsString();
    }

    @Override
    public boolean isDeprecated() {
        return docHandler.isDeprecated();
    }

    @Override
    public boolean isEditable() {
        return getOwningMember() != null && getOwningMember().isEditable();
    }

    @Override
    public boolean isExpanded() {
        return expanded;
    }

    @Override
    public boolean isInherited() {
        return false; // Override for classes that can be inherited (facets, properties)
    }

    @Override
    public boolean isRenameable() {
        return true;
    }

    @Override
    public boolean isValid() {
        return validationHandler.isValid( false );
    }

    @Override
    public boolean isValid(boolean refresh) {
        return validationHandler.isValid( refresh );
    }

    @Override
    public StringProperty nameEditingProperty() {
        if (nameEditingProperty == null)
            nameEditingProperty = setNameProperty( getName() );
        return nameEditingProperty;
    }

    @Override
    public StringProperty nameEditingProperty(String editableName) {
        nameEditingProperty = setNameProperty( editableName );
        return nameEditingProperty;
    }

    @Override
    public StringProperty nameProperty() {
        if (nameProperty == null)
            nameProperty = setNameProperty( getName() );
        return nameProperty;
    }

    @Override
    public void refresh() {
        docHandler.refresh();
        validationHandler.refresh();
        if (children != null)
            children.clear();
        if (inheritedChildren != null)
            inheritedChildren.clear();
        nameProperty = null;
        nameEditingProperty = null;
        // log.debug( "Refreshed " + getName() );
    }

    @Override
    public String setDeprecation(String deprecation) {
        return docHandler.setDeprecation( deprecation );
    }

    @Override
    public String setDescription(String description) {
        return docHandler.setDescription( description );
    }

    @Override
    public String setExample(String value) {
        return docHandler.setExample( value );
    }

    @Override
    public void setExpanded(boolean flag) {
        expanded = flag;
    }

    @Override
    public String setName(String name) {
        // NO-OP unless overridden
        // isValid(true);
        return getName();
    }

    private StringProperty setNameProperty(String value) {
        StringProperty property;
        if (getActionManager() != null && isRenameable())
            property = getActionManager().add( DexActions.NAMECHANGE, value, this );
        else
            property = new ReadOnlyStringWrapper( value );
        return property;
    }

    @Override
    public String toString() {
        return getNameWithPrefix();
    }

    @Override
    public ImageView validationImage() {
        return validationHandler.validationImage();
    }

    @Override
    public ObjectProperty<ImageView> validationImageProperty() {
        return validationHandler.validationImageProperty();
    }

    @Override
    public StringProperty validationProperty() {
        return validationHandler.validationProperty();
    }
}
