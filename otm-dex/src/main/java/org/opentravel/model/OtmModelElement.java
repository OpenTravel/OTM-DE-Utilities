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
import org.opentravel.common.ValidationUtils;
import org.opentravel.dex.actions.DexActionManager;
import org.opentravel.dex.actions.DexActionManager.DexActions;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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
public abstract class OtmModelElement<T extends TLModelElement> implements OtmObject {
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

    protected T tlObject;

    // leave empty if object can have children but does not or has not been modeled yet.
    // leave null if the element can not have children.
    protected List<OtmObject> children = new ArrayList<>();
    private ValidationFindings findings = null;
    // Inherited children can not be inflated until after the model completes initial loading.
    // Use lazy inflation on the getter.
    protected List<OtmObject> inheritedChildren = null;

    private SimpleStringProperty nameProperty;
    private SimpleStringProperty descriptionProperty;
    private SimpleStringProperty validationProperty;
    private ObjectProperty<ImageView> validationImageProperty;

    // private DexActionManager actionMgr = null;

    // @Deprecated
    // public OtmModelElement(T tl, DexActionManager dexActionManager) {
    // this( tl );
    // }

    public OtmModelElement(T tl) {
        if (tl == null)
            throw new IllegalArgumentException( "Must have a tl element to create facade." );
        tlObject = tl;
        tl.addListener( new OtmModelElementListener( this ) );
        // checkListener();
    }

    // private void checkListener() {
    // for (ModelElementListener l : tlObject.getListeners())
    // if (l instanceof OtmModelElementListener)
    // assert this == ((OtmModelElementListener) l).get();
    // }

    @Override
    public StringProperty descriptionProperty() {
        if (descriptionProperty == null) {
            if (isEditable()) {
                descriptionProperty = new SimpleStringProperty( getDescription() );
                if (getActionManager() != null)
                    getActionManager().addAction( DexActions.DESCRIPTIONCHANGE, descriptionProperty(), this );
            } else {
                descriptionProperty = new ReadOnlyStringWrapper( getDescription() );
            }
        }
        return descriptionProperty;
    }

    @Override
    public DexActionManager getActionManager() {
        return getOwningMember().getActionManager();
        // return actionMgr;
    }

    @Override
    public String getDeprecation() {
        if (getTL() instanceof TLDocumentationOwner) {
            TLDocumentation doc = ((TLDocumentationOwner) getTL()).getDocumentation();
            if (doc != null && doc.getDeprecations() != null && !doc.getDeprecations().isEmpty())
                return doc.getDeprecations().get( 0 ).getText();
        }
        return "";
    }

    @Override
    public String getDescription() {
        if (getTL() instanceof TLDocumentationOwner) {
            TLDocumentation doc = ((TLDocumentationOwner) getTL()).getDocumentation();
            if (doc != null)
                return doc.getDescription();
        }
        return "";
    }

    @Override
    public String getExample() {
        if (getTL() instanceof TLExampleOwner) {
            List<TLExample> exs = ((TLExampleOwner) getTL()).getExamples();
            if (exs != null && !exs.isEmpty())
                return exs.get( 0 ).getValue();
        }
        return "";
    }

    @Override
    public ValidationFindings getFindings() {
        if (findings == null)
            isValid( true );
        return findings;
    }

    @Override
    public Image getIcon() {
        return ImageManager.getImage( this.getIconType() );
    }

    // @Override
    // public abstract ImageManager.Icons getIconType();

    /**
     * @return this library, owning library or null
     */
    @Override
    public OtmLibrary getLibrary() {
        return getOwningMember() != null ? getOwningMember().getLibrary() : null;
    }

    /**
     * Return the named entity's local name
     * 
     * @see org.opentravel.model.OtmObject#getName()
     */
    @Override
    public String getName() {
        return tlObject instanceof NamedEntity ? ((NamedEntity) tlObject).getLocalName() : NONAME;
    }

    @Override
    public String getNameWithPrefix() {
        return getPrefix() + ":" + getName();
    }

    @Override
    public String getNamespace() {
        return tlObject instanceof NamedEntity ? ((NamedEntity) tlObject).getNamespace() : NONAMESPACE;
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

    /**
     * @return
     */
    // FIXME
    @Override
    public String getRole() {
        return getClass().getSimpleName();
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
        String msg = "Validation Findings: \n";
        String f = ValidationUtils.getMessagesAsString( getFindings() );
        if (isInherited())
            msg += "Not validated here because it is inherited.";
        else if (!f.isEmpty())
            msg += f;
        else
            msg += "No warnings or errors.";
        return msg;
    }

    @Override
    public boolean isInherited() {
        return false; // Override for classes that can be inherited (facets, properties)
    }

    @Override
    public boolean isEditable() {
        return getOwningMember() != null && getOwningMember().isEditable();
    }

    @Override
    public boolean isValid() {
        return isValid( false );
    }

    @Override
    public boolean isValid(boolean refresh) {
        if (getTL() == null)
            throw new IllegalStateException( "Tried to validation with null TL object." );

        // Inherited objects should not be validated
        if (isInherited())
            return true;

        if (findings == null || refresh) {
            boolean deep = false;
            try {
                // Get new findings
                findings = TLModelCompileValidator.validateModelElement( getTL(), deep );
                // Update the validation properties
                if (validationProperty != null)
                    validationProperty.setValue( ValidationUtils.getCountsString( findings ) );
                if (validationImageProperty() != null)
                    validationImageProperty().setValue( validationImage() );
            } catch (Exception e) {
                log.debug( "Validation threw error: " + e.getLocalizedMessage() );
            }

            // log.debug(findings != null ? findings.count() + " findings found" : " null" + " findings found.");
        }
        return findings == null || findings.isEmpty();
    }

    /**
     * FX observable property for this name. The nameProperty will be updated by the {@link OtmModelElementListener}
     * when the value changes.
     * 
     * @return
     */
    @Override
    public StringProperty nameProperty() {
        if (nameProperty == null) {
            if (isEditable()) {
                nameProperty = new SimpleStringProperty( getName() );
                if (getActionManager() != null)
                    getActionManager().addAction( DexActions.NAMECHANGE, nameProperty, this );
            } else {
                nameProperty = new ReadOnlyStringWrapper( "" + getName() );
            }
        }
        return nameProperty;
    }

    @Override
    public void setDescription(String description) {
        if (getTL() instanceof TLDocumentationOwner) {
            TLDocumentation doc = ((TLDocumentationOwner) getTL()).getDocumentation();
            if (doc == null) {
                doc = new TLDocumentation();
                ((TLDocumentationOwner) getTL()).setDocumentation( doc );
            }
            doc.setDescription( description );
        }
        // ModelEvents are only thrown when the documentation element changes.
        descriptionProperty.setValue( description );
    }

    /**
     * Set the name if possible.
     * 
     * @param name
     * @return the actual name after assignment attempted
     */
    @Override
    public String setName(String name) {
        // NO-OP unless overridden
        // isValid(true);
        return getName();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public ImageView validationImage() {
        if (isInherited())
            return null;

        isValid();
        if (findings.hasFinding( FindingType.ERROR ))
            return ImageManager.get( ImageManager.Icons.V_ERROR );
        if (findings.hasFinding( FindingType.WARNING ))
            return ImageManager.get( ImageManager.Icons.V_WARN );
        return ImageManager.get( ImageManager.Icons.V_OK );
    }

    @Override
    public ObjectProperty<ImageView> validationImageProperty() {
        if (validationImageProperty == null)
            validationImageProperty = new SimpleObjectProperty<>( validationImage() );
        return validationImageProperty;
    }

    @Override
    public StringProperty validationProperty() {
        if (validationProperty == null)
            validationProperty = new ReadOnlyStringWrapper( ValidationUtils.getCountsString( getFindings() ) );
        return validationProperty;
    }
}
