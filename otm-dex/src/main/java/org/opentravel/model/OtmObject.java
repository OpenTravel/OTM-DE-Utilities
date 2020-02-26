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
     * @return fx property for description
     */
    public StringProperty descriptionProperty();

    /**
     * Apply any naming rules specific to the object type and return the corrected name.
     * 
     * @param candidateName
     * @return
     */
    public String fixName(String candidateName);

    public DexActionManager getActionManager();

    public String getDeprecation();

    /**
     * @return new list of all descendants if any
     */
    public List<OtmObject> getDescendants();

    public String getDescription();

    public String getExample();

    public ValidationFindings getFindings();

    public Image getIcon();

    public ImageManager.Icons getIconType();

    /**
     * @return this member's, or owning object's library from the TLLibrary or null
     */
    public OtmLibrary getLibrary();

    public OtmModelManager getModelManager();

    /**
     * @return the named entity's local name
     * 
     **/
    public String getName();

    public String getNamespace();

    /**
     * @return
     */
    public String getNameWithPrefix();

    public String getObjectTypeName();

    public OtmLibraryMember getOwningMember();

    /**
     * 
     */
    public String getPrefix();

    public TLModelElement getTL();

    public Tooltip getTooltip();

    public String getValidationFindingsAsString();

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
     * 
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
     * @param force regenerating findings by validating with the compiler
     * @return
     */
    public boolean isValid(boolean refresh);

    /**
     * Get the FX observable property for this name. Intended for use where the name could be edited if enabled. Use
     * {@link #nameProperty()} if the name is simply for display.
     * <p>
     * If editable, returns a simple string property with a listener assigned by the object's action manager. If not
     * editable a read-only property is returned.
     * <p>
     * Objects whose name has contributions from other objects must override this method to create a property that has
     * only the editable portion of the name.
     * 
     * @return a string property (never null)
     */
    public StringProperty nameEditingProperty();

    /**
     * Create the nameEditingProperty string property and set its value. Used to override the default behavior of using
     * the same value as nameProperty().
     * 
     * @param editableName
     * @return
     */
    public StringProperty nameEditingProperty(String editableName);

    /**
     * Get the FX observable property for this name simply for display. Use {@link #nameEditingProperty()} if intended
     * for use where the name could be edited if enabled.
     * <p>
     * If editable, returns a simple string property with a listener assigned by the object's action manager. If not
     * editable a read-only property is returned.
     * 
     * @return a string property (never null)
     */
    public StringProperty nameProperty();

    /**
     * Remove any cached values. These include FX properties, children lists, validation results and other lazy
     * evaluated lists.
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

    public void setDescription(String description);

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

    @Override
    public String toString();

    public ImageView validationImage();

    public ObjectProperty<ImageView> validationImageProperty();

    public StringProperty validationProperty();
}
