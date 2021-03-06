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

import org.opentravel.common.ImageManager;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMemberType;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Interface for all OTM Facade objects which wrap all OTM libraries, objects, facets and properties.
 * 
 * @author Dave Hollander
 * 
 */
public interface OtmObject {

    /**
     * Simply set the name property to null.
     */
    void clearNameProperty();

    /**
     * @see DexActionManager#add(org.opentravel.dex.actions.DexActions, String, OtmObject)
     * @return FX property for deprecation or null
     */
    public StringProperty deprecationProperty();

    /**
     * @see DexActionManager#add(org.opentravel.dex.actions.DexActions, String, OtmObject)
     * @return FX property for description
     */
    public StringProperty descriptionProperty();

    /**
     * Apply any naming rules specific to the object type and return the corrected name.
     * 
     * @param candidateName
     * @return
     */
    public String fixName(String candidateName);

    /**
     * Get the active action manager for this object. Different action managers controls which set of actions are
     * enabled.
     * 
     * @return
     */
    public DexActionManager getActionManager();

    /**
     * @return the deprecation string from documentation or empty string
     */
    public String getDeprecation();

    /**
     * @return new list of all descendants if any
     */
    public List<OtmObject> getDescendants();

    public String getDescription();

    public String getExample();

    /**
     * Get the validation findings associated with this object. Lazy evaluated; if the findings were null validation
     * will be run.
     * 
     * @return
     */
    public ValidationFindings getFindings();

    /**
     * Get the image associated with type of object.
     * 
     * @return
     */
    public Image getIcon();

    public ImageManager.Icons getIconType();

    /**
     * @return this member's, or owning object's library from the TLLibrary or null
     */
    public OtmLibrary getLibrary();

    /**
     * @return the model manager for this object
     */
    public OtmModelManager getModelManager();

    /**
     * @return the named entity's local name
     * 
     **/
    public String getName();

    /**
     * @return the namespace associated with the owner's library.
     */
    public String getNamespace();

    /**
     * Get prefix from owning member's library + ":" + this object's name
     * 
     * @see {@link NamedEntity#getLocalName()}
     * 
     * @return prefix + ":" + name
     */
    public String getNameWithPrefix();

    /**
     * Get user display name of the object type. Display name is declared in the {@link OtmLibraryMemberType}
     * enumeration.
     * <p>
     * To use as a name, strip white space: <br>
     * {@code String typeName = member.getObjectTypeName().replaceAll( "\\s+", "" );}
     * 
     * @return display name for this type of object
     */
    public String getObjectTypeName();

    /**
     * @return the library member that owns this object. Library members return themselves.
     */
    public OtmLibraryMember getOwningMember();

    /**
     * @return the namespace prefix associated with the owner's library.
     */
    public String getPrefix();

    /**
     * Get the underlying TL element cast to its specific type.
     * 
     * @return the TL model element is object wraps.
     */
    public TLModelElement getTL();

    /**
     * @return FX Tooltip containing display string describing this object
     */
    public Tooltip getTooltip();

    /**
     * Findings will contain a header and either a message that the are no findings or the findings made into a single
     * string.
     * 
     * @return findings as a string
     */
    public String getValidationFindingsAsString();

    /**
     * @return true if deprecation string is not null and not empty.
     */
    boolean isDeprecated();

    /**
     * Is this object fully editable? Also see {@link OtmLibraryMember#isEditableMinor()}
     * 
     * @return true if this object is editable
     */
    public boolean isEditable();

    /**
     * Used by any view to remember if the object has been expanded.
     * 
     * @return
     */
    public boolean isExpanded();

    /**
     * Is the property inherited? Properties use the property specific TL Owner and compare with parent's TL object.
     * Contributed facets are inherited when their contributor does not point back to the contributed facet.
     * 
     * @return true if inherited as indicated by different owners
     */
    public boolean isInherited();

    /**
     * Is this object re-nameable and implement a setName() method that changes the underlying TL object?
     * <p>
     * <b>Note:</b> this is a property of the class and does not reflect if the type assigned is name controlled.
     * 
     * @see OtmTypeProvider#isNameControlled()
     * @return True unless overridden.
     */
    boolean isRenameable();

    /**
     * Are there any warnings or errors in the findings?
     * 
     * @return
     */
    public boolean isValid();

    /**
     * Are there any warnings or errors in the findings?
     * 
     * @param When force is true, run validation on all children and where used library members.
     * @return true if there are no warning or error findings
     */
    public boolean isValid(boolean refresh);

    /**
     * Get the FX observable property for this name. Intended for use where the name could be edited if enabled. Use
     * {@link #nameProperty()} if the name is simply for display.
     * <p>
     * The editing property may not contain the full name of the object. For example, contextual and extension point
     * facets the TL getName() used here is different than TL localName. Objects whose name has contributions from other
     * objects must override this method to create a property that has only the editable portion of the name.
     * <p>
     * If the object is editable, return a simple string property with a listener assigned by the object's action
     * manager. If not editable a read-only property is returned.
     * 
     * @see DexActionManager#add(org.opentravel.dex.actions.DexActions, String, OtmObject)
     * 
     * @return a string property (never null)
     */
    public StringProperty nameEditingProperty();

    /**
     * Create the nameEditingProperty FX string property and set its value. Used to override the default behavior of
     * using the same value as nameProperty().
     * 
     * @param editableName
     * @return
     */
    public StringProperty nameEditingProperty(String editableName);

    /**
     * Get the FX observable property for this name simply for display. By default, contains the value from
     * {@link #getName()}.
     * <p>
     * Use {@link #nameEditingProperty()} if intended for use where the name could be edited if enabled.
     * <p>
     * If editable, returns a simple string property with a listener assigned by the object's action manager. If not
     * editable a read-only property is returned.
     * 
     * @see DexActionManager#add(org.opentravel.dex.actions.DexActions, String, OtmObject)
     * @return a FX string property (never null)
     */
    public StringProperty nameProperty();

    /**
     * Remove any cached values. These include FX properties, children lists, validation results and other lazy
     * evaluated lists.
     * <p>
     * <b>Note: </b>Refresh will remove children including Contributed facets. Those will get recreated via lazy
     * evaluation but maybe be different instances.
     */
    public void refresh();

    /**
     * Set the deprecation documentation value. Even though the TL maintains a list, the facades only use the first
     * item.
     * 
     * @param deprecation set to this string. Remove deprecation if string is null or empty.
     * @return the deprecation string.
     */
    public String setDeprecation(String deprecation);

    public String setDescription(String description);

    /**
     * Set the example value. Even though the TL maintains a list, the facades only use the first item.
     * 
     * @param value to set the example. Remove example if string is null or empty.
     * @return the example string.
     */
    public String setExample(String value);

    /**
     * Used by any view to remember if the object has been expanded.
     * 
     * @param expanded will be set to flag.
     */
    public void setExpanded(boolean flag);

    /**
     * Set the name if possible.
     * 
     * @param name
     * @return the actual name after assignment attempted
     */
    public String setName(String name);

    /**
     * @return name with prefix or similar global identifier
     */
    @Override
    public String toString();

    /**
     * If the findings are null, returns null. Otherwise returns OK unless findings contains errors or warnings.
     * 
     * @return null, OK image view or error/warning image view
     */
    public ImageView validationImage();

    public ObjectProperty<ImageView> validationImageProperty();

    /**
     * @return a string wrapper around existing finding counts.
     */
    public StringProperty validationProperty();

}
